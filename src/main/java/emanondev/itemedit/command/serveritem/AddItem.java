package emanondev.itemedit.command.serveritem;

import emanondev.itemedit.ItemEdit;
import emanondev.itemedit.Util;
import emanondev.itemedit.aliases.Aliases;
import emanondev.itemedit.command.ServerItemCommand;
import emanondev.itemedit.command.SubCmd;
import me.gavvydizzle.rewardsinventory.api.RewardsInventoryAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AddItem extends SubCmd {

    private final RewardsInventoryAPI rewardsInventoryAPI;

    public AddItem(ServerItemCommand cmd) {
        super("additem", cmd, false, false);
        rewardsInventoryAPI = RewardsInventoryAPI.getInstance();
    }

    @Override
    public void onCommand(CommandSender sender, String alias, String[] args) {
        try {
            // <id> <amount> <player> <menuID> [silent]
            if (args.length < 5 || args.length > 6) {
                throw new IllegalArgumentException("Wrong param number");
            }

            Boolean silent = args.length == 6 ? (Aliases.BOOLEAN.convertAlias(args[5])) : ((Boolean) false);
            if (silent == null) {
                silent = Boolean.valueOf(args[5]);
            }

            int amount = Integer.parseInt(args[2]);
            if (amount < 1) {
                throw new IllegalArgumentException("Wrong amount number");
            }

            ItemStack item = ItemEdit.get().getServerStorage().getItem(args[1]);
            if (item == null) {
                sender.sendMessage(ChatColor.RED + "No serverItem exists with the id: " + args[1]);
                return;
            }

            OfflinePlayer target = Bukkit.getPlayer(args[3]);
            if (target == null) {
                target = Bukkit.getOfflinePlayer(args[3]);
                if (!target.hasPlayedBefore() && !target.isOnline()) {
                    sender.sendMessage(ChatColor.RED + args[3] + " is not a valid player.");
                    return;
                }
            }

            int pageMenuID = rewardsInventoryAPI.getMenuID(args[4]);
            if (pageMenuID == -1) {
                sender.sendMessage(ChatColor.RED + "No menu exists with the id: " + args[3]);
                return;
            }

            // Does not support item placeholders because of OfflinePlayer support

            ItemStack itemStack = item.clone();

            if (!rewardsInventoryAPI.addItem(target, pageMenuID, itemStack)) {
                sender.sendMessage(ChatColor.RED + "Failed to add the item");
                return;
            }

            if (!silent) {
                sender.sendMessage(ChatColor.GREEN + "Successfully put " + amount + " " + args[1] + " into " + target.getName() + "'s /rew " + args[4] + " menu");
            }
        } catch (Exception e) {
            onFail(sender, alias);
        }
    }

    @Override
    public List<String> onComplete(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return Collections.emptyList();
        switch (args.length) {
            case 2:
                return Util.complete(args[1], ItemEdit.get().getServerStorage().getIds());
            case 3:
                return Util.complete(args[2], Arrays.asList("1", "10", "64", "576", "2304"));
            case 4:
                return Util.completePlayers(args[3]);
            case 5:
                ArrayList<String> list = new ArrayList<>();
                StringUtil.copyPartialMatches(args[4], rewardsInventoryAPI.getPageMenuNames(), list);
                return list;
            case 6:
                return Util.complete(args[5], Aliases.BOOLEAN);
        }
        return Collections.emptyList();
    }

}
