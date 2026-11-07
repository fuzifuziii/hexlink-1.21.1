package jempasam.hexlink.mishap

import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.GarbageIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.utils.TreeList
import jempasam.hexlink.spirit.Spirit
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor

class MishapNotManifestable(val spirit: Spirit, val target: Iota) : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment = dyeColor(DyeColor.BLUE)

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context): Component
        = Component.translatable("hexlink.mishap.not_manifestable", spirit.getName(), target.display())

    override fun execute(ctx: CastingEnvironment, errorCtx: Context, stack: TreeList<Iota>): TreeList<Iota> =
        stack.updated(stack.size - 1, GarbageIota())
}