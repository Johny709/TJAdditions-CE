package tj.items.behaviours;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import tj.gui.TJGuiTextures;
import tj.gui.TJGuiUtils;
import tj.gui.widgets.TJLabelWidget;
import tj.gui.widgets.impl.AnimatedImageWidget;
import tj.gui.widgets.impl.ScrollableDisplayWidget;
import tj.items.TJMetaItems;

import java.util.List;

import static tj.gui.TJGuiTextures.MULTIBLOCK_DISPLAY_BASE;
import static tj.gui.TJGuiTextures.TJ_LOGO_ANIMATED;

public class NBTReaderBehaviour implements IItemBehaviour, ItemUIFactory {

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (!world.isRemote)
            PlayerInventoryHolder.openHandItemUI(player, hand);
        return ActionResult.newResult(EnumActionResult.SUCCESS, player.getHeldItemMainhand());
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        if (!player.isSneaking())
            return EnumActionResult.FAIL;
        final ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
        if (!TJMetaItems.NBT_READER.isItemEqual(stack))
            return EnumActionResult.FAIL;
        final TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity == null)
            return EnumActionResult.FAIL;
        final NBTTagCompound compound = stack.getOrCreateSubCompound("NBTReader");
        compound.setTag("TEData", tileEntity.writeToNBT(new NBTTagCompound()));
        compound.setString("TEName", tileEntity.getBlockType().getTranslationKey() + ".name");
        if (world.isRemote)
            player.sendStatusMessage(new TextComponentTranslation("metaitem.nbt_reader.saved", new TextComponentTranslation(tileEntity.getBlockType().getTranslationKey() + ".name")), true);
        return EnumActionResult.SUCCESS;
    }

    @Override
    public ModularUI createUI(PlayerInventoryHolder holder, EntityPlayer player) {
        final ItemStack itemStack = player.getHeldItem(EnumHand.MAIN_HAND);
        final NBTTagCompound compound = itemStack.getOrCreateSubCompound("NBTReader");
        final String name = compound.getString("TEName");
        final String data = compound.getTag("TEData").toString().replace(":", ":§e ")
                .replace(",", "\n§r ")
                .replace("[", "§b[§r")
                .replace("]", "§b]§r")
                .replace("{", "§b{§r")
                .replace("}", "§b}§r");
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 200, 216)
                .widget(new TJLabelWidget(9, -38, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(TJMetaItems.NBT_READER.getStackForm()).setLocale(name))
                .image(0, -20, 200, 237, GuiTextures.BORDERED_BACKGROUND)
                .image(6, -14, 188, 145, MULTIBLOCK_DISPLAY_BASE)
                .widget(new ScrollableDisplayWidget(10, -11, 187, 140)
                        .addTextWidget(new AdvancedTextWidget(0, 0, textList -> textList.add(new TextComponentString(data)), 0xFFFFFF)
                                .setMaxWidthLimit(180))
                        .setScrollPanelWidth(3))
                .widget(TJGuiUtils.bindPlayerInventory(new WidgetGroup(), player.inventory, 7, 134, itemStack))
                .widget(new AnimatedImageWidget(164, 102, 26, 26, 41, TJ_LOGO_ANIMATED))
                .build(holder, player);
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        lines.add(I18n.format("metaitem.nbt_reader.description"));
    }
}
