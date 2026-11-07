package jempasam.hexlink.item.functionnality

import net.minecraft.world.item.ItemStack
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand

interface ItemScrollable {
    fun roll(stack: ItemStack, player: ServerPlayer, hand: InteractionHand, delta: Double)
}