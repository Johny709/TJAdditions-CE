package tj.builder.multicontrollers;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregicadditions.GAUtility;
import gregicadditions.Gregicality;
import gregicadditions.capabilities.IMultiRecipe;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.recipes.CountableIngredient;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import tj.TJValues;
import tj.builder.WidgetTabBuilder;
import tj.capability.IParallelController;
import tj.capability.IRecipeMap;
import tj.capability.OverclockManager;
import tj.capability.TJCapabilities;
import tj.capability.impl.handler.IRecipeHandler;
import tj.capability.impl.workable.ParallelRecipeLogic;
import tj.gui.TJGuiTextures;
import tj.gui.widgets.AdvancedDisplayWidget;
import tj.gui.widgets.TJCycleButtonWidget;
import tj.gui.widgets.impl.JEIRecipeTransferWidget;
import tj.gui.widgets.impl.ScrollableDisplayWidget;
import tj.machines.multi.BatchMode;
import tj.multiblockpart.TJMultiblockAbility;
import tj.multiblockpart.utility.MetaTileEntityMachineController;
import tj.util.TextUtils;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.LongStream;

import static gregicadditions.capabilities.MultiblockDataCodes.RECIPE_MAP_INDEX;
import static gregtech.api.gui.GuiTextures.TOGGLE_BUTTON_BACK;
import static gregtech.api.gui.widgets.AdvancedTextWidget.withButton;
import static gregtech.common.blocks.BlockTurbineCasing.TurbineCasingType.STEEL_GEARBOX;
import static tj.capability.TJMultiblockDataCodes.PARALLEL_LAYER;
import static tj.gui.TJGuiTextures.*;
import static tj.gui.TJGuiTextures.FLUID_VOID_BUTTON;

public abstract class ParallelRecipeMapMultiblockController extends TJMultiblockControllerBase implements IParallelController, IMultiRecipe, IRecipeHandler {

    private final RecipeMap<?>[] recipeMaps;
    private final ParallelRecipeLogic<? extends IRecipeHandler> recipeLogic = this.createRecipeLogic();
    protected BatchMode batchMode = BatchMode.ONE;
    protected int recipeMapIndex;
    protected int parallelLayer = 1;
    protected int energyBonus = -1;
    protected int tier;
    protected long maxVoltage;

    public ParallelRecipeMapMultiblockController(ResourceLocation metaTileEntityId, RecipeMap<?>... recipeMaps) {
        super(metaTileEntityId);
        this.recipeMaps = recipeMaps;
        this.recipeLogic.setActiveConsumer((active, i) -> this.activeDate = active ? Instant.now() : null);
        this.recipeLogic.setProblemConsumer((problem, i)-> this.activeDate = null);
        this.recipeLogic.setWorkingConsumer((working, i) -> {
            if (this.recipeLogic.isActive())
                this.activeDate = working ? Instant.now() : null;
        });
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < this.getRecipeMaps().length; i++) {
            builder.append(this.getRecipeMaps()[i].getLocalizedName());
            if (i < this.getRecipeMaps().length - 1)
                builder.append(", ");
        }
        tooltip.add(I18n.format("tj.multiblock.parallel.extend.tooltip"));
        tooltip.add(I18n.format("gtadditions.multiblock.universal.tooltip.1", builder.toString()));
        tooltip.add(I18n.format("gtadditions.multiblock.universal.tooltip.2", TJValues.thousandTwoPlaceFormat.format(this.getEUtMultiplier() / 100.0)));
        tooltip.add(I18n.format("gtadditions.multiblock.universal.tooltip.3", TJValues.thousandTwoPlaceFormat.format(this.getDurationMultiplier() / 100.0)));
        tooltip.add(I18n.format("tj.multiblock.parallel.tooltip.1", this.getParallel()));
        tooltip.add(I18n.format("tj.multiblock.parallel.tooltip.2", this.getMaxParallel()));
        tooltip.add(I18n.format("gtadditions.multiblock.universal.tooltip.5", this.getChanceMultiplier()));
    }

    protected ParallelRecipeLogic<? extends IRecipeHandler> createRecipeLogic() {
        return new ParallelRecipeLogic<>(this);
    }

    @Override
    protected boolean checkStructureComponents(List<IMultiblockPart> parts, Map<MultiblockAbility<Object>, List<Object>> abilities) {
        return !abilities.getOrDefault(MultiblockAbility.INPUT_ENERGY, Collections.emptyList()).isEmpty() &&
                abilities.getOrDefault(MultiblockAbility.IMPORT_ITEMS, Collections.emptyList()).size() >= Math.min(1, this.getRecipeMap().getMinInputs()) &&
                abilities.getOrDefault(MultiblockAbility.EXPORT_ITEMS, Collections.emptyList()).size() >= Math.min(1, this.getRecipeMap().getMinOutputs()) &&
                abilities.getOrDefault(MultiblockAbility.IMPORT_FLUIDS, Collections.emptyList()).size() >= Math.min(1, this.getRecipeMap().getMinFluidInputs()) &&
                abilities.getOrDefault(MultiblockAbility.EXPORT_FLUIDS, Collections.emptyList()).size() >= Math.min(1, this.getRecipeMap().getMinFluidOutputs()) &&
                super.checkStructureComponents(parts, abilities);
    }

    @Override
    public void preOverclock(OverclockManager<?> overclockManager, Recipe recipe) {
        overclockManager.setChanceMultiplier(this.getChanceMultiplier());
        overclockManager.setEUt(overclockManager.getEUt() * this.getEUtMultiplier() / 100);
        overclockManager.setDuration(overclockManager.getDuration() * this.getDurationMultiplier() / 100);
        overclockManager.setParallel(this.getParallel() * this.getTierDifference(overclockManager.getEUt()) * this.batchMode.getAmount());
    }

    @Override
    public void postOverclock(OverclockManager<?> overclockManager, Recipe recipe) {
        overclockManager.setDuration(overclockManager.getDuration() * this.batchMode.getAmount());
    }

    @Override
    protected boolean shouldUpdate(MTETrait trait) {
        return false;
    }

    @Override
    protected void updateFormedValid() {
        if (this.isWorkingEnabled && ((this.getProblems() >> 5) & 1) != 0)
            for (int i = 0; i < this.recipeLogic.getSize(); i++)
                this.recipeLogic.update(i);
    }

    @Override
    protected void addTabs(WidgetTabBuilder tabBuilder, EntityPlayer player) {
        super.addTabs(tabBuilder, player);
        tabBuilder.addWidget(new JEIRecipeTransferWidget(0, 0, 100, 100)
                .setRecipeConsumer(this::setRecipe));
        tabBuilder.addTab("tj.multiblock.tab.workable", MetaBlocks.TURBINE_CASING.getItemVariant(STEEL_GEARBOX), workableTab -> {
            workableTab.add(new TJCycleButtonWidget(175, 133, 18, 18, BatchMode.class, this::getBatchMode, this::setBatchMode, BUTTON_BATCH_ONE, BUTTON_BATCH_FOUR, BUTTON_BATCH_SIXTEEN, BUTTON_BATCH_SIXTY_FOUR, BUTTON_BATCH_TWO_HUNDRED_FIFTY_SIX)
                    .setTooltipFormat(() -> ArrayUtils.toArray(String.valueOf(this.batchMode.getAmount())))
                    .setToggle(true)
                    .setButtonTexture(TOGGLE_BUTTON_BACK)
                    .setTooltipHoverString("machine.universal.batch.amount"));
            workableTab.add(new ToggleButtonWidget(175, 151, 18, 18, ITEM_VOID_BUTTON, this.recipeLogic::isVoidingItems, this.recipeLogic::setVoidingItems)
                    .setTooltipText("machine.universal.toggle.item_voiding"));
            workableTab.add(new ToggleButtonWidget(175, 169, 18, 18, FLUID_VOID_BUTTON, this.recipeLogic::isVoidingFluids, this.recipeLogic::setVoidingFluids)
                    .setTooltipText("machine.universal.toggle.fluid_voiding"));
            workableTab.add(new ScrollableDisplayWidget(10, -15, 183, 142)
                    .addDisplayWidget(new AdvancedDisplayWidget(0, 2, this::addWorkableDisplayText, 0xFFFFFF)
                            .setClickHandler(this::handleWorkableDisplayClick)
                            .setMaxWidthLimit(180))
                    .setScrollPanelWidth(3));
        });
        tabBuilder.addTab("tj.multiblock.tab.debug", MetaItems.WRENCH.getStackForm(), debugTab -> {
            debugTab.add(new ToggleButtonWidget(175, 133, 18, 18, RESET_BUTTON, () -> false, b -> this.recipeLogic.getRecipeLRUCache().clear())
                    .setTooltipText("tj.multiblock.parallel.recipe.clear"));
            debugTab.add(new ToggleButtonWidget(175, 151, 18, 18, ITEM_VOID_BUTTON, this.recipeLogic::isVoidingItems, this.recipeLogic::setVoidingItems)
                    .setTooltipText("machine.universal.toggle.item_voiding"));
            debugTab.add(new ToggleButtonWidget(175, 169, 18, 18, FLUID_VOID_BUTTON, this.recipeLogic::isVoidingFluids, this.recipeLogic::setVoidingFluids)
                    .setTooltipText("machine.universal.toggle.fluid_voiding"));
            debugTab.add(new ScrollableDisplayWidget(10, -15, 183, 142)
                    .addDisplayWidget(new AdvancedDisplayWidget(0, 2, this::addDebugDisplayText, 0xFFFFFF)
                            .setMaxWidthLimit(180))
                    .setScrollPanelWidth(3));
        });
    }

    @Override
    protected void mainDisplayTab(List<Widget> widgetGroup) {
        super.mainDisplayTab(widgetGroup);
        widgetGroup.add(new ToggleButtonWidget(175, 151, 18, 18, TJGuiTextures.DISTINCT_BUTTON, this.recipeLogic::isDistinct, this.recipeLogic::setDistinct)
                .setTooltipText("machine.universal.toggle.distinct.mode"));
    }

    @Override
    protected void addDisplayText(GUIDisplayBuilder builder) {
        super.addDisplayText(builder);
        if (!this.isStructureFormed()) return;
        builder.addVoltageInLine(this.inputEnergyContainer)
                .addVoltageTierLine(GAUtility.getTierByVoltage(this.maxVoltage))
                .addEnergyInputLine(this.inputEnergyContainer, this.getTotalEnergyConsumption())
                .addEnergyBonusLine(this.energyBonus, this.isStructureFormed() && this.energyBonus >= 0)
                .addRecipeMapLine(this.getMultiblockRecipe());
    }

    @Override
    protected void handleDisplayClick(String componentData, Widget.ClickData clickData) {
        if (this.recipeLogic.isActive() || !componentData.equals(this.getMultiblockRecipe().getUnlocalizedName())) return;
        this.recipeLogic.getRecipeLRUCache().clear();
        this.recipeMapIndex = this.recipeMapIndex >= this.recipeMaps.length - 1 ? 0 : this.recipeMapIndex + 1;
        if (!this.getWorld().isRemote) {
            this.writeCustomData(RECIPE_MAP_INDEX, buf -> buf.writeInt(this.recipeMapIndex));
            this.markDirty();
        }
    }

    private void addWorkableDisplayText(GUIDisplayBuilder builder) {
        builder.addTranslationLine("tj.multiblock.industrial_fusion_reactor.message", this.parallelLayer)
                .addTextComponent(new TextComponentTranslation("tj.multiblock.parallel.distinct").appendText(" ")
                        .appendSibling(this.recipeLogic.isDistinctRecipes() ? withButton(new TextComponentTranslation("machine.universal.toggle.run.mode.enabled"), "isDistinct")
                                : withButton(new TextComponentTranslation("machine.universal.toggle.run.mode.disabled"), "notDistinct")));
        if (!this.isStructureFormed()) return;
        for (int i = 0; i < this.recipeLogic.getSize(); i++) {
            int parallel = this.recipeLogic.getParallel(i);
            double progressPercent = this.recipeLogic.getProgressPercent(i) * 100;
            String isRunning = !this.recipeLogic.isWorkingEnabled(i) ? TextUtils.translate("machine.universal.work_paused")
                    : this.recipeLogic.hasProblems(i) ? TextUtils.translate("machine.universal.has_problems")
                    : !this.recipeLogic.isInstanceActive(i) ? TextUtils.translate("machine.universal.idling")
                    : TextUtils.translate("machine.universal.running");
            int finalI = i;
            builder.addTextComponentWithHover(new TextComponentString(": [§a" + (i + 1) + "§r] " + isRunning)
                    .appendText(" ")
                    .appendSibling(this.recipeLogic.isRecipeLocked(i) ? withButton(new TextComponentTranslation("tj.multiblock.parallel.lock"), "lock:" + i)
                            : withButton(new TextComponentTranslation("tj.multiblock.parallel.unlock"), "unlock:" + finalI))
                    .appendText(" ")
                    .appendSibling(withButton(new TextComponentTranslation("machine.universal.linked.remove"), "remove:" + finalI)), hoverBuilder -> {
                hoverBuilder.addTranslationLine("tj.multiblock.parallel.status", isRunning)
                        .addTranslationLine("tj.multiblock.handler", finalI + 1)
                        .addTranslationLine("tj.multiblock.eu", this.recipeLogic.getRecipeEUt(finalI))
                        .addTranslationLine("tj.multiblock.progress", TJValues.thousandTwoPlaceFormat.format((double) this.recipeLogic.getProgress(finalI) / 20), TJValues.thousandTwoPlaceFormat.format((double) this.recipeLogic.getMaxProgress(finalI) / 20), (int) progressPercent)
                        .addTranslationLine("tj.multiblock.parallel", parallel);
                List<ItemStack> itemInputs = this.recipeLogic.getItemInputsAt(finalI);
                List<FluidStack> fluidInputs = this.recipeLogic.getFluidInputsAt(finalI);
                if (itemInputs != null && !itemInputs.isEmpty() || fluidInputs != null && !fluidInputs.isEmpty())
                    hoverBuilder.addTranslationLine("machine.universal.consumption");
                if (itemInputs != null) {
                    for (ItemStack stack : itemInputs) {
                        hoverBuilder.addItemStack(stack);
                    }
                }
                if (fluidInputs != null) {
                    for (FluidStack stack : fluidInputs) {
                        hoverBuilder.addFluidStack(stack);
                    }
                }
                List<ItemStack> itemOutputs = this.recipeLogic.getItemOutputsAt(finalI);
                List<FluidStack> fluidOutputs = this.recipeLogic.getFluidOutputsAt(finalI);
                if (itemOutputs != null && !itemOutputs.isEmpty() || fluidOutputs != null && !fluidOutputs.isEmpty())
                    hoverBuilder.addTranslationLine("machine.universal.producing");
                if (itemOutputs != null) {
                    for (ItemStack stack : itemOutputs) {
                        hoverBuilder.addItemStack(stack);
                    }
                }
                if (fluidOutputs != null) {
                    for (FluidStack stack : fluidOutputs) {
                        hoverBuilder.addFluidStack(stack);
                    }
                }
            });
        }
    }

    private void addDebugDisplayText(GUIDisplayBuilder builder) {
        builder.addTranslationLine("tj.multiblock.parallel.debug.cache.capacity", this.recipeLogic.getRecipeLRUCache().getCapacity())
                .addTranslationLine(text -> text.setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation("tj.multiblock.parallel.debug.cache.hit.info")))),
                        "tj.multiblock.parallel.debug.cache.hit", this.recipeLogic.getRecipeLRUCache().getCacheHit())
                .addTranslationLine(text -> text.setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation("tj.multiblock.parallel.debug.cache.miss.info")))),
                        "tj.multiblock.parallel.debug.cache.miss", this.recipeLogic.getRecipeLRUCache().getCacheMiss())
                .addEmptyLine();
        int i = 1;
        for (Recipe recipe : this.recipeLogic.getRecipeLRUCache()) {
            builder.addTranslationLine("tj.multiblock.recipe_cache.slot", i++)
                    .addTranslationLine("tj.multiblock.recipe_cache.inputs");
            for (CountableIngredient ingredient : recipe.getInputs())
                builder.addItemStack(ingredient.getIngredient().getMatchingStacks()[0]);
            for (FluidStack stack : recipe.getFluidInputs())
                builder.addFluidStack(stack);
            if (!recipe.getOutputs().isEmpty() || !recipe.getFluidOutputs().isEmpty())
                builder.addTranslationLine("tj.multiblock.recipe_cache.outputs");
            for (ItemStack stack : recipe.getOutputs())
                builder.addItemStack(stack);
            for (FluidStack stack : recipe.getFluidOutputs())
                builder.addFluidStack(stack);
            if (!recipe.getChancedOutputs().isEmpty())
                builder.addTranslationLine("tj.multiblock.recipe_cache.chanced_outputs");
            for (Recipe.ChanceEntry entry : recipe.getChancedOutputs())
                builder.addItemStack(entry.getItemStack());
        }
    }

    private void handleWorkableDisplayClick(String componentData, Widget.ClickData clickData) {
        switch (componentData) {
            case "isDistinct":
                this.recipeLogic.setDistinctRecipes(false);
                return;
            case "notDistinct":
                this.recipeLogic.setDistinctRecipes(true);
                return;
            default:
                if (componentData.startsWith("lock")) {
                    String[] lock = componentData.split(":");
                    int index = Integer.parseInt(lock[1]);
                    this.recipeLogic.setRecipeLock(false, index);

                } else if (componentData.startsWith("unlock")) {
                    String[] unlock = componentData.split(":");
                    int index = Integer.parseInt(unlock[1]);
                    this.recipeLogic.setRecipeLock(true, index);

                } else if (componentData.startsWith("remove")) {
                    String[] remove = componentData.split(":");
                    int index = Integer.parseInt(remove[1]);
                    this.recipeLogic.setRecipe(null, index);
                }
        }
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        int size = this.recipeLogic.getSize();
        for (int i = 0; i < getAbilities(TJMultiblockAbility.REDSTONE_CONTROLLER).size(); i++) {
            MetaTileEntityMachineController controller = getAbilities(TJMultiblockAbility.REDSTONE_CONTROLLER).get(i);
            if (controller.isAutomatic() || controller.getId() >= size)
                controller.setID(Math.min(i, size - 1)).setController(this);
        }
    }

    @Override
    public void invalidateStructure() {
        for (MetaTileEntityMachineController controller : this.getAbilities(TJMultiblockAbility.REDSTONE_CONTROLLER))
            controller.setID(0).setController(null);
        super.invalidateStructure();
        this.recipeLogic.invalidate();
        this.maxVoltage = 0;
        this.tier = 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().render(renderState, translation, pipeline, this.getFrontFacing(), this.recipeLogic.isActive());
    }

    @Override
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        return !MetaItems.SCREWDRIVER.isItemEqual(playerIn.getHeldItem(hand)) && super.onRightClick(playerIn, hand, facing, hitResult);
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        if (!this.getWorld().isRemote) {
            int lastParallelLayer = this.parallelLayer;
            this.parallelLayer = MathHelper.clamp(playerIn.isSneaking() ? this.parallelLayer - 1 : this.parallelLayer + 1, 1, this.getMaxParallel());
            if (this.parallelLayer != lastParallelLayer) {
                playerIn.sendMessage(TextUtils.addTranslationText(playerIn.isSneaking() ? "tj.multiblock.parallel.layer.decrement.success" : "tj.multiblock.parallel.layer.increment.success", this.parallelLayer));
                this.recipeLogic.setLayer(this.parallelLayer, playerIn.isSneaking());
            } else playerIn.sendMessage(TextUtils.addTranslationText(playerIn.isSneaking() ? "tj.multiblock.parallel.layer.decrement.fail" : "tj.multiblock.parallel.layer.increment.fail", this.parallelLayer));
            this.resetStructure();
            this.writeCustomData(PARALLEL_LAYER, buf -> buf.writeInt(this.parallelLayer));
            this.markDirty();
        }
        return true;
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == PARALLEL_LAYER) {
            this.parallelLayer = buf.readInt();
            this.resetStructure();
            this.scheduleRenderUpdate();
        } else if (dataId == RECIPE_MAP_INDEX) {
            this.recipeMapIndex = buf.readInt();
            this.scheduleRenderUpdate();
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeInt(this.parallelLayer);
        buf.writeInt(this.recipeMapIndex);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.parallelLayer = buf.readInt();
        this.recipeMapIndex = buf.readInt();
        this.resetStructure();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("parallelLayer", this.parallelLayer);
        data.setInteger("recipeMapIndex", this.recipeMapIndex);
        data.setInteger("batchMode", this.batchMode.ordinal());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.parallelLayer = data.getInteger("parallelLayer");
        this.recipeMapIndex = data.getInteger("recipeMapIndex");
        this.batchMode = BatchMode.values()[data.getInteger("batchMode")];
        this.resetStructure();
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == TJCapabilities.CAPABILITY_PARALLEL_CONTROLLER)
            return TJCapabilities.CAPABILITY_PARALLEL_CONTROLLER.cast(this);
        return super.getCapability(capability, side);
    }

    private void setRecipe(List<ItemStack> itemInputs, List<ItemStack> itemOutputs, List<FluidStack> fluidInputs, List<FluidStack> fluidOutput, EntityPlayer player) {
        for (int i = 0; i < this.recipeLogic.getSize(); i++) {
            if (this.recipeLogic.getRecipe(i) == null) {
                Recipe newRecipe = ((IRecipeMap) this.recipeMaps[this.getRecipeMapIndex()]).findByInputsAndOutputs(this.maxVoltage, itemInputs, itemOutputs, fluidInputs, fluidOutput);
                this.recipeLogic.setRecipe(newRecipe, i);
                player.sendMessage(newRecipe != null ? this.displayRecipe(TextUtils.addTranslationText("tj.multiblock.recipe.transfer.success", i + 1), newRecipe)
                        : TextUtils.addTranslationText("tj.multiblock.recipe.transfer.fail_2", i + 1));
                return;
            }
        }
        player.sendMessage(TextUtils.addTranslationText("tj.multiblock.recipe.transfer.fail"));
    }

    private ITextComponent displayRecipe(ITextComponent textComponent, Recipe recipe) {
        return textComponent.appendText("\n")
                .appendSibling(this.displayItemInputs(recipe))
                .appendText("\n")
                .appendSibling(this.displayFluids(recipe.getFluidInputs(), "tj.multiblock.parallel.advanced.fluidInput"))
                .appendText("\n")
                .appendSibling(this.displayItemOutputs(recipe))
                .appendText("\n")
                .appendSibling(this.displayFluids(recipe.getFluidOutputs(), "tj.multiblock.parallel.advanced.fluidOutput"));
    }

    private ITextComponent displayItemInputs(Recipe recipe) {
        ITextComponent itemInputs = TextUtils.addTranslationText("tj.multiblock.parallel.advanced.itemInputs");
        for (CountableIngredient item : recipe.getInputs()) {
            itemInputs.appendText("\n-");
            itemInputs.appendSibling(new TextComponentString("§6" + item.getIngredient().getMatchingStacks()[0].getDisplayName()));
            itemInputs.appendText(" §7{");
            itemInputs.appendText("§6" + String.format("%,d", item.getCount()) + "§7}");
        }
        return itemInputs;
    }

    private ITextComponent displayItemOutputs(Recipe recipe) {
        ITextComponent itemOutputs = TextUtils.addTranslationText("tj.multiblock.parallel.advanced.itemOutputs");
        for (ItemStack item : recipe.getOutputs()) {
            itemOutputs.appendText("\n-");
            itemOutputs.appendSibling(new TextComponentString("§6" + item.getDisplayName()));
            itemOutputs.appendText(" §7{");
            itemOutputs.appendText("§6" + String.format("%,d", item.getCount()) + "§7}");
        }
        for (Recipe.ChanceEntry entry : recipe.getChancedOutputs()) {
            itemOutputs.appendText("\n-");
            itemOutputs.appendSibling(new TextComponentString("§6" + entry.getItemStack().getDisplayName()));
            itemOutputs.appendText(" §7{");
            itemOutputs.appendText("§6" + String.format("%,d", entry.getItemStack().getCount()) + "§7}");
            itemOutputs.appendText(" §7{");
            itemOutputs.appendText("§6" + TextUtils.addTranslationText("gregtech.recipe.chance", entry.getChance() / 100, entry.getBoostPerTier() / 100) + "§7}");
        }
        return itemOutputs;
    }

    private ITextComponent displayFluids(List<FluidStack> fluidStacks, String fluidTextLocale) {
        ITextComponent fluidInputs = TextUtils.addTranslationText(fluidTextLocale);
        for (FluidStack fluid : fluidStacks) {
            fluidInputs.appendText("\n-");
            fluidInputs.appendSibling(new TextComponentString("§b" + fluid.getLocalizedName()));
            fluidInputs.appendText(" §7{");
            fluidInputs.appendText("§b" + String.format("%,d", fluid.amount) + "L§7}");
        }
        return fluidInputs;
    }

    @Override
    public String getRecipeUid() {
        return Gregicality.MODID + ":" + this.getRecipeMap().getUnlocalizedName();
    }

    @Override
    public RecipeMap<?> getRecipeMap() {
        return this.recipeMaps[this.getRecipeMapIndex()];
    }

    @Override
    public RecipeMap<?> getMultiblockRecipe() {
        return this.getRecipeMap();
    }

    @Override
    public int getRecipeMapIndex() {
        return this.recipeMapIndex;
    }

    @Override
    public RecipeMap<?>[] getRecipeMaps() {
        return this.recipeMaps;
    }

    @Override
    public void addRecipeMaps(RecipeMap<?>[] recipeMaps) {
        // don't plan on adding more recipeMaps once initialized
    }

    @Override
    public long getMaxVoltage() {
        return this.maxVoltage;
    }

    @Override
    public int getTier() {
        return this.tier;
    }

    public void setBatchMode(BatchMode batchMode) {
        this.batchMode = batchMode;
        this.markDirty();
    }

    @Override
    public BatchMode getBatchMode() {
        return this.batchMode;
    }

    public int getTierDifference(long recipeEUt) {
        return this.getTier() - GAUtility.getTierByVoltage(recipeEUt);
    }

    @Override
    public long getMaxEUt() {
        return this.inputEnergyContainer.getInputVoltage();
    }

    @Override
    public long getTotalEnergyConsumption() {
        return LongStream.range(0, this.recipeLogic.getSize())
                .map(i -> this.recipeLogic.getRecipeEUt((int) i))
                .sum();
    }

    @Override
    public long getVoltageTier() {
        return this.maxVoltage;
    }

    @Override
    public int getEUBonus() {
        return this.energyBonus;
    }

    public int getEUtMultiplier() {
        return 100;
    }

    public int getDurationMultiplier() {
        return 100;
    }

    public int getChanceMultiplier() {
        return 100;
    }

    public int getMaxParallel() {
        return 1;
    }

    @Override
    protected void reinitializeStructurePattern() {
        this.parallelLayer = 1;
        super.reinitializeStructurePattern();
    }

    private void resetStructure() {
        if (this.isStructureFormed())
            this.invalidateStructure();
        this.structurePattern = this.createStructurePattern();
    }
}
