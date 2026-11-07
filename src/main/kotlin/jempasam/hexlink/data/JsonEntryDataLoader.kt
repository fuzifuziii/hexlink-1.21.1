package jempasam.hexlink.data

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import jempasam.hexlink.HexlinkMod
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener
import net.minecraft.util.profiling.ProfilerFiller

/**
 * Base class for Hexlink's data-driven content (spirits, extractors).
 *
 * Each subclass reads one directory of JSON files under `data/<namespace>/<name>/`.
 */
abstract class JsonEntryDataLoader(private val name: String) : SimpleJsonResourceReloadListener(Gson(), name) {

    /** Stable id used to order this listener against others. */
    val loaderId: ResourceLocation = ResourceLocation.fromNamespaceAndPath(HexlinkMod.MODID, "${name}_loader")

    override fun apply(prepared: Map<ResourceLocation, JsonElement>, manager: ResourceManager, profiler: ProfilerFiller) {
        before()
        for (entry in prepared) {
            try {
                val jsonObject = entry.value.asJsonObject
                apply(entry.key, jsonObject)
            } catch (e: Exception) {
                HexlinkMod.logger.error("In $name \"${entry.key}\", ${e.message}")
            }
        }
        after()
    }

    protected abstract fun apply(id: ResourceLocation, obj: JsonObject)

    protected open fun after() {}

    protected open fun before() {}
}
