package jempasam.hexlink.spirit

import jempasam.hexlink.HexlinkMod
import net.minecraft.Util
import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.core.HolderSet
import net.minecraft.core.Vec3i
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.entity.MobSpawnType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature
import net.minecraft.world.level.levelgen.placement.PlacedFeature
import net.minecraft.world.level.levelgen.placement.PlacementContext
import net.minecraft.world.level.levelgen.placement.PlacementModifierType
import net.minecraft.world.phys.Vec3
import java.util.Optional


class BiomeSpirit(val biome: Holder<Biome>): Spirit{

    companion object{
        const val VALIDATION_SQUARE_DISTANCE=400.0f // Distance of 20
        const val MAX_TRY_COUNT=5
    }

    fun placeAll(world: ServerLevel, center: BlockPos, positions: List<BlockPos>, feature: ConfiguredFeature<*,*>, rand: RandomSource): Int{
        if(positions.isEmpty())return 0

        var spawnCount=0
        var minx=Int.MAX_VALUE
        var miny=Int.MAX_VALUE
        var minz=Int.MAX_VALUE
        var maxx=Int.MIN_VALUE
        var maxy=Int.MIN_VALUE
        var maxz=Int.MIN_VALUE
        for(pos in positions){
            if(pos.x<minx)minx=pos.x
            if(pos.y<miny)miny=pos.y
            if(pos.z<minz)minz=pos.z
            if(pos.x>maxx)maxx=pos.x
            if(pos.y>maxy)maxy=pos.y
            if(pos.z>maxz)maxz=pos.z
        }

        val offset=Vec3i(
                center.x-(minx+(maxx-minx)/2),
                center.y-(miny+(maxy-miny)/2),
                center.z-(minz+(maxz-minz)/2)
        )

        for(spawnPos in positions){
            val squareDistance=spawnPos.distSqr(center)
            if(squareDistance< VALIDATION_SQUARE_DISTANCE){
                if(feature.place(world, world.chunkSource.generator, rand, spawnPos.offset(offset)))
                    spawnCount++
            }
        }

        return spawnCount
    }

    fun generateFeature(world: ServerLevel, startPos: BlockPos, feature: PlacedFeature, rand: RandomSource): Boolean{
        val placements=feature.placement()
        val configured=feature.feature().value()

        // Get structure positions
        var positions= listOf(startPos)
        val context=PlacementContext(world, world.chunkSource.generator, Optional.of(feature))
        for(placement in placements){
            val newpositions=mutableListOf<BlockPos>()
            if(placement.type()!= PlacementModifierType.BIOME_FILTER){
                for(pos in positions){
                    for(newpos in placement.getPositions(context, rand, pos)){
                        newpositions.add(newpos)
                    }
                }
                positions=newpositions
                if(positions.isEmpty())break
            }
        }

        // Try Spawning Features
        val spawnCount=placeAll(world, startPos, positions, configured, rand)
        return spawnCount>1
    }

    fun generateRandomFeature(world: ServerLevel, pos: BlockPos, features: HolderSet<PlacedFeature>, rand: RandomSource): Boolean{
        var tryCount=0
        var selected=rand.nextInt(features.size())
        while(tryCount< MAX_TRY_COUNT){
            val feature=features.get(selected%features.size())
            HexlinkMod.logger.info("Try "+feature.unwrapKey().get())
            if(generateFeature(world, pos, feature.value(), rand)){
                HexlinkMod.logger.info("Success with "+feature.unwrapKey().get())
                return true
            }
            selected++
            tryCount++
        }
        return false
    }

    fun generateFeatures(world: ServerLevel, position: BlockPos, number: Int, rand: RandomSource){
        val groups=biome.value().generationSettings.features()
        if(groups.isEmpty())return
        var seq=0
        for(group in groups){
            if(group.size()==0)continue
            for(repetition in 0 until number){
                val offsetSpace=number-1
                val offset=Vec3i(
                        rand.nextIntBetweenInclusive(-offsetSpace,offsetSpace+1),
                        0,
                        rand.nextIntBetweenInclusive(-offsetSpace,offsetSpace+1)
                )
                val finalPosition=position.offset(offset)
                if(!generateRandomFeature(world,finalPosition,group,rand))
                    HexlinkMod.logger.info("Biome Spirit generation structure n$repetition of step $seq fail")
                seq++
            }
        }
    }

    fun generateSnowAndIce(world: ServerLevel, position: BlockPos){
        val finalpos=position.above()
        if(biome.value().shouldSnow(world,finalpos)) world.setBlock(finalpos, Blocks.SNOW.defaultBlockState(), 3)
        if(biome.value().shouldFreeze(world,finalpos)) world.setBlock(finalpos, Blocks.ICE.defaultBlockState(), 3)
    }

    fun generateMob(world: ServerLevel, position: BlockPos, number: Int, rand: RandomSource){
        val finalpos=position.above()
        val spawnEntries=biome.value().mobSettings.getMobs(MobCategory.CREATURE)
        if(spawnEntries.isEmpty)return
        for(i in 0 until number){
            val spawnEntry= spawnEntries.getRandom(rand).orElse(null) ?: continue
            spawnEntry.type.spawn(world, finalpos, MobSpawnType.SPAWNER)
        }
    }

    fun generateAll(world: ServerLevel, position: BlockPos, number: Int){
        val rand=RandomSource.create()
        generateFeatures(world,position,number,rand)
        generateMob(world,position,number,rand)
        generateSnowAndIce(world,position)
    }



    override fun manifestAt(caster: Player, world: ServerLevel, position: Vec3, count: Int): Spirit.Manifestation {
        if(!world.getBlockState(BlockPos.containing(position).above()).isAir)
            return Spirit.NONE_MANIFESTATION
        else
            return Spirit.Manifestation(1,count){
                generateAll(world, BlockPos.containing(position), it)
            }
    }

    override fun manifestIn(caster: Player, world: ServerLevel, entity: Entity, count: Int): Spirit.Manifestation {
        return manifestAt(caster,world,entity.position().add(0.0,-0.5,0.0),count)
    }



    override fun lookAt(caster: Player, world: ServerLevel, position: Vec3): Boolean {
        return world.getBiome(BlockPos.containing(position)).value()==biome.value()
    }

    override fun lookIn(caster: Player, world: ServerLevel, entity: Entity): Boolean {
        return lookAt(caster, world, entity.position())
    }

    override fun getColor(): Int = biome.value().foliageColor

    override fun getName(): Component
        = Component.translatable(Util.makeDescriptionId("biome", biome.unwrapKey().get().location()))

    override fun equals(other: Any?): Boolean = other is BiomeSpirit && other.biome==biome

    override fun hashCode(): Int = biome.hashCode()


    override fun serialize(): Tag {
        return StringTag.valueOf(biome.unwrapKey().get().location().toString())
    }

    override fun getType(): Spirit.SpiritType<*> = Type

    object Type: Spirit.SpiritType<BiomeSpirit>{
        override fun getName(): Component = Component.translatable("hexlink.spirit.biome")

        override fun deserialize(nbt: Tag): BiomeSpirit? {
            if(nbt !is StringTag) return null
            val id = ResourceLocation.tryParse(nbt.asString) ?: return null
            val registry = jempasam.hexlink.utils.HexlinkRegistryAccess.biomes() ?: return null
            val biome = registry.getHolder(ResourceKey.create(Registries.BIOME, id))
            if(biome.isEmpty)return null
            return BiomeSpirit(biome.get())
        }
    }
}
