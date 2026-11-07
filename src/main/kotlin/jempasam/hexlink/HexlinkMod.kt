package jempasam.hexlink

import at.petrak.hexcasting.common.lib.HexRegistries
import jempasam.hexlink.block.HexlinkBlocks
import jempasam.hexlink.command.HexlinkCommands
import jempasam.hexlink.creative_tab.HexlinkCreativeTab
import jempasam.hexlink.data.HexlinkDataLoaders
import jempasam.hexlink.entity.HexlinkEntities
import jempasam.hexlink.gamerule.HexlinkGamerules
import jempasam.hexlink.iota.HexlinkIotas
import jempasam.hexlink.item.HexlinkItems
import jempasam.hexlink.loot.function.HexlinkLootFunctions
import jempasam.hexlink.network.HexlinkNetwork
import jempasam.hexlink.network.SendDataPacket
import jempasam.hexlink.particle.HexlinkParticles
import jempasam.hexlink.recipe.HexlinkRecipes
import jempasam.hexlink.spirit.HexlinkSpirits
import jempasam.hexlink.spirit.extractor.node.ExtNodeParsers
import jempasam.hexlink.utils.HexlinkRegistryAccess
import jempasam.hexlink.world.LevelRanks
import net.minecraft.core.Registry
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.DyeColor
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.Item
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.RegisterCommandsEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.event.server.ServerStartedEvent
import net.neoforged.neoforge.registries.RegisterEvent
import org.slf4j.LoggerFactory

@Mod(HexlinkMod.MODID)
class HexlinkMod(bus: IEventBus) {

    companion object {
        const val MODID = "hexlink"
        val logger = LoggerFactory.getLogger("hexlink")
    }

    init {
        logger.info("Hexlink started!")

        // Touch the lazy objects so their static content is built before registration.
        HexlinkPatterns
        HexlinkSpirits
        ExtNodeParsers
        HexlinkDataLoaders
        HexlinkGamerules.register()

        HexlinkRegistry.RANK.clear()
        HexlinkRegistry.RANK.register(
            ResourceLocation.fromNamespaceAndPath(MODID, "testrank"),
            LevelRanks.Rank(0.1f, 10.0f, DyeColor.YELLOW.getFireworkColor())
        )
        HexlinkRegistry.RANK.lock()

        register(bus)
        HexlinkNetwork.init(bus)

        NeoForge.EVENT_BUS.addListener(::onRegisterCommands)
        NeoForge.EVENT_BUS.addListener(HexlinkDataLoaders::register)
        NeoForge.EVENT_BUS.addListener(::onServerStarted)
        NeoForge.EVENT_BUS.addListener(::onPlayerLoggedIn)
    }

    /** Wires every registry Hexlink contributes to. */
    private fun register(bus: IEventBus) {
        bind(bus, Registries.BLOCK) { HexlinkBlocks.registerBlocks(it) }
        bind(bus, Registries.ITEM) { HexlinkItems.registerAll(it) }
        bind(bus, Registries.BLOCK_ENTITY_TYPE) { HexlinkEntities.registerBlockEntities(it) }
        bind(bus, Registries.PARTICLE_TYPE) { HexlinkParticles.registerAll(it) }
        bind(bus, Registries.RECIPE_SERIALIZER) { HexlinkRecipes.registerRecipes(it) }
        bind(bus, Registries.LOOT_FUNCTION_TYPE) { HexlinkLootFunctions.registerAll(it) }
        bind(bus, Registries.CREATIVE_MODE_TAB) { HexlinkCreativeTab.register(it) }
        bind(bus, HexRegistries.IOTA_TYPE) { HexlinkIotas.registerAll(it) }
        bind(bus, HexRegistries.ACTION) { HexlinkPatterns.registerAll(it) }
    }

    private fun <T> bind(bus: IEventBus, registry: ResourceKey<out Registry<T>>, action: (Registry<T>) -> Unit) {
        bus.addListener { event: RegisterEvent ->
            event.register(registry) { _ ->
                event.getRegistry(registry)?.let(action)
            }
        }
    }

    private fun onRegisterCommands(event: RegisterCommandsEvent) {
        HexlinkCommands.registerCommands(event.dispatcher, event.commandSelection, event.buildContext)
    }

    private fun onServerStarted(event: ServerStartedEvent) {
        HexlinkRegistryAccess.update(event.server.registryAccess())
    }

    private fun onPlayerLoggedIn(event: PlayerEvent.PlayerLoggedInEvent) {
        val player = event.entity as? ServerPlayer ?: return
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, SendDataPacket.build())
    }
}
