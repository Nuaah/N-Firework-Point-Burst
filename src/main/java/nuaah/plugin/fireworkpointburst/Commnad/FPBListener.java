package nuaah.plugin.fireworkpointburst.Commnad;

import nuaah.plugin.fireworkpointburst.FireworkManager;
import nuaah.plugin.fireworkpointburst.FireworkPointBurst;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class FPBListener implements Listener {
    public static Map<UUID,String> selectingPlayers = new HashMap<>();
    public static Set<UUID> deleteSelectingPlayers = new HashSet<>();
    public static Map<UUID,String[]> burstSelectingPlayers = new HashMap<>();
    public static List<Location> savedLocation =  new ArrayList<>();;

    private final FireworkPointBurst plugin;

    public FPBListener(FireworkPointBurst plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        if (e.getAction() != Action.LEFT_CLICK_BLOCK) return;

        Player player = e.getPlayer();

        if (selectingPlayers.containsKey(player.getUniqueId())){ //設定

            Block block = e.getClickedBlock();
            e.setCancelled(true);
            FireworkManager manager = plugin.getManager();

            if (block.getState() instanceof Chest) {
                List<ItemStack> fireworks = new ArrayList<>(); //花火保存
                for (ItemStack item : ((Chest) block.getState()).getBlockInventory()) {
                    if (item != null && item.getType() == Material.FIREWORK_ROCKET) {
                        fireworks.add(item.clone());
                    }
                }
                if (!fireworks.isEmpty()) { //あるよ
                    if(manager.getSavedData().containsKey(block.getLocation())){
                        manager.getSavedData().remove(block.getLocation());
                    }
                    manager.getSavedData().put(block.getLocation(),fireworks); //データ保存
                }

                manager.getSavedData().put(block.getLocation(),fireworks);

                String groupName = selectingPlayers.get(player.getUniqueId());

                player.sendMessage(groupName);

                if (!groupName.isEmpty()){
                    if(!manager.addToGroup(groupName,block.getLocation())){
                        player.sendMessage(plugin.getMsg(player,"noGroup"));
                        return;
                    }
                }

            } else {
                player.sendMessage(plugin.getMsg(player,"clickChest"));
                selectingPlayers.remove(player.getUniqueId());
                return;
            }

//            savedLocation.add(block.getLocation().add(0.5, 0.5, 0.5));
            selectingPlayers.remove(player.getUniqueId());
            block.setType(Material.AIR); //チェスト削除

            player.sendMessage(plugin.getMsg(player,"set"));
        }

        if (burstSelectingPlayers.containsKey(player.getUniqueId())) { //単体爆発
            Block block = e.getClickedBlock();

            if (block.getState() instanceof Chest) {


            } else {
                player.sendMessage(plugin.getMsg(player,"clickChest"));
            }

            e.setCancelled(true);
            block.setType(Material.AIR); //チェスト削除
            burstSelectingPlayers.remove(player.getUniqueId());
        }

        if (deleteSelectingPlayers.contains(player.getUniqueId())) { //削除
            Block block = e.getClickedBlock();
            FireworkManager manager = plugin.getManager();

            if (block.getState() instanceof Chest) {
                if (manager.getSavedData().containsKey(block.getLocation())){
                    manager.getSavedData().remove(block.getLocation());
                    player.sendMessage(plugin.getMsg(player,"removeChest"));
                } else {
                    player.sendMessage(plugin.getMsg(player,"noData"));
                }
            } else {
                player.sendMessage(plugin.getMsg(player,"clickChest"));
            }

            e.setCancelled(true);
            block.setType(Material.AIR); //チェスト削除
            deleteSelectingPlayers.remove(player.getUniqueId());
        }

    }

//    @EventHandler
//    public void onPlace(BlockPlaceEvent e) {
//
//        Location placed = e.getBlock().getLocation();
//        FireworkManager manager = plugin.getManager();
//
//        for (Map.Entry<Location, List<ItemStack>> entry : manager.getSavedData().entrySet()) {
//            if (entry.getKey().equals(placed)) {
//                e.setCancelled(true);
//                e.getPlayer().sendMessage(plugin.getMsg(player,"cannotPlace"));
//                return;
//            }
//        }
//    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Firework)) return;

        Firework fw = (Firework) e.getDamager();

        if (fw.hasMetadata("FPB")) {
            e.setCancelled(true);
        }
    }
}
