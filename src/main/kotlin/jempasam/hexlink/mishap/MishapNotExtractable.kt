package jempasam.hexlink.mishap

import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.utils.TreeList
import jempasam.hexlink.spirit.extractor.SpiritExtractor
import net.minecraft.world.entity.Entity
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor

class MishapNotExtractable(val entity: Entity, val extractor: SpiritExtractor<*>) : Mishap() {
    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context): Component {
        return Component.translatable("hexlink.mishap.not_extractable", entity.name, extractor.getName()).setStyle(Style.EMPTY.withColor(DyeColor.RED.getTextColor()))
        //return entity.name.copy().append(Component.translatable("hexlink.mishap.not_extractable")).append(item.name).setStyle(Style.EMPTY.withColor(DyeColor.RED.getTextColor()))
    }

    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment {
        return dyeColor(DyeColor.RED)
    }

    override fun execute(ctx: CastingEnvironment, errorCtx: Context, stack: TreeList<Iota>): TreeList<Iota> {
        (ctx.castingEntity as? net.minecraft.world.entity.LivingEntity)?.addEffect(MobEffectInstance(MobEffects.CONFUSION,120))
        return stack
    }

}