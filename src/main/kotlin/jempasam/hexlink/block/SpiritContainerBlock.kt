package jempasam.hexlink.block

import com.mojang.serialization.MapCodec
import jempasam.hexlink.block.functionnality.BlockSpiritContainer
import jempasam.hexlink.block.functionnality.BlockSpiritSource
import jempasam.hexlink.block.functionnality.BlockSpiritTarget
import jempasam.hexlink.particle.HexlinkParticles
import jempasam.hexlink.spirit.Spirit
import jempasam.hexlink.spirit.inout.SpiritSource
import jempasam.hexlink.spirit.inout.SpiritTarget
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

open class SpiritContainerBlock<T: BlockEntity>(
    settings: BlockBehaviour.Properties,
    protected val type: ()->BlockEntityType<T>,
    protected val ticker: BlockEntityTicker<T>,
    protected val slot_count: Int,
    protected val shape: VoxelShape
) : BaseEntityBlock(settings), BlockSpiritSource, BlockSpiritTarget, BlockSpiritContainer {

    companion object{
        fun coloredParticle(world: Level, pos: BlockPos, color: Int, count: Int){
            val center=Vec3.atLowerCornerOf(pos)
            val r = (color shr 16 and 0xFF).toDouble() / 255.0
            val g = (color shr 8 and 0xFF).toDouble() / 255.0
            val b = (color shr 0 and 0xFF).toDouble() / 255.0
            for (j in 0 until count) {
                val particlePos=center.add(Math.random(), Math.random(),Math.random())
                world.addParticle(
                    HexlinkParticles.SPIRIT,
                    particlePos.x, particlePos.y, particlePos.z,
                    r, g, b
                )
            }
        }
    }

    override fun codec(): MapCodec<out BaseEntityBlock> = simpleCodec { props -> SpiritContainerBlock(props, type, ticker, slot_count, shape) }

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

    override fun getSpiritSource(world: ServerLevel, pos: BlockPos): SpiritSource {
        return world.getBlockEntity(pos) as? SpiritSource ?: SpiritSource.NONE
    }

    override fun getSpiritTarget(world: ServerLevel, pos: BlockPos): SpiritTarget {
        return world.getBlockEntity(pos) as? SpiritTarget ?: SpiritTarget.NONE
    }

    fun addAt(world: ServerLevel, pos: BlockPos): Boolean{
        val bstate=world.getBlockState(pos)
        val block=bstate.block
        if(bstate.isAir){
            world.setBlock(pos, defaultBlockState(), 3)
        }
        else if(block!=this)return false
        return true
    }

    fun canAddAt(world: ServerLevel, pos: BlockPos): Boolean{
        val bstate=world.getBlockState(pos)
        val block=bstate.block
        return bstate.isAir || block==this
    }

    override fun getShape(state: BlockState, world: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape {
        return shape
    }

    override fun getSpiritContent(slot: Int, world: Level, pos: BlockPos): Sequence<Spirit>
        = (world.getBlockEntity(pos) as? BlockSpiritContainer)?.getSpiritContent(slot,world,pos)
                ?: listOf<Spirit>().asSequence()

    override fun getSlotCount(): Int = slot_count
}
