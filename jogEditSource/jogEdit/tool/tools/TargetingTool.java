package jogEdit.tool.tools;

import jogEdit.*;
import jogEdit.commands.*;
import jogEdit.tool.*;
import jogLib.command.executor.*;
import jogLib.values.*;
import jogUtil.data.values.*;
import jogUtil.richText.*;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.util.*;

public abstract class TargetingTool extends ToolType
{
	final Property<Integer> range;
	final Property<Boolean> selectEntities;
	final Property<FluidCollisionMode> fluidCollisionMode;
	final Property<Boolean> airSelection;
	final Property<Boolean> targetShown;
	final Property<Boolean> ignorePassable;
	
	public TargetingTool(String name, String description)
	{
		this(name, new RichString(description));
	}
	
	public TargetingTool(String name, RichString description)
	{
		super(name, description);
		
		range = addProperty("Range", IntegerValue.class, new IntegerValue(7), "How far away you can reach.");
		fluidCollisionMode = addProperty("FluidSelection", FluidCollisionModeValue.class, new FluidCollisionModeValue(FluidCollisionMode.ALWAYS), "Whether fluid blocks should be ignored.");
		selectEntities = addProperty("SelectEntities", BooleanValue.class, new BooleanValue(false), "Whether you can target an entity.");
		airSelection = addProperty("AirSelection", BooleanValue.class, new BooleanValue(true), "Whether you can target air blocks.");
		targetShown = addProperty("TargetShown", BooleanValue.class, new BooleanValue(true), "Whether the currently targeted block should be highlighted.");
		ignorePassable = addProperty("IgnorePassable", BooleanValue.class, new BooleanValue(false), "Whether blocks with no collision volume should be ignored.");
	}
	
	@Override
	public final void visualize(Player player, Tool tool)
	{
		if (targetShown(tool))
			Visualizer.highlightBlock(getTargetBlock(new WandHoldingModule(player), tool), player);
	}
	
	public abstract void targetingVisualize(Player player, Tool tool);
	
	public Block getTargetBlock(WandHoldingModule executor, Tool tool)
	{
		Entity entityToIgnore = null;
		Location origin = executor.getInteractionOrigin();
		
		if (executor.baseExecutor() instanceof EntityExecutor)
			entityToIgnore = ((EntityExecutor)executor.baseExecutor()).sender();
		
		int range = getRange(tool);
		if (selectEntities(tool))
		{
			Entity ignoredEntity = entityToIgnore;
			RayTraceResult result = origin.getWorld().rayTraceEntities(origin, origin.getDirection(), range, 0, (entity) -> {return !entity.equals(ignoredEntity);});
			if (result != null)
			{
				Location location = result.getHitEntity().getLocation();
				location.setY(result.getHitPosition().getY());
				return location.getBlock();
			}
		}
		RayTraceResult result = origin.getWorld().rayTraceBlocks(origin, origin.getDirection(), range, fluidSelection(tool), ignorePassable(tool));
		if (result != null)
		{
			return result.getHitBlock();
		}
		else if (airSelection(tool))
		{
			Vector vec = origin.getDirection();
			vec.multiply(range);
			return origin.getWorld().getBlockAt(origin.add(vec));
		}
		else return null;
	}
	
	public int getRange(Tool tool)
	{
		return range.get(tool);
	}
	
	public void setRange(Tool tool, int range)
	{
		this.range.set(tool, range);
	}
	
	public boolean selectEntities(Tool tool)
	{
		return selectEntities.get(tool);
	}
	
	public void setSelectEntities(Tool tool, boolean selectEntities)
	{
		this.selectEntities.set(tool, selectEntities);
	}
	
	public FluidCollisionMode fluidSelection(Tool tool)
	{
		return fluidCollisionMode.get(tool);
	}
	
	public void setFluidSelection(Tool tool, FluidCollisionMode mode)
	{
		this.fluidCollisionMode.set(tool, mode);
	}
	
	public boolean targetShown(Tool tool)
	{
		return targetShown.get(tool);
	}
	
	public void setTargetShown(Tool tool, boolean shown)
	{
		this.targetShown.set(tool, shown);
	}
	
	public boolean airSelection(Tool tool)
	{
		return airSelection.get(tool);
	}
	
	public void setAirSelection(Tool tool, boolean airSelection)
	{
		this.airSelection.set(tool, airSelection);
	}
	
	public boolean ignorePassable(Tool tool)
	{
		return ignorePassable.get(tool);
	}
	
	public void setIgnorePassable(Tool tool, boolean ignorePassable)
	{
		this.ignorePassable.set(tool, ignorePassable);
	}
}