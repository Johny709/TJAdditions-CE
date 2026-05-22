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
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import tj.gui.TJGuiTextures;
import tj.gui.widgets.*;
import tj.items.TJMetaItems;
import tj.textures.TJTextures;

import java.util.regex.Pattern;

import static gregtech.api.gui.GuiTextures.BORDERED_BACKGROUND;
import static tj.gui.TJGuiTextures.*;

public class CreativeEnergyCover extends CoverBehavior implements ITickable, CoverWithUI {

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
        return ModularUI.builder(BORDERED_BACKGROUND, 176, 170)
                .bindPlayerInventory(player.inventory, 90)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(TJMetaItems.CREATIVE_ENERGY_COVER.getStackForm()).setLocale("metaitem.creative.energy.cover.name"))
                .widget(new NewTextFieldWidget<>(26, 7, 124, 18, true, () -> String.valueOf(this.ticks), this::setTicks)
                        .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches())
                        .setTooltipText("machine.universal.ticks.operation")
                        .setUpdateOnTyping(true))
                .widget(new ToggleButtonWidget(7, 7, 18, 18, MINUS_BUTTON, () -> false, minus -> this.setTicks(String.valueOf((long) this.ticks - 1), "")))
                .widget(new ToggleButtonWidget(151, 7, 18, 18, PLUS_BUTTON, () -> false, plus -> this.setTicks(String.valueOf((long) this.ticks + 1), "")))
                .widget(new ToggleButtonWidget(7, 27, 18, 18, POWER_BUTTON, () -> this.isActive, this::setActive)
                        .setTooltipText("machine.universal.toggle.run.mode"))
                .widget(new ToggleButtonWidget(26, 27, 124, 18, () -> this.simulateVoltage, this::setSimulateVoltage))
                .widget(new CycleButtonWidget(151, 27, 18, 18, () -> this.isDraining, this::setDraining, "machine.universal.mode.transfer.in", "machine.universal.mode.transfer.out"))
                .widget(new PopUpWidget<>()
                        .setClickToDefault(false)
                        .setIndexSupplier(() -> this.simulateVoltage ? 1 : 0)
                        .addPopup(widgetGroup -> {
                            widgetGroup.addWidget(new NewTextFieldWidget<>(26, 45, 124, 18, true, () -> String.valueOf(this.energyRate), this::setEnergyRate)
                                    .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches())
                                    .setBackgroundText("metaitem.creative_energy_cover.set.energy_rate")
                                    .setTooltipText("metaitem.creative_energy_cover.set.energy_rate")
                                    .setUpdateOnTyping(true));
                            widgetGroup.addWidget(new ToggleButtonWidget(7, 45, 18, 18, MINUS_BUTTON, () -> false, minus -> this.setEnergyRate(String.valueOf((double) this.energyRate / 2), "")));
                            widgetGroup.addWidget(new ToggleButtonWidget(151, 45, 18, 18, PLUS_BUTTON, () -> false, plus -> this.setEnergyRate(String.valueOf((double) this.energyRate * 2), "")));
                            widgetGroup.addWidget(new ToggleButtonWidget(151, 63, 18, 18, RESET_BUTTON, () -> false, reset -> this.setEnergyRate(String.valueOf(Long.MAX_VALUE), ""))
                                    .setTooltipText("machine.universal.toggle.reset"));
                            widgetGroup.addWidget(new LabelWidget(32, 32, "metaitem.creative_energy_cover.simulate_voltage", false));
                            return false;
                        }).addPopup(widgetGroup -> false))
                .widget(new PopUpWidget<>()
                        .setClickToDefault(false)
                        .setIndexSupplier(() -> this.simulateVoltage ? 0 : 1)
                        .addPopup(widgetGroup -> {
                            widgetGroup.addWidget(new NewTextFieldWidget<>(26, 45, 124, 18, true, () -> String.valueOf(this.voltage), this::setVoltage)
                                    .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches())
                                    .setBackgroundText("metaitem.creative_energy_cover.set.voltage")
                                    .setTooltipText("metaitem.creative_energy_cover.set.voltage")
                                    .setUpdateOnTyping(true));
                            widgetGroup.addWidget(new NewTextFieldWidget<>(26, 63, 124, 18, true, () -> String.valueOf(this.amps), this::setAmps)
                                    .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches())
                                    .setBackgroundText("metaitem.creative_energy_cover.set.amps")
                                    .setTooltipText("metaitem.creative_energy_cover.set.amps")
                                    .setUpdateOnTyping(true));
                            widgetGroup.addWidget(new ToggleButtonWidget(7, 45, 18, 18, MINUS_BUTTON, () -> false, minus -> this.setVoltage(String.valueOf(this.voltage / 2), "")));
                            widgetGroup.addWidget(new ToggleButtonWidget(151, 45, 18, 18, PLUS_BUTTON, () -> false, plus -> this.setVoltage(String.valueOf(this.voltage * 2), "")));
                            widgetGroup.addWidget(new ToggleButtonWidget(7, 63, 18, 18, MINUS_BUTTON, () -> false, minus -> this.setAmps(String.valueOf(this.amps / 2), "")));
                            widgetGroup.addWidget(new ToggleButtonWidget(151, 63, 18, 18, PLUS_BUTTON, () -> false, plus -> this.setAmps(String.valueOf(this.amps * 2), "")));
                            widgetGroup.addWidget(new LabelWidget(32, 32, "metaitem.creative_energy_cover.simulate_voltage", true));
                            return false;
                        }).addPopup(widgetGroup -> false))
                .build(this, player);
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

    private void setActive(boolean isActive) {
        this.isActive = isActive;
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

    private void setTicks(String text, String id) {
        this.ticks = (int) Math.max(1, Math.min(Integer.MAX_VALUE, Long.parseLong(text)));
        this.markAsDirty();
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
}
