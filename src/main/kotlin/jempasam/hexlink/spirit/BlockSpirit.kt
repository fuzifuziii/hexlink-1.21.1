package jempasam.hexlink.spirit

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.item.FallingBlockEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.BlockItem
import net.minecraft.world.level.block.Block
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3

//TODO Long time block interaction with manifestation
class BlockSpirit(val block: Block): Spirit{

    override fun manifestAt(caster: Player, world: ServerLevel, position: Vec3, power: Int): Spirit.Manifestation {
        val startpos=BlockPos.containing(position)
        var testpos=startpos
        var finalPower=0
        while(finalPower<power){
            if(!world.getBlockState(testpos).isAir) break
            testpos=testpos.above()
            finalPower++
        }
        if(finalPower==0)return Spirit.NONE_MANIFESTATION

        return Spirit.Manifestation(3, finalPower){
            var blockpos=startpos
            for(i in 0..<it){
                world.setBlock(blockpos, block.defaultBlockState(), 3)
                blockpos=blockpos.above()
            }
        }
    }

    override fun manifestIn(caster: Player, world: ServerLevel, entity: Entity, count: Int): Spirit.Manifestation {
        return Spirit.Manifestation(1,1){
            val pos=BlockPos.containing(entity.position())
            val state=block.defaultBlockState()
            state.entityInside(world, pos, entity)
            block.stepOn(world, pos, state, entity)
            if(entity is Player){
                val hit = BlockHitResult(entity.position(), Direction.UP, pos, true)
                state.useItemOn(entity.mainHandItem, world, entity, InteractionHand.MAIN_HAND, hit)
            }
        }
    }

    override fun manifestBetween(caster: Player, world: ServerLevel, from: Vec3, to: Vec3, count: Int): Spirit.Manifestation {
        val blockpos=BlockPos.containing(from)
        if(!world.getBlockState(blockpos).isAir)return Spirit.NONE_MANIFESTATION
        return Spirit.Manifestation(1,count){
            var direction=to.subtract(from)
            if(direction.length()>5)direction=direction.normalize().scale(5.0)
            for(i in 0..<it){
                val falling=FallingBlockEntity.fall(world, blockpos, block.defaultBlockState())
                falling.setDeltaMovement(direction.x, direction.y+i*0.5, direction.z)
            }
        }
    }

    override fun manifestBetween(caster: Player, world: ServerLevel, from: Entity, to: Vec3, count: Int): Spirit.Manifestation {
        return super.manifestBetween(caster, world, from.position(), to, count)
    }

    override fun manifestBetween(caster: Player, world: ServerLevel, from: Entity, to: Entity, count: Int): Spirit.Manifestation {
        return super.manifestBetween(caster, world, from.position(), to.position(), count)
    }


    override fun lookAt(caster: Player, world: ServerLevel, position: Vec3): Boolean {
        return world.getBlockState(BlockPos.containing(position)).block==block
    }

    override fun lookIn(caster: Player, world: ServerLevel, entity: Entity): Boolean {
        val stack= StackHelper.stack(caster,entity) ?: return false
        val item=stack.stack.item
        if(item !is BlockItem)return false
        return item.block==block
    }



    override fun equals(other: Any?): Boolean = other is BlockSpirit && block===other.block

    override fun hashCode(): Int = block.hashCode()*36



    override fun getColor(): Int = block.defaultMapColor().col

    override fun getName(): Component = block.name

    override fun serialize(): Tag {
        return StringTag.valueOf(BuiltInRegistries.BLOCK.getKey(block).toString())
    }



    override fun getType(): Spirit.SpiritType<*> = Type

    object Type: Spirit.SpiritType<BlockSpirit>{
        override fun getName(): Component = Component.translatable("hexlink.spirit.block")

        override fun deserialize(nbt: Tag): BlockSpirit? {
            if(nbt !is StringTag) throw IllegalArgumentException()
            val id = ResourceLocation.tryParse(nbt.asString) ?: return null
            val type = BuiltInRegistries.BLOCK.getOptional(id).orElse(null) ?: return null
            return BlockSpirit(type)
        }
    }

}
