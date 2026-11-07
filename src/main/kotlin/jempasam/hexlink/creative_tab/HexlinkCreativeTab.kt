package jempasam.hexlink.creative_tab

import jempasam.hexlink.HexlinkMod
import jempasam.hexlink.HexlinkRegistry
import jempasam.hexlink.item.HexlinkItems
import jempasam.hexlink.spirit.SpecialSpirit
import net.minecraft.core.Registry
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.ItemStack

object HexlinkCreativeTab {

    private val TAB_KEY = ResourceLocation.fromNamespaceAndPath(HexlinkMod.MODID, "hexlink")

    val MAIN_TAB: CreativeModeTab = CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.${HexlinkMod.MODID}.hexlink"))
        .icon { HexlinkItems.MediumBag.defaultInstance }
        .displayItems { _, output -> fill(output) }
        .build()

    /** Called from the NeoForge `RegisterEvent` for `Registries.CREATIVE_MODE_TAB`. */
    fun register(registry: Registry<CreativeModeTab>) {
        Registry.register(registry, TAB_KEY, MAIN_TAB)
    }

    private fun fill(output: CreativeModeTab.Output) {
        val spirits = HexlinkRegistry.SPECIAL_SPIRIT.entrySet.map { it.value }.toList()
        output.accept(HexlinkItems.BigBag.defaultInstance)
        output.accept(HexlinkItems.MediumBag.defaultInstance)
        output.accept(HexlinkItems.SmallBag.defaultInstance)
        output.accept(HexlinkItems.Tablet.defaultInstance)

        if (spirits.isNotEmpty()) {
            output.accept(HexlinkItems.PhilosophicalCrystal.let {
                val stack = it.defaultInstance
                it.editSpirits(stack) { list ->
                    list.add(SpecialSpirit(spirits.random()))
                    list.add(SpecialSpirit(spirits.random()))
                }
                stack
            })

            output.accept(HexlinkItems.HauntedCrystal.let {
                val stack = it.defaultInstance
                it.editSpirits(stack) { list ->
                    list.add(SpecialSpirit(spirits.random()))
                    list.add(SpecialSpirit(spirits.random()))
                }
                stack
            })
        }

        for (extractor in HexlinkRegistry.EXTRACTOR.entrySet) {
            val stack = HexlinkItems.Crystal.defaultInstance
            HexlinkItems.Crystal.setExtractor(stack, extractor.value)
            output.accept(stack)
        }

        for (type in HexlinkRegistry.SPECIAL_SPIRIT.entrySet) {
            val stack = HexlinkItems.Spirit.defaultInstance
            HexlinkItems.Spirit.setSpirit(stack, SpecialSpirit(type.value))
            output.accept(stack)
        }
    }
}
