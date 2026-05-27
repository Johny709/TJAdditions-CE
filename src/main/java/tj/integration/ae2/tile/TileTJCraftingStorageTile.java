package tj.integration.ae2.tile;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridHost;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AEPartLocation;
import appeng.api.util.WorldCoord;
import appeng.me.cluster.IAECluster;
import appeng.me.cluster.implementations.CraftingCPUCalculator;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.AENetworkProxyMultiblock;
import appeng.tile.crafting.TileCraftingStorageTile;
import appeng.util.Platform;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import tj.blocks.block.TJBlocks;
import tj.integration.ae2.blocks.BlockTJCraftingUnit;

import java.util.*;

public class TileTJCraftingStorageTile extends TileCraftingStorageTile {

    private final CraftingCPUCalculator calc = new CraftingCPUCalculator(this);
    private NBTTagCompound previousState = null;
    private boolean isCoreBlock = false;
    private CraftingCPUCluster cluster;

    public TileTJCraftingStorageTile() {
        this.getProxy().setFlags(GridFlags.MULTIBLOCK, GridFlags.REQUIRE_CHANNEL);
        this.getProxy().setValidSides(EnumSet.noneOf(EnumFacing.class));
    }

    @Override
    protected AENetworkProxy createProxy() {
        return new AENetworkProxyMultiblock(this, "proxy", this.getItemFromTile(this), true);
    }

    @Override
    protected ItemStack getItemFromTile(final Object obj) {
        if (obj instanceof TileTJCraftingStorageTile) {
            final int storage = ((TileTJCraftingStorageTile) obj).getStorageBytes() / 1024;
            switch (storage) {
                case 65536: return TJBlocks.CRAFTING_STORAGE_65536K.maybeStack(1).orElse(ItemStack.EMPTY);
                default: return ItemStack.EMPTY;
            }
        } else return super.getItemFromTile(obj);
    }

    @Override
    public boolean canBeRotated() {
        return true;// return BlockCraftingUnit.checkType( world.getBlockMetadata( xCoord, yCoord, zCoord ),
        // BlockCraftingUnit.BASE_MONITOR );
    }

    @Override
    public void setName(final String name) {
        super.setName(name);
        if (this.cluster != null) {
            this.cluster.updateName();
        }
    }

    @Override
    public boolean isAccelerator() {
        return false;
    }

    @Override
    public void onReady() {
        super.onReady();
        this.getProxy().setVisualRepresentation(this.getItemFromTile(this));
        this.updateMultiBlock();
    }

    @Override
    public void updateMultiBlock() {
        this.calc.calculateMultiblock(this.world, this.getLocation());
    }

    @Override
    public void updateStatus(final CraftingCPUCluster c) {
        if (this.cluster != null && this.cluster != c) {
            this.cluster.breakCluster();
        }

        this.cluster = c;
        this.updateMeta(true);
    }

    @Override
    public void updateMeta(final boolean updateFormed) {
        if (this.world == null || this.notLoaded() || this.isInvalid()) {
            return;
        }

        final boolean formed = this.isFormed();
        boolean power = false;

        if (this.getProxy().isReady()) {
            power = this.getProxy().isActive();
        }

        final IBlockState current = this.world.getBlockState(this.pos);

        // The tile might try to update while being destroyed
        if (current.getBlock() instanceof BlockTJCraftingUnit) {
            final IBlockState newState = current.withProperty(BlockTJCraftingUnit.POWERED, power).withProperty(BlockTJCraftingUnit.FORMED, formed);

            if (current != newState) {
                // Not using flag 2 here (only send to clients, prevent block update) will cause infinite loops
                // In case there is an inconsistency in the crafting clusters.
                this.world.setBlockState(this.pos, newState, 2);
            }
        }

        if (updateFormed) {
            if (formed) {
                this.getProxy().setValidSides(EnumSet.allOf(EnumFacing.class));
            } else {
                this.getProxy().setValidSides(EnumSet.noneOf(EnumFacing.class));
            }
        }
    }

    @Override
    public boolean isFormed() {
        if (Platform.isClient()) {
            return this.world.getBlockState(this.pos).getValue(BlockTJCraftingUnit.FORMED);
        }
        return this.cluster != null;
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("core", this.isCoreBlock());
        if (this.isCoreBlock() && this.cluster != null) {
            this.cluster.writeToNBT(data);
        }
        return data;
    }

    @Override
    public void readFromNBT(final NBTTagCompound data) {
        super.readFromNBT(data);
        this.setCoreBlock(data.getBoolean("core"));
        if (this.isCoreBlock()) {
            if (this.cluster != null) {
                this.cluster.readFromNBT(data);
            } else {
                this.setPreviousState(data.copy());
            }
        }
    }

    @Override
    public void disconnect(final boolean update) {
        if (this.cluster != null) {
            this.cluster.destroy();
            if (update) {
                this.updateMeta(true);
            }
        }
    }

    @Override
    public IAECluster getCluster() {
        return this.cluster;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    @MENetworkEventSubscribe
    public void onPowerStateChange(final MENetworkChannelsChanged ev) {
        this.updateMeta(false);
    }

    @Override
    @MENetworkEventSubscribe
    public void onPowerStateChange(final MENetworkPowerStatusChange ev) {
        this.updateMeta(false);
    }

    @Override
    public boolean isStatus() {
        return false;
    }

    @Override
    public boolean isStorage() {
        return true;
    }

    @Override
    public int getStorageBytes() {
        if (this.world == null || this.notLoaded() || this.isInvalid()) {
            return 0;
        }

        final Block block = this.world.getBlockState(this.pos).getBlock();
        return block instanceof BlockTJCraftingUnit ? ((BlockTJCraftingUnit) block).type.getBytes() : 0;
    }

    @Override
    public void breakCluster() {
        if (this.cluster != null) {
            this.cluster.cancel();
            final IMEInventory<IAEItemStack> inv = this.cluster.getInventory();

            final LinkedList<WorldCoord> places = new LinkedList<>();

            final Iterator<IGridHost> i = this.cluster.getTiles();
            while (i.hasNext()) {
                final IGridHost h = i.next();
                if (h == this) {
                    places.add(new WorldCoord(this));
                } else {
                    final TileEntity te = (TileEntity) h;

                    for (final AEPartLocation d : AEPartLocation.SIDE_LOCATIONS) {
                        final WorldCoord wc = new WorldCoord(te);
                        wc.add(d, 1);
                        if (this.world.isAirBlock(wc.getPos())) {
                            places.add(wc);
                        }
                    }
                }
            }

            Collections.shuffle(places);

            if (places.isEmpty()) {
                throw new IllegalStateException(this.cluster + " does not contain any kind of blocks, which were destroyed.");
            }

            for (IAEItemStack ais : inv.getAvailableItems(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createList())) {
                ais = ais.copy();
                ais.setStackSize(ais.getDefinition().getMaxStackSize());
                while (true) {
                    final IAEItemStack g = inv.extractItems(ais.copy(), Actionable.MODULATE, this.cluster.getActionSource());
                    if (g == null) {
                        break;
                    }

                    final WorldCoord wc = places.poll();
                    places.add(wc);

                    Platform.spawnDrops(this.world, wc.getPos(), Collections.singletonList(g.createItemStack()));
                }
            }

            this.cluster.destroy();
        }
    }

    @Override
    public boolean isPowered() {
        if (Platform.isClient()) {
            return this.world.getBlockState(this.pos).getValue(BlockTJCraftingUnit.POWERED);
        }
        return this.getProxy().isActive();
    }

    @Override
    public boolean isActive() {
        if (Platform.isServer()) {
            return this.getProxy().isActive();
        }
        return this.isPowered() && this.isFormed();
    }

    @Override
    public boolean isCoreBlock() {
        return this.isCoreBlock;
    }

    @Override
    public void setCoreBlock(final boolean isCoreBlock) {
        this.isCoreBlock = isCoreBlock;
    }

    @Override
    public NBTTagCompound getPreviousState() {
        return this.previousState;
    }

    @Override
    public void setPreviousState(final NBTTagCompound previousState) {
        this.previousState = previousState;
    }
}
