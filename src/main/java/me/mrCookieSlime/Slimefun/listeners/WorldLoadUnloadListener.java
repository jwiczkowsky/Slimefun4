package me.mrCookieSlime.Slimefun.listeners;

import me.mrCookieSlime.Slimefun.SlimefunStartup;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

public class WorldLoadUnloadListener implements Listener {

	private final SlimefunStartup plugin;

	public WorldLoadUnloadListener(SlimefunStartup plugin) {
		this.plugin = plugin;

		this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
	}

	@EventHandler
	public void onWorldLoad(WorldLoadEvent e) {
		BlockStorage.getForcedStorage(e.getWorld());

		SlimefunStartup.getWhitelist().setDefaultValue(e.getWorld().getName() + ".enabled", true);
		SlimefunStartup.getWhitelist().setDefaultValue(e.getWorld().getName() + ".enabled-items.SLIMEFUN_GUIDE", true);
		SlimefunStartup.getWhitelist().save();
	}

	@EventHandler
	public void onWorldUnload(WorldUnloadEvent e) {
		BlockStorage storage = BlockStorage.getStorage(e.getWorld());
		if (storage != null) {
			storage.save(true);
		} else {
			this.plugin.getLogger().severe("[Slimefun] Could not save Slimefun Blocks for World \"" + e.getWorld().getName() + "\"");
		}
	}
}
