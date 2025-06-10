package dev.demeng.ultrarepair.manager;

import dev.demeng.pluginbase.Common;
import dev.demeng.pluginbase.Schedulers;
import dev.demeng.pluginbase.Services;
import dev.demeng.pluginbase.Sounds;
import dev.demeng.pluginbase.lib.xseries.XSound;
import dev.demeng.pluginbase.serialize.ItemSerializer;
import dev.demeng.ultrarepair.UltraRepair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class RepairManager {

  private static final String COOLDOWN_BYPASS_PERMISSION = "ultrarepair.bypass.cooldown";
  private static final String COST_BYPASS_PERMISSION = "ultrarepair.bypass.cost";

  private final UltraRepair i;
  private final Map<Player, Long> cooldowns = new HashMap<>();
  private final Map<ItemStack, Double> costExceptions = new HashMap<>();

  private long handCooldown;
  private long allCooldown;
  private double defaultCost;
  private double durabilityCostMultiplier;
  private Sound handSound;
  private Sound allSound;

  public RepairManager(UltraRepair i) {
    this.i = i;
    reload();
  }

  public void reload() {

    costExceptions.clear();

    this.handCooldown = i.getSettings().getLong("cooldown.hand") * 1000L;
    this.allCooldown = i.getSettings().getLong("cooldown.all") * 1000L;
    this.defaultCost = i.getSettings().getDouble("default-cost");
    this.durabilityCostMultiplier = i.getSettings().getDouble("durability-multiplier");

    final ConfigurationSection section = Objects.requireNonNull(
        i.getSettings().getConfigurationSection("cost-exceptions"),
        "Cost exceptions section is null");

    for (String key : section.getKeys(false)) {
      costExceptions.put(ItemSerializer.deserialize(
              Objects.requireNonNull(section.getConfigurationSection(key))),
          section.getDouble(key + ".cost"));
    }

    this.handSound = XSound.matchXSound(Objects.requireNonNull(
        i.getSettings().getString("sound.hand"))).orElse(XSound.BLOCK_ANVIL_USE).parseSound();
    this.allSound = XSound.matchXSound(Objects.requireNonNull(
        i.getSettings().getString("sound.all"))).orElse(XSound.BLOCK_ANVIL_USE).parseSound();
  }

  public boolean isRepairable(ItemStack stack) {
    return stack != null
        && stack.getType() != Material.AIR
        && (!Common.isServerVersionAtLeast(13) || !stack.getType().isAir())
        && !stack.getType().isBlock()
        && !stack.getType().isEdible()
        && stack.getType().getMaxDurability() > 0
        && stack.getDurability() != 0;
  }

  public boolean hasAnyRepairable(Player p) {

    for (ItemStack stack : getInventoryContents(p)) {
      if (isRepairable(stack)) {
        return true;
      }
    }

    return false;
  }

  public long getHandCooldown(Player p) {

    if (isBypassingCooldown(p)) {
      return 0;
    }

    return handCooldown;
  }

  public long getAllCooldown(Player p) {

    if (isBypassingCooldown(p)) {
      return 0;
    }

    return allCooldown;
  }

  public long getRemainingCooldownMs(Player p) {

    if (isBypassingCooldown(p)) {
      cooldowns.remove(p);
      return 0L;
    }

    if (!cooldowns.containsKey(p)) {
      return 0L;
    }

    final long remaining = cooldowns.get(p) - System.currentTimeMillis();

    if (remaining <= 0) {
      cooldowns.remove(p);
      return 0L;
    }

    return remaining;
  }

  public double calculateItemCost(Player p, ItemStack stack) {

    if (isBypassingCost(p)) {
      return 0;
    }

    if (!isRepairable(stack)) {
      return 0;
    }

    final ItemStack copy = new ItemStack(stack);
    copy.setDurability((short) 0);

    double cost = defaultCost;

    for (Map.Entry<ItemStack, Double> entry : costExceptions.entrySet()) {
      if (entry.getKey().isSimilar(copy)) {
        cost = entry.getValue();
        break;
      }
    }

    return (cost + (stack.getDurability() * durabilityCostMultiplier)) * stack.getAmount();
  }

  public double calculateInventoryCost(Player p) {

    double cost = 0;

    for (ItemStack stack : getInventoryContents(p)) {
      cost += calculateItemCost(p, stack);
    }

    return cost;
  }

  // Does not check for preconditions.
  public void repairHand(Player p) {

    cooldowns.put(p, System.currentTimeMillis() + handCooldown);
    Schedulers.sync().runLater(() -> cooldowns.remove(p), handCooldown / 50L);

    final ItemStack stack = p.getItemInHand();

    if (i.isEconomyEnabled()) {
      final Economy eco = Services.get(Economy.class).orElseThrow(NullPointerException::new);
      eco.withdrawPlayer(p, calculateItemCost(p, stack));
    }

    stack.setDurability((short) 0);
    Sounds.playVanillaToPlayer(p, handSound, 1F, 1F);
  }

  // Does not check for preconditions.
  public void repairAll(Player p) {

    cooldowns.put(p, System.currentTimeMillis() + allCooldown);
    Schedulers.sync().runLater(() -> cooldowns.remove(p), allCooldown / 50L);

    if (i.isEconomyEnabled()) {
      final Economy eco = Services.get(Economy.class).orElseThrow(NullPointerException::new);
      eco.withdrawPlayer(p, calculateInventoryCost(p));
    }

    for (ItemStack stack : getInventoryContents(p)) {
      if (isRepairable(stack)) {
        stack.setDurability((short) 0);
      }
    }

    Sounds.playVanillaToPlayer(p, allSound, 1F, 1F);
  }

  public static boolean isBypassingCost(Player p) {
    return p.hasPermission(COST_BYPASS_PERMISSION);
  }

  public static boolean isBypassingCooldown(Player p) {
    return p.hasPermission(COOLDOWN_BYPASS_PERMISSION);
  }

  public static List<ItemStack> getInventoryContents(Player p) {
    final List<ItemStack> contents = new ArrayList<>();

    for (ItemStack stack : p.getInventory().getContents()) {
      if (stack != null && stack.getType() != Material.AIR) {
        contents.add(stack);
      }
    }

    // MC 1.8's getContents() does not contain armor slots
    if (Common.getServerMajorVersion() == 8) {
      for (ItemStack stack : p.getInventory().getArmorContents()) {
        if (stack != null && stack.getType() != Material.AIR) {
          contents.add(stack);
        }
      }
    }

    return contents;
  }
}
