package nuaah.plugin.fireworkpointburst.Commnad;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.papermc.paper.command.brigadier.Commands;
import nuaah.plugin.fireworkpointburst.FireworkManager;
import nuaah.plugin.fireworkpointburst.FireworkPointBurst;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class FPBCommand implements CommandExecutor {

    private final FireworkPointBurst plugin;

    public FPBCommand(FireworkPointBurst plugin) {
        this.plugin = plugin;
    }

    //コマンド補完
    public void registerCommands(Commands commands) {
        FireworkManager manager = plugin.getManager();

        commands.register(
            Commands.literal("fpb")
                .then(Commands.literal("set")//チェスト選択モード
                    .then(Commands.argument("name", StringArgumentType.word())
                        .executes(ctx -> {

                            CommandSender sender = ctx.getSource().getSender();

                            if (!(sender instanceof Player player)) {
                                sender.sendMessage(plugin.getMsg(sender,"playerOnly"));
                                return 1;
                            }

                            String name = StringArgumentType.getString(ctx, "name");
                            player.sendMessage(name);

                            List<Location> targets = manager.getGroup(name);

                            if (targets == null) {
                                player.sendMessage(plugin.getMsg(sender,"noGroup"));
                                return 1;
                            }

                            // 👇 ここ変えるポイント
                            FPBListener.selectingPlayers.put(player.getUniqueId(), name);

                            player.sendMessage(plugin.getMsg(sender,"clickChest"));

                            return 1;
                        })
                    )
                )
                    .then(Commands.literal("play")//花火再生
                        .then(Commands.argument("min", IntegerArgumentType.integer(0))
                            .then(Commands.argument("max", IntegerArgumentType.integer(0))
                                .then(Commands.argument("loop", BoolArgumentType.bool())
                                    .then(Commands.argument("name", StringArgumentType.word())
                                        .executes(ctx -> {

                                            int min = IntegerArgumentType.getInteger(ctx, "min");
                                            int max = IntegerArgumentType.getInteger(ctx, "max");
                                            boolean loop = BoolArgumentType.getBool(ctx, "loop");
                                            String name = StringArgumentType.getString(ctx, "name");

                                            CommandSender sender = ctx.getSource().getSender();

                                            if (min > max) {
                                                int tmp = min;
                                                min = max;
                                                max = tmp;
                                            }

                                            List<Location> targets = manager.getGroup(name);

                                            if (targets == null) {
                                                if (sender instanceof Player player) {
                                                    player.sendMessage(plugin.getMsg(sender,"noGroup"));
                                                }
                                                return 1;
                                            }

                                            if (manager.startShow(name, min, max, loop,false)){
                                                if (sender instanceof Player player){
                                                    player.sendMessage(plugin.getMsg(sender,"play"));
                                                } else {
                                                    sender.sendMessage(plugin.getMsg(sender,"play"));
                                                }
                                            } else {
                                                if (sender instanceof Player player){
                                                    player.sendMessage(plugin.getMsg(sender,"noData"));
                                                } else {
                                                    sender.sendMessage(plugin.getMsg(sender,"noData"));
                                                }
                                            }

                                            return 1;
                                        })
                                    )
                                )
                            )
                        ).then(Commands.argument("min", IntegerArgumentType.integer(0))
                            .then(Commands.argument("max", IntegerArgumentType.integer(0))
                                .then(Commands.argument("loop", BoolArgumentType.bool())
                                    .then(Commands.argument("name", StringArgumentType.word())
                                        .then(Commands.argument("random", BoolArgumentType.bool())
                                            .executes(ctx -> {

                                                int min = IntegerArgumentType.getInteger(ctx, "min");
                                                int max = IntegerArgumentType.getInteger(ctx, "max");
                                                boolean loop = BoolArgumentType.getBool(ctx, "loop");
                                                String name = StringArgumentType.getString(ctx, "name");
                                                boolean random = BoolArgumentType.getBool(ctx, "random");

                                                CommandSender sender = ctx.getSource().getSender();

                                                if (min > max) {
                                                    int tmp = min;
                                                    min = max;
                                                    max = tmp;
                                                }

                                                List<Location> targets = manager.getGroup(name);

                                                if (targets == null) {
                                                    if (sender instanceof Player player) {
                                                        player.sendMessage(plugin.getMsg(sender,"noGroup"));
                                                    } else {
                                                        sender.sendMessage(plugin.getMsg(sender,"noGroup"));
                                                    }
                                                    return 1;
                                                }

                                                if (manager.startShow(name, min, max, loop,random)){
                                                    if (sender instanceof Player player){
                                                        player.sendMessage(plugin.getMsg(sender,"play"));
                                                    } else {
                                                        sender.sendMessage(plugin.getMsg(sender,"play"));
                                                    }
                                                } else {
                                                    if (sender instanceof Player player){
                                                        player.sendMessage(plugin.getMsg(sender,"noData"));
                                                    } else {
                                                        sender.sendMessage(plugin.getMsg(sender,"noData"));
                                                    }
                                                }

                                                return 1;
                                            })
                                        )
                                    )
                                )
                            )
                        )
                    )
                .then(Commands.literal("group")
                    .then(Commands.literal("create") //グループ作成
                        .then(Commands.argument("name", StringArgumentType.word())
                            .executes(ctx -> {

                                CommandSender sender = ctx.getSource().getSender();

                                String name = StringArgumentType.getString(ctx, "name");

                                if (manager.createGroup(name)){
                                    if (sender instanceof Player player) {
                                        player.sendMessage(plugin.getMsg(sender,"createGroup"));
                                    } else {
                                        sender.sendMessage(plugin.getMsg(sender,"createGroup"));
                                    }
                                } else {
                                    if (sender instanceof Player player) {
                                        player.sendMessage(plugin.getMsg(sender,"existGroup"));
                                    } else {
                                        sender.sendMessage(plugin.getMsg(sender,"existGroup"));
                                    }
                                }
                                return 1;
                            })
                        )
                    )
                    .then(Commands.literal("delete") //グループ削除
                        .then(Commands.argument("name", StringArgumentType.word())
                            .executes(ctx -> {

                                CommandSender sender = ctx.getSource().getSender();

                                String name = StringArgumentType.getString(ctx, "name");
                                if (manager.deleteGroup(name)){
                                    if (sender instanceof Player player) {
                                        player.sendMessage(plugin.getMsg(sender,"deleteGroup"));
                                    } else {
                                        sender.sendMessage(plugin.getMsg(sender,"deleteGroup"));
                                    }
                                } else {
                                    if (sender instanceof Player player) {
                                        player.sendMessage(plugin.getMsg(sender,"noGroup"));
                                    } else {
                                        sender.sendMessage(plugin.getMsg(sender,"noGroup"));
                                    }
                                }

                                return 1;
                            })
                        )
                    )
                )
                .then(Commands.literal("stop") //花火停止

                    // 引数なし → 全停止
                    .executes(ctx -> {

                        CommandSender sender = ctx.getSource().getSender();

                        manager.stopShow("all");

                        if (sender instanceof Player player) {
                            player.sendMessage(plugin.getMsg(sender,"stop"));
                        } else {
                            sender.sendMessage(plugin.getMsg(sender,"stop"));
                        }
                        return 1;
                    })

                    .then(Commands.argument("name", StringArgumentType.word())
                        .executes(ctx -> {

                            CommandSender sender = ctx.getSource().getSender();

                            String name = StringArgumentType.getString(ctx, "name");

                            manager.stopShow(name);

                            if (sender instanceof Player player) {
                                player.sendMessage(plugin.getMsg(sender,"stop"));
                            } else {
                                sender.sendMessage(plugin.getMsg(sender,"stop"));
                            }
                            return 1;
                        })
                    )
                )
                .then(Commands.literal("unhide") //表示
                    .executes(ctx -> {

                        CommandSender sender = ctx.getSource().getSender();

                        if (manager.getSavedData().isEmpty()) {
                            if (sender instanceof Player player) {
                                player.sendMessage(plugin.getMsg(sender,"noData"));
                            } else {
                                sender.sendMessage(plugin.getMsg(sender,"noData"));
                            }
                            return 1;
                        }

                        for (Map.Entry<Location, List<ItemStack>> entry : manager.getSavedData().entrySet()) {

                            Location loc = entry.getKey();
                            List<ItemStack> fireworks = entry.getValue();

                            // チェスト設置
                            loc.getBlock().setType(Material.CHEST);

                            Chest chest = (Chest) loc.getBlock().getState();

                            // 花火を入れる
                            for (ItemStack item : fireworks) {
                                chest.getInventory().addItem(item.clone());
                            }
                        }

                        if (sender instanceof Player player) {
                            player.sendMessage(plugin.getMsg(sender,"showChest"));
                        } else {
                            sender.sendMessage(plugin.getMsg(sender,"showChest"));
                        }

                        return 1;
                    })
                )
                    .then(Commands.literal("unhide") //指定表示
                        .then(Commands.argument("name", StringArgumentType.word())
                            .executes(ctx -> {

                                CommandSender sender = ctx.getSource().getSender();

                                if (manager.getSavedData().isEmpty()) {
                                    if (sender instanceof Player player) {
                                        player.sendMessage(plugin.getMsg(sender,"noData"));
                                    } else {
                                        sender.sendMessage(plugin.getMsg(sender,"noData"));
                                    }
                                    return 1;
                                }

                                String name = StringArgumentType.getString(ctx, "name");
                                List<Location> targets = manager.getGroup(name);

                                for (Map.Entry<Location, List<ItemStack>> entry : manager.getSavedData().entrySet()) {

                                    Location loc = entry.getKey();
                                    List<ItemStack> fireworks = entry.getValue();

                                    if (!targets.contains(loc)) continue; //グループにない場合は飛ばす

                                    // チェスト設置
                                    loc.getBlock().setType(Material.CHEST);

                                    Chest chest = (Chest) loc.getBlock().getState();

                                    // 花火を入れる
                                    for (ItemStack item : fireworks) {
                                        chest.getInventory().addItem(item.clone());
                                    }
                                }

                                if (sender instanceof Player player) {
                                    player.sendMessage(plugin.getMsg(sender,"showChest"));
                                } else {
                                    sender.sendMessage(plugin.getMsg(sender,"showChest"));
                                }

                                return 1;
                            })
                        )
                    )
                .then(Commands.literal("hide") //非表示
                    .executes(ctx -> {

                        CommandSender sender = ctx.getSource().getSender();

                        if (manager.getSavedData().isEmpty()) {
                            if (sender instanceof Player player) {
                                player.sendMessage(plugin.getMsg(sender,"noData"));
                            } else {
                                sender.sendMessage(plugin.getMsg(sender,"noData"));
                            }
                            return 1;
                        }

                        for (Map.Entry<Location, List<ItemStack>> entry : manager.getSavedData().entrySet()) {

                            Location loc = entry.getKey();

                            // チェスト削除
                            if (loc.getBlock().getType() == Material.CHEST) {
                                loc.getBlock().setType(Material.AIR);
                            }
                        }

                        if (sender instanceof Player player) {
                            player.sendMessage(plugin.getMsg(sender,"hideChest"));
                        } else {
                            sender.sendMessage(plugin.getMsg(sender,"hideChest"));
                        }

                        return 1;
                    })
                )
                    .then(Commands.literal("hide") //指定非表示
                        .then(Commands.argument("name", StringArgumentType.word())
                            .executes(ctx -> {

                                CommandSender sender = ctx.getSource().getSender();

                                if (manager.getSavedData().isEmpty()) {
                                    if (sender instanceof Player player) {
                                        player.sendMessage(plugin.getMsg(sender,"noData"));
                                    } else {
                                        sender.sendMessage(plugin.getMsg(sender,"noData"));
                                    }
                                    return 1;
                                }

                                String name = StringArgumentType.getString(ctx, "name");
                                List<Location> targets = manager.getGroup(name);

                                for (Map.Entry<Location, List<ItemStack>> entry : manager.getSavedData().entrySet()) {

                                    Location loc = entry.getKey();
                                    if (!targets.contains(loc)) continue; //グループにない場合は飛ばす

                                    // チェスト削除
                                    if (loc.getBlock().getType() == Material.CHEST) {
                                        loc.getBlock().setType(Material.AIR);
                                    }
                                }

                                if (sender instanceof Player player) {
                                    player.sendMessage(plugin.getMsg(sender,"hideChest"));
                                } else {
                                    sender.sendMessage(plugin.getMsg(sender,"hideChest"));
                                }

                                return 1;
                            })
                        )
                    )
                .then(Commands.literal("delete") //設定削除
                    .executes(ctx -> {

                        CommandSender sender = ctx.getSource().getSender();

                        if (!(sender instanceof Player player)) {
                            sender.sendMessage(plugin.getMsg(sender,"playerOnly"));
                            return 1;
                        }

                        FPBListener.deleteSelectingPlayers.add(player.getUniqueId());
                        return 1;
                    })
                    .then(Commands.argument("name", StringArgumentType.word())
                        .executes(ctx -> {
                            CommandSender sender = ctx.getSource().getSender();

                            String name = StringArgumentType.getString(ctx, "name");
                            List<Location> targets = manager.getGroup(name);

                            if (targets.isEmpty()){
                                if (sender instanceof Player player) {
                                    player.sendMessage(plugin.getMsg(sender,"noData"));
                                } else {
                                    sender.sendMessage(plugin.getMsg(sender,"noData"));
                                }
                                return 1;
                            }

                            for (Location loc : targets){
                                if (manager.getSavedData().containsKey(loc)){
                                    manager.getSavedData().remove(loc);
                                    if (sender instanceof Player player) {
                                        player.sendMessage(plugin.getMsg(sender,"removeChest"));
                                    } else {
                                        sender.sendMessage(plugin.getMsg(sender,"removeChest"));
                                    }
                                } else {
                                    if (sender instanceof Player player) {
                                        player.sendMessage(plugin.getMsg(sender,"noData"));
                                    } else {
                                        sender.sendMessage(plugin.getMsg(sender,"noData"));
                                    }
                                }
                            }
                            return 1;
                        })
                    )
                )
                .then(Commands.literal("list") //グループリスト表示
                    .executes(ctx -> {

                        CommandSender sender = ctx.getSource().getSender();

                        if (manager.getGroupList().isEmpty()){
                            if (sender instanceof Player player) {
                                player.sendMessage(plugin.getMsg(sender,"noData"));
                            } else {
                                sender.sendMessage(plugin.getMsg(sender,"noData"));
                            }
                        }
                        for (Map.Entry<String, List<Location>> entry : manager.getGroupList().entrySet()){
                            if (sender instanceof Player player) {
                                player.sendMessage("[" + entry.getKey() + "]");
                            }
                        }
                        return 1;
                    })
                )
            .build()
        );
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        return true;
    }
}
