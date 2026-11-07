package jempasam.hexlink.item.color

import net.minecraft.client.color.item.ItemColor
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack

/**
 * Tints an item with its `minecraft:dyed_color` component.
 *
 * 1.21 turned dyeing into a data component on the stack, so the item itself no longer
 * carries the colour; an undyed stack yields -1.
 */
class DyeableColor(val index: Int=1) : ItemColor {
    override fun getColor(stack: ItemStack, tintIndex: Int): Int {
        if(tintIndex!=index)return -1
        return stack.get(DataComponents.DYED_COLOR)?.rgb()?.let(::opaque) ?: -1
    }
}
