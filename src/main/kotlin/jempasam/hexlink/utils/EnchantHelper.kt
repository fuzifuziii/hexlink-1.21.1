package jempasam.hexlink.utils

import net.minecraft.core.Holder
import net.minecraft.world.item.EnchantedBookItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.item.enchantment.ItemEnchantments

/**
 * Enchantment helpers.
 *
 * In 1.21 enchantments are data-driven and referenced through [Holder]s, and an item's
 * enchantments live in the `minecraft:enchantments` / `minecraft:stored_enchantments`
 * data components rather than in an NBT list. Everything here therefore works through
 * [EnchantmentHelper] instead of hand-rolling NBT.
 */
object EnchantHelper {

    fun copy(stack: ItemStack, isEnchanted: Boolean): ItemStack {
        return if (isEnchanted) {
            if (stack.`is`(Items.BOOK)) Items.ENCHANTED_BOOK.defaultInstance
            else stack.copy()
        } else {
            if (stack.`is`(Items.ENCHANTED_BOOK)) Items.BOOK.defaultInstance
            else stack.copy()
        }
    }

    /** The enchantments currently stored on [target], whether on a book or a normal item. */
    fun get(target: ItemStack): MutableMap<Holder<Enchantment>, Int> {
        val ret = mutableMapOf<Holder<Enchantment>, Int>()
        for (entry in EnchantmentHelper.getEnchantmentsForCrafting(target).entrySet()) {
            ret[entry.key] = entry.value
        }
        return ret
    }

    /** Replace [target]'s enchantments with [enchantments]. */
    fun put(target: ItemStack, enchantments: Map<Holder<Enchantment>, Int>) {
        if (enchantments.isEmpty()) {
            EnchantmentHelper.setEnchantments(target, ItemEnchantments.EMPTY)
            return
        }
        val mutable = ItemEnchantments.Mutable(ItemEnchantments.EMPTY)
        for ((enchantment, level) in enchantments) {
            if (level > 0) mutable.set(enchantment, level)
        }
        EnchantmentHelper.setEnchantments(target, mutable.toImmutable())
    }

    fun canEnchant(target: ItemStack, enchantment: Holder<Enchantment>, level: Int): Boolean {
        val def = enchantment.value()

        // Check acceptable
        if (!def.canEnchant(target)) return false

        val enchants = get(target)

        // Check incompatible
        if (enchants.keys.any { it != enchantment && !Enchantment.areCompatible(it, enchantment) }) return false

        if (level > def.maxLevel) return false

        // Already present with an equal or higher level
        val already = enchants[enchantment] ?: 0
        if (level <= already) return false

        return true
    }

    fun enchant(target: ItemStack, enchantment: Holder<Enchantment>, level: Int): ItemStack? {
        val enchants = get(target)
        enchants[enchantment] = maxOf(level, enchants[enchantment] ?: 0)
        val stack = copy(target, enchants.isNotEmpty())
        put(stack, enchants)
        return stack
    }

    fun removeEnchantment(target: ItemStack, enchantment: Holder<Enchantment>): ItemStack {
        val enchants = get(target)
        enchants.remove(enchantment)
        val stack = copy(target, enchants.isNotEmpty())
        put(stack, enchants)
        return stack
    }
}
