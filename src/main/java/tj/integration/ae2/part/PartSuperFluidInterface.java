package tj.integration.ae2.part;

import appeng.api.parts.IPartModel;
import appeng.fluids.parts.PartFluidInterface;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import tj.TJ;
import tj.integration.ae2.helpers.TJDualityFluidInterface;
import tj.items.item.TJItems;

import javax.annotation.Nonnull;


public class PartSuperFluidInterface extends PartFluidInterface {

    public static final ResourceLocation MODEL_BASE = new ResourceLocation(TJ.MODID, "part/me.part.super_fluid_interface_base");

    @PartModels
    public static final PartModel MODELS_OFF = new PartModel(MODEL_BASE, new ResourceLocation(TJ.MODID, "part/me.part.super_fluid_interface_off"));

    @PartModels
    public static final PartModel MODELS_ON = new PartModel(MODEL_BASE, new ResourceLocation(TJ.MODID, "part/me.part.super_fluid_interface_on"));

    @PartModels
    public static final PartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, new ResourceLocation(TJ.MODID, "part/me.part.super_fluid_interface_has_channel"));

    public PartSuperFluidInterface(ItemStack is) {
        super(is);
        ObfuscationReflectionHelper.setPrivateValue(PartFluidInterface.class, this, new TJDualityFluidInterface(this.getProxy(), this), "duality");
    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return TJItems.PART_SUPER_FLUID_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY);
    }

    @Nonnull
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
