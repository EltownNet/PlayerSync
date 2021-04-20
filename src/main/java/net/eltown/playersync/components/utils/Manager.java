package net.eltown.playersync.components.utils;

import cn.nukkit.Player;
import cn.nukkit.level.Sound;
import cn.nukkit.network.protocol.PlaySoundPacket;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.eltown.playersync.PlayerSync;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class Manager {

    private final PlayerSync plugin;

    @Getter
    private final ArrayList<String> loaded = new ArrayList<>();

    public void savePlayerAsync(Player player) {
        CompletableFuture.runAsync(() -> {
            try {
                this.savePlayer(player);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void savePlayer(Player player) {
        if (!this.loaded.contains(player.getName())) return;
        String inv = "empty";
        String ec = "empty";
        if (player.getEnderChestInventory().getContents().size() > 0)
            ec = this.plugin.getItemAPI().invToString(player.getEnderChestInventory());
        if (player.getInventory().getContents().size() > 0) inv = this.plugin.getItemAPI().invToString(player.getInventory());

        this.plugin.getProvider().savePlayer(player.getName(), inv, ec, player.getHealth() + "", player.getFoodData().getLevel(), player.getExperienceLevel(), player.getExperience());
    }

    public void loadPlayer(Player player) {
        this.loaded.remove(player.getName());

        player.getInventory().clearAll();
        player.getEnderChestInventory().clearAll();
        player.setExperience(0, 0);

        player.sendMessage(Language.get("loadingData"));
        playSound(player, Sound.RANDOM_ORB);

        this.plugin.getProvider().getPlayer(player, (syncPlayer -> {
            player.getInventory().setContents(this.plugin.getItemAPI().invFromString(syncPlayer.getInvString()));
            player.getEnderChestInventory().setContents(this.plugin.getItemAPI().invFromString(syncPlayer.getEcString()));
            player.setHealth(syncPlayer.getHealth());
            player.getFoodData().setLevel(syncPlayer.getFood());
            player.setExperience(syncPlayer.getExp(), syncPlayer.getLevel());

            loaded.add(player.getName());
            player.sendMessage(Language.get("loadingDone"));
            playSound(player, Sound.RANDOM_LEVELUP);
        }));
    }

    public void playSound(Player player, Sound sound) {
        if (!this.plugin.isSounds()) return;
        PlaySoundPacket packet = new PlaySoundPacket();
        packet.name = sound.getSound();
        packet.x = (int) player.getPosition().getX();
        packet.y = (int) player.getPosition().getY();
        packet.z = (int) player.getPosition().getZ();
        packet.volume = 1.0f;
        packet.pitch = 1.0f;
        player.dataPacket(packet);
    }

}
