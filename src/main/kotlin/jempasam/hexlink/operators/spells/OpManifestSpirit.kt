package jempasam.hexlink.operators.spells

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.casting.getIntBetween
import jempasam.hexlink.data.HexlinkConfiguration
import jempasam.hexlink.mishap.MishapNoEnoughSoul
import jempasam.hexlink.mishap.MishapNotManifestable
import jempasam.hexlink.operators.getSpirit
import jempasam.hexlink.particle.HexlinkParticles
import jempasam.hexlink.spirit.Spirit
import jempasam.hexlink.spirit.inout.SpiritSource
import net.minecraft.server.level.ServerLevel
import net.minecraft.network.chat.Component
import net.minecraft.world.phys.Vec3

class OpManisfestSpirit(oncaster: Boolean) : SpiritSpellAction(oncaster) {

    override val argCount: Int get() = 3

    override fun execute(source: SpiritSource, sourcePos: Vec3, args: List<Iota>, ctx: CastingEnvironment): SpellAction.Result {
        val spirit=args.getSpirit(pos(0),argc)
        val power=args.getIntBetween(pos(1), 1, 100, argc)
        val target=args[pos(2)]

        val input=source.extract(power,spirit)
        if(input.maxcount==0)throw MishapNoEnoughSoul(spirit,1)

        val caster = ctx.castingEntity as? net.minecraft.world.entity.player.Player
            ?: throw at.petrak.hexcasting.api.casting.mishaps.MishapBadCaster()
        val (manifestation,targetPos)=when(target){
            is Vec3Iota ->{
                ctx.assertVecInRange(target.vec3)
                spirit.manifestAt(caster,ctx.world, target.vec3, input.maxcount) to target.vec3
            }
            is EntityIota ->{
                val entity = target.getEntity(ctx.world)
                ctx.assertEntityInRange(entity)
                spirit.manifestIn(caster,ctx.world, entity, input.maxcount) to entity.position()
            }
            else -> throw MishapInvalidIota(target, 0, Component.translatable("hexcasting.iota.hexcasting:entity").append(Component.translatable("hexlink.or")).append(Component.translatable("hexcasting.iota.hexcasting:vec3")))
        }

        if(manifestation.minimumCount>input.maxcount)throw MishapNoEnoughSoul(spirit,manifestation.minimumCount)
        if(manifestation.spiritCount==0)throw MishapNotManifestable(spirit, target)
        val manisfestedAmount=Math.min(manifestation.spiritCount,input.maxcount)

        return SpellAction.Result(
            ManifestSpell(ctx.world, manifestation, input, sourcePos, targetPos, spirit, manisfestedAmount),
            (manifestation.maxMediaCost*(HexlinkConfiguration.spirit_settings[spirit.getType()]?.media_cost ?: 5)).toLong(),
            listOf(ParticleSpray.burst(targetPos,1.0,1))
        )
    }

    class ManifestSpell(val world: ServerLevel, val manifestation: Spirit.Manifestation, val source: SpiritSource.SpiritOutputFlux, val from: Vec3, val to: Vec3, val spirit: Spirit, val count: Int) : RenderedSpell{
        override fun cast(ctx: CastingEnvironment) {
            source.consume(count)
            manifestation.execute(count)
            HexlinkParticles.sendLink(world, from, to, spirit.getColor(), count)
        }
    }
}