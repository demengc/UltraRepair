package com.demeng7215.ultrarepair.commands;

import com.demeng7215.demlib.api.CustomCommand;
import com.demeng7215.ultrarepair.UltraRepair;
import com.demeng7215.ultrarepair.utils.RepairUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class RepairCmd extends CustomCommand {

	private UltraRepair i;

	public RepairCmd(UltraRepair i) {
		super("repair");

		this.i = i;

		setDescription("Repair the item in your hand.");
		setAliases(Arrays.asList("fix", "repair-hand", "fix-hand"));
	}


	@Override
	protected void run(CommandSender sender, String[] args) {

		if (!checkIsPlayer(sender, i.getMessages().getString("console"))) return;

		final Player p = (Player) sender;

		if (args.length == 1 && args[0].equalsIgnoreCase("all")) {

			if (!checkHasPerm("ultrarepair.repair.all", sender,
					i.getMessages().getString("no-perms"))) return;

			RepairUtils.repairAll(p);
			return;
		}

		if (!checkHasPerm("ultrarepair.repair.hand", sender,
				i.getMessages().getString("no-perms"))) return;

		RepairUtils.repairHand(p);
	}
}
