package tj.machines.multi.electric;

import gregicadditions.capabilities.GregicAdditionsCapabilities;
import gregtech.api.GTValues;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.Textures;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockWireCoil;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import tj.builder.multicontrollers.TJRecipeMapMultiblockController;
import tj.capability.OverclockManager;
import tj.capability.impl.handler.IDistillationHandler;
import tj.capability.impl.workable.BasicRecipeLogic;
import tj.capability.impl.workable.DistillationRecipeLogic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static tj.machines.multi.electric.MetaTileEntityTJDistillationTower.outputHatchPredicate;

public class MetaTileEntityTJMegaDistillationTower extends TJRecipeMapMultiblockController implements IDistillationHandler {

    private final List<BlockPos> outputHatchPos = new ArrayList<>();

    public MetaTileEntityTJMegaDistillationTower(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.DISTILLATION_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityTJMegaDistillationTower(this.metaTileEntityId);
    }

    @Override
    protected BasicRecipeLogic<IDistillationHandler> createRecipeLogic() {
        return new DistillationRecipeLogic(this);
    }

    @Override
    public void preOverclock(OverclockManager<?> overclockManager, Recipe recipe) {
        overclockManager.setDuration(overclockManager.getDuration() * 2);
        overclockManager.setParallel(1 << overclockManager.getParallel() * 2);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(RIGHT, FRONT, DOWN)
                .aisle("~ZZZ~", "ZZZZZ", "ZZZZZ", "ZZZZZ", "~ZZZ~")
                .aisle("~XXX~", "XCPCX", "XPFPX", "XCPCX", "~XXX~").setRepeatable(1, 12)
                .aisle("~ZSZ~", "ZZZZZ", "ZZZZZ", "ZZZZZ", "~ZZZ~")
                .where('S', this.selfPredicate())
                .where('F', frameworkPredicate())
                .where('C', statePredicate(MetaBlocks.WIRE_COIL.getState(BlockWireCoil.CoilType.NICHROME)))
                .where('P', statePredicate(MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.TUNGSTENSTEEL_PIPE)))
                .where('Z', statePredicate(this.getCasingState()).or(abilityPartPredicate(MetaTileEntityTJDistillationTower.ALLOWED_ABILITIES)))
                .where('X', this.countMatch("outputHatchCount", tilePredicate(outputHatchPredicate())).or(statePredicate(this.getCasingState())).or(abilityPartPredicate(GregicAdditionsCapabilities.MAINTENANCE_HATCH, MultiblockAbility.INPUT_ENERGY)))
                .where('~', tile -> true)
                .validateLayer(1, context -> context.getOrDefault("outputHatchCount", 0) == 1)
                .build();
    }

    private IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STAINLESS_CLEAN);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.outputHatchPos.addAll(context.getOrDefault("outputHatches", new HashSet<>()));
        this.outputHatchPos.sort(Comparator.comparingInt(pos -> Math.abs(pos.getX() - this.getPos().getX()) + Math.abs(pos.getY() - this.getPos().getY()) + Math.abs(pos.getZ() - this.getPos().getZ())));
        this.tier = context.getOrDefault("frameworkTier", 0);
        this.maxVoltage = 8L << this.tier * 2;
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.outputHatchPos.clear();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.CLEAN_STAINLESS_STEEL_CASING;
    }

    @Override
    public int getParallel() {
        return Math.max(0, this.getTier() - GTValues.LuV);
    }

    @Override
    public IMultipleTankHandler getOutputHatchAt(int index) {
        if (index >= this.outputHatchPos.size())
            return null;
        TileEntity tileEntity = this.getWorld().getTileEntity(this.outputHatchPos.get(index));
        if (!(tileEntity instanceof MetaTileEntityHolder))
            return null;
        MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) tileEntity).getMetaTileEntity();
        return metaTileEntity != null ? metaTileEntity.getExportFluids() : null;
    }
}
