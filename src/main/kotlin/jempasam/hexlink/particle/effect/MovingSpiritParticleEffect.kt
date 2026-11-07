package jempasam.hexlink.particle.effect

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleType
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.phys.Vec3

/**
 * A spirit particle that travels along [offset] over its lifetime.
 *
 * 1.21 replaced the hand-written `ParticleOptions.Factory` with a [MapCodec] plus a
 * [StreamCodec], so both are declared here instead of `write` / `read`.
 */
class MovingSpiritParticleEffect(
    val red: Float,
    val green: Float,
    val blue: Float,
    val number: Int,
    val offset: Vec3
) : ParticleOptions {

    override fun getType(): ParticleType<*> = Type

    companion object {
        val CODEC: MapCodec<MovingSpiritParticleEffect> = RecordCodecBuilder.mapCodec { inst ->
            inst.group(
                com.mojang.serialization.Codec.FLOAT.fieldOf("red").forGetter { it.red },
                com.mojang.serialization.Codec.FLOAT.fieldOf("green").forGetter { it.green },
                com.mojang.serialization.Codec.FLOAT.fieldOf("blue").forGetter { it.blue },
                com.mojang.serialization.Codec.INT.fieldOf("number").forGetter { it.number },
                Vec3.CODEC.fieldOf("offset").forGetter { it.offset }
            ).apply(inst) { r, g, b, n, o -> MovingSpiritParticleEffect(r, g, b, n, o) }
        }

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, MovingSpiritParticleEffect> =
            object : StreamCodec<RegistryFriendlyByteBuf, MovingSpiritParticleEffect> {
                override fun decode(buf: RegistryFriendlyByteBuf): MovingSpiritParticleEffect =
                    MovingSpiritParticleEffect(
                        buf.readFloat(), buf.readFloat(), buf.readFloat(),
                        buf.readVarInt(),
                        Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble())
                    )

                override fun encode(buf: RegistryFriendlyByteBuf, value: MovingSpiritParticleEffect) {
                    buf.writeFloat(value.red)
                    buf.writeFloat(value.green)
                    buf.writeFloat(value.blue)
                    buf.writeVarInt(value.number)
                    buf.writeDouble(value.offset.x)
                    buf.writeDouble(value.offset.y)
                    buf.writeDouble(value.offset.z)
                }
            }
    }

    object Type : ParticleType<MovingSpiritParticleEffect>(false) {
        override fun codec(): MapCodec<MovingSpiritParticleEffect> = CODEC
        override fun streamCodec(): StreamCodec<RegistryFriendlyByteBuf, MovingSpiritParticleEffect> = STREAM_CODEC
    }
}
