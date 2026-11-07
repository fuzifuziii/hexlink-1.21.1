package jempasam.hexlink.particle


import jempasam.hexlink.HexlinkMod
import jempasam.hexlink.particle.effect.MovingSpiritParticleEffect
import net.minecraft.core.particles.SimpleParticleType
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleType
import net.minecraft.server.level.ServerLevel
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.Vec3
import net.minecraft.core.Registry
import net.minecraft.world.level.Level

object HexlinkParticles {
    private val TYPES = mutableMapOf<ResourceLocation, ParticleType<*>>()

    private fun <P: ParticleOptions, T: ParticleType<P>> create(name: String, type: T): T{
        TYPES[ResourceLocation.fromNamespaceAndPath(HexlinkMod.MODID, name)] = type
        return type
    }

    /** Called from the NeoForge `RegisterEvent` for `Registries.PARTICLE_TYPE`. */
    fun registerAll(registry: Registry<ParticleType<*>>){
        for((id, type) in TYPES) Registry.register(registry, id, type)
    }

    val SPIRIT: SimpleParticleType = create("spirit", SimpleParticleType(false))
    val SPIRIT_FLUX = create("spirit_flux", MovingSpiritParticleEffect.Type)

    fun burst(world: Level, pos: Vec3, color: Int, count: Int){
        val r = (color shr 16 and 0xFF).toDouble() / 255.0
        val g = (color shr 8 and 0xFF).toDouble() / 255.0
        val b = (color shr 0 and 0xFF).toDouble() / 255.0
        for (j in 0 until count) {
            val pos=pos.add(Math.random(), Math.random(),Math.random())
            world.addParticle(
                    SPIRIT,
                    pos.x, pos.y, pos.z,
                    r, g, b
            )
        }
    }

    fun sendLink(world: ServerLevel, from: Vec3, to: Vec3, color: Int, count: Int){
        val offset=to.subtract(from).scale(0.06)
        val r = (color shr 16 and 0xFF).toFloat() / 255.0f
        val g = (color shr 8 and 0xFF).toFloat() / 255.0f
        val b = (color shr 0 and 0xFF).toFloat() / 255.0f
        world.sendParticles(
                MovingSpiritParticleEffect(r,g,b,count, offset),
                from.x, from.y, from.z,
                count/5,
                0.1,0.1,0.1,
                0.0
        )
    }
}