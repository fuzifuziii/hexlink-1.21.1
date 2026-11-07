package jempasam.hexlink.item.color

import net.minecraft.util.FastColor

/**
 * Since 1.21 item and block tints are ARGB; an RGB value with no alpha byte renders fully transparent.
 * -1 (no tint) is already opaque white, so it passes through unchanged.
 */
internal fun opaque(rgb: Int): Int = if (rgb == -1) -1 else FastColor.ARGB32.opaque(rgb)
