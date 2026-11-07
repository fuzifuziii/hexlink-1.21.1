package jempasam.hexlink.spirit.extractor.node

import com.google.gson.JsonObject
import jempasam.hexlink.HexlinkRegistry
import jempasam.hexlink.spirit.extractor.NodeExtractor
import net.minecraft.resources.ResourceLocation
import net.minecraft.resources.ResourceKey

/**
 * A spirit extractor node referencing another spirit extractor
 */
class ReferenceExtNode(val referenced: ResourceKey<NodeExtractor>) : ExtractionNode {

    override fun filter(source: ExtractionNode.Source): ExtractionNode.Source {
        val extractor= HexlinkRegistry.EXTRACTOR.get(referenced)
        return extractor?.filter(source) ?: source
    }

    object Parser: ExtractionNode.Parser<ReferenceExtNode> {
        override fun parse(obj: JsonObject): ReferenceExtNode{
            val id= obj.get("id").asString
            val key= ResourceKey.create(HexlinkRegistry.EXTRACTOR_KEY, ResourceLocation.parse(id))
            return ReferenceExtNode(key)
        }
    }

    val CODEC= ResourceKey.codec(HexlinkRegistry.EXTRACTOR_KEY)
}