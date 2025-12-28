package tj.machines.multi.electric;

import tj.blocks.BlockSolidCasings;
import tj.blocks.TJMetaBlocks;
import tj.textures.TJTextures;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.render.ICubeRenderer;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

import static gregicadditions.GAMaterials.Seaborgium;

public class MetaTileEntityUltimateLargeMiner extends MetaTileEntityEliteLargeMiner {

    public MetaTileEntityUltimateLargeMiner(ResourceLocation metaTileEntityId, Type type) {
        super(metaTileEntityId, type);
    }

    @Override
    public IBlockState getFrameState() {
        return MetaBlocks.FRAMES.get(Seaborgium).getDefaultState();
    }

    @Override
    public IBlockState getCasingState() {
        return TJMetaBlocks.SOLID_CASING.getState(BlockSolidCasings.SolidCasingType.SEABORGIUM_CASING);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return TJTextures.SEABORGIUM;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityUltimateLargeMiner(metaTileEntityId, getType());
    }
}
