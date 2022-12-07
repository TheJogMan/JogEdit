package jogEdit.region;

import jogUtil.*;
import jogUtil.commander.*;
import jogUtil.commander.argument.*;
import jogUtil.indexable.*;

import java.util.*;

public class CornerArgument extends PlainArgument<Boolean>
{
	@Override
	public void initArgument(Object[] objects)
	{
	
	}
	
	@Override
	public String defaultName()
	{
		return "Region Corner";
	}
	
	@Override
	public List<String> argumentCompletions(Indexer<Character> indexer, Executor executor)
	{
		ArrayList<String> list = new ArrayList<>();
		list.add("1");
		list.add("2");
		return list;
	}
	
	@Override
	public ReturnResult<Boolean> interpretArgument(Indexer<Character> indexer, Executor executor)
	{
		char ch = indexer.next();
		if (ch == '1')
			return new ReturnResult<>(true);
		else if (ch == '2')
			return new ReturnResult<>(false);
		else
			return new ReturnResult<>("Not a valid region corner.");
	}
}