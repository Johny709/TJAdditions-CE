package tj.machines.multi.steam;

import tj.TJRecipeMaps;
import tj.builder.multicontrollers.TJRecipeMapMultiblockControllerBase;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.OrientedOverlayRenderer;
import gregtech.api.render.Textures;
import gregtech.common.blocks.BlockMachineCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MetaTileEntityPrimitiveAlloy extends TJRecipeMapMultiblockControllerBase {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = new MultiblockAbility[]{MultiblockAbility.IMPORT_FLUIDS, MultiblockAbility.EXPORT_ITEMS, MultiblockAbility.IMPORT_ITEMS};

    public MetaTileEntityPrimitiveAlloy (ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, TJRecipeMaps.PRIMITIVE_ALLOY_RECIPES);
        this.recipeMapWorkable = new MultiblockRecipeLogic(this);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityPrimitiveAlloy(this.metaTileEntityId);/*(3)!*/
    }

    @Override
    protected boolean checkStructureComponents(List<IMultiblockPart> parts, Map<MultiblockAbility<Object>, List<Object>> abilities) {
        //basically check minimal requirements for inputs count
        //noinspection SuspiciousMethodCalls
        int itemInputsCount = abilities.getOrDefault(MultiblockAbility.IMPORT_ITEMS, Collections.emptyList())
                .stream().map(it -> (IItemHandler) it).mapToInt(IItemHandler::getSlots).sum();
        //noinspection SuspiciousMethodCalls
        int fluidInputsCount = abilities.getOrDefault(MultiblockAbility.IMPORT_FLUIDS, Collections.emptyList()).size();
        //noinspection SuspiciousMethodCalls
        return itemInputsCount >= recipeMap.getMinInputs() &&
                fluidInputsCount >= recipeMap.getMinFluidInputs();
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start() /*(4)!*/
                .aisle("FFF", "FFF", "XXX")
                .aisle("FFF", "F#F", "X#X")
                .aisle("FFF", "FSF", "XXX")
                .where('S', selfPredicate())
                .where('F', statePredicate(getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('X', statePredicate(MetaBlocks.MACHINE_CASING.getState(BlockMachineCasing.MachineCasingType.BRONZE_HULL)))
                .where('#', isAirPredicate())
                .build();
    }

    protected IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.PRIMITIVE_BRICKS);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.PRIMITIVE_BRICKS;
    }

    @Nonnull
    @Override
    protected OrientedOverlayRenderer getFrontOverlay() {
        return Textures.PYROLYSE_OVEN_OVERLAY;
    }


}
