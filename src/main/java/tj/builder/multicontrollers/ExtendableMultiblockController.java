package tj.builder.multicontrollers;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import gregtech.common.items.MetaItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

import static tj.capability.TJMultiblockDataCodes.PARALLEL_LAYER;

public abstract class ExtendableMultiblockController extends TJMultiblockDisplayBase {

    protected int parallelLayer;

    public ExtendableMultiblockController(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    public void resetStructure() {
        this.invalidateStructure();
        this.structurePattern = createStructurePattern();
    }

    @Override
    protected void reinitializeStructurePattern() {
        this.parallelLayer = 1;
        super.reinitializeStructurePattern();
    }

    @Override
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        if (playerIn.getHeldItemMainhand().isItemEqual(MetaItems.SCREWDRIVER.getStackForm()))
            return false;
        return super.onRightClick(playerIn, hand, facing, hitResult);
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        ITextComponent textComponent;
        if (!playerIn.isSneaking()) {
            if (parallelLayer < this.getMaxParallel()) {
                this.parallelLayer++;
                textComponent = new TextComponentTranslation("tj.multiblock.parallel.layer.increment.success").appendSibling(new TextComponentString(" " + this.parallelLayer));
            } else
                textComponent = new TextComponentTranslation("tj.multiblock.parallel.layer.increment.fail").appendSibling(new TextComponentString(" " + this.parallelLayer));
        } else {
            if (parallelLayer > 1) {
                this.parallelLayer--;
                textComponent = new TextComponentTranslation("tj.multiblock.parallel.layer.decrement.success").appendSibling(new TextComponentString(" " + this.parallelLayer));
            } else textComponent = new TextComponentTranslation("tj.multiblock.parallel.layer.decrement.fail").appendSibling(new TextComponentString(" " + this.parallelLayer));
        }
        if (getWorld().isRemote)
            playerIn.sendMessage(textComponent);
        else {
            writeCustomData(PARALLEL_LAYER, buf -> buf.writeInt(this.parallelLayer));
        }
        this.resetStructure();
        return true;
    }

    public int getMaxParallel() {
        return 1;
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == PARALLEL_LAYER) {
            this.parallelLayer = buf.readInt();
            this.structurePattern = this.createStructurePattern();
            this.scheduleRenderUpdate();
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeInt(this.parallelLayer);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.parallelLayer = buf.readInt();
        this.structurePattern = this.createStructurePattern();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        NBTTagCompound tagCompound = super.writeToNBT(data);
        tagCompound.setInteger("Parallel", this.parallelLayer);
        return tagCompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.parallelLayer = data.getInteger("Parallel");
        if (data.hasKey("Parallel"))
            this.structurePattern = createStructurePattern();
    }
}
