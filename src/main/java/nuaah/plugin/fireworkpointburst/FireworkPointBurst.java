package nuaah.plugin.fireworkpointburst;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import nuaah.plugin.fireworkpointburst.Commnad.FPBCommand;
import nuaah.plugin.fireworkpointburst.Commnad.FPBListener;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class FireworkPointBurst extends JavaPlugin {

    private FileConfiguration langConfig; //翻訳ファイル
    private FireworkManager manager;

    @Override
    public void onEnable() {

        super.onEnable();

        manager = new FireworkManager(this);

        getCommand("fpb").setExecutor(new FPBCommand(this));
        getServer().getPluginManager().registerEvents(new FPBListener(this), this);
        getCommand("fpb").setTabCompleter(new TabCompleter());

        File file = new File(getDataFolder(), "lang.yml");
        if (!file.exists()) {
            saveResource("lang.yml", false);
        }
        loadLang();

        FPBCommand registrar = new FPBCommand(this);

        this.getLifecycleManager().registerEventHandler(
                LifecycleEvents.COMMANDS,
                event -> {
                    registrar.registerCommands(event.registrar());
                }
        );

        saveDefaultConfig();
        manager.loadData();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        super.onDisable();
        manager.saveData();
    }

    public FireworkManager getManager() {
        return manager;
    }

    public void loadLang() {
        File file = new File(getDataFolder(), "lang.yml");

        if (!file.exists()) {
            saveResource("lang.yml", false);
        }

        langConfig = YamlConfiguration.loadConfiguration(file);
    }

    public String getMsg(CommandSender sender, String key) {

        String lang = "en"; // デフォルト

        if (sender instanceof Player player) {
            String locale = player.locale().toString();

            if (locale.startsWith("ja")) lang = "ja";
            else lang = "en";
        }

        return langConfig.getString(lang + "." + key);
    }

}
