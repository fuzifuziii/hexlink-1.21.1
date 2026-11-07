package jempasam.hexlink.utils

import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.CraftingInput
import net.minecraft.world.item.crafting.CraftingRecipe
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.item.crafting.ShapedRecipe
import net.minecraft.world.level.Level

object RecipeHelper {

    /**
     * Find every recipe of [type] whose ingredients match [input] in order.
     *
     * 1.21 exposes recipes through [net.minecraft.world.item.crafting.RecipeHolder] and
     * matches against a [CraftingInput] rather than a container.
     */
    fun <T : Recipe<CraftingInput>> findAllSimpleMatches(world: Level, type: RecipeType<T>, input: List<ItemStack>): List<T> {
        val recipeManager = world.server?.recipeManager ?: return listOf()
        val matchedRecipes = mutableListOf<T>()
        for (holder in recipeManager.getAllRecipesFor(type)) {
            val recipe = holder.value()
            val ingredients = recipe.ingredients
            var stackPlace = 0

            for (ingrePlace in input.indices) {
                if (ingrePlace >= ingredients.size) break
                val ingredient = ingredients[ingrePlace]
                if (ingredient.isEmpty) continue
                if (ingredient.test(input[stackPlace])) {
                    stackPlace++
                    if (stackPlace == input.size) {
                        matchedRecipes.add(recipe)
                        break
                    }
                } else break
            }
        }
        return matchedRecipes
    }

    /** Craft [input] with the best-matching recipe of [type], if any. */
    fun <T : Recipe<CraftingInput>> craft(world: Level, type: RecipeType<T>, input: List<ItemStack>): Pair<ItemStack, Int>? {
        // Sort matches, test with most items first
        var matches = findAllSimpleMatches(world, type, input).map {
            var ingredientCount = 0
            for (ing in it.ingredients) if (!ing.isEmpty) ingredientCount++
            it to ingredientCount
        }
        matches = matches.sortedByDescending { it.second }

        // Test matches
        for (match in matches) {
            val recipe = match.first
            val ingredients = recipe.ingredients
            val dimension = if (recipe is ShapedRecipe) recipe.width to recipe.height else ingredients.size to 1

            // Fill inventory
            val stacks = MutableList(dimension.first * dimension.second) { ItemStack.EMPTY }
            var listi = 0
            for (i in 0 until minOf(ingredients.size, stacks.size)) {
                val ingredient = ingredients[i]
                if (listi < input.size && !ingredient.isEmpty) {
                    stacks[i] = input[listi]
                    listi++
                }
            }
            val craft = CraftingInput.of(dimension.first, dimension.second, stacks)
            if (recipe.matches(craft, world)) {
                val registries = world.registryAccess()
                return recipe.assemble(craft, registries) to ingredients.size
            }
        }
        return null
    }
}
