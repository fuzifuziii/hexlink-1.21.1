package jempasam.hexlink.world

import jempasam.hexlink.HexlinkMod
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.saveddata.SavedData
import java.util.UUID

/**
 * Per-level storage for mage ranks.
 *
 * Replaces the Fabric cardinal-components `LevelComponent`: NeoForge has no equivalent
 * component API for level data, but a [SavedData] attached to the overworld's
 * [net.minecraft.world.level.storage.DimensionDataStorage] gives the same lifetime and
 * NBT persistence.
 */
class HexlinkLevelData : SavedData(), LevelRanks {

    companion object {
        private const val DATA_NAME = "${HexlinkMod.MODID}_world_data"
        private const val RANKS_KEY = "ranks"

        private val FACTORY = Factory(
            ::HexlinkLevelData,
            { tag, provider -> HexlinkLevelData().apply { load(tag, provider) } }
        )

        /** The rank store for [level]'s server, creating it on first access. */
        fun get(level: ServerLevel): HexlinkLevelData =
            level.server.overworld().dataStorage.computeIfAbsent(FACTORY, DATA_NAME)
    }

    // Ranks
    private val rankMap = mutableMapOf<UUID, LevelRanks.Rank>()
    private val reverse_rank_map = mutableMapOf<LevelRanks.Rank, UUID>()

    override fun ranks(): Collection<LevelRanks.Rank> = rankMap.values

    override fun getPlayer(rank: LevelRanks.Rank): UUID? = reverse_rank_map[rank]

    override fun getRank(player: UUID): LevelRanks.Rank? = rankMap[player]

    override fun setPlayer(rank: LevelRanks.Rank, player: UUID?) {
        val oldPlayer = reverse_rank_map[rank]
        if (oldPlayer == player) return
        if (oldPlayer != null) {
            rankMap.remove(oldPlayer)
        }
        if (player != null) {
            val oldRank = rankMap[player]
            if (oldRank != null) reverse_rank_map.remove(oldRank)
            reverse_rank_map.put(rank, player)
            rankMap.put(player, rank)
        } else reverse_rank_map.remove(rank)
        setDirty()
    }

    override fun setRank(player: UUID, rank: LevelRanks.Rank?) {
        val oldRank = rankMap[player]
        if (oldRank == rank) return
        if (oldRank != null) {
            rankMap.remove(player)
        }
        if (rank != null) {
            val oldPlayer = reverse_rank_map[rank]
            if (oldPlayer != null) rankMap.remove(oldPlayer)
            rankMap.put(player, rank)
            reverse_rank_map.put(rank, player)
        } else rankMap.remove(player)
        setDirty()
    }

    override fun clearRanks() {
        rankMap.clear()
        reverse_rank_map.clear()
        setDirty()
    }

    private fun load(tag: CompoundTag, provider: HolderLookup.Provider) {
        tag.getCompound(RANKS_KEY)?.let { readRanksFromNbt(it) }
    }

    override fun save(tag: CompoundTag, provider: HolderLookup.Provider): CompoundTag {
        tag.put(RANKS_KEY, CompoundTag().also { writeRanksToNbt(it) })
        return tag
    }
}
