package jempasam.hexlink.spirit.extractor

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import jempasam.hexlink.HexlinkRegistry
import jempasam.hexlink.spirit.Spirit
import jempasam.hexlink.spirit.extractor.node.ExtractionNode
import jempasam.hexlink.spirit.extractor.node.ExtractionNode.Source
import jempasam.hexlink.utils.read
import net.minecraft.world.entity.Entity
import net.minecraft.server.level.ServerPlayer
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.GsonHelper

class NodeExtractor(private val name: Component, public val colors: List<Int>, private val cost: Int, public val duration: Int, private val root: PlacedNode): SpiritExtractor<Spirit> {

    override fun extract(caster: ServerPlayer?, target: Entity): SpiritExtractor.ExtractionResult<Spirit> {
        val ret=root.filter(Source(1, caster, target, null) {})
        return SpiritExtractor.ExtractionResult(ret.spirit, ret.count, ret::consume)
    }

    fun filter(source: ExtractionNode.Source): ExtractionNode.Source = root.filter(source)

    override fun getColor(): Int{
        if(colors.isEmpty())return 0x000000
        else if(colors.size==1)return colors[0]
        else{
            val time=(System.currentTimeMillis()%(colors.size*duration-1)).toInt()
            val actual=time/duration
            val previous= if(actual==0) colors.size-1 else actual-1
            val transition=time%duration/duration.toFloat()
            val rtransition=1f-transition

            val pcolor=colors[previous]
            val acolor=colors[actual]
            return net.minecraft.util.FastColor.ARGB32.color(
                255,
                (net.minecraft.util.FastColor.ARGB32.red(pcolor) *rtransition + net.minecraft.util.FastColor.ARGB32.red(acolor) *transition).toInt(),
                (net.minecraft.util.FastColor.ARGB32.green(pcolor) *rtransition + net.minecraft.util.FastColor.ARGB32.green(acolor) *transition).toInt(),
                (net.minecraft.util.FastColor.ARGB32.blue(pcolor) *rtransition + net.minecraft.util.FastColor.ARGB32.blue(acolor) *transition).toInt()
            )
        }
    }

    override fun getCost(): Int = cost

    override fun getName(): Component = name

    interface PlacedNode{
        fun filter(source: Source): Source
    }
    private class ListPlacedNode(val nodes: List<PlacedNode>): PlacedNode{
        override fun filter(source: Source): Source{
            var ret=source
            for(n in nodes){
                if(ret.count<=0)break
                ret=n.filter(ret)
            }
            return ret
        }
    }

    private class UniquePlacedNode(val node: ExtractionNode): PlacedNode{
        override fun filter(source: Source): Source = node.filter(source)
    }

    private class MultiPlacedNode(val content: List<PlacedNode>): PlacedNode{
        override fun filter(source: Source): Source{
            for(c in content){
                val ret=c.filter(source.copy())
                if(ret.count!=0 && ret.spirit!=null)return ret
            }
            return source.with { count=0 }
        }
    }

    companion object{

        fun of(name: Component, colors: List<Int>, duration: Int, cost: Int, node: ExtractionNode) = NodeExtractor(name,colors,duration,cost,UniquePlacedNode(node))

        fun parse(obj: JsonObject): NodeExtractor {
            return NodeExtractor(
                obj.get("name")?.let { net.minecraft.network.chat.ComponentSerialization.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE, it).result().orElse(null) } ?: Component.literal("INVALIDNAME"),
                obj.get("color")?.asJsonArray?.read { it.asInt } ?: listOf(0x000000),
                GsonHelper.getAsInt(obj, "media_cost", 2),
                GsonHelper.getAsInt(obj,"color_duration",1000),
                obj.get("nodes")?.let{parseAny(it)} ?: ListPlacedNode(listOf())
            )
        }

        private fun parseAny(element: JsonElement): PlacedNode{
            return if(element.isJsonArray){
                val array=element.asJsonArray
                if(array.size()>0 && array[0].isJsonPrimitive && array[0].asString=="ANY"){
                    parseMulti(element.asJsonArray)
                }
                else parseList(array)
            }
            else parseUnique(element)
        }

        private fun parseList(list: JsonArray): PlacedNode{
            val nodes= list.read(::parseAny)
            return ListPlacedNode(nodes)
        }

        private fun parseMulti(element: JsonArray): PlacedNode{
            element.remove(0)
            val ret= element.read(::parseAny)
            return MultiPlacedNode(ret)
        }

        private fun parseUnique(element: JsonElement): UniquePlacedNode{
            val (id,obj)=when{
                element.isJsonPrimitive && element.asJsonPrimitive.isString
                    -> element.asString to JsonObject()
                element.isJsonObject
                    -> GsonHelper.getAsString(element.asJsonObject,"node") to element.asJsonObject
                else
                    -> throw JsonParseException("Middle node should be an object of a string")
            }
            val parser=HexlinkRegistry.EXTRACTOR_NODE_PARSER.get(ResourceLocation.parse(id))
                ?: throw JsonParseException("\"${element.asString}\" is not a valid extraction node type")
            return UniquePlacedNode(parser.parse(obj))
        }

    }
}