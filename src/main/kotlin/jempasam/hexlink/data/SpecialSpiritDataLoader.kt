package jempasam.hexlink.data

import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import jempasam.hexlink.HexlinkRegistry
import jempasam.hexlink.spirit.SpecialSpirit
import jempasam.hexlink.spirit.Spirit
import jempasam.hexlink.utils.NbtHelper
import jempasam.hexlink.utils.asNBT
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.GsonHelper

object SpecialSpiritDataLoader: JsonEntryDataLoader("special_spirits") {

    override fun before() = HexlinkRegistry.SPECIAL_SPIRIT.clear()

    override fun apply(id: ResourceLocation, obj: JsonObject) {
        fun getSpiritOpt(obj: JsonObject, name: String): Spirit? {
            return obj.get(name)
                ?.asNBT()
                ?.let { if(it is CompoundTag) NbtHelper.readSpirit(it) else null }
        }

        fun cast(spirit: Spirit?, name: String): Spirit {
            return spirit ?: throw JsonParseException("Missing or invalid \"$name\"")
        }
        val name=obj.get("name")
            ?.let { net.minecraft.network.chat.ComponentSerialization.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE, it).result().orElse(null) }
            ?: throw JsonParseException("Missing or invalid \"name\"")

        val color= GsonHelper.getAsInt(obj,"color")

        var manifestAt: Spirit?=null
        var manifestIn: Spirit?=null
        var lookAt: Spirit?=null
        var lookIn: Spirit?=null

        getSpiritOpt(obj,"all")?.also {
            manifestAt=it
            manifestIn=it
            lookAt=it
            lookIn=it
        }

        getSpiritOpt(obj,"manifest")?.also {
            manifestAt=it
            manifestIn=it
        }

        getSpiritOpt(obj,"look")?.also {
            lookAt=it
            lookIn=it
        }

        getSpiritOpt(obj,"at")?.also {
            manifestAt=it
            lookAt=it
        }

        getSpiritOpt(obj,"in")?.also {
            manifestIn=it
            lookIn=it
        }

        getSpiritOpt(obj,"manifestAt")?.also { manifestAt=it }
        getSpiritOpt(obj,"manifestIn")?.also { manifestIn=it }
        getSpiritOpt(obj,"lookAt")?.also { lookAt=it }
        getSpiritOpt(obj,"lookIn")?.also { lookIn=it }

        val type= SpecialSpirit.SpecialType(
            cast(manifestAt,"manifestAt"),
            cast(manifestIn,"manifestIn"),
            cast(lookAt,"lookAt"),
            cast(lookIn,"lookIn"),
            name, color
        )

        HexlinkRegistry.SPECIAL_SPIRIT.register(id,type)
    }

    override fun after() = HexlinkRegistry.SPECIAL_SPIRIT.lock()

}