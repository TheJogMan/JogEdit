package jogEdit;

import jogEdit.region.*;
import jogUtil.*;
import jogUtil.data.*;
import jogUtil.data.values.*;
import jogUtil.indexable.*;
import org.bukkit.*;
import org.bukkit.block.data.*;

import java.io.*;
import java.util.*;

public class Clipboard
{
	private static Config.Setting<Integer> capacity;
	private static final HashMap<UUID, Entry> entries = new HashMap<>();
	private static File directory;
	
	static void init(JogEdit jogEdit)
	{
		capacity = jogEdit.config.createSetting("ClipboardCapacity", TypeRegistry.get(IntegerValue.class), new IntegerValue(1000),
												"How many schematics can be saved in the server's clipboard.");
		directory = new File(jogEdit.getDataFolder().getPath() + "/Clipboard");
		directory.mkdir();
		File[] files = directory.listFiles();
		for (File file : files)
		{
			try
			{
				new Entry(UUID.fromString(file.getName()));
			}
			catch (Exception ignored)
			{
			
			}
		}
		while (entries.size() > capacity.get())
			removeLeastRelevant();
	}
	
	static void removeLeastRelevant()
	{
		Entry oldest = null;
		for (Entry entry : entries.values())
		{
			if (oldest == null || entry.lastAccess() < oldest.lastAccess())
				oldest = entry;
		}
		if (oldest != null)
			oldest.remove();
	}
	
	public static int capacity()
	{
		return capacity.get();
	}
	
	public static Schematic getEntry(UUID id)
	{
		Entry entry = entries.get(id);
		if (entry != null)
			return entry.load();
		else
			return new Schematic(new BlockData[][][] {{{Bukkit.createBlockData(Material.AIR)}}});
	}
	
	public static UUID addEntry(Schematic schematic)
	{
		Entry entry = new Entry(UUID.randomUUID());
		entry.write(schematic);
		return entry.id;
	}
	
	public static void removeEntry(UUID id)
	{
		Entry entry = entries.get(id);
		if (entry != null)
			entry.remove();
	}
	
	private static class Entry
	{
		final UUID id;
		final File file;
		
		Entry(UUID id)
		{
			this.id = id;
			this.file = new File(directory.getPath() + "/" + id);
			entries.put(id, this);
		}
		
		void write(Schematic schematic)
		{
			try
			{
				FileOutputStream stream = new FileOutputStream(file);
				stream.write((new SchematicValue(schematic)).asBytes());
				stream.close();
				file.setLastModified(System.currentTimeMillis());
			}
			catch (IOException ignored)
			{
			
			}
		}
		
		Schematic load()
		{
			file.setLastModified(System.currentTimeMillis());
			try
			{
				Consumer.ConsumptionResult<Value<?, Schematic>, Byte> result = SchematicValue.getByteConsumer().consume((new IndexableInputStream(new FileInputStream(file))).iterator());
				if (result.success())
					return (Schematic)result.value().get();
			}
			catch (IOException ignored)
			{
			
			}
			return new Schematic(new BlockData[][][] {{{Bukkit.createBlockData(Material.AIR)}}});
		}
		
		long lastAccess()
		{
			return file.lastModified();
		}
		
		void remove()
		{
			file.delete();
			entries.remove(id);
		}
	}
}