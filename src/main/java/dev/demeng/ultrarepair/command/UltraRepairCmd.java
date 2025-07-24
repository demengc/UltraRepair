package dev.demeng.ultrarepair.command;

import de.tr7zw.changeme.nbtapi.NBT;
import dev.demeng.pluginbase.Common;
import dev.demeng.pluginbase.text.Text;
import dev.demeng.ultrarepair.UltraRepair;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@RequiredArgsConstructor
@Command({"ultrarepair", "ur"})
public class UltraRepairCmd {

  private final UltraRepair i;

  @DefaultFor("ultrarepair")
  public void runBase(CommandSender sender) {
    Text.coloredTell(sender, "&9&lRunning UltraRepair v" + Common.getVersion() + " by Demeng.");
    Text.coloredTell(sender, "&7Link: &bhttps://spigotmc.org/resources/63035/");
  }

  @Subcommand({"reload", "rl"})
  @CommandPermission("ultrarepair.reload")
  public String runReload(CommandSender sender) {

    try {
      i.getSettingsFile().reload();
      i.getMessagesFile().reload();
      i.getMenusFile().reload();
    } catch (IOException | InvalidConfigurationException ex) {
      Common.error(ex, "Failed to reload files.", false, sender);
      return null;
    }

    i.updateBaseSettings();
    i.getRepairManager().reload();

    return i.getMessages().getString("reloaded");
  }

  @Subcommand("excludeitem")
  @CommandPermission("ultrarepair.excludeitem")
  public void runExcludeItem(CommandSender sender) {

    if (!(sender instanceof Player)) {
      Text.coloredTell(sender, "&cThis command can only be used by players.");
      return;
    }

    Player player = (Player) sender;
    ItemStack item = player.getInventory().getItemInMainHand();

    if (item == null || item.getType() == Material.AIR) {
      Text.coloredTell(player, "&cYou must be holding an item to exclude it from repair.");
      return;
    }

    try {
      // Check if NBT-API is available
      if (!i.getServer().getPluginManager().isPluginEnabled("NBTAPI")) {
        Text.coloredTell(player, "&cNBT-API is not available. This feature requires NBT-API to be installed.");
        return;
      }

      // Check if item already has the exclude tag
      boolean alreadyExcluded = NBT.get(item, (nbt) -> {
        return nbt.hasTag("ultrarepair:exclude");
      });

      if (alreadyExcluded) {
        Text.coloredTell(player, "&eThis item is already excluded from repair.");
        return;
      }

      // Add the exclude tag
      NBT.modify(item, nbt -> {
        nbt.setBoolean("ultrarepair:exclude", true);
      });

      Text.coloredTell(player, "&aSuccessfully added exclusion tag to the item!");
      Text.coloredTell(player, "&7This item will now be ignored by repair commands.");

    } catch (Exception e) {
      Text.coloredTell(player, "&cFailed to add exclusion tag to the item. Please check console for errors.");
      i.getLogger().warning("Failed to add NBT exclusion tag: " + e.getMessage());
    }
  }
}
