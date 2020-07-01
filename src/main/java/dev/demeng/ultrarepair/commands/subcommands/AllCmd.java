package dev.demeng.ultrarepair.commands.subcommands;

import dev.demeng.demlib.api.commands.CommandSettings;
import dev.demeng.demlib.api.commands.types.SubCommand;
import dev.demeng.ultrarepair.UltraRepair;
import dev.demeng.ultrarepair.utils.RepairUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class AllCmd implements SubCommand {

  private final UltraRepair i;

  public AllCmd(UltraRepair i) {
    this.i = i;
  }

  @Override
  public CommandSettings getSettings() {
    return i.getCommandSettings();
  }

  @Override
  public String getBaseCommand() {
    return "repair";
  }

  @Override
  public String getName() {
    return "all";
  }

  @Override
  public List<String> getAliases() {
    return Collections.emptyList();
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
