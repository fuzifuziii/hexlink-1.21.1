package jempasam.hexlink.operators.spiritinfo

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import jempasam.hexlink.operators.getSpiritSourceOpt
import jempasam.hexlink.spirit.inout.SpiritHelper
import jempasam.hexlink.spirit.inout.SpiritSource

abstract class SpiritConstMediaAction(val onCaster: Boolean) : ConstMediaAction {
    abstract val argCount: Int

    fun pos(i: Int): Int = if(onCaster) i else i+1

    override val argc: Int get() = if(onCaster) argCount else argCount+1

    override fun execute(args: List<Iota>, ctx: CastingEnvironment): List<Iota> {
        if(onCaster){
            val caster = ctx.castingEntity as? net.minecraft.world.entity.player.Player
                ?: throw at.petrak.hexcasting.api.casting.mishaps.MishapBadCaster()
            val source=SpiritHelper.spiritSource(caster)
            return execute(source, args, ctx)
        }
        else{
            val source=args.getSpiritSourceOpt(ctx,0,argCount+1)
            return execute(source, args, ctx)
        }
    }

    abstract fun execute(source: SpiritSource?, args: List<Iota>, ctx: CastingEnvironment): List<Iota>
}