package tj.blocks;

import gregtech.common.blocks.VariantBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;


public class BlockSolidCasings extends VariantBlock<BlockSolidCasings.SolidCasingType> {

    public BlockSolidCasings() {
        super(Material.IRON);
        this.setHardness(5.0f);
        this.setResistance(10.0f);
        this.setTranslationKey("solid_casing");
        this.setRegistryName("solid_casing");
        this.setSoundType(SoundType.METAL);
        this.setHarvestLevel("wrench", 2);
        this.setDefaultState(getState(SolidCasingType.DRACONIC_CASING));
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, EntityLiving.SpawnPlacementType type) {
        return false;
    }

    public enum SolidCasingType implements IStringSerializable {
        DRACONIC_CASING("draconium"),
        AWAKENED_CASING("awakened"),
        CHOATIC_CASING("chaotic"),
        ETERNITY_CASING("eternity"),
        SOUL_CASING("soul"),
        DURANIUM_CASING("duranium"),
        SEABORGIUM_CASING("seaborgium"),
        TUNGSTEN_TITANIUM_CARBIDE_CASING("tungsten_titanium_carbide"),
        HEAVY_QUARK_DEGENERATE_MATTER("heavy_quark_degenerate_matter"),
        PERIODICIUM("periodicium");

        private final String name;

        SolidCasingType(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }
    }
}
