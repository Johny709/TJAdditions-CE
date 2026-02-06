package tj.mixin.ae2;

import appeng.api.networking.crafting.ICraftingLink;
import appeng.helpers.MultiCraftingTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = MultiCraftingTracker.class, remap = false)
public interface IMultiCraftingTrackerMixin {

    @Invoker("getSlot")
    int getTheSlot(ICraftingLink slot);

    @Invoker("isBusy")
    boolean isItBusy(int slot);
}
