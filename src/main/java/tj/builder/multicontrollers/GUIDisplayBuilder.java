package tj.builder.multicontrollers;

import gregicadditions.GAValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.recipes.CountableIngredient;
import gregtech.api.recipes.RecipeMap;
import gregtech.common.items.MetaItems;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.fluids.FluidStack;
import tj.TJValues;
import tj.capability.IItemFluidHandlerInfo;
import tj.gui.widgets.AdvancedDisplayWidget;
import tj.mixin.gregtech.IAbstractRecipeLogicMixin;
import tj.util.TJFluidUtils;
import tj.util.TJUtility;
import tj.util.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static gregtech.api.gui.widgets.AdvancedTextWidget.withButton;
import static tj.util.TJFluidUtils.VOID_TANK;

public final class GUIDisplayBuilder {

    private final List<AdvancedDisplayWidget.TextComponentWrapper<?>> textComponentWrappers = new ArrayList<>();
    private final boolean nested;
    private int count;

    public GUIDisplayBuilder(boolean nested) {
        this.nested = nested;
    }

    public int increment() {
        return this.count++;
    }

    public GUIDisplayBuilder customLine(Consumer<GUIDisplayBuilder> textList) {
        textList.accept(this);
        return this;
    }

    public GUIDisplayBuilder addTextComponent(ITextComponent component, int priority) {
        this.textComponentWrappers.add(new AdvancedDisplayWidget.TextComponentWrapper<>(component).setPriority(priority));
        return this;
    }

    public GUIDisplayBuilder addTextComponentWithHover(ITextComponent component, int priority, Consumer<GUIDisplayBuilder> uiBuilder) {
        if (this.nested)
            throw new IllegalArgumentException("Cannot set hover text on hover text");
        final GUIDisplayBuilder builder = new GUIDisplayBuilder(true);
        uiBuilder.accept(builder);
        this.textComponentWrappers.add(new AdvancedDisplayWidget.TextComponentWrapper<>(component).setPriority(priority)
                .setAdvancedHoverComponent(builder.getTextComponentWrappers()));
        return this;
    }

    public GUIDisplayBuilder addItemStack(ItemStack itemStack, int priority) {
        this.textComponentWrappers.add(new AdvancedDisplayWidget.TextComponentWrapper<>(itemStack).setPriority(priority));
        return this;
    }

    public GUIDisplayBuilder addItemStackWithHover(ItemStack itemStack, int priority, Consumer<GUIDisplayBuilder> uiBuilder) {
        if (this.nested)
            throw new IllegalArgumentException("Cannot set hover text on hover text");
        final GUIDisplayBuilder builder = new GUIDisplayBuilder(true);
        uiBuilder.accept(builder);
        this.textComponentWrappers.add(new AdvancedDisplayWidget.TextComponentWrapper<>(itemStack).setPriority(priority)
                .setAdvancedHoverComponent(builder.getTextComponentWrappers()));
        return this;
    }

    public GUIDisplayBuilder addIngredient(CountableIngredient ingredient, int priority) {
        this.textComponentWrappers.add(new AdvancedDisplayWidget.TextComponentWrapper<>(ingredient).setPriority(priority));
        return this;
    }

    public GUIDisplayBuilder addIngredientWithHover(CountableIngredient ingredient, int priority, Consumer<GUIDisplayBuilder> uiBuilder) {
        if (this.nested)
            throw new IllegalArgumentException("Cannot set hover text on hover text");
        final GUIDisplayBuilder builder = new GUIDisplayBuilder(true);
        uiBuilder.accept(builder);
        this.textComponentWrappers.add(new AdvancedDisplayWidget.TextComponentWrapper<>(ingredient).setPriority(priority)
                .setAdvancedHoverComponent(builder.getTextComponentWrappers()));
        return this;
    }

    public GUIDisplayBuilder addFluidStack(FluidStack fluidStack, int priority) {
        this.textComponentWrappers.add(new AdvancedDisplayWidget.TextComponentWrapper<>(fluidStack).setPriority(priority));
        return this;
    }

    public GUIDisplayBuilder addFluidStackWithHover(FluidStack fluidStack, int priority, Consumer<GUIDisplayBuilder> uiBuilder) {
        if (this.nested)
            throw new IllegalArgumentException("Cannot set hover text on hover text");
        final GUIDisplayBuilder builder = new GUIDisplayBuilder(true);
        uiBuilder.accept(builder);
        this.textComponentWrappers.add(new AdvancedDisplayWidget.TextComponentWrapper<>(fluidStack).setPriority(priority)
                .setAdvancedHoverComponent(builder.getTextComponentWrappers()));
        return this;
    }

    public GUIDisplayBuilder addTextComponent(ITextComponent component) {
        return this.addTextComponent(component, this.count++);
    }

    public GUIDisplayBuilder addTextComponentWithHover(ITextComponent component, Consumer<GUIDisplayBuilder> uiBuilder) {
        return this.addTextComponentWithHover(component, this.count++, uiBuilder);
    }

    public GUIDisplayBuilder addItemStack(ItemStack itemStack) {
        return this.addItemStack(itemStack, this.count++);
    }

    public GUIDisplayBuilder addItemStackWithHover(ItemStack itemStack, Consumer<GUIDisplayBuilder> uiBuilder) {
        return this.addItemStackWithHover(itemStack, this.count++, uiBuilder);
    }

    public GUIDisplayBuilder addFluidStack(FluidStack fluidStack) {
        return this.addFluidStack(fluidStack, this.count++);
    }

    public GUIDisplayBuilder addFluidStackWithHover(FluidStack fluidStack, Consumer<GUIDisplayBuilder> uiBuilder) {
        return this.addFluidStackWithHover(fluidStack, this.count++, uiBuilder);
    }

    public GUIDisplayBuilder addIngredient(CountableIngredient ingredient) {
        return this.addIngredient(ingredient, this.count++);
    }

    public GUIDisplayBuilder addIngredientWithHover(CountableIngredient ingredient, Consumer<GUIDisplayBuilder> uiBuilder) {
        return this.addIngredientWithHover(ingredient, this.count++, uiBuilder);
    }

    public GUIDisplayBuilder addTranslationLine(String locale, Object... format) {
        return this.addTranslationLine(0, locale, format);
    }

    public GUIDisplayBuilder addTranslationLine(int priority, String locale, Object... format) {
        return this.addTranslationLine(null, priority, locale, format);
    }

    public GUIDisplayBuilder addTranslationLine(Consumer<TextComponentTranslation> componentBuilder, String locale, Object... format) {
        return this.addTranslationLine(componentBuilder, 0, locale, format);
    }

    public GUIDisplayBuilder addTranslationLine(Consumer<TextComponentTranslation> componentBuilder, int priority, String locale, Object... format) {
        final TextComponentTranslation component = new TextComponentTranslation(locale, format);
        if (componentBuilder != null)
            componentBuilder.accept(component);
        if (priority != 0)
            return this.addTextComponent(component, priority);
        else return this.addTextComponent(component);
    }

    public GUIDisplayBuilder addEmptyLine() {
        return this.addEmptyLine(0);
    }

    public GUIDisplayBuilder addEmptyLine(int priority) {
        if (priority != 0)
            return this.addTextComponent(new TextComponentString(""), priority);
        else return this.addTextComponent(new TextComponentString(""));
    }

    public GUIDisplayBuilder addEnergyStoredLine(long energyStored, long energyCapacity) {
        return this.addEnergyStoredLine(energyStored, energyCapacity, 0);
    }

    public GUIDisplayBuilder addEnergyStoredLine(long energyStored, long energyCapacity, int priority) {
        if (priority != 0)
            return this.addTranslationLine(priority, "machine.universal.energy.stored", TJValues.thousandFormat.format(energyStored), TJValues.thousandFormat.format(energyCapacity));
        else return this.addTranslationLine("machine.universal.energy.stored", TJValues.thousandFormat.format(energyStored), TJValues.thousandFormat.format(energyCapacity));
    }

    public GUIDisplayBuilder addEnergyInputLine(IEnergyContainer container, long amount) {
        return this.addEnergyInputLine(container, amount, 1, 0);
    }

    public GUIDisplayBuilder addEnergyInputLine(IEnergyContainer container, long amount, int maxProgress) {
        return this.addEnergyInputLine(container, amount, maxProgress, 0);
    }

    public GUIDisplayBuilder addEnergyInputLine(IEnergyContainer container, long amount, int maxProgress, int priority) {
        if (amount == 0)
            return this;
        final ITextComponent textComponent = container.getEnergyStored() < amount ? new TextComponentTranslation("tj.multiblock.not_enough_energy")
                : maxProgress > 1 ? new TextComponentTranslation("tj.multiblock.parallel.sum.2", TJValues.thousandFormat.format(amount), TJValues.thousandFormat.format(maxProgress))
                : new TextComponentTranslation("tj.multiblock.parallel.sum", TJValues.thousandFormat.format(amount));
        if (priority != 0)
            return this.addTextComponent(textComponent, priority);
        else return this.addTextComponent(textComponent);
    }

    public GUIDisplayBuilder addVoltageTierLine(int tier) {
        return this.addVoltageTierLine(tier, 0);
    }

    public GUIDisplayBuilder addVoltageTierLine(int tier, int priority) {
        if (tier > 0) {
            final String text = tier > 14 ? "§c§lM§e§lA§a§lX§b§l+§d§l" + (tier - 14) : TJValues.VCC[tier] + GAValues.VN[tier] + "§r";
            if (priority != 0) {
                this.addTextComponent(new TextComponentTranslation("machine.universal.tooltip.voltage_tier")
                        .appendText(" §7(").appendSibling(new TextComponentString(text)).appendText("§7)"), priority);
            } else this.addTextComponent(new TextComponentTranslation("machine.universal.tooltip.voltage_tier")
                    .appendText(" §7(").appendSibling(new TextComponentString(text)).appendText("§7)"));
        }
        return this;
    }

    public GUIDisplayBuilder addVoltageInLine(IEnergyContainer energyContainer) {
        return this.addVoltageInLine(energyContainer, 0);
    }

    public GUIDisplayBuilder addVoltageInLine(IEnergyContainer energyContainer, int priority) {
        if (energyContainer != null && energyContainer.getEnergyCapacity() > 0) {
            long maxVoltage = energyContainer.getInputVoltage();
            if (maxVoltage >= Integer.MAX_VALUE)
                maxVoltage += maxVoltage / Integer.MAX_VALUE;
            final int tier = TJUtility.getTierByVoltage(maxVoltage);
            final String text = tier > 14 ? "§c§lM§e§lA§a§lX§b§l+§d§l" + (tier - 14) : TJValues.VCC[tier] + GAValues.VN[tier] + "§r";
            if (priority != 0) {
                this.addTextComponent(new TextComponentTranslation("tj.multiblock.max_energy_per_tick").appendText(" ")
                        .appendSibling(new TextComponentString("§e" + TJValues.thousandFormat.format(maxVoltage) + "§r")).appendText(" §7(")
                        .appendSibling(new TextComponentString(text)).appendText("§7)"), priority);
            } else this.addTextComponent(new TextComponentTranslation("tj.multiblock.max_energy_per_tick").appendText(" ")
                    .appendSibling(new TextComponentString("§e" + TJValues.thousandFormat.format(maxVoltage) + "§r")).appendText(" §7(")
                    .appendSibling(new TextComponentString(text)).appendText("§7)"));
        }
        return this;
    }

    public GUIDisplayBuilder addEnergyBonusLine(int energyBonus, boolean enabled) {
        return this.addEnergyBonusLine(energyBonus, enabled, 0);
    }

    public GUIDisplayBuilder addEnergyBonusLine(int energyBonus, boolean enabled, int priority) {
        if (enabled) {
            if (priority != 0) {
                this.addTextComponent(new TextComponentTranslation("gregtech.multiblock.universal.energy_usage", 100 - energyBonus)
                        .setStyle(new Style().setColor(TextFormatting.AQUA)), priority);
            } else this.addTextComponent(new TextComponentTranslation("gregtech.multiblock.universal.energy_usage", 100 - energyBonus)
                    .setStyle(new Style().setColor(TextFormatting.AQUA)));
        }
        return this;
    }

    public GUIDisplayBuilder addParallelLine(int parallelsPerformed, int parallel) {
        return this.addParallelLine(parallelsPerformed, parallel, 0);
    }

    public GUIDisplayBuilder addParallelLine(int parallelsPerformed, int parallel, int priority) {
        if (parallel < 1)
            return this;
        return priority != 0 ? this.addTranslationLine(priority, "tj.multiblock.max_parallel", TJValues.thousandFormat.format(parallelsPerformed), TJValues.thousandFormat.format(parallel))
                : this.addTranslationLine("tj.multiblock.max_parallel", TJValues.thousandFormat.format(parallelsPerformed), TJValues.thousandFormat.format(parallel));
    }

    public GUIDisplayBuilder addFluidInputLine(IMultipleTankHandler tanks, FluidStack fluidStack) {
        return this.addFluidInputLine(tanks, fluidStack, 0, 1);
    }

    public GUIDisplayBuilder addFluidInputLine(IMultipleTankHandler tanks, FluidStack fluidStack, long amount) {
        return this.addFluidInputLine(tanks, fluidStack, amount, 1);
    }

    public GUIDisplayBuilder addFluidInputLine(IMultipleTankHandler tanks, FluidStack fluidStack, long amount, int ticks) {
        return this.addFluidInputLine(tanks, fluidStack, amount, ticks, 0);
    }

    public GUIDisplayBuilder addFluidInputLine(IMultipleTankHandler tanks, FluidStack fluidStack, long amount, int ticks, int priority) {
        if (fluidStack == null)
            return this;
        amount = amount > 0 ? amount : fluidStack.amount;
        final String fluidName = fluidStack.getLocalizedName();
        final boolean hasEnoughFluid = amount < 1 || TJFluidUtils.drainFromTanksLong(tanks, fluidStack, amount, false) == amount;
        final ITextComponent fluidInputText = !hasEnoughFluid ? new TextComponentTranslation("tj.multiblock.not_enough_fluid", fluidName, TJValues.thousandFormat.format(amount))
                : ticks == 1 ? new TextComponentTranslation("machine.universal.fluid.input.tick", fluidName, TJValues.thousandFormat.format(amount))
                : ticks % 20 != 0 ? new TextComponentTranslation("machine.universal.fluid.input.ticks", TJValues.thousandFormat.format(amount), fluidName, TJValues.thousandFormat.format(ticks))
                : ticks == 20 ? new TextComponentTranslation("machine.universal.fluid.input.sec", fluidName, TJValues.thousandFormat.format(amount))
                : new TextComponentTranslation("machine.universal.fluid.input.secs", TJValues.thousandFormat.format(amount), fluidName, TJValues.thousandFormat.format(ticks / 20));
        if (priority != 0)
            return this.addTextComponent(fluidInputText, priority);
        else return this.addTextComponent(fluidInputText);
    }

    public GUIDisplayBuilder addFluidOutputLine(IMultipleTankHandler tanks, FluidStack fluidStack) {
        return this.addFluidOutputLine(tanks, fluidStack, 0, 1, 0);
    }

    public GUIDisplayBuilder addFluidOutputLine(IMultipleTankHandler tanks, FluidStack fluidStack, long amount) {
        return this.addFluidOutputLine(tanks, fluidStack, amount, 1, 0);
    }

    public GUIDisplayBuilder addFluidOutputLine(IMultipleTankHandler tanks, FluidStack fluidStack, long amount, int ticks) {
        return this.addFluidOutputLine(tanks, fluidStack, amount, ticks, 0);
    }

    public GUIDisplayBuilder addFluidOutputLine(IMultipleTankHandler tanks, FluidStack fluidStack, long amount, int ticks, int priority) {
        if (fluidStack == null)
            return this;
        amount = amount > 0 ? amount : fluidStack.amount;
        final String fluidName = fluidStack.getLocalizedName();
        final boolean hasEnoughFluid = amount < 1 || tanks == VOID_TANK || TJFluidUtils.fillIntoTanksLong(tanks, fluidStack, amount, false) == amount;
        final ITextComponent fluidInputText = !hasEnoughFluid ? new TextComponentTranslation("tj.multiblock.not_enough_fluid.space", fluidName, TJValues.thousandFormat.format(amount))
                : ticks == 1 ? new TextComponentTranslation("machine.universal.fluid.output.tick", fluidName, TJValues.thousandFormat.format(amount))
                : ticks % 20 != 0 ? new TextComponentTranslation("machine.universal.fluid.output.ticks", TJValues.thousandFormat.format(amount), fluidName, TJValues.thousandFormat.format(ticks))
                : ticks == 20 ? new TextComponentTranslation("machine.universal.fluid.output.sec", fluidName, TJValues.thousandFormat.format(amount))
                : new TextComponentTranslation("machine.universal.fluid.output.secs", fluidName, TJValues.thousandFormat.format(amount));
        if (priority != 0)
            return this.addTextComponent(fluidInputText, priority);
        else return this.addTextComponent(fluidInputText);
    }

    public GUIDisplayBuilder addIsWorkingLine(boolean isWorkingEnabled, boolean isActive, int progress, int maxProgress) {
        return this.addIsWorkingLine(isWorkingEnabled, isActive, progress, maxProgress, false, 0);
    }

    public GUIDisplayBuilder addIsWorkingLine(boolean isWorkingEnabled, boolean isActive, int progress, int maxProgress, int priority) {
        return this.addIsWorkingLine(isWorkingEnabled, isActive, progress, maxProgress, false, priority);
    }

    public GUIDisplayBuilder addIsWorkingLine(boolean isWorkingEnabled, boolean isActive, int progress, int maxProgress, boolean hasProblem) {
        return this.addIsWorkingLine(isWorkingEnabled, isActive, progress, maxProgress, hasProblem, 0);
    }

    public GUIDisplayBuilder addIsWorkingLine(boolean isWorkingEnabled, boolean isActive, int progress, int maxProgress, boolean hasProblems, int priority) {
        if (isActive) {
            progress--;
            final int currentProgress = (int) Math.floor(progress / (maxProgress * 1.0) * 100);
            if (priority != 0)
                this.addTranslationLine(priority, "tj.multiblock.progress", TJValues.thousandTwoPlaceFormat.format((double) progress / 20), TJValues.thousandTwoPlaceFormat.format((double) maxProgress / 20), currentProgress);
            else this.addTranslationLine("tj.multiblock.progress", TJValues.thousandTwoPlaceFormat.format((double) progress / 20), TJValues.thousandTwoPlaceFormat.format((double) maxProgress / 20), currentProgress);
        }
        final String isWorkingText = !isWorkingEnabled ? "machine.universal.work_paused"
                : hasProblems ? "machine.universal.has_problems"
                : !isActive ? "machine.universal.idling"
                : "machine.universal.running";
        if (priority != 0)
            this.addTranslationLine(priority, isWorkingText);
        else this.addTranslationLine(isWorkingText);
        return this;
    }

    public GUIDisplayBuilder addDistinctLine(boolean isDistinct) {
        return this.addDistinctLine(isDistinct, 0);
    }

    public GUIDisplayBuilder addDistinctLine(boolean isDistinct, int priority) {
        if (priority != 0) {
            this.addTextComponent(new TextComponentTranslation("gtadditions.multiblock.universal.distinct")
                    .setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation("gtadditions.multiblock.universal.distinct.info"))))
                    .appendText(" ")
                    .appendSibling(AdvancedTextWidget.withButton(new TextComponentTranslation(isDistinct ? "gtadditions.multiblock.universal.distinct.yes" : "gtadditions.multiblock.universal.distinct.no"), isDistinct ? "distinct:false" : "distinct:true")), priority);
        } else this.addTextComponent(new TextComponentTranslation("gtadditions.multiblock.universal.distinct")
                .setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation("gtadditions.multiblock.universal.distinct.info"))))
                .appendText(" ")
                .appendSibling(AdvancedTextWidget.withButton(new TextComponentTranslation(isDistinct ? "gtadditions.multiblock.universal.distinct.yes" : "gtadditions.multiblock.universal.distinct.no"), isDistinct ? "distinct:false" : "distinct:true")));
        return this;
    }

    public GUIDisplayBuilder addRecipeInputLine(IItemFluidHandlerInfo handlerInfo) {
        return this.addRecipeInputLine(handlerInfo, 0);
    }

    public GUIDisplayBuilder addRecipeInputLine(IItemFluidHandlerInfo handlerInfo, int priority) {
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

    public GUIDisplayBuilder addRecipeOutputLine(IItemFluidHandlerInfo handlerInfo) {
        return this.addRecipeOutputLine(handlerInfo, 0);
    }

    public GUIDisplayBuilder addRecipeOutputLine(IItemFluidHandlerInfo handlerInfo, int priority) {
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

    public GUIDisplayBuilder addRecipeOutputLine(AbstractRecipeLogic recipeLogic) {
        return this.addRecipeOutputLine(recipeLogic, 0);
    }

    public GUIDisplayBuilder addRecipeOutputLine(AbstractRecipeLogic recipeLogic, int priority) {
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

    public GUIDisplayBuilder addRecipeMapLine(RecipeMap<?> recipeMap) {
        return this.addTextComponent(new TextComponentTranslation("gtadditions.multiblock.universal.tooltip.1")
                .appendSibling(withButton(new TextComponentString("[" + TextUtils.translate("recipemap." + recipeMap.getUnlocalizedName() + ".name") + "]"), recipeMap.getUnlocalizedName())));
    }

    public GUIDisplayBuilder addTemperatureLine(long current, long max) {
        return this.addTemperatureLine(current, max, 0);
    }

    public GUIDisplayBuilder addTemperatureLine(long current, long max, int priority) {
        if (priority != 0)
            return this.addTranslationLine(priority, "tj.multiblock.temperature", TJValues.thousandFormat.format(current), TJValues.thousandFormat.format(max));
        else return this.addTranslationLine("tj.multiblock.temperature", TJValues.thousandFormat.format(current), TJValues.thousandFormat.format(max));
    }

    public GUIDisplayBuilder addMufflerDisplayLine(boolean isMufflerFaceFree) {
        return this.addMufflerDisplayLine(isMufflerFaceFree, 0);
    }

    public GUIDisplayBuilder addMufflerDisplayLine(boolean isMufflerFaceFree, int priority) {
        if (isMufflerFaceFree) return this;
        final ITextComponent component = new TextComponentTranslation("gtadditions.multiblock.universal.muffler_obstructed")
                .setStyle(new Style().setColor(TextFormatting.RED)
                        .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new TextComponentTranslation("gtadditions.multiblock.universal.muffler_obstructed.tooltip"))));
        if (priority != 0)
            return this.addTextComponent(component, priority);
        else return this.addTextComponent(component);
    }

    public GUIDisplayBuilder addMaintenanceDisplayLines(byte maintenanceProblems, boolean hasProblems) {
        return this.addMaintenanceDisplayLines(maintenanceProblems, hasProblems, 0);
    }

    public GUIDisplayBuilder addMaintenanceDisplayLines(byte maintenanceProblems, boolean hasProblems, int priority) {
        if (!hasProblems) {
            final ITextComponent hasNoProblemsComponent = new TextComponentTranslation("gtadditions.multiblock.universal.no_problems")
                    .setStyle(new Style().setColor(TextFormatting.GREEN));
            if (priority != 0)
                return this.addTextComponent(hasNoProblemsComponent, priority);
            else return this.addTextComponent(hasNoProblemsComponent);
        }
        final ITextComponent component = new TextComponentTranslation("gtadditions.multiblock.universal.has_problems")
                .setStyle(new Style().setColor(TextFormatting.DARK_RED));
        if (priority != 0)
            this.addTextComponent(component, priority);
        else this.addTextComponent(component);

        if (((maintenanceProblems) & 1) == 0) {
            final ITextComponent wrenchComponent = new TextComponentTranslation("gtadditions.multiblock.universal.problem.wrench")
                    .setStyle(new Style().setColor(TextFormatting.RED));
            if (priority != 0) {
                this.addTextComponentWithHover(wrenchComponent, priority, builder -> {
                    builder.addTranslationLine("gtadditions.multiblock.universal.problem.wrench.tooltip")
                            .addItemStack(MetaItems.WRENCH.getStackForm());
                });
            } else this.addTextComponentWithHover(wrenchComponent, builder -> {
                builder.addTranslationLine("gtadditions.multiblock.universal.problem.wrench.tooltip")
                        .addItemStack(MetaItems.WRENCH.getStackForm());
            });
        }
        if (((maintenanceProblems >> 1) & 1) == 0) {
            final ITextComponent screwdriverComponent = new TextComponentTranslation("gtadditions.multiblock.universal.problem.screwdriver")
                    .setStyle(new Style().setColor(TextFormatting.RED));
            if (priority != 0) {
                this.addTextComponentWithHover(screwdriverComponent, priority, builder -> {
                    builder.addTranslationLine("gtadditions.multiblock.universal.problem.screwdriver.tooltip")
                            .addItemStack(MetaItems.SCREWDRIVER.getStackForm());
                });
            } else this.addTextComponentWithHover(screwdriverComponent, builder -> {
                builder.addTranslationLine("gtadditions.multiblock.universal.problem.screwdriver.tooltip")
                        .addItemStack(MetaItems.SCREWDRIVER.getStackForm());
            });
        }
        if (((maintenanceProblems >> 2) & 1) == 0) {
            final ITextComponent softHammerComponent = new TextComponentTranslation("gtadditions.multiblock.universal.problem.softhammer")
                    .setStyle(new Style().setColor(TextFormatting.RED));
            if (priority != 0) {
                this.addTextComponentWithHover(softHammerComponent, priority, builder -> {
                    builder.addTranslationLine("gtadditions.multiblock.universal.problem.softhammer.tooltip")
                            .addItemStack(MetaItems.SOFT_HAMMER.getStackForm());
                });
            } else this.addTextComponentWithHover(softHammerComponent, builder -> {
                builder.addTranslationLine("gtadditions.multiblock.universal.problem.softhammer.tooltip")
                        .addItemStack(MetaItems.SOFT_HAMMER.getStackForm());
            });
        }
        if (((maintenanceProblems >> 3) & 1) == 0) {
            final ITextComponent hardHammerComponent = new TextComponentTranslation("gtadditions.multiblock.universal.problem.hardhammer")
                    .setStyle(new Style().setColor(TextFormatting.RED));
            if (priority != 0) {
                this.addTextComponentWithHover(hardHammerComponent, priority, builder -> {
                    builder.addTranslationLine("gtadditions.multiblock.universal.problem.hardhammer.tooltip")
                            .addItemStack(MetaItems.HARD_HAMMER.getStackForm());
                });
            } else this.addTextComponentWithHover(hardHammerComponent, builder -> {
                builder.addTranslationLine("gtadditions.multiblock.universal.problem.hardhammer.tooltip")
                        .addItemStack(MetaItems.HARD_HAMMER.getStackForm());
            });
        }
        if (((maintenanceProblems >> 4) & 1) == 0) {
            final ITextComponent wireCutterComponent = new TextComponentTranslation("gtadditions.multiblock.universal.problem.wirecutter")
                    .setStyle(new Style().setColor(TextFormatting.RED));
            if (priority != 0) {
                this.addTextComponentWithHover(wireCutterComponent, priority, builder -> {
                    builder.addTranslationLine("gtadditions.multiblock.universal.problem.wirecutter.tooltip")
                            .addItemStack(MetaItems.WIRE_CUTTER.getStackForm());
                });
            } else this.addTextComponentWithHover(wireCutterComponent, builder -> {
                builder.addTranslationLine("gtadditions.multiblock.universal.problem.wirecutter.tooltip")
                        .addItemStack(MetaItems.WIRE_CUTTER.getStackForm());
            });
        }
        if (((maintenanceProblems >> 5) & 1) == 0) {
            final ITextComponent crowbarComponent = new TextComponentTranslation("gtadditions.multiblock.universal.problem.crowbar")
                    .setStyle(new Style().setColor(TextFormatting.RED));
            if (priority != 0) {
                this.addTextComponentWithHover(crowbarComponent, priority, builder -> {
                    builder.addTranslationLine("gtadditions.multiblock.universal.problem.crowbar.tooltip")
                            .addItemStack(MetaItems.CROWBAR.getStackForm());
                });
            } else this.addTextComponentWithHover(crowbarComponent, builder -> {
                builder.addTranslationLine("gtadditions.multiblock.universal.problem.crowbar.tooltip")
                        .addItemStack(MetaItems.CROWBAR.getStackForm());
            });
        }
        return this;
    }

    public List<AdvancedDisplayWidget.TextComponentWrapper<?>> getTextComponentWrappers() {
        return this.textComponentWrappers;
    }
}
