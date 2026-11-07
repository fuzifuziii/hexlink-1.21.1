package jempasam.hexlink.data

import com.google.gson.JsonObject
import jempasam.hexlink.HexlinkRegistry
import jempasam.hexlink.spirit.extractor.NodeExtractor
import net.minecraft.resources.ResourceLocation

object SpecialExtractorDataLoader: JsonEntryDataLoader("extractors") {

    override fun before() = HexlinkRegistry.EXTRACTOR.clear()

    override fun apply(id: ResourceLocation, obj: JsonObject) {
        val extractor=NodeExtractor.parse(obj)
        HexlinkRegistry.EXTRACTOR.register(id, extractor)
    }

    override fun after() = HexlinkRegistry.EXTRACTOR.lock()
}