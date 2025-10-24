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

import dev.demeng.pluginbase.Services;
import dev.demeng.pluginbase.Time;
import dev.demeng.pluginbase.Time.DurationFormatter;
import dev.demeng.pluginbase.lib.lamp.annotation.Command;
import dev.demeng.pluginbase.lib.lamp.bukkit.annotation.CommandPermission;
import dev.demeng.pluginbase.text.Text;
import dev.demeng.ultrarepair.UltraRepair;
import dev.demeng.ultrarepair.manager.RepairManager;
import dev.demeng.ultrarepair.menu.ConfirmMenu;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public class RepairCmd {

  private final UltraRepair i;

  @Command({"repair", "repair hand"})
  @CommandPermission("ultrarepair.repair.hand")
  public void run(Player p) {
    attemptRepairHand(p, null);
  }

  @Command({"repairall", "repair all"})
  @CommandPermission("ultrarepair.repair.all")
  public void runAll(Player p) {
    attemptRepairAll(p, null);
  }

  // If confirmedCost is null, the repair has not been confirmed.
  private void attemptRepairHand(Player p, Double confirmedCost) {

    p.closeInventory();

    final ItemStack stack = p.getInventory().getItemInHand();

    if (!i.getRepairManager().isRepairable(stack)) {
      Text.tell(p, i.getMessages().getString("invalid-item"));
      return;
    }

    final long cooldown = i.getRepairManager().getRemainingCooldownMs(p);
    if (cooldown > 0) {
      Text.tell(p, Objects.requireNonNull(i.getMessages().getString("on-cooldown"))
          .replace("%time%", Time.formatDuration(DurationFormatter.LONG, cooldown)));
      return;
    }

    double cost = 0;

    if (i.isEconomyEnabled()) {
      final Economy eco = Services.get(Economy.class).orElse(null);
      cost = i.getRepairManager().calculateItemCost(p, stack);
      if (!eco.has(p, cost)) {
        Text.tell(p, Objects.requireNonNull(i.getMessages().getString("insufficient-funds"))
            .replace("%cost%", String.format("%.2f", cost)));
        return;
      }
    }

    final double finalCost = cost;

    if (confirmedCost == null) {
      new ConfirmMenu(Objects.requireNonNull(
          i.getMenus().getConfigurationSection("confirm-hand")),
          i.getRepairManager().getHandCooldown(p),
          finalCost,
          RepairManager.isBypassingCooldown(p),
          RepairManager.isBypassingCost(p),
          () -> attemptRepairHand(p, finalCost)).open(p);
      return;
    }

    if (confirmedCost != cost) {
      Text.tell(p, i.getMessages().getString("confirm-failed"));
      return;
    }

    i.getRepairManager().repairHand(p);
    Text.tell(p, i.getMessages().getString("repaired-hand"));
  }

  // If confirmedCost is null, the repair has not been confirmed.
  private void attemptRepairAll(Player p, Double confirmedCost) {

    p.closeInventory();

    if (!i.getRepairManager().hasAnyRepairable(p)) {
      Text.tell(p, i.getMessages().getString("invalid-items"));
      return;
    }

    final long cooldown = i.getRepairManager().getRemainingCooldownMs(p);
    if (cooldown > 0) {
      Text.tell(p, Objects.requireNonNull(i.getMessages().getString("on-cooldown"))
          .replace("%time%", Time.formatDuration(DurationFormatter.LONG, cooldown)));
      return;
    }

    double cost = 0;

    if (i.isEconomyEnabled()) {
      final Economy eco = Services.get(Economy.class).orElse(null);
      cost = i.getRepairManager().calculateInventoryCost(p);
      if (!eco.has(p, cost)) {
        Text.tell(p, Objects.requireNonNull(i.getMessages().getString("insufficient-funds"))
            .replace("%cost%", String.format("%.2f", cost)));
        return;
      }
    }

    final double finalCost = cost;

    if (confirmedCost == null) {
      new ConfirmMenu(Objects.requireNonNull(
          i.getMenus().getConfigurationSection("confirm-all")),
          i.getRepairManager().getAllCooldown(p),
          finalCost,
          RepairManager.isBypassingCooldown(p),
          RepairManager.isBypassingCost(p),
          () -> attemptRepairAll(p, finalCost)).open(p);
      return;
    }

    if (confirmedCost != cost) {
      Text.tell(p, i.getMessages().getString("confirm-failed"));
      return;
    }

    i.getRepairManager().repairAll(p);
    Text.tell(p, i.getMessages().getString("repaired-all"));
  }
}
