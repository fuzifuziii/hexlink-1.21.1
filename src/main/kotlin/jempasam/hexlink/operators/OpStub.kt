package jempasam.hexlink.operators

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import net.minecraft.network.chat.Component

class OpStub(val modid: String) : ConstMediaAction {
    override val argc: Int = 0

    override fun execute(args: List<Iota>, ctx: CastingEnvironment): List<Iota> {
        (ctx.castingEntity as? net.minecraft.world.entity.LivingEntity)?.sendSystemMessage(Component.translatable("hexlink.stub", modid))
        return listOf()
    }
}