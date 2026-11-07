package jempasam.hexlink.item.functionnality

import jempasam.hexlink.spirit.inout.SpiritSource
import net.minecraft.world.item.ItemStack

interface ItemSpiritSource {
    fun getSpiritSource(stack: ItemStack): SpiritSource
}