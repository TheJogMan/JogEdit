package jogEdit.commands;

import jogEdit.*;
import jogLib.*;
import jogUtil.commander.argument.*;
import jogUtil.commander.argument.arguments.*;
import jogUtil.commander.command.*;
import jogUtil.data.*;
import jogUtil.data.values.*;
import jogUtil.indexable.*;
import jogUtil.richText.*;

import java.io.*;

public class WandCategory extends Category
{
	final Category task;
	final ToolCategory tool;
	
	public WandCategory()
	{
		super(JogLib.commandConsole, "Wand", new RichString("Wand Commands."));
		addTransformer(new WandHoldingModule.WandHoldingExecutorTransformer());
		
		tool = new ToolCategory(this);
		task = new Category(this, "Task", "Current and Past tasks ran with this wand.");
		
		new WandCommand.SimpleWandCommand(this, "SetPhysics", "Sets whether physics should be calculated while this wand changes blocks.",
		AdaptiveArgumentList.create().addArgument(0, BooleanValue.class),
		(AdaptiveInterpretation result, WandHoldingModule executor, Wand.WandItem wand) ->
		{
			wand.setDoPhysics((boolean)result.value()[0]);
		});
		
		new WandCommand.SimpleWandCommand(this, "SetIgnoreAir", "Sets whether air should be ignored while this wand copies blocks.",
		AdaptiveArgumentList.create().addArgument(0, BooleanValue.class),
		(AdaptiveInterpretation result, WandHoldingModule executor, Wand.WandItem wand) ->
		{
			wand.setIgnoreAir((boolean)result.value()[0]);
		});
		
		new WandCommand.SimpleWandCommand(this, "Describe", "Describes the current settings of the wand.",
		(AdaptiveInterpretation result, WandHoldingModule executor, Wand.WandItem wand) ->
		{
			wand.describe(executor);
		});
		
		new WandCommand.SimpleWandCommand(task, "ClearHistory", "Clears this wands task history",
		(AdaptiveInterpretation result, WandHoldingModule executor, Wand.WandItem wand) ->
		{
			wand.clearTaskHistory();
			executor.respond("Task history has been cleared.");
		});
		
		new WandCommand.SimpleWandCommand(task, "View", "Views tasks ran with this wand.",
		AdaptiveArgumentList.create().addArgument(0, TokenArgument.class, "Category", new String[] {"Active", "Redo", "Undo"}),
		(AdaptiveInterpretation result, WandHoldingModule executor, Wand.WandItem wand) ->
		{
			TokenArgument.Token category = (TokenArgument.Token)result.value()[0];
			
			Data wandData = wand.getData();
			if (category.index() == 0)
			{
				ListValue<DataValue> tasks = wandData.getObject("Active Tasks", new ListValue<>(TypeRegistry.get(DataValue.class)));
				StringBuilder builder = new StringBuilder("Active Tasks:");
				if (tasks.size() > 0)
				{
					for (DataValue dataValue : tasks)
					{
						Data task = dataValue.get();
						builder.append("\n ").append(task.getValue("Name", new StringValue("Name missing.")));
					}
				}
				else
					builder.append("\n None.");
				executor.respond(builder.toString());
			}
			else if (category.index() == 1 || category.index() == 2)
			{
				ListValue<UUIDValue> stack;
				StringBuilder builder = new StringBuilder();
				if (category.index() == 1)
				{
					stack = wandData.getObject("Redo Stack", new ListValue<>(TypeRegistry.get(UUIDValue.class)));
					builder.append("Redo Queue:");
				}
				else
				{
					stack = wandData.getObject("Undo Stack", new ListValue<>(TypeRegistry.get(UUIDValue.class)));
					builder.append("Undo Queue:");
				}
				
				if (stack.size() > 0)
				{
					for (UUIDValue id : stack)
					{
						builder.append("\n ");
						try
						{
							String name = Task.readString(new IndexableInputStream(new FileInputStream(Task.getFile(id.get()))).iterator());
							builder.append(name);
						}
						catch (FileNotFoundException e)
						{
							builder.append("Could not read name.");
						}
					}
				}
				else
					builder.append("\n None.");
				
				executor.respond(builder.toString());
			}
		});
		
		new WandCommand.SimpleWandCommand(this, "DataDump", "Debug command which spams you with all of this wand's settings/data.",
		AdaptiveArgumentList.create(),
		(AdaptiveInterpretation result, WandHoldingModule executor, Wand.WandItem wand) ->
		{
			try
			{
				FileOutputStream outputStream = new FileOutputStream("WandDump");
				outputStream.write(wand.getData().toByteData());
				outputStream.close();
			}
			catch (IOException ignored)
			{
			
			}
		});
	}
}