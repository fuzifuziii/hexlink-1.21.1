package jempasam.hexlink.mixin;

import at.petrak.hexcasting.common.msgs.MsgShiftScrollC2S;
import jempasam.hexlink.item.functionnality.ItemScrollable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Forwards HexCasting's server-side shift-scroll handling to Hexlink's scrollable items. */
@Mixin(MsgShiftScrollC2S.class)
abstract class MsgShiftScrollSynMixin {

    @Inject(at = @At("TAIL"), method = "handleForHand")
    private void hexlink$handleForHand(ServerPlayer sender, InteractionHand hand, double delta, CallbackInfo info) {
        if (delta != 0) {
            var stack = sender.getItemInHand(hand);
            if (stack.getItem() instanceof ItemScrollable rollable) {
                rollable.roll(stack, sender, hand, delta);
            }
        }
    }
}
