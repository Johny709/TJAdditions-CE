package tj.machines.multi.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregicadditions.GAValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.builder.multicontrollers.TJMultiblockControllerBase;
import tj.capability.impl.handler.ICoilHandler;
import tj.capability.impl.workable.MultiSmelterWorkableHandler;
import tj.textures.TJTextures;
import tj.util.EnumFacingHelper;
import tj.util.TJUtility;

import javax.annotation.Nullable;
import java.util.List;

import static gregicadditions.capabilities.GregicAdditionsCapabilities.MAINTENANCE_HATCH;
import static gregicadditions.capabilities.GregicAdditionsCapabilities.MUFFLER_HATCH;
import static gregtech.api.metatileentity.multiblock.MultiblockAbility.*;

public class MetaTileEntityTJMultiSmelter extends TJMultiblockControllerBase implements ICoilHandler {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {IMPORT_ITEMS, EXPORT_ITEMS, INPUT_ENERGY, MAINTENANCE_HATCH};
    private final MultiSmelterWorkableHandler workableHandler = new MultiSmelterWorkableHandler(this);

    private long maxVoltage;
    private int coilEnergyDiscount;
    private int coilLevel;
    private int tier;

    public MetaTileEntityTJMultiSmelter(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityTJMultiSmelter(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("tj.multiblock.temporary"));
    }

    @Override
    protected void updateFormedValid() {
        if (((this.getProblems() >> 5) & 1) != 0)
            this.workableHandler.update();
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "CCC", "XXX")
                .aisle("XXX", "C#C", "XMX")
                .aisle("XSX", "CCC", "XXX")
                .where('S', this.selfPredicate())
                .where('X', statePredicate(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.INVAR_HEATPROOF)).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('C', coilPredicate())
                .where('M', abilityPartPredicate(MUFFLER_HATCH))
                .where('#', isAirPredicate())
                .build();
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.workableHandler.initialize(this.getAbilities(IMPORT_ITEMS).size());
        this.coilLevel = context.getOrDefault("coilLevel", 0);
        this.coilEnergyDiscount = context.getOrDefault("coilEnergyDiscount", 0);
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
        this.maxVoltage = 0;
        this.coilEnergyDiscount = 0;
        this.coilLevel = 0;
        this.tier = 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        TJTextures.TJ_MULTIBLOCK_WORKABLE_OVERLAY.render(renderState, translation, pipeline, this.getFrontFacing(), this.workableHandler.isActive());
        TJTextures.TJ_LOGO.renderSided(EnumFacingHelper.getLeftFacingFrom(this.getFrontFacing()), renderState, translation, pipeline);
        TJTextures.TJ_LOGO.renderSided(EnumFacingHelper.getRightFacingFrom(this.getFrontFacing()), renderState, translation, pipeline);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.HEAT_PROOF_CASING;
    }

    @Override
    public int getParallel() {
        return 32 * this.coilLevel;
    }

    @Override
    public int getCoilEnergyDiscount() {
        return this.coilEnergyDiscount;
    }

    @Override
    public int getTier() {
        return this.tier;
    }

    @Override
    public long getMaxVoltage() {
        return this.maxVoltage;
    }

    @Override
    public RecipeMap<?> getRecipeMap() {
        return RecipeMaps.FURNACE_RECIPES;
    }
}
