package net.eltown.playersync.components.provider;

import cn.nukkit.Player;
import cn.nukkit.utils.Config;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.eltown.playersync.PlayerSync;
import net.eltown.playersync.components.data.CallData;
import net.eltown.playersync.components.tinyrabbit.TinyRabbit;
import net.eltown.playersync.components.utils.SyncPlayer;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class Provider {

    private TinyRabbit rabbit;
    private final PlayerSync plugin;

    @SneakyThrows
    public Provider(final PlayerSync plugin) {
        this.plugin = plugin;
        this.rabbit = new TinyRabbit("localhost", "PlayerSync/Server");
        this.rabbit.throwExceptions(true);
    }

    public void savePlayer(String player, String invString, String ecString, String health, int food, int level, int exp) {
        this.rabbit.send("playersyncReceive", CallData.REQUEST_SETSYNC.name(), player, invString, ecString, health, "" + food, "" + level, "" + exp);
    }

    public void getPlayer(Player player, Consumer<SyncPlayer> callback) {
        CompletableFuture.runAsync(() -> {
            this.rabbit.sendAndReceive((delivery -> {
                switch (CallData.valueOf(delivery.getKey())) {
                    case GOT_NOSYNC:
                        this.plugin.getServer().getScheduler().scheduleDelayedTask(() -> this.getPlayer(player, callback), 5);
                        break;
                    case GOT_SYNC:
                        this.rabbit.send(CallData.REQUEST_SETNOSYNC.name(), player.getName());
                        callback.accept(new SyncPlayer(delivery.getData()[1], delivery.getData()[2], Float.parseFloat(delivery.getData()[3]), Integer.parseInt(delivery.getData()[4]), Integer.parseInt(delivery.getData()[5]), Integer.parseInt(delivery.getData()[6])));
                        break;
                }
            }), "playersync", CallData.REQUEST_SYNC.name(), player.getName());
        });
    }
}
