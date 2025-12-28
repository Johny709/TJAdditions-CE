package tj.blocks;

import gregtech.common.blocks.VariantBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class BlockFusionGlass extends VariantBlock<BlockFusionGlass.GlassType> {

    public BlockFusionGlass() {
        super(Material.IRON);
        this.setHardness(5.0f);
        this.setResistance(10.0f);
        this.setTranslationKey("fusion_glass");
        this.setRegistryName("fusion_glass");
        this.setSoundType(SoundType.GLASS);
        this.setHarvestLevel("wrench", 2);
        this.setDefaultState(getState(GlassType.FUSION_GLASS_LUV));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(I18n.format("tile.fusion_glass.description"));
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        IBlockState iblockstate = blockAccess.getBlockState(pos.offset(side));
        Block block = iblockstate.getBlock();

        return block != this && super.shouldSideBeRendered(blockState, blockAccess, pos, side);
    }

    public enum GlassType implements IStringSerializable {
        FUSION_GLASS_LUV("fusion_glass_luv", 6),
        FUSION_GLASS_ZPM("fusion_glass_zpm", 7),
        FUSION_GLASS_UV("fusion_glass_uv", 8),
        FUSION_GLASS_UHV("fusion_glass_uhv", 9),
        FUSION_GLASS_UEV("fusion_glass_uev", 10);

        GlassType(String name, int tier) {
            this.name = name;
            this.tier = tier;
        }

        private final String name;
        private final int tier;

        @Override
        public String getName() {
            return name;
        }

        public int getTier() {
            return tier;
        }
    }
}
