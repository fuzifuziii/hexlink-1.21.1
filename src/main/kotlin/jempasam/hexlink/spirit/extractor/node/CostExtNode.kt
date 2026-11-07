package jempasam.hexlink.spirit.extractor.node

import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.util.GsonHelper
import net.minecraft.util.ExtraCodecs
import kotlin.math.ceil

class CostExtNode(val base: Int, val multiplier: Float) : ExtractionNode {
    override fun filter(source: ExtractionNode.Source): ExtractionNode.Source {
        return source.with {
            val prev=consumer
            consumer={prev(ceil(it*multiplier+base).toInt())}
        }
    }

    object Parser: ExtractionNode.Parser<CostExtNode> {
        override fun parse(obj: JsonObject): CostExtNode {
            return CostExtNode(
                GsonHelper.getAsInt(obj,"base", 0),
                GsonHelper.getAsFloat(obj,"multiplier", 1.0f)
            )
        }
    }

    companion object{
        val CODEC= RecordCodecBuilder.create<CostExtNode> { inst ->
            inst.group(
                ExtraCodecs.NON_NEGATIVE_INT .fieldOf("base") .forGetter(CostExtNode::base),
                Codec.FLOAT.fieldOf("multiplier") .forGetter(CostExtNode::multiplier)
            ).apply(inst) { base, multiplier -> CostExtNode(base, multiplier) }
        }
    }
}