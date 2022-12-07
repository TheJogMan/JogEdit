package jogEdit.operation;

import jogEdit.*;
import jogEdit.commands.*;
import jogEdit.tool.*;
import jogUtil.commander.argument.*;
import jogUtil.richText.*;

public abstract class AsyncOperation extends Operation
{
	public AsyncOperation(String name, String description, boolean announceDuration, boolean pushToUndoStack)
	{
		super(name, description, announceDuration, pushToUndoStack);
	}
	
	public AsyncOperation(String name, String description, boolean announceDuration)
	{
		super(name, description, announceDuration);
	}
	
	public AsyncOperation(String name, String description)
	{
		super(name, description);
	}
	
	public AsyncOperation(String name, RichString description)
	{
		super(name, description);
	}
	
	public AsyncOperation(String name, RichString description, boolean announceDuration)
	{
		super(name, description, announceDuration);
	}
	
	public AsyncOperation(String name, RichString description, boolean announceDuration, boolean pushToUndoStack)
	{
		super(name, description, announceDuration, pushToUndoStack);
	}
	
	@Override
	void handle(AdaptiveInterpretation result, WandHoldingModule executor, Wand.WandItem wand, String name)
	{
		Thread thread = new OperationThread(result, executor, wand, name);
		if (JogEdit.doParallelization())
			thread.start();
		else
			thread.run();
	}
	
	private class OperationThread extends Thread
	{
		final AdaptiveInterpretation result;
		final WandHoldingModule executor;
		final Wand.WandItem wand;
		final String name;
		
		OperationThread(AdaptiveInterpretation result, WandHoldingModule executor, Wand.WandItem wand, String name)
		{
			super("JogEditAsyncOperation:" + name);
			this.result = result;
			this.executor = executor;
			this.wand = wand;
			this.name = name;
		}
		
		@Override
		public void run()
		{
			long start = System.currentTimeMillis();
			Task.TaskWriter writer = pushToUndoStack ? Task.create(name) : null;
			if (writer != null)
				wand.beginTask(writer.id(), Wand.WandItem.TaskDestination.UNDO, writer.name());
			BlockChanger changer = new BlockChanger(wand.doPhysics(), executor, RegionBehavior.CONTAIN, wand.getRegion(), name, writer);
			runOperation(result, executor, wand, changer);
			changer.finish();
			if (writer != null)
			{
				wand.endTask(writer.id());
				writer.close();
			}
			long duration = System.currentTimeMillis() - start;
			if (announceDuration)
				executor.respond(duration + "ms elapsed.");
		}
	}
	
	public static class SimpleAsyncOperation extends AsyncOperation
	{
		public static interface AsyncOperationExecutor
		{
			void execute(AdaptiveInterpretation result, WandHoldingModule executor, Wand.WandItem wand, BlockChanger blockChanger);
		}
		
		final AsyncOperationExecutor executor;
		
		public SimpleAsyncOperation(String name, RichString description, AsyncOperationExecutor executor, AdaptiveArgumentList list, boolean announceDuration, boolean pushToUndoStack)
		{
			super(name, description, pushToUndoStack);
			this.executor = executor;
			if (list != null)
				setArgumentList(list);
		}
		
		public SimpleAsyncOperation(String name, String description, AsyncOperationExecutor executor, AdaptiveArgumentList list, boolean announceDuration, boolean pushToUndoStack)
		{
			this(name, new RichString(description), executor, list, announceDuration, pushToUndoStack);
		}
		
		public SimpleAsyncOperation(String name, RichString description, AsyncOperationExecutor executor, boolean announceDuration, boolean pushToUndoStack)
		{
			this(name, description, executor, null, announceDuration, pushToUndoStack);
		}
		
		public SimpleAsyncOperation(String name, String description, AsyncOperationExecutor executor, boolean announceDuration, boolean pushToUndoStack)
		{
			this(name, description, executor, null, announceDuration, pushToUndoStack);
		}
		
		public SimpleAsyncOperation(String name, RichString description, AsyncOperationExecutor executor, AdaptiveArgumentList list, boolean announceDuration)
		{
			this(name, description, executor, list, announceDuration, true);
		}
		
		public SimpleAsyncOperation(String name, String description, AsyncOperationExecutor executor, AdaptiveArgumentList list, boolean announceDuration)
		{
			this(name, new RichString(description), executor, list, announceDuration, true);
		}
		
		public SimpleAsyncOperation(String name, RichString description, AsyncOperationExecutor executor, boolean announceDuration)
		{
			this(name, description, executor, null, announceDuration, true);
		}
		
		public SimpleAsyncOperation(String name, String description, AsyncOperationExecutor executor, boolean announceDuration)
		{
			this(name, description, executor, null, announceDuration, true);
		}
		
		public SimpleAsyncOperation(String name, RichString description, AsyncOperationExecutor executor, AdaptiveArgumentList list)
		{
			this(name, description, executor, list, true, true);
		}
		
		public SimpleAsyncOperation(String name, String description, AsyncOperationExecutor executor, AdaptiveArgumentList list)
		{
			this(name, new RichString(description), executor, list, true, true);
		}
		
		public SimpleAsyncOperation(String name, RichString description, AsyncOperationExecutor executor)
		{
			this(name, description, executor, null, true, true);
		}
		
		public SimpleAsyncOperation(String name, String description, AsyncOperationExecutor executor)
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