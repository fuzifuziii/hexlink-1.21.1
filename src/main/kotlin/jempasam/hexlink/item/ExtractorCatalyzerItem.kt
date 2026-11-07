package jempasam.hexlink.item

import jempasam.hexlink.item.functionnality.ExtractorItem
import net.minecraft.world.item.Item.TooltipContext
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.network.chat.Component

class ExtractorCatalyzerItem(settings: Item.Properties) : Item(settings), ExtractorItem {
    override fun appendHoverText(stack: ItemStack, context: TooltipContext, tooltip: MutableList<Component>, flag: TooltipFlag) {
        appendExtractorTooltip(stack, tooltip)
    }

    override fun getName(stack: ItemStack): Component = getExtractorName(stack)


}