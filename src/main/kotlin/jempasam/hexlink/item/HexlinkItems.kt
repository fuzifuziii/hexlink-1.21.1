package jempasam.hexlink.item

import at.petrak.hexcasting.common.items.ItemStaff
import jempasam.hexlink.HexlinkMod
import jempasam.hexlink.block.HexlinkBlocks
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Rarity
import net.minecraft.core.Registry

object HexlinkItems {

    var items=HashMap<String,Item>()

    fun <T: Item>make(id: String, item: T): T{
        items.put(id, item)
        return item
    }

    /** Called from the NeoForge `RegisterEvent` for `Registries.ITEM`. */
    fun registerAll(registry: Registry<Item>){
        for(entry in items) Registry.register(registry, ResourceLocation.fromNamespaceAndPath(HexlinkMod.MODID, entry.key), entry.value)
    }

    val Crystal= make("crystal", ExtractorCatalyzerItem(epicProps()))

    val Spirit=make("spirit",SingleSpiritContainerItem(stackableProps()))
    val PhilosophicalCrystal=make("philosophical_crystal",InfiniteSpiritItem(epicProps()))
    val HauntedCrystal=make("haunted_crystal",RandomSpiritItem(epicProps().durability(100)))
    val Tablet=make("tablet",SoulContainerItem(simpleProps(), 1, 10))
    val SmallBag=make("small_bag",SoulContainerItem(simpleProps(), 2, 30))
    val MediumBag=make("medium_bag",SoulContainerItem(simpleProps(), 5, 100))
    val BigBag=make("big_bag",SoulContainerItem(simpleProps(), 10, 300))



    fun epicProps(): Item.Properties{
        return Item.Properties()
                                .stacksTo(1)
                .rarity(Rarity.EPIC)
    }

    fun simpleProps(): Item.Properties{
        return Item.Properties()
                                .stacksTo(1)
                .rarity(Rarity.COMMON)
    }

    fun forbiddenProps(): Item.Properties{
        return Item.Properties()
                .fireResistant()
                .stacksTo(1)
                .rarity(Rarity.RARE)
    }

    fun stackableProps(): Item.Properties{
        return Item.Properties()
                                .stacksTo(64)
                .rarity(Rarity.COMMON)
    }
}