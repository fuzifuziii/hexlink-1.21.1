package jempasam.hexlink.spirit

import jempasam.hexlink.HexlinkRegistry
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.nbt.Tag
import net.minecraft.nbt.StringTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.Vec3

class SpecialSpirit(val specialType: SpecialType) : Spirit {
    override fun getColor(): Int = specialType.color

    override fun getName(): Component = specialType.text

    override fun equals(other: Any?): Boolean = other is SpecialSpirit && specialType===other.specialType

    override fun hashCode(): Int = specialType.hashCode()

    override fun lookAt(caster: Player, world: ServerLevel, position: Vec3): Boolean
        = specialType.lookAt.lookAt(caster, world, position)

    override fun lookIn(caster: Player, world: ServerLevel, entity: Entity): Boolean
        = specialType.lookIn.lookIn(caster, world, entity)

    override fun manifestAt(caster: Player, world: ServerLevel, position: Vec3, count: Int): Spirit.Manifestation
        = specialType.manifestAt.manifestAt(caster, world, position, count)

    override fun manifestIn(caster: Player, world: ServerLevel, entity: Entity, count: Int): Spirit.Manifestation
        = specialType.manifestIn.manifestIn(caster, world, entity, count)

    override fun serialize(): Tag {
        return StringTag.valueOf(HexlinkRegistry.SPECIAL_SPIRIT.getKey(specialType)?.toString() ?: "")
    }

    override fun getType(): Spirit.SpiritType<*> = Type

    object Type: Spirit.SpiritType<SpecialSpirit>{
        override fun getName(): Component
            = Component.translatable("hexlink.spirit.special")

        override fun deserialize(nbt: Tag): SpecialSpirit?{
            val id = ResourceLocation.tryParse(nbt.asString) ?: return null
            return HexlinkRegistry.SPECIAL_SPIRIT.get(id)?.let { SpecialSpirit(it) }
        }
    }

    class SpecialType(val manifestAt: Spirit, val manifestIn: Spirit, val lookAt: Spirit, val lookIn: Spirit, val text: Component, val color: Int)
}