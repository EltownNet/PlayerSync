package net.eltown.playersync.components.utils;

import cn.nukkit.potion.Effect;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Getter
@RequiredArgsConstructor
public class SyncPlayer {

    private final String invString, ecString;
    private final float health;
    private final int food, exp, level, gamemode;
    private final Set<Effect> effects;

}
