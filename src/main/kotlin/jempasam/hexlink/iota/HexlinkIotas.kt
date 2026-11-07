package jempasam.hexlink.iota

import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import jempasam.hexlink.HexlinkMod
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation

object HexlinkIotas {

    private val IOTAS = mutableMapOf<ResourceLocation, IotaType<*>>()

    fun <T : Iota> make(id: String, type: IotaType<T>): IotaType<T> {
        IOTAS[ResourceLocation.fromNamespaceAndPath(HexlinkMod.MODID, id)] = type
        return type
    }

    /** Called from the NeoForge `RegisterEvent` for HexCasting's iota-type registry. */
    fun registerAll(registry: Registry<IotaType<*>>) {
        for ((id, type) in IOTAS) {
            Registry.register(registry, id, type)
        }
    }

    val SPIRIT = make("spirit", SpiritIota.Type)
}
