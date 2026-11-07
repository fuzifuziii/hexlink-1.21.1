package jempasam.hexlink.data

import jempasam.hexlink.spirit.Spirit

/**
 * Per-spirit settings loaded from `data/hexlink/hexlink_spirits/​*.json`.
 *
 * This used to implement Cloth Config's `ConfigData`, but the values are plain data read from
 * the server's data pack, so no config-library integration is needed.
 */
object HexlinkConfiguration {
    class SpiritSettings(val use_soul: Boolean, val media_cost: Int)

    val spirit_settings = mutableMapOf<Spirit.SpiritType<*>, SpiritSettings>()
}
