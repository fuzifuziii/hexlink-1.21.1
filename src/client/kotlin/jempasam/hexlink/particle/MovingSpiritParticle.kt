package jempasam.hexlink.particle

import jempasam.hexlink.particle.effect.MovingSpiritParticleEffect
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.particle.Particle
import net.minecraft.client.particle.ParticleProvider
import net.minecraft.client.particle.ParticleRenderType
import net.minecraft.client.particle.SpriteSet
import net.minecraft.client.particle.TextureSheetParticle
import net.minecraft.util.Mth
import kotlin.math.max

class MovingSpiritParticle(
    world: ClientLevel, x: Double, y: Double, z: Double,
    velocityX: Double, velocityY: Double, velocityZ: Double,
    val spriteProvider: SpriteSet, number: Int
) : TextureSheetParticle(world, x, y, z, velocityX, velocityY, velocityZ) {

    val prob = max(if (number <= 0) lifetime / 2 else lifetime / (number + 1), 1)

    init {
        setSpriteFromAge(spriteProvider)
        gravity = 0.0f
        this.xd = velocityX
        this.yd = velocityY
        this.zd = velocityZ
    }

    override fun getRenderType(): ParticleRenderType = ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT

    override fun tick() {
        super.tick()
        if (age % prob == 0) level.addParticle(HexlinkParticles.SPIRIT, x, y, z, rCol.toDouble(), gCol.toDouble(), bCol.toDouble())
        setSpriteFromAge(this.spriteProvider)
        alpha = Mth.lerp(0.05f, alpha, 1.0f)
    }

    class Provider(private val spriteProvider: SpriteSet) : ParticleProvider<MovingSpiritParticleEffect> {
        override fun createParticle(
            type: MovingSpiritParticleEffect, clientWorld: ClientLevel,
            d: Double, e: Double, f: Double, g: Double, h: Double, i: Double
        ): Particle {
            val particle = MovingSpiritParticle(
                clientWorld,
                d, e, f,
                type.offset.x, type.offset.y, type.offset.z,
                spriteProvider, type.number
            )
            particle.setColor(type.red, type.green, type.blue)
            return particle
        }
    }
}
