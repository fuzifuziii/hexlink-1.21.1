package jempasam.hexlink.spirit

import jempasam.hexlink.HexlinkMod
import jempasam.hexlink.HexlinkRegistry
import jempasam.hexlink.spirit.extractor.NodeExtractor
import net.minecraft.resources.ResourceLocation


@Suppress("unused")
object HexlinkSpirits {
    private fun <T :Spirit>create(id: String, spirit: Spirit.SpiritType<T>): Spirit.SpiritType<T>{
        HexlinkRegistry.SPIRIT.register(ResourceLocation.fromNamespaceAndPath(HexlinkMod.MODID, id), spirit)
        return spirit
    }

    private fun <T: Spirit>create(id: String, spirit: NodeExtractor): NodeExtractor{
        HexlinkRegistry.EXTRACTOR.register(ResourceLocation.fromNamespaceAndPath(HexlinkMod.MODID, id), spirit)
        return spirit
    }

    val ITEM_SPIRIT= create("item", ItemSpirit.Type)
    val ENTITY_SPIRIT= create("entity", EntitySpirit.Type)
    val POTION_SPIRIT= create("potion", PotionSpirit.Type)
    val BLOCK_SPIRIT= create("block", BlockSpirit.Type)
    val ENCHANTMENT_SPIRIT= create("enchantment", EnchantmentSpirit.Type)
    val BIOME_SPIRIT= create("biome", BiomeSpirit.Type)
    val COLOR_SPIRIT= create("color", ColorSpirit.Type)
    val FUNCTION_SPIRIT= create("function", FunctionSpirit.Type)
    val SPECIAL_SPIRIT= create("special", SpecialSpirit.Type)
}