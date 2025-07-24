package dev.demeng.ultrarepair;

import dev.demeng.pluginbase.BaseSettings;
import dev.demeng.pluginbase.Common;
import dev.demeng.pluginbase.Schedulers;
import dev.demeng.pluginbase.UpdateChecker;
import dev.demeng.pluginbase.UpdateChecker.Result;
import dev.demeng.pluginbase.YamlConfig;
import dev.demeng.pluginbase.locale.reader.ConfigLocaleReader;
import dev.demeng.pluginbase.plugin.BasePlugin;
import dev.demeng.pluginbase.text.Text;
import dev.demeng.ultrarepair.command.RepairCmd;
import dev.demeng.ultrarepair.command.UltraRepairCmd;
import dev.demeng.ultrarepair.manager.RepairManager;
import java.io.IOException;
import java.util.Locale;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.bukkit.BukkitCommandHandler;

public final class UltraRepair extends BasePlugin {

  @Getter @Setter(AccessLevel.PACKAGE) private static UltraRepair instance;

  @Getter private YamlConfig settingsFile;
  @Getter private YamlConfig messagesFile;
  @Getter private YamlConfig menusFile;

  private static final int SETTINGS_VERSION = 2;
  private static final int MESSAGES_VERSION = 3;
  private static final int MENUS_VERSION = 1;

  @Getter private boolean economyEnabled;

  @Getter private RepairManager repairManager;

  @Override
  public void enable() {

    setInstance(this);

    Text.coloredConsole("\n\n"
        + "&1 ____ ___  __________ \n"
        + "&1|    |   \\ \\______   \\\n"
        + "&1|    |   /  |       _/\n"
        + "&9|    |  /   |    |   \\\n"
        + "&9|______/    |____|_  /\n"
        + "&9                   \\/ \n");

    getLogger().info("Loading configuration files...");
    if (!loadFiles()) {
      return;
    }

    getLogger().info("Initializing base settings...");
    updateBaseSettings();
    getTranslator().add(new ConfigLocaleReader(getMessages(), Locale.ENGLISH));

    getLogger().info("Checking economy integration...");
    if (!checkEconomy()) {
      getLogger().warning("Vault and/or economy plugin not found! All repairs will be free.");
    }

    getLogger().info("Loading repair manager...");
    repairManager = new RepairManager(this);

    getLogger().info("Registering commands...");
    final CommandHandler commandHandler = BukkitCommandHandler.create(this);
    commandHandler.register(new UltraRepairCmd(this));
    commandHandler.register(new RepairCmd(this));

    getLogger().info("Registering listeners...");

    getLogger().info("Loading metrics...");
    loadMetrics();

    getLogger().info("Checking for updates...");
    checkUpdates();

    if(this.getServer().getPluginManager().isPluginEnabled("NBTAPI")) {
      getLogger().info("NBTAPI detected, enabling support.");
    } else {
      getLogger().warning("NBTAPI not found, some features may not work.");
    }

    Text.console("&aUltraRepair v" + Common.getVersion()
        + " by Demeng has been enabled.");
  }

  @Override
  public void disable() {
    Text.console("&cUltraRepair v" + Common.getVersion() + " by Demeng has been disabled.");
  }

  private boolean loadFiles() {

    String currentlyLoading = "configuration files";

    try {
      currentlyLoading = "settings.yml";
      settingsFile = new YamlConfig(currentlyLoading);

      if (settingsFile.isOutdated(SETTINGS_VERSION)) {
        Common.error(null, "Outdated settings.yml file.", true);
        return false;
      }

      currentlyLoading = "messages.yml";
      messagesFile = new YamlConfig(currentlyLoading);

      if (messagesFile.isOutdated(MESSAGES_VERSION)) {
        Common.error(null, "Outdated messages.yml file.", true);
        return false;
      }

      currentlyLoading = "menus.yml";
      menusFile = new YamlConfig(currentlyLoading);

      if (menusFile.isOutdated(MENUS_VERSION)) {
        Common.error(null, "Outdated menus.yml file.", true);
        return false;
      }

    } catch (IOException | InvalidConfigurationException ex) {
      Common.error(ex, "Failed to load " + currentlyLoading + ".", true);
      return false;
    }

    return true;
  }

  public void updateBaseSettings() {
    setBaseSettings(new BaseSettings() {
      @Override
      public String prefix() {
        return getMessages().getString("prefix");
      }
    });
  }

  private boolean checkEconomy() {

    if (getServer().getPluginManager().getPlugin("Vault") == null) {
      return false;
    }

    final RegisteredServiceProvider<Economy> provider =
        getServer().getServicesManager().getRegistration(Economy.class);

    if (provider == null) {
      return false;
    }

    economyEnabled = true;
    return true;
  }

  private void loadMetrics() {
    try {
      new Metrics(this, 3712);
    } catch (IllegalStateException ex) {
      if (ex.getMessage().equals("bStats Metrics class has not been relocated correctly!")) {
        getLogger().warning("bStats has not been relocated, skipping.");
      }
    }
  }

  private void checkUpdates() {
    Schedulers.async().run(() -> {
      final UpdateChecker checker = new UpdateChecker(63035);

      if (checker.getResult() == Result.OUTDATED) {
        Text.coloredConsole("&2" + Text.CONSOLE_LINE);
        Text.coloredConsole("&aA newer version of UltraRepair is available!");
        Text.coloredConsole("&aCurrent version: &r" + Common.getVersion());
        Text.coloredConsole("&aLatest version: &r" + checker.getLatestVersion());
        Text.coloredConsole("&aGet the update: &rhttps://spigotmc.org/resources/63035");
        Text.coloredConsole("&2" + Text.CONSOLE_LINE);
        return;
      }

      if (checker.getResult() == Result.ERROR) {
        getLogger().warning("Failed to check for updates.");
      }
    });
  }

  public FileConfiguration getSettings() {
    return settingsFile.getConfig();
  }

  public FileConfiguration getMessages() {
    return messagesFile.getConfig();
  }

  public FileConfiguration getMenus() {
    return menusFile.getConfig();
  }
}
