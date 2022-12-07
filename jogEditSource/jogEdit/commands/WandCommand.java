package jogEdit.commands;

import jogEdit.*;
import jogUtil.commander.*;
import jogUtil.commander.argument.*;
import jogUtil.commander.command.*;
import jogUtil.richText.*;

public abstract class WandCommand extends Command
{
	public WandCommand(Category parent, String name, RichString description)
	{
		super(parent, name, description);
		this.addFilter(new WandHoldingModule.WandHoldingExecutorFilter());
	}
	
	public WandCommand(Category parent, String name, String description)
	{
		this(parent, name, new RichString(description));
	}
	
	public WandCommand(String name, RichString description)
	{
		this(null, name, description);
	}
	
	public WandCommand(String name, String description)
	{
		this(null, name, description);
	}
	
	@Override
	protected void execute(AdaptiveInterpretation result, Executor executor)
	{
		WandHoldingModule module = executor.getModule(WandHoldingModule.class);
		Wand.WandItem[] wands = module.getWands();
		for (Wand.WandItem wand : wands)
			wandExecute(result, module, wand);
	}
	
	public abstract void wandExecute(AdaptiveInterpretation result, WandHoldingModule executor, Wand.WandItem wand);
	
	public static class SimpleWandCommand extends WandCommand
	{
		public interface WandCommandExecutor
		{
			void execute(AdaptiveInterpretation result, WandHoldingModule executor, Wand.WandItem wand);
		}
		
		final WandCommandExecutor executor;
		
		public SimpleWandCommand(Category parent, String name, RichString description, AdaptiveArgumentList arguments, WandCommandExecutor executor)
		{
			super(parent, name, description);
			if (arguments != null)
				setArgumentList(arguments);
			this.executor = executor;
		}
		
		public SimpleWandCommand(Category parent, String name, String description, AdaptiveArgumentList arguments, WandCommandExecutor executor)
		{
			this(parent, name, new RichString(description), arguments, executor);
		}
		
		public SimpleWandCommand(Category parent, String name, String description, WandCommandExecutor executor)
		{
			this(parent, name, new RichString(description), null, executor);
		}
		
		public SimpleWandCommand(Category parent, String name, RichString description, WandCommandExecutor executor)
		{
			this(parent, name, description, null, executor);
		}
		
		@Override
		public void wandExecute(AdaptiveInterpretation result, WandHoldingModule executor, Wand.WandItem wand)
		{
			this.executor.execute(result, executor, wand);
		}
	}
}