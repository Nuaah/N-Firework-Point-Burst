package nuaah.plugin.fireworkpointburst;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class FireworkManager {

    private final JavaPlugin plugin;

    private Map<Location, List<ItemStack>> savedData = new HashMap<>(); //花火保存
    private Map<String, FireworkShow> shows = new HashMap<>(); //花火発射リスト
    Map<String, List<Location>> groups = new HashMap<>(); //グループ

    public FireworkManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    //花火スタート
    public boolean startShow(String name, int min, int max, boolean loop, boolean random) {

        List<Location> targets = getGroup(name);
        if (targets == null || targets.isEmpty()) {
            return false;
        }

        //古いやつ止める
        FireworkShow old = shows.get(name);
        if (old != null) {
            old.stop();
        }

        FireworkShow show = new FireworkShow(plugin, targets, savedData);
        shows.put(name, show);

        show.start(min, max, loop, random);

        return true;
    }

    //花火ストップ
    public void stopShow(String name) {

        if (name.equalsIgnoreCase("all")) {

            for (FireworkShow show : shows.values()) {
                if (show != null) {
                    show.stop();
                }
            }
            shows.clear();
            return;
        }

        FireworkShow show = shows.get(name);

        if (show != null) {
            show.stop();
            shows.remove(name);
        }
    }

    public Map<Location, List<ItemStack>> getSavedData() {
        return savedData;
    }

    public List<Location> getGroup(String name) {
        return groups.get(name);
    }

    public Map<String, List<Location>> getGroupList() {
        return groups;
    }

    //グループ作成
    public boolean createGroup(String name) {
        if (groups.containsKey(name)) {
            return false; // 既に存在
        }

        groups.put(name, new ArrayList<>());

        return true;
    }

    //グループ削除
    public boolean deleteGroup(String name) {
        if (groups.containsKey(name)) {
            groups.remove(name);
            return true;
        }
        return false;
    }

    //グループに追加
    public boolean addToGroup(String name, Location loc) {
        List<Location> list = groups.get(name);
        if (list == null) return false;

        list.add(loc);
        return true;
    }

    //データを保存
    public void saveData() {

        FileConfiguration config = plugin.getConfig();

        //消してから保存
        config.set("data", null);
        config.set("groups", null);

        for (Map.Entry<Location, List<ItemStack>> entry : savedData.entrySet()) {
            String path = "data." + serializeLocation(entry.getKey());
            config.set(path + ".items", entry.getValue());
        }


        for (Map.Entry<String, List<Location>> entry : groups.entrySet()) {

            String name = entry.getKey();
            List<String> locList = new ArrayList<>();

            for (Location loc : entry.getValue()) {
                locList.add(serializeLocation(loc));
            }

            config.set("groups." + name, locList);
        }


        plugin.saveConfig();
    }

    //読み込み
    public void loadData() {

        FileConfiguration config = plugin.getConfig();

        if (!config.contains("data")) return;

        for (String key : config.getConfigurationSection("data").getKeys(false)) {

            Location loc = deserializeLocation(key);

            List<ItemStack> items =
                    (List<ItemStack>) config.getList("data." + key + ".items");

            savedData.put(loc, items);
        }

        if (!config.contains("groups")) return;

        for (String name : config.getConfigurationSection("groups").getKeys(false)) {

            List<String> locStrings = config.getStringList("groups." + name);
            List<Location> locList = new ArrayList<>();

            for (String str : locStrings) {
                locList.add(deserializeLocation(str));
            }

            groups.put(name, locList);
        }
    }

    //保存
    private String serializeLocation(Location loc) {
        return loc.getWorld().getName() + ","
                + loc.getBlockX() + ","
                + loc.getBlockY() + ","
                + loc.getBlockZ();
    }

    //復元
    private Location deserializeLocation(String str) {

        String[] parts = str.split(",");

        return new Location(
                Bukkit.getWorld(parts[0]),
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2]),
                Integer.parseInt(parts[3])
        );
    }
}
