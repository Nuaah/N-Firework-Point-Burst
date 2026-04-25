package nuaah.plugin.fireworkpointburst;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class FireworkShow {

    private boolean running = false;
    private final JavaPlugin plugin;
    private final List<Location> targets;
    private final Map<Location, List<ItemStack>> data; //花火
    private final Map<Location, Integer> amountMap = new HashMap<>(); //花火とスタック数
    private final Map<Location, BukkitTask> tasks = new HashMap<>();

    private final Map<Location, Integer> indexMap = new HashMap<>();
    private BukkitTask task;

    public FireworkShow(JavaPlugin plugin,
                        List<Location> targets,
                        Map<Location, List<ItemStack>> data) {
        this.plugin = plugin;
        this.targets = targets;
        this.data = data;
    }

    public void start(int minSeconds, int maxSeconds, boolean loop, boolean random) {

        stop();

        running = true;

        indexMap.clear();
        amountMap.clear();

        for (Location loc : targets) {
            runNextForLocation(loc, minSeconds, maxSeconds, loop, random);
        }
    }

    //発射
    private void runNextForLocation(Location loc, int minSeconds, int maxSeconds, boolean loop, boolean random) {

        if (!running) return;

        long min = minSeconds * 20L;
        long max = maxSeconds * 20L;

        long delay = (min == max)
                ? min
                : ThreadLocalRandom.current().nextLong(min, max + 1);

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {

                if (!running) return;

                List<ItemStack> fireworks = data.get(loc);
                if (fireworks == null || fireworks.isEmpty()) return;

                int index = indexMap.getOrDefault(loc, 0);

                if (index >= fireworks.size()) {
                    if (loop) {
                        index = 0;
                    } else {
                        return;
                    }
                }

                ItemStack item;

                if (random) {
                    item = getRandomFirework(fireworks);
                } else {
                    item = fireworks.get(index);
                }

                //残り発射数
                int remaining = amountMap.getOrDefault(loc, item.getAmount());

                FireworkUtil.burst(plugin, loc, item.clone()); //発射

                remaining--;

                if (remaining <= 0) {
                    index++;

                    if (index >= fireworks.size()) {
                        if (loop) {
                            index = 0;
                        } else {
                            indexMap.remove(loc);
                            amountMap.remove(loc);
                            return;
                        }
                    }

                    //次のアイテムの個数
                    amountMap.put(loc, fireworks.get(index).getAmount());
                    indexMap.put(loc, index);

                } else { //同じスロット
                    amountMap.put(loc, remaining);
                    indexMap.put(loc, index);
                }

                // 次の実行（再帰）
                runNextForLocation(loc, minSeconds, maxSeconds, loop, random);
            }
        }.runTaskLater(plugin, delay);

        tasks.put(loc, task);
    }

    //ランダム選択
    private ItemStack getRandomFirework(List<ItemStack> list) {

        int total = 0;

        for (ItemStack item : list) {
            total += item.getAmount();
        }

        int rand = ThreadLocalRandom.current().nextInt(total);

        int current = 0;

        for (ItemStack item : list) {
            current += item.getAmount();
            if (rand < current) {
                return item;
            }
        }

        return list.get(0); // fallback
    }

    public void stop() {
        running = false;

        for (BukkitTask task : tasks.values()) {
            if (task != null) {
                task.cancel();
            }
        }

        tasks.clear();
        indexMap.clear();
        amountMap.clear();
    }

//    public void stop() {
//        running = false;
//
//        if (task != null) {
//            task.cancel();
//            task = null;
//        }
//
//        indexMap.clear();
//    }
}