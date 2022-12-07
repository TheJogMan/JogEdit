package jogEdit.commands;

import jogEdit.*;
import jogEdit.operation.*;
import jogUtil.commander.argument.*;
import jogUtil.commander.argument.arguments.*;
import jogUtil.commander.command.*;
import jogUtil.data.values.*;
import jogUtil.richText.*;

public class WandCategory extends Category
{
	final Category task;
	final ToolCategory tool;
	
	public WandCategory()
	{
		super(null, "Wand", new RichString("Wand Commands."));
		OperationBox.operationCategory.add(this);
		
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
			int index = category.index();
			executor.respond("you tried to view " + category.get() + " tasks (" + index + ") but this command isn't done yet.");
		});
	}
}