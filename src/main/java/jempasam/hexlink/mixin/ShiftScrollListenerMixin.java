package jempasam.hexlink.mixin;

import at.petrak.hexcasting.client.ShiftScrollListener;
import jempasam.hexlink.item.functionnality.ItemScrollable;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Lets HexCasting's client scroll listener treat Hexlink's scrollable items as scrollable. */
@Mixin(ShiftScrollListener.class)
public class ShiftScrollListenerMixin {
    @Inject(at = @At("RETURN"), method = "IsScrollableItem", cancellable = true)
    private static void hexlink$isScrollableItem(Item item, CallbackInfoReturnable<Boolean> cir) {
        if (item instanceof ItemScrollable) cir.setReturnValue(true);
    }
}
