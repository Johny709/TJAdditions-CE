package tj.builder.multicontrollers;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregicadditions.Gregicality;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.capability.OverclockManager;
import tj.capability.impl.handler.IRecipeHandler;
import tj.capability.impl.workable.BasicRecipeLogic;

public abstract class TJMultiblockRecipeController extends TJMultiblockControllerBase implements IRecipeHandler {

    protected final BasicRecipeLogic recipeLogic = this.createRecipeLogic();
    private final RecipeMap<?> recipeMap;

    public TJMultiblockRecipeController(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap) {
        this(metaTileEntityId, true, recipeMap);
    }

    public TJMultiblockRecipeController(ResourceLocation metaTileEntityId, boolean hasMaintenance, RecipeMap<?> recipeMap) {
        super(metaTileEntityId, hasMaintenance);
        this.recipeMap = recipeMap;
    }

    protected BasicRecipeLogic createRecipeLogic() {
        return new BasicRecipeLogic(this);
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
        overclockManager.setEUt(overclockManager.getEUt() * this.getEUtMultiplier() / 100);
        overclockManager.setDuration(overclockManager.getDuration() * this.getDurationMultiplier() / 100);
        overclockManager.setParallel(this.getParallel() * this.getTier());
    }

    @Override
    protected void addDisplayText(UIDisplayBuilder builder) {
        super.addDisplayText(builder);
        if (!this.isStructureFormed()) return;
        builder.voltageInLine(this.inputEnergyContainer)
                .energyInputLine(this.inputEnergyContainer, this.recipeLogic.getEnergyPerTick())
                .isWorkingLine(this.recipeLogic.isWorkingEnabled(), this.recipeLogic.isActive(), this.recipeLogic.getProgress(), this.recipeLogic.getMaxProgress(), 998)
                .addRecipeInputLine(this.recipeLogic, 999)
                .addRecipeOutputLine(this.recipeLogic, 1000);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.recipeLogic.initialize(this.getAbilities(MultiblockAbility.IMPORT_ITEMS).size());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().render(renderState, translation, pipeline, this.getFrontFacing(), this.recipeLogic.isActive());
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

    public int getEUtMultiplier() {
        return 100;
    }

    public int getDurationMultiplier() {
        return 100;
    }

    public int getBoostChance() {
        return 100;
    }
}
