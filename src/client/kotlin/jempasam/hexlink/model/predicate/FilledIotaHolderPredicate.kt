package jempasam.hexlink.model.predicate

import at.petrak.hexcasting.api.item.IotaHolderItem
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack

object FilledIotaHolderPredicate : ClampedItemPropertyFunction {
    override fun unclampedCall(stack: ItemStack, world: ClientLevel?, entity: LivingEntity?, seed: Int): Float {
        val item=stack.item
        return if(item is IotaHolderItem && item.readIota(stack)!=null) 1.0f else 0.0f
    }
}