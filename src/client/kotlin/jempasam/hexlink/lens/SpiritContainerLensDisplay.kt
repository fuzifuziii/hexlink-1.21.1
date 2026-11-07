package jempasam.hexlink.lens

import at.petrak.hexcasting.api.client.ScryingLensOverlayRegistry.OverlayBuilder
import com.mojang.datafixers.util.Pair
import jempasam.hexlink.block.functionnality.BlockSpiritContainer
import jempasam.hexlink.item.HexlinkItems
import jempasam.hexlink.spirit.Spirit
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.network.chat.Component
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level

class SpiritContainerLensDisplay : OverlayBuilder {
    override fun addLines(lines: MutableList<Pair<ItemStack, Component>>, state: BlockState, pos: BlockPos, observer: Player, world: Level, hitFace: Direction){
        val block=state.block
        if(block is BlockSpiritContainer){
            val slot_count=block.getSlotCount()
            for(i in 0..<slot_count){
                fillWith(lines,block.getSpiritContent(i,world,pos))
                if(i<slot_count-1)lines.add(Pair.of(ItemStack.EMPTY,Component.literal("---")))
            }
        }
    }

    fun fillWith(lines: MutableList<Pair<ItemStack, Component>>, spirits: Sequence<Spirit>){
        val addItemToLens={ spirit: Spirit, count: Int ->
            val icon= HexlinkItems.Spirit.defaultInstance
            HexlinkItems.Spirit.setSpirit(icon,spirit)
            lines.add(Pair.of(icon, Component.literal(count.toString()+" ").copy().append(spirit.getName())))
        }

        var count=0
        var last: Spirit?=null
        var actual_count=0

        for(spirit in spirits){
            if(count>=10)break
            if(last==null || last==spirit){
                actual_count++
                last=spirit
            }
            else{
                addItemToLens(last,actual_count)
                count++
                actual_count=1
                last=spirit
            }
        }
        if(count<10 && last!=null){
            addItemToLens(last,actual_count)
        }
        if(count==10)lines.add(Pair.of(ItemStack.EMPTY,Component.literal("...")))
    }
}