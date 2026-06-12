package tj.integration.ae2.part;

import appeng.api.config.*;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.parts.IPartModel;
import appeng.core.Api;
import appeng.fluids.helper.DualityFluidInterface;
import appeng.fluids.helper.IConfigurableFluidInventory;
import appeng.fluids.helper.IFluidInterfaceHost;
import appeng.helpers.DualityInterface;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;
import appeng.parts.misc.PartInterface;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.tile.networking.TileCableBus;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.tab.VerticalTabListRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import tj.TJ;
import tj.builder.WidgetTabBuilder;
import tj.integration.ae2.helpers.DualitySuperFluidInterface;
import tj.integration.ae2.helpers.DualitySuperInterface;
import tj.items.item.TJItems;
import tj.mui.TJGuiTextures;
import tj.mui.uifactory.ITileEntityUI;
import tj.mui.uifactory.TileEntityHolder;
import tj.mui.widgets.ButtonWidget;
import tj.mui.widgets.impl.*;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.regex.Pattern;

import static gregtech.api.gui.widgets.tab.VerticalTabListRenderer.HorizontalLocation.LEFT;
import static gregtech.api.gui.widgets.tab.VerticalTabListRenderer.VerticalStartCorner.TOP;

public class PartStockingDualInterface extends PartInterface implements IFluidInterfaceHost, IConfigurableFluidInventory, ITileEntityUI {

    public static final ResourceLocation MODEL_BASE = new ResourceLocation(TJ.MODID, "part/me.part.stocking_dual_interface_base");

    @PartModels
    public static final PartModel MODELS_OFF = new PartModel(MODEL_BASE, new ResourceLocation(TJ.MODID, "part/me.part.stocking_dual_interface_off"));

    @PartModels
    public static final PartModel MODELS_ON = new PartModel(MODEL_BASE, new ResourceLocation(TJ.MODID, "part/me.part.stocking_dual_interface_on"));

    @PartModels
    public static final PartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, new ResourceLocation(TJ.MODID, "part/me.part.stocking_dual_interface_has_channel"));

    private final DualitySuperFluidInterface dualityFluid = new DualitySuperFluidInterface(this.getProxy(), this, 18);

    public PartStockingDualInterface(ItemStack is) {
        super(is);
        ObfuscationReflectionHelper.setPrivateValue(PartInterface.class, this, new DualitySuperInterface(this.getProxy(), this, 10, 18, 72), "duality");
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
    public void gridChanged() {
        super.gridChanged();
        this.dualityFluid.gridChanged();
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        final NBTTagCompound compound = new NBTTagCompound();
        this.dualityFluid.writeToNBT(compound);
        data.setTag("dualityFluid", compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.dualityFluid.readFromNBT(data.getCompoundTag("dualityFluid"));
    }

    @Nonnull
    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        this.dualityFluid.tickingRequest(node, ticksSinceLastCall);
    return super.tickingRequest(node, ticksSinceLastCall);
    }

    @Override
    public ModularUI createUI(TileEntityHolder holder, EntityPlayer player) {
        final DualityInterface duality = this.getInterfaceDuality();
        final ButtonPopUpWidget<?> buttonPopUpWidget = new ButtonPopUpWidget<>();
        final WidgetTabBuilder tabBuilder = new WidgetTabBuilder()
                .setTabListRenderer(() -> new VerticalTabListRenderer(TOP, LEFT))
                .addTab("tile.me.stocking_interface.name", TJItems.PART_STOCKING_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY), widgets -> this.createInterfaceTab(widgets, buttonPopUpWidget))
                .addTab("tile.me.stocking_fluid_interface.name", TJItems.PART_STOCKING_FLUID_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY), widgets -> this.createFluidInterfaceTab(widgets, buttonPopUpWidget));
        return ModularUI.builder(TJGuiTextures.SUPER_INTERFACE, 211, 292)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(this.getItemStackRepresentation()).setLocale(this.getItemStackRepresentation().getDisplayName()))
                .widget(new TJLabelWidget(4, 0, 162, 18, null)
                        .setDynamicLocale(this::getCustomInventoryName)
                        .setCentered(false)
                        .setCanSlide(false))
                .widget(tabBuilder.build())
                .widget(buttonPopUpWidget.addPopup(widgetGroup -> true)
                        .addPopup(new ButtonWidget<>(154, 0, 22, 22)
                                .setItemDisplay(Api.INSTANCE.definitions().items().certusQuartzWrench().maybeStack(1).orElse(ItemStack.EMPTY))
                                .setBackgroundTextures(TJGuiTextures.INTERFACE_SETTINGS_EDGE_RIGHT)
                                .setTooltipText("gui.appliedenergistics2.Priority"), widgetGroup -> {
                            widgetGroup.addWidget(new ImageWidget(7, 107, 162, 100, GuiTextures.BORDERED_BACKGROUND));
                            widgetGroup.addWidget(new LabelWidget(14, 112, "gui.appliedenergistics2.Priority"));
                            widgetGroup.addWidget(new NewTextFieldWidget<>(14, 153, 148, 18, true, () -> String.valueOf(duality.getPriority()), this::setPriority)
                                    .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                                    .setUpdateOnTyping(true));
                            widgetGroup.addWidget(new ClickButtonWidget(15, 127, 25, 20, "+1", data -> this.setPriority(String.valueOf((long) duality.getPriority() + 1), "")));
                            widgetGroup.addWidget(new ClickButtonWidget(45, 127, 30, 20, "+10", data -> this.setPriority(String.valueOf((long) duality.getPriority() + 10), "")));
                            widgetGroup.addWidget(new ClickButtonWidget(80, 127, 35, 20, "+100", data -> this.setPriority(String.valueOf((long) duality.getPriority() + 100), "")));
                            widgetGroup.addWidget(new ClickButtonWidget(120, 127, 40, 20, "+1000", data -> this.setPriority(String.valueOf((long) duality.getPriority() + 1000), "")));
                            widgetGroup.addWidget(new ClickButtonWidget(15, 177, 25, 20, "-1", data -> this.setPriority(String.valueOf((long) duality.getPriority() - 1), "")));
                            widgetGroup.addWidget(new ClickButtonWidget(45, 177, 30, 20, "-10", data -> this.setPriority(String.valueOf((long) duality.getPriority() - 10), "")));
                            widgetGroup.addWidget(new ClickButtonWidget(80, 177, 35, 20, "-100", data -> this.setPriority(String.valueOf((long) duality.getPriority() - 100), "")));
                            widgetGroup.addWidget(new ClickButtonWidget(120, 177, 40, 20, "-1000", data -> this.setPriority(String.valueOf((long) duality.getPriority() - 1000), "")));
                            return false;
                        }))
                .bindPlayerInventory(player.inventory, 209)
                .build(holder, player);
    }

    private void createInterfaceTab(List<Widget> tab, ButtonPopUpWidget<?> buttonPopUpWidget) {

    }

    private void createFluidInterfaceTab(List<Widget> tab, ButtonPopUpWidget<?> buttonPopUpWidget) {

    }

    @Override
    public boolean hasCapability(Capability<?> capabilityClass) {
        return super.hasCapability(capabilityClass) || this.dualityFluid.hasCapability(capabilityClass, null);
    }

    @Override
    public <T> T getCapability(Capability<T> capabilityClass) {
        final T capability = super.getCapability(capabilityClass);
        return capability != null ? capability : this.dualityFluid.getCapability(capabilityClass, null);
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
    public ItemStack getItemStackRepresentation() {
        return TJItems.PART_STOCKING_DUAL_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY);
    }

    @Override
    public DualityFluidInterface getDualityFluidInterface() {
        return this.dualityFluid;
    }

    @Override
    public IFluidHandler getFluidInventoryByName(String name) {
        return this.dualityFluid.getFluidInventoryByName(name);
    }

    @Override
    public void getDrops(List<ItemStack> drops, boolean wrenched) {
        super.getDrops(drops, wrenched);
        this.dualityFluid.addDrops(drops);
    }

    private void setAutoPull(boolean blockingMode) {
        this.getInterfaceDuality().getConfigManager().putSetting(Settings.BLOCK, blockingMode ? YesNo.YES : YesNo.NO);
        this.getTile().markDirty();
    }

    private void setStackSize(String text, String id) {
        final int slot = Integer.parseInt(id);
        final int maxSize = this.getInterfaceDuality().getConfig().getSlotLimit(0);
        final int stackSize = (int) Math.max(1, Math.min(Long.parseLong(text), maxSize));
        final ItemStack itemStack = this.getInterfaceDuality().getConfig().extractItem(slot, Integer.MAX_VALUE, false);
        if (itemStack.isEmpty()) return;
        itemStack.setCount(stackSize);
        ((AppEngInternalAEInventory) this.getInterfaceDuality().getConfig()).setStackInSlot(slot, itemStack);
        this.getTile().markDirty();
    }

    private void setPriority(String text, String id) {
        this.getInterfaceDuality().setPriority((int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, Long.parseLong(text))));
        this.getTile().markDirty();
    }
}
