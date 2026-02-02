package tj.integration.appeng.tile.misc;

import appeng.api.definitions.IParts;
import appeng.api.parts.IPartModel;
import appeng.core.Api;
import appeng.core.AppEng;
import appeng.fluids.parts.PartFluidInterface;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import tj.integration.appeng.IApiParts;
import tj.integration.appeng.helpers.TJDualityFluidInterface;


public class TJPartFluidInterface extends PartFluidInterface {

    public static final ResourceLocation MODEL_BASE = new ResourceLocation(AppEng.MOD_ID, "part/super_fluid_interface_base");

    @PartModels
    public static final PartModel MODELS_OFF = new PartModel(MODEL_BASE, new ResourceLocation(AppEng.MOD_ID, "part/super_fluid_interface_off"));

    @PartModels
    public static final PartModel MODELS_ON = new PartModel(MODEL_BASE, new ResourceLocation(AppEng.MOD_ID, "part/super_fluid_interface_on"));

    @PartModels
    public static final PartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, new ResourceLocation(AppEng.MOD_ID, "part/super_fluid_interface_has_channel"));

    public TJPartFluidInterface(ItemStack is) {
        super(is);
        ObfuscationReflectionHelper.setPrivateValue(PartFluidInterface.class, this, new TJDualityFluidInterface(this.getProxy(), this), "duality");
    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return ((IApiParts) (IParts) Api.INSTANCE.definitions().parts()).getSuperFluidInterface().maybeStack(1).orElse(ItemStack.EMPTY);
    }

    @Override
    public IPartModel getStaticModels() {
        if (this.isActive() && this.isPowered()) {
            return MODELS_HAS_CHANNEL;
        } else if (this.isPowered()) {
            return MODELS_ON;
        } else {
            return MODELS_OFF;
        }
    }
}
