package jempasam.hexlink.utils

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.Lifecycle
import net.minecraft.core.MappedRegistry
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation

/**
 * A registry-like lookup table that can be emptied and repopulated at runtime.
 *
 * In 1.21.1 [Registry] is an interface backed by NeoForge's registry machinery and entries
 * cannot be removed from it. Hexlink needs to rebuild its extractor / special-spirit
 * tables whenever the server syncs data to the client, so those tables live here instead.
 *
 * The backing [MappedRegistry] is exposed through [inner] for the places that still need a
 * genuine [Registry] (vanilla registration helpers, codecs).
 */
class LoadableRegistry<T>(val key: ResourceKey<Registry<T>>, private val lifecycle: Lifecycle) {

    var inner: MappedRegistry<T> = MappedRegistry(key, lifecycle)
        private set

    private var locked = false

    /** Drop every entry and allow registration again. */
    fun clear() {
        inner = MappedRegistry(key, lifecycle)
        locked = false
    }

    /** Freeze the table so no further entries can be added. */
    fun lock() {
        locked = true
    }

    val isLocked: Boolean get() = locked

    fun register(id: ResourceLocation, value: T) {
        if (locked) throw IllegalStateException("Registry ${key.location()} is locked")
        Registry.register(inner, id, value)
    }

    fun register(key: ResourceKey<T>, value: T) {
        if (locked) throw IllegalStateException("Registry ${key.location()} is locked")
        Registry.register(inner, key, value)
    }

    fun get(id: ResourceLocation): T? = inner.get(id)

    fun get(id: ResourceKey<T>): T? = inner.get(id)

    fun getId(value: T): ResourceLocation? = inner.getKey(value)

    fun getKey(value: T): ResourceLocation? = inner.getKey(value)

    fun size(): Int = inner.size()

    fun contains(id: ResourceLocation): Boolean = inner.containsKey(id)

    val entrySet: Set<Map.Entry<ResourceKey<T>, T>> get() = inner.entrySet()

    val keys: Set<ResourceKey<T>> get() = inner.registryKeySet()

    fun asSequence(): Sequence<T> = inner.stream().toList().asSequence()

    operator fun iterator(): Iterator<T> = inner.iterator()

    /** Codec that resolves entries by their registry name, mirroring [Registry.byNameCodec]. */
    val codec: Codec<T> = ResourceLocation.CODEC.flatXmap(
        { id -> get(id)?.let { DataResult.success(it) } ?: DataResult.error { "Unknown id: $id" } },
        { value ->
            getId(value)?.let { DataResult.success(it) }
                ?: DataResult.error { "Unregistered value: $value" }
        }
    )
}
