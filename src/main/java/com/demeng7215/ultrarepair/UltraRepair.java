package com.demeng7215.ultrarepair;

import com.demeng7215.demlib.DemLib;
import com.demeng7215.demlib.api.Common;
import com.demeng7215.demlib.api.DeveloperNotifications;
import com.demeng7215.demlib.api.Registerer;
import com.demeng7215.demlib.api.connections.SpigotUpdateChecker;
import com.demeng7215.demlib.api.files.CustomConfig;
import com.demeng7215.demlib.api.messages.MessageUtils;
import com.demeng7215.ultrarepair.commands.RepairAllCmd;
import com.demeng7215.ultrarepair.commands.RepairCmd;
import com.demeng7215.ultrarepair.commands.UltraRepairCmd;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class UltraRepair extends JavaPlugin {

  /* ERROR CODES
  1: Failed to load files.
  2: Outdated config.
  3: Failed to hook into Vault.
   */

  @Getter private static UltraRepair instance;

  @Getter private CustomConfig settingsFile, messagesFile;

  private static final int SETTINGS_VERSION = 1;
  private static final int MESSAGES_VERSION = 1;

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
    Registerer.registerCommand(new UltraRepairCmd(this));
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
    new Metrics(this);

    getLogger().info("Checking for updates...");
    SpigotUpdateChecker.checkForUpdates(63035);

    MessageUtils.console(
        "&aUltraRepair v"
            + Common.getVersion()
            + " by Demeng7215 "
            + "has been successfully enabled!");
  }

  @Override
  public void onDisable() {
    MessageUtils.console(
        "&cUltraRepair v"
            + Common.getVersion()
            + " by Demeng7215 "
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
