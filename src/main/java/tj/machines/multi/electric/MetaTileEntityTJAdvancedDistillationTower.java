package tj.machines.multi.electric;

import gregicadditions.capabilities.GregicAdditionsCapabilities;
import gregicadditions.client.ClientHandler;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.metal.MetalCasing1;
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
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.builder.multicontrollers.TJMultiRecipeMapMultiblockController;
import tj.capability.OverclockManager;
import tj.capability.impl.handler.IDistillationHandler;
import tj.capability.impl.workable.BasicRecipeLogic;
import tj.capability.impl.workable.DistillationRecipeLogic;
import tj.textures.TJOrientedOverlayRenderer;
import tj.textures.TJTextures;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static tj.machines.multi.electric.MetaTileEntityTJDistillationTower.outputHatchPredicate;

public class MetaTileEntityTJAdvancedDistillationTower extends TJMultiRecipeMapMultiblockController implements IDistillationHandler {

    private final List<BlockPos> outputHatchPos = new ArrayList<>();

    public MetaTileEntityTJAdvancedDistillationTower(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, true, false, RecipeMaps.DISTILLATION_RECIPES, RecipeMaps.DISTILLERY_RECIPES, RecipeMaps.FLUID_HEATER_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityTJAdvancedDistillationTower(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("tj.multiblock.temporary"));
        tooltip.add(I18n.format("tj.multiblock.distillation_tower.layers", 2, 13));
        tooltip.add(I18n.format("gregtech.multiblock.advanced_distillation_tower.description1"));
        tooltip.add(I18n.format("gregtech.multiblock.advanced_distillation_tower.description2"));
        tooltip.add(I18n.format("gregtech.multiblock.advanced_distillation_tower.description3"));
        tooltip.add(I18n.format("gregtech.multiblock.advanced_distillation_tower.description4"));
        super.addInformation(stack, player, tooltip, advanced);
    }

    @Override
    protected BasicRecipeLogic<IDistillationHandler> createRecipeLogic() {
        return new DistillationRecipeLogic(this);
    }

    @Override
    public void preOverclock(OverclockManager<?> overclockManager, Recipe recipe) {
        super.preOverclock(overclockManager, recipe);
        overclockManager.setParallel(Math.min(this.getRecipeMapIndex() == 0 ? 16 : 64, overclockManager.getParallel()));
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(RIGHT, FRONT, DOWN)
                .aisle("XXX", "XXX", "XXX")
                .aisle("XXX", "X#X", "XXX").setRepeatable(0, 11)
                .aisle("ZSZ", "ZZZ", "ZZZ")
                .where('S', this.selfPredicate())
                .where('Z', statePredicate(this.getCasingState()).or(abilityPartPredicate(MultiblockAbility.IMPORT_FLUIDS, MultiblockAbility.EXPORT_ITEMS, MultiblockAbility.INPUT_ENERGY)))
                .where('X', this.countMatch("outputHatchCount", tilePredicate(outputHatchPredicate())).or(statePredicate(this.getCasingState())).or(abilityPartPredicate(GregicAdditionsCapabilities.MAINTENANCE_HATCH, MultiblockAbility.INPUT_ENERGY)))
                .where('#', isAirPredicate())
                .validateLayer(0, context -> context.getOrDefault("outputHatchCount", 0) == 1)
                .validateLayer(1, context -> context.getOrDefault("outputHatchCount", 0) == 1)
                .build();
    }

    private IBlockState getCasingState() {
        return GAMetaBlocks.METAL_CASING_1.getState(MetalCasing1.CasingType.BABBITT_ALLOY);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.outputHatchPos.addAll(context.getOrDefault("outputHatches", new HashSet<>()));
        this.outputHatchPos.sort(Comparator.comparingInt(pos -> Math.abs(pos.getX() - this.getPos().getX()) + Math.abs(pos.getY() - this.getPos().getY()) + Math.abs(pos.getZ() - this.getPos().getZ())));
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.outputHatchPos.clear();
    }

    @Override
    public TJOrientedOverlayRenderer getFrontalOverlay() {
        return TJTextures.TJ_DISTILLERY_OVERLAY;
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return ClientHandler.BABBITT_ALLOY_CASING;
    }

    @Override
    public int getParallel() {
        return this.getRecipeMapIndex() == 0 ? 4 : 8;
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
