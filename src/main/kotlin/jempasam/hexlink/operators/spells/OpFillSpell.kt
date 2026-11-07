package jempasam.hexlink.operators.spells

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadEntity
import at.petrak.hexcasting.api.casting.mishaps.MishapOthersName
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.getList
import jempasam.hexlink.item.functionnality.SpellHolderItem
import jempasam.hexlink.spirit.StackHelper
import net.minecraft.network.chat.Component

class OpFillSpell(val cost: Int) : SpellAction {
    override val argc: Int get() = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        val entity = args.getEntity(env.world, 0, argc)
        val spell = args.getList(1, argc).toList()

        val worldStack = StackHelper.stack(env.castingEntity as? net.minecraft.world.entity.player.Player, entity)

        if (worldStack == null || worldStack.stack.item !is SpellHolderItem) {
            throw MishapBadEntity(entity, Component.translatable("hexlink.mishap.spell_holder"))
        }

        MishapOthersName.getTrueNameMishapFromArgs(env.world, spell, env.castingEntity as? net.minecraft.world.entity.player.Player)
            ?.let { throw it }

        return SpellAction.Result(
            Spell(worldStack, spell),
            cost.toLong(),
            listOf(ParticleSpray.burst(entity.position(), 0.5, 1))
        )
    }

    class Spell(val worldStack: StackHelper.WorldStack, val spell: List<Iota>) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val item = worldStack.stack.item as SpellHolderItem
            item.setSpell(worldStack.stack, spell)
            worldStack.update()
        }
    }
}
