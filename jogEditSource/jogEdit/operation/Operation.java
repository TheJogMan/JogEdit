package jogEdit.operation;

import jogEdit.*;
import jogEdit.commands.*;
import jogEdit.tool.*;
import jogUtil.*;
import jogUtil.commander.argument.*;
import jogUtil.richText.*;

public abstract class Operation extends WandCommand
{
	final boolean announceDuration;
	
	public Operation(String name, String description, boolean announceDuration, boolean pushToUndoStack)
	{
		this(name, new RichString(description), announceDuration, pushToUndoStack);
	}
	
	public Operation(String name, String description, boolean announceDuration)
	{
		this(name, new RichString(description), announceDuration, true);
	}
	
	public Operation(String name, String description)
	{
		this(name, new RichString(description), true, true);
	}
	
	public Operation(String name, RichString description)
	{
		this(name, description, true, true);
	}
	
	public Operation(String name, RichString description, boolean announceDuration)
	{
		this(name, description, announceDuration, true);
	}
	
	final boolean pushToUndoStack;
	
	public Operation(String name, RichString description, boolean announceDuration, boolean pushToUndoStack)
	{
		super(null, name, description);
		this.pushToUndoStack = pushToUndoStack;
		this.announceDuration = announceDuration;
		Result result = OperationBox.register(this);
		if (!result.success())
			System.err.println("Could not register operation \"" + name + "\": " + result.description().encode(EncodingType.CODED));
	}
	
	public abstract void runOperation(AdaptiveInterpretation result, WandHoldingModule executor, Wand.WandItem wand, BlockChanger blockChanger);
	
	void handle(AdaptiveInterpretation result, WandHoldingModule executor, Wand.WandItem wand, String name)
	{
		long start = System.currentTimeMillis();
		Task.TaskWriter writer = pushToUndoStack ? Task.create(name) : null;
		if (writer != null)
			wand.beginTask(writer.id(), Wand.WandItem.TaskDestination.UNDO, writer.name());
		BlockChanger changer = new BlockChanger(wand.doPhysics(), executor, RegionBehavior.CONTAIN, wand.getRegion(), name, writer);
		runOperation(result, executor, wand, changer);
		if (writer != null)
		{
			wand.endTask(writer.id());
			writer.close();
		}
		long duration = System.currentTimeMillis() - start;
		if (announceDuration)
			executor.respond(duration + "ms elapsed.");
	}
	
	@Override
	public final void wandExecute(AdaptiveInterpretation result, WandHoldingModule executor, Wand.WandItem wand)
	{
		handle(result, executor, wand, name());
	}
	
	public static class SimpleOperation extends Operation
	{
		public static interface OperationExecutor
		{
			void execute(AdaptiveInterpretation result, WandHoldingModule executor, Wand.WandItem wand, BlockChanger blockChanger);
		}
		
		final OperationExecutor executor;
		
		public SimpleOperation(String name, RichString description, OperationExecutor executor, AdaptiveArgumentList list, boolean announceDuration, boolean pushToUndoStack)
		{
			super(name, description, announceDuration, pushToUndoStack);
			this.executor = executor;
			if (list != null)
				setArgumentList(list);
		}
		
		public SimpleOperation(String name, String description, OperationExecutor executor, AdaptiveArgumentList list, boolean announceDuration, boolean pushToUndoStack)
		{
			this(name, new RichString(description), executor, list, announceDuration, pushToUndoStack);
		}
		
		public SimpleOperation(String name, RichString description, OperationExecutor executor, boolean announceDuration, boolean pushToUndoStack)
		{
			this(name, description, executor, null, announceDuration, pushToUndoStack);
		}
		
		public SimpleOperation(String name, String description, OperationExecutor executor, boolean announceDuration, boolean pushToUndoStack)
		{
			this(name, description, executor, null, announceDuration, pushToUndoStack);
		}
		
		public SimpleOperation(String name, RichString description, OperationExecutor executor, AdaptiveArgumentList list, boolean announceDuration)
		{
			this(name, description, executor, list, announceDuration, true);
		}
		
		public SimpleOperation(String name, String description, OperationExecutor executor, AdaptiveArgumentList list, boolean announceDuration)
		{
			this(name, new RichString(description), executor, list, announceDuration, true);
		}
		
		public SimpleOperation(String name, RichString description, OperationExecutor executor, boolean announceDuration)
		{
			this(name, description, executor, null, announceDuration, true);
		}
		
		public SimpleOperation(String name, String description, OperationExecutor executor, boolean announceDuration)
		{
			this(name, description, executor, null, announceDuration, true);
		}
		
		public SimpleOperation(String name, RichString description, OperationExecutor executor, AdaptiveArgumentList list)
		{
			this(name, description, executor, list, true, true);
		}
		
		public SimpleOperation(String name, String description, OperationExecutor executor, AdaptiveArgumentList list)
		{
			this(name, new RichString(description), executor, list, true, true);
		}
		
		public SimpleOperation(String name, RichString description, OperationExecutor executor)
		{
			this(name, description, executor, null, true, true);
		}
		
		public SimpleOperation(String name, String description, OperationExecutor executor)
		{
			this(name, description, executor, null, true, true);
		}
		
		@Override
		public void runOperation(AdaptiveInterpretation result, WandHoldingModule executor, Wand.WandItem wand, BlockChanger blockChanger)
		{
			this.executor.execute(result, executor, wand, blockChanger);
		}
	}
}