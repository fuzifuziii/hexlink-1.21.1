package jempasam.hexlink.spirit

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Rarity
import net.minecraft.world.item.UseAnim
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3

class ItemSpirit(val item: Item): Spirit {

    private val color: Int = run {
        val stack = item.defaultInstance
        when {
            stack.has(DataComponents.FIRE_RESISTANT) -> DyeColor.ORANGE.getFireworkColor()
            stack.get(DataComponents.FOOD) != null -> DyeColor.BROWN.getFireworkColor()
            item.isEnchantable(stack) -> DyeColor.YELLOW.getFireworkColor()
            item.isFoil(stack) -> DyeColor.MAGENTA.getFireworkColor()
            item is BlockItem -> item.block.defaultMapColor().col
            stack.rarity != Rarity.COMMON ->
                stack.rarity.color().color ?: DyeColor.ORANGE.getTextColor()
            else -> DyeColor.ORANGE.getTextColor()
        }
    }

    override fun manifestAt(caster: Player, world: ServerLevel, position: Vec3, count: Int): Spirit.Manifestation {
        return Spirit.Manifestation(1,count){
            val stack=item.defaultInstance
            stack.count=it
            val oldHandItem=caster.mainHandItem
            caster.setItemInHand(InteractionHand.MAIN_HAND,stack)
            val hit = BlockHitResult(position, Direction.UP, BlockPos.containing(position), true)
            for(i in 0..<it){
                stack.useOn(UseOnContext(caster, InteractionHand.MAIN_HAND, hit))
            }
            caster.setItemInHand(InteractionHand.MAIN_HAND, oldHandItem)
        }
    }

    override fun manifestIn(caster: Player, world: ServerLevel, entity: Entity, count: Int): Spirit.Manifestation {
        return Spirit.Manifestation(1,count){
            val stack=item.defaultInstance
            stack.count=it
            val oldHandItem=caster.mainHandItem
            caster.setItemInHand(InteractionHand.MAIN_HAND,stack)
            if(entity==caster){
                for(i in 0..<it){
                    val action=item.getUseAnimation(stack)
                    val success=item.use(world, caster, InteractionHand.MAIN_HAND)
                    if(action!=UseAnim.NONE && action!=UseAnim.BLOCK && success.result.consumesAction()){
                        item.releaseUsing(stack, world, caster, 0)
                        item.finishUsingItem(stack,world,caster)
                    }
                }
            }
            else{
                for(i in 0..<it) {
                    if (entity is LivingEntity) {
                        stack.interactLivingEntity(caster, entity, InteractionHand.MAIN_HAND)
                    }
                    entity.interact(caster, InteractionHand.MAIN_HAND)
                }
            }
            caster.setItemInHand(InteractionHand.MAIN_HAND, oldHandItem)
        }
    }



    override fun lookIn(caster: Player, world: ServerLevel, entity: Entity): Boolean {
        val stack= StackHelper.stack(caster,entity) ?: return false
        return stack.stack.item==this.item
    }

    override fun lookAt(caster: Player, world: ServerLevel, position: Vec3): Boolean {
        val area = AABB(position.x-0.35, position.y-0.35, position.z-0.35, position.x+0.35, position.y+0.35, position.z+0.35)
        val entities=world.getEntities(EntityType.ITEM, area) { true }
        return entities.any { entity -> entity.item.item==item }
    }



    override fun equals(other: Any?): Boolean = other is ItemSpirit && item===other.item

    override fun hashCode(): Int = item.hashCode()*36

    override fun getColor(): Int = color

    override fun getName(): Component = item.defaultInstance.hoverName

    override fun serialize(): Tag {
        return StringTag.valueOf(BuiltInRegistries.ITEM.getKey(item).toString())
    }



    override fun getType(): Spirit.SpiritType<*> = Type

    object Type: Spirit.SpiritType<ItemSpirit>{
        override fun getName(): Component = Component.translatable("hexlink.spirit.item")

        override fun deserialize(nbt: Tag): ItemSpirit? {
            if(nbt !is StringTag) throw IllegalArgumentException()
            val id = ResourceLocation.tryParse(nbt.asString) ?: return null
            val type = BuiltInRegistries.ITEM.getOptional(id).orElse(null) ?: return null
            return ItemSpirit(type)
        }
    }
}
