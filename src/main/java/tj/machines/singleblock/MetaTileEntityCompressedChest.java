package tj.machines.singleblock;

import codechicken.lib.colour.ColourRGBA;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.SortingButtonWidget;
import gregtech.api.metatileentity.IFastRenderMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.recipes.ModHandler;
import gregtech.api.render.Textures;
import gregtech.api.unification.material.type.Material;
import gregtech.api.util.GTUtility;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.input.Keyboard;
import tj.gui.widgets.impl.SlotScrollableWidgetGroup;
import tj.items.handlers.LargeItemStackHandler;
import tj.util.Color;
import tj.util.TooltipHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static gregtech.api.unification.material.Materials.Obsidian;
import static gregtech.api.util.GTUtility.convertOpaqueRGBA_CLtoRGB;


public class MetaTileEntityCompressedChest extends MetaTileEntity implements IFastRenderMetaTileEntity {

    private static final IndexedCuboid6 CHEST_COLLISION = new IndexedCuboid6(null, new Cuboid6(1 / 16.0, 1 / 16.0, 1 / 16.0, 15 / 16.0, 14 / 16.0, 15 / 16.0));
    private static final int ROW_SIZE = 27;
    private static final int AMOUNT_OF_ROWS = 27;

    private final boolean isInfinite;
    private final Material material;
    private float lidAngle;
    private float prevLidAngle;
    private int numPlayersUsing;

    public MetaTileEntityCompressedChest(ResourceLocation metaTileEntityId, boolean isInfinite) {
        super(metaTileEntityId);
        this.isInfinite = isInfinite;
        this.material = this.isInfinite ? Material.MATERIAL_REGISTRY.getObject("chaosalloy") : Obsidian;
        this.initializeInventory();
        this.itemInventory = this.importItems;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityCompressedChest(this.metaTileEntityId, this.isInfinite);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("tj.machine.compressed_chest.description"));
        tooltip.add(I18n.format("machine.universal.stack", this.isInfinite ? Integer.MAX_VALUE : 64));
        tooltip.add(I18n.format("machine.universal.slots", ROW_SIZE * AMOUNT_OF_ROWS));
        TooltipHelper.shiftText(tooltip, tip -> {
            NBTTagCompound compound = stack.getTagCompound();
            if (compound == null || compound.isEmpty()) return;
            NBTTagList itemList = compound.getCompoundTag("Inventory").getTagList("Items", 10);
            int size = itemList.tagCount() / 10;
            TooltipHelper.pageText(tip, size, (tip1, tooltipHandler) -> {
                int start = tooltipHandler.getIndex() * 10;
                for (int i = start; i < Math.min(itemList.tagCount(), start + 10); i++) {
                    NBTTagCompound itemCompound = itemList.getCompoundTagAt(i);
                    ItemStack itemStack = new ItemStack(Item.getByNameOrId(itemCompound.getString("id")), itemCompound.getInteger("Count"), itemCompound.getShort("Damage"));
                    tip1.add(I18n.format("tj.machine.compressed_chest.slot", itemCompound.getInteger("Slot"), itemStack.getDisplayName(), itemStack.getCount()));
                }
            });
        });
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return this.isInfinite ? new LargeItemStackHandler(ROW_SIZE * AMOUNT_OF_ROWS, Integer.MAX_VALUE) : new ItemStackHandler(ROW_SIZE * AMOUNT_OF_ROWS);
    }

    @Override
    public void update() {
        super.update();
        BlockPos blockPos = this.getPos();
        this.prevLidAngle = this.lidAngle;

        if (!this.getWorld().isRemote && this.numPlayersUsing != 0 && this.getOffsetTimer() % 200 == 0) {
            int lastPlayersUsing = this.numPlayersUsing;
            this.numPlayersUsing = GTUtility.findPlayersUsing(this, 5.0).size();
            if (lastPlayersUsing != this.numPlayersUsing) {
                this.updateNumPlayersUsing();
            }
        }

        if (this.numPlayersUsing > 0 && this.lidAngle == 0.0F) {
            double soundX = blockPos.getX() + 0.5;
            double soundZ = blockPos.getZ() + 0.5;
            double soundY = blockPos.getY() + 0.5;
            this.getWorld().playSound(null, soundX, soundY, soundZ, SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, 0.5F, getWorld().rand.nextFloat() * 0.1F + 0.9F);
        }

        if ((this.numPlayersUsing == 0 && this.lidAngle > 0.0F) || (this.numPlayersUsing > 0 && this.lidAngle < 1.0F)) {
            float previousValue = this.lidAngle;

            if (this.numPlayersUsing > 0) {
                this.lidAngle = Math.min(this.lidAngle + 0.1F, 1.0F);
            } else {
                this.lidAngle = Math.max(this.lidAngle - 0.1F, 0.0F);
            }
            if (this.lidAngle < 0.5F && previousValue >= 0.5F) {
                double soundX = blockPos.getX() + 0.5;
                double soundZ = blockPos.getZ() + 0.5;
                double soundY = blockPos.getY() + 0.5;
                this.getWorld().playSound(null, soundX, soundY, soundZ, SoundEvents.BLOCK_CHEST_CLOSE, SoundCategory.BLOCKS, 0.5F, getWorld().rand.nextFloat() * 0.1F + 0.9F);
            }
        }
    }

    private void updateNumPlayersUsing() {
        this.writeCustomData(100, buffer -> buffer.writeVarInt(numPlayersUsing));
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

    @Override
    public int getLightOpacity() {
        return 0;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public String getHarvestTool() {
        return ModHandler.isMaterialWood(this.material) ? "axe" : "pickaxe";
    }

    @Override
    public double getCoverPlateThickness() {
        return 1.0 / 16.0; //1/16th of the block size
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        if (ModHandler.isMaterialWood(this.material)) {
            return Pair.of(Textures.WOODEN_CHEST.getParticleTexture(), getPaintingColor());
        } else {
            int color = ColourRGBA.multiply(
                    GTUtility.convertRGBtoOpaqueRGBA_CL(this.material.materialRGB),
                    GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColor())
            );
            color = convertOpaqueRGBA_CLtoRGB(color);
            return Pair.of(Textures.METAL_CHEST.getParticleTexture(), color);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {}

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntityFast(CCRenderState renderState, Matrix4 translation, float partialTicks) {
        float angle = this.prevLidAngle + (this.lidAngle - this.prevLidAngle) * partialTicks;
        angle = 1.0f - (1.0f - angle) * (1.0f - angle) * (1.0f - angle);
        float resultLidAngle = angle * 90.0f;
        if (ModHandler.isMaterialWood(this.material)) {
            ColourMultiplier multiplier = new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()));
            Textures.WOODEN_CHEST.render(renderState, translation, new IVertexOperation[]{multiplier}, this.getFrontFacing(), resultLidAngle);
        } else {
            ColourMultiplier multiplier = new ColourMultiplier(ColourRGBA.multiply(
                    GTUtility.convertRGBtoOpaqueRGBA_CL(this.material.materialRGB),
                    GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering())));
            Textures.METAL_CHEST.render(renderState, translation, new IVertexOperation[]{multiplier}, this.getFrontFacing(), resultLidAngle);
        }
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(this.getPos().add(-1, 0, -1), this.getPos().add(2, 2, 2));
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND,
                        Math.max(176, 14 + Math.min(27, ROW_SIZE) * 18),
                        18 + 18 * Math.min(12, AMOUNT_OF_ROWS) + 94)
                .label(5, 5, this.getMetaFullName());
        SlotScrollableWidgetGroup scrollableListWidget = new SlotScrollableWidgetGroup(7, 18, 18 + ROW_SIZE * 18,  18 * Math.min(7, AMOUNT_OF_ROWS) + 94, ROW_SIZE);
        builder.widget(new SortingButtonWidget(111, 4, 60, 10, "gregtech.gui.sort",
                (info) -> sortInventorySlotContents(this.importItems)));

        for (int i = 0; i < this.importItems.getSlots(); i++) {
            scrollableListWidget.addWidget(new SlotWidget(this.importItems, i, 18 * (i % ROW_SIZE), 18 * (i / ROW_SIZE), true, true)
                    .setBackgroundTexture(GuiTextures.SLOT));
        }
        int startX = (Math.max(176, 14 + ROW_SIZE * 18) - 162) / 2;
        builder.widget(scrollableListWidget);
        builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, startX, 18 + 18 * Math.min(12, AMOUNT_OF_ROWS) + 12);
        if (!this.getWorld().isRemote) {
            builder.bindOpenListener(() -> this.onContainerOpen(entityPlayer));
            builder.bindCloseListener(() -> this.onContainerClose(entityPlayer));
        }
        return builder.build(this.getHolder(), entityPlayer);
    }

    private void onContainerOpen(EntityPlayer player) {
        if (!player.isSpectator()) {
            if (this.numPlayersUsing < 0) {
                this.numPlayersUsing = 0;
            }
            ++this.numPlayersUsing;
            this.updateNumPlayersUsing();
        }
    }

    private void onContainerClose(EntityPlayer player) {
        if (!player.isSpectator()) {
            --this.numPlayersUsing;
            this.updateNumPlayersUsing();
        }
    }

    public static void sortInventorySlotContents(IItemHandlerModifiable inventory) {
        //stack item stacks with equal items and compounds
        for (int i = 0; i < inventory.getSlots(); i++) {
            for (int j = i + 1; j < inventory.getSlots(); j++) {
                ItemStack stack1 = inventory.getStackInSlot(i);
                ItemStack stack2 = inventory.getStackInSlot(j);
                if (!stack1.isEmpty() && ItemStack.areItemsEqual(stack1, stack2) &&
                        ItemStack.areItemStackTagsEqual(stack1, stack2)) {
                    int maxStackSize = Math.min(stack1.getMaxStackSize(), inventory.getSlotLimit(i));
                    int itemsCanAccept = Math.min(stack2.getCount(), maxStackSize - Math.min(stack1.getCount(), maxStackSize));
                    if (itemsCanAccept > 0) {
                        stack1.grow(itemsCanAccept);
                        stack2.shrink(itemsCanAccept);
                    }
                }
            }
        }
        //create itemstack pairs and sort them out by attributes
        ArrayList<ItemStack> inventoryContents = new ArrayList<>();
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack itemStack = inventory.getStackInSlot(i);
            if (!itemStack.isEmpty()) {
                inventory.setStackInSlot(i, ItemStack.EMPTY);
                inventoryContents.add(itemStack);
            }
        }
        inventoryContents.sort(GTUtility.createItemStackComparator());
        for (int i = 0; i < inventoryContents.size(); i++) {
            inventory.setStackInSlot(i, inventoryContents.get(i));
        }
    }

    @Override
    public int getActualComparatorValue() {
        return ItemHandlerHelper.calcRedstoneFromInventory(this.importItems);
    }

    @Override
    public void addCollisionBoundingBox(List<IndexedCuboid6> collisionList) {
        collisionList.add(CHEST_COLLISION);
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeVarInt(this.numPlayersUsing);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.numPlayersUsing = buf.readVarInt();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == 100) {
            this.numPlayersUsing = buf.readVarInt();
        }
    }
}
