package jempasam.hexlink.loot.function

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import jempasam.hexlink.HexlinkMod
import jempasam.hexlink.item.functionnality.ItemSpiritTarget
import jempasam.hexlink.utils.NbtHelper
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.functions.LootItemFunction
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType

/**
 * Fills a spirit container with [count] spirits.
 *
 * The spirit is kept as raw type + value and only decoded when the loot is rolled: loot tables
 * are parsed before Hexlink has a registry access, and some spirits (enchantments, biomes) can
 * only be resolved through one.
 */
class SpiritTargetLootFunction(val type: ResourceLocation, val value: Tag, val count: Int) : LootItemFunction {
    override fun apply(t: ItemStack, u: LootContext): ItemStack {
        val item = t.item
        if (item is ItemSpiritTarget) {
            val tag = CompoundTag().apply {
                putString("type", type.toString())
                put("value", value.copy())
            }
            val spirit = NbtHelper.readSpirit(tag)
            if (spirit == null) {
                HexlinkMod.logger.warn("Loot function hexlink:spirit could not resolve spirit $type / $value")
                return t
            }
            val flux = item.getSpiritTarget(t).fill(count, spirit)
            flux.fill(flux.maxcount)
        }
        return t
    }

    override fun getType(): LootItemFunctionType<SpiritTargetLootFunction> = Type

    companion object {
        private val SPIRIT_CODEC: MapCodec<Pair<ResourceLocation, Tag>> = RecordCodecBuilder.mapCodec { inst ->
            inst.group(
                ResourceLocation.CODEC.fieldOf("type").forGetter { it.first },
                NbtHelper.ELEMENT_CODEC.fieldOf("value").forGetter { it.second }
            ).apply(inst) { type, value -> type to value }
        }

        val CODEC: MapCodec<SpiritTargetLootFunction> = RecordCodecBuilder.mapCodec { inst ->
            inst.group(
                SPIRIT_CODEC.codec().fieldOf("spirit").forGetter { it.type to it.value },
                Codec.INT.optionalFieldOf("count", 1).forGetter { it.count }
            ).apply(inst) { spirit, count -> SpiritTargetLootFunction(spirit.first, spirit.second, count) }
        }

        val Type: LootItemFunctionType<SpiritTargetLootFunction> = LootItemFunctionType(CODEC)
    }
}
