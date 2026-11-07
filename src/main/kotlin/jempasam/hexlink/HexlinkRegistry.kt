package jempasam.hexlink

import com.mojang.serialization.Lifecycle
import jempasam.hexlink.spirit.SpecialSpirit
import jempasam.hexlink.spirit.Spirit
import jempasam.hexlink.spirit.extractor.NodeExtractor
import jempasam.hexlink.spirit.extractor.SpiritExtractor
import jempasam.hexlink.spirit.extractor.node.ExtractionNode
import jempasam.hexlink.utils.LoadableRegistry
import jempasam.hexlink.world.LevelRanks
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation

/**
 * Hexlink's own content tables.
 *
 * These are deliberately *not* NeoForge registries: several of them (extractors, special
 * spirits) are wiped and rebuilt whenever the server syncs its data to
 * the client, which a frozen [Registry] cannot express. See [LoadableRegistry].
 */
object HexlinkRegistry {

    private fun <T> registryId(id: String): ResourceKey<Registry<T>> =
        ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(HexlinkMod.MODID, id))

    private fun <T> loadableRegistry(key: ResourceKey<Registry<T>>): LoadableRegistry<T> =
        LoadableRegistry(key, Lifecycle.stable())

    // Spirit and extractors
    val SPIRIT_KEY = registryId<Spirit.SpiritType<*>>("spirit")
    val SPIRIT = loadableRegistry(SPIRIT_KEY)

    val EXTRACTOR_KEY = registryId<NodeExtractor>("spirit_extractor")
    val EXTRACTOR = loadableRegistry(EXTRACTOR_KEY)

    val EXTRACTOR_SERIALIZER_KEY = registryId<SpiritExtractor.Serializer<*>>("extractor_serializer")
    val EXTRACTOR_SERIALIZER = loadableRegistry(EXTRACTOR_SERIALIZER_KEY)

    val EXTRACTOR_NODE_PARSER_KEY = registryId<ExtractionNode.Parser<*>>("extractor_parser")
    val EXTRACTOR_NODE_PARSER = loadableRegistry(EXTRACTOR_NODE_PARSER_KEY)
    val SPECIAL_SPIRIT_KEY = registryId<SpecialSpirit.SpecialType>("special_spirit")
    val SPECIAL_SPIRIT = loadableRegistry(SPECIAL_SPIRIT_KEY)

    val RANK_KEY = registryId<LevelRanks.Rank>("mage_rank")
    val RANK = loadableRegistry(RANK_KEY)
}
