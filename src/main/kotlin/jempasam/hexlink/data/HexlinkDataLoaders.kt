package jempasam.hexlink.data

import net.neoforged.neoforge.event.AddReloadListenerEvent

object HexlinkDataLoaders {

    @JvmStatic
    val SPIRIT_CONFIG = HexlinkSpiritDataLoader()

    @JvmStatic
    val SPIRITS = SpecialSpiritDataLoader
    @JvmStatic
    val EXTRACTORS = SpecialExtractorDataLoader

    private val ALL = listOf(SPIRIT_CONFIG, SPIRITS, EXTRACTORS)

    /** Called from NeoForge's `AddReloadListenerEvent`. */
    fun register(event: AddReloadListenerEvent) {
        // Entity/item predicates reference tags and enchantments are datapack registries, so
        // the loaders below need the registry access of the reload that is about to run.
        jempasam.hexlink.utils.HexlinkRegistryAccess.update(event.registryAccess)
        for (loader in ALL) {
            event.addListener(loader)
        }
    }
}
