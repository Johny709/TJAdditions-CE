package tj.items.behaviours;

import gregtech.api.block.machines.BlockMachine;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import tj.capability.LinkEntity;
import tj.capability.LinkPos;
import tj.event.MTELinkEvent;
import tj.gui.widgets.NewTextFieldWidget;
import tj.items.LinkingMode;

import java.util.List;
import java.util.regex.Pattern;

import static gregtech.api.gui.GuiTextures.BORDERED_BACKGROUND;
import static gregtech.api.gui.GuiTextures.TOGGLE_BUTTON_BACK;
import static net.minecraft.util.EnumHand.MAIN_HAND;
import static net.minecraft.util.EnumHand.OFF_HAND;
import static tj.items.LinkingMode.*;

public class LinkingDeviceBehavior implements IItemBehaviour, ItemUIFactory {

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        NBTTagCompound nbt = stack.getOrCreateSubCompound("Link.XYZ");
        NBTTagCompound promptNBT = stack.getOrCreateSubCompound("Prompt.XYZ");
        NBTTagCompound modeNBT = stack.getOrCreateSubCompound("LinkMode");
        LinkingMode linkingMode = modeNBT.hasKey("Mode") ? LinkingMode.values()[modeNBT.getInteger("Mode")] : BLOCK;
        double x = nbt.hasKey("X") ? nbt.getDouble("X") : 0;
        double y = nbt.hasKey("Y") ? nbt.getDouble("Y") : 0;
        double z = nbt.hasKey("Z") ? nbt.getDouble("Z") : 0;
        int linkI = nbt.hasKey("I") ? nbt.getInteger("I") : 0;
        String name = nbt.hasKey("Name") ? nbt.getString("Name") : "Null";
        MetaTileEntity targetGTTE = BlockMachine.getMetaTileEntity(world, pos);
        TileEntity targetTE = world.getTileEntity(pos);
        if (!world.isRemote && hand == MAIN_HAND) {
            if (!player.isSneaking()) {
                if (!name.equals("Null")) {
                    WorldServer getWorld = nbt.hasKey("DimensionID") ? DimensionManager.getWorld(nbt.getInteger("DimensionID")) : (WorldServer) world;
                    BlockPos worldPos = new BlockPos(x, y, z);
                    getWorld.getChunk(worldPos);
                    MetaTileEntity linkedGTTE = BlockMachine.getMetaTileEntity(getWorld, worldPos);
                    if (linkedGTTE instanceof LinkPos && !(linkedGTTE instanceof LinkEntity) &&(linkingMode == BLOCK || linkingMode == BLOCK_PROMPT)) {
                        LinkPos linkPos = (LinkPos) linkedGTTE;
                        nbt.setInteger("Range", linkPos.getRange());
                        promptNBT.setInteger("world", world.provider.getDimension());
                        promptNBT.setInteger("x", pos.getX());
                        promptNBT.setInteger("y", pos.getY());
                        promptNBT.setInteger("z", pos.getZ());
                        promptNBT.setString("name", world.getBlockState(pos).getBlock().getLocalizedName());
                        stack.getTagCompound().setTag("Link.XYZ", linkPos.getLinkData());
                        if (targetTE != null) {
                            promptNBT.setInteger("x", targetTE.getPos().getX());
                            promptNBT.setInteger("y", targetTE.getPos().getY());
                            promptNBT.setInteger("z", targetTE.getPos().getZ());
                            name = targetTE.getBlockType().getLocalizedName();
                        }
                        if (targetGTTE != null) {
                            promptNBT.setInteger("x", targetGTTE.getPos().getX());
                            promptNBT.setInteger("y", targetGTTE.getPos().getY());
                            promptNBT.setInteger("z", targetGTTE.getPos().getZ());
                            name = targetGTTE.getMetaFullName();
                        }
                        promptNBT.setString("name", net.minecraft.util.text.translation.I18n.translateToLocal(name));
                        if (linkI > 0) {
                            for (int i = 0; i < 2; i++) {
                                for (int j = 0; j < linkPos.getPosSize(); j++) {
                                    BlockPos targetPos = linkPos.getPos(j);
                                    if (i == 0 && targetPos != null && targetPos.getX() == promptNBT.getInteger("x") && targetPos.getY() == promptNBT.getInteger("y") && targetPos.getZ() == promptNBT.getInteger("z")) {
                                        player.sendMessage(new TextComponentTranslation("metaitem.linking.device.message.occupied")
                                                .appendText(" ")
                                                .appendSibling(new TextComponentTranslation(linkedGTTE.getMetaFullName()).setStyle(new Style().setColor(TextFormatting.YELLOW))));
                                        return EnumActionResult.SUCCESS;
                                    }
                                    if (i == 1 && targetPos == null) {
                                        promptNBT.setInteger("index", j);
                                        if (linkingMode == BLOCK_PROMPT) {
                                            PlayerInventoryHolder.openHandItemUI(player, hand);
                                            return EnumActionResult.SUCCESS;
                                        }
                                        if (this.inRange(nbt, promptNBT, player))
                                            this.setPos(nbt, promptNBT, player);
                                        break;
                                    }
                                }
                            }
                        } else player.sendMessage(new TextComponentTranslation("metaitem.linking.device.message.no_remaining"));
                        return EnumActionResult.SUCCESS;
                    }
                } else {
                    player.sendMessage(new TextComponentTranslation("metaitem.linking.device.message.nolink"));
                    return EnumActionResult.SUCCESS;
                }
            } else {
                if (targetGTTE instanceof LinkPos) {
                    LinkPos linkPos = (LinkPos) targetGTTE;
                    boolean hasLink = false;
                    if (linkPos.getLinkData() == null) {
                        linkPos.setLinkData(nbt);
                        this.setLinkData(nbt, targetGTTE, linkPos);
                    } else {
                        nbt = linkPos.getLinkData();
                        player.getHeldItem(hand).getTagCompound().setTag("Link.XYZ", nbt);
                        hasLink = true;
                    }
                    String worldName = world.provider.getDimensionType().getName();
                    int worldID = world.provider.getDimension();
                    ITextComponent textComponent = new TextComponentString(net.minecraft.util.text.translation.I18n.translateToLocalFormatted(hasLink ? "metaitem.linking.device.message.link.continue" : "metaitem.linking.device.message.link",
                            targetGTTE.getMetaFullName(), worldName, worldID, targetGTTE.getPos().getX(), targetGTTE.getPos().getY(), targetGTTE.getPos().getZ()));
                    player.sendMessage(textComponent.appendSibling(new TextComponentString(net.minecraft.util.text.translation.I18n.translateToLocalFormatted("metaitem.linking.device.message.remaining", nbt.getInteger("I")))));
                } else {
                    player.sendMessage(new TextComponentTranslation("metaitem.linking.device.message.capable"));
                    return EnumActionResult.SUCCESS;
                }
                return EnumActionResult.SUCCESS;
            }
        }
        return EnumActionResult.PASS;
    }

    private void setLinkData(NBTTagCompound nbt, MetaTileEntity targetGTTE, LinkPos linkPos) {
        nbt.setString("Name", targetGTTE.getMetaFullName());
        nbt.setDouble("X", targetGTTE.getPos().getX());
        nbt.setDouble("Y", targetGTTE.getPos().getY());
        nbt.setDouble("Z", targetGTTE.getPos().getZ());
        nbt.setInteger("I", linkPos.getPosSize());
        nbt.setInteger("Size", linkPos.getPosSize());
        nbt.setInteger("Range", linkPos.getRange());
        nbt.removeTag("DimensionID");
        if (linkPos.isInterDimensional())
            nbt.setInteger("DimensionID", linkPos.dimensionID());
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        NBTTagCompound nbt = stack.getOrCreateSubCompound("Link.XYZ");
        NBTTagCompound promptNBT = stack.getOrCreateSubCompound("Prompt.XYZ");
        NBTTagCompound modeNBT = stack.getOrCreateSubCompound("LinkMode");
        LinkingMode linkingMode = modeNBT.hasKey("Mode") ? LinkingMode.values()[modeNBT.getInteger("Mode")] : BLOCK;
        double x = nbt.hasKey("X") ? nbt.getDouble("X") : 0;
        double y = nbt.hasKey("Y") ? nbt.getDouble("Y") : 0;
        double z = nbt.hasKey("Z") ? nbt.getDouble("Z") : 0;
        int linkI = nbt.hasKey("I") ? nbt.getInteger("I") : 0;
        String name = nbt.hasKey("Name") ? nbt.getString("Name") : "Null";
        if (!world.isRemote) {
            if (!name.equals("Null") && player.isSneaking() && hand == MAIN_HAND) {
                WorldServer getWorld = nbt.hasKey("DimensionID") ? DimensionManager.getWorld(nbt.getInteger("DimensionID")) : (WorldServer) world;
                BlockPos worldPos = new BlockPos(x, y, z);
                getWorld.getChunk(worldPos);
                MetaTileEntity metaTileEntity = BlockMachine.getMetaTileEntity(getWorld, worldPos);
                if (metaTileEntity instanceof LinkEntity && (linkingMode == ENTITY || linkingMode == ENTITY_PROMPT)) {
                    LinkEntity linkEntity = (LinkEntity) metaTileEntity;
                    promptNBT.setString("name", player.getName());
                    promptNBT.setInteger("world", world.provider.getDimension());
                    promptNBT.setInteger("x", player.getPosition().getX());
                    promptNBT.setInteger("y", player.getPosition().getY());
                    promptNBT.setInteger("z", player.getPosition().getZ());
                    player.getHeldItem(hand).getTagCompound().setTag("Link.XYZ", linkEntity.getLinkData());
                    if (linkI > 0) {
                        for (int i = 0; i < 2; i++) {
                            for (int j = 0; j < linkEntity.getPosSize(); j++) {
                                Entity targetEntity = linkEntity.getEntity(j);
                                if (i == 0 && targetEntity != null && targetEntity.isEntityEqual(player)) {
                                    player.sendMessage(new TextComponentTranslation("metaitem.linking.device.message.occupied", targetEntity.getName()));
                                    return ActionResult.newResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
                                }
                                if (i == 1 && targetEntity == null) {
                                    if (linkingMode == ENTITY_PROMPT) {
                                        PlayerInventoryHolder.openHandItemUI(player, hand);
                                        return ActionResult.newResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
                                    } else if (this.inRange(nbt, promptNBT, player))
                                        this.setPos(nbt, promptNBT, player);
                                    break;
                                }
                            }
                        }
                    } else player.sendMessage(new TextComponentTranslation("metaitem.linking.device.message.no_remaining"));
                }
            } else if (hand == OFF_HAND) {
                int mode = linkingMode.ordinal();
                modeNBT.setInteger("Mode", ++mode > 3 ? 0 : mode);
                linkingMode = LinkingMode.values()[modeNBT.getInteger("Mode")];
                player.sendMessage(new TextComponentTranslation("metaitem.linking.device.message.mode", net.minecraft.util.text.translation.I18n.translateToLocal(linkingMode.getMode())));
            }
        }
        return ActionResult.newResult(EnumActionResult.PASS, player.getHeldItem(hand));
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        NBTTagCompound nbt = itemStack.getOrCreateSubCompound("Link.XYZ");
        NBTTagCompound modeNBT = itemStack.getOrCreateSubCompound("LinkMode");
        LinkingMode linkingMode = modeNBT.hasKey("Mode") ? LinkingMode.values()[modeNBT.getInteger("Mode")] : BLOCK;
        double x = nbt.hasKey("X") ? nbt.getDouble("X") : 0;
        double y = nbt.hasKey("Y") ? nbt.getDouble("Y") : 0;
        double z = nbt.hasKey("Z") ? nbt.getDouble("Z") : 0;
        int linkI = nbt.hasKey("I") ? nbt.getInteger("I") : 0;
        int range = nbt.hasKey("Range") ? nbt.getInteger("Range") : 0;
        int dimensionID = nbt.hasKey("DimensionID") ? nbt.getInteger("DimensionID") : 0;
        String dimensionName = DimensionType.getById(dimensionID).getName();
        String name = nbt.hasKey("Name") ? nbt.getString("Name") : "Null";
        lines.add(I18n.format("metaitem.linking.device.description"));
        lines.add(I18n.format("metaitem.linking.device.name", I18n.format(name)));
        lines.add(I18n.format("machine.universal.linked.dimension", dimensionName, dimensionID));
        lines.add(I18n.format("metaitem.linking.device.x", x));
        lines.add(I18n.format("metaitem.linking.device.y", y));
        lines.add(I18n.format("metaitem.linking.device.z", z));
        lines.add(I18n.format("metaitem.linking.device.message.remaining", linkI));
        lines.add(I18n.format("metaitem.linking.device.range", range));
        lines.add(I18n.format("metaitem.linking.device.message.mode.description"));
        lines.add(I18n.format("metaitem.linking.device.message.mode", net.minecraft.util.text.translation.I18n.translateToLocal(linkingMode.getMode())));
    }

    @Override
    public ModularUI createUI(PlayerInventoryHolder holder, EntityPlayer player) {
        ItemStack stack = player.getHeldItem(MAIN_HAND);
        NBTTagCompound promptNBT = stack.getOrCreateSubCompound("Prompt.XYZ");
        return ModularUI.builder(BORDERED_BACKGROUND, 176, 138)
                .widget(new NewTextFieldWidget<>(4, 14, 166, 18, true)
                        .setValidator(str -> Pattern.compile(".*").matcher(str).matches())
                        .setTextResponder((s, id) -> promptNBT.setString("name", s))
                        .setTextSupplier(() -> promptNBT.getString("name"))
                        .setTooltipText("metaitem.linking.device.set.name")
                        .setMaxStringLength(256)
                        .setUpdateOnTyping(true))
                .widget(new NewTextFieldWidget<>(4, 34, 166, 18, true)
                        .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                        .setTextResponder((s, id) -> promptNBT.setInteger("world", Integer.parseInt(s)))
                        .setTextSupplier(() -> String.valueOf(promptNBT.getInteger("world")))
                        .setTooltipText("metaitem.linking.device.set.world")
                        .setUpdateOnTyping(true)
                        .setMaxStringLength(9))
                .widget(new NewTextFieldWidget<>(4, 54, 166, 18, true)
                        .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                        .setTextResponder((s, id) -> promptNBT.setInteger("x", Integer.parseInt(s)))
                        .setTextSupplier(() -> String.valueOf(promptNBT.getInteger("x")))
                        .setTooltipText("metaitem.linking.device.set.x")
                        .setUpdateOnTyping(true)
                        .setMaxStringLength(9))
                .widget(new NewTextFieldWidget<>(4, 74, 166, 18, true)
                        .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                        .setTextResponder((s, id) -> promptNBT.setInteger("y", Integer.parseInt(s)))
                        .setTextSupplier(() -> String.valueOf(promptNBT.getInteger("y")))
                        .setTooltipText("metaitem.linking.device.set.y")
                        .setUpdateOnTyping(true)
                        .setMaxStringLength(9))
                .widget(new NewTextFieldWidget<>(4, 94, 166, 18, true)
                        .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                        .setTextResponder((s, id) -> promptNBT.setInteger("z", Integer.parseInt(s)))
                        .setTextSupplier(() -> String.valueOf(promptNBT.getInteger("z")))
                        .setTooltipText("metaitem.linking.device.set.z")
                        .setUpdateOnTyping(true)
                        .setMaxStringLength(9))
                .widget(new ToggleButtonWidget(4, 114, 166, 18, TOGGLE_BUTTON_BACK, () -> false, (bool) -> {
                    if (this.inRange(stack.getOrCreateSubCompound("Link.XYZ"), promptNBT, player))
                        this.setPos(stack.getOrCreateSubCompound("Link.XYZ"), promptNBT, player);
                    player.closeScreen();
                })).label(60, 120, "metaitem.linking.device.set.position")
                .build(holder, player);
    }

    private boolean inRange(NBTTagCompound nbt, NBTTagCompound promptNBT, EntityPlayer player) {
        double x = nbt.hasKey("X") ? nbt.getDouble("X") : 0;
        double y = nbt.hasKey("Y") ? nbt.getDouble("Y") : 0;
        double z = nbt.hasKey("Z") ? nbt.getDouble("Z") : 0;
        int xDiff = (int) Math.abs(promptNBT.getInteger("x") - x);
        int yDiff = (int) Math.abs(promptNBT.getInteger("y") - y);
        int zDiff = (int) Math.abs(promptNBT.getInteger("z") - z);
        int targetRange = xDiff + yDiff + zDiff;
        int range = nbt.getInteger("Range");
        boolean inRange = range >= targetRange;
        if (!inRange)
            player.sendMessage(new TextComponentTranslation("metaitem.linking.device.message.far"));
        return inRange;
    }

    private void setPos(NBTTagCompound nbt, NBTTagCompound promptNBT, EntityPlayer player) {
        World world = player.getEntityWorld();
        BlockPos pos = new BlockPos(promptNBT.getInteger("x"), promptNBT.getInteger("y"), promptNBT.getInteger("z"));
        double x = nbt.getDouble("X");
        double y = nbt.getDouble("Y");
        double z = nbt.getDouble("Z");
        WorldServer getWorld = nbt.hasKey("DimensionID") ? DimensionManager.getWorld(nbt.getInteger("DimensionID")) : (WorldServer) world;
        MetaTileEntity linkedGTTE = BlockMachine.getMetaTileEntity(getWorld, new BlockPos(x, y, z));
        MetaTileEntity targetGTTE = BlockMachine.getMetaTileEntity(world, pos);
        if (linkedGTTE instanceof LinkPos && !MinecraftForge.EVENT_BUS.post(new MTELinkEvent(linkedGTTE, targetGTTE))) {
            int linkI = nbt.getInteger("I");
            nbt.setInteger("I", linkI - 1);
            world.getChunk(pos);
            ((LinkPos) linkedGTTE).setPos(promptNBT.getString("name"), pos, player, player.getEntityWorld(), promptNBT.getInteger("index"));
            ((LinkPos) linkedGTTE).setLinkData(nbt);
            player.sendMessage(new TextComponentTranslation("metaitem.linking.device.message.success")
                    .appendText("\n")
                    .appendSibling(new TextComponentTranslation("metaitem.linking.device.message.remaining"))
                    .appendSibling(new TextComponentString(" " + nbt.getInteger("I"))));
        } else player.sendMessage(new TextComponentTranslation("metaitem.linking.device.message.fail"));
    }
}
