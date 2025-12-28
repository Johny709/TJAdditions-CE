package tj.blocks;

import gregtech.common.blocks.VariantBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockFusionCasings extends VariantBlock<BlockFusionCasings.FusionType> {

    public BlockFusionCasings() {
        super(Material.IRON);
        this.setHardness(5.0f);
        this.setResistance(10.0f);
        this.setTranslationKey("fusion_casing");
        this.setRegistryName("fusion_casing");
        this.setSoundType(SoundType.METAL);
        this.setHarvestLevel("wrench", 2);
        this.setDefaultState(getState(FusionType.FUSION_CASING_UHV));
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, EntityLiving.SpawnPlacementType type) {
        return false;
    }

    public enum FusionType implements IStringSerializable {
        FUSION_CASING_UHV("fusion_casing_uhv"),
        FUSION_COIL_UHV("fusion_coil_uhv"),
        FUSION_CASING_UEV("fusion_casing_uev"),
        FUSION_COIL_UEV("fusion_coil_uev");

        FusionType(String name) {
            this.name = name;
        }

        private final String name;

        @Override
        public String getName() {
            return this.name;
        }
    }
}
