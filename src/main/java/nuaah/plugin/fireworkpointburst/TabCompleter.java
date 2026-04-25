package nuaah.plugin.fireworkpointburst;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TabCompleter implements org.bukkit.command.TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (strings.length == 1) {
            return List.of("set", "play","look","hide","stop","delete","group");
        }

        if(strings[0].equals("group")){
            if (strings.length == 2) {
                return List.of("create","delete");
            }
        }

        if(strings[0].equals("play")){
            if (strings.length == 3) {
                return List.of("false","true");
            }
            if (strings.length == 4) {
                return List.of("select","random");
            }
        }

        return Collections.emptyList();
    }


}
