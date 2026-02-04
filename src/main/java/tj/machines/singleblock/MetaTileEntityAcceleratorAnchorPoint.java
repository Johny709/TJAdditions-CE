package tj.machines.singleblock;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import tj.TJValues;
import tj.capability.LinkEvent;
import tj.gui.TJGuiTextures;
import tj.gui.widgets.TJLabelWidget;
import tj.machines.multi.electric.MetaTileEntityLargeWorldAccelerator;
import tj.textures.TJTextures;
import gregicadditions.GAUtility;
import gregicadditions.client.ClientHandler;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.gui.widgets.CycleButtonWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.render.SimpleSidedCubeRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class MetaTileEntityAcceleratorAnchorPoint extends MetaTileEntity implements LinkEvent {

    private MetaTileEntity metaTileEntity;
    private boolean isActive;
    private int tier;
    private boolean redStonePowered;
    private boolean inverted;

    public MetaTileEntityAcceleratorAnchorPoint(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityAcceleratorAnchorPoint(metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("tj.machine.accelerator_anchor_point.description"));
        tooltip.add(I18n.format("metaitem.item.linking.device.link.to"));
    }

    @Override
    public void update() {
        super.update();
        if (getWorld().isRemote || metaTileEntity == null)
            return;
        if (getOffsetTimer() % 5 == 0 && metaTileEntity instanceof MetaTileEntityLargeWorldAccelerator) {
            MetaTileEntityLargeWorldAccelerator accelerator = (MetaTileEntityLargeWorldAccelerator) metaTileEntity;
            setActive(accelerator.isActive());
            if (isActive)
                this.redStonePowered = Arrays.stream(EnumFacing.values()).anyMatch(enumFacing -> getInputRedstoneSignal(enumFacing, true) > 0);
        }
    }

    @Override
    protected boolean canMachineConnectRedstone(EnumFacing side) {
        return true;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        getBaseRenderer().render(renderState, translation, pipeline);

        int oldBaseColor = renderState.baseColour;
        int oldAlphaOverride = renderState.alphaOverride;
        renderState.baseColour = TJValues.VC[tier] << 8;
        renderState.alphaOverride = 0xFF;
        TJTextures.FIELD_GENERATOR_CORE.render(renderState, translation, pipeline);

        renderState.baseColour = oldBaseColor;
        renderState.alphaOverride = oldAlphaOverride;

        for (EnumFacing facing : EnumFacing.VALUES) {
            TJTextures.FIELD_GENERATOR_SPIN.renderSided(facing, renderState, translation, pipeline);
        }
    }

    @SideOnly(Side.CLIENT)
    protected SimpleSidedCubeRenderer getBaseRenderer() {
        return ClientHandler.VOLTAGE_CASINGS[tier];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(getBaseRenderer().getParticleSprite(), getPaintingColor());
    }

    @Override
    protected ModularUI createUI(EntityPlayer player) {
        WidgetGroup widgetGroup = new WidgetGroup();
        widgetGroup.addWidget(new ImageWidget(10, 20, 150, 40, GuiTextures.DISPLAY));
        widgetGroup.addWidget(new AdvancedTextWidget(15, 25, this::addDisplayText, 0xFFFFFF));
        widgetGroup.addWidget(new CycleButtonWidget(10, 60, 150, 20, this::isInverted, this::setInverted,
                "cover.machine_controller.normal", "cover.machine_controller.inverted"));
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 176, 187)
                .widget(new TJLabelWidget(7, -18, 166, 18, TJGuiTextures.MACHINE_LABEL)
                        .setItemLabel(this.getStackForm()).setLocale(this.getMetaFullName()))
                .widget(widgetGroup)
                .bindPlayerInventory(player.inventory, GuiTextures.SLOT, 7, 105)
                .build(getHolder(), player);
    }

    private void addDisplayText(List<ITextComponent> textList) {
        textList.add(metaTileEntity != null ? new TextComponentTranslation(metaTileEntity.getMetaFullName())
                .appendText("\n")
                .appendSibling(new TextComponentTranslation("machine.universal.linked.entity.radius", tier, tier))
                .appendText("\n")
                .appendSibling(new TextComponentString("X: ").appendSibling(new TextComponentString("" + metaTileEntity.getPos().getX()).setStyle(new Style().setColor(TextFormatting.YELLOW))).setStyle(new Style().setBold(true)))
                .appendSibling(new TextComponentString(" Y: ").appendSibling(new TextComponentString("" + metaTileEntity.getPos().getY()).setStyle(new Style().setColor(TextFormatting.YELLOW))).setStyle(new Style().setBold(true)))
                .appendSibling(new TextComponentString(" Z: ").appendSibling(new TextComponentString("" + metaTileEntity.getPos().getZ()).setStyle(new Style().setColor(TextFormatting.YELLOW))).setStyle(new Style().setBold(true)))
                : new TextComponentTranslation("machine.universal.linked.entity.null"));
    }

    public boolean isRedStonePowered() {
        return !isInverted() == redStonePowered;
    }

    public boolean isInverted() {
        return inverted;
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == 1) {
            this.isActive = buf.readBoolean();
        }
        if (dataId == 2) {
            this.tier = buf.readInt();
        }
        scheduleRenderUpdate();
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isActive);
        buf.writeInt(tier);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.isActive = buf.readBoolean();
        this.tier = buf.readInt();
    }

    protected void setActive(boolean active) {
        this.isActive = active;
        if (!getWorld().isRemote) {
            writeCustomData(1, buf -> buf.writeBoolean(active));
            markDirty();
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("Tier", tier);
        data.setBoolean("IsActive", isActive);
        data.setBoolean("Inverted", inverted);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.tier = data.getInteger("Tier");
        this.isActive = data.getBoolean("IsActive");
        this.inverted = data.getBoolean("Inverted");
    }

    @Override
    public void onLink(MetaTileEntity tileEntity) {
        if (tileEntity instanceof MetaTileEntityLargeWorldAccelerator) {
            MetaTileEntityLargeWorldAccelerator accelerator = (MetaTileEntityLargeWorldAccelerator) tileEntity;
            this.tier = GAUtility.getTierByVoltage(accelerator.getVoltageTier());
            this.metaTileEntity = tileEntity;
            if (!getWorld().isRemote) {
                writeCustomData(2, buf -> buf.writeInt(tier));
                markDirty();
            }
        } else {
            this.tier = 0;
            writeCustomData(2, buf -> buf.writeInt(tier));
            markDirty();
        }
    }
}
