package jempasam.hexlink.item.color

import jempasam.hexlink.item.functionnality.ItemSpiritSource
import net.minecraft.client.color.item.ItemColor
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.DyeColor

class SpiritSourceColor(val index: Int=1) : ItemColor {
    override fun getColor(stack: ItemStack, tintIndex: Int): Int {
        val item=stack.item
        if(item is ItemSpiritSource){
            if(tintIndex==index)return opaque(item.getSpiritSource(stack).last()?.getColor() ?: DyeColor.GRAY.getTextColor())
        }
        return -1
    }
}