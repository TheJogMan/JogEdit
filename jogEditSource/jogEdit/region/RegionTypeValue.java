package jogEdit.region;

import jogUtil.*;
import jogUtil.commander.*;
import jogUtil.data.*;
import jogUtil.data.values.*;
import jogUtil.indexable.*;
import jogUtil.richText.*;

import java.util.*;

public class RegionTypeValue extends Value<Region.RegionType, Region.RegionType>
{
	public RegionTypeValue()
	{
		super();
	}
	
	public RegionTypeValue(Region.RegionType type)
	{
		super(type);
	}
	
	@Override
	public Region.RegionType emptyValue()
	{
		return Region.RegionType.RECTANGULAR;
	}
	
	@Override
	public String asString()
	{
		return toString(get());
	}
	
	public static String toString(Region.RegionType type)
	{
		return type.toString();
	}
	
	public static byte[] toBytes(Region.RegionType type)
	{
		return StringValue.toByteData(type.name());
	}
	
	@Override
	public byte[] asBytes()
	{
		return toBytes(get());
	}
	
	@Override
	protected Value<Region.RegionType, Region.RegionType> makeCopy()
	{
		return new RegionTypeValue(get());
	}
	
	@Override
	protected boolean checkDataEquality(Value<?, ?> value)
	{
		return value instanceof RegionTypeValue other && other.get().equals(get());
	}
	
	@Override
	public void initArgument(Object[] objects)
	{
	
	}
	
	@Override
	public String defaultName()
	{
		return "Region Type";
	}
	
	@Override
	public List<String> argumentCompletions(Indexer<Character> indexer, Executor executor)
	{
		ArrayList<String> completions = new ArrayList<>();
		for (Region.RegionType type : Region.RegionType.values())
			completions.add(type.toString());
		return completions;
	}
	
	@TypeRegistry.ByteConsumer
	public static Consumer<Value<?, Region.RegionType>, Byte> getByteConsumer()
	{
		return ((source) ->
		{
			Consumer.ConsumptionResult<Value<?, String>, Byte> result = StringValue.getByteConsumer().consume(source);
			if (!result.success())
				return new Consumer.ConsumptionResult<>(source, RichStringBuilder.start("Could not parse: ").append(result.description()).build());
			
			try
			{
				return new Consumer.ConsumptionResult<>(new RegionTypeValue(Region.RegionType.valueOf((String)result.value().get())), source);
			}
			catch (Exception e)
			{
				return new Consumer.ConsumptionResult<>(source, "Not a valid region type.");
			}
		});
	}
	
	@TypeRegistry.CharacterConsumer
	public static Consumer<Value<?, Region.RegionType>, Character> getCharacterConsumer()
	{
		return ((source) ->
		{
			String name = StringValue.consumeAlphabeticalString(source);
			
			try
			{
				return new Consumer.ConsumptionResult<>(new RegionTypeValue(Region.RegionType.valueOf(name)), source);
			}
			catch (Exception e)
			{
				return new Consumer.ConsumptionResult<>(source, "Not a valid region type.");
			}
		});
	}
	
	@TypeRegistry.ValidationValues
	public static Value<?, Region.RegionType>[] validationValues()
	{
		return new RegionTypeValue[] {
				new RegionTypeValue(Region.RegionType.RECTANGULAR)
		};
	}
}