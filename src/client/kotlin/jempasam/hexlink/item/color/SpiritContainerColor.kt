package jempasam.hexlink.item.color

import jempasam.hexlink.item.SoulContainerItem
import net.minecraft.client.color.item.ItemColor
import net.minecraft.world.item.ItemStack

class SpiritContainerColor : ItemColor {
    override fun getColor(stack: ItemStack, tintIndex: Int): Int {
        val item=stack.item
        if(item is SoulContainerItem){
            if(tintIndex==0)return -1
            else if(tintIndex==1)return opaque(item.getBarColor(stack))
        }
        return -1
    }
}