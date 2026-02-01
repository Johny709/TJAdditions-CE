package tj.integration.ae2.tile.misc;

import appeng.api.definitions.IParts;
import appeng.core.Api;
import appeng.fluids.parts.PartFluidInterface;
import net.minecraft.item.ItemStack;
import tj.integration.ae2.IApiParts;

public class TJPartFluidInterface extends PartFluidInterface {
    public TJPartFluidInterface(ItemStack is) {
        super(is);
    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return ((IApiParts) (IParts) Api.INSTANCE.definitions().parts()).getSuperFluidInterface().maybeStack(1).orElse(ItemStack.EMPTY);
    }
}
