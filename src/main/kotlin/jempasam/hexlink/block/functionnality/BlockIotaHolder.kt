package jempasam.hexlink.block.functionnality

import at.petrak.hexcasting.api.casting.iota.Iota
import net.minecraft.server.level.ServerLevel
import net.minecraft.core.BlockPos

interface BlockIotaHolder {
    fun getIota(blockpos: BlockPos, world: ServerLevel): Iota
}