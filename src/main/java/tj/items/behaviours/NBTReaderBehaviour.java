package tj.items.behaviours;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.gui.widgets.ScrollableListWidget;
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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import java.util.List;

public class NBTReaderBehaviour implements IItemBehaviour, ItemUIFactory {

    private String name;
    private NBTTagCompound compound;

    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        TileEntity tileEntity = world.getTileEntity(pos);
        this.name = tileEntity.getBlockType().getTranslationKey() + ".name";
        this.compound = new NBTTagCompound();
        if (!world.isRemote) {
         tileEntity.writeToNBT(compound);

         PlayerInventoryHolder holder = new PlayerInventoryHolder(player, hand);
         holder.openUI();
         return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.PASS;
    }

    @Override
    public ModularUI createUI(PlayerInventoryHolder holder, EntityPlayer player) {
        ScrollableListWidget scrollWidget = new ScrollableListWidget(10, 15, 180, 114);
        scrollWidget.addWidget(new AdvancedTextWidget(2, 5, this::addDisplayText, 0xFFFFFF)
                .setMaxWidthLimit(174));
        ModularUI.Builder builder = new ModularUI.Builder(GuiTextures.BORDERED_BACKGROUND, 176, 226)
                .label(7, 4, name)
                .image(7, 12, 162, 120, GuiTextures.DISPLAY)
                .bindPlayerInventory(player.inventory, 145)
                .widget(scrollWidget);
        return builder.build(holder, player);
    }

    private void addDisplayText(List<ITextComponent> textList) {
        textList.add(new TextComponentString(compound.toString()
                .replace(":", ":§e ")
                .replace(",", "\n§r ")
                .replace("[", "§b[§r")
                .replace("]", "§b]§r")
                .replace("{", "§b{§r")
                .replace("}", "§b}§r")));

    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        lines.add(I18n.format("metaitem.nbt_reader.description"));
    }
}
