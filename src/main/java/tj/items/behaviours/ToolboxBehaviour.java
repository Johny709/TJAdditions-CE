package tj.items.behaviours;

import gregicadditions.item.GAMetaItems;
import gregicadditions.machines.multi.IMaintenance;
import gregicadditions.machines.multi.multiblockpart.MetaTileEntityMaintenanceHatch;
import gregicadditions.tools.GTToolTypes;
import gregtech.api.block.machines.BlockMachine;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.items.toolitem.ToolMetaItem;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.Position;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;
import tj.gui.TJGuiUtils;
import tj.gui.widgets.TJSlotWidget;
import tj.items.handlers.FilteredItemStackHandler;

import java.util.List;

public class ToolboxBehaviour implements IItemBehaviour, ItemUIFactory {

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (world.isRemote || hand == EnumHand.OFF_HAND)
            return ActionResult.newResult(EnumActionResult.FAIL, player.getHeldItem(hand));
        PlayerInventoryHolder holder = new PlayerInventoryHolder(player, hand);
        holder.openUI();
        return ActionResult.newResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        if (world.isRemote || hand == EnumHand.OFF_HAND)
            return EnumActionResult.PASS;
        ItemStack toolStack = player.getHeldItem(hand);
        NBTTagCompound compound = toolStack.getTagCompound();
        MetaTileEntity metaTileEntity = BlockMachine.getMetaTileEntity(world, pos);
        if (metaTileEntity instanceof MetaTileEntityMaintenanceHatch) {
            MetaTileEntityMaintenanceHatch maintenanceHatch = (MetaTileEntityMaintenanceHatch) metaTileEntity;
            IMaintenance controller = (IMaintenance) maintenanceHatch.getController();
            if (!controller.hasProblems()) {
                player.sendMessage(new TextComponentTranslation("gtadditions.multiblock.universal.no_problems")
                        .setStyle(new Style().setColor(TextFormatting.GREEN)));
                return EnumActionResult.SUCCESS;
            }
            if (controller == null || compound == null || !compound.hasKey("inventory"))
                return EnumActionResult.PASS;
            ItemStackHandler handler = new ItemStackHandler(18);
            handler.deserializeNBT(compound.getCompoundTag("inventory"));

            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stack = handler.getStackInSlot(i);
                if (GAMetaItems.INSULATING_TAPE.isItemEqual(stack)) {
                    stack.shrink(1);
                    for (int j = 0; j < 6; j++)
                        controller.setMaintenanceFixed(j);
                    maintenanceHatch.setTaped(true);
                    break;
                }
            }

            byte problems = controller.getProblems();
            for (byte i = 0; i < 6; i++) {
                if (((problems >> i) & 1) == 0) {
                    List<ToolMetaItem.MetaToolValueItem> tools = null;

                    switch (i) {
                        case 0:
                            tools = GTToolTypes.wrenches;
                            break;
                        case 1:
                            tools = GTToolTypes.screwdrivers;
                            break;
                        case 2:
                            tools = GTToolTypes.softHammers;
                            break;
                        case 3:
                            tools = GTToolTypes.hardHammers;
                            break;
                        case 4:
                            tools = GTToolTypes.wireCutters;
                            break;
                        case 5:
                            tools = GTToolTypes.crowbars;
                            break;
                    }

                    for (int j = 0; j < tools.size(); j++) {
                        ToolMetaItem.MetaToolValueItem tool = tools.get(j);
                        for (int k = 0; k < handler.getSlots(); k++) {
                            ItemStack itemStack = handler.getStackInSlot(k);
                            if (itemStack.isItemEqualIgnoreDurability(tool.getStackForm())) {
                                controller.setMaintenanceFixed(i);
                                damageTool(itemStack);
                                maintenanceHatch.setTaped(false);
                            }
                        }
                    }
                }
            }

            compound.setTag("inventory", handler.serializeNBT());
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.PASS;
    }

    private void damageTool(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ToolMetaItem) {
            ToolMetaItem<?> toolMetaItem = (ToolMetaItem<?>) itemStack.getItem();
            toolMetaItem.damageItem(itemStack, 1, true, false);
        }
    }

    @Override
    public ModularUI createUI(PlayerInventoryHolder holder, EntityPlayer player) {
        ItemStack playerStack = player.getHeldItem(EnumHand.MAIN_HAND);
        if (playerStack.getTagCompound() == null)
            playerStack.setTagCompound(new NBTTagCompound());
        final NBTTagCompound compound = playerStack.getTagCompound();
        ItemStackHandler toolboxInventory = new FilteredItemStackHandler(null, 18) {
            @Override
            protected void onContentsChanged(int slot) {
                compound.setTag("inventory", this.serializeNBT());
            }
        }.setItemStackPredicate((slot, stack) -> GAMetaItems.INSULATING_TAPE.isItemEqual(stack) || stack.getItem() instanceof ToolMetaItem<?>);
        WidgetGroup widgetGroup = new WidgetGroup(new Position(7, 20));
        for (int i = 0; i < toolboxInventory.getSlots(); i++) {
            widgetGroup.addWidget(new TJSlotWidget<>(toolboxInventory, i, 18 * (i % 9), 18 * (i / 9))
                    .setBackgroundTexture(GuiTextures.SLOT));
        }
        return ModularUI.defaultBuilder()
                .bindOpenListener(() -> toolboxInventory.deserializeNBT(compound.getCompoundTag("inventory")))
                .bindCloseListener(() -> compound.setTag("inventory", toolboxInventory.serializeNBT()))
                .widget(widgetGroup)
                .widget(TJGuiUtils.bindPlayerInventory(new WidgetGroup(), player.inventory, 7, 84, playerStack))
                .label(7, 5, "metaitem.toolbox.name")
                .build(holder, player);
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        lines.add(I18n.format("metaitem.toolbox.description"));
    }
}
