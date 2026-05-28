package tj.integration.ae2.part;

import appeng.api.parts.IPartModel;
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
import tj.TJ;
import tj.blocks.block.TJBlocks;
import tj.gui.TJGuiTextures;
import tj.gui.uifactory.ITileEntityUI;
import tj.gui.uifactory.TileEntityHolder;
import tj.gui.widgets.TJLabelWidget;
import tj.gui.widgets.TJSlotWidget;
import tj.gui.widgets.impl.TJPhantomItemSlotWidget;
import tj.integration.ae2.helpers.SuperDualityInterface;

import javax.annotation.Nonnull;


public class PartSuperInterface extends PartInterface implements ITileEntityUI {

    public static final ResourceLocation MODEL_BASE = new ResourceLocation(TJ.MODID, "part/me.part.super_interface_base");

    @PartModels
    public static final PartModel MODELS_OFF = new PartModel(MODEL_BASE, new ResourceLocation(TJ.MODID, "part/me.part.super_interface_off"));

    @PartModels
    public static final PartModel MODELS_ON = new PartModel(MODEL_BASE, new ResourceLocation(TJ.MODID, "part/me.part.super_interface_on"));

    @PartModels
    public static final PartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, new ResourceLocation(TJ.MODID, "part/me.part.super_interface_has_channel"));

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
        return TJBlocks.SUPER_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY);
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

    @Override
    public ModularUI createUI(TileEntityHolder holder, EntityPlayer player) {
        final WidgetGroup widgetGroup = new WidgetGroup();
        final DualityInterface duality = this.getInterfaceDuality();
        for (int i = 0; i < duality.getConfig().getSlots(); i++)
            widgetGroup.addWidget(new TJPhantomItemSlotWidget(7 + (18 * (i % 9)), 34 + (36 * (i / 9)), 18, 18, i, duality.getConfig())
                    .setBackgroundTextures(TJGuiTextures.SLOT_DOWN));
        for (int i = 0; i < duality.getStorage().getSlots(); i++)
            widgetGroup.addWidget(new TJSlotWidget<>(duality.getStorage(), i, 7 + (18 * (i % 9)), 52 + (36 * (i / 9)))
                    .setBackgroundTexture(GuiTextures.SLOT));
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 176, 292)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(this.getItemStackRepresentation()).setLocale(String.format("tile.%s.name", this.getTile())))
                .widget(widgetGroup)
                .bindPlayerInventory(player.inventory, 209)
                .build(holder, player);
    }
}
