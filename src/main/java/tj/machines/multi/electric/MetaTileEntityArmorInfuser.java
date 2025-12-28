package tj.machines.multi.electric;

import gregicadditions.capabilities.GregicAdditionsCapabilities;
import gregicadditions.capabilities.impl.GAMultiblockRecipeLogic;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.fusion.GAFusionCasing;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.OrientedOverlayRenderer;
import gregtech.api.render.Textures;
import gregtech.common.blocks.BlockMultiblockCasing;
import gregtech.common.blocks.BlockWireCoil;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import tj.TJRecipeMaps;
import tj.blocks.BlockSolidCasings;
import tj.blocks.TJMetaBlocks;
import tj.builder.multicontrollers.TJGARecipeMapMultiblockControllerBase;
import tj.textures.TJTextures;

import javax.annotation.Nonnull;


public class MetaTileEntityArmorInfuser extends TJGARecipeMapMultiblockControllerBase {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {MultiblockAbility.IMPORT_ITEMS, MultiblockAbility.IMPORT_FLUIDS, MultiblockAbility.EXPORT_ITEMS, MultiblockAbility.EXPORT_FLUIDS, MultiblockAbility.INPUT_ENERGY, GregicAdditionsCapabilities.MAINTENANCE_HATCH};

    public MetaTileEntityArmorInfuser(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, TJRecipeMaps.ARMOR_INFUSER_RECIPES, false, true, false);
        this.recipeMapWorkable = new GAMultiblockRecipeLogic(this);
    }

    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityArmorInfuser(this.metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("HHHHH", "~~~~~", "~~~~~", "GGGGG", "~~~~~", "~~~~~", "GGGGG", "~~~~~", "~~~~~", "DDDDD")
                .aisle("HDDDH", "~~A~~", "~~A~~", "G~A~G", "~~A~~", "~~A~~", "G~A~G", "~~A~~", "~~A~~", "DDDDD")
                .aisle("HDDDH", "~AFA~", "~AFA~", "GAFAG", "~AFA~", "~AFA~", "GAFAG", "~AFA~", "~AFA~", "DDDDD")
                .aisle("HDDDH", "~~A~~", "~~A~~", "G~A~G", "~~A~~", "~~A~~", "G~A~G", "~~A~~", "~~A~~", "DDDDD")
                .aisle("HHSHH", "~~~~~", "~~~~~", "GGGGG", "~~~~~", "~~~~~", "GGGGG", "~~~~~", "~~~~~", "DDDDD")
                .where('S', selfPredicate())
                .where('H', statePredicate(getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)).or(blockPredicate(Block.getBlockFromName("contenttweaker:draconiccasing"))))
                .where('A', statePredicate(MetaBlocks.MUTLIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.FUSION_CASING_MK2)))
                .where('D', statePredicate(getCasingState()))
                .where('F', statePredicate(MetaBlocks.WIRE_COIL.getState(BlockWireCoil.CoilType.FUSION_COIL)))
                .where('G', statePredicate(GAMetaBlocks.FUSION_CASING.getState(GAFusionCasing.CasingType.ADV_FUSION_COIL_1)))
                .where('~', (tile) -> true)
                .build();
    }

    protected IBlockState getCasingState() {
        return TJMetaBlocks.SOLID_CASING.getState(BlockSolidCasings.SolidCasingType.DRACONIC_CASING);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return TJTextures.DRACONIC;
    }

    @Nonnull
    @Override
    protected OrientedOverlayRenderer getFrontOverlay() {
        return Textures.PYROLYSE_OVEN_OVERLAY;
    }
}
