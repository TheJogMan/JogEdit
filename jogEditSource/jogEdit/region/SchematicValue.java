package jogEdit.region;

import jogLib.values.*;
import jogUtil.*;
import jogUtil.commander.*;
import jogUtil.data.*;
import jogUtil.data.values.*;
import jogUtil.indexable.*;
import jogUtil.richText.*;
import org.bukkit.*;
import org.bukkit.block.data.*;

import java.util.*;

public class SchematicValue extends Value<Schematic, Schematic>
{
	public SchematicValue()
	{
		super();
	}
	
	public SchematicValue(Schematic schematic)
	{
		super(schematic);
	}
	
	public static Data toData(Schematic clipboard)
	{
		Data data = new Data();
		data.put("Offset X", new IntegerValue(clipboard.offX));
		data.put("Offset Y", new IntegerValue(clipboard.offY));
		data.put("Offset Z", new IntegerValue(clipboard.offZ));
		
		ListValue<ListValue<ListValue<BlockDataValue>>> blockData = new ListValue<>(TypeRegistry.get(ListValue.class));
		for (int x = 0; x < clipboard.data.length; x++)
		{
			ListValue<ListValue<BlockDataValue>> column = new ListValue<>(TypeRegistry.get(ListValue.class));
			for (int y = 0; y < clipboard.data[x].length; y++)
			{
				ListValue<BlockDataValue> row = new ListValue<>(TypeRegistry.get(BlockDataValue.class));
				for (int z = 0; z < clipboard.data[x][y].length; z++)
				{
					row.add(new BlockDataValue(clipboard.data[x][y][z]));
				}
				column.add(row);
			}
			blockData.add(column);
		}
		data.put("Block Data", blockData);
		return data;
	}
	
	public static SchematicValue fromData(Data data)
	{
		int offX = (int)data.get("Offset X", new IntegerValue(0)).get();
		int offY = (int)data.get("Offset Y", new IntegerValue(0)).get();
		int offZ = (int)data.get("Offset Z", new IntegerValue(0)).get();
		
		ListValue<ListValue<ListValue<BlockDataValue>>> blockDataValue;
		blockDataValue = (ListValue<ListValue<ListValue<BlockDataValue>>>)data.get("Block Data", new ListValue<>(TypeRegistry.get(ListValue.class)));
		BlockData[][][] blockData = new BlockData[blockDataValue.size()][][];
		for (int x = 0; x < blockDataValue.size(); x++)
		{
			ListValue<ListValue<BlockDataValue>> columnValue = blockDataValue.get(x);
			BlockData[][] column = new BlockData[columnValue.size()][];
			for (int y = 0; y < columnValue.size(); y++)
			{
				ListValue<BlockDataValue> rowValue = columnValue.get(y);
				BlockData[] row = new BlockData[rowValue.size()];
				for (int z = 0; z < rowValue.size(); z++)
					row[z] = rowValue.get(z).get();
				column[y] = row;
			}
			blockData[x] = column;
		}
		
		return new SchematicValue(new Schematic(blockData, offX, offY, offZ));
	}
	
	@Override
	public Schematic emptyValue()
	{
		return new Schematic(new BlockData[0][0][0]);
	}
	
	@Override
	public String asString()
	{
		return toData(get()).toString();
	}
	
	@Override
	public byte[] asBytes()
	{
		return toData(get()).toByteData();
	}
	
	@Override
	protected Value<Schematic, Schematic> makeCopy()
	{
		return new SchematicValue(get().clone());
	}
	
	@Override
	protected boolean checkDataEquality(Value<?, ?> value)
	{
		return value instanceof SchematicValue other && other.get().equals(get());
	}
	
	@Override
	public void initArgument(Object[] objects)
	{
	
	}
	
	@Override
	public String defaultName()
	{
		return "Schematic";
	}
	
	@Override
	public List<String> argumentCompletions(Indexer<Character> indexer, Executor executor)
	{
		return null;
	}
	
	@TypeRegistry.ByteConsumer
	public static Consumer<Value<?, Schematic>, Byte> getByteConsumer()
	{
		return ((source) ->
		{
			Consumer.ConsumptionResult<Value<?, Data>, Byte> result = DataValue.getByteConsumer().consume(source);
			if (!result.success())
				return new Consumer.ConsumptionResult<>(source, RichStringBuilder.start("Could not parse data: ").append(result.description()).build());
			else
				return new Consumer.ConsumptionResult<>(fromData((Data)result.value().get()), source);
		});
	}
	
	@TypeRegistry.CharacterConsumer
	public static Consumer<Value<?, Schematic>, Character> getCharacterConsumer()
	{
		return ((source) ->
		{
			Consumer.ConsumptionResult<Value<?, Data>, Character> result = DataValue.getCharacterConsumer().consume(source);
			if (!result.success())
				return new Consumer.ConsumptionResult<>(source, RichStringBuilder.start("Could not parse data: ").append(result.description()).build());
			else
				return new Consumer.ConsumptionResult<>(fromData((Data)result.value().get()), source);
		});
	}
	
	@TypeRegistry.ValidationValues
	public static Value<?, Schematic>[] validationValues()
	{
		BlockData[][][] data = new BlockData[2][2][2];
		data[0][0][0] = Bukkit.createBlockData(Material.AIR);
		data[1][0][0] = Bukkit.createBlockData(Material.AIR);
		data[0][0][1] = Bukkit.createBlockData(Material.AIR);
		data[1][0][1] = Bukkit.createBlockData(Material.AIR);
		data[0][1][0] = Bukkit.createBlockData(Material.STONE);
		data[1][1][0] = Bukkit.createBlockData(Material.AIR);
		data[0][1][1] = Bukkit.createBlockData(Material.AIR);
		data[1][1][1] = Bukkit.createBlockData(Material.AIR);
		
		return new SchematicValue[] {new SchematicValue(new Schematic(data))};
	}
}