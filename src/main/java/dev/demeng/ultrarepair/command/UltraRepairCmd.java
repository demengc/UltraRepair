package dev.demeng.ultrarepair.command;

import dev.demeng.pluginbase.Common;
import dev.demeng.pluginbase.text.Text;
import dev.demeng.ultrarepair.UltraRepair;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
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

  @Subcommand("exclude")
  @CommandPermission("ultrarepair.exclude")
  public String runExclude(Player player) {
    
    final ItemStack hand = player.getItemInHand();
    
    if (!i.getRepairManager().isPotentiallyRepairable(hand)) {
      return i.getMessages().getString("exclude-unexclude-invalid");
    }
    
    i.getRepairManager().addExclusionTag(hand);
    return i.getMessages().getString("exclude-success");
  }

  @Subcommand("unexclude")
  @CommandPermission("ultrarepair.exclude")
  public String runUnexclude(Player player) {
    
    final ItemStack hand = player.getItemInHand();
    
    if (!i.getRepairManager().isPotentiallyRepairable(hand)) {
      return i.getMessages().getString("exclude-unexclude-invalid");
    }
    
    i.getRepairManager().removeExclusionTag(hand);
    return i.getMessages().getString("unexclude-success");
  }
}
