package tj.builder.multicontrollers;

import gregicadditions.GAUtility;
import gregicadditions.GAValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.recipes.RecipeMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.*;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import tj.TJValues;
import tj.capability.IItemFluidHandlerInfo;
import tj.gui.widgets.AdvancedDisplayWidget;
import tj.mixin.gregtech.IAbstractRecipeLogicMixin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static gregtech.api.gui.widgets.AdvancedTextWidget.withButton;

public final class UIDisplayBuilder {

    private final List<AdvancedDisplayWidget.TextComponentWrapper<?>> textComponentWrappers = new ArrayList<>();
    private final boolean nested;
    private int count;

    public UIDisplayBuilder(boolean nested) {
        this.nested = nested;
    }

    public int increment() {
        return this.count++;
    }

    public UIDisplayBuilder customLine(Consumer<UIDisplayBuilder> textList) {
        textList.accept(this);
        return this;
    }

    public UIDisplayBuilder addTextComponent(ITextComponent component, int priority) {
        this.textComponentWrappers.add(new AdvancedDisplayWidget.TextComponentWrapper<>(component).setPriority(priority));
        return this;
    }

    public UIDisplayBuilder addTextComponentWithHover(ITextComponent component, int priority, Consumer<UIDisplayBuilder> uiBuilder) {
        if (this.nested)
            throw new IllegalArgumentException("Cannot set hover text on hover text");
        UIDisplayBuilder builder = new UIDisplayBuilder(true);
        uiBuilder.accept(builder);
        this.textComponentWrappers.add(new AdvancedDisplayWidget.TextComponentWrapper<>(component).setPriority(priority)
                .setAdvancedHoverComponent(builder.getTextComponentWrappers()));
        return this;
    }

    public UIDisplayBuilder addItemStack(ItemStack itemStack, int priority) {
        this.textComponentWrappers.add(new AdvancedDisplayWidget.TextComponentWrapper<>(itemStack).setPriority(priority));
        return this;
    }

    public UIDisplayBuilder addItemStackWithHover(ItemStack itemStack, int priority, Consumer<UIDisplayBuilder> uiBuilder) {
        if (this.nested)
            throw new IllegalArgumentException("Cannot set hover text on hover text");
        UIDisplayBuilder builder = new UIDisplayBuilder(true);
        uiBuilder.accept(builder);
        this.textComponentWrappers.add(new AdvancedDisplayWidget.TextComponentWrapper<>(itemStack).setPriority(priority)
                .setAdvancedHoverComponent(builder.getTextComponentWrappers()));
        return this;
    }

    public UIDisplayBuilder addFluidStack(FluidStack fluidStack, int priority) {
        this.textComponentWrappers.add(new AdvancedDisplayWidget.TextComponentWrapper<>(fluidStack).setPriority(priority));
        return this;
    }

    public UIDisplayBuilder addFluidStackWithHover(FluidStack fluidStack, int priority, Consumer<UIDisplayBuilder> uiBuilder) {
        if (this.nested)
            throw new IllegalArgumentException("Cannot set hover text on hover text");
        UIDisplayBuilder builder = new UIDisplayBuilder(true);
        uiBuilder.accept(builder);
        this.textComponentWrappers.add(new AdvancedDisplayWidget.TextComponentWrapper<>(fluidStack).setPriority(priority)
                .setAdvancedHoverComponent(builder.getTextComponentWrappers()));
        return this;
    }

    public UIDisplayBuilder addTextComponent(ITextComponent component) {
        return this.addTextComponent(component, this.count++);
    }

    public UIDisplayBuilder addTextComponentWithHover(ITextComponent component, Consumer<UIDisplayBuilder> uiBuilder) {
        return this.addTextComponentWithHover(component, this.count++, uiBuilder);
    }

    public UIDisplayBuilder addItemStack(ItemStack itemStack) {
        return this.addItemStack(itemStack, this.count++);
    }

    public UIDisplayBuilder addItemStackWithHover(ItemStack itemStack, Consumer<UIDisplayBuilder> uiBuilder) {
        return this.addItemStackWithHover(itemStack, this.count++, uiBuilder);
    }

    public UIDisplayBuilder addFluidStack(FluidStack fluidStack) {
        return this.addFluidStack(fluidStack, this.count++);
    }

    public UIDisplayBuilder addFluidStackWithHover(FluidStack fluidStack, Consumer<UIDisplayBuilder> uiBuilder) {
        return this.addFluidStackWithHover(fluidStack, this.count++, uiBuilder);
    }

    public UIDisplayBuilder addTranslationLine(String locale, Object... format) {
        return this.addTranslationLine(0, locale, format);
    }

    public UIDisplayBuilder addTranslationLine(int priority, String locale, Object... format) {
        if (priority != 0)
            this.addTextComponent(new TextComponentString(I18n.translateToLocalFormatted(locale, format)), priority);
        else this.addTextComponent(new TextComponentString(I18n.translateToLocalFormatted(locale, format)));
        return this;
    }

    public UIDisplayBuilder energyStoredLine(long energyStored, long energyCapacity) {
        return this.energyStoredLine(energyStored, energyCapacity, 0);
    }

    public UIDisplayBuilder energyStoredLine(long energyStored, long energyCapacity, int priority) {
        if (priority != 0)
            this.addTextComponent(new TextComponentString(I18n.translateToLocalFormatted("machine.universal.energy.stored", energyStored, energyCapacity)), priority);
        else this.addTextComponent(new TextComponentString(I18n.translateToLocalFormatted("machine.universal.energy.stored", energyStored, energyCapacity)));
        return this;
    }

    public UIDisplayBuilder energyInputLine(IEnergyContainer container, long amount) {
        return this.energyInputLine(container, amount, 1, 0);
    }


    public UIDisplayBuilder energyInputLine(IEnergyContainer container, long amount, int maxProgress) {
        return this.energyInputLine(container, amount, maxProgress, 0);
    }

    public UIDisplayBuilder energyInputLine(IEnergyContainer container, long amount, int maxProgress, int priority) {
        if (amount == 0)
            return this;
        ITextComponent textComponent = container.getEnergyStored() < amount ? new TextComponentString(I18n.translateToLocal("tj.multiblock.not_enough_energy"))
                : maxProgress > 1 ? new TextComponentString(I18n.translateToLocalFormatted("tj.multiblock.parallel.sum.2", amount, maxProgress))
                : new TextComponentString(I18n.translateToLocalFormatted("tj.multiblock.parallel.sum", amount)) ;
        if (priority != 0)
            this.addTextComponent(textComponent, priority);
        else this.addTextComponent(textComponent);
        return this;
    }

    public UIDisplayBuilder voltageTierLine(int tier) {
        if (tier > 0) {
            String color = TJValues.VCC[tier];
            this.addTextComponent(new TextComponentTranslation("machine.universal.tooltip.voltage_tier")
                    .appendText(" §7(")
                    .appendSibling(new TextComponentString(color + GAValues.VN[tier] + "§r"))
                    .appendText("§7)"));
        }
        return this;
    }

    public UIDisplayBuilder voltageInLine(IEnergyContainer energyContainer) {
        if (energyContainer != null && energyContainer.getEnergyCapacity() > 0) {
            long maxVoltage = energyContainer.getInputVoltage();
            int tier = GAUtility.getTierByVoltage(maxVoltage);
            String color = TJValues.VCC[tier];
            this.addTextComponent(new TextComponentTranslation("tj.multiblock.max_energy_per_tick")
                    .appendText(" ")
                    .appendSibling(new TextComponentString("§e" + TJValues.thousandFormat.format(maxVoltage) + "§r"))
                    .appendText(" §7(")
                    .appendSibling(new TextComponentString(color + GAValues.VN[tier] + "§r"))
                    .appendText("§7)"));
        }
        return this;
    }

    public UIDisplayBuilder energyBonusLine(int energyBonus, boolean enabled) {
        return this.energyBonusLine(energyBonus, enabled, 0);
    }

    public UIDisplayBuilder energyBonusLine(int energyBonus, boolean enabled, int priority) {
        if (enabled) {
            if (priority != 0)
                this.addTextComponent(new TextComponentTranslation("gregtech.multiblock.universal.energy_usage", 100 - energyBonus)
                        .setStyle(new Style().setColor(TextFormatting.AQUA)), priority);
            else this.addTextComponent(new TextComponentTranslation("gregtech.multiblock.universal.energy_usage", 100 - energyBonus)
                    .setStyle(new Style().setColor(TextFormatting.AQUA)));
        }
        return this;
    }

    public UIDisplayBuilder fluidInputLine(IMultipleTankHandler tanks, FluidStack fluidStack) {
        return this.fluidInputLine(tanks, fluidStack, 1);
    }
    public UIDisplayBuilder fluidInputLine(IMultipleTankHandler tanks, FluidStack fluidStack, int ticks) {
        return this.fluidInputLine(tanks, fluidStack, ticks, 0);
    }

    public UIDisplayBuilder fluidInputLine(IFluidHandler tanks, FluidStack fluidStack, int ticks, int priority) {
        if (fluidStack == null)
            return this;
        String fluidName = fluidStack.getLocalizedName();
        int amount = fluidStack.amount;
        boolean hasEnoughFluid = fluidStack.isFluidStackIdentical(tanks.drain(fluidStack, false)) || amount == 0;
        ITextComponent fluidInputText = !hasEnoughFluid ? new TextComponentString(I18n.translateToLocalFormatted("tj.multiblock.not_enough_fluid", fluidName, amount))
                : ticks == 1 ? new TextComponentString(I18n.translateToLocalFormatted("machine.universal.fluid.input.tick", fluidName, amount))
                : ticks % 20 != 0 ? new TextComponentString(I18n.translateToLocalFormatted("machine.universal.fluid.input.ticks", amount, fluidName, ticks))
                : ticks == 20 ? new TextComponentString(I18n.translateToLocalFormatted("machine.universal.input.sec", fluidName, amount))
                : new TextComponentString(I18n.translateToLocalFormatted("machine.universal.fluid.input.secs", amount, fluidName, ticks / 20));
        if (priority != 0)
            this.addTextComponent(fluidInputText, priority);
        else this.addTextComponent(fluidInputText);
        return this;
    }

    public UIDisplayBuilder fluidOutputLine(IFluidHandler tanks, FluidStack fluidStack) {
        return this.fluidOutputLine(tanks, fluidStack, 0);
    }

    public UIDisplayBuilder fluidOutputLine(IFluidHandler tanks, FluidStack fluidStack, int priority) {
        if (fluidStack == null)
            return this;
        String fluidName = fluidStack.getLocalizedName();
        int amount = fluidStack.amount;
        boolean hasEnoughFluid = tanks.fill(fluidStack, false) == amount || amount == 0;
        ITextComponent fluidInputText = hasEnoughFluid ? new TextComponentString(I18n.translateToLocalFormatted("machine.universal.fluid.output.sec", fluidName, amount))
                : new TextComponentString(I18n.translateToLocalFormatted("tj.multiblock.not_enough_fluid.space", fluidName, amount));
        if (priority != 0)
            this.addTextComponent(fluidInputText, priority);
        else this.addTextComponent(fluidInputText);
        return this;
    }

    public UIDisplayBuilder isWorkingLine(boolean isWorkingEnabled, boolean isActive, int progress, int maxProgress) {
        return this.isWorkingLine(isWorkingEnabled, isActive, progress, maxProgress, 0);
    }

    public UIDisplayBuilder isWorkingLine(boolean isWorkingEnabled, boolean isActive, int progress, int maxProgress, int priority) {
        int currentProgress = (int) Math.floor(progress / (maxProgress * 1.0) * 100);
        ITextComponent isWorkingText = !isWorkingEnabled ? new TextComponentString(I18n.translateToLocal("machine.universal.work_paused"))
                : !isActive ? new TextComponentString(I18n.translateToLocal("machine.universal.idling"))
                : new TextComponentString(I18n.translateToLocal("machine.universal.running"));
        if (priority != 0)
            this.addTextComponent(isWorkingText, priority);
        else this.addTextComponent(isWorkingText);
        if (isActive) {
            if (priority != 0)
                this.addTextComponent(new TextComponentString(I18n.translateToLocalFormatted("tj.multiblock.progress", TJValues.thousandTwoPlaceFormat.format((double) progress / 20), TJValues.thousandTwoPlaceFormat.format((double) maxProgress / 20), currentProgress)), priority);
            else this.addTextComponent(new TextComponentString(I18n.translateToLocalFormatted("tj.multiblock.progress", TJValues.thousandTwoPlaceFormat.format((double) progress / 20), TJValues.thousandTwoPlaceFormat.format((double) maxProgress / 20), currentProgress)));
        }
        return this;
    }

    public UIDisplayBuilder addRecipeInputLine(IItemFluidHandlerInfo handlerInfo) {
        return this.addRecipeInputLine(handlerInfo, 0);
    }

    public UIDisplayBuilder addRecipeInputLine(IItemFluidHandlerInfo handlerInfo, int priority) {
        if ((handlerInfo.getItemInputs() != null && !handlerInfo.getItemInputs().isEmpty()) || (handlerInfo.getFluidInputs() != null && !handlerInfo.getFluidInputs().isEmpty())) {
            if (priority != 0)
                this.addTranslationLine(priority, "machine.universal.consumption");
            else this.addTranslationLine("machine.universal.consumption");
            if (handlerInfo.getItemInputs() != null)
                for (ItemStack stack : handlerInfo.getItemInputs()) {
                    if (priority != 0)
                        this.addItemStack(stack, priority);
                    else this.addItemStack(stack);
                }
            if (handlerInfo.getFluidInputs() != null)
                for (FluidStack stack : handlerInfo.getFluidInputs()) {
                    if (priority != 0)
                        this.addFluidStack(stack, priority);
                    else this.addFluidStack(stack);
                }
        }
        return this;
    }

    public UIDisplayBuilder addRecipeOutputLine(IItemFluidHandlerInfo handlerInfo) {
        return this.addRecipeOutputLine(handlerInfo, 0);
    }

    public UIDisplayBuilder addRecipeOutputLine(IItemFluidHandlerInfo handlerInfo, int priority) {
        if ((handlerInfo.getItemOutputs() != null && !handlerInfo.getItemOutputs().isEmpty()) || (handlerInfo.getFluidOutputs() != null && !handlerInfo.getFluidOutputs().isEmpty())) {
            if (priority != 0)
                this.addTranslationLine(priority, "machine.universal.producing");
            else this.addTranslationLine("machine.universal.producing");
            if (handlerInfo.getItemOutputs() != null)
                for (ItemStack stack : handlerInfo.getItemOutputs()) {
                    if (priority != 0)
                        this.addItemStack(stack, priority);
                    else this.addItemStack(stack);
                }
            if (handlerInfo.getFluidOutputs() != null)
                for (FluidStack stack : handlerInfo.getFluidOutputs()) {
                    if (priority != 0)
                        this.addFluidStack(stack, priority);
                    else this.addFluidStack(stack);
                }
        }
        return this;
    }

    public UIDisplayBuilder addRecipeOutputLine(AbstractRecipeLogic recipeLogic) {
        return this.addRecipeOutputLine(recipeLogic, 0);
    }

    public UIDisplayBuilder addRecipeOutputLine(AbstractRecipeLogic recipeLogic, int priority) {
        if (!recipeLogic.isActive())
            return this;
        if (priority != 0)
            this.addTranslationLine(priority, "machine.universal.producing");
        else this.addTranslationLine("machine.universal.producing");
        if (((IAbstractRecipeLogicMixin) recipeLogic).getItemOutputs() != null)
            for (ItemStack stack : ((IAbstractRecipeLogicMixin) recipeLogic).getItemOutputs()) {
                if (priority != 0)
                    this.addItemStack(stack, priority);
                else this.addItemStack(stack);
            }
        if (((IAbstractRecipeLogicMixin) recipeLogic).getFluidOutputs() != null)
            for (FluidStack stack : ((IAbstractRecipeLogicMixin) recipeLogic).getFluidOutputs()) {
                if (priority != 0)
                    this.addFluidStack(stack, priority);
                else this.addFluidStack(stack);
            }
        return this;
    }

    public UIDisplayBuilder recipeMapLine(RecipeMap<?> recipeMap) {
        this.addTextComponent(new TextComponentTranslation("gtadditions.multiblock.universal.tooltip.1")
                .appendSibling(withButton(new TextComponentString("[" + I18n.translateToLocal("recipemap." + recipeMap.getUnlocalizedName() + ".name") + "]"), recipeMap.getUnlocalizedName())));
        return this;
    }

    public UIDisplayBuilder temperatureLine(long current, long max) {
        return this.temperatureLine(current, max, 0);
    }

    public UIDisplayBuilder temperatureLine(long current, long max, int priority) {
        if (priority != 0)
            this.addTextComponent(new TextComponentString(I18n.translateToLocalFormatted("tj.multiblock.temperature", current, max)), priority);
        else this.addTextComponent(new TextComponentString(I18n.translateToLocalFormatted("tj.multiblock.temperature", current, max)));
        return this;
    }

    public List<AdvancedDisplayWidget.TextComponentWrapper<?>> getTextComponentWrappers() {
        return this.textComponentWrappers;
    }
}
