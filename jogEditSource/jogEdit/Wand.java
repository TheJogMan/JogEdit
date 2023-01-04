package jogEdit;

import jogEdit.commands.*;
import jogEdit.region.*;
import jogEdit.tool.*;
import jogEdit.tool.tools.*;
import jogLib.customContent.*;
import jogUtil.data.*;
import jogUtil.data.values.*;
import jogUtil.richText.*;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.enchantments.*;
import org.bukkit.entity.*;
import org.bukkit.event.block.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;
import org.bukkit.plugin.*;

import java.util.*;

public class Wand extends CustomItemType<Wand.WandItem>
{
	Wand(Plugin plugin)
	{
		super(new NamespacedKey(plugin, "Wand"), Material.STICK, "JEdit Wand");
	}
	
	public static boolean isWand(ItemStack item)
	{
		return isWand(item.getItemMeta());
	}
	
	public static boolean isWand(ItemMeta meta)
	{
		return CustomItemType.isCustomItem(meta) && CustomItemType.getType(meta) instanceof Wand;
	}
	
	public static WandItem getWand(ItemStack item)
	{
		return JogEdit.wand().getObject(item);
	}
	
	public static WandItem getWand(ItemMeta meta)
	{
		return JogEdit.wand().getObject(meta);
	}
	
	@Override
	protected void configureMeta(ItemMeta meta)
	{
		ArrayList<String> lore = new ArrayList<>();
		lore.add("Used to select regions for JEdit.");
		
		meta.setLore(lore);
		meta.setUnbreakable(true);
		meta.addEnchant(Enchantment.DURABILITY, 1, true);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
	}
	
	@Override
	protected void finalizeStack(ItemStack stack)
	{
	
	}
	
	@Override
	protected WandItem getObject(ItemMeta meta)
	{
		return new WandItem(meta);
	}
	
	@Override
	protected WandItem getObject(ItemStack stack)
	{
		return new WandItem(stack);
	}
	
	@Override
	protected void playerInteract(WandItem item, Player player, Block block, Action action, BlockFace face, EquipmentSlot slot, PlayerInteractEvent event)
	{
		event.setCancelled(true);
		item.getToolType().interact(new WandHoldingModule(player), block, action, face, slot, event, item.getTool(), item);
	}
	
	public static class WandItem extends CustomItemType.CustomItem
	{
		WandItem(ItemMeta meta)
		{
			super(meta);
		}
		
		WandItem(ItemStack item)
		{
			super(item);
		}
		
		public void describe(WandHoldingModule executor)
		{
			RichStringBuilder builder = RichStringBuilder.start();
			builder.append("Wand Properties:", builder.style().color(RichColor.AQUA));
			builder.newLine();
			builder.style(builder.style().color(RichColor.ORANGE));
			builder.append("Region: ");
			if (hasRegion())
				builder.append(getRegion().toRichString());
			else
				builder.append("None.", builder.style().color(RichColor.WHITE));
			builder.newLine();
			builder.append("Do Physics: ");
			builder.append(doPhysics() + "", builder.style().color(RichColor.WHITE));
			builder.newLine();
			builder.append("Ignore Air: ");
			builder.append(ignoreAir() + "", builder.style().color(RichColor.WHITE));
			builder.newLine();
			builder.append(getTool().toRichString());
			executor.respond(builder.build());
		}
		
		public Tool getTool()
		{
			return ToolBox.getTool(getToolType(), this);
		}
		
		public ToolType getToolType()
		{
			Data data = getData();
			return ToolBox.getType(((StringValue)data.get("Tool", new StringValue(ToolBox.getType(SelectionTool.class).name()))).get());
		}
		
		public void setTool(ToolType type)
		{
			Data data = getData();
			data.put("Tool", new StringValue(type.name()));
			setData(data);
		}
		
		public void setTool(ToolType type, WandHoldingModule executor)
		{
			setTool(type);
			if (executor != null) executor.respond("Tool set to " + type.name());
		}
		
		public void setToolProperties(ToolType type, Data properties)
		{
			Data data = getData();
			Data propertiesData = ((DataValue)data.get("Tool Properties", new DataValue())).get();
			propertiesData.put(type.name(), new DataValue(properties));
			setData(data);
		}
		
		public void resetToolProperties(ToolType type)
		{
			Data data = getData();
			data.remove("Tool Properties");
			setData(data);
		}
		
		public Data getToolProperties(ToolType type)
		{
			Data data = getData();
			Data propertiesData = ((DataValue)data.get("Tool Properties", new DataValue())).get();
			return ((DataValue)propertiesData.get(type.name(), new DataValue())).get();
		}
		
		public boolean hasRegion()
		{
			Data data = getData();
			return data.has("Region");
		}
		
		public Region getRegion()
		{
			Data data = getData();
			return ((RegionValue)data.get("Region", new RegionValue())).get();
		}
		
		public void setRegion(Region region)
		{
			Data data = getData();
			data.put("Region", new RegionValue(region));
			setData(data);
		}
		
		public void setCorner(boolean corner1, Location location)
		{
			Region region = getRegion();
			if (!hasRegion())
				region.setCorner(!corner1, location);
			region.setCorner(corner1, location);
			setRegion(region);
		}
		
		public void setCorner(boolean corner1, Location location, WandHoldingModule executor)
		{
			boolean hasRegion = hasRegion();
			setCorner(corner1, location);
			if (executor != null)
			{
				if (hasRegion)
					executor.respond("Corner " + (corner1 ? "1" : "2") + " set.");
				else
					executor.respond("Both corners set.");
			}
		}
		
		public void clearSelection()
		{
			Data data = getData();
			data.remove("Region");
			setData(data);
		}
		
		public Location[] getCorners()
		{
			Location[] corners = new Location[2];
			Region region = getRegion();
			corners[0] = region.cornerLocation(true);
			corners[1] = region.cornerLocation(false);
			return corners;
		}
		
		public Location[] getOrientedCorners()
		{
			Location[] corners = getCorners();
			if (corners == null)
				return null;
			else
				return Region.orientCorners(corners);
		}
		
		public boolean ignoreAir()
		{
			Data data = getData();
			return ((BooleanValue)data.get("Ignore Air", new BooleanValue(false))).get();
		}
		
		public void setIgnoreAir(boolean ignoreAir)
		{
			Data data = getData();
			data.put("Ignore Air", new BooleanValue(ignoreAir));
			setData(data);
		}
		
		public boolean doPhysics()
		{
			Data data = getData();
			return ((BooleanValue)data.get("Do Physics", new BooleanValue(true))).get();
		}
		
		public void setDoPhysics(boolean doPhysics)
		{
			Data data = getData();
			data.put("Do Physics", new BooleanValue(doPhysics));
			setData(data);
		}
		
		public Schematic getClipboard()
		{
			Data data = getData();
			UUID id = (UUID)data.get("Clipboard ID", new UUIDValue(UUID.randomUUID())).get();
			return Clipboard.getEntry(id);
		}
		
		public void setClipboard(Schematic schematic)
		{
			UUID id = Clipboard.addEntry(schematic);
			Data data = getData();
			data.put("Clipboard ID", new UUIDValue(id));
			setData(data);
		}
		
		public void clearTaskHistory()
		{
			Data data = getData();
			ListValue<UUIDValue> undoStack = data.getObject("Undo Stack", new ListValue<>(TypeRegistry.get(UUIDValue.class)));
			ListValue<UUIDValue> redoStack = data.getObject("Redo Stack", new ListValue<>(TypeRegistry.get(UUIDValue.class)));
			undoStack.forEach(value -> Task.remove(value.get()));
			redoStack.forEach(value -> Task.remove(value.get()));
			data.remove("Undo Stack");
			data.remove("Redo Stack");
			setData(data);
		}
		
		public enum TaskDestination
		{
			UNDO, REDO, NONE
		}
		
		public void beginTask(UUID id, TaskDestination destination, String name)
		{
			Data data = new Data();
			data.put("ID", new UUIDValue(id));
			data.put("Destination", new StringValue(destination.toString()));
			data.put("Name", new StringValue(name));
			data.put("Instance ID", new UUIDValue(JogEdit.sessionID));
			
			Data wandData = getData();
			ListValue<DataValue> tasks = wandData.getObject("Active Tasks", new ListValue<>(TypeRegistry.get(DataValue.class)));
			tasks.add(new DataValue(data));
			setData(wandData);
		}
		
		public void endTask(UUID id)
		{
			Data wandData = getData();
			ListValue<DataValue> tasks = wandData.getObject("Active Tasks", new ListValue<>(TypeRegistry.get(DataValue.class)));
			int taskIndex = -1;
			for (int index = 0; index < tasks.size(); index++)
			{
				if (tasks.get(index).get().getValue("ID", new UUIDValue()).equals(id))
				{
					taskIndex = index;
					break;
				}
			}
			if (taskIndex != -1)
			{
				Data data = tasks.remove(taskIndex).get();
				setData(wandData);
				TaskDestination destination = TaskDestination.valueOf(data.getValue("Destination", new StringValue(TaskDestination.NONE.toString())));
				if (destination.equals(TaskDestination.UNDO))
					pushToUndo(data.getObject("ID", new UUIDValue()).get());
				else if (destination.equals(TaskDestination.REDO))
					pushToRedo(data.getObject("ID", new UUIDValue()).get());
			}
		}
		
		public void pushToUndo(UUID id)
		{
			Data data = getData();
			ListValue<UUIDValue> stack = data.getObject("Undo Stack", new ListValue<>(TypeRegistry.get(UUIDValue.class)));
			stack.add(new UUIDValue(id));
			setData(data);
		}
		
		public void pushToRedo(UUID id)
		{
			Data data = getData();
			ListValue<UUIDValue> stack = data.getObject("Redo Stack", new ListValue<>(TypeRegistry.get(UUIDValue.class)));
			stack.add(new UUIDValue(id));
			setData(data);
		}
		
		public UUID popFromUndo()
		{
			Data data = getData();
			ListValue<UUIDValue> stack = data.getObject("Undo Stack", new ListValue<>(TypeRegistry.get(UUIDValue.class)));
			if (stack.size() > 0)
			{
				UUID id = stack.remove(stack.size() - 1).get();
				setData(data);
				return id;
			}
			else
				return null;
		}
		
		public UUID popFromRedo()
		{
			Data data = getData();
			ListValue<UUIDValue> stack = data.getObject("Redo Stack", new ListValue<>(TypeRegistry.get(UUIDValue.class)));
			if (stack.size() > 0)
			{
				UUID id = stack.remove(stack.size() - 1).get();
				setData(data);
				return id;
			}
			else
				return null;
		}
		
		public UUID getFromUndo()
		{
			Data data = getData();
			ListValue<UUIDValue> stack = data.getObject("Undo Stack", new ListValue<>(TypeRegistry.get(UUIDValue.class)));
			if (stack.size() > 0)
			{
				return stack.get(stack.size() - 1).get();
			}
			else
				return null;
		}
		
		public UUID getFromRedo()
		{
			Data data = getData();
			ListValue<UUIDValue> stack = data.getObject("Redo Stack", new ListValue<>(TypeRegistry.get(UUIDValue.class)));
			if (stack.size() > 0)
			{
				return stack.get(stack.size() - 1).get();
			}
			else
				return null;
		}
	}
}