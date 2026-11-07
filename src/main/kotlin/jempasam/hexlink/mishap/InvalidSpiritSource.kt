package jempasam.hexlink.mishap

import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.utils.TreeList
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor

class InvalidSpiritSource(val iota: Iota) : Mishap() {
    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context): Component {
        return Component.translatable("hexlink.mishap.invalid_spirit_source", iota.display())
    }

    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment {
        return dyeColor(DyeColor.PINK)
    }

    override fun execute(ctx: CastingEnvironment, errorCtx: Context, stack: TreeList<Iota>): TreeList<Iota> {
        (ctx.castingEntity as? net.minecraft.world.entity.LivingEntity)?.addEffect(MobEffectInstance(MobEffects.BLINDNESS,40))
        return stack
    }
}