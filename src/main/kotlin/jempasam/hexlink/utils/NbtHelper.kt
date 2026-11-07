package jempasam.hexlink.utils

import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.Dynamic
import jempasam.hexlink.HexlinkRegistry
import jempasam.hexlink.spirit.Spirit
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.StringTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.GsonHelper
import net.minecraft.core.Registry

object NbtHelper {
    fun readSpirit(nbt: CompoundTag): Spirit?{
        val typeId=nbt.getString("type")
        if(typeId.isEmpty())return null
        val type= HexlinkRegistry.SPIRIT.get(ResourceLocation.parse(typeId)) ?: return null

        val value_nbt= nbt.get("value") ?: return null
        return type.deserialize(value_nbt)
    }

    fun readSpirit(obj: JsonObject): Spirit{
        val typeId=GsonHelper.getAsString(obj, "type")

        val type= HexlinkRegistry.SPIRIT.get(ResourceLocation.parse(typeId))
            ?: throw JsonParseException("Invalid spirit type \"$typeId\"")

        val value_nbt= obj.get("value")
            ?: throw JsonParseException("Missing spirit value")

        return type.deserialize(value_nbt.asNBT())
            ?: throw JsonParseException("Invalid spirit value for \"$typeId\"")
    }

    fun writeSpirit(spirit: Spirit): CompoundTag{
        val compound=CompoundTag()
        compound.putString("type", HexlinkRegistry.SPIRIT.getId(spirit.getType()).toString())
        compound.put("value", spirit.serialize())
        return compound
    }

    fun <T>readRegistry(reg: Registry<T>, nbt: Tag): T?{
        val id = (nbt as? StringTag)?.let { ResourceLocation.tryParse(it.asString) } ?: return null
        return reg.get(id)
    }

    fun <T>writeRegistry(reg: Registry<T>, value: T?): Tag
            = value
                    ?.let { reg.getId(value) }
                    ?.let { StringTag.valueOf(it.toString()) }
                    ?: StringTag.valueOf("")

    val ELEMENT_CODEC= Codec.PASSTHROUGH.comapFlatMap(
        { dynamic: Dynamic<*> ->
            val nbtElement = dynamic.convert(NbtOps.INSTANCE).value as Tag
            DataResult.success(nbtElement)
        },
        { nbt: Tag? ->
            Dynamic( NbtOps.INSTANCE, nbt )
        }
    )
}