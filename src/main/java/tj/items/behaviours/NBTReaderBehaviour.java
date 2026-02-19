package tj.items.behaviours;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import tj.gui.widgets.impl.ScrollableDisplayWidget;
import tj.items.TJMetaItems;

import java.util.List;

public class NBTReaderBehaviour implements IItemBehaviour, ItemUIFactory {

    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity == null)
            return EnumActionResult.FAIL;
        ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
        if (!TJMetaItems.NBT_READER.isItemEqual(stack))
            return EnumActionResult.FAIL;
        stack.getOrCreateSubCompound("NBTReader").setTag("TEData", tileEntity.writeToNBT(new NBTTagCompound()));
        stack.getOrCreateSubCompound("NBTReader").setString("TEName", tileEntity.getBlockType().getTranslationKey() + ".name");
        if (!world.isRemote) {
         PlayerInventoryHolder holder = new PlayerInventoryHolder(player, hand);
         holder.openUI();
         return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.PASS;
    }

    @Override
    public ModularUI createUI(PlayerInventoryHolder holder, EntityPlayer player) {
        NBTTagCompound compound = player.getHeldItem(EnumHand.MAIN_HAND).getOrCreateSubCompound("NBTReader");
        String name = compound.getString("TEName");
        String data = compound.getTag("TEData").toString().replace(":", ":§e ")
                .replace(",", "\n§r ")
                .replace("[", "§b[§r")
                .replace("]", "§b]§r")
                .replace("{", "§b{§r")
                .replace("}", "§b}§r");
        ScrollableDisplayWidget scrollWidget = new ScrollableDisplayWidget(10, 15, 180, 114);
        scrollWidget.addWidget(new AdvancedTextWidget(2, 5, textList -> textList.add(new TextComponentString(data)), 0xFFFFFF)
                .setMaxWidthLimit(174));
        ModularUI.Builder builder = new ModularUI.Builder(GuiTextures.BORDERED_BACKGROUND, 176, 226)
                .label(7, 4, name)
                .image(7, 12, 162, 120, GuiTextures.DISPLAY)
                .bindPlayerInventory(player.inventory, 145)
                .widget(scrollWidget);
        return builder.build(holder, player);
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        lines.add(I18n.format("metaitem.nbt_reader.description"));
    }
}
