package jempasam.hexlink.item

import jempasam.hexlink.spirit.Spirit
import jempasam.hexlink.spirit.inout.SpiritSource
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.util.RandomSource

class RandomSpiritItem(settings: Item.Properties): InfiniteSpiritItem(settings) {

    override fun getSpiritSource(stack: ItemStack): SpiritSource {
        return object: SpiritSource {
            override fun last(): Spirit?{
                return getSpirits(stack)
                    .run { if(size>0) this[RandomSource.create(System.currentTimeMillis()/1000).nextInt(this.size)] else null }
            }
            override fun all(): Set<Spirit> = last()?.let { setOf(it) } ?: emptySet()
            override fun extract(count: Int, spirit: Spirit): SpiritSource.SpiritOutputFlux {
                val fcount= if(stack.isDamageableItem) minOf(count, stack.maxDamage-stack.damageValue) else count
                if(fcount>0 && getSpirits(stack).contains(spirit))return SpiritSource.SpiritOutputFlux(fcount) { used ->
                    if(stack.isDamageableItem) stack.setDamageValue(minOf(stack.maxDamage, stack.damageValue + used))
                }
                else return SpiritSource.NONE.FLUX
            }
        }
    }

}