package tj.items.covers;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IWorkable;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.util.function.BooleanConsumer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraftforge.common.capabilities.Capability;
import tj.integration.ae2.ITickSetter;
import tj.mui.TJGuiTextures;
import tj.mui.TJGuiUtils;
import tj.mui.widgets.ButtonWidget;
import tj.mui.widgets.PopUpWidget;
import tj.mui.widgets.TJModularUI;
import tj.mui.widgets.impl.NewTextFieldWidget;
import tj.mui.widgets.impl.PanelWidgetGroup;
import tj.mui.widgets.impl.TJLabelWidget;
import tj.items.TJMetaItems;
import tj.mui.widgets.impl.TJToggleButtonWidget;
import tj.textures.TJTextures;
import tj.util.TJItemUtils;

import java.util.regex.Pattern;


public class CreativeEnergyCover extends CoverBehavior implements ITickable, CoverWithUI, IWorkable, ITickSetter {

    private final IEnergyContainer energyContainer;
    private boolean simulateVoltage;
    private boolean isDraining;
    private boolean isActive = true;
    private long energyRate = Long.MAX_VALUE;
    private long voltage = 32;
    private long amps = 1;
    private int ticks = 1;

    public CreativeEnergyCover(ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
        this.energyContainer = coverHolder.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, null);
    }

    @Override
    public boolean canAttach() {
        return this.coverHolder.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, this.attachedSide) != null;
    }

    @Override
    public void renderCover(CCRenderState ccRenderState, Matrix4 matrix4, IVertexOperation[] iVertexOperations, Cuboid6 cuboid6, BlockRenderLayer blockRenderLayer) {
        TJTextures.COVER_CREATIVE_ENERGY.renderSided(this.attachedSide, cuboid6, ccRenderState, iVertexOperations, matrix4);
    }

    @Override
    public void update() {
        if (this.isActive && this.coverHolder.getOffsetTimer() % this.ticks == 0) {
            if (this.isDraining) {
                this.energyContainer.removeEnergy(this.simulateVoltage ? this.voltage * this.amps : this.energyRate);
                return;
            }
            if (this.simulateVoltage) {
                this.energyContainer.acceptEnergyFromNetwork(this.attachedSide, this.voltage, this.amps);
            } else this.energyContainer.addEnergy(this.energyRate);
        }
    }

    @Override
    public EnumActionResult onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, CuboidRayTraceResult hitResult) {
        if (!playerIn.world.isRemote)
            this.openUI((EntityPlayerMP) playerIn);
        return EnumActionResult.SUCCESS;
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {
        final PanelWidgetGroup panelWidgetGroup = TJGuiUtils.createTickPanel(this);
        return TJModularUI.builder(this, player)
                .setWidthHeight(200, 187)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(TJMetaItems.CREATIVE_ENERGY_COVER.getStackForm()).setLocale("metaitem.creative.energy.cover.name"))
                .widget(new TJToggleButtonWidget(26, 9, 124, 18, GuiTextures.VANILLA_BUTTON, () -> this.simulateVoltage, this::setSimulateVoltage))
                .widget(new TJToggleButtonWidget(151, 9, 18, 18, GuiTextures.VANILLA_BUTTON, () -> this.isDraining, this::setDraining)
                        .setToggleDisplayText("machine.universal.mode.transfer.in", "machine.universal.mode.transfer.out"))
                .widget(new PopUpWidget<>()
                        .setClickToDefault(false)
                        .setIndexSupplier(() -> this.simulateVoltage ? 1 : 0)
                        .addPopup(widgetGroup -> {
                            widgetGroup.addWidget(new NewTextFieldWidget<>(26, 27, 124, 18, true,
                                    () -> String.valueOf(this.energyRate), this::setEnergyRate)
                                    .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches())
                                    .setBackgroundText("metaitem.creative_energy_cover.set.energy_rate")
                                    .setTooltipText("metaitem.creative_energy_cover.set.energy_rate")
                                    .setUpdateOnTyping(true));
                            widgetGroup.addWidget(new ButtonWidget<>(7, 27, 18, 18, "-", minus ->
                                    this.setEnergyRate(String.valueOf((double) this.energyRate / 2), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                            widgetGroup.addWidget(new ButtonWidget<>(151, 27, 18, 18, "+", plus ->
                                    this.setEnergyRate(String.valueOf((double) this.energyRate * 2), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                            widgetGroup.addWidget(new LabelWidget(32, 14, "metaitem.creative_energy_cover.simulate_voltage", false));
                            return false;
                        }).addPopup(widgetGroup -> false))
                .widget(new PopUpWidget<>()
                        .setClickToDefault(false)
                        .setIndexSupplier(() -> this.simulateVoltage ? 0 : 1)
                        .addPopup(widgetGroup -> {
                            widgetGroup.addWidget(new NewTextFieldWidget<>(26, 27, 124, 18, true,
                                    () -> String.valueOf(this.voltage), this::setVoltage)
                                    .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches())
                                    .setBackgroundText("metaitem.creative_energy_cover.set.voltage")
                                    .setTooltipText("metaitem.creative_energy_cover.set.voltage")
                                    .setUpdateOnTyping(true));
                            widgetGroup.addWidget(new NewTextFieldWidget<>(26, 45, 124, 18, true,
                                    () -> String.valueOf(this.amps), this::setAmps)
                                    .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches())
                                    .setBackgroundText("metaitem.creative_energy_cover.set.amps")
                                    .setTooltipText("metaitem.creative_energy_cover.set.amps")
                                    .setUpdateOnTyping(true));
                            widgetGroup.addWidget(new ButtonWidget<>(7, 27, 18, 18, "-", minus ->
                                    this.setVoltage(String.valueOf(this.voltage / 2), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                            widgetGroup.addWidget(new ButtonWidget<>(151, 27, 18, 18, "+", plus ->
                                    this.setVoltage(String.valueOf(this.voltage * 2), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                            widgetGroup.addWidget(new ButtonWidget<>(7, 45, 18, 18, "-", minus ->
                                    this.setAmps(String.valueOf(this.amps / 2), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                            widgetGroup.addWidget(new ButtonWidget<>(151, 45, 18, 18, "+", plus ->
                                    this.setAmps(String.valueOf(this.amps * 2), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                            widgetGroup.addWidget(new LabelWidget(32, 14, "metaitem.creative_energy_cover.simulate_voltage", true));
                            return false;
                        }).addPopup(widgetGroup -> false))
                .widget(new TJToggleButtonWidget(174, 105, 18, 18, TJGuiTextures.TOGGLE_RESET_BUTTON, () -> false, this::onReset)
                        .setTitleHoverTooltipText("machine.universal.toggle.reset.disabled"))
                .widget(new TJToggleButtonWidget(174, 162, 18, 18, TJGuiTextures.TOGGLE_POWER_BUTTON, this::isWorkingEnabled, this::setWorkingEnabled)
                        .setToggleTitleTooltipHoverText("machine.universal.toggle.run.mode.disabled",
                                "machine.universal.toggle.run.mode.enabled"))
                .widget(new NewTextFieldWidget<>(7, 82, 162, 18, true, () -> String.valueOf(this.ticks), (text, id) ->
                        this.setTickTime(Long.parseLong(text)))
                        .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches())
                        .setTooltipText("machine.universal.ticks.operation")
                        .setUpdateOnTyping(true)
                        .setMaxStringLength(11))
                .widget(new TJToggleButtonWidget(174, 82, 18, 18, panelWidgetGroup::isPanelOpen, (BooleanConsumer) value -> {
                    if (!value && panelWidgetGroup.isPanelOpen()) {
                        panelWidgetGroup.closePanel();
                    } else if (value) {
                        panelWidgetGroup.openPanel();
                    }
                }).setToggleTexture(GuiTextures.TOGGLE_BUTTON_BACK)
                        .setItemDisplay(TJItemUtils.getItemStackFromName("enderio:item_material", 1, 11)) // infinity bimetal gear
                        .setHoverTooltipText("tj.machine.universal.ticks.operation_configure")
                        .useToggleTexture(true))
                .widget(panelWidgetGroup)
                .build();
    }

    @Override
    public void onAttached(ItemStack itemStack) {
        final NBTTagCompound compound = itemStack.getOrCreateSubCompound("init");
        if (compound.hasKey("simulateVoltage"))
            this.simulateVoltage = compound.getBoolean("simulateVoltage");
        if (compound.hasKey("draining"))
            this.isDraining = compound.getBoolean("draining");
        if (compound.hasKey("active"))
            this.isActive = compound.getBoolean("active");
        if (compound.hasKey("energyRate"))
            this.energyRate = compound.getLong("energyRate");
        if (compound.hasKey("voltage"))
            this.voltage = compound.getLong("voltage");
        if (compound.hasKey("amps"))
            this.amps = compound.getLong("amps");
        if (compound.hasKey("ticks"))
            this.ticks = compound.getInteger("ticks");
    }

    private void onReset(boolean reset) {
        this.ticks = 1;
        this.markAsDirty();
    }

    private void setDraining(boolean isDraining) {
        this.isDraining = isDraining;
        this.markAsDirty();
    }

    private void setSimulateVoltage(boolean simulateVoltage) {
        this.simulateVoltage = simulateVoltage;
        this.markAsDirty();
    }

    private void setEnergyRate(String text, String id) {
        this.energyRate = (long) Math.max(0, Math.min(Long.MAX_VALUE, Double.parseDouble(text)));
        this.markAsDirty();
    }

    private void setVoltage(String text, String id) {
        this.voltage = Math.max(0, Math.min(2147483648L, Long.parseLong(text)));
        this.markAsDirty();
    }

    private void setAmps(String text, String id) {
        this.amps = Math.max(0, Math.min(4294967295L, Long.parseLong(text)));
        this.markAsDirty();
    }

    @Override
    public void setTickTime(long tickTime) {
        this.ticks = (int) Math.max(1, Math.min(Integer.MAX_VALUE, tickTime));
        this.markAsDirty();
    }

    @Override
    public int getTickTime() {
        return this.ticks;
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("simulateVoltage", this.simulateVoltage);
        data.setBoolean("isDraining", this.isDraining);
        data.setBoolean("isActive", this.isActive);
        data.setLong("energyRate", this.energyRate);
        data.setLong("voltage", this.voltage);
        data.setLong("amps", this.amps);
        data.setInteger("ticks", this.ticks);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.simulateVoltage = data.getBoolean("simulateVoltage");
        this.isDraining = data.getBoolean("isDraining");
        this.voltage = data.getLong("voltage");
        this.amps = data.getLong("amps");
        this.ticks = Math.max(1, data.getInteger("ticks"));
        if (data.hasKey("energyRate"))
            this.energyRate = data.getLong("energyRate");
        if (data.hasKey("isActive"))
            this.isActive = data.getBoolean("isActive");
    }

    @Override
    public <T> T getCapability(Capability<T> capability, T defaultValue) {
        if (capability == GregtechTileCapabilities.CAPABILITY_WORKABLE)
            return GregtechTileCapabilities.CAPABILITY_WORKABLE.cast(this);
        return null;
    }

    @Override
    public int getProgress() {
        return (int) this.coverHolder.getOffsetTimer() % this.ticks;
    }

    @Override
    public int getMaxProgress() {
        return this.ticks;
    }

    @Override
    public boolean isActive() {
        return this.isActive;
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.isActive;
    }

    @Override
    public void setWorkingEnabled(boolean b) {
        this.isActive = b;
        this.markAsDirty();
    }
}
