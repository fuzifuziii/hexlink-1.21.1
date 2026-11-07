package jempasam.hexlink.loot.function

import jempasam.hexlink.HexlinkMod
import net.minecraft.core.Registry
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType

object HexlinkLootFunctions {

    private val FUNCTIONS = mutableMapOf<ResourceLocation, LootItemFunctionType<*>>()

    private fun register(id: String, lootFunction: LootItemFunctionType<*>) {
        FUNCTIONS[ResourceLocation.fromNamespaceAndPath(HexlinkMod.MODID, id)] = lootFunction
    }

    init {
        register("spirit", SpiritTargetLootFunction.Type)
        register("easy_nbt", EasyNBTLootFunction.Type)
    }

    /** Called from the NeoForge `RegisterEvent` for [Registries.LOOT_FUNCTION_TYPE]. */
    fun registerAll(registry: Registry<LootItemFunctionType<*>>) {
        for ((id, type) in FUNCTIONS) {
            Registry.register(registry, id, type)
        }
    }
}
