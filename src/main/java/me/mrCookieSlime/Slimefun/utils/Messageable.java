package me.mrCookieSlime.Slimefun.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public interface Messageable {

	default String colorize(String text) {
		return ChatColor.translateAlternateColorCodes('&', text);
	}

	default void sendMessage(CommandSender sender, String message) {
		sender.sendMessage(colorize(message));
	}
}
