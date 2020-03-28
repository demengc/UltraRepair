package com.demeng7215.ultrarepair.commands;

import com.demeng7215.demlib.api.Common;
import com.demeng7215.demlib.api.CustomCommand;
import com.demeng7215.demlib.api.messages.MessageUtils;
import com.demeng7215.ultrarepair.UltraRepair;
import org.bukkit.command.CommandSender;

public class UltraRepairCmd extends CustomCommand {

	private UltraRepair i;

	public UltraRepairCmd(UltraRepair i) {
		super("ultrarepair");

		this.i = i;

		setDescription("Displays plugin information.");
	}

	@Override
	protected void run(CommandSender sender, String[] args) {

		if(args.length == 1 && args[0].equalsIgnoreCase("reload")) {
			i.getSettingsFile().reloadConfig();
			i.getMessagesFile().reloadConfig();
			MessageUtils.setPrefix(i.getMessages().getString("prefix"));
			MessageUtils.tell(sender, i.getMessages().getString("reloaded"));
			return;
		}

		MessageUtils.tell(sender, "&9Running UltraRepair v" + Common.getVersion() + " by Demeng7215.",
				"&9Link: &7https://www.spigotmc.org/resources/63035/",
				"&9Type &7/repair &9to repair.");
	}
}
