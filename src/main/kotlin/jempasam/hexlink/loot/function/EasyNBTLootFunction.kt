package jempasam.hexlink.loot.function

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import jempasam.hexlink.utils.setNbt
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.functions.LootItemFunction
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType

class EasyNBTLootFunction(val nbt: CompoundTag) : LootItemFunction {
    override fun apply(t: ItemStack, u: LootContext): ItemStack {
        t.setNbt(nbt.copy())
        return t
    }

    override fun getType(): LootItemFunctionType<EasyNBTLootFunction> = Type

    companion object {
        val CODEC: MapCodec<EasyNBTLootFunction> = RecordCodecBuilder.mapCodec { inst ->
            inst.group(
                CompoundTag.CODEC.fieldOf("nbt").forGetter { it.nbt }
            ).apply(inst) { nbt -> EasyNBTLootFunction(nbt) }
        }

        val Type: LootItemFunctionType<EasyNBTLootFunction> = LootItemFunctionType(CODEC)
    }
}
