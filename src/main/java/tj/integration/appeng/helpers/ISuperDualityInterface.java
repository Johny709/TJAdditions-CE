package tj.integration.appeng.helpers;

import appeng.api.config.Actionable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.InventoryAdaptor;
import appeng.util.inv.InvOperation;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public interface ISuperDualityInterface {

    boolean useThePlan(final int x, final IAEItemStack itemStack);

    InventoryAdaptor getTheAdaptor(final int slot);

    IAEItemStack injectTheCraftedItems(final ICraftingLink link, final IAEItemStack acquired, final Actionable mode);

    TickRateModulation tickingTheRequest(final IGridNode node, final int tickSinceLastCall);

    boolean updateTheStorage();

    void readTheConfig();

    void onChangeTheInventory(final IItemHandler inv, final int slot, final InvOperation mc, final ItemStack removed, final ItemStack added);
}
