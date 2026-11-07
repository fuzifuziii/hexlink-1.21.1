package jempasam.hexlink.item.functionnality

import jempasam.hexlink.spirit.inout.SpiritTarget
import net.minecraft.world.item.ItemStack

interface ItemSpiritTarget{
    fun getSpiritTarget(stack: ItemStack): SpiritTarget
}