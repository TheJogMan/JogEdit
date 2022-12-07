package jogEdit.tool.tools;

import jogEdit.*;
import jogEdit.commands.*;
import jogEdit.tool.*;
import jogUtil.richText.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.event.block.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;

public abstract class BrushTool extends TargetingTool
{
	final Property<RegionBehavior> regionBehavior;
	final boolean pushToUndoStack;
	
	public BrushTool(String name, RichString description, boolean pushToUndoStack)
	{
		super(name, description);
		this.pushToUndoStack = pushToUndoStack;
		regionBehavior = addProperty("RegionBehavior", RegionBehaviorValue.class, new RegionBehaviorValue(RegionBehavior.IGNORE),
									 "How the current region selection should apply to this brush.");
	}
	
	public BrushTool(String name, RichString description)
	{
		this(name, description, true);
	}
	
	public BrushTool(String name, String description, boolean pushToUndoStack)
	{
		this(name, new RichString(description));
	}
	
	public BrushTool(String name, String description)
	{
		this(name, new RichString(description), true);
	}
	
	public abstract void brushVisualize(Player player, Tool tool, Block target);
	
	@Override
	public void targetingVisualize(Player player, Tool tool)
	{
		brushVisualize(player, tool, getTargetBlock(new WandHoldingModule(player), tool));
	}
	
	public abstract void execute(WandHoldingModule executor, Block target, Action action, Tool tool, Wand.WandItem wand, BlockChanger changer);
	
	@Override
	public void interact(WandHoldingModule executor, Block block, Action action, BlockFace face, EquipmentSlot slot, PlayerInteractEvent event, Tool tool, Wand.WandItem wand)
	{
		Task.TaskWriter writer = pushToUndoStack ? Task.create("Brush Stroke") : null;
		if (writer != null)
			wand.beginTask(writer.id(), Wand.WandItem.TaskDestination.UNDO, writer.name());
		BlockChanger changer = new BlockChanger(wand.doPhysics(), executor, regionBehavior.get(tool), wand.getRegion(), "Brush Stroke", null);
		execute(executor, getTargetBlock(executor, tool), action, tool, wand, changer);
		if (writer != null)
		{
			wand.endTask(writer.id());
			writer.close();
		}
	}
}