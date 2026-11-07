package jempasam.hexlink.operators

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import at.petrak.hexcasting.api.casting.getEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3

/**
 * Small bridges over the HexCasting 0.12 API.
 *
 * In 0.12 `CastingEnvironment` exposes [CastingEnvironment.getCastingEntity] instead of a
 * `caster` property, and [EntityIota] resolves its entity per level rather than holding one.
 */

/** The player casting, or null when the caster is not a player (e.g. a circle). */
val CastingEnvironment.casterOrNull: Player?
    get() = castingEntity as? Player

/** The casting player, or throws if the caster is not a player. */
fun CastingEnvironment.requireCaster(): Player =
    casterOrNull ?: throw IllegalStateException("Hexlink requires a player caster")

/** Resolve an entity iota against the environment's level. */
fun EntityIota.resolveEntity(env: CastingEnvironment): Entity = getEntity(env.world)

/** The position an iota points at: an entity's position, or a literal vector. */
fun iotaPosition(env: CastingEnvironment, iota: at.petrak.hexcasting.api.casting.iota.Iota): Vec3 =
    when (iota) {
        is EntityIota -> iota.resolveEntity(env).position()
        is Vec3Iota -> iota.vec3
        else -> Vec3.ZERO
    }

/** Block position for a vector, for APIs that still take a [BlockPos]. */
fun Vec3.toBlockPos(): BlockPos = BlockPos.containing(this)
