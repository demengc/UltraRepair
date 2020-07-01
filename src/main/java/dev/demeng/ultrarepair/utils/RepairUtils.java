package dev.demeng.ultrarepair.utils;

import dev.demeng.demlib.api.messages.MessageUtils;
import dev.demeng.ultrarepair.UltraRepair;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class RepairUtils {

  private static final Map<UUID, Long> timeouts = new HashMap<>();

  public static void repairHand(Player p) {

    final UltraRepair i = UltraRepair.getInstance();

    long timeout = 0;
    if (timeouts.containsKey(p.getUniqueId())) timeout = timeouts.get(p.getUniqueId());

    if (p.getItemInHand().getType() == Material.AIR
        || p.getItemInHand().getType().isBlock()
        || p.getItemInHand().getType().isEdible()
        || p.getItemInHand().getType().getMaxDurability() <= 0
        || p.getItemInHand().getDurability() == 0) {
      MessageUtils.tell(p, i.getMessages().getString("invalid-item"));
      return;
    }

    if (!p.hasPermission("ultrarepair.bypass.cooldown") && System.currentTimeMillis() < timeout) {
      MessageUtils.tell(
          p,
          i.getMessages()
              .getString("cooldown-active")
              .replace("%seconds%", (timeout - System.currentTimeMillis()) / 1000 + ""));
      return;
    }

    if (!p.hasPermission("ultrarepair.bypass.cost")
        && !i.getEconomy().has(p, i.getSettings().getDouble("repair-hand.cost"))) {
      MessageUtils.tell(p, i.getMessages().getString("cannot-afford"));
      return;
    }

    if (!p.hasPermission("ultrarepair.bypass.cooldown"))
      timeouts.put(
          p.getUniqueId(),
          System.currentTimeMillis() + (i.getSettings().getLong("repair-hand.cooldown") * 1000));

    if (!p.hasPermission("ultrarepair.bypass.cost"))
      i.getEconomy().withdrawPlayer(p, i.getSettings().getDouble("repair-hand.cost"));

    p.getItemInHand().setDurability((short) 0);
    p.updateInventory();

    p.playSound(
        p.getLocation(),
        dev.demeng.xseries.XSound.valueOf(i.getSettings().getString("repair-hand.sound"))
            .parseSound(),
        100F,
        100F);

    MessageUtils.tell(p, i.getMessages().getString("repaired-hand"));
  }

  public static void repairAll(Player p) {

    final UltraRepair i = UltraRepair.getInstance();

    final boolean chargePerItem = i.getSettings().getBoolean("repair-all.charge-per-item");
    double cost = 0;

    final List<ItemStack> queue = new ArrayList<>();

    for (ItemStack stack : p.getInventory().getContents()) {

      if (stack != null
          && stack.getType() != Material.AIR
          && !stack.getType().isBlock()
          && !stack.getType().isEdible()
          && stack.getType().getMaxDurability() > 0
          && stack.getDurability() != 0) {

        queue.add(stack);
        if (chargePerItem) cost += i.getSettings().getDouble("repair-all.cost");
      }
    }

    if (queue.isEmpty()) {
      MessageUtils.tell(p, i.getMessages().getString("invalid-items"));
      return;
    }

    long timeout = 0;
    if (timeouts.containsKey(p.getUniqueId())) timeout = timeouts.get(p.getUniqueId());

    if (!p.hasPermission("ultrarepair.bypass.cooldown") && System.currentTimeMillis() < timeout) {
      MessageUtils.tell(
          p,
          i.getMessages()
              .getString("cooldown-active")
              .replace("%seconds%", (timeout - System.currentTimeMillis()) / 1000 + ""));
      return;
    }

    if (!chargePerItem) cost = i.getSettings().getDouble("repair-all.cost");

    if (!p.hasPermission("ultrarepair.bypass.cost") && !i.getEconomy().has(p, cost)) {
      MessageUtils.tell(p, i.getMessages().getString("cannot-afford"));
      return;
    }

    if (!p.hasPermission("ultrarepair.bypass.cooldown"))
      timeouts.put(
          p.getUniqueId(),
          System.currentTimeMillis() + (i.getSettings().getLong("repair-all.cooldown") * 1000));

    if (!p.hasPermission("ultrarepair.bypass.cost")) i.getEconomy().withdrawPlayer(p, cost);

    for (ItemStack stack : queue) stack.setDurability((short) 0);
    p.updateInventory();

    p.playSound(
        p.getLocation(),
        dev.demeng.xseries.XSound.valueOf(i.getSettings().getString("repair-all.sound"))
            .parseSound(),
        100F,
        100F);

    MessageUtils.tell(p, i.getMessages().getString("repaired-all"));
  }
}
