package jempasam.hexlink.spirit.extractor.node

import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import jempasam.hexlink.spirit.StackHelper
import jempasam.hexlink.utils.HexlinkRegistryAccess
import net.minecraft.advancements.critereon.ItemPredicate
import net.minecraft.util.GsonHelper

class FilterItemExtNode(val predicate: ItemPredicate, val not: Boolean) : ExtractionNode {
    override fun filter(source: ExtractionNode.Source): ExtractionNode.Source {
        val target=StackHelper.stack(source.caster,source.entity)
        return if(target!=null && predicate.test(target.stack) != not) source else source.with {count=0}
    }

    object Parser: ExtractionNode.Parser<FilterItemExtNode> {
        override fun parse(obj: JsonObject): FilterItemExtNode = FilterItemExtNode(
            // Item tags are holder sets, which only decode through registry-aware ops.
            ItemPredicate.CODEC.parse(HexlinkRegistryAccess.jsonOps(), obj)
                .getOrThrow { JsonParseException("Invalid item predicate: $it") },
            GsonHelper.getAsBoolean(obj,"not", false)
        )
    }
}
