package net.arjun.pool.init;

import net.arjun.pool.PoolRooms;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class PoolSounds {
	public static final Identifier POOL_AMBIENCE_ID = new Identifier(PoolRooms.MOD_ID, "music.pool_ambience");

	public static final SoundEvent POOL_AMBIENCE = SoundEvent.createFixedRangeEvent(POOL_AMBIENCE_ID, 16.0F);

	public static void register() {
		Registry.register(Registries.SOUND_EVENT, POOL_AMBIENCE_ID, POOL_AMBIENCE);
	}
}
