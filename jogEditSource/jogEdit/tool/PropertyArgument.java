package jogEdit.tool;

import jogUtil.*;
import jogUtil.commander.*;
import jogUtil.commander.argument.*;
import jogUtil.data.values.*;
import jogUtil.indexable.*;

import java.util.*;

public class PropertyArgument extends PlainArgument<ToolType.Property<?>>
{
	ToolType type;
	
	@Override
	public void initArgument(Object[] objects)
	{
		type = (ToolType)objects[0];
	}
	
	@Override
	public String defaultName()
	{
		return "Property Name";
	}
	
	@Override
	public List<String> argumentCompletions(Indexer<Character> indexer, Executor executor)
	{
		ArrayList<String> names = new ArrayList<>();
		type.properties().forEach(property -> names.add(property.name()));
		return names;
	}
	
	@Override
	public ReturnResult<ToolType.Property<?>> interpretArgument(Indexer<Character> indexer, Executor executor)
	{
		String name = StringValue.consumeString(indexer, ' ');
		ToolType.Property<?> property = type.getProperty(name.toLowerCase());
		if (property == null)
			return new ReturnResult<>("This tool does not have a property with that name.");
		return new ReturnResult<>(property);
	}
}