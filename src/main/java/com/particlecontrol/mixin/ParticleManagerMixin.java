package com.particlecontrol.mixin;

import com.particlecontrol.config.ParticleConfig;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Every particle spawned client-side eventually funnels through
 * ParticleManager#addParticle(ParticleEffect, x, y, z, vx, vy, vz).
 * Cancelling here stops the particle before an instance is even
 * created, which is cheaper than filtering after the fact and
 * catches vanilla particles and modded ones alike.
 */
@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {

    @Inject(
            method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)Lnet/minecraft/client/particle/Particle;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void particlecontrol$filterAddParticle(ParticleEffect parameters, double x, double y, double z,
                                                     double velocityX, double velocityY, double velocityZ,
                                                     CallbackInfoReturnable<Particle> cir) {
        Identifier id = Registries.PARTICLE_TYPE.getId(parameters.getType());
        if (id == null) {
            return;
        }
        if (!ParticleConfig.isEnabled(id)) {
            cir.setReturnValue(null);
        }
    }
}
