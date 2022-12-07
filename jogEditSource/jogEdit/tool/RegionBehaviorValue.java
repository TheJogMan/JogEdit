package jogEdit.tool;

import jogLib.values.*;
import jogUtil.*;
import jogUtil.commander.*;
import jogUtil.data.*;
import jogUtil.data.values.*;
import jogUtil.indexable.*;

import java.util.*;

public class RegionBehaviorValue extends Value<RegionBehavior, RegionBehavior>
{
	public RegionBehaviorValue()
	{
		super();
	}
	
	public RegionBehaviorValue(RegionBehavior behavior)
	{
		super(behavior);
	}
	
	@Override
	public RegionBehavior emptyValue()
	{
		return RegionBehavior.IGNORE;
	}
	
	@Override
	public String asString()
	{
		return MaterialValue.convertOut(get().toString());
	}
	
	@Override
	public byte[] asBytes()
	{
		return StringValue.toByteData(get().toString());
	}
	
	@Override
	protected Value<RegionBehavior, RegionBehavior> makeCopy()
	{
		return new RegionBehaviorValue(get());
	}
	
	@Override
	protected boolean checkDataEquality(Value<?, ?> value)
	{
		return value instanceof RegionBehaviorValue other && other.get().equals(get());
	}
	
	@Override
	public void initArgument(Object[] objects)
	{
	
	}
	
	@Override
	public String defaultName()
	{
		return "Region Behavior";
	}
	
	@Override
	public List<String> argumentCompletions(Indexer<Character> indexer, Executor executor)
	{
		ArrayList<String> completions = new ArrayList<>();
		for (RegionBehavior mode : RegionBehavior.values())
			completions.add(MaterialValue.convertOut(mode.toString()));
		return completions;
	}
	
	@TypeRegistry.ByteConsumer
	public static Consumer<Value<?, RegionBehavior>, Byte> getByteConsumer()
	{
		return ((source) ->
		{
			Consumer.ConsumptionResult<Value<?, String>, Byte> result = StringValue.getByteConsumer().consume(source);
			if (!result.success())
				return new Consumer.ConsumptionResult<>(source, result.description());
			String mode = (String)result.value().get();
			
			try
			{
				return new Consumer.ConsumptionResult<>(new RegionBehaviorValue(RegionBehavior.valueOf(mode)), source);
			}
			catch (Exception e)
			{
				return new Consumer.ConsumptionResult<>(source, "Not a valid region behavior mode.");
			}
		});
	}
	
	@TypeRegistry.CharacterConsumer
	public static Consumer<Value<?, RegionBehavior>, Character> getCharacterConsumer()
	{
		return ((source) ->
		{
			int index = source.position();
			for (RegionBehavior mode : RegionBehavior.values())
			{
				source.setPosition(index);
				if (StringValue.consumeSequence(source, MaterialValue.convertOut(mode.toString())))
					return new Consumer.ConsumptionResult<>(new RegionBehaviorValue(mode), source);
			}
			
			return new Consumer.ConsumptionResult<>(source, "Not a valid region behavior mode.");
		});
	}
	
	@TypeRegistry.ValidationValues
	public static Value<?, RegionBehavior>[] validationValues()
	{
		return new RegionBehaviorValue[] {
				new RegionBehaviorValue(RegionBehavior.CONTAIN),
				new RegionBehaviorValue(RegionBehavior.EXCLUDE),
				new RegionBehaviorValue(RegionBehavior.IGNORE),
		};
	}
}