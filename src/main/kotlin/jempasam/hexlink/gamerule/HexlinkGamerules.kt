package jempasam.hexlink.gamerule

import net.minecraft.world.level.GameRules

/**
 * Hexlink's game rules.
 *
 * Fabric registered these through `GameRuleRegistry` and offered a double rule type. Vanilla
 * 1.21 only has boolean and integer rule types, so the extraction probability is stored as a
 * whole percentage (0..100) instead of the old 0.0..1.0 double.
 */
object HexlinkGamerules {

    val EXTRACTION_PROBABILITY: GameRules.Key<GameRules.IntegerValue> =
        GameRules.register(
            "spiritExtractionSuccessProb",
            GameRules.Category.MISC,
            GameRules.IntegerValue.create(1) { _, _ -> }
        )

    /** Forces the rule to be created; called during mod construction. */
    fun register() {
        EXTRACTION_PROBABILITY
    }
}
