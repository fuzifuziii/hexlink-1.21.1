package jempasam.hexlink.operators.spells

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import jempasam.hexlink.operators.getSpiritSourceAndPos
import jempasam.hexlink.spirit.inout.SpiritHelper
import jempasam.hexlink.spirit.inout.SpiritSource
import net.minecraft.world.phys.Vec3

abstract class SpiritSpellAction(val onCaster: Boolean) : SpellAction {
    abstract val argCount: Int

    fun pos(i: Int): Int = if(onCaster) i else i+1

    override val argc: Int get() = if(onCaster) argCount else argCount+1

    override fun execute(args: List<Iota>, ctx: CastingEnvironment): SpellAction.Result {
        if(onCaster){
            val caster = ctx.castingEntity as? net.minecraft.world.entity.player.Player
                ?: throw at.petrak.hexcasting.api.casting.mishaps.MishapBadCaster()
            val source=SpiritHelper.spiritSource(caster)
            return execute(source, caster.position(), args, ctx)
        }
        else{
            val source=args.getSpiritSourceAndPos(ctx,0,argCount+1)
            return execute(source.first, source.second, args, ctx)
        }
    }

    abstract fun execute(source: SpiritSource, sourcePos: Vec3, args: List<Iota>, ctx: CastingEnvironment): SpellAction.Result
}