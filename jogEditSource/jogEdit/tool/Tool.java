package jogEdit.tool;

import jogEdit.*;
import jogUtil.data.*;
import jogUtil.richText.*;

import java.util.*;

public class Tool
{
	final ToolType type;
	final Wand.WandItem wand;
	
	Tool(ToolType type, Wand.WandItem wand)
	{
		this.type = type;
		this.wand = wand;
	}
	
	public ToolType type()
	{
		return type;
	}
	
	public Data properties()
	{
		return wand.getToolProperties(type);
	}
	
	public void setProperties(Data properties)
	{
		wand.setToolProperties(type, properties);
	}
	
	public void resetProperties()
	{
		wand.resetToolProperties(type);
	}
	
	public Wand.WandItem wand()
	{
		return wand;
	}
	
	public RichString toRichString()
	{
		RichStringBuilder builder = RichStringBuilder.start();
		builder.style(builder.style().color(RichColor.ORANGE));
		builder.append("Current Tool: ");
		builder.append(type.name(), builder.style().color(RichColor.WHITE));
		builder.newLine();
		builder.append("Tool Properties:", builder.style().color(RichColor.AQUA));
		List<ToolType.Property<?>> properties = type.properties();
		if (properties.size() == 0)
		{
			builder.newLine();
			builder.append("None.", builder.style().color(RichColor.WHITE));
		}
		else
		{
			for (ToolType.Property<?> value : properties)
			{
				builder.newLine();
				builder.append(" " + value.name + ": ");
				builder.append(value.getValue(this).toString(), builder.style().color(RichColor.WHITE));
			}
		}
		return builder.build();
	}
}