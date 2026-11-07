package jempasam.hexlink.particle

import net.minecraft.client.particle.SpellParticle
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleType
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent

/**
 * Client-side particle providers.
 *
 * The old Fabric sprite callback is gone: 1.21 loads particle textures from the
 * particle definition files in the mod's assets, so only the providers are registered here.
 */
object HexlinkClientParticles {

    fun register(event: RegisterParticleProvidersEvent) {
        event.registerSpriteSet(HexlinkParticles.SPIRIT) { spriteSet -> SpellParticle.Provider(spriteSet) }
        event.registerSpriteSet(HexlinkParticles.SPIRIT_FLUX) { spriteSet -> MovingSpiritParticle.Provider(spriteSet) }
    }
}
