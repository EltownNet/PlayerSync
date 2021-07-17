package net.eltown.playersync.components.provider;

import cn.nukkit.Player;
import cn.nukkit.potion.Effect;
import cn.nukkit.utils.Config;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.eltown.playersync.PlayerSync;
import net.eltown.playersync.components.data.CallData;
import net.eltown.playersync.components.tinyrabbit.TinyRabbit;
import net.eltown.playersync.components.utils.SyncPlayer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    public void savePlayer(String player, String invString, String ecString, String health, int food, int level, int exp, int gamemode, final List<String> effects) {
        this.rabbit.send("playersyncReceive", CallData.REQUEST_SETSYNC.name(), player, invString, ecString, health, "" + food, "" + level, "" + exp, effects.size() > 0 ? String.join("#", effects) : "empty", "" + gamemode);
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
                        final Set<Effect> effects = new HashSet<>();
                        if (!delivery.getData()[7].equalsIgnoreCase("empty")) {
                            final String[] splittedEffects = delivery.getData()[7].split("#");

                            for (final String str : splittedEffects) {
                                final String[] effectSplit = str.split(":");
                                effects.add(Effect.getEffect(Integer.parseInt(effectSplit[0]))
                                        .setAmplifier(Integer.parseInt(effectSplit[1]))
                                        .setDuration(Integer.parseInt(effectSplit[2]))
                                );
                            }
                        }
                        callback.accept(new SyncPlayer(delivery.getData()[1], delivery.getData()[2], Float.parseFloat(delivery.getData()[3]), Integer.parseInt(delivery.getData()[4]), Integer.parseInt(delivery.getData()[5]), Integer.parseInt(delivery.getData()[6]), Integer.parseInt(delivery.getData()[8]), effects));
                        break;
                }
            }), "playersync", CallData.REQUEST_SYNC.name(), player.getName());
        });
    }
}
