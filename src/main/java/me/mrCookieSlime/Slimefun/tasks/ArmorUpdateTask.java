package me.mrCookieSlime.Slimefun.tasks;

import me.mrCookieSlime.Slimefun.Lists.SlimefunItems;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunArmorPiece;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import me.mrCookieSlime.Slimefun.Setup.SlimefunManager;
import me.mrCookieSlime.Slimefun.SlimefunStartup;
import me.mrCookieSlime.Slimefun.api.Slimefun;
import me.mrCookieSlime.Slimefun.api.energy.ItemEnergy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class ArmorUpdateTask extends BukkitRunnable {

	private final SlimefunStartup plugin;

	public ArmorUpdateTask(SlimefunStartup plugin) {
		this.plugin = plugin;

		runTaskTimer(this.plugin,
				0L,
				SlimefunStartup.getCfg().getInt("options.armor-update-interval") * 20L
		);
	}

	@Override
	public void run() {
		for (Player p : Bukkit.getOnlinePlayers()) {
			for (ItemStack armor : p.getInventory().getArmorContents()) {
				if (armor == null) continue;

				if (!Slimefun.hasUnlocked(p, armor, true)) return;

				if (SlimefunItem.getByItem(armor) instanceof SlimefunArmorPiece) {
					for (PotionEffect effect : ((SlimefunArmorPiece) SlimefunItem.getByItem(armor)).getEffects()) {
						p.removePotionEffect(effect.getType());
						p.addPotionEffect(effect);
					}
				}

				if (SlimefunManager.isItemSimiliar(armor, SlimefunItem.getItem("SOLAR_HELMET"), false)) {
					if (!(p.getWorld().getTime() < 12300 || p.getWorld().getTime() > 23850)) continue;
					if (!(p.getEyeLocation().getBlock().getLightFromSky() == 15)) continue;

					ItemEnergy.chargeInventory(p, Float.valueOf(String.valueOf(Slimefun.getItemValue("SOLAR_HELMET", "charge-amount"))));
				}
			}

			for (ItemStack radioactive : SlimefunItem.radioactive) {
				if (!(p.getInventory().containsAtLeast(radioactive, 1) || SlimefunManager.isItemSimiliar(p.getInventory().getItemInOffHand(), radioactive, true))) {
					continue;
				}

				// Check if player is wearing the hazmat suit
				// If so, break the loop
				if (SlimefunManager.isItemSimiliar(SlimefunItems.SCUBA_HELMET, p.getInventory().getHelmet(), true) &&
						SlimefunManager.isItemSimiliar(SlimefunItems.HAZMATSUIT_CHESTPLATE, p.getInventory().getChestplate(), true) &&
						SlimefunManager.isItemSimiliar(SlimefunItems.HAZMATSUIT_LEGGINGS, p.getInventory().getLeggings(), true) &&
						SlimefunManager.isItemSimiliar(SlimefunItems.RUBBER_BOOTS, p.getInventory().getBoots(), true)) {
					break;
				}

				// If the item is enabled in the world, then make radioactivity do its job
				if (Slimefun.isEnabled(p, radioactive, false)) {
					p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 400, 3));
					p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 400, 3));
					p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 400, 3));
					p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 400, 3));
					p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 400, 1));
					p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 400, 1));
					p.setFireTicks(400);
					break; // Break the loop to save some calculations
				}
			}
		}
	}
}
