/*
 * MIT License
 *
 * Copyright (c) 2025 Demeng Chen
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.demeng.ultrarepair.command;

import dev.demeng.pluginbase.Common;
import dev.demeng.pluginbase.lib.lamp.annotation.Command;
import dev.demeng.pluginbase.lib.lamp.annotation.Subcommand;
import dev.demeng.pluginbase.lib.lamp.bukkit.annotation.CommandPermission;
import dev.demeng.pluginbase.text.Text;
import dev.demeng.ultrarepair.UltraRepair;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
@Command({"ultrarepair", "ur"})
public class UltraRepairCmd {

  private final UltraRepair i;

  @Subcommand("info")
  public void runBase(CommandSender sender) {
    Text.coloredTell(sender, "&r");
    Text.coloredTell(sender, "&9&lRunning UltraRepair v" + Common.getVersion() + " by Demeng.");
    Text.coloredTell(sender, "&7Link: &bhttps://spigotmc.org/resources/63035/");
    Text.coloredTell(sender, "&r");
  }

  @Subcommand("help")
  public void runHelp(CommandSender sender) {
    for (String line : i.getMessages().getStringList("help")) {
      Text.coloredTell(sender, line);
    }
  }

  @Subcommand("reload")
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
      return i.getMessages().getString("exclude-invalid");
    }

    if (i.getRepairManager().hasExclusionTag(hand)) {
      return i.getMessages().getString("exclude-already-excluded");
    }

    player.setItemInHand(i.getRepairManager().addExclusionTag(hand));
    return i.getMessages().getString("exclude-success");
  }

  @Subcommand("unexclude")
  @CommandPermission("ultrarepair.exclude")
  public String runUnexclude(Player player) {

    final ItemStack hand = player.getItemInHand();

    if (!i.getRepairManager().isPotentiallyRepairable(hand)) {
      return i.getMessages().getString("unexclude-invalid");
    }

    if (!i.getRepairManager().hasExclusionTag(hand)) {
      return i.getMessages().getString("unexclude-not-excluded");
    }

    player.setItemInHand(i.getRepairManager().removeExclusionTag(hand));
    return i.getMessages().getString("unexclude-success");
  }
}
