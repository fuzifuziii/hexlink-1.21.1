package jempasam.hexlink.spirit.extractor.node

import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import jempasam.hexlink.utils.HexlinkRegistryAccess
import net.minecraft.advancements.critereon.EntityPredicate
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.GsonHelper

class FilterEntityExtNode(val predicate: EntityPredicate, val not: Boolean) : ExtractionNode {
    override fun filter(source: ExtractionNode.Source): ExtractionNode.Source {
        val success = if(source.caster!=null){
            predicate.matches(source.caster,source.entity) != not
        } else{
            predicate.matches(source.entity.level() as ServerLevel, source.entity.position(), source.entity) != not
        }
        return if(success) source else source.with{count=0}
    }

    object Parser: ExtractionNode.Parser<FilterEntityExtNode> {
        override fun parse(obj: JsonObject): FilterEntityExtNode = FilterEntityExtNode(
            // Entity type tags are holder sets, which only decode through registry-aware ops.
            EntityPredicate.CODEC.parse(HexlinkRegistryAccess.jsonOps(), obj)
                .getOrThrow { JsonParseException("Invalid entity predicate: $it") },
            GsonHelper.getAsBoolean(obj,"not", false)
        )
    }
}
