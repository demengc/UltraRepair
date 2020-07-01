package dev.demeng.ultrarepair.commands;

import dev.demeng.demlib.api.commands.CommandSettings;
import dev.demeng.demlib.api.commands.types.BaseCommand;
import dev.demeng.ultrarepair.UltraRepair;
import dev.demeng.ultrarepair.utils.RepairUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class RepairAllCmd implements BaseCommand {

  private final UltraRepair i;

  public RepairAllCmd(UltraRepair i) {
    this.i = i;
  }

  @Override
  public CommandSettings getSettings() {
    return i.getCommandSettings();
  }

  @Override
  public String getName() {
    return "repairall";
  }

  @Override
  public List<String> getAliases() {
    return Arrays.asList("fixall", "repair-all", "fix-all");
  }

  @Override
  public boolean isPlayerCommand() {
    return true;
  }

  @Override
  public String getPermission() {
    return "ultrarepair.repair.all";
  }

  @Override
  public String getUsage() {
    return "";
  }

  @Override
  public int getArgs() {
    return 0;
  }

  @Override
  public void execute(CommandSender sender, String[] args) {
    RepairUtils.repairAll((Player) sender);
  }
}
