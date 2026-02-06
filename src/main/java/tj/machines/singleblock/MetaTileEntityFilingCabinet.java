package tj.machines.singleblock;

import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.IFastRenderMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.MetaTileEntityUIFactory;
import gregtech.api.render.Textures;
import gregtech.api.util.GTUtility;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import tj.gui.TJGuiTextures;
import tj.gui.widgets.impl.SlotScrollableWidgetGroup;
import tj.gui.widgets.TJLabelWidget;
import tj.gui.widgets.TJSlotWidget;
import tj.items.handlers.CabinetItemStackHandler;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MetaTileEntityFilingCabinet extends MetaTileEntity implements IFastRenderMetaTileEntity {

    private static final IndexedCuboid6 COLLISION_BOX = new IndexedCuboid6(null, new Cuboid6(3 / 16.0, 0 / 16.0, 3 / 16.0, 13 / 16.0, 14 / 16.0, 13 / 16.0));
    private static final int SLOT_LIMIT = 8192;
    private final Map<EntityPlayer, SlotScrollableWidgetGroup> guiUsers = new ConcurrentHashMap<>();
    private boolean resetUIs;
    private float doorAngle = 0.0f;
    private float prevDoorAngle = 0.0f;

    public MetaTileEntityFilingCabinet(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        this.initializeInventory();
        this.itemInventory = this.importItems;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityFilingCabinet(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(net.minecraft.client.resources.I18n.format("tj.machine.compressed_chest.description"));
        tooltip.add(net.minecraft.client.resources.I18n.format("tj.machine.filing_cabinet.description", 27));
        tooltip.add(net.minecraft.client.resources.I18n.format("machine.universal.stack", 64));
        tooltip.add(net.minecraft.client.resources.I18n.format("machine.universal.slots", SLOT_LIMIT));
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new CabinetItemStackHandler(27, 64)
                .setSizeChangeListener(this::resizeInventory);
    }

    @Override
    public void update() {
        super.update();
        if (!this.getWorld().isRemote && this.resetUIs) {
            this.resetUIs = false;
            this.guiUsers.forEach((player, scrollableWidgetGroup) -> MetaTileEntityUIFactory.INSTANCE.openUI(this.getHolder(), (EntityPlayerMP) player));
        }
    }

    @Override
    public void clearMachineInventory(NonNullList<ItemStack> itemBuffer) {}

    @Override
    public void writeItemStackData(NBTTagCompound itemStack) {
        super.writeItemStackData(itemStack);
        itemStack.setTag("Inventory", ((ItemStackHandler) this.importItems).serializeNBT());
    }

    @Override
    public void initFromItemStackData(NBTTagCompound itemStack) {
        super.initFromItemStackData(itemStack);
        ((ItemStackHandler) this.importItems).deserializeNBT(itemStack.getCompoundTag("Inventory"));
    }

    private void resizeInventory(int size) {
        if (size > SLOT_LIMIT)
            return;
        NonNullList<ItemStack> transferStackList = NonNullList.create();
        String name = ((CabinetItemStackHandler) this.importItems).getAllowedItemName();
        boolean locked = ((CabinetItemStackHandler) this.importItems).isItemUnlocked();
        for (int i = 0; i < this.importItems.getSlots(); i++) {
            transferStackList.add(this.importItems.getStackInSlot(i));
        }
        this.itemInventory = this.importItems = new CabinetItemStackHandler(size, 64)
                .setSizeChangeListener(this::resizeInventory)
                .setAllowedItemByName(name)
                .setItemUnlocked(locked);
        int minSize = Math.min(size, transferStackList.size());
        for (int i = 0; i < minSize; i++) {
            this.importItems.setStackInSlot(i, transferStackList.get(i));
        }
        if (!this.getWorld().isRemote) {
            this.writeCustomData(10, buf -> buf.writeInt(size));
            this.markDirty();
            this.resetUIs = true;
        }
    }

    @Override
    protected ModularUI createUI(EntityPlayer player) {
        SlotScrollableWidgetGroup slotScrollableWidgetGroup = this.guiUsers.getOrDefault(player, new SlotScrollableWidgetGroup(7, 35, 180, 72, 9))
                .setItemHandler(this.importItems);
        slotScrollableWidgetGroup.clearWidgets();
        for (int i = 0; i < this.importItems.getSlots(); i++) {
            slotScrollableWidgetGroup.addWidget(new TJSlotWidget<>(this.importItems, i, 18 * (i % 9), 18 * (i / 9))
                    .setWidgetGroup(slotScrollableWidgetGroup)
                    .setBackgroundTexture(GuiTextures.SLOT));
        }
        return ModularUI.builder(GuiTextures.BACKGROUND, 176, 197)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL)
                        .setItemLabel(this.getStackForm()).setLocale(this.getMetaFullName()))
                .bindOpenListener(() -> this.guiUsers.put(player, slotScrollableWidgetGroup))
                .bindCloseListener(() -> this.guiUsers.remove(player))
                .widget(new ImageWidget(7, 15, 126, 18, GuiTextures.DISPLAY))
                .widget(new AdvancedTextWidget(10, 20, this::addDisplayText, 0xFFFFFF))
                .widget(new ToggleButtonWidget(133, 15, 18, 18, TJGuiTextures.CLEAR_GRID_BUTTON, () -> false, this::onClear)
                        .setTooltipText("machine.universal.toggle.clear"))
                .widget(new ToggleButtonWidget(151, 15, 18, 18, GuiTextures.BUTTON_BLACKLIST, () -> ((CabinetItemStackHandler) this.importItems).isItemUnlocked(), this::setLocked)
                        .setTooltipText("tj.machine.filing_cabinet.toggle"))
                .widget(new SortingButtonWidget(109, 4, 60, 10, "gregtech.gui.sort", (info) -> MetaTileEntityCompressedChest.sortInventorySlotContents(this.importItems)))
                .widget(slotScrollableWidgetGroup)
                .bindPlayerInventory(player.inventory, 115)
                .build(this.getHolder(), player);
    }

    private void addDisplayText(List<ITextComponent> textList) {
        String itemName = ((CabinetItemStackHandler) this.importItems).getAllowedItemName();
        itemName = itemName != null ? itemName : I18n.translateToLocal("gregtech.fluid.empty");
        textList.add(new TextComponentString(itemName)
                .setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(itemName)))));
    }

    private void setLocked(boolean locked) {
        ((CabinetItemStackHandler) this.importItems).setItemUnlocked(locked);
        this.writeCustomData(11, buf -> buf.writeBoolean(locked));
        this.markDirty();
    }

    private void onClear(boolean onClear) {
        ((CabinetItemStackHandler) this.importItems).clear(onClear);
        this.writeCustomData(12, buf -> buf.writeBoolean(onClear));
        this.markDirty();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {}

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntityFast(CCRenderState renderState, Matrix4 translation, float partialTicks) {
        ColourMultiplier colourMultiplier = new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(GTValues.VC[1]));
        float angle = this.prevDoorAngle + (this.doorAngle - this.prevDoorAngle) * partialTicks;
        angle = 1.0f - (1.0f - angle) * (1.0f - angle) * (1.0f - angle);
        float resultDoorAngle = angle * 120.0f;
        Textures.SAFE.render(renderState, translation, new IVertexOperation[] { colourMultiplier }, getFrontFacing(), resultDoorAngle);
    }

    @Override
    public void addCollisionBoundingBox(List<IndexedCuboid6> collisionList) {
        collisionList.add(COLLISION_BOX);
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public int getLightOpacity() {
        return 1;
    }

    @Override
    protected boolean shouldSerializeInventories() {
        return false;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(this.getPos().add(-1, 0, -1), this.getPos().add(2, 1, 2));
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeCompoundTag(((ItemStackHandler) this.importItems).serializeNBT());
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        try {
            ((ItemStackHandler) this.importItems).deserializeNBT(buf.readCompoundTag());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == 10)
            this.resizeInventory(buf.readInt());
        else if (dataId == 11)
            ((CabinetItemStackHandler) this.importItems).setItemUnlocked(buf.readBoolean());
        else if (dataId == 12)
            ((CabinetItemStackHandler) this.importItems).clear(buf.readBoolean());

    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("Inventory", ((ItemStackHandler) this.importItems).serializeNBT());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.hasKey("Inventory"))
            ((ItemStackHandler) this.importItems).deserializeNBT(data.getCompoundTag("Inventory"));
    }
}
