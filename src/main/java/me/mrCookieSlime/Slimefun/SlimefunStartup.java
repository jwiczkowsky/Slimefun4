package me.mrCookieSlime.Slimefun;

import java.io.File;

import me.mrCookieSlime.Slimefun.listeners.*;
import me.mrCookieSlime.Slimefun.tasks.ArmorUpdateTask;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import me.mrCookieSlime.CSCoreLibPlugin.CSCoreLib;
import me.mrCookieSlime.CSCoreLibPlugin.PluginUtils;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.CSCoreLibPlugin.general.Reflection.ReflectionUtils;
import me.mrCookieSlime.Slimefun.AncientAltar.Pedestals;
import me.mrCookieSlime.Slimefun.CSCoreLibSetup.CSCoreLibLoader;
import me.mrCookieSlime.Slimefun.Commands.SlimefunCommand;
import me.mrCookieSlime.Slimefun.Commands.SlimefunTabCompleter;
import me.mrCookieSlime.Slimefun.GEO.OreGenSystem;
import me.mrCookieSlime.Slimefun.GEO.Resources.NetherIceResource;
import me.mrCookieSlime.Slimefun.GEO.Resources.OilResource;
import me.mrCookieSlime.Slimefun.GPS.Elevator;
import me.mrCookieSlime.Slimefun.GitHub.GitHubConnector;
import me.mrCookieSlime.Slimefun.GitHub.GitHubSetup;
import me.mrCookieSlime.Slimefun.Hashing.ItemHash;
import me.mrCookieSlime.Slimefun.Lists.SlimefunItems;
import me.mrCookieSlime.Slimefun.Objects.MultiBlock;
import me.mrCookieSlime.Slimefun.Objects.Research;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunArmorPiece;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.AContainer;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.machines.AutoEnchanter;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.machines.ElectricDustWasher;
import me.mrCookieSlime.Slimefun.Setup.Files;
import me.mrCookieSlime.Slimefun.Setup.Messages;
import me.mrCookieSlime.Slimefun.Setup.MiscSetup;
import me.mrCookieSlime.Slimefun.Setup.ResearchSetup;
import me.mrCookieSlime.Slimefun.Setup.SlimefunManager;
import me.mrCookieSlime.Slimefun.Setup.SlimefunSetup;
import me.mrCookieSlime.Slimefun.URID.AutoSavingTask;
import me.mrCookieSlime.Slimefun.URID.URID;
import me.mrCookieSlime.Slimefun.WorldEdit.WESlimefunManager;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.Slimefun;
import me.mrCookieSlime.Slimefun.api.SlimefunBackup;
import me.mrCookieSlime.Slimefun.api.TickerTask;
import me.mrCookieSlime.Slimefun.api.energy.ChargableBlock;
import me.mrCookieSlime.Slimefun.api.energy.EnergyNet;
import me.mrCookieSlime.Slimefun.api.energy.ItemEnergy;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.CargoNet;
import me.mrCookieSlime.Slimefun.api.item_transport.ChestManipulator;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;

public class SlimefunStartup extends JavaPlugin {

	public static SlimefunStartup instance;

	private static PluginUtils utils;
	private static Config researches;
	private static Config items;
	private static Config whitelist;
	private static Config config;

	public static TickerTask ticker;

	private CoreProtectAPI coreProtectAPI;

	private boolean clearlag = false;
	private boolean exoticGarden = false;
	private boolean coreProtect = false;

	// Supported Versions of Minecraft
	private final String[] supported = {"v1_9_", "v1_10_", "v1_11_", "v1_12_"};
	private final String[] supportedHumanReadable = {"v1.9.X", "v1.10.X", "v1.11.X", "v1.12.X"};

	@SuppressWarnings("deprecation")
	@Override
	public void onEnable() {
		CSCoreLibLoader loader = new CSCoreLibLoader(this);

		if (!loader.load()) {
			return;
		}

		if (!isVersionCompatible()) {
			getLogger().severe("### Slimefun failed to load!");
			getLogger().severe("###");
			getLogger().severe("### You are using the wrong Version of Minecraft!!!");
			getLogger().severe("###");
			getLogger().severe("### You are using Minecraft " + ReflectionUtils.getVersion());
			getLogger().severe("### but Slimefun v" + getDescription().getVersion() + " requires you to be using");
			getLogger().severe("### Minecraft " + StringUtils.join(supportedHumanReadable, ", "));
			getLogger().severe("###");
			getLogger().severe("### Please use an older Version of Slimefun and disable auto-updating");
			getLogger().severe("### or consider updating your Server Software.");

			getServer().getPluginManager().disablePlugin(this);

			return;
		}

		instance = this;
		getLogger().info("[Slimefun] Loading Files...");
		Files.cleanup();

		getLogger().info("[Slimefun] Loading Config...");

		utils = new PluginUtils(this);
		utils.setupConfig();

		// Loading all extra configs
		researches = new Config(Files.RESEARCHES);
		items = new Config(Files.ITEMS);
		whitelist = new Config(Files.WHITELIST);

		// Init Config, Updater, Metrics and messages.yml
		utils.setupUpdater(53485, getFile());
		utils.setupMetrics();
		utils.setupLocalization();
		config = utils.getConfig();
		Messages.local = utils.getLocalization();
		Messages.setup();

		// Creating all necessary Folders
		createDirs(
				new File("data-storage/Slimefun/blocks"),
				new File("data-storage/Slimefun/stored-blocks"),
				new File("data-storage/Slimefun/stored-inventories"),
				new File("data-storage/Slimefun/stored-chunks"),
				new File("data-storage/Slimefun/universal-inventories"),
				new File("data-storage/Slimefun/waypoints"),
				new File("data-storage/Slimefun/block-backups"),
				new File("plugins/Slimefun/scripts"),
				new File("plugins/Slimefun/generators"),
				new File("plugins/Slimefun/error-reports"),
				new File("plugins/Slimefun/cache/github")
		);

		SlimefunManager.plugin = this;

		getLogger().info("[Slimefun] Loading Items...");

		MiscSetup.setupItemSettings();

		try {
			SlimefunSetup.setupItems();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		MiscSetup.loadDescriptions();

		getLogger().info("[Slimefun] Loading Researches...");
		Research.enabled = getResearchCfg().getBoolean("enable-researching");
		ResearchSetup.setupResearches();

		MiscSetup.setupMisc();

		BlockStorage.info_delay = config.getInt("URID.info-delay");

		getLogger().info("[Slimefun] Loading World Generators...");

		// Generating Oil as an OreGenResource (its a cool API)
		OreGenSystem.registerResource(new OilResource());
		OreGenSystem.registerResource(new NetherIceResource());

		// Setting up GitHub Connectors...

		GitHubSetup.setup();

		// All Slimefun Listeners
		new ArmorListener(this);
		new ItemListener(this);
		new BlockListener(this);
		new GearListener(this);
		new AutonomousToolsListener(this);
		new DamageListener(this);
		new BowListener(this);
		new ToolListener(this);
		new FurnaceListener(this);
		new TeleporterListener(this);
		new AndroidKillingListener(this);
		new NetworkListener(this);
		new WorldLoadUnloadListener(this);
		new PlayerQuitListener(this);

		if (ReflectionUtils.getVersion().startsWith("v1_12_")) {
			new ItemPickupListener_1_12(this);
		} else {
			new ItemPickupListener(this);
		}

		// Toggleable Listeners for performance
		if (config.getBoolean("items.talismans")) new TalismanListener(this);
		if (config.getBoolean("items.backpacks")) new BackpackListener(this);
		if (config.getBoolean("items.coolers")) new CoolerListener(this);

		// Handle Slimefun Guide being given on Join
		if (config.getBoolean("options.give-guide-on-first-join")) {
			new PlayerJoinListener(this);
		}

		// Initiating various Stuff and all Items with a slightly delay (0ms after the Server finished loading)
		getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
			Slimefun.emeraldenchants = getServer().getPluginManager().isPluginEnabled("EmeraldEnchants");
			SlimefunGuide.all_recipes = config.getBoolean("options.show-vanilla-recipes-in-guide");
			MiscSetup.loadItems();

			for (World world : Bukkit.getWorlds()) {
				new BlockStorage(world);
			}

			if (SlimefunItem.getByID("ANCIENT_ALTAR") != null)
				new AncientAltarListener((SlimefunStartup) instance);
		}, 0);

		// WorldEdit Hook to clear Slimefun Data upon //set 0 //cut or any other equivalent
		if (getServer().getPluginManager().isPluginEnabled("WorldEdit")) {
			try {
				Class.forName("com.sk89q.worldedit.extent.Extent");
				new WESlimefunManager();
				getLogger().info("[Slimefun] Successfully hooked into WorldEdit!");
			} catch (Exception x) {
				getLogger().severe("[Slimefun] Failed to hook into WorldEdit!");
				getLogger().severe("[Slimefun] Maybe consider updating WorldEdit or Slimefun?");
			}
		}

		getCommand("slimefun").setExecutor(new SlimefunCommand(this));
		getCommand("slimefun").setTabCompleter(new SlimefunTabCompleter());

		// Armor Update Task
		if (config.getBoolean("options.enable-armor-effects")) {
			new ArmorUpdateTask(this);
		}

		ticker = new TickerTask();

		// Starting all ASYNC Tasks
		getServer().getScheduler().scheduleAsyncRepeatingTask(this, new AutoSavingTask(), 1200L, config.getInt("options.auto-save-delay-in-minutes") * 60L * 20L);
		getServer().getScheduler().scheduleAsyncRepeatingTask(this, ticker, 100L, config.getInt("URID.custom-ticker-delay"));

		getServer().getScheduler().scheduleAsyncRepeatingTask(this, () -> {
			for (GitHubConnector connector : GitHubConnector.connectors) {
				connector.pullFile();
			}
		}, 80L, 60 * 60 * 20L);

		// Hooray!
		getLogger().info("[Slimefun] Finished!");

		clearlag = getServer().getPluginManager().isPluginEnabled("ClearLag");

		coreProtect = getServer().getPluginManager().isPluginEnabled("CoreProtect");

		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new BukkitRunnable() {
			@Override
			public void run() {
				exoticGarden = getServer().getPluginManager().isPluginEnabled("ExoticGarden"); //Had to do it this way, otherwise it seems disabled.
			}
		}, 0);

		if (clearlag) new ClearLaggIntegration(this);

		if (coreProtect)
			coreProtectAPI = ((CoreProtect) getServer().getPluginManager().getPlugin("CoreProtect")).getAPI();

		Research.creative_research = config.getBoolean("options.allow-free-creative-research");

		AutoEnchanter.max_emerald_enchantments = config.getInt("options.emerald-enchantment-limit");

		SlimefunSetup.legacy_ore_washer = config.getBoolean("options.legacy-ore-washer");
		ElectricDustWasher.legacy_dust_washer = config.getBoolean("options.legacy-dust-washer");

		// Do not show /sf elevator command in our Log, it could get quite spammy
		CSCoreLib.getLib().filterLog("([A-Za-z0-9_]{3,16}) issued server command: /sf elevator (.{0,})");
	}

	@Override
	public void onDisable() {
		Bukkit.getScheduler().cancelTasks(this);

		// Finishes all started movements/removals of block data
		ticker.HALTED = true;
		ticker.run();

		try {
			for (World world : Bukkit.getWorlds()) {
				BlockStorage storage = BlockStorage.getStorage(world);
				if (storage != null) {
					storage.save(true);
				} else {
					getLogger().severe("[Slimefun] Could not save Slimefun Blocks for World \"" + world.getName() + "\"");
				}
			}

			SlimefunBackup.start();
		} catch (Exception x) {
		}

		// Prevent Memory Leaks
		config = null;
		researches = null;
		items = null;
		whitelist = null;
		instance = null;
		Messages.local = null;
		Files.CONFIG = null;
		Files.DATABASE = null;
		Files.ITEMS = null;
		Files.RESEARCHES = null;
		Files.WHITELIST = null;
		MultiBlock.list = null;
		Research.list = null;
		Research.researching = null;
		SlimefunItem.all = null;
		SlimefunItem.items = null;
		SlimefunItem.map_id = null;
		SlimefunItem.handlers = null;
		SlimefunItem.radioactive = null;
		Variables.damage = null;
		Variables.jump = null;
		Variables.mode = null;
		SlimefunGuide.history = null;
		Variables.altarinuse = null;
		Variables.enchanting = null;
		Variables.backpack = null;
		Variables.soulbound = null;
		Variables.blocks = null;
		Variables.cancelPlace = null;
		Variables.arrows = null;
		SlimefunCommand.arguments = null;
		SlimefunCommand.descriptions = null;
		SlimefunCommand.tabs = null;
		URID.objects = null;
		URID.ids = null;
		SlimefunItem.blockhandler = null;
		BlockMenuPreset.presets = null;
		BlockStorage.loaded_tickers = null;
		BlockStorage.ticking_chunks = null;
		BlockStorage.worlds = null;
		ChargableBlock.capacitors = null;
		ChargableBlock.max_charges = null;
		AContainer.processing = null;
		AContainer.progress = null;
		Slimefun.guide_handlers = null;
		Pedestals.recipes = null;
		Elevator.ignored = null;
		EnergyNet.listeners = null;
		EnergyNet.machines_input = null;
		EnergyNet.machines_output = null;
		EnergyNet.machines_storage = null;
		CargoNet.faces = null;
		BlockStorage.universal_inventories = null;
		TickerTask.block_timings = null;
		OreGenSystem.map = null;
		SlimefunGuide.contributors = null;
		GitHubConnector.connectors = null;
		ChestManipulator.listeners = null;
		ItemHash.digest = null;
		ItemHash.map = null;

		for (Player p : Bukkit.getOnlinePlayers()) {
			p.closeInventory();
		}
	}

	public static Config getCfg() {
		return config;
	}

	public static Config getResearchCfg() {
		return researches;
	}

	public static Config getItemCfg() {
		return items;
	}

	public static Config getWhitelist() {
		return whitelist;
	}

	private void createDirs(File... files) {
		for (File file : files) {
			if (file.exists()) continue;

			file.mkdirs();
		}
	}

	private boolean isVersionCompatible() {
		String currentVersion = ReflectionUtils.getVersion();

		if (!currentVersion.startsWith("v")) return false;

		boolean isCompatible = false;

		for (String version : supported) {
			if (currentVersion.startsWith(version)) {
				isCompatible = true;

				break;
			}
		}

		return isCompatible;
	}

	public static int randomize(int max) {
		if (max < 1) return 0;
		return CSCoreLib.randomizer().nextInt(max);
	}

	public static boolean chance(int max, int percentage) {
		if (max < 1) return false;
		return CSCoreLib.randomizer().nextInt(max) <= percentage;
	}

	public boolean isClearLagInstalled() {
		return clearlag;
	}

	public boolean isExoticGardenInstalled() {
		return exoticGarden;
	}

	public boolean isCoreProtectInstalled() {
		return coreProtect;
	}

	public CoreProtectAPI getCoreProtectAPI() {
		return coreProtectAPI;
	}
}
