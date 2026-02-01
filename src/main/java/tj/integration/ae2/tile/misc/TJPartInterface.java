package tj.integration.ae2.tile.misc;

import appeng.api.definitions.IParts;
import appeng.core.Api;
import appeng.parts.misc.PartInterface;
import net.minecraft.item.ItemStack;
import tj.integration.ae2.IApiParts;

public class TJPartInterface extends PartInterface {

    public TJPartInterface(ItemStack is) {
        super(is);
    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return ((IApiParts) (IParts) Api.INSTANCE.definitions().parts()).getSuperInterface().maybeStack(1).orElse(ItemStack.EMPTY);
    }
}
