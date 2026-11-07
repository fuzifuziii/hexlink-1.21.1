package jempasam.hexlink.item.functionnality

import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.iota.ListIota
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.Item.TooltipContext
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level

interface SpellHolderItem {

    fun getSpellNbt(stack: ItemStack): ListTag?

    fun setSpellNbt(stack: ItemStack, list: ListTag)

    fun hasSpell(stack: ItemStack): Boolean = getSpellNbt(stack) != null

    fun getSpell(stack: ItemStack, level: ServerLevel): List<Iota>
        = getSpellNbt(stack)
            ?.mapNotNull { tag ->
                (tag as? CompoundTag)?.let {
                    IotaType.TYPED_CODEC.parse(NbtOps.INSTANCE, it).result().orElse(null)
                }
            }
            ?: listOf()

    fun setSpell(stack: ItemStack, spell: List<Iota>) {
        val list = ListTag()
        spell.forEach { iota ->
            IotaType.TYPED_CODEC.encodeStart(NbtOps.INSTANCE, iota).result().ifPresent { list.add(it) }
        }
        setSpellNbt(stack, list)
    }

    fun appendSpellTooltip(stack: ItemStack, world: Level?, tooltip: MutableList<Component>, context: TooltipContext) {
        val nbt = getSpellNbt(stack) ?: return
        val iotas = nbt.mapNotNull { tag ->
            (tag as? CompoundTag)?.let { IotaType.TYPED_CODEC.parse(NbtOps.INSTANCE, it).result().orElse(null) }
        }
        tooltip.add(ListIota(iotas).display())
    }
}
