package net.eltown.playersync;

import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import lombok.Getter;
import net.eltown.playersync.components.listener.PlayerListener;
import net.eltown.playersync.components.provider.Provider;
import net.eltown.playersync.components.utils.ItemAPI;
import net.eltown.playersync.components.utils.Language;
import net.eltown.playersync.components.utils.Manager;

public class PlayerSync extends PluginBase {

    @Getter
    private Provider provider;
    @Getter
    private boolean sounds;
    @Getter
    private Manager manager;
    @Getter
    private final ItemAPI itemAPI = new ItemAPI();

    @Override
    public void onLoad() {
        Language.init(this);
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        Config c = getConfig();
        this.manager = new Manager(this);
        this.provider = new Provider(this);
        this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    @Override
    public void onDisable() {
        getServer().getOnlinePlayers().forEach(((uuid, player) -> this.manager.savePlayer(player)));
    }
}
