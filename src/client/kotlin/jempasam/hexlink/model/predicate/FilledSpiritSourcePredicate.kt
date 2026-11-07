package jempasam.hexlink.model.predicate

import jempasam.hexlink.item.functionnality.ItemSpiritSource
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack

object FilledSpiritSourcePredicate : ClampedItemPropertyFunction {
    override fun unclampedCall(stack: ItemStack, world: ClientLevel?, entity: LivingEntity?, seed: Int): Float {
        val item=stack.item
        return if(item is ItemSpiritSource && item.getSpiritSource(stack).last()!=null) 1.0f else 0.0f
    }
}