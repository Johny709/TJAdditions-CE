package tj.gui;

import appeng.client.gui.implementations.GuiInterface;
import appeng.container.implementations.ContainerInterface;
import appeng.helpers.IInterfaceHost;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import tj.gui.container.ContainerPatternInterface;
import tj.gui.gui.GuiPatternInterface;

import javax.annotation.Nullable;

public class GuiHandler implements IGuiHandler {

    public static int SUPER_INTERFACE = 0;
    public static int SUPER_FLUID_INTERFACE = 1;
    public static int SUPER_DUAL_INTERFACE = 2;
    public static int PATTERN_INTERFACE = 3;

    @Nullable
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        final TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
        if (ID == SUPER_INTERFACE && tileEntity instanceof IInterfaceHost) {
            return new ContainerInterface(player.inventory, (IInterfaceHost) tileEntity);
        } else if (ID == SUPER_FLUID_INTERFACE && tileEntity instanceof IInterfaceHost) {
            return new ContainerInterface(player.inventory, (IInterfaceHost) tileEntity);
        } else if (ID == SUPER_DUAL_INTERFACE && tileEntity instanceof IInterfaceHost) {
            return new ContainerInterface(player.inventory, (IInterfaceHost) tileEntity);
        } else if (ID == PATTERN_INTERFACE && tileEntity instanceof IInterfaceHost) {
            return new ContainerPatternInterface(player.inventory, (IInterfaceHost) tileEntity);
        } else return null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        final TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
        if (ID == SUPER_INTERFACE && tileEntity instanceof IInterfaceHost) {
            return new GuiInterface(player.inventory, (IInterfaceHost) tileEntity);
        } else if (ID == SUPER_FLUID_INTERFACE && tileEntity instanceof IInterfaceHost) {
            return new GuiInterface(player.inventory, (IInterfaceHost) tileEntity);
        } else if (ID == SUPER_DUAL_INTERFACE && tileEntity instanceof IInterfaceHost) {
            return new GuiInterface(player.inventory, (IInterfaceHost) tileEntity);
        } else if (ID == PATTERN_INTERFACE && tileEntity instanceof IInterfaceHost) {
            return new GuiPatternInterface(player.inventory, (IInterfaceHost) tileEntity);
        } else return null;
    }
}
