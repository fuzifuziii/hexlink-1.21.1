package jempasam.hexlink.world

import jempasam.hexlink.HexlinkMod
import jempasam.hexlink.HexlinkRegistry
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtUtils
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import java.util.*

interface LevelRanks {

    fun getRank(player: UUID): Rank?
    fun setRank(player: UUID, rank: Rank?)

    fun getPlayer(rank: Rank): UUID?
    fun setPlayer(rank: Rank, player: UUID?)

    fun clearRanks()

    fun ranks(): Collection<Rank>

    class Rank(val manaCost: Float, val artifactBattery: Float, val color: Int){

        fun getName(): Component
            = HexlinkRegistry.RANK.getId(this)
                    ?.let{ Component.translatable("rank."+it.toLanguageKey()).setStyle(Style.EMPTY.withColor(color))}
                    ?: Component.translatable("hexlink.invalid_rank")
    }

    fun readRanksFromNbt(tag: CompoundTag) {
        clearRanks()
        for(key in tag.allKeys){
            try{
                val rank=HexlinkRegistry.RANK.get(ResourceLocation.parse(key))
                        ?: throw IllegalArgumentException("No rank associated to this id")

                val player=(tag.get(key) ?: throw IllegalArgumentException("Weird error"))
                        .let { NbtUtils.loadUUID(it) }

                setRank(player,rank)
            }catch (e: Exception){
                HexlinkMod.logger.error("While loading rank \"$key\": "+e.message)
            }
        }
    }

    fun writeRanksToNbt(tag: CompoundTag) {
        for(rank in ranks()){
            val player=getPlayer(rank)
            val id=HexlinkRegistry.RANK.getId(rank)
            if(player!=null && id!=null)
                tag.put(id.toString(), NbtUtils.createUUID(player))
        }
    }
}