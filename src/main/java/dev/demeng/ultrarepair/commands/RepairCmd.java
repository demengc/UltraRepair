package dev.demeng.ultrarepair.commands;

import dev.demeng.demlib.api.commands.CommandSettings;
import dev.demeng.demlib.api.commands.types.BaseCommand;
import dev.demeng.ultrarepair.UltraRepair;
import dev.demeng.ultrarepair.utils.RepairUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class RepairCmd implements BaseCommand {

  private final UltraRepair i;

  public RepairCmd(UltraRepair i) {
    this.i = i;
  }

  @Override
  public CommandSettings getSettings() {
    return i.getCommandSettings();
  }

  @Override
  public String getName() {
    return "repair";
  }

  @Override
  public List<String> getAliases() {
    return Arrays.asList("fix", "repair-hand", "fix-hand");
  }

  @Override
  public boolean isPlayerCommand() {
    return true;
  }

  @Override
  public String getPermission() {
    return "ultrarepair.repair.hand";
  }

  @Override
  public String getUsage() {
    return "[all]";
  }

  @Override
  public int getArgs() {
    return 0;
  }

  @Override
  public void execute(CommandSender sender, String[] args) {
    RepairUtils.repairHand((Player) sender);
  }
}
