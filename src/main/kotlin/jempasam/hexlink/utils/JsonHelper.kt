package jempasam.hexlink.utils

import com.google.gson.*
import com.mojang.brigadier.StringReader
import jempasam.hexlink.spirit.Spirit
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.DoubleTag
import net.minecraft.nbt.IntTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.nbt.TagParser
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.GsonHelper
import net.minecraft.world.item.crafting.AbstractCookingRecipe
import net.minecraft.world.item.crafting.RecipeType
import java.math.BigInteger

fun JsonObject.asNBT(): CompoundTag {
    val ret = CompoundTag()
    for (e in this.entrySet()) {
        ret.put(e.key, e.value.asNBT())
    }
    return ret
}

fun JsonArray.asNBT(): ListTag {
    val ret = ListTag()
    for (e in this) {
        ret.add(e.asNBT())
    }
    return ret
}

fun JsonElement.asNBT(): Tag {
    if (this.isJsonObject) return (this as JsonObject).asNBT()
    if (this.isJsonArray) return (this as JsonArray).asNBT()
    val prim = this.asJsonPrimitive
    if (prim.isNumber) {
        when (val number = prim.asNumber) {
            is Byte, is Short, is Int, is Long, is BigInteger -> return IntTag.valueOf(number.toInt())
            is Float, is Double -> return DoubleTag.valueOf(number.toDouble())
        }
    }
    return TagParser(AcceptAllStringReader(this.asString)).readValue()
}

fun CompoundTag.asJSON(): JsonObject {
    val ret = JsonObject()
    for (key in this.allKeys) {
        this.get(key)?.let { ret.add(key, it.asJSON()) }
    }
    return ret
}

fun ListTag.asJSON(): JsonArray {
    val ret = JsonArray()
    for (e in this) {
        ret.add(e.asJSON())
    }
    return ret
}

fun Tag.asJSON(): JsonElement {
    return when (this) {
        is CompoundTag -> this.asJSON()
        is ListTag -> this.asJSON()
        is IntTag -> JsonPrimitive(this.asInt)
        is DoubleTag -> JsonPrimitive(this.asDouble)
        else -> JsonPrimitive(this.asString)
    }
}

fun JsonObject.getSpirit(name: String): Spirit {
    try {
        return NbtHelper.readSpirit(GsonHelper.getAsJsonObject(this, name))
    } catch (e: Exception) {
        throw JsonParseException("For \"$name\": ${e.message}")
    }
}

fun JsonObject.addSpirit(name: String, spirit: Spirit) {
    this.add(name, NbtHelper.writeSpirit(spirit).asJSON())
}

fun JsonElement.asSpirit(): Spirit {
    val obj = this.asJsonObject
    return NbtHelper.readSpirit(obj.asNBT())
        ?: throw JsonParseException("Invalid Spirit")
}

fun Spirit.toJSON(): JsonElement {
    return NbtHelper.writeSpirit(this).asJSON()
}

@Suppress("UNCHECKED_CAST")
fun JsonObject.getCookingRecipeType(name: String): RecipeType<AbstractCookingRecipe> {
    val recipe = BuiltInRegistries.RECIPE_TYPE.get(ResourceLocation.parse(GsonHelper.getAsString(this, name)))
    recipe ?: throw JsonParseException("No such recipe type of furnace")
    return recipe as RecipeType<AbstractCookingRecipe>
}

fun JsonObject.addCookingRecipeType(name: String, recipeType: RecipeType<AbstractCookingRecipe>) {
    val id = BuiltInRegistries.RECIPE_TYPE.getKey(recipeType)
    if (id != null) {
        this.addProperty(name, id.toString())
    }
}

class AcceptAllStringReader(reader: String) : StringReader(reader) {
    override fun readUnquotedString(): String {
        val start = cursor
        while (canRead()) {
            skip()
        }
        return string.substring(start, cursor)
    }
}

fun <T> jsonArray(list: List<T>, serializer: (T) -> JsonElement): JsonArray {
    val ret = JsonArray()
    for (element in list) {
        ret.add(serializer(element))
    }
    return ret
}

fun <T> JsonArray.read(deserializer: (JsonElement) -> T): List<T> {
    val ret = mutableListOf<T>()
    for (element in this) {
        ret.add(deserializer(element))
    }
    return ret
}
