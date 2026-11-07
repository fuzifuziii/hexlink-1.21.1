package jempasam.hexlink.utils

import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.iota.NullIota
import net.minecraft.nbt.ByteTag
import net.minecraft.nbt.CollectionTag
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.DoubleTag
import net.minecraft.nbt.FloatTag
import net.minecraft.nbt.IntTag
import net.minecraft.nbt.LongTag
import net.minecraft.nbt.NumericTag
import net.minecraft.nbt.ShortTag
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag
import ram.talia.moreiotas.api.casting.iota.StringIota

object NbtToIotaHelper {
    fun CompoundTag.toIota(): ListIota {
        val ret = mutableListOf<Iota>()
        for (key in this.allKeys) {
            ret.add(ListIota(listOf(StringIota.makeUnchecked(key), this.get(key)?.toIota() ?: NullIota())))
        }
        return ListIota(ret)
    }

    fun CollectionTag<*>.toIota(): ListIota {
        val ret = mutableListOf<Iota>()
        for (e in this) ret.add(e.toIota())
        return ListIota(ret)
    }

    fun StringTag.toIota(): StringIota = StringIota.makeUnchecked(this.asString)

    fun NumericTag.toIota(): DoubleIota = DoubleIota(this.asDouble)

    fun Tag.toIota(): Iota {
        return when (this) {
            is CompoundTag -> this.toIota()
            is CollectionTag<*> -> this.toIota()
            is StringTag -> this.toIota()
            is NumericTag -> this.toIota()
            else -> NullIota()
        }
    }
}
