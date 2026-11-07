package jempasam.hexlink.item

import at.petrak.hexcasting.api.utils.getOrCreateList
import at.petrak.hexcasting.api.utils.putCompound
import jempasam.hexlink.utils.editNbt
import jempasam.hexlink.utils.readNbt
import jempasam.hexlink.item.functionnality.ItemScrollable
import jempasam.hexlink.item.functionnality.ItemSpiritSource
import jempasam.hexlink.item.functionnality.ItemSpiritTarget
import jempasam.hexlink.spirit.Spirit
import jempasam.hexlink.spirit.inout.SpiritSource
import jempasam.hexlink.spirit.inout.SpiritTarget
import jempasam.hexlink.utils.NbtHelper
import net.minecraft.world.item.Item.TooltipContext
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.nbt.ListTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.InteractionHand
import net.minecraft.world.level.Level
import kotlin.math.min

class SoulContainerItem(settings: Item.Properties, val maxBoxCount: Int, val max_soul_count: Int): Item(settings), ItemSpiritSource, ItemSpiritTarget, ItemScrollable {

    /**
     * The stack's soul list, or null when it has none.
     *
     * The list is read out of the stack's custom data, so mutations must be flushed back with
     * [editSouls] — otherwise they only touch a detached copy.
     */
    fun souls(stack: ItemStack): Souls?
        = stack.readNbt { it.getList("souls", Tag.TAG_COMPOUND.toInt()) }?.let { Souls(it) }

    /** Run [block] over the stack's soul list, creating and storing it when absent. */
    fun editSouls(stack: ItemStack, block: (Souls) -> Unit) {
        stack.editNbt { tag ->
            val list = tag.getList("souls", Tag.TAG_COMPOUND.toInt())
            block(Souls(list))
            tag.put("souls", list)
        }
    }

    fun soulsOrCreate(stack: ItemStack): Souls
        = Souls(stack.readNbt { it.getOrCreateList("souls", Tag.TAG_COMPOUND.toInt()) } ?: ListTag())

    class Souls(val nbt: ListTag): Sequence<SoulStack>{

        fun remove(stack: SoulStack) = nbt.remove(stack.nbt)
        fun remove(index: Int) = nbt.removeAt(index)

        val size: Int get()=nbt.size

        fun get(index: Int): SoulStack = SoulStack(nbt.getCompound(index))

        fun get(tofind: Spirit): SoulStack?{
            val key=NbtHelper.writeSpirit(tofind)
            val removed= mutableListOf<SoulStack>()
            for(entry in this){
                val spiritNbt=entry.spirit_nbt
                if(!spiritNbt.isEmpty){
                    if(entry.count==0) removed.add(entry)
                    else if(entry.spirit_nbt==key) return entry
                }
                else removed.add(entry)
            }
            removed.forEach{ remove(it) }
            return null
        }

        fun getOrCreate(spirit: Spirit): SoulStack{
            val entry=get(spirit)
            if(entry==null){
                val newEntry=CompoundTag()
                newEntry.put("spirit",NbtHelper.writeSpirit(spirit))
                newEntry.putInt("count",0)
                nbt.add(newEntry)
                return SoulStack(newEntry)
            }
            else return entry
        }

        fun add(spirit: Spirit, count: Int){
            val compound=CompoundTag()
            compound.putInt("count", count)
            compound.putCompound("spirit", NbtHelper.writeSpirit(spirit))
            nbt.add(compound)
        }

        fun add(index: Int, spirit: Spirit, count: Int){
            val compound=CompoundTag()
            compound.putInt("count", count)
            compound.putCompound("spirit", NbtHelper.writeSpirit(spirit))
            nbt.add(index,compound)
        }

        override fun iterator(): Iterator<SoulStack> {
            return nbt.asSequence().mapNotNull { it as? CompoundTag }.map { SoulStack(it) }.iterator()
        }

    }

    class SoulStack(val nbt: CompoundTag){
        var count: Int
            get()= nbt.getInt("count")
            set(value)= nbt.putInt("count", value)

        var spirit: Spirit?
            get()= NbtHelper.readSpirit(nbt.getCompound("spirit"))
            set(value){
                if(value==null)nbt.remove("spirit")
                else nbt.putCompound("spirit", NbtHelper.writeSpirit(value))
            }

        val spirit_nbt: CompoundTag get() =  nbt.getCompound("spirit")
    }

    fun last(stack: ItemStack): Spirit?
        = souls(stack)?.let { if(it.size>0) it.get(it.size-1) else null }?.spirit

    override fun isBarVisible(stack: ItemStack): Boolean
        = souls(stack)?.size?.let { it!=maxBoxCount && it!=0 } ?: false

    override fun getBarWidth(stack: ItemStack): Int {
        val souls=souls(stack)
        return (13.0f*(souls?.size ?: 0)/maxBoxCount).toInt()
    }

    override fun getBarColor(stack: ItemStack): Int {
        return last(stack)?.getColor() ?: DyeColor.MAGENTA.getFireworkColor()
    }

    override fun getName(stack: ItemStack): Component
        = last(stack) ?.getName() ?.let{Component.translatable(getDescriptionId(stack), it)}
            ?: Component.translatable(getDescriptionId(stack)+".none")

    override fun appendHoverText(stack: ItemStack, context: TooltipContext, tooltip: MutableList<Component>, flag: TooltipFlag) {
        val souls=souls(stack) ?: return
        for(i in 0..<souls.size){
            val spiritstack=souls.get(i)
            val spirit=spiritstack.spirit ?: continue
            if(spiritstack.count>0){
                tooltip.add(
                        spirit.getName().copy()
                                .append(Component.literal(" ("))
                                .append(Component.literal(spiritstack.count.toString()))
                                .append(Component.literal(")"))
                )
            }
        }
    }

    override fun getSpiritSource(stack: ItemStack): SpiritSource {
        return object: SpiritSource{
            override fun extract(count: Int, spirit: Spirit): SpiritSource.SpiritOutputFlux {
                val souls=souls(stack) ?: return SpiritSource.NONE.FLUX
                val entry=souls.get(spirit) ?: return SpiritSource.NONE.FLUX
                val final_count=min(entry.count, count)
                return SpiritSource.SpiritOutputFlux(
                    final_count
                ) {
                    editSouls(stack) { live ->
                        val liveEntry = live.get(spirit) ?: return@editSouls
                        val newcount = liveEntry.count - it
                        if (newcount <= 0) live.remove(liveEntry)
                        else liveEntry.count = newcount
                    }
                }
            }

            override fun last(): Spirit? = last(stack)

            override fun all(): Set<Spirit> = souls(stack)?.map { it.spirit }?.filterNotNull()?.toSet() ?: setOf()
        }
    }

    override fun getSpiritTarget(stack: ItemStack): SpiritTarget {
        return object: SpiritTarget{
            override fun fill(count: Int, spirit: Spirit): SpiritTarget.SpiritInputFlux {
                val souls=soulsOrCreate(stack)
                if( souls.get(spirit)!=null || souls.size<maxBoxCount ){
                    val optEntry=souls.get(spirit)
                    val ecount=optEntry?.count ?: 0

                    val newValue= min(ecount+count, max_soul_count)
                    val offset=newValue-ecount
                    if(offset<=0)return SpiritTarget.NONE.FLUX

                    return SpiritTarget.SpiritInputFlux(
                            {
                                editSouls(stack) { live ->
                                    val entry=live.getOrCreate(spirit)
                                    entry.count+=min(it,offset)
                                }
                            },
                            offset
                    )
                }
                return SpiritTarget.NONE.FLUX
            }
        }
    }

    override fun roll(stack: ItemStack, player: ServerPlayer, hand: InteractionHand, delta: Double) {
        val current=souls(stack)
        if(current==null || current.size<=1) return
        editSouls(stack) { souls ->
            if(delta>0){
                val top=souls.get(0)
                val spirit=top.spirit
                if(spirit!=null)souls.add(spirit,top.count)
                souls.remove(0)
            }
            else{
                val top=souls.get(souls.size-1)
                val spirit=top.spirit
                if(spirit!=null)souls.add(0,spirit,top.count)
                souls.remove(souls.size-1)
            }
        }
    }
}