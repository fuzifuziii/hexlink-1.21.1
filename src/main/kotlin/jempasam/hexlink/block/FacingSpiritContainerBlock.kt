package jempasam.hexlink.block

import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.Mirror
import net.minecraft.world.level.block.Rotation
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

class FacingSpiritContainerBlock<T: BlockEntity>(
    settings: BlockBehaviour.Properties,
    type: () -> BlockEntityType<T>,
    ticker: BlockEntityTicker<T>,
    slot_count: Int, shape: VoxelShape
) : SpiritContainerBlock<T>(settings, type, ticker, slot_count, shape){

    override fun codec(): MapCodec<out BaseEntityBlock> =
        simpleCodec { props -> FacingSpiritContainerBlock(props, type, ticker, slot_count, shape) }

    override fun rotate(state: BlockState, rotation: Rotation): BlockState {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)))
    }

    override fun mirror(state: BlockState, mirror: Mirror): BlockState {
        return state.rotate(mirror.getRotation(state.getValue(FACING)))
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        super.createBlockStateDefinition(builder)
        builder.add(FACING)
    }

    override fun getShape(state: BlockState, world: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape {
        val base=super.getShape(state, world, pos, context)
        if(state.getValue(FACING).axis==Direction.Axis.Z){
            return base
        }
        else{
            return base.toAabbs()
                .map { AABB(it.minZ,it.minY,it.minX, it.maxZ,it.maxY,it.maxX) }
                .map { Shapes.create(it) }
                .reduceRight{a,b -> Shapes.or(a,b)}
        }
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState {
        for (direction in ctx.nearestLookingDirections) {
            if (direction.axis.isHorizontal) {
                return defaultBlockState().setValue(FACING, direction.opposite)
            }
        }
        return defaultBlockState()
    }

    companion object{
        val FACING = HorizontalDirectionalBlock.FACING
    }
}
