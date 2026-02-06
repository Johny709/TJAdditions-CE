package tj.capability.impl.workable;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import gregicadditions.GAValues;
import gregtech.api.block.machines.BlockMachine;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.pipenet.block.material.TileEntityMaterialPipeBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import tj.builder.multicontrollers.TJMultiblockControllerBase;
import tj.capability.IMachineHandler;
import tj.capability.LinkEvent;
import tj.capability.AbstractWorkableHandler;
import tj.machines.AcceleratorBlacklist;
import tj.machines.singleblock.MetaTileEntityAcceleratorAnchorPoint;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;

import static gregtech.api.unification.material.Materials.UUMatter;
import static tj.capability.impl.workable.AcceleratorWorkableHandler.AcceleratorMode.*;

public class AcceleratorWorkableHandler extends AbstractWorkableHandler<IMachineHandler> {

    private final BlockPos.MutableBlockPos cell = new BlockPos.MutableBlockPos();
    private AcceleratorMode acceleratorMode = AcceleratorMode.RANDOM_TICK;
    private int gtAcceleratorTier;
    private int energyMultiplier = 1;
    private int fluidConsumption;
    private int area;
    private String[] entityLinkName;
    private BlockPos[] entityLinkBlockPos;
    private NBTTagCompound linkData;

    public AcceleratorWorkableHandler(MetaTileEntity metaTileEntity) {
        super(metaTileEntity);
        this.maxProgress = 1;
        this.isWorking = false;
    }

    @Override
    public void initialize(int busCount) {
        super.initialize(busCount);
        this.area = (this.handler.getTier() * 2) + 1;
        this.gtAcceleratorTier = this.handler.getTier() - GAValues.UHV;
        if (this.acceleratorMode == AcceleratorMode.GT_TILE_ENTITY) {
            this.entityLinkName = this.entityLinkName != null ? this.entityLinkName : new String[1];
            this.entityLinkBlockPos = this.entityLinkBlockPos != null ? this.entityLinkBlockPos : new BlockPos[1];
        } else {
            this.entityLinkName = this.entityLinkName != null ? Arrays.copyOf(this.entityLinkName, this.handler.getTier()) : new String[this.handler.getTier()];
            this.entityLinkBlockPos = this.entityLinkBlockPos != null ? Arrays.copyOf(this.entityLinkBlockPos, this.handler.getTier()) : new BlockPos[this.handler.getTier()];
        }
        this.fluidConsumption = (int) Math.pow(4, this.gtAcceleratorTier - 1) * 1000;
        this.setLinkedEntitiesPos(this.metaTileEntity);
        this.updateEnergyPerTick();
        if (this.linkData != null) {
            int size = this.linkData.getInteger("Size") - this.entityLinkBlockPos.length;
            int remaining = Math.max(0, (this.linkData.getInteger("I") - size));
            this.linkData.setInteger("Size", this.entityLinkBlockPos.length);
            this.linkData.setInteger("I", remaining);
        }
    }

    @Override
    protected boolean startRecipe() {
        return true;
    }

    @Override
    protected boolean completeRecipe() {
        WorldServer world = (WorldServer) this.metaTileEntity.getWorld();
        switch (this.acceleratorMode) {
            case TILE_ENTITY:
                for (BlockPos pos : this.entityLinkBlockPos) {
                    if (pos == null) {
                        continue;
                    }
                    TileEntity targetTE = world.getTileEntity(pos);
                    if (targetTE == null || targetTE instanceof TileEntityMaterialPipeBase || targetTE instanceof MetaTileEntityHolder) {
                        continue;
                    }
                    boolean horror = false;
                    if (clazz != null && targetTE instanceof ITickable) {
                        horror = clazz.isInstance(targetTE);
                    }
                    if (targetTE instanceof ITickable && (!horror || !world.isRemote)) {
                        IntStream.range(0, (int) Math.pow(2, this.handler.getTier())).forEach(value -> ((ITickable) targetTE).update());
                    }
                }
                break;

            case GT_TILE_ENTITY:
                if (this.gtAcceleratorTier < 1 || this.entityLinkBlockPos[0] == null) {
                    break;
                }

                FluidStack uuMatter = UUMatter.getFluid(this.fluidConsumption);
                if (uuMatter.isFluidStackIdentical(this.handler.getImportFluidTank().drain(uuMatter, false))) {
                    this.handler.getImportFluidTank().drain(uuMatter, true);
                    MetaTileEntity targetGTTE = BlockMachine.getMetaTileEntity(world, this.entityLinkBlockPos[0]);
                    if (targetGTTE == null || targetGTTE instanceof AcceleratorBlacklist) {
                        break;
                    }
                    IntStream.range(0, (int) Math.pow(4, this.gtAcceleratorTier)).forEach(value -> targetGTTE.update());
                }
                break;

            case RANDOM_TICK:
                for (int i = 0; i < this.entityLinkBlockPos.length; i++) {
                    BlockPos blockPos = this.entityLinkBlockPos[i];
                    if (blockPos == null)
                        continue;
                    MetaTileEntity targetGTTE = BlockMachine.getMetaTileEntity(world, blockPos);
                    if (targetGTTE instanceof MetaTileEntityAcceleratorAnchorPoint) {
                        if (((MetaTileEntityAcceleratorAnchorPoint) targetGTTE).isRedStonePowered())
                            continue;
                    }
                    this.cell.setPos(blockPos.getX() - this.handler.getTier(), blockPos.getY() - this.handler.getTier(), blockPos.getZ());
                    for (int x = 0; x < this.area; x++) {
                        for (int y = 0; y < this.area; y++) {
                            IBlockState targetBlock = world.getBlockState(this.cell.setPos(this.cell.getX() + x, this.cell.getY() + y, this.cell.getZ()));
                            IntStream.range(0, (int) Math.pow(2, this.handler.getTier())).forEach(value -> {
                                if (world.rand.nextInt(100) == 0) {
                                    if (targetBlock.getBlock().getTickRandomly()) {
                                        targetBlock.getBlock().randomTick(world, cell, targetBlock, world.rand);
                                    }
                                }
                            });
                        }
                    }
                }
        }
        if (this.metaTileEntity instanceof TJMultiblockControllerBase)
            ((TJMultiblockControllerBase) this.metaTileEntity).calculateMaintenance(this.maxProgress);
        return true;
    }

    static Class clazz;

    static {
        try {
            clazz = Class.forName("cofh.core.block.TileCore");
        } catch (Exception ignored) {

        }
    }

    public boolean onScrewDriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        String tileMode = "null";
        switch (this.acceleratorMode) {
            case RANDOM_TICK:
                this.acceleratorMode = TILE_ENTITY;
                this.energyMultiplier = 1;
                this.entityLinkName = new String[this.handler.getTier()];
                this.entityLinkBlockPos = new BlockPos[this.handler.getTier()];
                tileMode = "gregtech.machine.world_accelerator.mode.tile";
                break;
            case TILE_ENTITY:
                this.acceleratorMode = GT_TILE_ENTITY;
                this.energyMultiplier = 64;
                this.entityLinkName = new String[1];
                this.entityLinkBlockPos = new BlockPos[1];
                tileMode = "tj.multiblock.large_world_accelerator.mode.GT";
                break;
            case GT_TILE_ENTITY:
                this.acceleratorMode = RANDOM_TICK;
                this.energyMultiplier = 1;
                this.entityLinkName = new String[this.handler.getTier()];
                this.entityLinkBlockPos = new BlockPos[this.handler.getTier()];
                tileMode = "gregtech.machine.world_accelerator.mode.entity";
        }
        this.updateEnergyPerTick();
        if (this.linkData != null) {
            this.linkData.setInteger("Size", this.entityLinkBlockPos.length);
            this.linkData.setInteger("I", this.entityLinkBlockPos.length);
        }
        if (this.metaTileEntity.getWorld().isRemote) {
            playerIn.sendStatusMessage(new TextComponentTranslation(tileMode), false);
        }
        return true;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = super.serializeNBT();
        NBTTagList linkList = new NBTTagList();
        for (int i = 0; i < this.entityLinkBlockPos.length; i++) {
            if (this.entityLinkBlockPos[i] != null) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setInteger("index", i);
                tag.setDouble("x", this.entityLinkBlockPos[i].getX());
                tag.setDouble("y", this.entityLinkBlockPos[i].getY());
                tag.setDouble("z", this.entityLinkBlockPos[i].getZ());
                tag.setString("name", this.entityLinkName[i]);
                linkList.appendTag(tag);
            }
        }
        compound.setTag("links", linkList);
        compound.setInteger("energyMultiplier", this.energyMultiplier);
        compound.setInteger("acceleratorMode", this.acceleratorMode.ordinal());
        compound.setInteger("blockPosSize", this.entityLinkBlockPos.length);
        if (this.linkData != null)
            compound.setTag("link.XYZ", this.linkData);
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        super.deserializeNBT(compound);
        this.energyMultiplier = compound.getInteger("energyMultiplier");
        this.acceleratorMode = AcceleratorMode.values()[compound.getInteger("acceleratorMode")];
        this.entityLinkName = new String[compound.getInteger("blockPosSize")];
        this.entityLinkBlockPos = new BlockPos[compound.getInteger("blockPosSize")];
        NBTTagList linkList = compound.getTagList("links", Constants.NBT.TAG_COMPOUND);
        for (NBTBase nbtBase : linkList) {
            NBTTagCompound tag = (NBTTagCompound) nbtBase;
            int i = tag.getInteger("index");
            this.entityLinkBlockPos[i] = new BlockPos(tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z"));
            this.entityLinkName[i] = tag.getString("name");
        }
        if (compound.hasKey("link.XYZ"))
            this.linkData = compound.getCompoundTag("link.XYZ");
    }

    public void renameLink(String name, String id) {
        int index = id.lastIndexOf(";");
        index = Integer.parseInt(id.substring(index + 1));
        this.entityLinkName[index] = name;
        this.metaTileEntity.markDirty();
    }

    public void updateEnergyPerTick() {
        long count = Arrays.stream(this.entityLinkBlockPos).filter(Objects::nonNull).count();
        this.energyPerTick = (long) ((Math.pow(4, this.handler.getTier()) * 8) * this.energyMultiplier) * count;
        this.metaTileEntity.markDirty();
    }

    public void setLinkedEntitiesPos(MetaTileEntity metaTileEntity) {
        if (this.entityLinkBlockPos != null)
            Arrays.stream(this.entityLinkBlockPos)
                    .filter(Objects::nonNull)
                    .map(blockPos -> BlockMachine.getMetaTileEntity(this.metaTileEntity.getWorld(), blockPos))
                    .filter(entity -> entity instanceof LinkEvent)
                    .forEach(entity -> ((LinkEvent) entity).onLink(metaTileEntity));
    }

    public int getFluidConsumption() {
        return this.fluidConsumption;
    }

    public BlockPos[] getEntityLinkBlockPos() {
        return this.entityLinkBlockPos;
    }

    public String[] getEntityLinkName() {
        return this.entityLinkName;
    }

    public void setReset(boolean reset) {
        Arrays.fill(this.entityLinkName, null);
        Arrays.fill(this.entityLinkBlockPos, null);
        this.linkData.setInteger("I", this.entityLinkBlockPos.length);
        this.updateEnergyPerTick();
        this.metaTileEntity.markDirty();
    }

    public NBTTagCompound getLinkData() {
        return this.linkData;
    }

    public void setLinkData(NBTTagCompound linkData) {
        this.linkData = linkData;
        this.metaTileEntity.markDirty();
    }

    public AcceleratorMode getAcceleratorMode() {
        return this.acceleratorMode;
    }

    public enum AcceleratorMode {
        RANDOM_TICK,
        TILE_ENTITY,
        GT_TILE_ENTITY
    }
}
