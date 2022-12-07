package jogEdit;

import org.bukkit.inventory.*;
import org.bukkit.util.Vector;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;

public class Visualizer implements Runnable
{
	public static final long tickInterval = 5;
	
	@Override
	public void run()
	{
		for (Player player : Bukkit.getOnlinePlayers()) renderPlayerWands(player);
	}
	
	public static void renderPlayerWands(Player player)
	{
		ItemStack item = player.getInventory().getItemInMainHand();
		if (Wand.isWand(item))
			renderWand(player, (Wand.WandItem)Wand.getCustomObject(item));
		item = player.getInventory().getItemInOffHand();
		if (Wand.isWand(item))
			renderWand(player, (Wand.WandItem)Wand.getCustomObject(item));
	}
	
	public static void renderWand(Player player, Wand.WandItem wand)
	{
		if (wand.hasRegion())
		{
			wand.getRegion().visualize(player);
		}
		wand.getToolType().visualize(player, wand.getTool());
	}
	
	static void renderLine(Player player, double startX, double startY, double startZ, double endX, double endY, double endZ, Particle particle, double size, double interval)
	{
		renderLine(player, new Vector(startX, startY, startZ), new Vector(endX, endY, endZ), particle, size, interval);
	}
	
	static void renderLine(Player player, Vector start, Vector end, Particle particle, double size, double interval)
	{
		double length = start.distance(end);
		length /= interval;
		Vector step = end.clone().subtract(start);
		step.divide(new Vector(length, length, length));
		for (int stepNum = 0; stepNum <= length; stepNum++)
		{
			renderPoint(player, start, particle, size);
			start.add(step);
		}
	}
	
	static void renderPoint(Player player, Vector point, Particle particle, double size)
	{
		renderPoint(player, point.getX(), point.getY(), point.getZ(), particle, size);
	}
	
	static void renderPoint(Player player, double x, double y, double z, Particle particle, double size)
	{
		Location location = new Location(player.getWorld(), x, y, z);
		if (location.distance(player.getLocation()) < 150)
			player.spawnParticle(particle, x + .5, y + .5, z + .5, 1, size, size, size, 0);
	}
	
	public static void highlightRegion(Vector[] corners, Player player, Particle particle, double size, double interval)
	{
		renderLine(player,corners[0].getBlockX()-.5,corners[0].getBlockY()-.5,corners[0].getBlockZ()-.5,
				   corners[1].getBlockX()+.5,corners[0].getBlockY()-.5,corners[0].getBlockZ()-.5,particle,size,interval);
		renderLine(player,corners[0].getBlockX()-.5,corners[0].getBlockY()-.5,corners[0].getBlockZ()-.5,
				   corners[0].getBlockX()-.5,corners[0].getBlockY()-.5,corners[1].getBlockZ()+.5,particle,size,interval);
		renderLine(player,corners[1].getBlockX()+.5,corners[0].getBlockY()-.5,corners[0].getBlockZ()-.5,
				   corners[1].getBlockX()+.5,corners[0].getBlockY()-.5,corners[1].getBlockZ()+.5,particle,size,interval);
		renderLine(player,corners[0].getBlockX()-.5,corners[0].getBlockY()-.5,corners[1].getBlockZ()+.5,
				   corners[1].getBlockX()+.5,corners[0].getBlockY()-.5,corners[1].getBlockZ()+.5,particle,size,interval);
		
		renderLine(player,corners[0].getBlockX()-.5,corners[1].getBlockY()+.5,corners[0].getBlockZ()-.5,
				   corners[1].getBlockX()+.5,corners[1].getBlockY()+.5,corners[0].getBlockZ()-.5,particle,size,interval);
		renderLine(player,corners[0].getBlockX()-.5,corners[1].getBlockY()+.5,corners[0].getBlockZ()-.5,
				   corners[0].getBlockX()-.5,corners[1].getBlockY()+.5,corners[1].getBlockZ()+.5,particle,size,interval);
		renderLine(player,corners[1].getBlockX()+.5,corners[1].getBlockY()+.5,corners[0].getBlockZ()-.5,
				   corners[1].getBlockX()+.5,corners[1].getBlockY()+.5,corners[1].getBlockZ()+.5,particle,size,interval);
		renderLine(player,corners[0].getBlockX()-.5,corners[1].getBlockY()+.5,corners[1].getBlockZ()+.5,
				   corners[1].getBlockX()+.5,corners[1].getBlockY()+.5,corners[1].getBlockZ()+.5,particle,size,interval);
		
		renderLine(player,corners[0].getBlockX()-.5,corners[0].getBlockY()-.5,corners[0].getBlockZ()-.5,
				   corners[0].getBlockX()-.5,corners[1].getBlockY()+.5,corners[0].getBlockZ()-.5,particle,size,interval);
		renderLine(player,corners[1].getBlockX()+.5,corners[0].getBlockY()-.5,corners[0].getBlockZ()-.5,
				   corners[1].getBlockX()+.5,corners[1].getBlockY()+.5,corners[0].getBlockZ()-.5,particle,size,interval);
		renderLine(player,corners[0].getBlockX()-.5,corners[0].getBlockY()-.5,corners[1].getBlockZ()+.5,
				   corners[0].getBlockX()-.5,corners[1].getBlockY()+.5,corners[1].getBlockZ()+.5,particle,size,interval);
		renderLine(player,corners[1].getBlockX()+.5,corners[0].getBlockY()-.5,corners[1].getBlockZ()+.5,
				   corners[1].getBlockX()+.5,corners[1].getBlockY()+.5,corners[1].getBlockZ()+.5,particle,size,interval);
	}
	
	public static void highlightBlock(Vector blockPosition, Player player)
	{
		highlightBlock(player.getWorld().getBlockAt(blockPosition.getBlockX(), blockPosition.getBlockY(), blockPosition.getBlockZ()), player);
	}
	
	public static void highlightBlock(Block block, Player player)
	{
		if (block == null)
			return;
		
		renderLine(player, block.getX() - .5, block.getY() + .5, block.getZ() - .5,
				   block.getX() + .5, block.getY() + .5, block.getZ() - .5, Particle.FLAME, 0, .25);
		renderLine(player, block.getX() - .5, block.getY() + .5, block.getZ() + .5,
				   block.getX() + .5, block.getY() + .5, block.getZ() + .5, Particle.FLAME, 0, .25);
		renderLine(player, block.getX() - .5, block.getY() + .5, block.getZ() - .5,
				   block.getX() - .5, block.getY() + .5, block.getZ() + .5, Particle.FLAME, 0, .25);
		renderLine(player, block.getX() + .5, block.getY() + .5, block.getZ() - .5,
				   block.getX() + .5, block.getY() + .5, block.getZ() + .5, Particle.FLAME, 0, .25);
		
		renderLine(player, block.getX() - .5, block.getY() - .5, block.getZ() - .5,
				   block.getX() + .5, block.getY() - .5, block.getZ() - .5, Particle.FLAME, 0, .25);
		renderLine(player, block.getX() - .5, block.getY() - .5, block.getZ() + .5,
				   block.getX() + .5, block.getY() - .5, block.getZ() + .5, Particle.FLAME, 0, .25);
		renderLine(player, block.getX() - .5, block.getY() - .5, block.getZ() - .5,
				   block.getX() - .5, block.getY() - .5, block.getZ() + .5, Particle.FLAME, 0, .25);
		renderLine(player, block.getX() + .5, block.getY() - .5, block.getZ() - .5,
				   block.getX() + .5, block.getY() - .5, block.getZ() + .5, Particle.FLAME, 0, .25);
		
		renderLine(player, block.getX() - .5, block.getY() - .5, block.getZ() - .5,
				   block.getX() - .5, block.getY() + .5, block.getZ() - .5, Particle.FLAME, 0, .25);
		renderLine(player, block.getX() + .5, block.getY() - .5, block.getZ() + .5,
				   block.getX() + .5, block.getY() + .5, block.getZ() + .5, Particle.FLAME, 0, .25);
		renderLine(player, block.getX() + .5, block.getY() - .5, block.getZ() - .5,
				   block.getX() + .5, block.getY() + .5, block.getZ() - .5, Particle.FLAME, 0, .25);
		renderLine(player, block.getX() - .5, block.getY() - .5, block.getZ() + .5,
				   block.getX() - .5, block.getY() + .5, block.getZ() + .5, Particle.FLAME, 0, .25);
	}
}