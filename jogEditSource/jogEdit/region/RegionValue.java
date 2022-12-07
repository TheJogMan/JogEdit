package jogEdit.region;

import jogUtil.data.values.*;
import org.bukkit.*;
import org.bukkit.util.Vector;
import jogLib.values.*;
import jogUtil.*;
import jogUtil.commander.*;
import jogUtil.data.*;
import jogUtil.indexable.*;
import jogUtil.richText.*;

import java.util.*;

public class RegionValue extends Value<Region, Region>
{
	public RegionValue()
	{
		super();
	}
	
	public RegionValue(Region region)
	{
		super(region);
	}
	
	@Override
	public Region emptyValue()
	{
		return new Region(null, new Vector(0, 0, 0), new Vector(0, 0, 0));
	}
	
	@Override
	public String asString()
	{
		return toString(get());
	}
	
	public static String toString(Region region)
	{
		return "{World: " + (region.world() == null ? "None." : region.world().getName()) + ", " +
			   "Corner 1: " + VectorValue.toString(region.corner1) + "," +
			   "Corner 2: " + VectorValue.toString(region.corner2) + ", " +
			   "Type: " + RegionTypeValue.toString(region.type) + "}";
	}
	
	public static RichString toRichString(Region region)
	{
		RichStringBuilder builder = RichStringBuilder.start();
		builder.style(builder.style().color(RichColor.ORANGE));
		builder.append("{", builder.style().color(RichColor.AQUA));
		builder.append("World: ");
		builder.append((region.world == null ? "None." : region.world.getName()), builder.style().color(RichColor.WHITE));
		builder.append(", Corner 1: ").append(VectorValue.toRichString(region.corner1)).append(", Corner 2: ").append(VectorValue.toRichString(region.corner2)).append(", Dimensions: ")
			   .append(VectorValue.toRichString(region.dimensions())).append(", Volume: ");
		builder.append(region.volume() + "", builder.style().color(RichColor.WHITE));
		builder.append("}", builder.style().color(RichColor.AQUA));
		return builder.build();
	}
	
	@Override
	public byte[] asBytes()
	{
		Region region = get();
		ByteArrayBuilder builder = new ByteArrayBuilder();
		UUID id;
		if (region.world != null)
			id = region.world.getUID();
		else
			id = new UUID(0, 0);
		
		builder.add(id);
		builder.add(VectorValue.toBytes(region.corner1));
		builder.add(VectorValue.toBytes(region.corner2));
		builder.add(RegionTypeValue.toBytes(region.type));
		
		return builder.toPrimitiveArray();
	}
	
	@Override
	protected Value<Region, Region> makeCopy()
	{
		return new RegionValue(get().clone());
	}
	
	@Override
	protected boolean checkDataEquality(Value<?, ?> value)
	{
		return value instanceof RegionValue other && other.get().equals(get());
	}
	
	@Override
	public void initArgument(Object[] objects)
	{
	
	}
	
	@Override
	public String defaultName()
	{
		return "Region";
	}
	
	@Override
	public List<String> argumentCompletions(Indexer<Character> indexer, Executor executor)
	{
		return null;
	}
	
	@TypeRegistry.ByteConsumer
	public static Consumer<Value<?, Region>, Byte> getByteConsumer()
	{
		return ((source) ->
		{
			Consumer.ConsumptionResult<Value<?, UUID>, Byte> uuidResult = UUIDValue.getByteConsumer().consume(source);
			if (!uuidResult.success())
				return new Consumer.ConsumptionResult<>(source, new RichStringBuilder("Could not parse world ID: ").append(uuidResult.description()).build());
			
			Consumer.ConsumptionResult<Value<?, Vector>, Byte> corner1Result = VectorValue.getByteConsumer().consume(source);
			if (!corner1Result.success())
				return new Consumer.ConsumptionResult<>(source, new RichStringBuilder("Could not parse corner 1: ").append(corner1Result.description()).build());
			
			Consumer.ConsumptionResult<Value<?, Vector>, Byte> corner2Result = VectorValue.getByteConsumer().consume(source);
			if (!corner2Result.success())
				return new Consumer.ConsumptionResult<>(source, new RichStringBuilder("Could not parse corner 2: ").append(corner2Result.description()).build());
			
			Consumer.ConsumptionResult<Value<?, Region.RegionType>, Byte> typeResult = RegionTypeValue.getByteConsumer().consume(source);
			if (!typeResult.success())
				return new Consumer.ConsumptionResult<>(source, new RichStringBuilder("Could not parse region type: ").append(typeResult.description()).build());
			
			World world;
			UUID id = (UUID)uuidResult.value().get();
			if (id.getLeastSignificantBits() == 0 && id.getMostSignificantBits() == 0)
				world = null;
			else
				world = Bukkit.getWorld(id);
			
			Region region = new Region(world, (Vector)corner1Result.value().get(), (Vector)corner2Result.value().get());
			region.type = (Region.RegionType)typeResult.value().get();
			return new Consumer.ConsumptionResult<>(new RegionValue(region), source);
		});
	}
	
	@TypeRegistry.CharacterConsumer
	public static Consumer<Value<?, Region>, Character> getCharacterConsumer()
	{
		return ((source) ->
		{
			source.pushFilterState();
			source.addFilter(new Indexer.ExclusionFilter<>(' '));
			
			if (!StringValue.consumeSequence(source, "{world:", false))
				return new Consumer.ConsumptionResult<>(source, "Must begin with '{world:'");
			
			if (!StringValue.consumeSequence(source, ",corner1:", false))
				return new Consumer.ConsumptionResult<>(source, "World name must be followed by ',corner1:'");
			
			Consumer.ConsumptionResult<Value<?, Vector>, Character> corner1Result = VectorValue.getCharacterConsumer().consume(source);
			if (!corner1Result.success())
				return new Consumer.ConsumptionResult<>(source, RichStringBuilder.start("Could not parse corner 1: ").append(corner1Result.description()).build());
			
			if (!StringValue.consumeSequence(source, ",corner2:", false))
				return new Consumer.ConsumptionResult<>(source, "Corner 1 must be followed by ',corner2:'");
			
			Consumer.ConsumptionResult<Value<?, Vector>, Character> corner2Result = VectorValue.getCharacterConsumer().consume(source);
			if (!corner2Result.success())
				return new Consumer.ConsumptionResult<>(source, RichStringBuilder.start("Could not parse corner 2: ").append(corner2Result.description()).build());
			
			if (!StringValue.consumeSequence(source, ",type:", false))
				return new Consumer.ConsumptionResult<>(source, "Corner 2 must be followed by ',type:'");
			
			Consumer.ConsumptionResult<Value<?, Region.RegionType>, Character> typeResult = RegionTypeValue.getCharacterConsumer().consume(source);
			if (!typeResult.success())
				return new Consumer.ConsumptionResult<>(source, RichStringBuilder.start("Could not parse region type: ").append(typeResult.description()).build());
			
			source.popFilterState();
			if (source.atEnd() || source.next() != '}')
				return new Consumer.ConsumptionResult<>(source, "Must end with '}");
			
			String name = StringValue.consumeString(source, ',');
			World world;
			if (name.equalsIgnoreCase("none."))
				world = null;
			else
				world = Bukkit.getWorld(name);
			
			Region region = new Region(world, (Vector)corner1Result.value().get(), (Vector)corner2Result.value().get());
			region.type = (Region.RegionType)typeResult.value().get();
			return new Consumer.ConsumptionResult<>(new RegionValue(region), source);
		});
	}
	
	@TypeRegistry.ValidationValues
	public static Value<?, Region>[] validationValues()
	{
		return new RegionValue[] {
			new RegionValue(new Region(null, new Vector(1, 0, 2.5), new Vector(0, 2, 0)))
		};
	}
}