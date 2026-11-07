package jempasam.hexlink.block.color

import jempasam.hexlink.spirit.inout.SpiritSource
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.client.color.block.BlockColor
import net.minecraft.core.BlockPos
import net.minecraft.world.level.BlockAndTintGetter

class SpiritContainerBlockColor : BlockColor {
    override fun getColor(state: BlockState, world: BlockAndTintGetter?, pos: BlockPos?, tintIndex: Int): Int {
        if(pos!=null && world!=null){
            val entity=world.getBlockEntity(pos)
            if(entity is SpiritSource){
                val last=entity.last()
                if(last!=null)return last.getColor()
            }
        }
        return 0xffffff
    }
}