package com.demeng7215.ultrarepair.commands;

import com.demeng7215.demlib.api.CustomCommand;
import com.demeng7215.ultrarepair.UltraRepair;
import com.demeng7215.ultrarepair.utils.RepairUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class RepairAllCmd extends CustomCommand {

	private UltraRepair i;

	public RepairAllCmd(UltraRepair i) {
		super("repairall");

		this.i = i;

		setDescription("Repair all items in your inventory.");
		setAliases(Arrays.asList("fixall", "repair-all", "fix-all"));
	}

	@Override
	protected void run(CommandSender sender, String[] args) {

		if (!checkIsPlayer(sender, i.getMessages().getString("console"))) return;

		if (!checkHasPerm("ultrarepair.repair.all", sender,
				i.getMessages().getString("no-perms"))) return;

		RepairUtils.repairAll((Player) sender);
	}
}
