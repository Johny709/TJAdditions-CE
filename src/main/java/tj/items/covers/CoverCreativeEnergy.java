package tj.items.covers;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.CycleButtonWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import tj.gui.widgets.PopUpWidgetGroup;
import tj.gui.widgets.TJTextFieldWidget;
import tj.textures.TJTextures;

import java.util.regex.Pattern;

import static gregtech.api.gui.GuiTextures.BORDERED_BACKGROUND;
import static gregtech.api.gui.GuiTextures.DISPLAY;
import static tj.gui.TJGuiTextures.*;

public class CoverCreativeEnergy extends CoverBehavior implements ITickable, CoverWithUI {

    private final IEnergyContainer energyContainer;
    private boolean simulateVoltage;
    private boolean isDraining;
    private boolean isActive = true;
    private long energyRate = Long.MAX_VALUE;
    private long voltage;
    private long amps;

    public CoverCreativeEnergy(ICoverable coverHolder, EnumFacing attachedSide) {
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
        if (!this.isActive)
            return;
        if (this.isDraining) {
            this.energyContainer.removeEnergy(this.simulateVoltage ? this.voltage * this.amps : this.energyRate);
            return;
        }
        if (this.simulateVoltage)
            this.energyContainer.acceptEnergyFromNetwork(this.attachedSide, this.voltage, this.amps);
        else this.energyContainer.addEnergy(this.energyRate);
    }

    @Override
    public EnumActionResult onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, CuboidRayTraceResult hitResult) {
        if (!playerIn.world.isRemote)
            this.openUI((EntityPlayerMP) playerIn);
        return EnumActionResult.SUCCESS;
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {
        return ModularUI.builder(BORDERED_BACKGROUND, 176, 170)
                .bindPlayerInventory(player.inventory, 90)
                .label(30, 4, "metaitem.creative.energy.cover.name")
                .widget(new ImageWidget(26, 40, 124, 18, DISPLAY))
                .widget(new ToggleButtonWidget(7, 22, 18, 18, POWER_BUTTON, this::isActive, this::setActive)
                        .setTooltipText("machine.universal.toggle.run.mode"))
                .widget(new ToggleButtonWidget(26, 22, 124, 18, this::getSimulateVoltage, this::setSimulateVoltage))
                .widget(new CycleButtonWidget(151, 22, 18, 18, this::isDraining, this::setDraining, "machine.universal.mode.transfer.in", "machine.universal.mode.transfer.out"))
                .widget(new PopUpWidgetGroup(7, 40, 162, 40)
                        .addWidgets(new TJTextFieldWidget(24, 5, 119, 12, false, this::getEnergyRate, this::setEnergyRate)
                                .setTooltipText("metaitem.creative_energy_cover.set.energy_rate")
                                .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches()))
                        .addWidgets(new ToggleButtonWidget(0, 0, 18, 18, PLUS_BUTTON, () -> false, (plus) -> this.energyRate = Math.max(1, this.energyRate * 2)))
                        .addWidgets(new ToggleButtonWidget(144, 0, 18, 18, MINUS_BUTTON, () -> false, (minus) -> this.energyRate = Math.max(1, this.energyRate / 2)))
                        .addWidgets(new ToggleButtonWidget(144, 18, 18, 18, RESET_BUTTON, () -> false, (reset) -> this.energyRate = Long.MAX_VALUE)
                                .setTooltipText("machine.universal.toggle.reset"))
                        .addWidgets(new LabelWidget(25, -13, "metaitem.creative_energy_cover.simulate_voltage", false))
                        .setPredicate(this::getSimulateVoltage))
                .widget(new PopUpWidgetGroup(7, 40, 162, 40)
                        .addWidgets(new ImageWidget(19, 18, 124, 18, DISPLAY))
                        .addWidgets(new TJTextFieldWidget(24, 5, 119, 12, false, this::getVoltage, this::setVoltage)
                                .setTooltipText("metaitem.creative_energy_cover.set.voltage")
                                .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches()))
                        .addWidgets(new TJTextFieldWidget(24, 23, 119, 12, false, this::getAmps, this::setAmps)
                                .setTooltipText("metaitem.creative_energy_cover.set.amps")
                                .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches()))
                        .addWidgets(new ToggleButtonWidget(0, 0, 18, 18, PLUS_BUTTON, () -> false, (plus) -> this.voltage = Math.max(1, this.voltage * 2)))
                        .addWidgets(new ToggleButtonWidget(144, 0, 18, 18, MINUS_BUTTON, () -> false, (minus) -> this.voltage = Math.max(1, this.voltage / 2)))
                        .addWidgets(new ToggleButtonWidget(0, 18, 18, 18, PLUS_BUTTON, () -> false, (plus) -> this.amps = Math.max(1, this.amps * 2)))
                        .addWidgets(new ToggleButtonWidget(144, 18, 18, 18, MINUS_BUTTON, () -> false, (minus) -> this.amps = Math.max(1, this.amps / 2)))
                        .addWidgets(new LabelWidget(25, -13, "metaitem.creative_energy_cover.simulate_voltage", true))
                        .setPredicate(this::getSimulateVoltage)
                        .setInverted())
                .build(this, player);
    }

    private void setActive(boolean isActive) {
        this.isActive = isActive;
        this.markAsDirty();
    }

    private boolean isActive() {
        return this.isActive;
    }

    private void setDraining(boolean isDraining) {
        this.isDraining = isDraining;
        this.markAsDirty();
    }

    private boolean isDraining() {
        return this.isDraining;
    }

    private void setSimulateVoltage(boolean simulateVoltage) {
        this.simulateVoltage = simulateVoltage;
        this.markAsDirty();
    }

    private boolean getSimulateVoltage() {
        return this.simulateVoltage;
    }

    private void setEnergyRate(String energyRate) {
        this.energyRate = Long.parseLong(energyRate);
        this.markAsDirty();
    }

    private String getEnergyRate() {
        return String.valueOf(this.energyRate);
    }

    private void setVoltage(String voltage) {
        this.voltage = Long.parseLong(voltage);
        this.markAsDirty();
    }

    private String getVoltage() {
        return String.valueOf(this.voltage);
    }

    private void setAmps(String amps) {
        this.amps = Long.parseLong(amps);
        this.markAsDirty();
    }

    private String getAmps() {
        return String.valueOf(this.amps);
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
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.simulateVoltage = data.getBoolean("simulateVoltage");
        this.isDraining = data.getBoolean("isDraining");
        this.voltage = data.getLong("voltage");
        this.amps = data.getLong("amps");
        if (data.hasKey("energyRate"))
            this.energyRate = data.getLong("energyRate");
        if (data.hasKey("isActive"))
            this.isActive = data.getBoolean("isActive");
    }
}
