package jempasam.hexlink.data

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import jempasam.hexlink.HexlinkRegistry
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.profiling.ProfilerFiller

class HexlinkSpiritDataLoader : SimpleJsonResourceReloadListener(Gson(),"hexlink_spirits") {
    override fun apply(prepared: Map<ResourceLocation, JsonElement>, manager: ResourceManager, profiler: ProfilerFiller) {
        for(entry in prepared){
            val spirit=HexlinkRegistry.SPIRIT.get(entry.key)
            val obj=entry.value
            if(spirit!=null && obj is JsonObject){
                HexlinkConfiguration.spirit_settings.put(spirit,HexlinkConfiguration.SpiritSettings(
                        obj.get("use_soul")?.asBoolean ?: true,
                        obj.get("media_cost")?.asInt ?: 5
                ))
            }
        }
    }
}