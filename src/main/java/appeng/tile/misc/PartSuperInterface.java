package appeng.tile.misc;

import appeng.api.definitions.IParts;
import appeng.api.parts.IPartModel;
import appeng.core.Api;
import appeng.core.AppEng;
import appeng.helpers.DualityInterface;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;
import appeng.parts.misc.PartInterface;
import appeng.tile.networking.TileCableBus;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.WidgetGroup;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import tj.gui.TJGuiTextures;
import tj.gui.uifactory.ITileEntityUI;
import tj.gui.uifactory.TileEntityHolder;
import tj.gui.widgets.TJSlotWidget;
import tj.gui.widgets.impl.TJPhantomSlotWidget;
import tj.integration.appeng.IApiParts;
import appeng.helpers.SuperDualityInterface;


public class PartSuperInterface extends PartInterface implements ITileEntityUI {

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
    public boolean onPartActivate(EntityPlayer player, EnumHand hand, Vec3d pos) {
        final TileCableBus tileCableBus = (TileCableBus) this.getTile();
        if (tileCableBus != null) {
            if (!player.getEntityWorld().isRemote) {
                TileEntityHolder holder = new TileEntityHolder(tileCableBus);
                holder.setFacing(this.getSide().getFacing());
                holder.openUI((EntityPlayerMP) player);
            }
            return true;
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

    @Override
    public ModularUI createUI(TileEntityHolder holder, EntityPlayer player) {
        WidgetGroup widgetGroup = new WidgetGroup();
        DualityInterface duality = this.getInterfaceDuality();
        for (int i = 0; i < duality.getConfig().getSlots(); i++)
            widgetGroup.addWidget(new TJPhantomSlotWidget(duality.getConfig(), i, 7 + (18 * (i % 9)), 34 + (36 * (i / 9)))
                    .setBackgroundTexture(TJGuiTextures.SLOT_DOWN));
        for (int i = 0; i < duality.getStorage().getSlots(); i++)
            widgetGroup.addWidget(new TJSlotWidget<>(duality.getStorage(), i, 7 + (18 * (i % 9)), 52 + (36 * (i / 9)))
                    .setBackgroundTexture(GuiTextures.SLOT));
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 176, 292)
                .widget(widgetGroup)
                .bindPlayerInventory(player.inventory, 209)
                .build(holder, player);
    }
}
