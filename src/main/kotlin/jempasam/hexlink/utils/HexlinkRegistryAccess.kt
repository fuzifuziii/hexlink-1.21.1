package jempasam.hexlink.utils

import net.minecraft.core.Registry
import net.minecraft.core.RegistryAccess
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.enchantment.Enchantment

/**
 * Cached handle on the current [RegistryAccess].
 *
 * Enchantments stopped being a static registry in 1.21 — they are datapack-driven and only
 * reachable through a [RegistryAccess] (the server's, or the client level's). Several spirits
 * need to resolve an enchantment id back to a value outside of any level context (tooltips,
 * NBT deserialization), so the most recent access is cached here.
 *
 * The cache is refreshed on server start and on client level load; see `HexlinkMod`.
 */
object HexlinkRegistryAccess {

    @Volatile
    private var cached: RegistryAccess? = null

    fun update(access: RegistryAccess?) {
        cached = access
    }

    val current: RegistryAccess? get() = cached

    /** The enchantment registry, or null when no registry access has been seen yet. */
    fun enchantments(): Registry<Enchantment>? =
        cached?.registry(Registries.ENCHANTMENT)?.orElse(null)

    /**
     * JSON ops that can resolve registry entries and tags (`#minecraft:wool`, holder sets).
     * Falls back to plain JSON ops when no registry access is known yet.
     */
    fun jsonOps(): com.mojang.serialization.DynamicOps<com.google.gson.JsonElement> =
        cached?.createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE)
            ?: com.mojang.serialization.JsonOps.INSTANCE

    /** The biome registry, or null when no registry access has been seen yet. */
    fun biomes(): Registry<net.minecraft.world.level.biome.Biome>? =
        cached?.registry(Registries.BIOME)?.orElse(null)
}
