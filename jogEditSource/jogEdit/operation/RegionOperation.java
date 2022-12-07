package jogEdit.operation;

import jogEdit.*;
import jogEdit.commands.*;
import jogEdit.region.*;
import jogUtil.commander.argument.*;
import jogUtil.richText.*;

public abstract class RegionOperation extends Operation
{
	public RegionOperation(String name, RichString description, boolean announceDuration, boolean pushToUndoStack)
	{
		super(name, description, announceDuration, pushToUndoStack);
		addFilter(new WandHoldingModule.RegionFilter());
	}
	
	public RegionOperation(String name, RichString description)
	{
		this(name, description, true, true);
	}
	
	public RegionOperation(String name, RichString description, boolean announceDuration)
	{
		this(name, description, announceDuration, true);
	}
	
	public RegionOperation(String name, String description, boolean announceDuration, boolean pushToUndoStack)
	{
		this(name, new RichString(description), announceDuration, pushToUndoStack);
	}
	
	public RegionOperation(String name, String description)
	{
		this(name, new RichString(description), true);
	}
	
	public RegionOperation(String name, String description, boolean announceDuration)
	{
		this(name, new RichString(description), announceDuration, true);
	}
	
	public abstract void runRegionOperation(AdaptiveInterpretation result, WandHoldingModule executor, Wand.WandItem wand, Region region, BlockChanger changer);
	
	@Override
	public void runOperation(AdaptiveInterpretation result, WandHoldingModule executor, Wand.WandItem wand, BlockChanger blockChanger)
	{
		if (wand.hasRegion())
			runRegionOperation(result, executor, wand, wand.getRegion(), blockChanger);
	}
	
	public static class SimpleRegionOperation extends RegionOperation
	{
		public static interface RegionOperationExecutor
		{
			void execute(AdaptiveInterpretation result, WandHoldingModule executor, Wand.WandItem wand, Region region, BlockChanger changer);
		}
		
		final RegionOperationExecutor executor;
		
		public SimpleRegionOperation(String name, RichString description, RegionOperationExecutor executor, AdaptiveArgumentList list, boolean announceDuration, boolean pushToUndoStack)
		{
			super(name, description, pushToUndoStack);
			this.executor = executor;
			if (list != null)
				setArgumentList(list);
		}
		
		public SimpleRegionOperation(String name, String description, RegionOperationExecutor executor, AdaptiveArgumentList list, boolean announceDuration, boolean pushToUndoStack)
		{
			this(name, new RichString(description), executor, list, announceDuration, pushToUndoStack);
		}
		
		public SimpleRegionOperation(String name, RichString description, RegionOperationExecutor executor, boolean announceDuration, boolean pushToUndoStack)
		{
			this(name, description, executor, null, announceDuration, pushToUndoStack);
		}
		
		public SimpleRegionOperation(String name, String description, RegionOperationExecutor executor, boolean announceDuration, boolean pushToUndoStack)
		{
			this(name, description, executor, null, announceDuration, pushToUndoStack);
		}
		
		public SimpleRegionOperation(String name, RichString description, RegionOperationExecutor executor, AdaptiveArgumentList list, boolean announceDuration)
		{
			this(name, description, executor, list, announceDuration, true);
		}
		
		public SimpleRegionOperation(String name, String description, RegionOperationExecutor executor, AdaptiveArgumentList list, boolean announceDuration)
		{
			this(name, new RichString(description), executor, list, announceDuration, true);
		}
		
		public SimpleRegionOperation(String name, RichString description, RegionOperationExecutor executor, boolean announceDuration)
		{
			this(name, description, executor, null, announceDuration, true);
		}
		
		public SimpleRegionOperation(String name, String description, RegionOperationExecutor executor, boolean announceDuration)
		{
			this(name, description, executor, null, announceDuration, true);
		}
		
		public SimpleRegionOperation(String name, String description, RegionOperationExecutor executor, AdaptiveArgumentList list)
		{
			this(name, new RichString(description), executor, list, true, true);
		}
		
		public SimpleRegionOperation(String name, RichString description, RegionOperationExecutor executor)
		{
			this(name, description, executor, null, true, true);
		}
		
		public SimpleRegionOperation(String name, String description, RegionOperationExecutor executor)
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