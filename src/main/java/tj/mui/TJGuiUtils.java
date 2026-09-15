package tj.mui;

import appeng.core.Api;
import baubles.api.BaublesApi;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.util.TextFormattingUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import tj.TJValues;
import tj.items.handlers.FilteredItemStackHandler;
import tj.items.item.TJItems;
import tj.mui.widgets.ButtonWidget;
import tj.mui.widgets.impl.AEPatternSlotWidget;
import tj.mui.widgets.impl.SlotScrollableWidgetGroup;
import tj.mui.widgets.impl.TJSlotWidget;
import tj.util.TJItemUtils;

import javax.annotation.Nullable;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.Optional;
import java.util.function.LongUnaryOperator;
import java.util.function.Predicate;

import static gregtech.api.gui.resources.RenderUtil.setGlColorFromInt;


public final class TJGuiUtils {

    private TJGuiUtils() {}

    public static boolean isServer() {
        return FMLCommonHandler.instance().getEffectiveSide().isServer();
    }

    /**
     *
     * @param widgetGroup widget group for slots to get added to
     * @param inventoryPlayer inventory of player
     * @param x X position
     * @param y Y position
     * @param stack the stack used to open this GUI. this prevents the item being taken out from slot while GUI is open.
     * @return widget
     */
    public static WidgetGroup bindPlayerInventory(WidgetGroup widgetGroup, InventoryPlayer inventoryPlayer, int x, int y, ItemStack stack) {
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                final boolean interact = inventoryPlayer.player.inventory.getStackInSlot(col + (row + 1) * 9) != stack || stack.isEmpty();
                widgetGroup.addWidget((new SlotWidget(inventoryPlayer, col + (row + 1) * 9, x + col * 18, y + row * 18, interact, interact))
                        .setBackgroundTexture(GuiTextures.SLOT)
                        .setLocationInfo(true, false));
            }
        }
        return bindPlayerHotbar(widgetGroup, inventoryPlayer, x, y + 58, stack);
    }

    private static WidgetGroup bindPlayerHotbar(WidgetGroup widgetGroup, InventoryPlayer inventoryPlayer, int x, int y, ItemStack stack) {
        for (int slot = 0; slot < 9; ++slot) {
            final boolean interact = inventoryPlayer.player.inventory.getStackInSlot(slot) != stack || stack.isEmpty();
            widgetGroup.addWidget((new SlotWidget(inventoryPlayer, slot, x + slot * 18, y, interact, interact))
                    .setBackgroundTexture(GuiTextures.SLOT)
                    .setLocationInfo(true, true));
        }
        return widgetGroup;
    }

    public static void drawFluidForGui(FluidStack contents, long fluidAmount, long tankCapacity, long startX, long startY, long widthT, long heightT) {
        widthT--;
        final Fluid fluid = contents.getFluid();
        final ResourceLocation fluidStill = fluid.getStill();
        final TextureAtlasSprite fluidStillSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(fluidStill.toString());
        final int fluidColor = fluid.getColor(contents);
        long scaledAmount = fluidAmount * widthT / Math.max(1, tankCapacity);
        if (fluidAmount > 0 && scaledAmount < 1) {
            scaledAmount = 1;
        }
        if (scaledAmount > widthT) {
            scaledAmount = widthT;
        }
        GlStateManager.enableBlend();
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        setGlColorFromInt(fluidColor, 200);

        final long xTileCount = scaledAmount / 16;
        final long xRemainder = scaledAmount - xTileCount * 16;
        final long yTileCount = heightT / 16;
        final long yRemainder = heightT - yTileCount * 16;

        final long yStart = startY + heightT;

        for (long xTile = 0; xTile <= xTileCount; xTile++) {
            for (long yTile = 0; yTile <= yTileCount; yTile++) {
                final long width = xTile == xTileCount ? xRemainder : 16;
                final long height = yTile == yTileCount ? yRemainder : 16;
                final long x = startX + xTile * 16;
                final long y = yStart - (yTile + 1) * 16;
                if (width > 0 && height > 0) {
                    final long maskTop = 16 - height;
                    final long maskRight = 16 - width;

                    drawFluidTexture(x, y, fluidStillSprite, maskTop, maskRight, 0.0);
                }
            }
        }
        GlStateManager.disableBlend();
    }

    private static void drawFluidTexture(double xCoord, double yCoord, TextureAtlasSprite textureSprite, long maskTop, long maskRight, double zLevel) {
        final double uMin = textureSprite.getMinU();
        double uMax = textureSprite.getMaxU();
        final double vMin = textureSprite.getMinV();
        double vMax = textureSprite.getMaxV();
        uMax = uMax - maskRight / 16.0 * (uMax - uMin);
        vMax = vMax - maskTop / 16.0 * (vMax - vMin);

        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(xCoord, yCoord + 16, zLevel).tex(uMin, vMax).endVertex();
        buffer.pos(xCoord + 16 - maskRight, yCoord + 16, zLevel).tex(uMax, vMax).endVertex();
        buffer.pos(xCoord + 16 - maskRight, yCoord + maskTop, zLevel).tex(uMax, vMin).endVertex();
        buffer.pos(xCoord, yCoord + maskTop, zLevel).tex(uMin, vMin).endVertex();
        tessellator.draw();
    }

    public static ItemStack getPatternMultiTool(EntityPlayer player) {
        return Optional.of(player.inventory.mainInventory)
                .map(inventory -> {
                    final ItemStack patternMultitool = TJItemUtils.getItemStackFromName("nae2:pattern_multiplier");
                    final ItemStack patternMultitool2 = TJItems.SUPER_PATTERN_MULTITOOL.maybeStack(1).orElse(ItemStack.EMPTY);
                    for (ItemStack stack : inventory)
                        if (stack.isItemEqual(patternMultitool) || stack.isItemEqual(patternMultitool2))
                            return stack;
                    final IItemHandlerModifiable baubleSlots = BaublesApi.getBaublesHandler(player);
                    for (int i = 0; i < baubleSlots.getSlots(); i++)
                        if (baubleSlots.getStackInSlot(i).isItemEqual(patternMultitool) || baubleSlots.getStackInSlot(i).isItemEqual(patternMultitool2))
                            return baubleSlots.getStackInSlot(i);
                    return ItemStack.EMPTY;
                }).get();
    }

    public static void createPatternMultiToolGUI(ModularUI.Builder builder, ItemStack patternMultiTool, FilteredItemStackHandler multiUpgradeSlots, IItemHandler patternSlots, FilteredItemStackHandler multiPatternSlots, NBTTagCompound invTag) {
        if (!patternMultiTool.isEmpty()) {
            final boolean isUpgraded = patternMultiTool.isItemEqual(TJItems.SUPER_PATTERN_MULTITOOL.maybeStack(1).orElse(ItemStack.EMPTY));
            final SlotScrollableWidgetGroup multiPatternSlotGroup = new SlotScrollableWidgetGroup(-122, 14, 76, 162, 4)
                    .setItemStackTransfer(itemStack -> TJItemUtils.insertIntoItemHandler(patternSlots, itemStack, false))
                    .setItemHandler(multiPatternSlots)
                    .setScrollWidth(4);
            builder.widget(new ImageWidget(-129, 0, 109, 218, GuiTextures.BORDERED_BACKGROUND))
                    .widget(new LabelWidget(-122, 4, "item.nae2.pattern_multiplier.name"))
                    .widget(new ButtonWidget<>(-122, 176, 18, 18, "*2", data ->
                            changePatternAmount(multiPatternSlots, multiPatternSlotGroup, m -> m * 2,
                                    () -> writePatternMultiToolToNBT(multiPatternSlots, invTag)))
                            .setHoverTooltipText("gui.pattern_term.auto_fill_pattern.MULTIPLY_2.text")
                            .setTitleHoverTooltipText("gui.action.MULTIPLY_2.name")
                            .setBackgroundTextures(GuiTextures.VANILLA_BUTTON))
                    .widget(new ButtonWidget<>(-122, 194, 18, 18, "/2", data ->
                            changePatternAmount(multiPatternSlots, multiPatternSlotGroup, m -> m / 2, () -> writePatternMultiToolToNBT(multiPatternSlots, invTag)))
                            .setHoverTooltipText("gui.pattern_term.auto_fill_pattern.DIVIDE_2.text")
                            .setTitleHoverTooltipText("gui.action.DIVIDE_2.name")
                            .setBackgroundTextures(GuiTextures.VANILLA_BUTTON))
                    .widget(new ButtonWidget<>(-104, 176, 18, 18, "*3", data ->
                            changePatternAmount(multiPatternSlots, multiPatternSlotGroup, m -> m * 3, () -> writePatternMultiToolToNBT(multiPatternSlots, invTag)))
                            .setHoverTooltipText("gui.pattern_term.auto_fill_pattern.MULTIPLY_3.text")
                            .setTitleHoverTooltipText("gui.action.MULTIPLY_3.name")
                            .setBackgroundTextures(GuiTextures.VANILLA_BUTTON))
                    .widget(new ButtonWidget<>(-104, 194, 18, 18, "/3", data ->
                            changePatternAmount(multiPatternSlots, multiPatternSlotGroup, m -> m / 3, () -> writePatternMultiToolToNBT(multiPatternSlots, invTag)))
                            .setHoverTooltipText("gui.pattern_term.auto_fill_pattern.DIVIDE_3.text")
                            .setTitleHoverTooltipText("gui.action.DIVIDE_3.name")
                            .setBackgroundTextures(GuiTextures.VANILLA_BUTTON))
                    .widget(new ButtonWidget<>(-86, 176, 18, 18, "+1", data ->
                            changePatternAmount(multiPatternSlots, multiPatternSlotGroup, m -> m + 1, () -> writePatternMultiToolToNBT(multiPatternSlots, invTag)))
                            .setTitleHoverTooltipText("gui.tooltips.appliedenergistics2.IncreaseByOne")
                            .setHoverTooltipText("gui.tooltips.appliedenergistics2.IncreaseByOneDesc")
                            .setBackgroundTextures(GuiTextures.VANILLA_BUTTON))
                    .widget(new ButtonWidget<>(-86, 194, 18, 18, "-1", data ->
                            changePatternAmount(multiPatternSlots, multiPatternSlotGroup, m -> m - 1, () -> writePatternMultiToolToNBT(multiPatternSlots, invTag)))
                            .setTitleHoverTooltipText("gui.tooltips.appliedenergistics2.DecreaseByOne")
                            .setHoverTooltipText("gui.tooltips.appliedenergistics2.DecreaseByOneDesc")
                            .setBackgroundTextures(GuiTextures.VANILLA_BUTTON))
                    .widget(new ButtonWidget<>(-68, 176, 36, 36, "X", data ->
                            clearPatterns(multiPatternSlots, () -> writePatternMultiToolToNBT(multiPatternSlots, invTag)))
                            .setTitleHoverTooltipText("nae2.pattern_multiplier.unencode")
                            .setHoverTooltipText("nae2.pattern_multiplier.unencode.desc")
                            .setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
            if (isUpgraded) {
                multiPatternSlots.setSize(72);
                multiUpgradeSlots.setSize(7);
            }
            for (int i = 0; i < multiPatternSlots.getSlots(); i++) {
                final int index = i;
                multiPatternSlotGroup.addWidget(new AEPatternSlotWidget(multiPatternSlots, i, 18 * (i / (isUpgraded ? 18 : 9)), 18 * (i % (isUpgraded ? 18 : 9)))
                        .setActiveBackgroundTexture(GuiTextures.SLOT, TJGuiTextures.PATTERN_OVERLAY)
                        .setActiveSupplier(() -> index / 9 <= multiUpgradeSlots.getSlotsFilled())
                        .setSlotLocationInfo(true, false)
                        .setInactiveBackgroundTexture(TJGuiTextures.BLANK_SLOT)
                        .setWidgetGroup(multiPatternSlotGroup)
                        .setSlotProtection(true));
            }
            builder.widget(multiPatternSlotGroup);
            for (int i = 0; i < multiUpgradeSlots.getSlots(); i++) {
                builder.widget(new TJSlotWidget<>(multiUpgradeSlots, i, -46, 14 + (i * 18))
                        .setActiveBackgroundTexture(GuiTextures.SLOT, TJGuiTextures.UPGRADE_OVERLAY)
                        .setSlotProtection(true));
            }
        }
    }

    public static void writePatternMultiToolToNBT(IItemHandler itemHandler, NBTTagCompound compound) {
        final NBTTagList tagList = new NBTTagList();
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            final ItemStack stack = itemHandler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                final NBTTagCompound tagCompound = stack.serializeNBT();
                tagCompound.setInteger("Slot", i);
                tagList.appendTag(tagCompound);
            }
        }
        compound.setTag("Items", tagList);
    }

    public static void readPatternMultiToolNBT(IItemHandlerModifiable itemHandler, NBTTagList tagList) {
        for (int i = 0; i < tagList.tagCount(); i++) {
            final NBTTagCompound compound = tagList.getCompoundTagAt(i);
            if (compound.hasKey("Slot")) {
                final ItemStack patternStack = TJItemUtils.getItemStackFromName(compound.getString("id"), compound.getInteger("Count"), compound.getShort("Damage"));
                if (compound.hasKey("tag", Constants.NBT.TAG_COMPOUND))
                    patternStack.setTagCompound(compound.getCompoundTag("tag"));
                itemHandler.setStackInSlot(compound.getInteger("Slot"), patternStack);
            }
        }
    }

    public static void changePatternAmount(IItemHandler patternSlots, SlotScrollableWidgetGroup patternSlotWidgets, LongUnaryOperator multiplier, Runnable callback) {
        for (int i = 0; i < patternSlots.getSlots(); i++) {
            final ItemStack stack = patternSlots.getStackInSlot(i);
            final NBTTagCompound compound = stack.getTagCompound();
            if (stack.isEmpty() || compound == null) continue;
            final ResourceLocation resourcelocation = Item.REGISTRY.getNameForObject(stack.getItem());
            final String id = resourcelocation != null ? resourcelocation.toString() : "minecraft:air";
            final NBTTagList inputList = compound.getTagList(id.equals("ae2fc:dense_encoded_pattern") ? "Inputs" : "in", 10);
            final NBTTagList outputList = compound.getTagList(id.equals("ae2fc:dense_encoded_pattern") ? "Outputs" : "out", 10);
            final NBTTagList newInputList = new NBTTagList(), newOutputList = new NBTTagList();
            final Predicate<Boolean> setPatternInputs = simulate -> {
                for (int j = 0; j < inputList.tagCount(); j++) {
                    final NBTTagCompound patternCompound = inputList.getCompoundTagAt(j);
                    final long amount = patternCompound.hasKey("Cnt") ? patternCompound.getLong("Cnt") : patternCompound.getInteger("Count");
                    final long newAmount = multiplier.applyAsLong(amount);
                    if (patternCompound.isEmpty()) {
                        if (!simulate)
                            newInputList.appendTag(patternCompound);
                        continue;
                    }
                    if (newAmount > 0 && newAmount <= Integer.MAX_VALUE) {
                        if (!simulate) {
                            if (id.equals("ae2fc:dense_encoded_pattern")) {
                                patternCompound.setLong("Cnt", newAmount);
                            } else patternCompound.setInteger("Count", (int) newAmount);
                            newInputList.appendTag(patternCompound);
                        }
                    } else return false;
                }
                for (int j = 0; j < outputList.tagCount(); j++) {
                    final NBTTagCompound patternCompound = outputList.getCompoundTagAt(j);
                    final long amount = patternCompound.hasKey("Cnt") ? patternCompound.getLong("Cnt") : patternCompound.getInteger("Count");
                    final long newAmount = multiplier.applyAsLong(amount);
                    if (patternCompound.isEmpty()) {
                        if (!simulate)
                            newOutputList.appendTag(patternCompound);
                        continue;
                    }
                    if (newAmount > 0 && newAmount <= Integer.MAX_VALUE) {
                        if (!simulate) {
                            if (id.equals("ae2fc:dense_encoded_pattern")) {
                                patternCompound.setLong("Cnt", newAmount);
                            } else patternCompound.setInteger("Count", (int) newAmount);
                            newOutputList.appendTag(patternCompound);
                        }
                    } else return false;
                }
                if (!simulate) {
                    compound.setTag("in", newInputList);
                    compound.setTag("out", newOutputList);
                    if (id.equals("ae2fc:dense_encoded_pattern")) {
                        compound.setTag("Inputs", newInputList);
                        compound.setTag("Outputs", newOutputList);
                    }
                }
                return true;
            };
            if (setPatternInputs.test(true))
                setPatternInputs.test(false);
        }
        callback.run();
        patternSlotWidgets.getNativeWidgets().stream()
                .filter(widget -> widget instanceof TJSlotWidget<?>)
                .forEach(slot -> ((TJSlotWidget<?>) slot).forceUpdate());
    }

    public static void clearPatterns(IItemHandler patternSlots, Runnable callback) {
        for (int i = 0; i < patternSlots.getSlots(); i++) {
            ItemStack pattern = patternSlots.extractItem(i, Integer.MAX_VALUE, false);
            pattern = Api.INSTANCE.definitions().materials().blankPattern().maybeStack(pattern.getCount()).orElse(ItemStack.EMPTY);
            patternSlots.insertItem(i, pattern, false);
        }
        callback.run();
    }

    public static void updatePatterns(IItemHandler patternSlots) {
        final NonNullList<ItemStack> itemStacks = NonNullList.create();
        for (int i = 0; i < patternSlots.getSlots(); i++)
            itemStacks.add(patternSlots.extractItem(i, Integer.MAX_VALUE, false));
        for (int i = 0; i < patternSlots.getSlots(); i++)
            patternSlots.insertItem(i, itemStacks.get(i), false);
    }

    @SideOnly(Side.CLIENT)
    public static void draw(double x, double y, int width, int height, ResourceLocation imageLocation) {
        drawSubArea(x, y, width, height, 0.0, 0.0, 1.0, 1.0, imageLocation);
    }

    @SideOnly(Side.CLIENT)
    public static void drawSubArea(double x, double y, int width, int height, double drawnU, double drawnV, double drawnWidth, double drawnHeight, ResourceLocation imageLocation) {
        //sub area is just different width and height
        double imageU = 0.0 + (drawnU);
        double imageV = 0.0 + (drawnV);
        Minecraft.getMinecraft().renderEngine.bindTexture(imageLocation);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(x, y + height, 0.0D).tex(imageU, imageV + drawnHeight).endVertex();
        bufferbuilder.pos(x + width, y + height, 0.0D).tex(imageU + drawnWidth, imageV + drawnHeight).endVertex();
        bufferbuilder.pos(x + width, y, 0.0D).tex(imageU + drawnWidth, imageV).endVertex();
        bufferbuilder.pos(x, y, 0.0D).tex(imageU, imageV).endVertex();
        tessellator.draw();
    }

    @SideOnly(Side.CLIENT)
    public static void drawItemStack(int x, int y, ItemStack itemStack, long amount) {
        drawItemStack(x, y, itemStack, amount, null);
    }

    @SideOnly(Side.CLIENT)
    public static void drawItemStack(int x, int y, ItemStack itemStack, long amount, @Nullable String altTxt) {
        itemStack.setCount(1);
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, 0.0F, 32.0F);
        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableLighting();
        RenderHelper.enableGUIStandardItemLighting();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0f, 240.0f);
        Minecraft mc = Minecraft.getMinecraft();
        RenderItem itemRender = mc.getRenderItem();
        itemRender.renderItemAndEffectIntoGUI(itemStack, x, y);
        itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, x, y, altTxt);
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableLighting();
        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.scale(0.5, 0.5, 1);
        if (amount > 1) {
            final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            final String s = TextFormattingUtil.formatLongToCompactString(amount, 4);
            fontRenderer.drawStringWithShadow(s, (x + 6) * 2 - fontRenderer.getStringWidth(s) + 21, (y + 12) * 2, 0xFFFFFF);
        }
        GlStateManager.popMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
    }

    @SideOnly(Side.CLIENT)
    public static void drawFluidStack(int x, int y, FluidStack fluidStack, long amount) {
        final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        GlStateManager.disableBlend();
        TJGuiUtils.drawFluidForGui(fluidStack, amount, amount, x + 1, y + 1, 18 - 1, 18 - 2);
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5, 0.5, 1);
        final String s = TextFormattingUtil.formatLongToCompactString(amount, 4) + "L";
        fontRenderer.drawStringWithShadow(s, (x + 6) * 2 - fontRenderer.getStringWidth(s) + 21, (y + 12) * 2, 0xFFFFFF);
        GlStateManager.popMatrix();
        GlStateManager.enableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f);
    }

    @SideOnly(Side.CLIENT)
    public static Dimension getBarSize(String name,
                                       String progressStr,
                                       String maxProgressStr,
                                       String progressSuffixStr,
                                       String maxProgressSuffixStr,
                                       String decimalFormat) {
        final int text = Minecraft.getMinecraft().fontRenderer.getStringWidth(name + " ");
        final double progress = Double.parseDouble(progressStr);
        final double  maxProgress = Double.parseDouble(maxProgressStr);
        final DecimalFormat progressFormat = new DecimalFormat(decimalFormat);
        final String percentage = String.format("%s%%", TJValues.thousandFormat.format(progress / maxProgress * 100));
        final int progressWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(progressFormat.format(progress));
        final int slash = Minecraft.getMinecraft().fontRenderer.getStringWidth(" / ");
        final int maxProgressWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(progressFormat.format(maxProgress));
        final int colon = Minecraft.getMinecraft().fontRenderer.getStringWidth(" : ");
        final int percentageWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(percentage);
        final int progressSuffix = Minecraft.getMinecraft().fontRenderer.getStringWidth(progressSuffixStr);
        final int maxProgressSuffix = Minecraft.getMinecraft().fontRenderer.getStringWidth(maxProgressSuffixStr);
        return new Dimension(text + Math.max(50, progressWidth + progressSuffix + slash + maxProgressWidth + maxProgressSuffix + colon + percentageWidth),
                12);
    }

    @SideOnly(Side.CLIENT)
    public static void drawBar(int x,
                               int y,
                               String name,
                               String progressStr,
                               String maxProgressStr,
                               String progressSuffixStr,
                               String maxProgressSuffixStr,
                               String color,
                               String decimalFormat,
                               int nameColor) {
        final int offsetX = Minecraft.getMinecraft().fontRenderer.getStringWidth(name + " ");
        final double progress = Double.parseDouble(progressStr);
        final double maxProgress = Double.parseDouble(maxProgressStr);
        final DecimalFormat progressFormat = new DecimalFormat(decimalFormat);
        final String percentage = String.format("%s%%", TJValues.thousandFormat.format(progress / maxProgress * 100));
        final int progressWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(progressFormat.format(progress));
        final int slashWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(" / ");
        final int maxProgressWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(progressFormat.format(maxProgress));
        final int colonWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(" : ");
        final int percentageWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(percentage);
        final int progressSuffixWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(progressSuffixStr);
        final int maxProgressSuffixWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(maxProgressSuffixStr);
        final int totalLength = Math.max(50,
                progressWidth + progressSuffixWidth + slashWidth + maxProgressWidth + maxProgressSuffixWidth + colonWidth + percentageWidth + 6);
        final int barWidth = maxProgress == 0 ? 0 : (int) (totalLength * (progress / maxProgress));

        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        GuiTextures.DISPLAY.draw(x + offsetX, y, totalLength, 12);
        TJGuiTextures.getBarByColor(color).draw(x + offsetX + 1, y + 1, barWidth - 1, 10);
        GlStateManager.enableBlend();
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(name, x, y + 2, nameColor);
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(progressFormat.format(progress), x + offsetX + 3, y + 2, 0xFFFFFF);
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(progressSuffixStr, x + offsetX + 3 + progressWidth, y + 2, 0xFFFFFF);
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(" / ", x + offsetX + 3 + progressWidth + progressSuffixWidth, y + 2, 0xFFFFFF);
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(progressFormat.format(maxProgress),
                x + offsetX + 3 + progressWidth + progressSuffixWidth + slashWidth, y + 2, 0xFFFFFF);
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(maxProgressSuffixStr,
                x + offsetX + 3 + progressWidth + progressSuffixWidth + slashWidth + maxProgressWidth, y + 2, 0xFFFFFF);
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(" : ",
                x + offsetX + 3 + progressWidth + progressSuffixWidth + slashWidth + maxProgressWidth + maxProgressSuffixWidth, y + 2, 0xFFFFFF);
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(percentage,
                x + offsetX + 3 + progressWidth + progressSuffixWidth + slashWidth + maxProgressWidth + maxProgressSuffixWidth + colonWidth,
                y + 2, 0xFFFFFF);
    }
}
