package jempasam.hexlink.spirit

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.decoration.ItemFrame
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.HopperBlockEntity
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3

object StackHelper {


    class WorldStack(val stack: ItemStack, val killer: ()->Unit, val update: ()->Unit, val replace: (ItemStack)->Unit)

    // Stack from entity
    fun stack(caster: Player, predicate: (ItemStack)->Boolean): WorldStack?{
        val inventory=caster.inventory
        if(predicate(caster.mainHandItem)) return WorldStack(
                caster.mainHandItem,
                {caster.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY)},
                {caster.setItemInHand(InteractionHand.MAIN_HAND,caster.mainHandItem)},
                {caster.setItemInHand(InteractionHand.MAIN_HAND,it)}
        )

        if(predicate(caster.offhandItem)) return WorldStack(
                caster.offhandItem,
                {caster.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY)},
                {caster.setItemInHand(InteractionHand.OFF_HAND,caster.offhandItem)},
                {caster.setItemInHand(InteractionHand.OFF_HAND,it)}
        )

        for(i in 0 ..< inventory.containerSize){
            val stack= inventory.getItem(i)
            if(predicate(stack))return WorldStack(
                    stack,
                    {inventory.setItem(i, ItemStack.EMPTY)},
                    {inventory.setItem(i, inventory.getItem(i))},
                    {inventory.setItem(i, it)}
            )
        }
        return null
    }

    fun stack(caster: Player?, target: Entity): WorldStack?{
        return when(target){
            is ItemEntity
                -> WorldStack(target.item, {target.kill()}, {target.item=target.item.copy()}, {target.item=it})
            is ItemFrame
                -> WorldStack(target.item, {target.item=ItemStack.EMPTY}, {target.item=target.item}, {target.item=it})
            is Player
                -> WorldStack(target.offhandItem, {target.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY)}, {target.setItemInHand(InteractionHand.OFF_HAND,target.offhandItem)}, {target.setItemInHand(InteractionHand.OFF_HAND, it)})
            else -> null
        }
    }

    fun stackOrThrow(caster: Player?, target: Entity): WorldStack
        = stack(caster,target) ?: throw Error("Checked not null, but finally is null")



    // Stack from block pos
    fun stack(caster: Player?, world: ServerLevel, pos: Vec3): WorldStack?{
        val bpos=BlockPos.containing(pos)
        val blockInv=HopperBlockEntity.getContainerAt(world,bpos)
        if(blockInv!=null){
            for (i in 0 until blockInv.containerSize) {
                if (blockInv.getItem(i).isEmpty) continue
                // Work on a copy and write that copy back, otherwise changes are lost.
                val stack = blockInv.getItem(i).copy()
                return WorldStack(
                    stack,
                    {blockInv.setItem(i, ItemStack.EMPTY); blockInv.setChanged()},
                    {blockInv.setItem(i, stack); blockInv.setChanged()},
                    {blockInv.setItem(i, it); blockInv.setChanged()}
                )
            }
        }
        else{
            val entities=world.getEntities(null as Entity?, AABB(bpos)) { true }
            for(entity in entities){
                val ret=stack(caster, entity)
                if(ret!=null)return ret
            }
        }
        return null
    }

    fun stackOrThrow(caster: Player?, world: ServerLevel, pos: Vec3): WorldStack
            = stack(caster,world,pos) ?: throw Error("Checked not null, but finally is null")

}
