package jogEdit.operation;

import jogEdit.*;
import jogEdit.commands.*;
import jogEdit.region.*;
import jogUtil.commander.argument.*;
import jogUtil.richText.*;

public abstract class AsyncRegionOperation extends AsyncOperation
{
	public AsyncRegionOperation(String name, RichString description, boolean announceDuration)
	{
		super(name, description, announceDuration);
	}
	
	public AsyncRegionOperation(String name, RichString description)
	{
		super(name, description);
	}
	
	public AsyncRegionOperation(String name, String description, boolean announceDuration)
	{
		super(name, description, announceDuration);
	}
	
	public AsyncRegionOperation(String name, String description)
	{
		super(name, description);
	}
	
	public AsyncRegionOperation(String name, RichString description, boolean announceDuration, boolean pushToUndoStack)
	{
		super(name, description, announceDuration, pushToUndoStack);
	}
	
	public AsyncRegionOperation(String name, String description, boolean announceDuration, boolean pushToUndoStack)
	{
		super(name, description, announceDuration, pushToUndoStack);
	}
	
	public abstract void runRegionOperation(AdaptiveInterpretation result, WandHoldingModule executor, Wand.WandItem wand, Region region, BlockChanger changer);
	
	@Override
	public void runOperation(AdaptiveInterpretation result, WandHoldingModule executor, Wand.WandItem wand, BlockChanger blockChanger)
	{
		if (wand.hasRegion())
			runRegionOperation(result, executor, wand, wand.getRegion(), blockChanger);
	}
	
	public static class SimpleAsyncRegionOperation extends AsyncRegionOperation
	{
		public static interface AsyncRegionOperationExecutor
		{
			void execute(AdaptiveInterpretation result, WandHoldingModule executor, Wand.WandItem wand, Region region, BlockChanger changer);
		}
		
		AsyncRegionOperationExecutor executor;
		
		public SimpleAsyncRegionOperation(String name, RichString description, AsyncRegionOperationExecutor executor, AdaptiveArgumentList list, boolean announceDuration, boolean pushToUndoStack)
		{
			super(name, description, pushToUndoStack);
			this.executor = executor;
			if (list != null)
				setArgumentList(list);
		}
		
		public SimpleAsyncRegionOperation(String name, String description, AsyncRegionOperationExecutor executor, AdaptiveArgumentList list, boolean announceDuration, boolean pushToUndoStack)
		{
			this(name, new RichString(description), executor, list, pushToUndoStack);
		}
		
		public SimpleAsyncRegionOperation(String name, RichString description, AsyncRegionOperationExecutor executor, boolean announceDuration, boolean pushToUndoStack)
		{
			this(name, description, executor, null, pushToUndoStack);
		}
		
		public SimpleAsyncRegionOperation(String name, String description, AsyncRegionOperationExecutor executor, boolean announceDuration, boolean pushToUndoStack)
		{
			this(name, description, executor, null, pushToUndoStack);
		}
		
		public SimpleAsyncRegionOperation(String name, RichString description, AsyncRegionOperationExecutor executor, AdaptiveArgumentList list, boolean announceDuration)
		{
			this(name, description, executor, list, announceDuration, true);
		}
		
		public SimpleAsyncRegionOperation(String name, String description, AsyncRegionOperationExecutor executor, AdaptiveArgumentList list, boolean announceDuration)
		{
			this(name, new RichString(description), executor, list, announceDuration, true);
		}
		
		public SimpleAsyncRegionOperation(String name, RichString description, AsyncRegionOperationExecutor executor, boolean announceDuration)
		{
			this(name, description, executor, null, announceDuration, true);
		}
		
		public SimpleAsyncRegionOperation(String name, String description, AsyncRegionOperationExecutor executor, boolean announceDuration)
		{
			this(name, description, executor, null, announceDuration, true);
		}
		
		public SimpleAsyncRegionOperation(String name, RichString description, AsyncRegionOperationExecutor executor, AdaptiveArgumentList list)
		{
			super(name, description, true, true);
		}
		
		public SimpleAsyncRegionOperation(String name, String description, AsyncRegionOperationExecutor executor, AdaptiveArgumentList list)
		{
			this(name, new RichString(description), executor, list, true, true);
		}
		
		public SimpleAsyncRegionOperation(String name, RichString description, AsyncRegionOperationExecutor executor)
		{
			this(name, description, executor, null, true, true);
		}
		
		public SimpleAsyncRegionOperation(String name, String description, AsyncRegionOperationExecutor executor)
		{
			this(name, description, executor, null, true, true);
		}
		
		@Override
		public void runRegionOperation(AdaptiveInterpretation result, WandHoldingModule executor, Wand.WandItem wand, Region region, BlockChanger changer)
		{
			this.executor.execute(result, executor, wand, region, changer);
		}
	}
}