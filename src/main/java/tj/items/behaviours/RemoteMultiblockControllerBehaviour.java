package tj.items.behaviours;

import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.MetaTileEntityUIFactory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.util.List;

public class RemoteMultiblockControllerBehaviour implements IItemBehaviour {

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        NBTTagCompound nbt = player.getHeldItem(hand).getOrCreateSubCompound("Machine");
        if (!world.isRemote && player.isSneaking()) {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (!(tileEntity instanceof MetaTileEntityHolder))
                return EnumActionResult.SUCCESS;
            MetaTileEntityHolder holder = (MetaTileEntityHolder) tileEntity;
            MetaTileEntity metaTileEntity = holder.getMetaTileEntity();
            String tileEntityName = I18n.translateToLocal(metaTileEntity.getMetaFullName());
            String worldName = world.provider.getDimensionType().getName();
            int worldID = world.provider.getDimension();
            nbt.setInteger("X", pos.getX());
            nbt.setInteger("Y", pos.getY());
            nbt.setInteger("Z", pos.getZ());
            nbt.setInteger("World", worldID);
            nbt.setString("WorldName", worldName);
            nbt.setString("Name", tileEntityName);
            player.sendMessage(new TextComponentString(I18n.translateToLocalFormatted("metaitem.remote_multiblock_controller.set", tileEntityName, worldName, worldID, pos.getX(), pos.getY(), pos.getZ())));
        }
        return EnumActionResult.PASS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        NBTTagCompound nbt = player.getHeldItem(hand).getOrCreateSubCompound("Machine");
        if (!world.isRemote && nbt.hasKey("Name")) {
            BlockPos pos = new BlockPos(nbt.getInteger("X"), nbt.getInteger("Y"), nbt.getInteger("Z"));
            World dimension = DimensionManager.getWorld(nbt.getInteger("World"));
            if (dimension == null)
                return ActionResult.newResult(EnumActionResult.PASS, player.getHeldItem(hand));
            TileEntity tileEntity = dimension.getTileEntity(pos);
            if (!(tileEntity instanceof MetaTileEntityHolder))
                return ActionResult.newResult(EnumActionResult.PASS, player.getHeldItem(hand));
            MetaTileEntityHolder holder = (MetaTileEntityHolder) tileEntity;
            if (holder.getMetaTileEntity() == null)
                return ActionResult.newResult(EnumActionResult.PASS, player.getHeldItem(hand));
            MetaTileEntityUIFactory.INSTANCE.openUI(holder, (EntityPlayerMP) player);
        }
        return ActionResult.newResult(EnumActionResult.PASS, player.getHeldItem(hand));
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        NBTTagCompound nbt = itemStack.getOrCreateSubCompound("Machine");
        int x = nbt.hasKey("X") ? nbt.getInteger("X") : 0;
        int y = nbt.hasKey("Y") ? nbt.getInteger("Y") : 0;
        int z = nbt.hasKey("Z") ? nbt.getInteger("Z") : 0;
        int worldID = nbt.hasKey("World") ? nbt.getInteger("World") : 0;
        String worldName = nbt.hasKey("WorldName") ? nbt.getString("WorldName") : "Null";
        String name = nbt.hasKey("Name") ? nbt.getString("Name") : "Null";

        lines.add(net.minecraft.client.resources.I18n.format("metaitem.remote_multiblock_controller.description"));
        lines.add(net.minecraft.client.resources.I18n.format("metaitem.remote_multiblock_controller.machine", name));
        lines.add(net.minecraft.client.resources.I18n.format("machine.universal.linked.dimension", worldName, worldID));
        lines.add(net.minecraft.client.resources.I18n.format("metaitem.linking.device.x", x));
        lines.add(net.minecraft.client.resources.I18n.format("metaitem.linking.device.y", y));
        lines.add(net.minecraft.client.resources.I18n.format("metaitem.linking.device.z", z));
    }
}
