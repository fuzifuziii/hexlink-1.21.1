package jempasam.hexlink.block

import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.NullIota
import com.mojang.serialization.MapCodec
import jempasam.hexlink.block.functionnality.BlockIotaHolder
import jempasam.hexlink.entity.functionnality.IotaHolder
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape

class IotaHolderBlock<T: BlockEntity>(
    settings: BlockBehaviour.Properties,
    private val type: ()->BlockEntityType<T>,
    private val ticker: BlockEntityTicker<T>,
    private val shape: VoxelShape
) : BaseEntityBlock(settings), BlockIotaHolder {

    override fun codec(): MapCodec<out BaseEntityBlock> =
        simpleCodec { props -> IotaHolderBlock(props, type, ticker, shape) }

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = type().create(pos, state)!!

    override fun getRenderShape(state: BlockState): RenderShape = RenderShape.MODEL

    override fun <T : BlockEntity?> getTicker(world: Level, state: BlockState, type: BlockEntityType<T>): BlockEntityTicker<T>? {
        return createTickerHelper(type, this.type()) { w, p, s, be -> ticker.tick(w, p, s, be) }
    }

    override fun onRemove(state: BlockState, world: Level, pos: BlockPos, newState: BlockState, moved: Boolean) {
        if (!state.`is`(newState.block) && world is ServerLevel) {
            val center=Vec3.atCenterOf(pos)
            world.sendParticles(
                    ParticleTypes.CLOUD,
                    center.x, center.y, center.z,
                    10,
                    0.5, 0.5, 0.5,
                    0.1
            )

        }

        super.onRemove(state, world, pos, newState, moved)
    }

    override fun getIota(blockpos: BlockPos, world: ServerLevel): Iota
        = (world.getBlockEntity(blockpos) as? IotaHolder)?.getIota() ?: NullIota()

    override fun getShape(state: BlockState, world: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape {
        return shape
    }
}
