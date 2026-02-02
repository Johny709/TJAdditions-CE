package tj.integration.appeng.tile.misc;

import appeng.api.definitions.IParts;
import appeng.api.parts.IPartModel;
import appeng.core.Api;
import appeng.core.AppEng;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;
import appeng.parts.misc.PartInterface;
import appeng.util.Platform;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import tj.gui.TJGuiUtils;
import tj.integration.appeng.IApiParts;
import tj.integration.appeng.core.sync.TJGuiBridge;
import tj.integration.appeng.helpers.SuperDualityInterface;


public class PartSuperInterface extends PartInterface {

    public static final ResourceLocation MODEL_BASE = new ResourceLocation(AppEng.MOD_ID, "part/super_interface_base");

    @PartModels
    public static final PartModel MODELS_OFF = new PartModel(MODEL_BASE, new ResourceLocation(AppEng.MOD_ID, "part/super_interface_off"));

    @PartModels
    public static final PartModel MODELS_ON = new PartModel(MODEL_BASE, new ResourceLocation(AppEng.MOD_ID, "part/super_interface_on"));

    @PartModels
    public static final PartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, new ResourceLocation(AppEng.MOD_ID, "part/super_interface_has_channel"));

    public PartSuperInterface(ItemStack is) {
        super(is);
        ObfuscationReflectionHelper.setPrivateValue(PartInterface.class, this, new SuperDualityInterface(this.getProxy(), this), "duality");
    }

    @Override
    public boolean onPartActivate(EntityPlayer p, EnumHand hand, Vec3d pos) {
        if (Platform.isServer()) {
            TJGuiUtils.openAEGui(p, this.getTileEntity(), this.getSide(), TJGuiBridge.GUI_SUPER_INTERFACE);
        }
        return true;
    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return ((IApiParts) (IParts) Api.INSTANCE.definitions().parts()).getSuperInterface().maybeStack(1).orElse(ItemStack.EMPTY);
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
