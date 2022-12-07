package jogEdit.tool.tools;

import jogEdit.*;
import jogEdit.commands.*;
import jogEdit.tool.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.event.block.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;

public class NoTool extends ToolType
{
	public NoTool()
	{
		super("None", "Does nothing.");
	}
	
	@Override
	public void visualize(Player player, Tool tool)
	{
	
	}
	
	@Override
	public void interact(WandHoldingModule executor, Block block, Action action, BlockFace face, EquipmentSlot slot, PlayerInteractEvent event, Tool tool, Wand.WandItem wand)
	{
	
	}
}