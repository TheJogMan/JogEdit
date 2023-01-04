package jogEdit.operation;

import jogEdit.commands.*;
import jogEdit.operation.operations.*;
import jogEdit.region.*;
import jogLib.*;
import jogLib.values.*;
import jogUtil.*;
import jogUtil.commander.argument.*;
import jogUtil.commander.command.*;
import jogUtil.richText.*;
import org.bukkit.*;

import java.util.*;

public class OperationBox
{
	static final HashMap<String, Operation> operations = new HashMap<>();
	public static final OperationCategory operationCategory = new OperationCategory();
	
	static Result register(Operation operation)
	{
		if (operations.containsValue(operation))
			return new Result("That operation has already been registered.");
		else if (operations.containsKey(operation.name()))
			return new Result("An Operation has already been registered with that name.");
		operations.put(operation.name(), operation);
		
		Result result = operationCategory.add(operation);
		if (!result.success())
			return new Result(RichStringBuilder.start().append("Could not add operation command: ").append(result.description()).build());
		
		return new Result(true);
	}
	
	public static class OperationCategory extends Category
	{
		OperationCategory()
		{
			super(JogLib.commandConsole, "Operation");
			addTransformer(new WandHoldingModule.WandHoldingExecutorTransformer());
		}
		
		public Result add(CommandComponent component)
		{
			return this.addComponent(component);
		}
	}
	
	public static Operation getType(String name)
	{
		return operations.get(name);
	}
	
	public static List<Operation> operations()
	{
		return new ArrayList<>(operations.values());
	}
	
	public static void addOperations()
	{
		new UndoRedo.Undo();
		new UndoRedo.Redo();
		
		new AsyncRegionOperation.SimpleAsyncRegionOperation("Replace", "Replaces blocks of a given material in a region with a given material.", (result, executor, wand, region, changer) ->
		{
			Material template = (Material)result.value()[0];
			Material material = (Material)result.value()[1];
			region.forEach(block ->
			{
				if (block.getType().equals(template))
					changer.change(block, material);
			});
			changer.finish();
			executor.respond("Replaced " + changer.changedBlocks() + " blocks.");
		}, (new AdaptiveArgumentList()).addArgument(0, MaterialValue.class, new Object[] {true}).addArgument(0, MaterialValue.class, new Object[] {true, MaterialValue.BoundingFormat.SPACE_TERMINATED}));
		
		new AsyncRegionOperation.SimpleAsyncRegionOperation("Fill", "Fills a region with the given material.", (result, executor, wand, region, changer) ->
		{
			Material material = (Material)result.value()[0];
			region.forEach(block -> changer.change(block, material));
			changer.finish();
			executor.respond("Filled " + changer.changedBlocks() + " blocks.");
		}, (new AdaptiveArgumentList()).addArgument(0, MaterialValue.class, new Object[] {true, MaterialValue.BoundingFormat.SPACE_TERMINATED}));
		
		new Operation.SimpleOperation("Clear", "Clears the wands region selection.", (result, executor, wand, changer) ->
		{
			wand.clearSelection();
			executor.respond("Selection cleared.");
		}, false);
		
		new Operation.SimpleOperation("SetCorner", "Sets a corner of the wands region selection.", (result, executor, wand, changer) ->
		{
			Location location = null;
			if (result.listNumber() == 0)
				location = executor.getLocation();
			else if (result.listNumber() == 1)
				location = (Location)result.value()[1];
			wand.setCorner((Boolean)result.value()[0], location, executor);
		}, (new AdaptiveArgumentList()).addArgument(0, CornerArgument.class)
							.addList().addArgument(1, CornerArgument.class).addArgument(1, LocationValue.class), false);
	}
}