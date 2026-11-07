package jempasam.hexlink.recipe

import jempasam.hexlink.HexlinkMod
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeSerializer

object HexlinkRecipes {

    private val SERIALIZERS = mutableMapOf<ResourceLocation, RecipeSerializer<*>>()

    private fun <T : Recipe<*>> create(id: String, serializer: RecipeSerializer<T>): RecipeSerializer<T> {
        SERIALIZERS[ResourceLocation.fromNamespaceAndPath(HexlinkMod.MODID, id)] = serializer
        return serializer
    }

    /** Called from the NeoForge `RegisterEvent` for `Registries.RECIPE_SERIALIZER`. */
    fun registerRecipes(registry: Registry<RecipeSerializer<*>>) {
        for ((id, serializer) in SERIALIZERS) {
            Registry.register(registry, id, serializer)
        }
    }

    val SHAPED_EXTRACTOR = create("shaped_extractor", ExtractorShapedRecipe.SERIALIZER)
}
