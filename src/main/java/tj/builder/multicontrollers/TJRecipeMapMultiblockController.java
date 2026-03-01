package tj.builder.multicontrollers;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregicadditions.GAUtility;
import gregicadditions.Gregicality;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.TJValues;
import tj.capability.OverclockManager;
import tj.capability.impl.handler.IRecipeHandler;
import tj.capability.impl.workable.BasicRecipeLogic;
import tj.gui.TJGuiTextures;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gtadditions.multiblock.universal.tooltip.1", this.getRecipeMapNames()));
        tooltip.add(I18n.format("gtadditions.multiblock.universal.tooltip.2", TJValues.thousandTwoPlaceFormat.format(this.getEUtMultiplier() / 100.0)));
        tooltip.add(I18n.format("gtadditions.multiblock.universal.tooltip.3", TJValues.thousandTwoPlaceFormat.format(this.getDurationMultiplier() / 100.0)));
        tooltip.add(I18n.format("gtadditions.multiblock.universal.tooltip.4", this.getParallel()));
        tooltip.add(I18n.format("gtadditions.multiblock.universal.tooltip.5", this.getChanceMultiplier()));
    }

    protected BasicRecipeLogic<? extends IRecipeHandler> createRecipeLogic() {
        return new BasicRecipeLogic<>(this);
    }

    @Override
    protected boolean checkStructureComponents(List<IMultiblockPart> parts, Map<MultiblockAbility<Object>, List<Object>> abilities) {
        return !abilities.getOrDefault(MultiblockAbility.INPUT_ENERGY, Collections.emptyList()).isEmpty() &&
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
    protected void mainDisplayTab(List<Widget> widgetGroup) {
        super.mainDisplayTab(widgetGroup);
        if (!this.hasDistinct()) return;
        widgetGroup.add(new ToggleButtonWidget(175, 151, 18, 18, TJGuiTextures.DISTINCT_BUTTON, this.recipeLogic::isDistinct, this.recipeLogic::setDistinct)
                .setTooltipText("machine.universal.toggle.distinct.mode"));
    }

    @Override
    protected void addDisplayText(UIDisplayBuilder builder) {
        super.addDisplayText(builder);
        if (!this.isStructureFormed()) return;
        builder.voltageInLine(this.inputEnergyContainer)
                .voltageTierLine(this.tier)
                .energyInputLine(this.inputEnergyContainer, this.recipeLogic.getEnergyPerTick())
                .isWorkingLine(this.recipeLogic.isWorkingEnabled(), this.recipeLogic.isActive(), this.recipeLogic.getProgress(), this.recipeLogic.getMaxProgress(), 998)
                .addRecipeInputLine(this.recipeLogic, 999)
                .addRecipeOutputLine(this.recipeLogic, 1000);
        if (this.hasDistinct())
            builder.addDistinctLine(this.recipeLogic.isDistinct(), 997);
    }

    @Override
    protected void handleDisplayClick(String componentData, Widget.ClickData clickData) {
        String[] data = componentData.split(":");
        if (data[0].equals("distinct"))
            this.recipeLogic.setDistinct(data[1].equals("true"));
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.recipeLogic.initialize(this.getAbilities(MultiblockAbility.IMPORT_ITEMS).size());
        this.maxVoltage = Math.max(this.inputEnergyContainer.getInputVoltage(), this.outputEnergyContainer.getOutputVoltage());
        this.tier = GAUtility.getTierByVoltage(this.maxVoltage);
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
        this.getFrontOverlay().render(renderState, translation, pipeline, this.getFrontFacing(), this.recipeLogic.isActive());
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
        return Gregicality.MODID + ":" + this.recipeMap.getUnlocalizedName();
    }

    public String getRecipeMapNames() {
        return this.recipeMap.getLocalizedName();
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
