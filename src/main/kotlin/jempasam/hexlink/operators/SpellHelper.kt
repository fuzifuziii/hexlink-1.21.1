package jempasam.hexlink.operators

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadBlock
import at.petrak.hexcasting.api.casting.mishaps.MishapBadEntity
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import jempasam.hexlink.iota.SpiritIota
import jempasam.hexlink.item.functionnality.ExtractorItem
import jempasam.hexlink.mishap.InvalidSpiritSource
import jempasam.hexlink.mishap.InvalidSpiritTarget
import jempasam.hexlink.spirit.Spirit
import jempasam.hexlink.spirit.StackHelper
import jempasam.hexlink.spirit.extractor.SpiritExtractor
import jempasam.hexlink.spirit.inout.SpiritHelper
import jempasam.hexlink.spirit.inout.SpiritSource
import jempasam.hexlink.spirit.inout.SpiritTarget
import net.minecraft.network.chat.Component
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3

fun <T>List<Iota>.makePosPair(ctx: CastingEnvironment, first: T, index: Int): Pair<T,Vec3>{
    val pos=iotaPosition(ctx, get(index))
    return first to pos
}



fun List<Iota>.getSpirit(index: Int, max: Int): Spirit{
    val ret= this[index]
    if(ret is SpiritIota)return ret.spirit
    else throw MishapInvalidIota(ret, max-index-1, Component.translatable("hexlink.spirit_iota"))
}

fun List<Iota>.getSpiritSourceOpt(ctx: CastingEnvironment, index: Int, max: Int): SpiritSource?{
    val source=when(val sourceIota=get(index)){
        is EntityIota ->{
            ctx.assertEntityInRange(sourceIota.resolveEntity(ctx))
            SpiritHelper.spiritSource(ctx.requireCaster(), sourceIota.resolveEntity(ctx))
        }
        is Vec3Iota ->{
            ctx.assertVecInRange(sourceIota.vec3)
            SpiritHelper.spiritSource(ctx.requireCaster(), ctx.world, sourceIota.vec3)
        }
        else -> throw MishapInvalidIota(get(index), max-index-1, Component.translatable("hexcasting.iota.hexcasting:entity").append(Component.translatable("hexlink.or")).append(Component.translatable("hexcasting.iota.hexcasting:vec3")))
    }
    return source
}

fun List<Iota>.getSpiritSource(ctx: CastingEnvironment, index: Int, max: Int): SpiritSource
    = getSpiritSourceOpt(ctx, index, max) ?: throw InvalidSpiritSource(get(index))


fun List<Iota>.getSpiritSourceAndPos(ctx: CastingEnvironment, index: Int, max: Int): Pair<SpiritSource,Vec3>
    = makePosPair(ctx, getSpiritSource(ctx,index,max), index)


fun List<Iota>.getSpiritTarget(ctx: CastingEnvironment, index: Int, max: Int): SpiritTarget{
    val target=when(val targetIota=get(index)){
        is EntityIota ->{
            ctx.assertEntityInRange(targetIota.resolveEntity(ctx))
            SpiritHelper.spiritTarget(ctx.requireCaster(), targetIota.resolveEntity(ctx))
        }
        is Vec3Iota ->{
            ctx.assertVecInRange(targetIota.vec3)
            SpiritHelper.spiritTarget(ctx.requireCaster(), ctx.world, targetIota.vec3)
        }
        else -> throw MishapInvalidIota(get(index), max-index-1, Component.translatable("hexcasting.iota.hexcasting:entity").append(Component.translatable("hexlink.or")).append(Component.translatable("hexcasting.iota.hexcasting:vec3")))
    }
    return target ?: throw InvalidSpiritTarget(get(index))
}

fun List<Iota>.getSpiritTargetAndPos(ctx: CastingEnvironment, index: Int, max: Int): Pair<SpiritTarget,Vec3>
        = makePosPair(ctx, getSpiritTarget(ctx,index,max), index)

fun List<Iota>.getExtractorItem(ctx: CastingEnvironment, index: Int, max: Int): SpiritExtractor<*>{
    val source=when(val iota=get(index)){
        is EntityIota ->{
            ctx.assertEntityInRange(iota.resolveEntity(ctx))
            val stack=StackHelper.stack(ctx.requireCaster(), iota.resolveEntity(ctx))?.stack
            val item=stack?.item
            val extractor=if(item is ExtractorItem) item.getExtractor(stack) else null
            extractor ?: throw MishapBadEntity(iota.resolveEntity(ctx), Component.translatable("hexlink.mishap.extractor_item"))
        }
        is Vec3Iota ->{
            ctx.assertVecInRange(iota.vec3)
            val stack=StackHelper.stack(ctx.requireCaster(), ctx.world, iota.vec3)?.stack
            val item=stack?.item
            val extractor=if(item is ExtractorItem) item.getExtractor(stack) else null
            extractor ?: throw MishapBadBlock(iota.vec3.toBlockPos(), Component.translatable("hexlink.mishap.extractor_item"))
        }
        else -> throw MishapInvalidIota(get(index), max-index-1, Component.translatable("hexcasting.iota.hexcasting:entity").append(Component.translatable("hexlink.or")).append(Component.translatable("hexcasting.iota.hexcasting:vec3")))
    }
    return source
}


fun List<Iota>.getVec3orEntity(ctx: CastingEnvironment, index: Int, max: Int): Any{
    return when(val iota=get(index)){
        is EntityIota ->{
            val entity = iota.resolveEntity(ctx)
            ctx.assertEntityInRange(entity)
            entity
        }
        is Vec3Iota ->{
            ctx.assertVecInRange(iota.vec3)
            iota.vec3
        }
        else -> throw MishapInvalidIota(get(index), max-index-1, Component.translatable("hexcasting.iota.hexcasting:entity").append(Component.translatable("hexlink.or")).append(Component.translatable("hexcasting.iota.hexcasting:vec3")))
    }
}

fun List<Iota>.getExtractorItemAndPos(ctx: CastingEnvironment, index: Int, max: Int): Pair<SpiritExtractor<*>,Vec3>
    = makePosPair(ctx, getExtractorItem(ctx,index,max),index)