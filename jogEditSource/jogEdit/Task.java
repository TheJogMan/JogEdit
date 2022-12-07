package jogEdit;

import jogUtil.*;
import jogUtil.data.*;
import jogUtil.data.values.*;
import jogUtil.indexable.*;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.block.data.*;

import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.util.*;

public class Task
{
	private static Config.Setting<Integer> capacity;
	private static File directory;
	
	static void init(JogEdit jogEdit)
	{
		capacity = jogEdit.config.createSetting("TaskHistoryCapacity", TypeRegistry.get(IntegerValue.class), new IntegerValue(100),
												"How many tasks can be saved in the server's task history.");
		
		capacity.addListener((oldValue, newValue) ->
		{
			while (entryCount() > newValue)
				removeLeastRelevant();
		});
		
		directory = new File(jogEdit.getDataFolder().getPath() + "/TaskHistory");
		directory.mkdir();
		while (entryCount() > capacity.get())
			removeLeastRelevant();
	}
	
	static int entryCount()
	{
		return directory.listFiles().length;
	}
	
	static void removeLeastRelevant()
	{
		File[] files = directory.listFiles();
		File oldest = null;
		for (File file : files)
		{
			if (oldest == null || file.lastModified() < oldest.lastModified())
				oldest = file;
		}
		if (oldest != null)
			oldest.delete();
	}
	
	public static TaskWriter create(String name)
	{
		if (entryCount() == capacity.get())
			removeLeastRelevant();
		
		return new TaskWriter(getFile(UUID.randomUUID()), name);
	}
	
	public static boolean exists(UUID id)
	{
		return getFile(id).exists();
	}
	
	public static TaskReader get(UUID id)
	{
		return new TaskReader(getFile(id));
	}
	
	private static File getFile(UUID id)
	{
		return new File(directory.getPath() + "/" + id);
	}
	
	public static void remove(UUID id)
	{
		getFile(id).delete();
	}
	
	public static int capacity()
	{
		return capacity.get();
	}
	
	/**
	 * Writes a task to an external source.
	 @author Joseph Delorme
	 */
	public static class TaskWriter implements AutoCloseable
	{
		String name;
		UUID id;
		BufferedOutputStream stream;
		final ArrayList<UUID> worlds = new ArrayList<>();
		final ArrayList<String> materials = new ArrayList<>();
		
		boolean closed = false;
		
		/**
		 * Creates a writer to write a task to a file.
		 * <p>
		 * The files name must be a UUID.
		 * </p>
		 @param file file to write to
		 @param name task name
		 */
		public TaskWriter(File file, String name)
		{
			try
			{
				id = UUID.fromString(file.getName().substring(0, file.getName().length() - 4));
				
				file.createNewFile();
				
				if (!file.canWrite())
					throw new RuntimeException("Do not have write access to write task to file.");
				
				try
				{
					create(new BufferedOutputStream(new FileOutputStream(file)), name);
				}
				catch (FileNotFoundException e)
				{
					e.printStackTrace();
					throw new RuntimeException("Could not find file to write task to.");
				}
			}
			catch (IllegalArgumentException e)
			{
				throw new RuntimeException("Task file must have a UUID as it's file name.");
			}
			catch (IOException e1)
			{
				throw new RuntimeException("Could not create file to write task to.");
			}
		}
		
		void create(BufferedOutputStream stream, String name)
		{
			this.stream = stream;
			this.name = name;
			
			try
			{
				writeString(stream, name);
			}
			catch (IOException e)
			{
				e.printStackTrace();
				throw new RuntimeException("Couldn't write to task");
			}
		}
		
		/**
		 * Creates a writer to write a task to an output stream
		 @param stream stream to write to
		 @param name task name
		 */
		public TaskWriter(OutputStream stream, String name)
		{
			create(new BufferedOutputStream(stream), name);
		}
		
		public UUID id()
		{
			return id;
		}
		
		public String name()
		{
			return name;
		}
		
		@Override
		public synchronized void close()
		{
			if (!closed)
			{
				try
				{
					stream.write(0);
					stream.close();
					closed = true;
				}
				catch (IOException e)
				{
					e.printStackTrace();
					throw new RuntimeException("Couldn't write to task");
				}
			}
		}
		
		int worldNumber(UUID world) throws IOException
		{
			int worldNum = worlds.indexOf(world);
			if (worldNum == -1)
			{
				worldNum = worlds.size();
				worlds.add(world);
				stream.write(1);
				stream.write(UUIDValue.toByteData(world));
			}
			return worldNum;
		}
		
		int materialNumber(String material) throws IOException
		{
			int matNum = materials.indexOf(material);
			if (matNum == -1)
			{
				matNum = materials.size();
				materials.add(material);
				stream.write(2);
				writeString(stream, material);
			}
			return matNum;
		}
		
		public synchronized void write(ChangedBlock change)
		{
			if (!closed)
			{
				try
				{
					String[] pr = fromData(change.previousData);
					String[] nw = fromData(change.newData);
					int worldNum = worldNumber(change.worldID);
					int preMatNum = materialNumber(pr[0]);
					int newMatNum = materialNumber(nw[0]);
					
					stream.write(3);
					stream.write(IntegerValue.toByteData(worldNum));
					stream.write(IntegerValue.toByteData(change.x));
					stream.write(IntegerValue.toByteData(change.y));
					stream.write(IntegerValue.toByteData(change.z));
					stream.write(IntegerValue.toByteData(preMatNum));
					writeString(stream, pr[1]);
					stream.write(IntegerValue.toByteData(newMatNum));
					writeString(stream, nw[1]);
				}
				catch (IOException e)
				{
					e.printStackTrace();
					throw new RuntimeException("Couldn't write to task");
				}
			}
		}
	}
	
	private static void writeString(BufferedOutputStream stream, String string) throws IOException
	{
		byte[] data = string.getBytes(StandardCharsets.UTF_16BE);
		stream.write(IntegerValue.toByteData(data.length));
		stream.write(data);
	}
	
	private static String readString(Indexer<Byte> source)
	{
		int length = IntegerValue.simpleByteConsume(source).value();
		ByteBuffer buffer = ByteBuffer.allocate(length);
		for (int index = 0; index < length; index++)
			buffer.put(source.next());
		return new String(buffer.array(), StandardCharsets.UTF_16BE);
	}
	
	private static String[] fromData(String str)
	{
		int pos = str.indexOf('[');
		if (pos == -1)
			return new String[] {str, "[]"};
		else
			return new String[] {str.substring(0, pos), str.substring(pos)};
	}
	
	private static String toData(String material, String data)
	{
		return material + data;
	}
	
	/**
	 * Reads a task from an external source.
	 * <p>
	 * A secondary thread is created to parse block changes from the source, attempting to retrieve changes that have already
	 * been parsed will return the change immediately however attempting to retrieve a change that has not yet been parsed will
	 * cause the retrieving thread to wait until the change has been successfully parsed or the end of the source has been reached.
	 * </p>
	 @see #next()
	 @see #fullRead()
	 */
	public static class TaskReader implements AutoCloseable
	{
		private void read()
		{
			try
			{
				byte ch = source.next();
				if (ch == 0)
				{
					foundEnd = true;
					stop();
				}
				else if (ch == 1)
				{
					UUID world = (UUID)UUIDValue.getByteConsumer().consume(source).value().get();
					worlds.add(world);
				}
				else if (ch == 2)
				{
					String material = readString(source);
					materials.add(material);
				}
				else if (ch == 3)
				{
					
					
					int worldNum = IntegerValue.simpleByteConsume(source).value();
					int x = IntegerValue.simpleByteConsume(source).value();
					int y = IntegerValue.simpleByteConsume(source).value();
					int z = IntegerValue.simpleByteConsume(source).value();
					int preMatNum = IntegerValue.simpleByteConsume(source).value();
					String preData = readString(source);
					int newMatNum = IntegerValue.simpleByteConsume(source).value();
					String newData = readString(source);
					
					ChangedBlock change = new ChangedBlock(worlds.get(worldNum), x, y, z, toData(materials.get(preMatNum), preData), toData(materials.get(newMatNum), newData));
					changes.add(change);
					wakeWaiters();
				}
			}
			catch (Exception e)
			{
				error = e;
			}
		}
		
		String name;
		UUID id;
		Indexer<Byte> source;
		IndexableInputStream eater;
		
		final ArrayList<UUID> worlds = new ArrayList<>();
		final ArrayList<String> materials = new ArrayList<>();
		
		boolean foundEnd = false;
		boolean fileWasIncomplete = false;
		Exception error;
		int position = 0;
		final ArrayList<ChangedBlock> changes = new ArrayList<>();
		final ParsingThread thread = new ParsingThread();
		
		/**
		 * Creates a task reader from an input stream
		 @param stream stream to read task from
		 @param id task ID
		 */
		public TaskReader(InputStream stream, UUID id)
		{
			create(new BufferedInputStream(stream), id);
		}
		
		void create(BufferedInputStream stream, UUID id)
		{
			this.id = id;
			eater = new IndexableInputStream(stream);
			source = eater.iterator();
			
			name = readString(source);
			
			thread.start();
		}
		
		/**
		 * Creates a task reader from a file
		 * <p>
		 * File name must be a UUID."
		 * </p>
		 @param file file to read from
		 */
		public TaskReader(File file)
		{
			if (!file.canRead())
				throw new RuntimeException("Do not have read access to read task from file.");
			
			try
			{
				UUID id = UUID.fromString(file.getName());
				
				try
				{
					create(new BufferedInputStream(new FileInputStream(file)), id);
				}
				catch (FileNotFoundException e)
				{
					throw new RuntimeException("Could not find file to read task from.");
				}
			}
			catch (IllegalArgumentException e)
			{
				throw new RuntimeException("Task file must have a UUID as it's file name.");
			}
		}
		
		/**
		 * Gets this task's ID
		 @return task ID
		 */
		public UUID id()
		{
			return id;
		}
		
		/**
		 * Gets this task's name
		 @return task name
		 */
		public String name()
		{
			return name;
		}
		
		private class ParsingThread extends Thread
		{
			boolean stopped = false;
			
			@Override
			public void run()
			{
				while (!atEnd() && !foundEnd && error == null)
				{
					read();
				}
				end();
			}
			
			void end()
			{
				synchronized (waitingPoint)
				{
					eater.stop();
					thread.stopped = true;
					if (!foundEnd)
						fileWasIncomplete = true;
					wakeWaiters();
					wakeFullReadWaiters();
				}
			}
		}
		
		@Override
		public void close()
		{
			stop();
		}
		
		/**
		 * Stops the parsing thread, even if it hasn't fully parsed the input stream.
		 */
		public void stop()
		{
			thread.end();
		}
		
		private final WaitingPoint waitingPoint = new WaitingPoint();
		private final WaitingPoint fullReadPoint = new WaitingPoint();
		
		private static class WaitingPoint
		{
			boolean hasWaiting = false;
		}
		
		public final void waitForData()
		{
			synchronized(waitingPoint)
			{
				if (!thread.stopped)
				{
					waitingPoint.hasWaiting = true;
					while (waitingPoint.hasWaiting)
					{
						try
						{
							waitingPoint.wait();
						}
						catch (InterruptedException ignored)
						{
						
						}
					}
				}
			}
		}
		
		public final void waitForFullRead()
		{
			synchronized(fullReadPoint)
			{
				if (!thread.stopped)
				{
					fullReadPoint.hasWaiting = true;
					while (fullReadPoint.hasWaiting)
					{
						try
						{
							fullReadPoint.wait();
						}
						catch (InterruptedException ignored)
						{
						
						}
					}
				}
			}
		}
		
		protected final void wakeWaiters()
		{
			synchronized(waitingPoint)
			{
				waitingPoint.hasWaiting = false;
				waitingPoint.notifyAll();
			}
		}
		
		protected final void wakeFullReadWaiters()
		{
			synchronized(fullReadPoint)
			{
				fullReadPoint.hasWaiting = false;
				fullReadPoint.notifyAll();
			}
		}
		
		/**
		 * Checks if the input stream didn't have a proper ending
		 @return if the input stream ended properly
		 */
		public boolean sourceWasIncomplete()
		{
			return fileWasIncomplete;
		}
		
		/**
		 * Checks if the parser has encountered an exception
		 @return if an exception was encountered
		 */
		public boolean encounteredException()
		{
			return error != null;
		}
		
		/**
		 * Returns the exception that was encountered by the parser.
		 @return parsing exception, or null if no exception has been encountered.
		 */
		public Exception exception()
		{
			return error;
		}
		
		/**
		 * Checks if the parser is still processing the input source.
		 @return if the end has been reached
		 */
		public boolean atEnd()
		{
			return thread.stopped;
		}
		
		/**
		 * Checks if there is currently at least on block change that can be retrieved without having to wait for more to load.
		 @return if at least one change is available
		 */
		public boolean hasNext()
		{
			return available() > 0;
		}
		
		/**
		 * Gets the current position for stepping through the sequence
		 @return current position
		 */
		public int getPosition()
		{
			return position;
		}
		
		/**
		 * Sets the current position for stepping through the sequence
		 @param position
		 */
		public void setPosition(int position)
		{
			this.position = position;
		}
		
		private void ensureDataPresence()
		{
			if (!hasNext() && !atEnd())
				waitForData();
		}
		
		/**
		 * Returns the number of block changes that are currently loaded
		 @return loaded change count
		 */
		public int size()
		{
			return changes.size();
		}
		
		/**
		 * Returns the number of block changes that are currently available to retrieve without having to wait for more to load.
		 @return available change count
		 */
		public int available()
		{
			return changes.size() - position;
		}
		
		/**
		 * Returns the next block change in the sequence
		 * <p>
		 * If the next change still needs to be parsed from the input stream, this thread will wait until that process finishes.
		 * </p>
		 @return The next block change in the sequence, or null if the end of the sequence has been reached.
		 @see #hasNext()
		 @see #atEnd()
		 */
		public ChangedBlock next()
		{
			ensureDataPresence();
			if (hasNext() && position >= 0 && position < changes.size())
			{
				int index = position;
				position++;
				return changes.get(index);
			}
			else
				return null;
		}
		
		/**
		 * Waits until the input source has been fully parsed.
		 */
		public void fullRead()
		{
			waitForFullRead();
		}
	}
	
	public static class ChangedBlock
	{
		final UUID worldID;
		final int x;
		final int y;
		final int z;
		
		final String previousData;
		final String newData;
		
		public ChangedBlock(Block block, BlockData previousData, BlockData newData)
		{
			this(block.getLocation(), previousData, newData);
		}
		
		public ChangedBlock(Location location, BlockData previousData, BlockData newData)
		{
			this(location.getWorld() == null ? null : location.getWorld().getUID(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), previousData, newData);
		}
		
		public ChangedBlock(UUID worldID, int x, int y, int z, BlockData previousData, BlockData newData)
		{
			this(worldID, x, y, z, previousData.getAsString(), newData.getAsString());
		}
		
		ChangedBlock(UUID worldID, int x, int y, int z, String previousData, String newData)
		{
			this.worldID = worldID;
			this.x = x;
			this.y = y;
			this.z = z;
			this.previousData = previousData;
			this.newData = newData;
		}
		
		public boolean equals(ChangedBlock change)
		{
			return change != null && locationMatch(change) && change.previousData.matches(previousData) && change.newData.matches(newData);
		}
		
		private boolean locationMatch(ChangedBlock change)
		{
			boolean worldEquals = (worldID == null && change.worldID == null) || worldID.equals(change.worldID);
			return worldEquals && x == change.x && y == change.y && z == change.z;
		}
		
		@Override
		public ChangedBlock clone()
		{
			return new ChangedBlock(worldID, x, y, z, previousData, newData);
		}
		
		public Block block()
		{
			World world = Bukkit.getWorld(worldID);
			if (world == null)
				world = Bukkit.getWorlds().get(0);
			return world.getBlockAt(x, y, z);
		}
		
		public void undo(BlockChanger changer)
		{
			System.out.println(previousData);
			changer.change(block(), Bukkit.createBlockData(previousData));
		}
		
		public void redo(BlockChanger changer)
		{
			changer.change(block(), Bukkit.createBlockData(newData));
		}
		
		@Override
		public String toString()
		{
			return worldID + "," + x + "," + y + "," + z + "," + previousData + "," + newData;
		}
	}
}