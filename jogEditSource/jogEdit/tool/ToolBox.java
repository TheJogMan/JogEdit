package jogEdit.tool;

import jogEdit.*;
import jogEdit.tool.tools.*;
import jogUtil.*;
import jogUtil.commander.*;
import jogUtil.commander.argument.*;
import jogUtil.data.values.*;
import jogUtil.indexable.*;
import jogUtil.richText.*;

import java.util.*;

public class ToolBox
{
	static final HashMap<String, ToolType> tools = new HashMap<>();
	
	static Result register(ToolType type)
	{
		if (tools.containsValue(type))
			return new Result("Type has already been registered.");
		else if (tools.containsKey(type.name))
			return new Result("A ToolType has already been registered with that name.");
		tools.put(type.name, type);
		return new Result(true);
	}
	
	public static <ClassType extends ToolType> ClassType getType(Class<ClassType> type)
	{
		for (ToolType tool : tools.values())
		{
			if (tool.getClass().equals(type))
				return (ClassType) tool;
		}
		return null;
	}
	
	public static ToolType getType(String name)
	{
		return tools.get(name);
	}
	
	public static Tool getTool(ToolType type, Wand.WandItem wand)
	{
		return new Tool(type, wand);
	}
	
	public static List<ToolType> tools()
	{
		return new ArrayList<>(tools.values());
	}
	
	public static class ToolArgument extends PlainArgument<ToolType>
	{
		@Override
		public ReturnResult<ToolType> interpretArgument(Indexer<Character> source, Executor executor)
		{
			String name = StringValue.consumeAlphabeticalString(source);
			ToolType type = tools.get(name);
			if (type == null)
				return new ReturnResult<>("There is no tool with that name.");
			else
				return new ReturnResult<>(type);
		}
		
		@Override
		public List<String> argumentCompletions(Indexer<Character> source, Executor executor)
		{
			ArrayList<String> completions = new ArrayList<>();
			tools().forEach(type -> completions.add(type.name()));
			return completions;
		}
		
		@Override
		public void initArgument(Object[] data)
		{
		
		}
		
		@Override
		public String defaultName()
		{
			return "Tool";
		}
		
		@Override
		public RichString defaultDescription()
		{
			return null;
		}
	}
	
	public static void addTools()
	{
		new NoTool();
		new SelectionTool();
	}
}