package jogEdit.commands;

import jogEdit.*;
import jogLib.command.executor.*;
import jogUtil.*;
import jogUtil.commander.*;
import jogUtil.richText.*;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;

import java.util.*;

public class WandHoldingModule implements Executor.ExecutorModule
{
	final Wand.WandItem[] wands;
	boolean isPlayer = false;
	UUID id = null;
	final Location interactionOrigin;
	final PluginExecutor executor;
	
	public WandHoldingModule(PluginExecutor executor, Wand.WandItem[] wands, Location interactionOrigin)
	{
		this.executor = executor;
		this.wands = wands;
		this.interactionOrigin = interactionOrigin;
		if (executor instanceof LivingEntityExecutor entity)
			id = entity.getUniqueID();
		if (executor instanceof PlayerExecutor)
			isPlayer = true;
		executor.addModule(this);
	}
	
	public WandHoldingModule(LivingEntityExecutor executor)
	{
		this(executor, null, null);
	}
	
	public WandHoldingModule(Player player)
	{
		this((PlayerExecutor)PluginExecutor.convert(player));
	}
	
	public PluginExecutor baseExecutor()
	{
		return executor;
	}
	
	public Location getLocation()
	{
		if (executor instanceof LivingEntityExecutor)
			return ((LivingEntityExecutor)executor).getLocation();
		else
			return interactionOrigin;
	}
	
	public Location getInteractionOrigin()
	{
		if (executor instanceof LivingEntityExecutor)
			return ((LivingEntityExecutor)executor).getEyeLocation();
		else
			return interactionOrigin;
	}
	
	public Wand.WandItem[] getWands()
	{
		if (executor instanceof LivingEntityExecutor)
		{
			ArrayList<Wand.WandItem> wands = new ArrayList<>();
			if (Wand.isWand(((LivingEntityExecutor)executor).sender().getEquipment().getItem(EquipmentSlot.HAND)))
				wands.add(Wand.getWand(((LivingEntityExecutor)executor).sender().getEquipment().getItem(EquipmentSlot.HAND)));
			if (Wand.isWand(((LivingEntityExecutor)executor).sender().getEquipment().getItem(EquipmentSlot.OFF_HAND)))
				wands.add(Wand.getWand(((LivingEntityExecutor)executor).sender().getEquipment().getItem(EquipmentSlot.OFF_HAND)));
			return wands.toArray(new Wand.WandItem[0]);
		}
		else
			return wands;
	}
	
	public static class WandHoldingExecutorTransformer implements ExecutorFilter.Transformer
	{
		@Override
		public void transform(Executor executor)
		{
			if (executor instanceof LivingEntityExecutor)
				new WandHoldingModule((LivingEntityExecutor)executor);
		}
	}
	
	public static class WandHoldingExecutorFilter implements ExecutorFilter.Filter
	{
		@Override
		public Result canExecute(Executor executor)
		{
			if (executor.hasModule(WandHoldingModule.class))
			{
				WandHoldingModule module = executor.getModule(WandHoldingModule.class);
				if (module.getWands().length == 0)
					return new Result("You must be holding at least one wand.");
				else
					return new Result();
			}
			else
				return new Result("You must be a wand holder.");
		}
	}
	
	public static class RegionFilter extends WandHoldingExecutorFilter
	{
		@Override
		public Result canExecute(Executor executor)
		{
			Result result = super.canExecute(executor);
			if (!result.success())
				return result;
			WandHoldingModule module = executor.getModule(WandHoldingModule.class);
			Wand.WandItem[] wands = module.getWands();
			for (Wand.WandItem wand : wands)
			{
				if (wand.hasRegion())
					return new Result();
			}
			return new Result("At least one of your wands must have a region set.");
		}
	}
	
	public void respond(String string)
	{
		executor.respond(string);
	}
	
	public void respond(RichString string)
	{
		executor.respond(string);
	}
	
	public boolean canInteractWith(Block block)
	{
		return true;
	}
	
	public boolean canInteractWith(Entity entity)
	{
		return true;
	}
}