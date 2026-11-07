package jempasam.hexlink.item

import jempasam.hexlink.item.functionnality.ItemScrollable
import jempasam.hexlink.item.functionnality.ItemSpiritSource
import jempasam.hexlink.item.functionnality.ItemSpiritTarget
import jempasam.hexlink.spirit.Spirit
import jempasam.hexlink.spirit.inout.SpiritSource
import jempasam.hexlink.spirit.inout.SpiritTarget
import jempasam.hexlink.utils.NbtAdapterList
import jempasam.hexlink.utils.NbtHelper
import jempasam.hexlink.utils.editNbt
import jempasam.hexlink.utils.readNbt
import net.minecraft.world.item.Item.TooltipContext
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.nbt.ListTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.util.RandomSource
import net.minecraft.world.level.Level
import kotlin.math.min

open class InfiniteSpiritItem(settings: Item.Properties): Item(settings), ItemSpiritSource, ItemSpiritTarget, ItemScrollable {

    /** The stack's spirit list. Mutations must be flushed with [editSpirits] to persist. */
    fun getSpirits(stack: ItemStack): SpiritList
        = SpiritList(stack.readNbt { it.getList("spirits", Tag.TAG_COMPOUND.toInt()) } ?: ListTag())

    /** Run [block] over the stack's spirit list, creating and storing it when absent. */
    fun editSpirits(stack: ItemStack, block: (SpiritList) -> Unit) {
        stack.editNbt { tag ->
            val list = tag.getList("spirits", Tag.TAG_COMPOUND.toInt())
            block(SpiritList(list))
            tag.put("spirits", list)
        }
    }

    override fun getSpiritSource(stack: ItemStack): SpiritSource {
        return object:SpiritSource{
            override fun last(): Spirit? = getSpirits(stack).run { if(size>0) this[0] else null }
            override fun all(): Set<Spirit> = getSpirits(stack).run { toSet() }
            override fun extract(count: Int, spirit: Spirit): SpiritSource.SpiritOutputFlux {
                val fcount= if(stack.isDamageableItem) min(count, stack.maxDamage-stack.damageValue) else count
                if(fcount>0 && getSpirits(stack).contains(spirit))return SpiritSource.SpiritOutputFlux(fcount) { used ->
                    if(stack.isDamageableItem) stack.setDamageValue(min(stack.maxDamage, stack.damageValue + used))
                }
                else return SpiritSource.NONE.FLUX
            }
        }
    }

    override fun getSpiritTarget(stack: ItemStack): SpiritTarget {
        return object:SpiritTarget{
            override fun fill(count: Int, spirit: Spirit): SpiritTarget.SpiritInputFlux {
                if(getSpirits(stack).size>0)
                    return SpiritTarget.SpiritInputFlux({ editSpirits(stack) { it.add(spirit) } }, 1)
                else
                    return SpiritTarget.SpiritInputFlux({}, 0)
            }
        }
    }


    override fun getName(stack: ItemStack): Component{
        return getSpirits(stack).first?.getName()?.let{ Component.translatable(getDescriptionId(stack), it)}
                ?: Component.translatable(getDescriptionId(stack)+".none")
    }

    override fun appendHoverText(stack: ItemStack, context: TooltipContext, tooltip: MutableList<Component>, flag: TooltipFlag) {
        for(spirit in getSpirits(stack)){
            tooltip.add(spirit.getName().copy().setStyle(Style.EMPTY.withColor(spirit.getColor()).withItalic(true)))
        }

    }




    class SpiritList(nbt: ListTag): NbtAdapterList<Spirit>(nbt){
        override fun from(e: Tag): Spirit? = if(e is CompoundTag) NbtHelper.readSpirit(e) else null
        override fun to(e: Spirit): Tag = NbtHelper.writeSpirit(e)
    }

    override fun roll(stack: ItemStack, player: ServerPlayer, hand: InteractionHand, delta: Double) {
        if(getSpirits(stack).size<=1) return
        editSpirits(stack) { spirits ->
            if(delta>0){
                val top=spirits[0]
                if(top!=null)spirits.add(top)
                spirits.remove(0)
            }
            else{
                val top=spirits[spirits.size-1]
                if(top!=null)spirits.add(0, top)
                spirits.remove(spirits.size-1)
            }
        }
    }
}