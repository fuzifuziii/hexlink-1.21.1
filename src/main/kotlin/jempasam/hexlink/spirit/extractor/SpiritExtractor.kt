package jempasam.hexlink.spirit.extractor

import com.google.gson.JsonObject
import jempasam.hexlink.HexlinkMod
import jempasam.hexlink.spirit.Spirit
import net.minecraft.world.entity.Entity
import net.minecraft.server.level.ServerPlayer
import net.minecraft.network.chat.Component

/**
 * Extract a Spirit Iota from a target entity
 */
interface SpiritExtractor<T: Spirit>{


    /**
     * Try to extract spirits from an entity
     * @return The result containing the spirits extracted and that can consume the entity
     */
    fun extract(caster: ServerPlayer?, target: Entity): ExtractionResult<T>

    /**
     * Get a name describing extracted spirits
     */
    fun getName(): Component

    /**
     * Get a color representation of extracted spirits
     */
    fun getColor(): Int


    /**
     * Media cost
     */
    fun getCost(): Int



    /**
    * The result of an extraction
     * @param spirit The spirit extracted
     * @param maxCount The number of spirit extracted
     * @param consumer The method used to change or kill the entity after the spirit are extracted
     */
    class ExtractionResult<T: Spirit>(val spirit: T?, val maxCount: Int, private val consumer: (Int) -> Unit){
        /**
         * Consume the entity after extraction
         */
        fun consume(count: Int){
            if(count>maxCount){
                HexlinkMod.logger.warn("Try to extract more than max")
                return consumer(maxCount)
            }
            else return consumer(count)
        }

        fun downCast() = ExtractionResult<Spirit>(spirit, maxCount, consumer)
    }

    fun noResult(): ExtractionResult<T> = noResult<T>()

    companion object{
        fun <T: Spirit>noResult(): ExtractionResult<T> = ExtractionResult(null, 0) {}
    }

    interface Serializer<T: Spirit>{
        fun deserialize(obj: JsonObject)
    }

}