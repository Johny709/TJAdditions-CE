package tj.integration.ae2.part;

import appeng.api.config.Upgrades;
import appeng.core.Api;
import appeng.parts.misc.PartStorageBus;
import appeng.parts.reporting.PartInterfaceTerminal;
import appeng.tile.networking.TileCableBus;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.tab.VerticalTabListRenderer;
import gregtech.api.util.TextFormattingUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import tj.TJValues;
import tj.builder.WidgetTabBuilder;
import tj.items.item.TJItems;
import tj.mui.TJGuiTextures;
import tj.mui.TJGuiUtils;
import tj.mui.uifactory.ITileEntityUI;
import tj.mui.uifactory.TileEntityHolder;
import tj.mui.widgets.impl.AEGhostItemListWidget;
import tj.mui.widgets.impl.AEItemListWidget;
import tj.mui.widgets.impl.NewTextFieldWidget;
import tj.mui.widgets.impl.TJLabelWidget;
import tj.util.TJItemUtils;
import tj.util.references.ObjectReference;

import java.util.List;
import java.util.regex.Pattern;

import static gregtech.api.gui.widgets.tab.VerticalTabListRenderer.HorizontalLocation.LEFT;
import static gregtech.api.gui.widgets.tab.VerticalTabListRenderer.VerticalStartCorner.TOP;

public class PartStorageBusTerminal extends PartInterfaceTerminal implements ITileEntityUI {

    private final BlockPos.MutableBlockPos storageBusPos = new BlockPos.MutableBlockPos();

    public PartStorageBusTerminal(ItemStack is) {
        super(is);
    }

    @Override
    public boolean onPartActivate(EntityPlayer player, EnumHand hand, Vec3d pos) {
        final TileCableBus tileCableBus = (TileCableBus) this.getTile();
        if (tileCableBus != null && !player.getEntityWorld().isRemote) {
            TileEntityHolder holder = new TileEntityHolder(tileCableBus);
            holder.setFacing(this.getSide().getFacing());
            holder.openUI((EntityPlayerMP) player);
        }
        return true;
    }

    @Override
    public ModularUI createUI(TileEntityHolder holder, EntityPlayer player) {
        return ModularUI.builder(TJGuiTextures.SUPER_INTERFACE, 176, 292)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(TJItems.PART_STORAGE_BUS_TERMINAL.maybeStack(1).orElse(ItemStack.EMPTY))
                        .setLocale("item.me.part.storage_bus_terminal.name"))
                .widget(new TJLabelWidget(4, 0, 162, 18, null)
                        .setDynamicLocale(this::getCustomInventoryName)
                        .setCentered(false)
                        .setCanSlide(false))
                .widget(new ImageWidget(-22, 0, 4, 55, GuiTextures.BORDERED_BACKGROUND)) // to move JEI GUI out of the way for tabs
                .widget(new WidgetTabBuilder()
                        .setTabListRenderer(() -> new VerticalTabListRenderer(TOP, LEFT))
                        .addTab("tj.multiblock.tab.config", Api.INSTANCE.definitions().items().certusQuartzWrench().maybeStack(1).orElse(ItemStack.EMPTY), this::createConfigTab)
                        .addTab("tj.multiblock.tab.storage", TJItemUtils.getItemStackFromName("minecraft:chest"), this::createStorageTab)
                        .build())
                .bindPlayerInventory(player.inventory, 209)
                .build(holder, player);
    }

    private void createConfigTab(List<Widget> tab) {
        final ObjectReference<String> searchName = new ObjectReference<>("");
        tab.add(new ImageWidget(6, 33, 164, 164, TJGuiTextures.BLANK_SLOT) {
            @Override
            @SideOnly(Side.CLIENT)
            public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
                GlStateManager.popMatrix();
                GlStateManager.enableBlend();
                GlStateManager.color(1.0f, 1.0f, 1.0f);
                super.drawInBackground(mouseX, mouseY, context);
            }
        });
        tab.add(new NewTextFieldWidget<>(7, 16, 90, 12, true, searchName::getValue, (s, id) -> searchName.setValue(s))
                .setValidator(str -> Pattern.compile(".*").matcher(str).matches())
                .setTooltipText("gui.tooltips.appliedenergistics2.SearchFieldInputs")
                .setUpdateOnTyping(true));
        tab.add(new AEGhostItemListWidget<PartStorageBus>(7, 34, 162, 162, this.getGridNode(), PartStorageBus.class)
                .setPredicate(storageBus -> searchName.getValue().isEmpty() || TJItemUtils.isItemPresent(storageBus.getInventoryByName("config"), searchName.getValue()))
                .setSlotPredicate((slot, storageBus) -> slot / 9 <= (storageBus.getInstalledUpgrades(Upgrades.CAPACITY) + 1))
                .setInventorySupplier(storageBus -> storageBus.getInventoryByName("config"))
                .setScrollSlider(1, 1, 10, 24, GuiTextures.BORDERED_BACKGROUND)
                .setItemStackTransfer((itemStack, aBoolean) -> itemStack)
                .setScrollbar(10, 0, 12, 162, GuiTextures.SLOT)
                .setRenderCallback(this::renderCallback));
    }

    private void createStorageTab(List<Widget> tab) {
        final ObjectReference<String> searchName = new ObjectReference<>("");
        tab.add(new NewTextFieldWidget<>(7, 16, 90, 12, true, searchName::getValue, (s, id) -> searchName.setValue(s))
                .setValidator(str -> Pattern.compile(".*").matcher(str).matches())
                .setTooltipText("gui.tooltips.appliedenergistics2.SearchFieldInputs")
                .setUpdateOnTyping(true));
        tab.add(new ImageWidget(6, 33, 164, 164, TJGuiTextures.BLANK_SLOT) {
            @Override
            @SideOnly(Side.CLIENT)
            public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
                GlStateManager.popMatrix();
                GlStateManager.enableBlend();
                GlStateManager.color(1.0f, 1.0f, 1.0f);
                super.drawInBackground(mouseX, mouseY, context);
            }
        });
        tab.add(new AEItemListWidget<PartStorageBus>(7, 34, 162, 162, this.getGridNode(), PartStorageBus.class)
                .setPredicate(storageBus -> searchName.getValue().isEmpty() || TJItemUtils.isItemPresent(this.getInventory(storageBus), searchName.getValue()))
                .setScrollSlider(1, 1, 10, 24, GuiTextures.BORDERED_BACKGROUND)
                .setItemStackTransfer((itemStack, aBoolean) -> itemStack)
                .setScrollbar(10, 0, 12, 162, GuiTextures.SLOT)
                .setSlotPredicate((s, storageBus) -> true)
                .setInventorySupplier(this::getInventory)
                .setRenderCallback(this::renderCallback));
    }

    private IItemHandler getInventory(PartStorageBus storageBus) {
        final BlockPos pos = storageBus.getTile().getPos();
        this.storageBusPos.setPos(pos.getX(), pos.getY(), pos.getZ());
        final TileEntity tileEntity = storageBus.getTile().getWorld().getTileEntity(this.storageBusPos.move(storageBus.getSide().getFacing()));
        if (tileEntity != null) {
            final IItemHandler itemHandler = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, storageBus.getSide().getFacing().getOpposite());
            if (itemHandler != null)
                return itemHandler;
        }
        return TJValues.DUMMY_ITEM_HANDLER;
    }

    private void renderCallback(ItemStack itemStack, int x, int y) {
        final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        final NBTTagCompound compound = TJItemUtils.getCompoundFromStack(itemStack);
        final NBTTagCompound outputCompound = compound.getCompoundTag("#0");
        final String id = outputCompound.getString("id");
        ItemStack output;
        FluidStack fluidOutput = null;
        long count;
        if (id.isEmpty()) {
            output = ItemStack.EMPTY;
            if (outputCompound.hasKey("FluidName"))
                fluidOutput = new FluidStack(FluidRegistry.getFluid(outputCompound.getString("FluidName")), 1);
        } else output = TJItemUtils.getItemStackFromName(id, 1, outputCompound.getShort("Damage"));
        count = outputCompound.getLong("Cnt");
        if (count < 1)
            count = outputCompound.getInteger("Count");
        if (output.isEmpty() && fluidOutput == null) {
            final NBTTagCompound partition = compound.getCompoundTag("list").getTagList("Items", 10).getCompoundTagAt(0);
            final String partitionId = partition.getString("id");
            if (partitionId.equals("appliedenergistics2:dummy_fluid_item")) {
                fluidOutput = new FluidStack(FluidRegistry.getFluid(partition.getCompoundTag("tag").getString("FluidName")), 1);
                count = -1;
            } else {
                output = TJItemUtils.getItemStackFromName(partitionId, 1, partition.getShort("Damage"));
                if (!output.isEmpty())
                    count = -1;
            }
        }
        GlStateManager.disableBlend();
        if (!output.isEmpty()) {
            Widget.drawItemStack(output, x + 1, y + 1, null);
        } else if (fluidOutput != null) {
            TJGuiUtils.drawFluidForGui(fluidOutput, Math.max(1, count), Math.max(1, count), x + 1, y + 1, 17, 17);
        } else Widget.drawItemStack(itemStack, x + 1, y + 1, null);
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5, 0.5, 1);
        final String s = count < 0 ? "§eP" : TextFormattingUtil.formatLongToCompactString(count, 4) + (fluidOutput != null ? "L" : "");
        fontRenderer.drawStringWithShadow(s, (x + 6) * 2 - fontRenderer.getStringWidth(s) + 21, (y + 12) * 2, 0xFFFFFF);
        GlStateManager.popMatrix();
        GlStateManager.enableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f);
    }
}
