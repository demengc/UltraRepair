package dev.demeng.ultrarepair.commands.subcommands;

import dev.demeng.demlib.api.commands.CommandSettings;
import dev.demeng.demlib.api.commands.types.SubCommand;
import dev.demeng.demlib.api.messages.MessageUtils;
import dev.demeng.ultrarepair.UltraRepair;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class ReloadCmd implements SubCommand {

  private final UltraRepair i;

  public ReloadCmd(UltraRepair i) {
    this.i = i;
  }

  @Override
  public String getBaseCommand() {
    return "ultrarepair";
  }

  @Override
  public CommandSettings getSettings() {
    return i.getCommandSettings();
  }

  @Override
  public String getName() {
    return "reload";
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
    return "ultrarepair.reload";
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
    i.getSettingsFile().reloadConfig();
    i.getMessagesFile().reloadConfig();
    MessageUtils.setPrefix(i.getMessages().getString("prefix"));
    MessageUtils.tell(sender, i.getMessages().getString("reloaded"));
  }
}
