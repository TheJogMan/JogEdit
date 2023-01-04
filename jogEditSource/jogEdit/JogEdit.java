package jogEdit;

import jogEdit.commands.*;
import jogEdit.operation.*;
import jogEdit.region.*;
import jogEdit.tool.*;
import jogLib.*;
import jogLib.command.executor.*;
import jogLib.command.filter.*;
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
	
	public static final UUID sessionID = UUID.randomUUID();
	
	@Override
	public void onEnable()
	{
		TypeRegistry.RegistrationQueue.start()
			.add("Region", RegionValue.class)
			.add("RegionType", RegionTypeValue.class)
			.add("Schematic", SchematicValue.class)
			.add("RegionBehaviorData", RegionBehaviorValue.class)
		.process();
		
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