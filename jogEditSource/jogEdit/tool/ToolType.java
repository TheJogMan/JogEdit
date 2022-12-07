package jogEdit.tool;

import jogEdit.*;
import jogEdit.commands.*;
import jogUtil.*;
import jogUtil.data.*;
import jogUtil.richText.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.event.block.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;

import java.util.*;

public abstract class ToolType
{
	final String name;
	final HashMap<String, Property<?>> properties = new HashMap<>();
	final RichString description;
	
	public ToolType(String name, String description)
	{
		this(name, new RichString(description));
	}
	
	public ToolType(String name, RichString description)
	{
		this.name = name;
		this.description = description;
		Result result = ToolBox.register(this);
		if (!result.success())
			System.err.println("Could not register tool \"" + name + "\": " + result.description().encode(EncodingType.CODED));
	}
	
	public final String name()
	{
		return name;
	}
	
	public final RichString description()
	{
		return description;
	}
	
	public abstract void visualize(Player player, Tool tool);
	public abstract void interact(WandHoldingModule executor, Block block, Action action, BlockFace face, EquipmentSlot slot, PlayerInteractEvent event, Tool tool, Wand.WandItem wand);
	
	public final List<Property<?>> properties()
	{
		return new ArrayList<>(properties.values());
	}
	
	public final <PropertyType> Property<PropertyType> addProperty(Property<PropertyType> property)
	{
		if (!properties.containsKey(name))
		{
			properties.put(name, property);
			return property;
		}
		else
			throw new IllegalArgumentException("Another Property has already been registered with that name.");
	}
	
	public final <PropertyType> Property<PropertyType> addProperty(String name, Class<? extends Value<PropertyType, PropertyType>> value,
																   Value<PropertyType, PropertyType> defaultValue, String description)
	{
		return addProperty(name, value, defaultValue, new RichString(description));
	}
	
	public final <PropertyType> Property<PropertyType> addProperty(String name, Class<? extends Value<PropertyType, PropertyType>> value,
																   Value<PropertyType, PropertyType> defaultValue, RichString description)
	{
		if (!properties.containsKey(name))
		{
			Property<PropertyType> property = new Property<>(name, value, defaultValue, description);
			properties.put(name, property);
			return property;
		}
		else
			throw new IllegalArgumentException("Another Property has already been registered with that name.");
	}
	
	public Property<?> getProperty(String name)
	{
		return properties.get(name);
	}
	
	public final static class Property<PropertyType>
	{
		String name;
		RichString description;
		TypeRegistry.RegisteredType valueType;
		Value<PropertyType, PropertyType> defaultValue;
		
		public Property(String name, Class<? extends Value<PropertyType, PropertyType>> value, Value<PropertyType, PropertyType> defaultValue, String description)
		{
			this(name, value, defaultValue, new RichString(description));
		}
		
		public Property(String name, Class<? extends Value<PropertyType, PropertyType>> value, Value<PropertyType, PropertyType> defaultValue, RichString description)
		{
			if (name.indexOf(' ') != -1)
				throw new IllegalArgumentException("Property name can not contain a space character.");
			TypeRegistry.RegisteredType valueType = TypeRegistry.get(value);
			if (valueType == null)
				throw new IllegalArgumentException("That value type is not registered.");
			
			this.name = name;
			this.valueType = valueType;
			this.defaultValue = defaultValue;
			this.description = description;
		}
		
		public Value<PropertyType, PropertyType> getValue(Tool tool)
		{
			return (Value<PropertyType, PropertyType>)tool.properties().get(name, defaultValue);
		}
		
		public PropertyType get(Tool tool)
		{
			return getValue(tool).get();
		}
		
		public void reset(Tool tool)
		{
			Data data = tool.properties();
			data.remove(name);
			if (data.size() == 0)
				tool.resetProperties();
			else
				tool.setProperties(data);
		}
		
		public void set(Tool tool, PropertyType value)
		{
			Value<PropertyType, PropertyType> valueObject = defaultValue.copy();
			valueObject.set(value);
			Data data = tool.properties();
			data.put(name, valueObject);
			tool.setProperties(data);
		}
		
		public TypeRegistry.RegisteredType valueType()
		{
			return valueType;
		}
		
		public String name()
		{
			return name;
		}
		
		public RichString description()
		{
			return description;
		}
		
		public Value<PropertyType, PropertyType> defaultValue()
		{
			return defaultValue.copy();
		}
	}
}