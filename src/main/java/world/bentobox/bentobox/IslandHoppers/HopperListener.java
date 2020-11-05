package world.bentobox.bentobox.IslandHoppers;

import com.google.common.collect.Lists;
import net.minecraft.server.v1_16_R2.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.Hopper;
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.RanksManager;

import java.util.List;

public class HopperListener implements Listener {

    BentoBox pl;
    private static HopperListener instance;

    public String hopperName = "§a§lIsland §c§lHopper";

    public HopperListener(BentoBox plugin){
        pl = plugin;
        instance = this;
    }

    public static HopperListener getInstance(){
        return instance;
    }

    public ItemStack getIslandHopper(int amount){
        ItemStack hopper = new ItemStack(Material.HOPPER, amount);
        ItemMeta meta = hopper.getItemMeta();
        meta.setDisplayName(hopperName);
        meta.setLore(Lists.newArrayList("§a§l1. §7Place ANYWHERE on your island","§a§l2. §7Right Click to edit filter","§a§l3. §7All filtered drops within island bounds will be picked up"));
        hopper.setItemMeta(meta);
        return hopper;
    }

    public static boolean hasRoomForItem(Inventory inventory, ItemStack item) {
        int space_available = 0;
        for (ItemStack compare : inventory.getContents()) {
            if (compare == null) {
                space_available += item.getMaxStackSize();
                continue;
            }
            if (!item.isSimilar(compare)) continue;
            space_available += compare.getMaxStackSize() - compare.getAmount();
        }
        return space_available >= item.getAmount();
    }

    @EventHandler
    public void breakIslandHopper(BlockBreakEvent e){
        if(e.getBlock().getType().equals(Material.HOPPER)){
            Hopper hopper = (Hopper) e.getBlock().getState();
            if(hopper.getCustomName() == null)return;
            if(hopper.getCustomName().equals(hopperName)){
                if(pl.getIslands().getIslandCache().getIslandAt(e.getBlock().getLocation().clone()) != null) {
                    Island is = pl.getIslands().getIslandCache().getIslandAt(e.getBlock().getLocation().clone());
                    if (is.getRank(e.getPlayer().getUniqueId()) >= RanksManager.MEMBER_RANK) {
                        e.setCancelled(true);
                        e.getBlock().setType(Material.AIR);
                        e.getPlayer().getInventory().addItem(getIslandHopper(1));
                        for (IslandHopper hop : is.getHoppers()) {
                            if (hop.getLocation().distance(e.getBlock().getLocation()) < 1) {
                                is.getHoppers().remove(hop);
                                return;
                            }
                        }
                        e.getPlayer().sendMessage("§cRemoved Island Hopper and filter settings");
                        return;
                    }
                    e.setCancelled(true);
                    e.getPlayer().sendMessage("§cYou can't break that");
                    return;
                }
            }
        }
    }

    @EventHandler
    public void placeIslandHopper(BlockPlaceEvent e){
        if(e.getBlock().getType().equals(Material.HOPPER)){
            if(e.getItemInHand().hasItemMeta()){
                if(e.getItemInHand().getItemMeta().hasDisplayName()){
                    if(e.getItemInHand().getItemMeta().getDisplayName().equals(hopperName)){
                        if(pl.getIslands().getIslandCache().getIslandAt(e.getBlock().getLocation().clone()) != null) {
                            Island is = pl.getIslands().getIslandCache().getIslandAt(e.getBlock().getLocation().clone());
                            if (is.getRank(e.getPlayer().getUniqueId()) >= RanksManager.MEMBER_RANK) {
                                IslandHopper hopper = new IslandHopper(e.getBlock().getLocation());
                                is.addIslandHopper(hopper);
                            }else {
                                e.setCancelled(true);
                                e.getPlayer().sendMessage("§cYou can't place that here - Not your Island");
                            }
                        }else{
                            e.setCancelled(true);
                            e.getPlayer().sendMessage("§cYou can't place that here - Not an island");
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void openHopper(PlayerInteractEvent e){
        if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
            if(e.getClickedBlock().getType().equals(Material.HOPPER)){
                Hopper hopper = (Hopper) e.getClickedBlock().getState();
                if(hopper.getCustomName() == null)return;
                if(hopper.getCustomName().equals(hopperName)){
                    e.setCancelled(true);
                    Island is = pl.getIslands().getIslandCache().getIslandAt(e.getClickedBlock().getLocation().clone());
                    if (is.getRank(e.getPlayer().getUniqueId()) >= RanksManager.MEMBER_RANK) {
                        for(IslandHopper isHopper : is.getHoppers()){
                            if((isHopper.getLocation().getBlockX() == e.getClickedBlock().getX()) &&
                                    (isHopper.getLocation().getBlockY() == e.getClickedBlock().getY()) &&
                                    (isHopper.getLocation().getBlockZ() == e.getClickedBlock().getZ())){
                                openFilterInv(e.getPlayer(), isHopper, is);
                                return;
                            }
                        }

                    }
                }
            }
        }
    }

    public void openFilterInv(Player p, IslandHopper hopper, Island is){
        Inventory inv = Bukkit.createInventory(p, 27, hopperName + " §9Filter");
        for(int i = 0; i < 9; i ++){
            inv.setItem(i, new ItemStack(Material.BLACK_STAINED_GLASS_PANE));
        }
        if(!hopper.getFilter().isEmpty())
            for(Material mat : hopper.getFilter()){
                inv.addItem(new ItemStack(mat));
            }
        for(int i = 18; i < 27; i ++){
            inv.setItem(i, new ItemStack(Material.BLACK_STAINED_GLASS_PANE));
        }
        net.minecraft.server.v1_16_R2.ItemStack stack = CraftItemStack.asNMSCopy(new ItemStack(Material.BLACK_STAINED_GLASS_PANE));
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("UUID", is.getUniqueId());
        tag.setString("HopperID", hopper.getID().toString());
        stack.setTag(tag);
        inv.setItem(22, CraftItemStack.asCraftMirror(stack));
        p.openInventory(inv);
    }

    public void CloseFilterInv(Player p, Inventory inv, IslandHopper hopper){
        for(int i = 0; i < 9; i ++){
            inv.setItem(i, new ItemStack(Material.AIR));
        }
        for(int i = 18; i < 27; i ++){
            inv.setItem(i, new ItemStack(Material.AIR));
        }
        List<Material> mats = Lists.newArrayList();
        if(inv.getContents().length == 0)return;
        for(ItemStack item : inv.getContents()){
            if(item == null)continue;
            mats.add(item.getType());
        }
        hopper.setFilter(mats);
        p.sendMessage("§aSaved Hopper Filter settings");
    }

    @EventHandler
    public void filterClose(InventoryCloseEvent e){
        if(e.getInventory() == null)return;
        if(e.getView().getTitle() == null)return;
        if(e.getView().getTitle().equals(hopperName + " §9Filter")){
            net.minecraft.server.v1_16_R2.ItemStack stack = CraftItemStack.asNMSCopy(e.getInventory().getItem(22));
            NBTTagCompound tag = stack.getTag();
            for(IslandHopper hopper : pl.getIslands().getIslandCache().getIslandById(tag.getString("UUID")).getHoppers()){
                if(hopper.getID().toString().equals(tag.getString("HopperID"))){
                    CloseFilterInv((Player) e.getPlayer(), e.getInventory(), hopper);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void itemAdd(InventoryClickEvent e){
        if(e.getClickedInventory() == null)return;
        if(e.getView().getTitle() == null)return;
        if(e.getView().getTitle().equals(hopperName + " §9Filter")){
            e.setCancelled(true);
            if(e.getRawSlot() < e.getView().getTopInventory().getSize()){
                //top Inv
                String name = "";
                Material type = e.getCurrentItem().getType();
                if(type.equals(Material.BLACK_STAINED_GLASS_PANE))return;
                for(String part : type.toString().split("_"))name += ((part.substring(0, 1).toUpperCase() + part.substring(1).toLowerCase()) + " ");
                e.getClickedInventory().setItem(e.getSlot(), new ItemStack(Material.AIR));
                e.getWhoClicked().sendMessage("§aRemoved §9" + name + "§afrom the filter list");
            }else{
                //bottom Inv
                Island is = pl.getIslands().getIslandCache().getIslandAt(e.getWhoClicked().getLocation().clone());
                for(IslandHopper test : is.getHoppers()){
                    if(test.getFilter().isEmpty())continue;
                    if(test.getFilter().contains(e.getCurrentItem().getType())){
                        e.getWhoClicked().sendMessage("§cThat item is already being filtered and collected.");
                        return;
                    }
                }
                String name = "";
                Material type = e.getCurrentItem().getType();
                for(String part : type.toString().split("_"))name += ((part.substring(0, 1).toUpperCase() + part.substring(1).toLowerCase()) + " ");
                e.getView().getTopInventory().addItem( new ItemStack(type));
                e.getWhoClicked().sendMessage("§aAdded §9" + name + "§ato the filter list");
            }
        }
    }

    @EventHandler
    public void itemDrop(ItemSpawnEvent e){
        Island is = pl.getIslands().getIslandCache().getIslandAt(e.getLocation().clone());
        if(is == null)return;
        if(is.getHoppers().isEmpty())return;
        for(IslandHopper hopper : is.getHoppers()){
            if(hopper.getFilter().contains(e.getEntity().getItemStack().getType())){
                Location check = hopper.getLocation().clone().add(0, -1, 0);
                if(check.getBlock().getState() instanceof Chest){
                    Chest chest = (Chest) check.getBlock().getState();
                    if(hasRoomForItem(chest.getBlockInventory(), e.getEntity().getItemStack())){
                        chest.getBlockInventory().addItem(e.getEntity().getItemStack());
                        e.getEntity().remove();
                    }else{
                        e.getEntity().setVelocity(new Vector(0,0,0));
                        e.getEntity().teleport(hopper.getLocation().clone().add(0.5, -0.5, 0.5));
                    }
                    return;
                }else{
                    e.getEntity().setVelocity(new Vector(0,0,0));
                    e.getEntity().teleport(hopper.getLocation().clone().add(0.5, -0.5, 0.5));
                    return;
                }
            }
        }
    }
}
