package tj.integration.ae2.part;

import appeng.api.parts.IPartModel;
import appeng.core.Api;
import appeng.helpers.DualityInterface;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;
import appeng.parts.misc.PartInterface;
import appeng.tile.networking.TileCableBus;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.LabelWidget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.items.IItemHandler;
import tj.TJ;
import tj.gui.TJGuiTextures;
import tj.gui.uifactory.ITileEntityUI;
import tj.gui.uifactory.TileEntityHolder;
import tj.gui.widgets.ButtonWidget;
import tj.gui.widgets.NewTextFieldWidget;
import tj.gui.widgets.TJLabelWidget;
import tj.gui.widgets.impl.ButtonPopUpWidget;
import tj.gui.widgets.impl.SlotScrollableWidgetGroup;
import tj.gui.widgets.impl.TJSlotWidget;
import tj.gui.widgets.impl.TJPhantomItemSlotWidget;
import tj.integration.ae2.helpers.DualitySuperInterface;
import tj.items.item.TJItems;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;


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
        ObfuscationReflectionHelper.setPrivateValue(PartInterface.class, this, new DualitySuperInterface(this.getProxy(), this), "duality");
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
        return TJItems.PART_SUPER_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY);
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
        final DualityInterface duality = this.getInterfaceDuality();
        final SlotScrollableWidgetGroup scrollableWidgetGroup = new SlotScrollableWidgetGroup(7, 120, 166, 72, 9)
                .setScrollWidth(4);
        final IItemHandler patternHandler = duality.getInventoryByName("patterns");
        final IItemHandler upgradeHandler = duality.getInventoryByName("upgrades");
        final ModularUI.Builder builder = ModularUI.builder(TJGuiTextures.SUPER_INTERFACE, 211, 292);
        for (int i = 0; i < duality.getConfig().getSlots(); i++) {
            builder.widget(new TJPhantomItemSlotWidget(7 + (18 * (i % 9)), 34 + (36 * (i / 9)), 18, 18, i, duality.getConfig())
                    .setBackgroundTextures(TJGuiTextures.SLOT_DOWN));
        }
        for (int i = 0; i < duality.getStorage().getSlots(); i++) {
            builder.widget(new TJSlotWidget<>(duality.getStorage(), i, 7 + (18 * (i % 9)), 52 + (36 * (i / 9)))
                    .setBackgroundTexture(GuiTextures.SLOT));
        }
        for (int i = 0; i < upgradeHandler.getSlots(); i++) {
            builder.widget(new TJSlotWidget<>(upgradeHandler, i, 186, 7 + (18 * i))
                    .setBackgroundTexture(GuiTextures.SLOT, TJGuiTextures.UPGRADE_OVERLAY));
        }
        for (int i = 0; i < patternHandler.getSlots(); i++) {
            scrollableWidgetGroup.addWidget(new TJSlotWidget<>(patternHandler, i, 18 * (i % 9), 18 * (i / 9))
                    .setPutItemsPredicate(Api.INSTANCE.definitions().items().encodedPattern().maybeStack(1).orElse(ItemStack.EMPTY)::isItemEqual)
                    .setBackgroundTexture(GuiTextures.SLOT, TJGuiTextures.PATTERN_OVERLAY));
        }
        return builder.widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(this.getItemStackRepresentation()).setLocale(this.getItemStackRepresentation().getDisplayName()))
                .widget(scrollableWidgetGroup)
                .widget(new ButtonPopUpWidget<>()
                        .addPopup(widgetGroup -> true)
                        .addPopup(new ButtonWidget<>(154, 0, 22, 22)
                                .setBackgroundTextures(TJGuiTextures.INTERFACE_SETTINGS)
                                .setTooltipText("gui.appliedenergistics2.Priority"), widgetGroup -> {
                            widgetGroup.addWidget(new ImageWidget(7, 90, 162, 100, GuiTextures.BORDERED_BACKGROUND));
                            widgetGroup.addWidget(new LabelWidget(14, 95, "gui.appliedenergistics2.Priority"));
                            widgetGroup.addWidget(new NewTextFieldWidget<>(14, 136, 148, 18, true, () -> String.valueOf(duality.getPriority()), this::setPriority)
                                    .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                                    .setUpdateOnTyping(true));
                            widgetGroup.addWidget(new ClickButtonWidget(15, 110, 25, 20, "+1", data -> this.setPriority(String.valueOf((long) duality.getPriority() + 1), "")));
                            widgetGroup.addWidget(new ClickButtonWidget(45, 110, 30, 20, "+10", data -> this.setPriority(String.valueOf((long) duality.getPriority() + 10), "")));
                            widgetGroup.addWidget(new ClickButtonWidget(80, 110, 35, 20, "+100", data -> this.setPriority(String.valueOf((long) duality.getPriority() + 100), "")));
                            widgetGroup.addWidget(new ClickButtonWidget(120, 110, 40, 20, "+1000", data -> this.setPriority(String.valueOf((long) duality.getPriority() + 1000), "")));
                            widgetGroup.addWidget(new ClickButtonWidget(15, 160, 25, 20, "-1", data -> this.setPriority(String.valueOf((long) duality.getPriority() - 1), "")));
                            widgetGroup.addWidget(new ClickButtonWidget(45, 160, 30, 20, "-10", data -> this.setPriority(String.valueOf((long) duality.getPriority() - 10), "")));
                            widgetGroup.addWidget(new ClickButtonWidget(80, 160, 35, 20, "-100", data -> this.setPriority(String.valueOf((long) duality.getPriority() - 100), "")));
                            widgetGroup.addWidget(new ClickButtonWidget(120, 160, 40, 20, "-1000", data -> this.setPriority(String.valueOf((long) duality.getPriority() - 1000), "")));
                            return false;
                        }))
                .bindPlayerInventory(player.inventory, 209)
                .build(holder, player);
    }

    private void setPriority(String text, String id) {
        this.getInterfaceDuality().setPriority((int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, Long.parseLong(text))));
        this.getTile().markDirty();
    }
}
