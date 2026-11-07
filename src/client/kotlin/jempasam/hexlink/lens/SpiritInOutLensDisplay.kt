package jempasam.hexlink.lens

import at.petrak.hexcasting.api.client.ScryingLensOverlayRegistry.OverlayBuilder
import com.mojang.datafixers.util.Pair
import jempasam.hexlink.block.functionnality.BlockSpiritSource
import jempasam.hexlink.block.functionnality.BlockSpiritTarget
import jempasam.hexlink.item.HexlinkItems
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.network.chat.Component
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level

class SpiritInOutLensDisplay : OverlayBuilder {
    override fun addLines(lines: MutableList<Pair<ItemStack, Component>>, state: BlockState, pos: BlockPos, observer: Player, world: Level, hitFace: Direction) {
        val block=state.block
        if(block is BlockSpiritTarget){
            lines.add(Pair.of(HexlinkItems.SmallBag.defaultInstance,Component.translatable("hexlink.spirit_target")))
        }
        if(block is BlockSpiritSource){
            lines.add(Pair.of(HexlinkItems.SmallBag.defaultInstance,Component.translatable("hexlink.spirit_source")))
        }
    }
}