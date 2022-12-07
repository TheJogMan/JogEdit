package jogEdit.tool;

import jogEdit.region.*;
import org.bukkit.block.*;

public enum RegionBehavior
{
	CONTAIN((region, block) ->
	{
		return region.contains(block.getLocation());
	}),
	EXCLUDE((region, block) ->
	{
		return !region.contains(block.getLocation());
	}),
	IGNORE((region, block) ->
	{
		return true;
	});
	
	final CanEdit canEdit;
	
	RegionBehavior(CanEdit canEdit)
	{
		this.canEdit = canEdit;
	}
	
	public boolean canEdit(Region region, Block block)
	{
		return canEdit.canEdit(region, block);
	}
	
	interface CanEdit
	{
		boolean canEdit(Region region, Block block);
	}
}