package jempasam.hexlink.spirit.extractor.node

import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import jempasam.hexlink.spirit.PotionSpirit
import jempasam.hexlink.spirit.StackHelper
import net.minecraft.world.item.Items
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.alchemy.PotionContents
import kotlin.math.max

object PotionExtNode : ExtractionNode {

    private val POTION_ITEMS=setOf(Items.POTION, Items.LINGERING_POTION, Items.SPLASH_POTION)
    override fun filter(source: ExtractionNode.Source): ExtractionNode.Source {
        val worldStack=StackHelper.stack(source.caster,source.entity)
        worldStack ?: return source
        val stack=worldStack.stack

        if(!POTION_ITEMS.contains(stack.item)) return source

        val contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY)
        val effects = contents.allEffects.toList()
        if(effects.isEmpty())return source

        return source.with {
            count *= stack.count * max(1,effects[0].duration/1200) * (effects[0].amplifier+1)
            val prev=consumer
            consumer={
                prev(it)
                if (effects.size == 1) worldStack.killer()
                else {
                    val remaining = effects.drop(1)
                    val newContents = PotionContents(
                        contents.potion(),
                        contents.customColor(),
                        remaining
                    )
                    stack.set(DataComponents.POTION_CONTENTS, newContents)
                    worldStack.update()
                }
            }
            spirit= PotionSpirit(effects[0].effect)
        }
    }

    object Parser: ExtractionNode.Parser<PotionExtNode> {
        override fun parse(obj: JsonObject): PotionExtNode = PotionExtNode
    }

    val CODEC= Codec.unit(PotionExtNode)
}