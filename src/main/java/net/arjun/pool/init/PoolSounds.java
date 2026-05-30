package net.arjun.pool.init;

import net.arjun.pool.PoolRooms;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public class PoolSounds {
	public static final Identifier POOL_AMBIENCE_ID = new Identifier(PoolRooms.MOD_ID, "music.pool_ambience");

	public static final SoundEvent POOL_AMBIENCE = SoundEvent.createFixedRangeEvent(POOL_AMBIENCE_ID, 16.0F);

	public static void register() {
		Registry.register(Registries.SOUND_EVENT, POOL_AMBIENCE_ID, POOL_AMBIENCE);
	}

	public static void runAmbientSounds(MinecraftClient client) {
		if (client.player == null || client.world == null) return;

		String currentDimension = client.world.getRegistryKey().getValue().toString();
		if (!currentDimension.equals("pool:pools")) return;

		Vec3d velocity = client.player.getVelocity();
		double horizontalSpeed = Math.sqrt((velocity.x * velocity.x) + (velocity.z * velocity.z));

		if (horizontalSpeed > 0.05) {

			// 4. The RNG (1 in 1200 chance per tick = roughly once every 60 seconds of movement)
			if (client.world.random.nextInt(1200) == 0) {

				// 5. The Math: Find the spot 3 blocks directly behind where the player is looking
				Vec3d lookVec = client.player.getRotationVec(1.0F);
				Vec3d playerPos = client.player.getPos();

				// We only subtract X and Z so the sound stays at foot level, not up in the air!
				double behindX = playerPos.x - (lookVec.x * 12.0);
				double behindY = playerPos.y;
				double behindZ = playerPos.z - (lookVec.z * 12.0);

				// 6. Play the ghost footstep
				client.world.playSound(
					behindX, behindY, behindZ,
					SoundEvents.ENTITY_PLAYER_SPLASH, // The vanilla splash sound
					SoundCategory.AMBIENT,
					0.7f, // Volume
					0.8f + (client.world.random.nextFloat() * 0.4f), // Randomize pitch so it sounds organic
					false
				);
			}
		}
	}
}
