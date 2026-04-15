package tj.builder.multicontrollers;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregicadditions.GAUtility;
import gregicadditions.GAValues;
import gregicadditions.Gregicality;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.recipes.CountableIngredient;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.common.items.MetaItems;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.TJValues;
import tj.builder.WidgetTabBuilder;
import tj.capability.OverclockManager;
import tj.capability.impl.handler.IRecipeHandler;
import tj.capability.impl.workable.BasicRecipeLogic;
import tj.gui.TJGuiTextures;
import tj.gui.widgets.AdvancedDisplayWidget;
import tj.gui.widgets.impl.ScrollableDisplayWidget;
import tj.textures.TJOrientedOverlayRenderer;
import tj.textures.TJTextures;
import tj.util.EnumFacingHelper;
import tj.util.TJUtility;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static tj.gui.TJGuiTextures.*;

public abstract class TJRecipeMapMultiblockController extends TJMultiblockControllerBase implements IRecipeHandler {

    protected final BasicRecipeLogic<? extends IRecipeHandler> recipeLogic = this.createRecipeLogic();
    protected final RecipeMap<?> recipeMap;
    protected long maxVoltage;
    protected int tier;

    public TJRecipeMapMultiblockController(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap) {
        this(metaTileEntityId, recipeMap, true, true);
    }

    public TJRecipeMapMultiblockController(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, boolean hasMaintenance, boolean hasDistinct) {
        super(metaTileEntityId, hasMaintenance, hasDistinct);
        this.recipeMap = recipeMap;
        this.recipeLogic.setActiveConsumer(active -> this.activeDate = active ? Instant.now() : null);
        this.recipeLogic.setProblemConsumer(problem -> this.activeDate = null);
        this.recipeLogic.setWorkingConsumer(working -> {
            if (this.recipeLogic.isActive())
                this.activeDate = working ? Instant.now() : null;
        });
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gtadditions.multiblock.universal.tooltip.1", this.getRecipeMapNames()));
        tooltip.add(I18n.format("gtadditions.multiblock.universal.tooltip.2", TJValues.thousandTwoPlaceFormat.format(this.getEUtMultiplier() / 100.0)));
        tooltip.add(I18n.format("gtadditions.multiblock.universal.tooltip.3", TJValues.thousandTwoPlaceFormat.format(this.getDurationMultiplier() / 100.0)));
        if (this.getParallel() > 0)
            tooltip.add(I18n.format("gtadditions.multiblock.universal.tooltip.4", this.getParallel()));
        tooltip.add(I18n.format("gtadditions.multiblock.universal.tooltip.5", this.getChanceMultiplier()));
    }

    protected BasicRecipeLogic<? extends IRecipeHandler> createRecipeLogic() {
        return new BasicRecipeLogic<>(this);
    }

    @Override
    protected boolean checkStructureComponents(List<IMultiblockPart> parts, Map<MultiblockAbility<Object>, List<Object>> abilities) {
        return (!this.usesEnergy() || !abilities.getOrDefault(MultiblockAbility.INPUT_ENERGY, Collections.emptyList()).isEmpty()) &&
                abilities.getOrDefault(MultiblockAbility.IMPORT_ITEMS, Collections.emptyList()).size() >= Math.min(1, this.recipeMap.getMinInputs()) &&
                abilities.getOrDefault(MultiblockAbility.EXPORT_ITEMS, Collections.emptyList()).size() >= Math.min(1, this.recipeMap.getMinOutputs()) &&
                abilities.getOrDefault(MultiblockAbility.IMPORT_FLUIDS, Collections.emptyList()).size() >= Math.min(1, this.recipeMap.getMinFluidInputs()) &&
                abilities.getOrDefault(MultiblockAbility.EXPORT_FLUIDS, Collections.emptyList()).size() >= Math.min(1, this.recipeMap.getMinFluidOutputs()) &&
                super.checkStructureComponents(parts, abilities);
    }

    @Override
    protected boolean shouldUpdate(MTETrait trait) {
        return false;
    }

    @Override
    protected void updateFormedValid() {
        if (((this.getProblems() >> 5) & 1) != 0)
            this.recipeLogic.update();
    }

    @Override
    public void preOverclock(OverclockManager<?> overclockManager, Recipe recipe) {
        overclockManager.setChanceMultiplier(this.getChanceMultiplier());
        overclockManager.setEUt(overclockManager.getEUt() * this.getEUtMultiplier() / 100);
        overclockManager.setDuration(overclockManager.getDuration() * this.getDurationMultiplier() / 100);
        overclockManager.setParallel(this.getParallel() * this.getTierDifference(overclockManager.getEUt()));
    }

    @Override
    protected void addTabs(WidgetTabBuilder tabBuilder, EntityPlayer player) {
        super.addTabs(tabBuilder, player);
        tabBuilder.addTab("tj.multiblock.tab.debug", MetaItems.WRENCH.getStackForm(), debugTab -> {
            debugTab.add(new ToggleButtonWidget(175, 133, 18, 18, RESET_BUTTON, () -> false, b -> this.recipeLogic.getRecipeLRUCache().clear())
                    .setTooltipText("tj.multiblock.parallel.recipe.clear"));
            debugTab.add(new ToggleButtonWidget(175, 151, 18, 18, ITEM_VOID_BUTTON, this.recipeLogic::isVoidingItems, this.recipeLogic::setVoidingItems)
                    .setTooltipText("machine.universal.toggle.item_voiding"));
            debugTab.add(new ToggleButtonWidget(175, 169, 18, 18, FLUID_VOID_BUTTON, this.recipeLogic::isVoidingFluids, this.recipeLogic::setVoidingFluids)
                    .setTooltipText("machine.universal.toggle.fluid_voiding"));
            debugTab.add(new ScrollableDisplayWidget(10, -11, 187, 140)
                    .addDisplayWidget(new AdvancedDisplayWidget(0, 0, this::addDebugDisplayText, 0xFFFFFF)
                            .setMaxWidthLimit(180))
                    .setScrollPanelWidth(3));
        });
    }

    @Override
    protected void mainDisplayTab(List<Widget> widgetGroup) {
        super.mainDisplayTab(widgetGroup);
        if (!this.hasDistinct()) return;
        widgetGroup.add(new ToggleButtonWidget(175, 151, 18, 18, TJGuiTextures.DISTINCT_BUTTON, this.recipeLogic::isDistinct, this.recipeLogic::setDistinct)
                .setTooltipText("machine.universal.toggle.distinct.mode"));
    }

    @Override
    protected void addDisplayText(GUIDisplayBuilder builder) {
        super.addDisplayText(builder);
        if (!this.isStructureFormed()) return;
        builder.addVoltageInLine(this.inputEnergyContainer)
                .addVoltageTierLine(this.tier)
                .addEnergyInputLine(this.inputEnergyContainer, this.recipeLogic.getEnergyPerTick())
                .addParallelLine(this.recipeLogic.getParallelsPerformed(), this.recipeLogic.getParallel())
                .addIsWorkingLine(this.recipeLogic.isWorkingEnabled(), this.recipeLogic.isActive(), this.recipeLogic.getProgress(), this.recipeLogic.getMaxProgress(), this.recipeLogic.hasProblem(), 998)
                .addRecipeInputLine(this.recipeLogic, 999)
                .addRecipeOutputLine(this.recipeLogic, 1000);
        if (this.hasDistinct())
            builder.addDistinctLine(this.recipeLogic.isDistinct(), 997);
    }

    protected void addDebugDisplayText(GUIDisplayBuilder builder) {
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
                builder.addIngredient(ingredient);
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

    @Override
    protected void handleDisplayClick(String componentData, Widget.ClickData clickData) {
        final String[] data = componentData.split(":");
        if (data[0].equals("distinct"))
            this.recipeLogic.setDistinct(data[1].equals("true"));
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.recipeLogic.initialize(this.getAbilities(MultiblockAbility.IMPORT_ITEMS).size());
        this.maxVoltage = Math.max(this.inputEnergyContainer.getInputVoltage(), this.outputEnergyContainer.getOutputVoltage());
        this.tier = this.maxVoltage >= Integer.MAX_VALUE ? 14 : TJUtility.getTierByVoltage(this.maxVoltage);
        if (this.tier >= GAValues.MAX) {
            this.maxVoltage += this.maxVoltage / Integer.MAX_VALUE;
            this.tier = TJUtility.getTierByVoltage(this.maxVoltage); // correct tier for post MAX
        }
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.recipeLogic.invalidate();
        this.maxVoltage = 0;
        this.tier = 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (this.renderTJLogoOverlay() && !this.isStructureFormed()) {
            TJTextures.TJ_LOGO.renderSided(EnumFacingHelper.getLeftFacingFrom(this.getFrontFacing()), renderState, translation, pipeline);
            TJTextures.TJ_LOGO.renderSided(EnumFacingHelper.getRightFacingFrom(this.getFrontFacing()), renderState, translation, pipeline);
            TJTextures.TJ_LOGO.renderSided(this.getFrontFacing().getOpposite(), renderState, translation, pipeline);
        }
        if (this.getFrontalOverlay() != null)
            this.getFrontalOverlay().render(renderState, translation, pipeline, this.getFrontFacing(), this.recipeLogic.isActive(), this.recipeLogic.hasProblem(), this.recipeLogic.isWorkingEnabled());
    }

    public TJOrientedOverlayRenderer getFrontalOverlay() {
        return TJTextures.TJ_MULTIBLOCK_WORKABLE_OVERLAY;
    }

    public boolean renderTJLogoOverlay() {
        return false;
    }

    public boolean usesEnergy() {
        return true;
    }

    @Override
    public long getMaxVoltage() {
        return this.maxVoltage;
    }

    @Override
    public int getTier() {
        return this.tier;
    }

    public int getTierDifference(long recipeEUt) {
        return this.getTier() - GAUtility.getTierByVoltage(recipeEUt);
    }

    @Override
    public void setWorkingEnabled(boolean isActivationAllowed) {
        this.recipeLogic.setWorkingEnabled(isActivationAllowed);
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.recipeLogic.isWorkingEnabled();
    }

    @Override
    public RecipeMap<?> getRecipeMap() {
        return this.recipeMap;
    }

    @Override
    public String getRecipeUid() {
        return this.recipeMap != null ? Gregicality.MODID + ":" + this.recipeMap.getUnlocalizedName() : null;
    }

    public String getRecipeMapNames() {
        return this.recipeMap != null ? this.recipeMap.getLocalizedName() : "Null";
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
}
