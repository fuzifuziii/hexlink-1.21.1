package jempasam.hexlink.operators

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.BooleanIota
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import jempasam.hexlink.iota.SpiritIota
import net.minecraft.network.chat.Component

class OpSpiritTest : ConstMediaAction {
    override val argc: Int
        get() = 2

    override fun execute(args: List<Iota>, ctx: CastingEnvironment): List<Iota> {
        val spirit=args[0]
        val target=args[1]
        return if(spirit is SpiritIota){
            when (target) {
                is Vec3Iota ->
                    listOf(BooleanIota(spirit.spirit.lookAt(ctx.requireCaster(),ctx.world,target.vec3)))
                is EntityIota ->
                    listOf(BooleanIota(spirit.spirit.lookIn(ctx.requireCaster(),ctx.world,target.resolveEntity(ctx))))
                else ->
                    throw MishapInvalidIota(target, 0, Component.translatable("hexcasting.iota.hexcasting:entity").append(Component.translatable("hexlink.or")).append(Component.translatable("hexcasting.iota.hexcasting:vec3")))
            }
        }
        else throw MishapInvalidIota(spirit,1, Component.translatable("hexlink.spirit_iota"))
    }
}