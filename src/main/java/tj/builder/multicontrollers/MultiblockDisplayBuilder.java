package tj.builder.multicontrollers;

import gregicadditions.GAUtility;
import gregicadditions.GAValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.recipes.RecipeMap;
import net.minecraft.util.text.*;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fluids.FluidStack;
import tj.TJValues;

import java.util.List;
import java.util.function.Consumer;

import static gregtech.api.gui.widgets.AdvancedTextWidget.withButton;


public class MultiblockDisplayBuilder {

    private final List<ITextComponent> textList;

    public MultiblockDisplayBuilder(List<ITextComponent> textList) {
        this.textList = textList;
    }

    public static MultiblockDisplayBuilder start(List<ITextComponent> textList) {
        return new MultiblockDisplayBuilder(textList);
    }

    public MultiblockDisplayBuilder custom(Consumer<List<ITextComponent>> textList) {
        textList.accept(this.textList);
        return this;
    }

    public MultiblockDisplayBuilder addTranslation(String locale, Object... format) {
        this.textList.add(new TextComponentString(I18n.translateToLocalFormatted(locale, format)));
        return this;
    }


    public MultiblockDisplayBuilder energyStored(long energyStored, long energyCapacity) {
        this.textList.add(new TextComponentString(I18n.translateToLocalFormatted("machine.universal.energy.stored", energyStored, energyCapacity)));
        return this;
    }

    public MultiblockDisplayBuilder energyInput(boolean hasEnoughEnergy, long amount) {
        return this.energyInput(hasEnoughEnergy, amount, 1);
    }

    public MultiblockDisplayBuilder energyInput(boolean hasEnoughEnergy, long amount, int maxProgress) {
        ITextComponent textComponent = !hasEnoughEnergy ? new TextComponentString(I18n.translateToLocal("tj.multiblock.not_enough_energy"))
                : maxProgress > 1 ? new TextComponentString(I18n.translateToLocalFormatted("tj.multiblock.parallel.sum.2", amount, maxProgress))
                : new TextComponentString(I18n.translateToLocalFormatted("tj.multiblock.parallel.sum", amount)) ;
        this.textList.add(textComponent);
        return this;
    }

    public MultiblockDisplayBuilder voltageTier(int tier) {
        if (tier > 0) {
            String color = TJValues.VCC[tier];
            this.textList.add(new TextComponentTranslation("machine.universal.tooltip.voltage_tier")
                    .appendText(" §7(")
                    .appendSibling(new TextComponentString(color + GAValues.VN[tier] + "§r"))
                    .appendText("§7)"));
        }
        return this;
    }

    public MultiblockDisplayBuilder voltageIn(IEnergyContainer energyContainer) {
        if (energyContainer != null && energyContainer.getEnergyCapacity() > 0) {
            long maxVoltage = energyContainer.getInputVoltage();
            int tier = GAUtility.getTierByVoltage(maxVoltage);
            String color = TJValues.VCC[tier];
            this.textList.add(new TextComponentTranslation("tj.multiblock.max_energy_per_tick")
                    .appendText(" ")
                    .appendSibling(new TextComponentString("§e" + TJValues.thousandFormat.format(maxVoltage) + "§r"))
                    .appendText(" §7(")
                    .appendSibling(new TextComponentString(color + GAValues.VN[tier] + "§r"))
                    .appendText("§7)"));
        }
        return this;
    }

    public MultiblockDisplayBuilder energyBonus(int energyBonus, boolean enabled) {
        if (enabled)
            this.textList.add(new TextComponentTranslation("gregtech.multiblock.universal.energy_usage", 100 - energyBonus).setStyle(new Style().setColor(TextFormatting.AQUA)));
        return this;
    }

    public MultiblockDisplayBuilder fluidInput(boolean hasEnoughAmount, FluidStack fluidStack) {
        return this.fluidInput(hasEnoughAmount, fluidStack, 1);
    }

    public MultiblockDisplayBuilder fluidInput(boolean hasEnoughAmount, FluidStack fluidStack, int ticks) {
        if (fluidStack == null)
            return this;
        String fluidName = fluidStack.getLocalizedName();
        int amount = fluidStack.amount;
        boolean hasEnoughFluid = hasEnoughAmount || amount == 0;
        ITextComponent fluidInputText = !hasEnoughFluid ? new TextComponentString(I18n.translateToLocalFormatted("tj.multiblock.not_enough_fluid", fluidName, amount))
                : ticks == 1 ? new TextComponentString(I18n.translateToLocalFormatted("machine.universal.fluid.input.tick", fluidName, amount))
                : ticks % 20 != 0 ? new TextComponentString(I18n.translateToLocalFormatted("machine.universal.fluid.input.ticks", amount, fluidName, ticks))
                : ticks == 20 ? new TextComponentString(I18n.translateToLocalFormatted("machine.universal.input.sec", fluidName, amount))
                : new TextComponentString(I18n.translateToLocalFormatted("machine.universal.fluid.input.secs", amount, fluidName, ticks / 20));
        this.textList.add(fluidInputText);
        return this;
    }

    public MultiblockDisplayBuilder fluidOutput(boolean hasEnoughSpace, FluidStack fluidStack) {
        String fluidName = fluidStack.getLocalizedName();
        int amount = fluidStack.amount;
        boolean hasEnoughFluid = hasEnoughSpace || amount == 0;
        ITextComponent fluidInputText = hasEnoughFluid ? new TextComponentString(I18n.translateToLocalFormatted("machine.universal.fluid.output.sec", fluidName, amount))
                : new TextComponentString(I18n.translateToLocalFormatted("tj.multiblock.not_enough_fluid.space", fluidName, amount));
        this.textList.add(fluidInputText);
        return this;
    }

    public MultiblockDisplayBuilder isWorking(boolean isWorkingEnabled, boolean isActive, int progress, int maxProgress) {
        int currentProgress = (int) Math.floor(progress / (maxProgress * 1.0) * 100);
        ITextComponent isWorkingText = !isWorkingEnabled ? new TextComponentString(I18n.translateToLocal("machine.universal.work_paused"))
                : !isActive ? new TextComponentString(I18n.translateToLocal("machine.universal.idling"))
                : new TextComponentString(I18n.translateToLocal("machine.universal.running"));
        this.textList.add(isWorkingText);
        if (isActive)
            this.textList.add(new TextComponentString(I18n.translateToLocalFormatted("tj.multiblock.progress", TJValues.thousandTwoPlaceFormat.format((double) progress / 20), TJValues.thousandTwoPlaceFormat.format((double) maxProgress / 20), currentProgress)));
        return this;
    }

    public MultiblockDisplayBuilder recipeMap(RecipeMap<?> recipeMap) {
        this.textList.add(new TextComponentTranslation("gtadditions.multiblock.universal.tooltip.1")
                .appendSibling(withButton(new TextComponentString("[" + I18n.translateToLocal("recipemap." + recipeMap.getUnlocalizedName() + ".name") + "]"), recipeMap.getUnlocalizedName())));
        return this;
    }

    public MultiblockDisplayBuilder temperature(long current, long max) {
        this.textList.add(new TextComponentString(I18n.translateToLocalFormatted("tj.multiblock.temperature", current, max)));
        return this;
    }
}
