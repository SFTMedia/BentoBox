package world.bentobox.bentobox.IslandHoppers;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HopperCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender theSender, Command cmd, String commandLabel, String[] args){
            if(theSender.hasPermission("islandhopper.give")){
                if(args.length == 0){
                    theSender.sendMessage("§a/Islandhopper §c(Player Name) §7<Amount>");
                    return true;
                }else if(args.length == 1){
                    if(Bukkit.getPlayer(args[0]) == null){
                        theSender.sendMessage("§c" + args[0] + " §bis not a player name");
                        theSender.sendMessage("§a/Islandhopper §c" + args[0] + " §7<Amount>");
                        return true;
                    }else{
                        Player rec = Bukkit.getPlayer(args[0]);
                        rec.getInventory().addItem(HopperListener.getInstance().getIslandHopper(1));
                    }
                }else if(args.length == 2){
                    if(Bukkit.getPlayer(args[0]) == null){
                        theSender.sendMessage("§c" + args[0] + " §bis not a player name");
                        theSender.sendMessage("§a/Islandhopper §c" + args[0] + " §7" + args[1]);
                        return true;
                    }
                    Player rec = Bukkit.getPlayer(args[0]);
                    try{
                        Integer.parseInt(args[1]);
                    } catch(Exception e){
                        theSender.sendMessage("§c" + args[1] + " §bis not a Number");
                        theSender.sendMessage("§a/Islandhopper " + args[0] + " §c" + args[1]);
                        return true;
                    }
                    rec.getInventory().addItem(HopperListener.getInstance().getIslandHopper(Integer.parseInt(args[1])));
                    return true;
                }else{
                    theSender.sendMessage("§cToo Many Arguments");
                    theSender.sendMessage("§a/Islandhopper §c(Player Name) §7<Amount>");
                    return true;
                }
            }
        return true;
    }
}
