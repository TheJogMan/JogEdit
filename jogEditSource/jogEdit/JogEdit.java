package jogEdit;

import jogEdit.commands.*;
import jogEdit.operation.*;
import jogEdit.region.*;
import jogEdit.tool.*;
import jogLib.*;
import jogLib.command.executor.*;
import jogLib.command.filter.*;
import jogLib.values.*;
import jogUtil.*;
import jogUtil.commander.*;
import jogUtil.commander.argument.*;
import jogUtil.commander.command.*;
import jogUtil.data.*;
import jogUtil.data.values.*;
import org.bukkit.*;
import org.bukkit.entity.*;

import java.util.*;

public class JogEdit extends JogPlugin
{
	static Wand wand;
	static Config.Setting<Boolean> doParallelization;
	
	public static final UUID instanceID = UUID.randomUUID();
	
	@Override
	public void onEnable()
	{
		registerValues();
		
		doParallelization = config.createSetting("DoParallelization", TypeRegistry.get(BooleanValue.class), new BooleanValue(true),
												 "Should asynchronous operations be run in parallel threads.");
		
		Clipboard.init(this);
		Task.init(this);
		OperationBox.addOperations();
		ToolBox.addTools();
		wand = new Wand(this);
		new WandCategory();
		new GetWand(JogLib.commandConsole);
		
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Visualizer(), 0, Visualizer.tickInterval);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new BlockChanger.ChangeHandler(this), 0, 1);
	}
	
	private static ReturnResult<Result[]> registerValues()
	{
		Object[][] typeClasses = {
				{"Region", NamespacedKeyValue.class},
				{"RegionType", LocationValue.class},
				{"Schematic", SchematicValue.class},
				{"RegionBehavior", RegionBehaviorValue.class}
		};
		
		Result[] registrationResults = new Result[typeClasses.length];
		for (int index = 0; index < typeClasses.length; index++)
		{
			Class<? extends Value<?, ?>> typeClass = (Class<? extends Value<?, ?>>)typeClasses[index][1];
			String name = (String)typeClasses[index][0];
			Result result = TypeRegistry.register(name, typeClass);
			if (!result.success())
				throw new RuntimeException("Could not register value: " + result.description());
			registrationResults[index] = result;
		}
		
		return new ReturnResult<>(registrationResults);
	}
	
	public static Wand wand()
	{
		return wand;
	}
	
	public static boolean doParallelization()
	{
		return doParallelization.get();
	}
	
	public static boolean worldEditCompatibilityMode()
	{
		return Bukkit.getPluginManager().isPluginEnabled("WorldEdit") || Bukkit.getPluginManager().isPluginEnabled("FAWE");
	}
	
	public static class GetWand extends Command
	{
		public GetWand(Category parent)
		{
			super(parent, "GetWand", "Get a wand.");
			addFilter(new OperatorFilter());
			addFilter(new PlayerFilter());
		}
		
		@Override
		public void execute(AdaptiveInterpretation result, Executor executor)
		{
			Player player = ((PlayerExecutor)executor).sender();
			player.getInventory().addItem(JogEdit.wand().createStack());
		}
	}
}