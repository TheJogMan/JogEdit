package jogEdit.operation.operations;

import jogEdit.*;
import jogEdit.commands.*;
import jogEdit.operation.*;
import jogEdit.region.*;
import jogUtil.commander.argument.*;

import java.util.*;

public abstract class UndoRedo extends AsyncRegionOperation
{
	final Wand.WandItem.TaskDestination destination;
	
	public UndoRedo(String name, String description, Wand.WandItem.TaskDestination destination)
	{
		super(name, description, true, false);
		this.destination = destination;
	}
	
	protected abstract UUID getID(Wand.WandItem wand);
	protected abstract void perform(Task.ChangedBlock change, BlockChanger changer);
	
	@Override
	public void runRegionOperation(AdaptiveInterpretation result, WandHoldingModule executor, Wand.WandItem wand, Region region, BlockChanger changer)
	{
		UUID id = getID(wand);
		if (id == null)
		{
			executor.respond("Nothing to " + this.name().toLowerCase() + ".");
			return;
		}
		if (!Task.exists(id))
		{
			executor.respond("That task was deleted and can't be " + this.name().toLowerCase() + "ne.");
			return;
		}
		
		Task.TaskReader reader = Task.get(id);
		executor.respond(this.name() + "ing " + reader.name());
		wand.beginTask(id, destination, this.name() + " " + reader.name());
		while (reader.hasNext() || !reader.atEnd())
		{
			Task.ChangedBlock change = reader.next();
			if (change != null)
				perform(change, changer);
		}
		changer.finish();
		executor.respond("Reverted " + changer.changedBlocks() + " blocks.");
		if (reader.sourceWasIncomplete())
			executor.respond("This task's file wasn't terminated properly, the operation may not have finished properly.");
		if (reader.encounteredException())
		{
			executor.respond("An exception occurred while trying to parse the task's file, check the server console for details.");
			reader.exception().printStackTrace();
		}
		wand.endTask(id);
	}
	
	public static class Undo extends UndoRedo
	{
		public Undo()
		{
			super("Undo", "Reverses the last task performed with this wand.", Wand.WandItem.TaskDestination.REDO);
		}
		
		@Override
		protected UUID getID(Wand.WandItem wand)
		{
			return wand.popFromUndo();
		}
		
		@Override
		protected void perform(Task.ChangedBlock change, BlockChanger changer)
		{
			change.undo(changer);
		}
	}
	
	public static class Redo extends UndoRedo
	{
		public Redo()
		{
			super("Redo", "Re-performs the last task undone with this wand.", Wand.WandItem.TaskDestination.UNDO);
		}
		
		@Override
		protected UUID getID(Wand.WandItem wand)
		{
			return wand.popFromRedo();
		}
		
		@Override
		protected void perform(Task.ChangedBlock change, BlockChanger changer)
		{
			change.redo(changer);
		}
	}
}