package tj.integration.ae2.blocks;

import appeng.api.util.AEPartLocation;
import appeng.block.crafting.BlockCraftingUnit;
import appeng.client.render.crafting.CraftingCubeState;
import appeng.core.sync.GuiBridge;
import appeng.util.Platform;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.integration.ae2.tile.TileTJCraftingStorageTile;
import tj.rendering.IBlockModel;


import javax.annotation.Nonnull;
import java.util.EnumSet;

public class BlockTJCraftingUnit extends BlockCraftingUnit implements IBlockModel {

    public final TJCraftingUnitType type;

    public BlockTJCraftingUnit(final TJCraftingUnitType type) {
        super(CraftingUnitType.UNIT);
        this.type = type;
        this.setTileEntity(TileTJCraftingStorageTile.class);
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    protected IProperty[] getAEStates() {
        return new IProperty[]{POWERED, FORMED};
    }

    @Nonnull
    @Override
    public IExtendedBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {

        EnumSet<EnumFacing> connections = EnumSet.noneOf(EnumFacing.class);

        for (EnumFacing facing : EnumFacing.values()) {
            if (this.isConnected(world, pos, facing)) {
                connections.add(facing);
            }
        }

        IExtendedBlockState extState = (IExtendedBlockState) state;

        return extState.withProperty(STATE, new CraftingCubeState(connections));
    }

    private boolean isConnected(IBlockAccess world, BlockPos pos, EnumFacing side) {
        BlockPos adjacentPos = pos.offset(side);
        return world.getBlockState(adjacentPos).getBlock() instanceof BlockCraftingUnit;
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, this.getAEStates(), new IUnlistedProperty[]{STATE});
    }

    @Nonnull
    @Override
    public IBlockState getStateFromMeta(final int meta) {
        return this.getDefaultState().withProperty(POWERED, (meta & 1) == 1).withProperty(FORMED, (meta & 2) == 2);
    }

    @Override
    public int getMetaFromState(final IBlockState state) {
        boolean p = state.getValue(POWERED);
        boolean f = state.getValue(FORMED);
        return (p ? 1 : 0) | (f ? 2 : 0);
    }

    @Override
    public void neighborChanged(final IBlockState state, final World worldIn, final BlockPos pos, final Block blockIn, final BlockPos fromPos) {
        final TileTJCraftingStorageTile cp = this.getTileEntity(worldIn, pos);
        if (cp != null) {
            cp.updateMultiBlock();
        }
    }

    @Nonnull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public void breakBlock(final World w, final BlockPos pos, final IBlockState state) {
        final TileTJCraftingStorageTile cp = this.getTileEntity(w, pos);
        if (cp != null) {
            cp.breakCluster();
        }

        super.breakBlock(w, pos, state);
    }

    @Override
    public boolean onBlockActivated(final World w, final BlockPos pos, final IBlockState state, final EntityPlayer p, final EnumHand hand, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        final TileTJCraftingStorageTile tg = this.getTileEntity(w, pos);

        if (tg != null && !p.isSneaking() && tg.isFormed() && tg.isActive()) {
            if (Platform.isClient()) {
                return true;
            }

            Platform.openGUI(p, tg, AEPartLocation.fromFacing(side), GuiBridge.GUI_CRAFTING_CPU);
            return true;
        }

        return super.onBlockActivated(w, pos, state, p, hand, side, hitX, hitY, hitZ);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public StateMapperBase getStateMapper(ResourceLocation resourceLocation) {
        return new StateMapperBase() {

            @Nonnull
            @Override
            protected ModelResourceLocation getModelResourceLocation(@Nonnull IBlockState state) {
                return state.getValue(FORMED) ? new ModelResourceLocation(resourceLocation.toString()) : new ModelResourceLocation(resourceLocation, "inventory");
            }
        };
    }

    public enum TJCraftingUnitType {
        UNIT(0),
        ACCELERATOR(0),
        STORAGE_65536k(67108864),
        STORAGE_262144k(268435456),
        STORAGE_1048M(1073741824),
        STORAGE_SINGULARITY(2147483647),
        MONITOR(0);

        private final int bytes;

        TJCraftingUnitType(int bytes) {
            this.bytes = bytes;
        }

        public int getBytes() {
            return this.bytes;
        }
    }
}
