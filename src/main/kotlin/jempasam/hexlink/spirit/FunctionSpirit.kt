package jempasam.hexlink.spirit

import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.Vec3
import java.util.*

class FunctionSpirit(val manifestAt: ResourceLocation, val manifestIn: ResourceLocation, val manifestAtCost: Int, val manifestInCost: Int) : Spirit{

    private val regex=Regex.fromLiteral("{_[a-z],[A-Z]}")
    private val name=manifestAt.path
            .substringAfter("/")
            .replace(regex){" "+it.value.uppercase()}

    override fun manifestAt(caster: Player, world: ServerLevel, position: Vec3, count: Int): Spirit.Manifestation {
        val function = world.server.functions.get(manifestAt)
        if(function.isEmpty)
            return Spirit.NONE_MANIFESTATION
        else
            return Spirit.Manifestation(manifestAtCost, count){
                for(i in 0..<it){
                    val source = caster.createCommandSourceStack()
                        .withSuppressedOutput()
                        .withPosition(position)
                        .withLevel(world)
                    world.server.functions.execute(function.get(), source)
                }
            }
    }

    override fun manifestIn(caster: Player, world: ServerLevel, entity: Entity, count: Int): Spirit.Manifestation {
        val function = world.server.functions.get(manifestIn)
        if(function.isEmpty)
            return Spirit.NONE_MANIFESTATION
        else
            return Spirit.Manifestation(manifestInCost, count){
                for(i in 0..<it){
                    val source = caster.createCommandSourceStack()
                        .withSuppressedOutput()
                        .withEntity(entity)
                        .withPosition(entity.position())
                        .withLevel(world)
                    world.server.functions.execute(function.get(), source)
                }
            }
    }

    override fun lookAt(caster: Player, world: ServerLevel, position: Vec3): Boolean = false

    override fun lookIn(caster: Player, world: ServerLevel, entity: Entity): Boolean = false

    override fun getName(): Component = Component.literal(name)

    override fun getColor(): Int = DyeColor.ORANGE.getFireworkColor()

    override fun equals(other: Any?): Boolean
        = other is FunctionSpirit &&
            other.manifestAt==manifestAt && other.manifestIn==manifestIn &&
            other.manifestAtCost==manifestAtCost && other.manifestInCost==manifestInCost

    override fun hashCode(): Int
        = Objects.hash(manifestIn, manifestAt, manifestInCost, manifestAtCost)

    override fun getType(): Spirit.SpiritType<*> = Type

    override fun serialize(): CompoundTag = CompoundTag().apply {
        putString("manifestAt", manifestAt.toString())
        putString("manifestIn", manifestIn.toString())
        putInt("manifestAtCost", manifestAtCost)
        putInt("manifestInCost", manifestInCost)
    }



    object Type : Spirit.SpiritType<FunctionSpirit>{
        override fun getName(): Component = Component.translatable("hexlink.spirit.function")

        override fun deserialize(nbt: Tag): FunctionSpirit? {
            if(nbt is CompoundTag){
                return FunctionSpirit(
                        ResourceLocation.parse(nbt.getString("manifestAt")),
                        ResourceLocation.parse(nbt.getString("manifestIn")),
                        nbt.getInt("manifestAtCost"),
                        nbt.getInt("manifestInCost")
                )
            }
            return null
        }
    }

}