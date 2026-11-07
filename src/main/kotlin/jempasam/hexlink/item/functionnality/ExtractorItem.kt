package jempasam.hexlink.item.functionnality

import jempasam.hexlink.HexlinkRegistry
import jempasam.hexlink.spirit.Spirit
import jempasam.hexlink.spirit.extractor.NodeExtractor
import jempasam.hexlink.spirit.extractor.SpiritExtractor
import jempasam.hexlink.utils.editNbt
import jempasam.hexlink.utils.readNbt
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.server.level.ServerPlayer
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor
import net.minecraft.resources.ResourceLocation
import net.minecraft.core.NonNullList

interface ExtractorItem {

    fun extractFrom(stack: ItemStack, caster: ServerPlayer?, target: Entity): SpiritExtractor.ExtractionResult<*>
        = getExtractor(stack)?.extract(caster, target) ?: SpiritExtractor.noResult<Spirit>()

    fun setExtractor(stack: ItemStack, extractor: NodeExtractor){
        val id= HexlinkRegistry.EXTRACTOR.getId(extractor)
        stack.editNbt { it.putString("extractor", id?.toString() ?: "none") }
    }

    fun getExtractor(stack: ItemStack): NodeExtractor?{
        val extractor=stack.readNbt { it.getString("extractor") } ?: ""
        if(extractor.isEmpty())return null
        else return HexlinkRegistry.EXTRACTOR.get(ResourceLocation.parse(extractor))
    }

    fun appendStacks(item :Item, stacks: NonNullList<ItemStack>) {
        for(extractor in HexlinkRegistry.EXTRACTOR.entrySet){
            val stack=item.defaultInstance
            setExtractor(stack, extractor.value)
            stacks.add(stack)
        }
    }

    fun getExtractorName(stack: ItemStack): Component
        = getExtractor(stack)?.let { Component.translatable(stack.getDescriptionId(), it.getName())  }
                ?: Component.translatable(stack.getDescriptionId()+".none")

    fun appendExtractorTooltip(pStack: ItemStack, pTooltipComponents: MutableList<Component>)
        = getExtractor(pStack)?.apply { pTooltipComponents.add(getName().copy().setStyle(extractorStyle)) }

    companion object{
        val extractorStyle= Style.EMPTY.withItalic(true).withColor(DyeColor.MAGENTA.getTextColor())
    }
}