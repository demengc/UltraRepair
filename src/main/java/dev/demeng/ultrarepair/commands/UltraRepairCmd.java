package dev.demeng.ultrarepair.commands;

import dev.demeng.demlib.api.Common;
import dev.demeng.demlib.api.commands.CommandSettings;
import dev.demeng.demlib.api.commands.types.BaseCommand;
import dev.demeng.demlib.api.messages.MessageUtils;
import dev.demeng.ultrarepair.UltraRepair;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class UltraRepairCmd implements BaseCommand {

  private final UltraRepair i;

  public UltraRepairCmd(UltraRepair i) {
    this.i = i;
  }

  @Override
  public CommandSettings getSettings() {
    return i.getCommandSettings();
  }

  @Override
  public String getName() {
    return "ultrarepair";
  }

  @Override
  public List<String> getAliases() {
    return Collections.emptyList();
  }

  @Override
  public boolean isPlayerCommand() {
    return false;
  }

  @Override
  public String getPermission() {
    return null;
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
    MessageUtils.tell(
        sender,
        "&9Running UltraRepair v" + Common.getVersion() + " by Demeng.",
        "&9Link: &7https://www.spigotmc.org/resources/63035/",
        "&9Type &7/repair &9to repair.");
  }
}
