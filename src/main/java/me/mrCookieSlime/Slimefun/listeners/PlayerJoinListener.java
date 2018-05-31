package me.mrCookieSlime.Slimefun.listeners;

import me.mrCookieSlime.Slimefun.SlimefunGuide;
import me.mrCookieSlime.Slimefun.SlimefunStartup;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

	private final SlimefunStartup plugin;

	public PlayerJoinListener(SlimefunStartup plugin) {
		this.plugin = plugin;

		this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		if (!event.getPlayer().hasPlayedBefore()) {
			Player p = event.getPlayer();
			if (!SlimefunStartup.getWhitelist().getBoolean(p.getWorld().getName() + ".enabled")) return;
			if (!SlimefunStartup.getWhitelist().getBoolean(p.getWorld().getName() + ".enabled-items.SLIMEFUN_GUIDE"))
				return;

			p.getInventory().addItem(SlimefunGuide.getItem(SlimefunStartup.getCfg().getBoolean("guide.default-view-book")));
		}
	}
}
