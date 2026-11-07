package jempasam.hexlink.iota

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import com.mojang.serialization.MapCodec
import jempasam.hexlink.HexlinkRegistry
import jempasam.hexlink.spirit.Spirit
import jempasam.hexlink.utils.NbtHelper
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel

class SpiritIota(val spirit: Spirit) : Iota({ Type }) {

    override fun toleratesOther(that: Iota): Boolean = that is SpiritIota && spirit == that.spirit

    override fun isTruthy(env: CastingEnvironment?): Boolean = true

    override fun display(): Component =
        spirit.getName().copy().append(Component.translatable("hexlink.spirit"))

    override fun hashCode(): Int = spirit.hashCode()

    object Type : IotaType<SpiritIota>() {

        val CODEC: MapCodec<SpiritIota> = CompoundTag.CODEC
            .xmap({ tag -> SpiritIota(NbtHelper.readSpirit(tag) ?: throw IllegalStateException("Invalid spirit")) },
                  { NbtHelper.writeSpirit(it.spirit) })
            .fieldOf("spirit")

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, SpiritIota> =
            object : StreamCodec<RegistryFriendlyByteBuf, SpiritIota> {
                override fun decode(buf: RegistryFriendlyByteBuf): SpiritIota =
                    SpiritIota(NbtHelper.readSpirit(buf.readNbt() ?: CompoundTag()) ?: throw IllegalStateException("Invalid spirit"))

                override fun encode(buf: RegistryFriendlyByteBuf, value: SpiritIota) {
                    buf.writeNbt(NbtHelper.writeSpirit(value.spirit))
                }
            }

        override fun codec(): MapCodec<SpiritIota> = CODEC

        override fun streamCodec(): StreamCodec<RegistryFriendlyByteBuf, SpiritIota> = STREAM_CODEC

        override fun color(): Int = 0xCDE7FF

        override fun validate(iota: SpiritIota, level: ServerLevel): Boolean = true
    }
}
