package jempasam.hexlink.spirit

import net.minecraft.core.Holder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.AreaEffectCloud
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.alchemy.PotionContents
import net.minecraft.world.phys.Vec3
import java.util.Optional

class PotionSpirit(val potionEffect: Holder<MobEffect>) : Spirit {

    override fun manifestAt(caster: Player, world: ServerLevel, position: Vec3, count: Int): Spirit.Manifestation {
        return Spirit.Manifestation(1,count){
            val cloud = AreaEffectCloud(world, position.x, position.y, position.z)
            cloud.setPotionContents(
                PotionContents(Optional.empty(), Optional.empty(), listOf(MobEffectInstance(potionEffect, it*100, it/4)))
            )
            cloud.radius = it.toFloat()
            cloud.duration = it*100
            world.addFreshEntity(cloud)
        }
    }

    override fun manifestIn(caster: Player, world: ServerLevel, entity: Entity, count: Int): Spirit.Manifestation {
        if(entity !is LivingEntity)
            return Spirit.NONE_MANIFESTATION
        else
            return Spirit.Manifestation(1,count){
                entity.addEffect(MobEffectInstance(potionEffect, it*200, it/3))
            }
    }

    override fun lookIn(caster: Player, world: ServerLevel, entity: Entity): Boolean {
        return entity is LivingEntity && entity.activeEffects.any { it.effect == potionEffect }
    }

    override fun lookAt(caster: Player, world: ServerLevel, position: Vec3): Boolean {
        return false
    }

    override fun equals(other: Any?): Boolean = other is PotionSpirit && potionEffect == other.potionEffect

    override fun hashCode(): Int = potionEffect.hashCode()*36

    override fun getColor(): Int = potionEffect.value().color

    override fun getName(): Component = potionEffect.value().displayName

    override fun serialize(): Tag {
        return StringTag.valueOf(
            (BuiltInRegistries.MOB_EFFECT.getKey(potionEffect.value()) ?: return StringTag.valueOf("")).toString()
        )
    }

    override fun getType(): Spirit.SpiritType<*> = Type

    object Type: Spirit.SpiritType<PotionSpirit>{
        override fun getName(): Component = Component.translatable("hexlink.spirit.potion")

        override fun deserialize(nbt: Tag): PotionSpirit? {
            if(nbt !is StringTag) throw IllegalArgumentException()
            val id = ResourceLocation.tryParse(nbt.asString) ?: return null
            val effect = BuiltInRegistries.MOB_EFFECT.getHolder(id).orElse(null) ?: return null
            return PotionSpirit(effect)
        }
    }
}
