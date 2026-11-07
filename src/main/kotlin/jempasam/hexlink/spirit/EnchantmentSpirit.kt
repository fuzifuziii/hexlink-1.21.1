package jempasam.hexlink.spirit

import jempasam.hexlink.utils.EnchantHelper
import jempasam.hexlink.utils.HexlinkRegistryAccess
import net.minecraft.core.Holder
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.phys.Vec3
import kotlin.math.min

class EnchantmentSpirit(val enchantment: Holder<Enchantment>): Spirit {

    override fun getName(): Component = enchantment.value().description()

    override fun getColor(): Int = DyeColor.MAGENTA.getFireworkColor()

    override fun equals(other: Any?): Boolean = other is EnchantmentSpirit && enchantment == other.enchantment

    override fun hashCode(): Int = enchantment.hashCode()*39

    override fun manifestAt(caster: Player, world: ServerLevel, position: Vec3, count: Int): Spirit.Manifestation {
        return manifestStack(StackHelper.stack(caster,world,position), count)
    }
    override fun manifestIn(caster: Player, world: ServerLevel, entity: Entity, count: Int): Spirit.Manifestation {
        return manifestStack(StackHelper.stack(caster,entity), count)
    }
    private fun manifestStack(worldStack: StackHelper.WorldStack?, count: Int): Spirit.Manifestation{
        if(worldStack==null)return Spirit.NONE_MANIFESTATION
        val level= min((count.takeHighestOneBit().countTrailingZeroBits()+1), enchantment.value().maxLevel)
        if(EnchantHelper.canEnchant(worldStack.stack, enchantment, level)){
            val cost=1 shl (level-1)
            val new_stack= EnchantHelper.enchant(worldStack.stack, enchantment, level) ?: return Spirit.NONE_MANIFESTATION
            return Spirit.Manifestation(1, cost, cost){
                worldStack.replace(new_stack)
            }
        }
        return Spirit.NONE_MANIFESTATION
    }

    override fun lookIn(caster: Player, world: ServerLevel, entity: Entity): Boolean {
        return lookStack(StackHelper.stack(caster,entity))
    }
    override fun lookAt(caster: Player, world: ServerLevel, position: Vec3): Boolean {
        return lookStack(StackHelper.stack(caster,world,position))
    }
    private fun lookStack(worldStack: StackHelper.WorldStack?): Boolean{
        return worldStack!=null && EnchantmentHelper.getItemEnchantmentLevel(enchantment, worldStack.stack)!=0
    }

    override fun serialize(): Tag {
        // Enchantments are a datapack registry in 1.21, so the id comes from the holder itself.
        return StringTag.valueOf(enchantment.unwrapKey().map { it.location().toString() }.orElse(""))
    }

    override fun getType(): Spirit.SpiritType<*> = Type
    object Type: Spirit.SpiritType<EnchantmentSpirit>{
        override fun getName(): Component = Component.translatable("hexlink.spirit.enchantment")
        override fun deserialize(nbt: Tag): EnchantmentSpirit? {
            if (nbt !is StringTag) return null
            val id = ResourceLocation.tryParse(nbt.asString) ?: return null
            val registry = HexlinkRegistryAccess.enchantments() ?: return null
            val holder = registry.getHolder(ResourceKey.create(Registries.ENCHANTMENT, id)).orElse(null) ?: return null
            return EnchantmentSpirit(holder)
        }
    }
}
