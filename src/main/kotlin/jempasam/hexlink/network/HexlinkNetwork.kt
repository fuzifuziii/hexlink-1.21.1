package jempasam.hexlink.network

import jempasam.hexlink.HexlinkMod
import jempasam.hexlink.utils.HexlinkRegistryAccess
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent

object HexlinkNetwork {

    /** Called during mod construction with the mod event bus. */
    fun init(bus: IEventBus) {
        bus.addListener { event: RegisterPayloadHandlersEvent ->
            val registrar = event.registrar("1")
            registrar.playToClient(
                SendDataPacket.TYPE,
                SendDataPacket.STREAM_CODEC
            ) { payload, context ->
                // In singleplayer the integrated server shares these tables (and the registry
                // access cache) with the client. Applying the lossy synced copy would replace the
                // server's real extractors and special spirits with stubs, so only remote
                // connections take the client-side path.
                if (!context.connection().isMemoryConnection) {
                    HexlinkRegistryAccess.update(context.player().level().registryAccess())
                    payload.apply()
                }
            }
        }
        HexlinkMod.logger.debug("Hexlink network handlers registered.")
    }
}
