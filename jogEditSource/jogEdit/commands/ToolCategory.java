package jogEdit.commands;

import jogEdit.*;
import jogEdit.tool.*;
import jogUtil.commander.*;
import jogUtil.commander.argument.*;
import jogUtil.commander.command.*;
import jogUtil.data.*;
import jogUtil.richText.*;

import java.util.*;

public class ToolCategory extends Category
{
	public ToolCategory(Category parent)
	{
		super(parent, "Tool", "Tools.");
		
		new WandCommand.SimpleWandCommand(this, "Get", "Shows the currently set tool.",
		(AdaptiveInterpretation result, WandHoldingModule executor, Wand.WandItem wand) ->
		{
			executor.respond("Current Tool: " + wand.getToolType().name());
		});
		
		new WandCommand.SimpleWandCommand(this, "Set", "Sets the current tool.",
		AdaptiveArgumentList.create().addArgument(0, ToolBox.ToolArgument.class),
		(AdaptiveInterpretation result, WandHoldingModule executor, Wand.WandItem wand) ->
		{
			ToolType type = (ToolType)result.value()[0];
			wand.setTool(type, executor);
		});
		
		addContextFiller(new ContextualizedSettingCommandSource());
	}
	
	public static class ContextualizedSettingCommandSource implements ContextFiller
	{
		@Override
		public Collection<CommandComponent> getComponents(Executor executor)
		{
			WandHoldingModule module = executor.getModule(WandHoldingModule.class);
			Wand.WandItem[] wands = module.getWands();
			ArrayList<ToolType> tools = new ArrayList<>();
			for (Wand.WandItem wand : wands)
			{
				if (!tools.contains(wand.getToolType()))
					tools.add(wand.getToolType());
			}
			ArrayList<CommandComponent> collector = new ArrayList<>();
			for (ToolType tool : tools)
			{
				if (tool.properties().size() > 0)
					collector.add(new ToolSettingsCategory(module, tool));
			}
			return collector;
		}
	}
	
	public static class ToolSettingsCategory extends Category
	{
		final ToolType tool;
		
		public ToolSettingsCategory(WandHoldingModule executor, ToolType tool)
		{
			super(tool.name(), "Settings for the " + tool.name() + " tool.");
			this.tool = tool;
			
			Category setters = new Category(this, "Set", "Set values.");
			Category getters = new Category(this, "Get", "Get Values.");
			
			tool.properties().forEach(property ->
			{
				new ToolPropertyCommand(setters, tool, property, false);
				new ToolPropertyCommand(getters, tool, property, true);
			});
			
			new ToolDescriptionCommand(this, tool);
			new ResetCommand(this, tool);
		}
		
		public static class ResetCommand extends WandCommand
		{
			final ToolType type;
			
			public ResetCommand(Category parent, ToolType type)
			{
				super(parent, "Reset", "Reset setting values.");
				this.type = type;
				setDefaultListDescription("Reset all settings.");
				addArgumentList("Reset one setting.");
				addArgument(1, PropertyArgument.class, "Property Name", new Object[] {type});
			}
			
			@Override
			public void wandExecute(AdaptiveInterpretation result, WandHoldingModule executor, Wand.WandItem wand)
			{
				if (result.listNumber() == 0)
				{
					wand.resetToolProperties(type);
					executor.respond("All properties reset.");
				}
				else
					reset(type, (ToolType.Property<?>)result.value()[0], executor, wand);
			}
			
			static <PropertyType> void reset(ToolType type, ToolType.Property<PropertyType> property, WandHoldingModule executor, Wand.WandItem wand)
			{
				Tool tool = ToolBox.getTool(type, wand);
				Value<PropertyType, PropertyType> oldValue = property.getValue(tool);
				Value<PropertyType, PropertyType> newValue = property.defaultValue();
				RichStringBuilder builder = RichStringBuilder.start();
				builder.style(builder.style().color(RichColor.ORANGE));
				builder.append(property.name() + " has been changed from ");
				builder.append(oldValue.toString(), builder.style().color(RichColor.WHITE));
				builder.append(" to ");
				builder.append(newValue.toString(), builder.style().color(RichColor.WHITE));
				
				property.reset(tool);
				
				executor.respond(builder.build());
			}
		}
		
		public static class ToolDescriptionCommand extends WandCommand
		{
			final ToolType tool;
			
			public ToolDescriptionCommand(Category parent, ToolType tool)
			{
				super(parent, "Describe", "Provides a description of the " + tool.name() + " tool, and its current settings on your wand.");
				this.tool = tool;
			}
			
			@Override
			public void wandExecute(AdaptiveInterpretation result, WandHoldingModule executor, Wand.WandItem wand)
			{
				Tool tool = ToolBox.getTool(this.tool, wand);
				RichStringBuilder builder = RichStringBuilder.start();
				builder.style(builder.style().color(RichColor.ORANGE));
				builder.append("Description:");
				builder.newLine();
				builder.append(this.tool.description());
				builder.newLine();
				builder.append("Properties:");
				this.tool.properties().forEach(property ->
				{
					builder.newLine();
					builder.append(" " + property.name());
					builder.append(": ");
					builder.append(property.getValue(tool).toString(), builder.style().color(RichColor.AQUA));
					builder.append(" - ");
					builder.append(property.description());
				});
				executor.respond(builder.build());
			}
		}
		
		public static class ToolPropertyCommand extends WandCommand
		{
			final ToolType tool;
			final ToolType.Property<Object> property;
			final boolean getter;
			
			public ToolPropertyCommand(Category parent, ToolType tool, ToolType.Property<?> property, boolean getter)
			{
				super(parent, property.name(), property.description());
				this.property = (ToolType.Property<Object>)property;
				this.tool = tool;
				this.getter = getter;
				if (!getter)
					addArgument(property.valueType().typeClass());
			}
			
			@Override
			public void wandExecute(AdaptiveInterpretation result, WandHoldingModule executor, Wand.WandItem wand)
			{
				Tool tool = ToolBox.getTool(this.tool, wand);
				if (getter)
				{
					RichStringBuilder builder = RichStringBuilder.start();
					builder.style(builder.style().color(RichColor.ORANGE));
					builder.append(property.name() + " is currently ");
					builder.append(property.getValue(tool).toString(), builder.style().color(RichColor.WHITE));
					builder.newLine();
					builder.append("Default value would be ");
					builder.append(property.defaultValue().toString(), builder.style().color(RichColor.WHITE));
					executor.respond(builder.build());
				}
				else
				{
					Value<Object, Object> oldValue = property.getValue(tool);
					Object value = result.value()[0];
					property.set(tool, value);
					Value<Object, Object> newValue = property.getValue(tool);
					RichStringBuilder builder = RichStringBuilder.start();
					builder.style(builder.style().color(RichColor.ORANGE));
					builder.append(property.name() + " has been changed from ");
					builder.append(oldValue.toString(), builder.style().color(RichColor.WHITE));
					builder.append(" to ");
					builder.append(newValue.toString(), builder.style().color(RichColor.WHITE));
					executor.respond(builder.build());
				}
			}
		}
	}
}