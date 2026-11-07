package jempasam.hexlink.item

import jempasam.hexlink.HexlinkRegistry
import jempasam.hexlink.item.functionnality.ItemSpiritSource
import jempasam.hexlink.item.functionnality.ItemSpiritTarget
import jempasam.hexlink.spirit.SpecialSpirit
import jempasam.hexlink.spirit.Spirit
import jempasam.hexlink.spirit.inout.SpiritSource
import jempasam.hexlink.spirit.inout.SpiritTarget
import jempasam.hexlink.utils.NbtHelper
import jempasam.hexlink.utils.nbt
import jempasam.hexlink.utils.readNbt
import jempasam.hexlink.utils.editNbt
import net.minecraft.world.item.Item.TooltipContext
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.Component
import net.minecraft.world.level.Level
import kotlin.math.min

class SingleSpiritContainerItem(settings: Item.Properties): Item(settings), ItemSpiritSource, ItemSpiritTarget {
    fun setSpirit(stack: ItemStack, spirit: Spirit?){
        if(spirit==null) stack.editNbt { it.remove("spirit") }
        else stack.editNbt { it.put("spirit", NbtHelper.writeSpirit(spirit)) }
    }

    fun getSpirit(stack: ItemStack): Spirit?{
        return stack.readNbt { it.getCompound("spirit") }?.let { NbtHelper.readSpirit(it) }
    }

    override fun getSpiritSource(stack: ItemStack): SpiritSource {
        return object:SpiritSource{
            override fun last(): Spirit? = getSpirit(stack)
            override fun all(): Set<Spirit> = last()?.let { setOf(it) } ?: emptySet()
            override fun extract(count: Int, spirit: Spirit): SpiritSource.SpiritOutputFlux {
                return SpiritSource.SpiritOutputFlux(min(count,stack.count)) { stack.count -= it }
            }
        }
    }

    override fun getSpiritTarget(stack: ItemStack): SpiritTarget {
        return object:SpiritTarget{
            override fun fill(count: Int, spirit: Spirit): SpiritTarget.SpiritInputFlux {
                if(getSpirit(stack)==null)return SpiritTarget.SpiritInputFlux({setSpirit(stack,spirit)},1)
                else return SpiritTarget.NONE.FLUX
            }
        }
    }

    override fun getName(stack: ItemStack): Component
            = getSpirit(stack) ?.getName() ?.let{ Component.translatable(getDescriptionId(stack), it)}
            ?: Component.translatable(getDescriptionId(stack)+".none")

    override fun appendHoverText(stack: ItemStack, context: TooltipContext, tooltip: MutableList<Component>, flag: TooltipFlag) {
        val spirit=getSpirit(stack) ?: return
        tooltip.add(spirit.getName().copy().setStyle(Style.EMPTY.withColor(spirit.getColor()).withItalic(true)))
    }

}