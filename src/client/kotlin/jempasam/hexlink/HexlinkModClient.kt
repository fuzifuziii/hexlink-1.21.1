package jempasam.hexlink

import jempasam.hexlink.config.HexlinkClientConfig
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import jempasam.hexlink.block.HexlinkBlockClient
import jempasam.hexlink.config.Config
import jempasam.hexlink.item.color.HexlinkColorProviders
import jempasam.hexlink.lens.HexlinkScryingLensDisplay
import jempasam.hexlink.model.predicate.HexlinkModelPredicates
import jempasam.hexlink.particle.HexlinkClientParticles
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.fml.loading.FMLPaths
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent

/**
 * Client-only entry point.
 *
 * `@Mod(dist = CLIENT)` keeps this class off dedicated servers, so it is safe to reference
 * client types here.
 */
@Mod(value = HexlinkMod.MODID, dist = [Dist.CLIENT])
class HexlinkModClient(bus: IEventBus) {

    init {
        // Config
        config(HexlinkClientConfig, "hexlink-client")

        bus.addListener(::onClientSetup)
        bus.addListener(::onRegisterParticleProviders)
        bus.addListener(::onRegisterBlockColors)
        bus.addListener(::onRegisterItemColors)
    }

    fun config(config: Config, file: String) {
        val gson = GsonBuilder().setPrettyPrinting().create()
        val clientConfigFile = FMLPaths.CONFIGDIR.get().resolve("$file.json")
        if (clientConfigFile.toFile().exists()) {
            clientConfigFile.toFile().reader().use { reader ->
                val obj = gson.fromJson(reader, JsonObject::class.java)
                if (obj != null) config.load(obj)
            }
        } else {
            clientConfigFile.toFile().createNewFile()
        }

        clientConfigFile.toFile().writer().use { writer ->
            gson.toJson(config.save(), writer)
        }
    }

    private fun onClientSetup(event: FMLClientSetupEvent) {
        event.enqueueWork {
            HexlinkModelPredicates.registerItemPredicates()
            HexlinkBlockClient.registerBlockRender()
            HexlinkScryingLensDisplay.registerDisplays()
        }
    }

    private fun onRegisterParticleProviders(event: RegisterParticleProvidersEvent) {
        HexlinkClientParticles.register(event)
    }

    private fun onRegisterBlockColors(event: RegisterColorHandlersEvent.Block) {
        HexlinkBlockClient.registerBlockColors(event)
    }

    private fun onRegisterItemColors(event: RegisterColorHandlersEvent.Item) {
        HexlinkColorProviders.registerItemColors(event)
    }
}
