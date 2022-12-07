package jogEdit.region;

import org.bukkit.block.data.*;
import org.bukkit.util.Vector;

import java.util.*;

public class Schematic implements Iterable<BlockData>
{
	int offX, offY, offZ;
	BlockData[][][] data;
	
	public Schematic(BlockData[][][] data)
	{
		this(data, 0, 0, 0);
	}
	
	public Schematic(BlockData[][][] data, int offX, int offY, int offZ)
	{
		if (!verifyUniformity(data))
			throw new IllegalArgumentException("Provided data must be uniform in size.");
		
		this.data = data;
		this.offX = offX;
		this.offY = offY;
		this.offZ = offZ;
	}
	
	public Schematic(Region region)
	{
		this(region, 0, 0, 0);
	}
	
	public Schematic(Region region, int offX, int offY, int offZ)
	{
		this.offX = offX;
		this.offY = offY;
		this.offZ = offZ;
		
		Vector dimensions = region.dimensions();
		data = new BlockData[dimensions.getBlockX()][dimensions.getBlockY()][dimensions.getBlockZ()];
		for (SchematicDataIterator iterator = iterator(); iterator.hasNext();)
		{
			data[iterator.x()][iterator.y()][iterator.z()] = region.get(iterator().x, iterator().y, iterator().z).getBlockData();
			iterator.next();
		}
	}
	
	public static boolean verifyUniformity(BlockData[][][] data)
	{
		//we are not uniform when null
		if (data == null)
			return false;
		//we are uniform when empty
		if (data.length == 0)
			return true;
		//if we are not empty, then we must have data in all dimensions
		if (data[0].length == 0 || data[0][0].length == 0)
			return false;
		
		int height = data[0].length;
		int depth = data[0][0].length;
		for (BlockData[][] slice : data)
		{
			if (slice.length != height)
				return false;
			for (BlockData[] row : slice)
				if (row.length != depth)
					return false;
		}
		
		return true;
	}
	
	@Override
	public Schematic clone()
	{
		BlockData[][][] clonedData = new BlockData[width()][height()][depth()];
		for (SchematicDataIterator iterator = iterator(); iterator.hasNext();)
		{
			clonedData[iterator.x()][iterator.y()][iterator.z()] = iterator.next();
		}
		return new Schematic(clonedData, offX, offY, offZ);
	}
	
	public boolean equals(Object object)
	{
		if (!(object instanceof Schematic other))
			return false;
		if (other.offX != offX || other.offY != offY || other.offZ != offZ)
			return false;
		if (other.width() != width() || other.height() != height() || other.depth() != depth())
			return false;
		
		for (SchematicDataIterator iterator = iterator(); iterator.hasNext();)
		{
			if (!other.get(iterator.x(), iterator.y(), iterator.z()).equals(iterator.next()))
				return false;
		}
		
		return true;
	}
	
	public int width()
	{
		return data.length;
	}
	
	public int height()
	{
		return data.length > 0 ? data[0].length : 0;
	}
	
	public int depth()
	{
		return data.length > 0 ? data[0][0].length : 0;
	}
	
	public int offX()
	{
		return offX;
	}
	
	public int offY()
	{
		return offY;
	}
	
	public int offZ()
	{
		return offZ;
	}
	
	public BlockData get(int x, int y, int z)
	{
		if (x >= 0 && x < width() && y>= 0 && y < height() && z >= 0 && z < depth())
			return data[x][y][z];
		else
			throw new IndexOutOfBoundsException();
	}
	
	@Override
	public SchematicDataIterator iterator()
	{
		return new SchematicDataIterator(this);
	}
	
	public static class SchematicDataIterator implements Iterator<BlockData>
	{
		final Schematic schematic;
		int x, y, z = 0;
		
		public SchematicDataIterator(Schematic schematic)
		{
			this.schematic = schematic;
		}
		
		@Override
		public boolean hasNext()
		{
			return x <= schematic.width() && y <= schematic.height() && z <= schematic.depth();
		}
		
		@Override
		public BlockData next()
		{
			BlockData data = schematic.data[x][y][z];
			x++;
			if (x > schematic.width())
			{
				x = 0;
				z++;
			}
			if (z > schematic.depth())
			{
				z = 0;
				y++;
			}
			return data;
		}
		
		public int x()
		{
			return x;
		}
		
		public int y()
		{
			return y;
		}
		
		public int z()
		{
			return z;
		}
	}
}