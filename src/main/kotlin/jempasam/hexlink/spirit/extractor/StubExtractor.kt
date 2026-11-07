package jempasam.hexlink.spirit.extractor

import jempasam.hexlink.spirit.Spirit
import net.minecraft.world.entity.Entity
import net.minecraft.server.level.ServerPlayer
import net.minecraft.network.chat.Component

/**
 * Extract a Spirit Iota from a target entity
 */
class StubExtractor(private val name: Component, private val color: Int, private val cost: Int): SpiritExtractor<Spirit>{
    override fun extract(caster: ServerPlayer?, target: Entity) = SpiritExtractor.ExtractionResult(null as Spirit?, 0) {}

    override fun getName() = name

    override fun getColor() = color

    override fun getCost() = cost

}