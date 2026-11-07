package jempasam.hexlink.item.color

import jempasam.hexlink.item.HexlinkItems
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent

object HexlinkColorProviders {
    fun registerItemColors(event: RegisterColorHandlersEvent.Item){
        event.register(SpiritContainerColor(), HexlinkItems.Tablet)

        event.register(ExtractorItemColor(), HexlinkItems.Crystal)
        event.register(SpiritContainerColor(), HexlinkItems.SmallBag)
        event.register(SpiritContainerColor(), HexlinkItems.MediumBag)
        event.register(SpiritContainerColor(), HexlinkItems.BigBag)

        event.register(SpiritSourceColor(0), HexlinkItems.Spirit)
        event.register(SpiritSourceColor(0), HexlinkItems.PhilosophicalCrystal)
        event.register(SpiritSourceColor(0), HexlinkItems.HauntedCrystal)
    }
}