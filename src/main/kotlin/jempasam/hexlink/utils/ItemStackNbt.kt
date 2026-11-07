package jempasam.hexlink.utils

import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData

/**
 * 1.21 replaced per-item NBT with the `minecraft:custom_data` data component, so the old
 * `stack.nbt` / `stack.orCreateNbt` properties no longer exist. These helpers keep the
 * call sites readable.
 *
 * [nbt] returns a detached copy — mutating it does not affect the stack. Use [editNbt] for
 * writes; it runs [block] on the live tag and stores the result back into the component.
 */

/** The stack's custom data, or null when it carries none. The returned tag is a copy. */
val ItemStack.nbt: CompoundTag?
    get() = get(DataComponents.CUSTOM_DATA)?.copyTag()

/** True when the stack carries any custom data. */
val ItemStack.hasNbt: Boolean
    get() = has(DataComponents.CUSTOM_DATA)

/** Read a value out of the stack's custom data without copying the whole tag. */
fun <T> ItemStack.readNbt(block: (CompoundTag) -> T): T? =
    get(DataComponents.CUSTOM_DATA)?.copyTag()?.let(block)

/** Mutate the stack's custom data, creating the component when it is absent. */
fun ItemStack.editNbt(block: (CompoundTag) -> Unit) {
    CustomData.update(DataComponents.CUSTOM_DATA, this, block)
}

/** Replace the stack's custom data wholesale, or clear it when [tag] is null. */
fun ItemStack.setNbt(tag: CompoundTag?) {
    if (tag == null) remove(DataComponents.CUSTOM_DATA)
    else set(DataComponents.CUSTOM_DATA, CustomData.of(tag))
}
