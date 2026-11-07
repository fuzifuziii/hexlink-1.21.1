package jempasam.hexlink.utils

import at.petrak.hexcasting.api.utils.asInt
import net.minecraft.nbt.IntTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag

/**
 * A [Collection] view over a [ListTag], converting each element through [from] / [to].
 *
 * Note that in 1.21 an [net.minecraft.world.item.ItemStack]'s custom data is a data component,
 * so callers must read the tag, mutate it and write it back (see [editNbt]). A view built from
 * a copy will mutate the copy only.
 */
abstract class NbtAdapterList<T>(val nbt: ListTag) : Collection<T> {
    protected abstract fun from(e: Tag): T?
    protected abstract fun to(e: T): Tag

    fun add(e: T) = nbt.add(to(e))

    fun add(index: Int, e: T) = nbt.add(index, to(e))
    operator fun set(i: Int, e: T) = nbt.set(i, to(e))
    fun remove(i: Int) = nbt.removeAt(i)

    operator fun get(i: Int): T? {
        val obj = from(nbt.get(i))
        if (obj == null) {
            nbt.removeAt(i)
            return null
        }
        return obj
    }

    val first: T? get() = if (isNotEmpty()) get(0) else null
    override fun isEmpty(): Boolean = nbt.isEmpty()
    override val size: Int get() = nbt.size
    override fun contains(element: T): Boolean = nbt.contains(to(element))
    override fun containsAll(elements: Collection<T>): Boolean = nbt.containsAll(elements.map { to(it) })
    override fun iterator(): Iterator<T> = nbt.asSequence().mapNotNull { from(it) }.iterator()
}

class IntListNbtAdapter(list: ListTag) : NbtAdapterList<Int>(list) {
    override fun from(e: Tag): Int = e.asInt
    override fun to(e: Int): Tag = IntTag.valueOf(e)
}
