package jempasam.hexlink.recipe

import com.mojang.serialization.MapCodec
import jempasam.hexlink.item.functionnality.ExtractorItem
import jempasam.hexlink.spirit.extractor.NodeExtractor
import net.minecraft.core.HolderLookup
import net.minecraft.core.NonNullList
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.CraftingBookCategory
import net.minecraft.world.item.crafting.CraftingInput
import net.minecraft.world.item.crafting.CraftingRecipe
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.ShapedRecipe
import net.minecraft.world.level.Level

/**
 * A shaped recipe that copies the extractor id from its inputs onto the crafted item.
 */
class ExtractorShapedRecipe(val recipe: ShapedRecipe) : CraftingRecipe {

    override fun assemble(input: CraftingInput, registries: HolderLookup.Provider): ItemStack {
        val result = recipe.assemble(input, registries)
        val resultItem = result.item
        if (resultItem is ExtractorItem) {
            var extractor: NodeExtractor? = null
            for (i in 0 until input.size()) {
                val item = input.getItem(i).item
                if (item is ExtractorItem) {
                    val ext = item.getExtractor(input.getItem(i))
                    if (ext != null && extractor == null) extractor = ext
                }
            }
            if (extractor != null) resultItem.setExtractor(result, extractor)
        }
        return result
    }

    override fun matches(input: CraftingInput, world: Level): Boolean = recipe.matches(input, world)

    override fun canCraftInDimensions(width: Int, height: Int): Boolean = recipe.canCraftInDimensions(width, height)

    override fun getResultItem(registries: HolderLookup.Provider): ItemStack = recipe.getResultItem(registries)

    override fun getIngredients(): NonNullList<Ingredient> = recipe.ingredients

    override fun getGroup(): String = recipe.group

    override fun category(): CraftingBookCategory = recipe.category()

    override fun getSerializer(): RecipeSerializer<*> = SERIALIZER

    object SERIALIZER : RecipeSerializer<ExtractorShapedRecipe> {
        private val SHAPED_SERIALIZER = ShapedRecipe.Serializer()

        override fun codec(): MapCodec<ExtractorShapedRecipe> =
            SHAPED_SERIALIZER.codec().xmap(::ExtractorShapedRecipe, ExtractorShapedRecipe::recipe)

        override fun streamCodec(): StreamCodec<RegistryFriendlyByteBuf, ExtractorShapedRecipe> =
            SHAPED_SERIALIZER.streamCodec().map(::ExtractorShapedRecipe, ExtractorShapedRecipe::recipe)
    }
}
