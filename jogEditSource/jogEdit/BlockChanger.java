package jogEdit;

import jogEdit.commands.*;
import jogEdit.region.*;
import jogEdit.tool.*;
import jogUtil.*;
import jogUtil.data.*;
import jogUtil.data.values.*;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.block.data.*;

import java.util.*;

public class BlockChanger
{
	final boolean doPhysics;
	final WandHoldingModule executor;
	final RegionBehavior behavior;
	final Region region;
	final String name;
	final Task.TaskWriter writer;
	
	int changedBlocks = 0;
	
	public BlockChanger(boolean doPhysics, WandHoldingModule executor, RegionBehavior behavior, Region region, String taskName, Task.TaskWriter writer)
	{
		this.doPhysics = doPhysics;
		this.executor = executor;
		this.behavior = behavior;
		this.region = region;
		this.name = taskName;
		this.writer = writer;
	}
	
	public void change(Block block, Material material)
	{
		change(block, Bukkit.createBlockData(material));
	}
	
	public void change(Block block, BlockData newData)
	{
		new Change(block, doPhysics, newData);
	}
	
	public int changedBlocks()
	{
		return changedBlocks;
	}
	
	public void finish()
	{
		if (!Bukkit.isPrimaryThread())
			waitForClear();
	}
	
	static ChangeHandler handler;
	
	public static class ChangeHandler implements Runnable
	{
		final ChangeQueue changeQueue;
		final Config.Setting<Integer> changesPerTick;
		
		ChangeHandler(JogEdit jogEdit)
		{
			BlockChanger.handler = this;
			
			changesPerTick = jogEdit.config.createSetting("ChangesPerTick", TypeRegistry.get(IntegerValue.class), new IntegerValue(500),
														  "Maximum number of blocks that can be changed per tick.", new Object[] {1, Integer.MAX_VALUE});
			changeQueue = new ChangeQueue(changesPerTick.get(), this);
		}
		
		@Override
		public void run()
		{
			boolean cleared = false;
			synchronized(changeQueue)
			{
				if (changeQueue.changes.size() > 0)
				{
					int maxChanges = changesPerTick.get();
					int changes = 0;
					while (changeQueue.changes.size() > 0 && changes < maxChanges)
					{
						changeQueue.pop().apply();
						changes++;
					}
				}
				if (changeQueue.changes.size() == 0)
					cleared = true;
			}
			wakeWaiters();
			if (cleared)
				wakeClearWaiters();
		}
	}
	
	private class Change
	{
		final Block block;
		final boolean doPhysics;
		final BlockData data;
		
		Change(Block block, boolean doPhysics, BlockData data)
		{
			this.block = block;
			this.doPhysics = doPhysics;
			this.data = data;
			
			if (Bukkit.isPrimaryThread())
				apply();
			else
				handler.changeQueue.push(this);
		}
		
		void apply()
		{
			BlockData previousData = block.getBlockData();
			if (behavior.canEdit(region, block) && executor.canInteractWith(block) && (!data.matches(previousData) || !data.equals(previousData)))
			{
				block.setBlockData(data, doPhysics);
				if (writer != null)
					writer.write(new Task.ChangedBlock(block.getLocation(), previousData, data));
				changedBlocks++;
			}
		}
	}
	
	private static class ChangeQueue
	{
		final ArrayList<Change> changes;
		final ChangeHandler handler;
		
		ChangeQueue(int initialSize, ChangeHandler handler)
		{
			changes = new ArrayList<>(initialSize);
			this.handler = handler;
		}
		
		Change pop()
		{
			Change change = changes.get(0);
			changes.remove(0);
			return change;
		}
		
		void push(Change change)
		{
			boolean added = false;
			while (!added)
			{
				synchronized(handler.changeQueue)
				{
					if (changes.size() < handler.changesPerTick.get() || Bukkit.getServer().isPrimaryThread())
					{
						changes.add(change);
						added = true;
					}
				}
				if (!added)
					waitForHandler();
			}
		}
	}
	
	private static final WaitingPoint waitingPoint = new WaitingPoint();
	private static final WaitingPoint clearWaitingPoint = new WaitingPoint();
	
	private static class WaitingPoint
	{
		boolean hasWaiting = false;
	}
	
	private static void waitForClear()
	{
		if (Bukkit.isPrimaryThread())
			throw new RuntimeException("Can't wait for change handler while on primary thread.");
		synchronized(clearWaitingPoint)
		{
			clearWaitingPoint.hasWaiting = true;
			while (clearWaitingPoint.hasWaiting)
			{
				try
				{
					clearWaitingPoint.wait();
				}
				catch (InterruptedException ignored)
				{
				
				}
			}
		}
	}
	
	private static void waitForHandler()
	{
		if (Bukkit.isPrimaryThread())
			throw new RuntimeException("Can't wait for change handler while on primary thread.");
		synchronized(waitingPoint)
		{
			waitingPoint.hasWaiting = true;
			while (waitingPoint.hasWaiting)
			{
				try
				{
					waitingPoint.wait();
				}
				catch (InterruptedException ignored)
				{
				
				}
			}
		}
	}
	
	private static void wakeWaiters()
	{
		synchronized(waitingPoint)
		{
			waitingPoint.hasWaiting = false;
			waitingPoint.notifyAll();
		}
	}
	
	private static void wakeClearWaiters()
	{
		synchronized(clearWaitingPoint)
		{
			clearWaitingPoint.hasWaiting = false;
			clearWaitingPoint.notifyAll();
		}
	}
	
	public String taskName()
	{
		return name;
	}
}