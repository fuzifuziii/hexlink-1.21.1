package jempasam.hexlink

import at.petrak.hexcasting.api.casting.ActionRegistryEntry
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.xplat.IXplatAbstractions
import jempasam.hexlink.operators.OpSpiritTest
import jempasam.hexlink.operators.OpStub
import jempasam.hexlink.operators.spells.*
import jempasam.hexlink.operators.spiritinfo.OpGetSpiritIota
import jempasam.hexlink.operators.spiritinfo.OpGetSpiritListIota
import jempasam.hexlink.operators.spiritinfo.OpSpiritCountIota
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation

object HexlinkPatterns {

    private val PATTERNS = mutableMapOf<ResourceLocation, ActionRegistryEntry>()

    private fun register(id: String, pattern: HexPattern, op: Action) {
        PATTERNS[ResourceLocation.fromNamespaceAndPath(HexlinkMod.MODID, id)] = ActionRegistryEntry(pattern, op)
    }

    private fun registerFor(modid: String, id: String, pattern: HexPattern, op: () -> () -> Action) {
        if (IXplatAbstractions.INSTANCE.isModPresent(modid)) {
            register(id, pattern, op()())
        } else {
            register(id, pattern, OpStub(modid))
        }
    }

    /** Called from the NeoForge `RegisterEvent` for HexCasting's action registry. */
    fun registerAll(registry: Registry<ActionRegistryEntry>) {
        for ((id, entry) in PATTERNS) {
            Registry.register(registry, id, entry)
        }
    }

    init {
        register("spirit_extraction",
            HexPattern.fromAngleString("aawddaweddwaa", HexDir.NORTH_EAST),
            OpExtractSpirit()
        )

        register("spirit_transfer",
            HexPattern.fromAngleString("aawddawdaqqqa", HexDir.NORTH_EAST),
            OpSpiritTransfer()
        )
        register("spirit_transfer_look",
            HexPattern.fromAngleString("aawddaeqqqqq", HexDir.NORTH_EAST),
            OpGetSpiritIota(false)
        )
        register("spirit_transfer_look_all",
            HexPattern.fromAngleString("aawddaqeeeee", HexDir.NORTH_EAST),
            OpGetSpiritListIota(false)
        )

        register("spirit_self_transfer_look",
            HexPattern.fromAngleString("aawddaeqqqqqaw", HexDir.NORTH_EAST),
            OpGetSpiritIota(true)
        )

        register("spirit_transfer_count",
            HexPattern.fromAngleString("aawddaeqq", HexDir.NORTH_EAST),
            OpSpiritCountIota(false)
        )

        register("spirit_self_transfer_count",
            HexPattern.fromAngleString("aawddaeqqaw", HexDir.NORTH_EAST),
            OpSpiritCountIota(true)
        )

        register("spirit_manifestation",
            HexPattern.fromAngleString("aawddaweqaeaq", HexDir.NORTH_EAST),
            OpManisfestSpirit(false)
        )

        register("spirit_self_manifestation",
            HexPattern.fromAngleString("aawddaweqaeaqa", HexDir.NORTH_EAST),
            OpManisfestSpirit(true)
        )

        register("spirit_look",
            HexPattern.fromAngleString("aawddaweqqqqq", HexDir.NORTH_EAST),
            OpSpiritTest()
        )

        register("write_spell",
            HexPattern.fromAngleString("waaddaawwwddaaddwaweqqqqqwaeaeaeaeaeaqwwwewddaaddwwwaaddaaw", HexDir.NORTH_EAST),
            OpFillSpell(MediaConstants.CRYSTAL_UNIT.toInt())
        )
    }
}
