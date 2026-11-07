package jempasam.hexlink.item.color

import jempasam.hexlink.item.functionnality.ExtractorItem
import net.minecraft.client.color.item.ItemColor
import net.minecraft.world.item.ItemStack

class ExtractorItemColor : ItemColor {
    override fun getColor(stack: ItemStack, tintIndex: Int): Int {
        val item=stack.item
        if(item is ExtractorItem){
            if(tintIndex==0)return item.getExtractor(stack)?.getColor()?.let(::opaque) ?: -1
        }
        return -1
    }
}