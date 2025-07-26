package dev.demeng.ultrarepair.command;

import dev.demeng.pluginbase.Services;
import dev.demeng.pluginbase.Time;
import dev.demeng.pluginbase.Time.DurationFormatter;
import dev.demeng.pluginbase.text.Text;
import dev.demeng.ultrarepair.UltraRepair;
import dev.demeng.ultrarepair.manager.RepairManager;
import dev.demeng.ultrarepair.menu.ConfirmMenu;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@RequiredArgsConstructor
public class RepairCmd {

  private final UltraRepair i;

  @DefaultFor("repair")
  @Command("repair hand")
  @CommandPermission("ultrarepair.repair.hand")
  public String run(Player p) {
    attemptRepairHand(p, null);
    return null;
  }

  @Command({"repairall", "repair all"})
  @CommandPermission("ultrarepair.repair.all")
  public String runAll(Player p) {
    attemptRepairAll(p, null);
    return null;
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
