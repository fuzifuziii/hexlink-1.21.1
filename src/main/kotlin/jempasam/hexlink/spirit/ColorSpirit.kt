package jempasam.hexlink.spirit

import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.DyeItem
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.component.DyedItemColor
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.nbt.NumericTag
import net.minecraft.nbt.Tag
import net.minecraft.nbt.IntTag
import net.minecraft.core.particles.DustParticleOptions
import net.minecraft.server.level.ServerLevel
import net.minecraft.network.chat.Component
import net.minecraft.util.FastColor
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f

class ColorSpirit(private val color: Int) : Spirit {
    private val entry= find(color)

    override fun getColor(): Int = color

    override fun getName(): Component = Component.literal(entry.name)

    override fun equals(other: Any?): Boolean = other is ColorSpirit && color==other.color

    override fun hashCode(): Int = color*873

    fun colorize(stack: StackHelper.WorldStack?): Spirit.Manifestation{
        if(stack==null)return Spirit.NONE_MANIFESTATION
        // Only items that already carry a dye colour can be recoloured.
        if(stack.stack.get(DataComponents.DYED_COLOR)!=null){
            return Spirit.Manifestation(5,1){
                stack.stack.set(DataComponents.DYED_COLOR, DyedItemColor(color, true))
                stack.update()
            }
        }
        return Spirit.NONE_MANIFESTATION
    }

    override fun manifestAt(caster: Player, world: ServerLevel, position: Vec3, count: Int): Spirit.Manifestation{
        return Spirit.Manifestation(1,count){
            val color=Vector3f(
                    FastColor.ARGB32.red(color)/255f,
                    FastColor.ARGB32.green(color)/255f,
                    FastColor.ARGB32.blue(color)/255f
            )
            world.sendParticles(
                    DustParticleOptions(color, 1.0f*count),
                    position.x, position.y, position.z,
                    count,
                    0.1*count, 0.1*count, 0.1*count,
                    0.1+0.01*count
            )
        }
    }

    override fun manifestIn(caster: Player, world: ServerLevel, entity: Entity, count: Int): Spirit.Manifestation
        = colorize(StackHelper.stack(caster,entity))


    fun testColor(stack: StackHelper.WorldStack?): Boolean{
        if(stack==null)return false
        val dyed=stack.stack.get(DataComponents.DYED_COLOR) ?: return false
        return color==DyedItemColor.getOrDefault(stack.stack, dyed.rgb())
    }
    override fun lookAt(caster: Player, world: ServerLevel, position: Vec3): Boolean
            = testColor(StackHelper.stack(caster,world,position))

    override fun lookIn(caster: Player, world: ServerLevel, entity: Entity): Boolean
            = testColor(StackHelper.stack(caster,entity))



    override fun serialize(): Tag {
        return IntTag.valueOf(color)
    }

    override fun getType(): Spirit.SpiritType<*> = Type

    object Type: Spirit.SpiritType<ColorSpirit>{
        override fun getName(): Component = Component.translatable("hexlink.spirit.color")

        override fun deserialize(nbt: Tag): ColorSpirit?{
            return (nbt as? NumericTag)?.let { ColorSpirit(it.asInt) }
        }
    }


    companion object{
        private data class ColorCode(val r: Int, val g: Int, val b:Int)
        private data class ColorEntry(val dye: DyeItem, val name: String, val hash: Int)
        private fun color(r: Int, g: Int, b: Int, item: Item, name: String): Pair<ColorCode,ColorEntry>
            = ColorCode(r,g,b) to ColorEntry(item as DyeItem, name, r*100+g*10+b)

        private fun find(color: Int): ColorEntry = color_map[ColorCode(
                FastColor.ARGB32.red(color)/86,
                FastColor.ARGB32.green(color)/86,
                FastColor.ARGB32.blue(color)/86
            )] ?: invalid_color

        private val invalid_color=ColorEntry(Items.BLACK_DYE as DyeItem, "Invalid Color", 9999)
        private val color_map= mapOf(
            color(0,0,0, Items.BLACK_DYE, "Black"),
            color(0,0,1, Items.BLUE_DYE, "Dark Blue"),
            color(0,0,2, Items.BLUE_DYE, "Blue"),
            color(0,1,0, Items.GREEN_DYE, "Dark green"),
            color(0,1,1, Items.CYAN_DYE, "Dark Cyan"),
            color(0,1,2, Items.LIGHT_BLUE_DYE, "Sky Blue"),
            color(0,2,0, Items.LIME_DYE, "Green"),
            color(0,2,1, Items.LIME_DYE, "Mint"),
            color(0,2,2, Items.CYAN_DYE, "Cyan"),
            color(1,0,0, Items.RED_DYE, "Dark Red"),
            color(1,0,1, Items.PURPLE_DYE, "Purple"),
            color(1,0,2, Items.PURPLE_DYE, "Lavender"),
            color(1,1,0, Items.GREEN_DYE, "Kaki"),
            color(1,1,1, Items.BLACK_DYE, "Black"),
            color(1,1,2, Items.GRAY_DYE, "Black"),
            color(1,2,0, Items.LIME_DYE, "Lime"),
            color(1,2,1, Items.LIME_DYE, "Pale Lime"),
            color(1,2,2, Items.LIGHT_BLUE_DYE, "Light Blue"),
            color(2,0,0, Items.RED_DYE, "Red"),
            color(2,0,1, Items.PINK_DYE, "Fuschia"),
            color(2,0,2, Items.MAGENTA_DYE, "Magenta"),
            color(2,1,0, Items.ORANGE_DYE, "Orange"),
            color(2,1,1, Items.PINK_DYE, "Tulip"),
            color(2,1,2, Items.PINK_DYE, "Fuschia Pink"),
            color(2,2,0, Items.YELLOW_DYE, "Yellow"),
            color(2,2,1, Items.YELLOW_DYE, "Light Yellow"),
            color(2,2,2, Items.WHITE_DYE, "White")
        )
    }
}