package dev.demeng.ultrarepair;

import dev.demeng.demlib.DemLib;
import dev.demeng.demlib.api.Common;
import dev.demeng.demlib.api.DeveloperNotifications;
import dev.demeng.demlib.api.Registerer;
import dev.demeng.demlib.api.commands.CommandSettings;
import dev.demeng.demlib.api.connections.SpigotUpdateChecker;
import dev.demeng.demlib.api.files.CustomConfig;
import dev.demeng.demlib.api.messages.MessageUtils;
import dev.demeng.ultrarepair.commands.RepairAllCmd;
import dev.demeng.ultrarepair.commands.RepairCmd;
import dev.demeng.ultrarepair.commands.UltraRepairCmd;
import dev.demeng.ultrarepair.commands.subcommands.ReloadCmd;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class UltraRepair extends JavaPlugin {

  @Getter private static UltraRepair instance;

  @Getter private CustomConfig settingsFile, messagesFile;

  private static final int SETTINGS_VERSION = 1;
  private static final int MESSAGES_VERSION = 1;

  @Getter private CommandSettings commandSettings;

  @Getter private Economy economy;

  @Override
  public void onEnable() {

    instance = this;
    DemLib.setPlugin(this);
    MessageUtils.setPrefix("&7[&1UltraRepair&7] &r");

    MessageUtils.consoleWithoutPrefix(
        "Enabling UltraRepair...\n\n"
            + "&1 ____ ___.__   __               __________                    .__        \n"
            + "&1|    |   \\  |_/  |_____________ \\______   \\ ____ ___________  |__|______ \n"
            + "&1|    |   /  |\\   __\\_  __ \\__  \\ |       _// __ \\\\____ \\__  \\ |  \\_  __ \\\n"
            + "&1|    |  /|  |_|  |  |  | \\// __ \\|    |   \\  ___/|  |_> > __ \\|  ||  | \\/\n"
            + "&1|______/ |____/__|  |__|  (____  /____|_  /\\___  >   __(____  /__||__|   \n"
            + "&1                               \\/       \\/     \\/|__|       \\/           \n\n");

    MessageUtils.log("Loading configuration files...");
    if (!loadFiles()) return;

    getLogger().info("Registering commands...");
    this.commandSettings = new CommandSettings();
    commandSettings.setNotPlayerMessage(getMessages().getString("console"));
    commandSettings.setNoPermissionMessage(getMessages().getString("no-perms"));
    commandSettings.setIncorrectUsageMessage("");

    Registerer.registerCommand(new UltraRepairCmd(this));
    Registerer.registerCommand(new ReloadCmd(this));
    Registerer.registerCommand(new RepairCmd(this));
    Registerer.registerCommand(new RepairAllCmd(this));

    getLogger().info("Registering listeners...");
    DeveloperNotifications.enableNotifications("ca19af04-a156-482e-a35d-3f5f434975b5");

    getLogger().info("Hooking into Vault...");
    if (!setupEconomy()) {
      MessageUtils.error(null, 3, "Failed to hook into Vault.", true);
      return;
    }

    getLogger().info("Loading metrics...");
    new Metrics(this, 3712);

    getLogger().info("Checking for updates...");
    SpigotUpdateChecker.checkForUpdates(63035);

    MessageUtils.console(
        "&aUltraRepair v" + Common.getVersion() + " by Demeng " + "has been successfully enabled!");
  }

  @Override
  public void onDisable() {
    MessageUtils.console(
        "&cUltraRepair v"
            + Common.getVersion()
            + " by Demeng "
            + "has been successfully disabled.");
  }

  private boolean loadFiles() {

    try {
      settingsFile = new CustomConfig("settings.yml");
      messagesFile = new CustomConfig("messages.yml");

    } catch (final Exception ex) {
      MessageUtils.error(ex, 1, "Failed to load configuration files.", true);
      return false;
    }

    if (!settingsFile.configUpToDate(SETTINGS_VERSION)) {
      MessageUtils.error(null, 2, "Outdated settings file.", true);
      return false;
    }

    if (!messagesFile.configUpToDate(MESSAGES_VERSION)) {
      MessageUtils.error(null, 2, "Outdated messages file.", true);
      return false;
    }

    MessageUtils.setPrefix(getMessages().getString("prefix"));

    return true;
  }

  public FileConfiguration getSettings() {
    return settingsFile.getConfig();
  }

  public FileConfiguration getMessages() {
    return messagesFile.getConfig();
  }

  private boolean setupEconomy() {
    if (getServer().getPluginManager().getPlugin("Vault") == null) {
      return false;
    }
    RegisteredServiceProvider<Economy> rsp =
        getServer().getServicesManager().getRegistration(Economy.class);
    if (rsp == null) {
      return false;
    }
    economy = rsp.getProvider();
    return economy != null;
  }
}
