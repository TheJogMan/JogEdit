package jogEdit.region;

import jogEdit.*;
import jogUtil.richText.*;
import org.bukkit.util.Vector;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;

import java.util.*;

public class Region implements Iterable<Block>
{
	World world;
	Vector corner1;
	Vector corner2;
	RegionType type = RegionType.RECTANGULAR;
	
	public Region(World world, Vector corner1, Vector corner2)
	{
		this.world = world;
		this.corner1 = corner1.clone();
		this.corner2 = corner2.clone();
	}
	
	public Region(Location corner1, Location corner2)
	{
		this(corner1.getWorld(), corner1.toVector(), corner2.toVector());
	}
	
	@Override
	public boolean equals(Object object)
	{
		if (!(object instanceof Region))
			return false;
		if (!(world == null && ((Region)object).world == null) || (world != null && !world.equals(((Region)object).world)))
			return false;
		if (!type.equals(((Region)object).type))
			return false;
		if (!corner1.equals(((Region)object).corner1))
			return false;
		return corner2.equals(((Region) object).corner2);
	}
	
	@Override
	public String toString()
	{
		return RegionValue.toString(this);
	}
	
	public RichString toRichString()
	{
		return RegionValue.toRichString(this);
	}
	
	@Override
	public Region clone()
	{
		Region region = new Region(world, corner1.clone(), corner2.clone());
		region.type = type;
		return region;
	}
	
	public void setCorner(boolean corner1, Vector corner)
	{
		if (corner1)
			this.corner1 = corner.clone();
		else
			this.corner2 = corner.clone();
	}
	
	public void setCorner(boolean corner1, Location location)
	{
		world = location.getWorld();
		setCorner(corner1, location.toVector());
	}
	
	public Vector cornerVector(boolean corner1)
	{
		return corner1 ? this.corner1.clone() : corner2.clone();
	}
	
	public Location cornerLocation(boolean corner1)
	{
		Vector corner = cornerVector(corner1);
		return new Location(world, corner.getX(), corner.getY(), corner.getZ());
	}
	
	public World world()
	{
		return world;
	}
	
	public void setWorld(World world)
	{
		this.world = world;
	}
	
	public RegionType type()
	{
		return type;
	}
	
	public void setType(RegionType type)
	{
		this.type = type;
	}
	
	public void visualize(Player player)
	{
		type.regionVisualizer.visualize(this, player);
	}
	
	public boolean contains(Location location)
	{
		if (world.equals(location.getWorld()))
			return contains(location.toVector());
		else
			return false;
	}
	
	public boolean contains(Vector position)
	{
		return type.boundChecker.inRegion(this, position);
	}
	
	public double volume()
	{
		Vector dimensions = dimensions();
		return dimensions.getX() * dimensions.getY() * dimensions.getZ();
	}
	
	public Vector dimensions()
	{
		Vector[] corners = orientedCorners();
		return new Vector(corners[1].getX() - corners[0].getX(), corners[1].getY() - corners[0].getY(), corners[1].getZ() - corners[0].getZ());
	}
	
	public Vector[] orientedCorners()
	{
		Vector[] corners = {new Vector(0.0, 0.0, 0.0), new Vector(0.0, 0.0, 0.0)};
		if (corner1.getX() > corner2.getX())
		{
			corners[0].setX(corner2.getX());
			corners[1].setX(corner1.getX());
		}
		else
		{
			corners[0].setX(corner1.getX());
			corners[1].setX(corner2.getX());
		}
		if (corner1.getY() > corner2.getY())
		{
			corners[0].setY(corner2.getY());
			corners[1].setY(corner1.getY());
		}
		else
		{
			corners[0].setY(corner1.getY());
			corners[1].setY(corner2.getY());
		}
		if (corner1.getZ() > corner2.getZ())
		{
			corners[0].setZ(corner2.getZ());
			corners[1].setZ(corner1.getZ());
		}
		else
		{
			corners[0].setZ(corner1.getZ());
			corners[1].setZ(corner2.getZ());
		}
		return corners;
	}
	
	public enum RegionType
	{
		RECTANGULAR((region, position) ->
		{
			Vector[] corners = region.orientedCorners();
			return position.getX() >= corners[0].getX() && position.getX() <= corners[1].getX()
				&& position.getY() >= corners[0].getY() && position.getY() <= corners[1].getY()
				&& position.getZ() >= corners[0].getZ() && position.getZ() <= corners[1].getZ();
		},
		(region, player) ->
		{
			Visualizer.highlightRegion(region.orientedCorners(), player, Particle.FLAME, 0, .5);
		}),
		OVULAR((region, position) ->
		{
			return true;
		},
		(region, player) ->
		{
		
		}),
		CUBIC((region, position) ->
		{
			return true;
		},
		(region, player) ->
		{
			
		}),
		SPHERICAL((region, position) ->
		{
			return true;
		},
		(region, player) ->
		{
			
		});
		
		final BoundChecker boundChecker;
		final RegionVisualizer regionVisualizer;
		
		RegionType(BoundChecker boundChecker, RegionVisualizer regionVisualizer)
		{
			this.boundChecker = boundChecker;
			this.regionVisualizer = regionVisualizer;
		}
		
		static interface BoundChecker
		{
			boolean inRegion(Region region, Vector position);
		}
		
		static interface RegionVisualizer
		{
			void visualize(Region region, Player player);
		}
	}
	
	@Override
	public RegionRaster iterator()
	{
		return new RegionRaster(orientedCorners(), world);
	}
	
	public Block get(int x, int y, int z)
	{
		Vector[] orientedCorners = orientedCorners();
		x += orientedCorners[0].getBlockX();
		y += orientedCorners[0].getBlockY();
		z += orientedCorners[0].getBlockZ();
		
		return world.getBlockAt(x, y, z);
	}
	
	public static class RegionRaster implements Iterator<Block>
	{
		final World world;
		final int xStart;
		final int zStart;
		
		int x;
		int y;
		int z;
		
		final int xStop;
		final int yStop;
		final int zStop;
		
		RegionRaster(Vector[] orientedCorners, World world)
		{
			this.world = world;
			xStart = orientedCorners[0].getBlockX();
			y = orientedCorners[0].getBlockY();
			zStart = orientedCorners[0].getBlockZ();
			
			x = xStart;
			z = zStart;
			
			xStop = orientedCorners[1].getBlockX();
			yStop = orientedCorners[1].getBlockY();
			zStop = orientedCorners[1].getBlockZ();
		}
		
		@Override
		public boolean hasNext()
		{
			return x <= xStop && y <= yStop && z <= zStop;
		}
		
		@Override
		public Block next()
		{
			Block block = world.getBlockAt(x, y, z);
			x++;
			if (x > xStop)
			{
				x = xStart;
				z++;
			}
			if (z > zStop)
			{
				z = zStart;
				y++;
			}
			return block;
		}
	}
	
	public static Location[] orientCorners(Location[] corners)
	{
		if (corners != null)
		{
			Location[] newCorners = {
					new Location(corners[0].getWorld(), 0, 0, 0),
					new Location(corners[0].getWorld(), 0, 0, 0)
			};
			if (corners[0].getBlockX() < corners[1].getBlockX())
			{
				newCorners[0].setX(corners[0].getBlockX());
				newCorners[1].setX(corners[1].getBlockX());
			}
			else
			{
				newCorners[0].setX(corners[1].getBlockX());
				newCorners[1].setX(corners[0].getBlockX());
			}
			if (corners[0].getBlockY() < corners[1].getBlockY())
			{
				newCorners[0].setY(corners[0].getBlockY());
				newCorners[1].setY(corners[1].getBlockY());
			}
			else
			{
				newCorners[0].setY(corners[1].getBlockY());
				newCorners[1].setY(corners[0].getBlockY());
			}
			if (corners[0].getBlockZ() < corners[1].getBlockZ())
			{
				newCorners[0].setZ(corners[0].getBlockZ());
				newCorners[1].setZ(corners[1].getBlockZ());
			}
			else
			{
				newCorners[0].setZ(corners[1].getBlockZ());
				newCorners[1].setZ(corners[0].getBlockZ());
			}
			return newCorners;
		}
		else
		{
			World world = Bukkit.getWorlds().get(0);
			return new Location[] {
					new Location(world, 0, 0, 0),
					new Location(world, 0, 0, 0)
			};
		}
	}
}