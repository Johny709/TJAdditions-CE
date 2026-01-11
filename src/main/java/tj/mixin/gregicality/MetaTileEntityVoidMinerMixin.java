package tj.mixin.gregicality;

import gregicadditions.GAUtility;
import gregicadditions.GAValues;
import gregicadditions.machines.multi.miner.MetaTileEntityVoidMiner;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import tj.builder.multicontrollers.UIDisplayBuilder;
import tj.capability.IProgressBar;
import tj.capability.ProgressBar;
import tj.gui.TJGuiTextures;
import tj.util.TJFluidUtils;

import java.util.Queue;

import static tj.machines.multi.electric.MetaTileEntityVoidMOreMiner.*;

@Mixin(value = MetaTileEntityVoidMiner.class, remap = false)
public abstract class MetaTileEntityVoidMinerMixin extends GAMultiblockWithDisplayBaseMixin implements IProgressBar {

    @Shadow
    private IEnergyContainer energyContainer;

    @Shadow
    private boolean overheat;

    @Shadow
    @Final
    private long energyDrain;

    @Shadow
    private int temperature;

    @Shadow
    @Final
    private int maxTemperature;

    @Shadow
    private double currentDrillingFluid;

    @Shadow
    private IMultipleTankHandler importFluidHandler;

    public MetaTileEntityVoidMinerMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Shadow
    public abstract int getTemperature();

    @Shadow
    public abstract int getMaxTemperature();

    @Override
    protected void configureDisplayText(UIDisplayBuilder builder) {
        super.configureDisplayText(builder);
        if (!this.isStructureFormed()) return;
        if (this.energyContainer != null && this.energyContainer.getEnergyCapacity() > 0) {
            long maxVoltage = this.energyContainer.getInputVoltage();
            String voltageName = GAValues.VN[GAUtility.getTierByVoltage(maxVoltage)];
            builder.addTextComponent(new TextComponentTranslation("gregtech.multiblock.max_energy_per_tick", maxVoltage, voltageName));
        }
        builder.addTextComponent(new TextComponentTranslation("gregtech.multiblock.universal.energy_used", this.energyDrain))
                .addTextComponent(new TextComponentTranslation("gregtech.multiblock.large_boiler.temperature", this.temperature, this.maxTemperature))
                .addTextComponent(new TextComponentTranslation("gregtech.multiblock.universal.drilling_fluid_amount", (int) this.currentDrillingFluid));
        if (this.overheat) {
            builder.addTextComponent(new TextComponentTranslation("gregtech.multiblock.universal.overheat").setStyle(new Style().setColor(TextFormatting.RED)));
        }
    }

    @Override
    public int[][] getBarMatrix() {
        return new int[][]{{0}, {0, 0, 0}};
    }

    @Override
    public void getProgressBars(Queue<ProgressBar> bars, ProgressBar.ProgressBarBuilder barBuilder) {
        bars.add(barBuilder.setProgress(this::getTemperature).setMaxProgress(this::getMaxTemperature)
                .setLocale("tj.multiblock.bars.heat")
                .setBarTexture(TJGuiTextures.BAR_RED)
                .build());
        bars.add(barBuilder.setProgress(this::getDrillingMudAmount).setMaxProgress(this::getDrillingMudCapacity)
                .setLocale("tj.multiblock.bars.fluid").setParams(() -> new Object[]{DRILLING_MUD.getLocalizedName()})
                .setFluidStackSupplier(() -> DRILLING_MUD)
                .build());
        bars.add(barBuilder.setProgress(this::getPyrotheumAmount).setMaxProgress(this::getPyrotheumCapacity)
                .setLocale("tj.multiblock.bars.fluid").setParams(() -> new Object[]{PYROTHEUM.getLocalizedName()})
                .setFluidStackSupplier(() -> PYROTHEUM)
                .build());
        bars.add(barBuilder.setProgress(this::getCryotheumAmount).setMaxProgress(this::getCryotheumCapacity)
                .setLocale("tj.multiblock.bars.fluid").setParams(() -> new Object[]{CRYOTHEUM.getLocalizedName()})
                .setFluidStackSupplier(() -> CRYOTHEUM)
                .build());
    }

    @Unique
    private long getDrillingMudAmount() {
        return TJFluidUtils.getFluidAmountFromTanks(DRILLING_MUD, this.importFluidHandler);
    }

    @Unique
    private long getDrillingMudCapacity() {
        return TJFluidUtils.getFluidCapacityFromTanks(DRILLING_MUD, this.importFluidHandler);
    }

    @Unique
    private long getPyrotheumAmount() {
        return TJFluidUtils.getFluidAmountFromTanks(PYROTHEUM, this.importFluidHandler);
    }

    @Unique
    private long getPyrotheumCapacity() {
        return TJFluidUtils.getFluidCapacityFromTanks(PYROTHEUM, this.importFluidHandler);
    }

    @Unique
    private long getCryotheumAmount() {
        return TJFluidUtils.getFluidAmountFromTanks(CRYOTHEUM, this.importFluidHandler);
    }

    @Unique
    private long getCryotheumCapacity() {
        return TJFluidUtils.getFluidCapacityFromTanks(CRYOTHEUM, this.importFluidHandler);
    }
}
