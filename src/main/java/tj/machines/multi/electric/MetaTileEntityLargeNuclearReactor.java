package tj.machines.multi.electric;

import gregicadditions.client.ClientHandler;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.metal.MetalCasing2;
import gregicadditions.recipes.GARecipeMaps;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.render.ICubeRenderer;
import net.minecraft.util.ResourceLocation;
import tj.builder.multicontrollers.TJMultiRecipeMapMultiblockController;

public class MetaTileEntityLargeNuclearReactor extends TJMultiRecipeMapMultiblockController {

    public MetaTileEntityLargeNuclearReactor(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GARecipeMaps.NUCLEAR_REACTOR_RECIPES, GARecipeMaps.NUCLEAR_BREEDER_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityLargeNuclearReactor(this.metaTileEntityId);
    }

    // TODO WIP
    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "XXX", "XXX")
                .aisle("XXX", "X#X", "XXX")
                .aisle("XXX", "XSX", "XXX")
                .where('S', this.selfPredicate())
                .where('X', statePredicate(GAMetaBlocks.METAL_CASING_2.getState(MetalCasing2.CasingType.ENRICHED_NAQUADAH_ALLOY)))
                .where('#', isAirPredicate())
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return ClientHandler.CLADDED_REACTOR_CASING;
    }
}
