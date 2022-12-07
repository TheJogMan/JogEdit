package jogEdit.tool.tools;

import jogEdit.*;
import jogEdit.commands.*;
import jogEdit.tool.*;
import jogLib.command.executor.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.event.block.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;

public class SelectionTool extends TargetingTool
{
	public SelectionTool()
	{
		super("Selection", "Used to select regions.");
	}
	
	@Override
	public void targetingVisualize(Player player, Tool tool)
	{
	
	}
	
	@Override
	public void interact(WandHoldingModule executor, Block block, Action action, BlockFace face, EquipmentSlot slot,
						 PlayerInteractEvent event, Tool tool, Wand.WandItem wand)
	{
		block = getTargetBlock(executor, tool);
		boolean changed = false;
		if (block != null)
		{
			if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_AIR))
			{
				wand.setCorner(false, block.getLocation(), executor);
				changed = true;
			}
			else if (event.getAction().equals(Action.LEFT_CLICK_BLOCK) || event.getAction().equals(Action.LEFT_CLICK_AIR))
			{
				wand.setCorner(true, block.getLocation(), executor);
				changed = true;
			}
		}
		if (changed && executor.baseExecutor() instanceof PlayerExecutor)
		{
			Visualizer.renderWand(((PlayerExecutor)executor.baseExecutor()).sender(), wand);
		}
	}
}