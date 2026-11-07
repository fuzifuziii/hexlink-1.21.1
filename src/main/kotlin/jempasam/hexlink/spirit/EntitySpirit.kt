package jempasam.hexlink.spirit

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.FastColor
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.SpawnEggItem
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import kotlin.math.sin

class EntitySpirit(val entityType: EntityType<*>): Spirit {

    // Spawn At
    fun spawn(world: Level, position: Vec3, count: Int, modifier: (Int, Entity)->Unit): Spirit.Manifestation{
        if(!entityType.canSummon())
            return Spirit.NONE_MANIFESTATION
        else
            return Spirit.Manifestation(1,count){
                for(i in 0..<it){
                    try {
                        val summoned = entityType.create(world)
                        if (summoned != null) {
                            summoned.setPos(position)
                            modifier(i, summoned)
                            world.addFreshEntity(summoned)
                        }
                    }
                    catch (e: Exception){
                        //TODO Find something better
                        world.explode(null, position.x, position.y, position.z, 1f, Level.ExplosionInteraction.NONE)
                    }
                }
            }
    }

    override fun manifestAt(caster: Player, world: ServerLevel, position: Vec3, count: Int): Spirit.Manifestation {
        return spawn(world, position, count){_,_->}
    }

    override fun manifestBetween(caster: Player, world: ServerLevel, from: Vec3, to: Vec3, count: Int): Spirit.Manifestation {
        var direction=to.subtract(from)
        if(direction.length()>5)direction=direction.normalize().scale(5.0)
        return spawn(world, from, count){ i,e->
            e.setDeltaMovement(direction.x, direction.y+i*0.5, direction.z)
        }
    }

    override fun manifestBetween(caster: Player, world: ServerLevel, from: Entity, to: Vec3, count: Int): Spirit.Manifestation {
        return spawn(world, to, count){ _, e ->
            if(from is LivingEntity && e is LivingEntity)from.lastHurtByMob=e
        }
    }



    // Spawn Riding
    fun spawn(world: Level, rided: Entity, count: Int, modifier: (Entity)->Unit): Spirit.Manifestation{
        if(!entityType.canSummon())
            return Spirit.NONE_MANIFESTATION
        else
            return Spirit.Manifestation(1,count){
                var riding=rided
                for(i in 0..<it){
                    val summoned = entityType.create(world)
                    if(summoned!=null) {
                        summoned.setPos(riding.position())
                        summoned.deltaMovement=riding.deltaMovement
                        world.addFreshEntity(summoned)
                        riding.startRiding(summoned)
                        riding=summoned
                    }
                }
                modifier(riding)
            }
    }

    override fun manifestIn(caster: Player, world: ServerLevel, entity: Entity, count: Int): Spirit.Manifestation {
        return spawn(world, entity, count){}
    }

    override fun manifestBetween(caster: Player, world: ServerLevel, from: Entity, to: Entity, count: Int): Spirit.Manifestation {
        return spawn(world, from, count){
            if(it is LivingEntity && to is LivingEntity)it.lastHurtByMob=to
        }
    }

    override fun lookIn(caster: Player, world: ServerLevel, entity: Entity): Boolean {
        return entity.type==entityType
    }

    override fun lookAt(caster: Player, world: ServerLevel, position: Vec3): Boolean {
        val area = AABB(position.x-0.35, position.y-0.35, position.z-0.35, position.x+0.35, position.y+0.35, position.z+0.35)
        return world.getEntities(entityType, area) { true }.isNotEmpty()
    }



    override fun equals(other: Any?): Boolean = other is EntitySpirit && entityType===other.entityType

    override fun hashCode(): Int = entityType.hashCode()*36



    override fun getColor(): Int{
        val egg=SpawnEggItem.byId(entityType)
        if(egg!=null){
            val forward=sin((System.currentTimeMillis()%2000)/2000f*Math.PI)
            val revert=1f-forward
            val tint1=egg.getColor(0)
            val tint2=egg.getColor(1)
            return FastColor.ARGB32.color(
                    255,
                    (FastColor.ARGB32.red(tint1)*forward+FastColor.ARGB32.red(tint2)*revert).toInt(),
                    (FastColor.ARGB32.green(tint1)*forward+FastColor.ARGB32.green(tint2)*revert).toInt(),
                    (FastColor.ARGB32.blue(tint1)*forward+FastColor.ARGB32.blue(tint2)*revert).toInt()
            )
        }
        else if(entityType.fireImmune())return DyeColor.ORANGE.getFireworkColor()
        else{
            val color= from_group_to_color[entityType.category]
            if(color!=null)return color
            else return DyeColor.RED.getFireworkColor()
        }
    }

    override fun getName(): Component = entityType.description

    override fun serialize(): Tag {
        return StringTag.valueOf(BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString())
    }



    override fun getType(): Spirit.SpiritType<*> = Type

    object Type: Spirit.SpiritType<EntitySpirit>{
        override fun getName(): Component = Component.translatable("hexlink.spirit.entity")

        override fun deserialize(nbt: Tag): EntitySpirit? {
            if(nbt !is StringTag) throw IllegalArgumentException()
            val id = ResourceLocation.tryParse(nbt.asString) ?: return null
            val type = BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(null) ?: return null
            return EntitySpirit(type)
        }
    }

    companion object{
        private val from_group_to_color= mapOf(
                MobCategory.MONSTER to DyeColor.RED.getFireworkColor(),
                MobCategory.CREATURE to DyeColor.LIME.getFireworkColor(),
                MobCategory.AMBIENT to DyeColor.WHITE.getFireworkColor(),
                MobCategory.AXOLOTLS to DyeColor.PINK.getFireworkColor(),
                MobCategory.UNDERGROUND_WATER_CREATURE to DyeColor.BLUE.getFireworkColor(),
                MobCategory.WATER_CREATURE to DyeColor.LIGHT_BLUE.getFireworkColor(),
                MobCategory.WATER_AMBIENT to DyeColor.LIGHT_BLUE.getFireworkColor(),
                MobCategory.MISC to DyeColor.PURPLE.getFireworkColor()
        )
    }
}
