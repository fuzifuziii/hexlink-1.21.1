package jempasam.hexlink.network

import com.google.gson.Gson
import com.google.gson.JsonObject
import jempasam.hexlink.HexlinkMod
import jempasam.hexlink.HexlinkRegistry
import jempasam.hexlink.spirit.BlockSpirit
import jempasam.hexlink.spirit.SpecialSpirit
import jempasam.hexlink.spirit.extractor.NodeExtractor
import jempasam.hexlink.spirit.extractor.node.ResultExtNode
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.util.GsonHelper
import net.minecraft.world.level.block.Blocks

/**
 * Server -> client sync of Hexlink's data-driven content.
 *
 * Fabric sent this as a raw buffer on a named channel. NeoForge requires a typed
 * [CustomPacketPayload]; the wire format below is unchanged, only wrapped.
 */
class SendDataPacket(private val data: ByteArray) : CustomPacketPayload {

    companion object {
        val TYPE = CustomPacketPayload.Type<SendDataPacket>(
            ResourceLocation.fromNamespaceAndPath(HexlinkMod.MODID, "data_sync")
        )

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, SendDataPacket> =
            object : StreamCodec<RegistryFriendlyByteBuf, SendDataPacket> {
                override fun decode(buf: RegistryFriendlyByteBuf): SendDataPacket {
                    val bytes = ByteArray(buf.readableBytes())
                    buf.readBytes(bytes)
                    return SendDataPacket(bytes)
                }

                override fun encode(buf: RegistryFriendlyByteBuf, value: SendDataPacket) {
                    buf.writeBytes(value.data)
                }
            }

        /** Build the sync payload from the current server-side registries. */
        fun build(): SendDataPacket {
            val buffer = FriendlyByteBuf(io.netty.buffer.Unpooled.buffer())

            // Extractors
            buffer.writeVarInt(HexlinkRegistry.EXTRACTOR.size())
            HexlinkRegistry.EXTRACTOR.entrySet.forEach { (id, extractor) ->
                buffer.writeResourceLocation(id.location())
                buffer.writeUtf(componentToJson(extractor.getName()))
                buffer.writeCollection(extractor.colors) { buf, it -> buf.writeInt(it) }
                buffer.writeInt(extractor.getCost())
                buffer.writeInt(extractor.duration)
            }

            // Special Spirits
            buffer.writeVarInt(HexlinkRegistry.SPECIAL_SPIRIT.size())
            HexlinkRegistry.SPECIAL_SPIRIT.entrySet.forEach { (id, spirit) ->
                buffer.writeResourceLocation(id.location())
                buffer.writeUtf(componentToJson(spirit.text))
                buffer.writeInt(spirit.color)
            }

            val bytes = ByteArray(buffer.readableBytes())
            buffer.readBytes(bytes)
            return SendDataPacket(bytes)
        }
    }

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

    /** Apply the synced data on the client. */
    fun apply() {
        val buf = FriendlyByteBuf(io.netty.buffer.Unpooled.wrappedBuffer(data))

        HexlinkRegistry.EXTRACTOR.clear()
        val extractorCount = buf.readVarInt()
        for (i in 0 until extractorCount) {
            val id = buf.readResourceLocation()
            val extractor = NodeExtractor.of(
                componentFromJson(buf.readUtf()),
                buf.readList { it.readInt() },
                buf.readInt(),
                buf.readInt(),
                ResultExtNode(0, 0f, null)
            )
            HexlinkRegistry.EXTRACTOR.register(id, extractor)
        }
        HexlinkRegistry.EXTRACTOR.lock()

        HexlinkRegistry.SPECIAL_SPIRIT.clear()
        val spiritCount = buf.readVarInt()
        for (i in 0 until spiritCount) {
            val id = buf.readResourceLocation()
            val spirit = SpecialSpirit.SpecialType(
                BlockSpirit(Blocks.AIR), BlockSpirit(Blocks.AIR), BlockSpirit(Blocks.AIR), BlockSpirit(Blocks.AIR),
                componentFromJson(buf.readUtf()), buf.readInt()
            )
            HexlinkRegistry.SPECIAL_SPIRIT.register(id, spirit)
        }
        HexlinkRegistry.SPECIAL_SPIRIT.lock()
        HexlinkMod.logger.info("Hexlink synchronized with server data.")
    }
}

/** Serialize a chat component as JSON so styles survive the round trip. */
private fun componentToJson(component: Component): String =
    ComponentSerialization.CODEC
        .encodeStart(com.mojang.serialization.JsonOps.INSTANCE, component)
        .getOrThrow()
        .toString()

/** Inverse of [componentToJson]; falls back to a literal on malformed input. */
private fun componentFromJson(json: String): Component =
    ComponentSerialization.CODEC
        .parse(com.mojang.serialization.JsonOps.INSTANCE, com.google.gson.JsonParser.parseString(json))
        .result()
        .orElse(Component.literal(json))
