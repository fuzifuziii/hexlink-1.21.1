package jempasam.hexlink.mishap

import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.utils.TreeList
import jempasam.hexlink.spirit.Spirit
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor

class MishapNoEnoughSoul(val spirit: Spirit, val missing: Int): Mishap() {
    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context): Component {
        return Component.translatable("hexlink.mishap.not_enough_soul", missing)
    }

    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment = dyeColor(DyeColor.CYAN)

    override fun execute(ctx: CastingEnvironment, errorCtx: Context, stack: TreeList<Iota>): TreeList<Iota> = stack
}