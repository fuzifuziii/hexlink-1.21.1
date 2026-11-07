package jempasam.hexlink.spirit

import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.mojang.serialization.codecs.RecordCodecBuilder
import jempasam.hexlink.HexlinkMod
import jempasam.hexlink.HexlinkRegistry
import jempasam.hexlink.utils.NbtHelper
import jempasam.hexlink.utils.asNBT
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel
import net.minecraft.network.chat.Component
import net.minecraft.world.phys.Vec3

/**
 * A Spirit Type
 */
interface Spirit {

    /**
     * Manifest the spirit at a location.
     * @param caster The caster of the spell
     * @param world The world where the spell is cast
     * @param position The location
     * @param count Spirit count used in the infusion
     */
    fun manifestAt(caster: Player, world: ServerLevel, position: Vec3, count: Int): Manifestation

    /**
     * Manifest the spirit in an entity.
     * @param caster The caster of the spell
     * @param world The world where the spell is cast
     * @param entity The entity
     * @param count Spirit count used in the infusion
     */
    fun manifestIn(caster: Player, world: ServerLevel, entity: Entity, count: Int): Manifestation

    /**
     * Manifest the spirit as a link between two location.
     * @param caster The caster of the spell
     * @param world The world where the spell is cast
     * @param from First location
     * @param to Second location
     * @param count Spirit count used in the infusion
     */
    fun manifestBetween(caster: Player, world: ServerLevel, from: Vec3, to: Vec3, count: Int): Manifestation{
        return manifestAt(caster,world,from,count)
    }

    /**
     * Manifest the spirit as a link between two entities.
     * @param caster The caster of the spell
     * @param world The world where the spell is cast
     * @param from First entity
     * @param to Second entity
     * @param count Spirit count used in the infusion
     */
    fun manifestBetween(caster: Player, world: ServerLevel, from: Entity, to: Entity, count: Int): Manifestation{
        return manifestIn(caster,world,from,count)
    }

    /**
     * Manifest the spirit as a link between a location and an entity.
     * @param caster The caster of the spell
     * @param world The world where the spell is cast
     * @param from An entity
     * @param to A location
     * @param count Spirit count used in the infusion
     */
    fun manifestBetween(caster: Player, world: ServerLevel, from: Entity, to: Vec3, count: Int): Manifestation{
        return manifestIn(caster,world,from,count)
    }

    class Manifestation(val perSpiritCost:Int, val spiritCount:Int, val minimumCount:Int=0, private val action: (Int)->Unit){
        fun execute(count: Int){
            if(count>spiritCount){
                HexlinkMod.logger.warn("Try to manifest more spirit than the Manifestation maximum")
                action(spiritCount)
            }
            else if(minimumCount>count){
                HexlinkMod.logger.warn("Try to manifest less spirit than the Manifestation minimum")
            }
            else action(count)
        }

        fun mediaCost(count: Int) = count*perSpiritCost

        val maxMediaCost: Int get()=perSpiritCost*spiritCount
    }


    /**
     * Make the spirit look at a location
     */
    fun lookAt(caster: Player, world: ServerLevel, position: Vec3): Boolean

    /**
     * Make the spirit look at an entity
     */
    fun lookIn(caster: Player, world: ServerLevel, entity: Entity): Boolean

    override fun equals(other: Any?): Boolean

    override fun hashCode(): Int

    /**
     * Get spirit color
     */
    fun getColor(): Int

    /**
     * Get spirit name
     */
    fun getName(): Component

    fun serialize(): Tag

    fun getType(): SpiritType<*>


    interface SpiritType<T: Spirit>{
        fun deserialize(nbt: Tag): T?

        fun getName(): Component

        fun deserialize(obj: JsonObject): T{
            val element= obj.get("value") ?: throw JsonSyntaxException("Expected \"value\"")
            val ret= deserialize(element.asNBT()) ?: throw JsonSyntaxException("Invalid json")
            return ret
        }

    }


    companion object{

        const val CANNOT_USE=-1

        val NONE_MANIFESTATION=Manifestation(0,0){}

        val CODEC=RecordCodecBuilder.create<Spirit> { inst->
            inst.group(
                HexlinkRegistry.SPIRIT.codec .fieldOf("type") .forGetter{it.getType()},
                NbtHelper.ELEMENT_CODEC .fieldOf("value") .forGetter{it.serialize()}
            ).apply(inst){ type,value -> type.deserialize(value) }
        }
    }
}