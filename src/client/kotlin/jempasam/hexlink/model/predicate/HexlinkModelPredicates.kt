package jempasam.hexlink.model.predicate

import at.petrak.hexcasting.api.item.IotaHolderItem
import jempasam.hexlink.HexlinkMod
import jempasam.hexlink.item.HexlinkItems
import jempasam.hexlink.item.functionnality.ItemSpiritSource
import net.minecraft.client.renderer.item.ItemProperties
import net.minecraft.resources.ResourceLocation

object HexlinkModelPredicates {
    fun registerItemPredicates(){
        for(item_entry in HexlinkItems.items){
            if(item_entry.value is IotaHolderItem){
                ItemProperties.register(item_entry.value, ResourceLocation.fromNamespaceAndPath(HexlinkMod.MODID, "is_filled"), FilledIotaHolderPredicate)
            }
            if(item_entry.value is ItemSpiritSource){
                ItemProperties.register(item_entry.value, ResourceLocation.fromNamespaceAndPath(HexlinkMod.MODID, "source_filled"), FilledSpiritSourcePredicate)
            }
        }
    }
}