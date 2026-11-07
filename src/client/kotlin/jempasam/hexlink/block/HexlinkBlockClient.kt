package jempasam.hexlink.block

import jempasam.hexlink.block.color.SpiritContainerBlockColor
import net.minecraft.client.renderer.ItemBlockRenderTypes
import net.minecraft.client.renderer.RenderType
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent

object HexlinkBlockClient {

    /** Cutout rendering is registered per block; NeoForge has no render-layer map. */
    fun registerBlockRender() {
    }

    fun registerBlockColors(event: RegisterColorHandlersEvent.Block) {
    }
}
