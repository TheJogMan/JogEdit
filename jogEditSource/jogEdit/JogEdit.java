package jogEdit;

import jogLib.*;
import jogUtil.*;
import jogUtil.data.*;
import jogUtil.data.values.*;
import org.bukkit.*;

public class JogEdit extends JogPlugin
{
	static boolean worldEditCompatibilityMode = false;
	static Config.Setting<Boolean> doParallelization;
	
	@Override
	public void onEnable()
	{
		if (Bukkit.getPluginManager().isPluginEnabled("WorldEdit") || Bukkit.getPluginManager().isPluginEnabled("FAWE"))
			worldEditCompatibilityMode = true;
		
		doParallelization = config.createSetting("DoParallelization", TypeRegistry.get(BooleanValue.class), new BooleanValue(true),
												 "Should asynchronous operations be run in parallel threads.");
	}
	
	public static boolean doParallelization()
	{
		return doParallelization.get();
	}
	
	public static boolean worldEditCompatibilityMode()
	{
		return worldEditCompatibilityMode;
	}
}