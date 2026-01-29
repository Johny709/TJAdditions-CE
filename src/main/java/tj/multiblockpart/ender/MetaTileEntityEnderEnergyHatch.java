package tj.multiblockpart.ender;

import gregicadditions.GAValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.TJValues;
import tj.capability.impl.workable.BasicEnergyHandler;
import tj.capability.impl.workable.EnderEnergyHandler;
import tj.items.covers.EnderCoverProfile;
import tj.textures.TJSimpleOverlayRenderer;
import tj.textures.TJTextures;
import tj.util.EnderWorldData;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static gregtech.api.gui.widgets.ProgressWidget.MoveType.VERTICAL;
import static tj.gui.TJGuiTextures.BAR_HEAT;
import static tj.gui.TJGuiTextures.BAR_STEEL;

public class MetaTileEntityEnderEnergyHatch extends AbstractEnderHatch<IEnergyContainer, BasicEnergyHandler> {

    private final EnderEnergyHandler enderEnergyHandler = new EnderEnergyHandler(TJValues.DUMMY_ENERGY);
    private final long capacity;
    private final boolean isOutput;

    public MetaTileEntityEnderEnergyHatch(ResourceLocation metaTileEntityId, int tier, boolean isOutput) {
        super(metaTileEntityId, tier);
        this.capacity = (long) (Math.pow(4, tier) * 1000);
        this.isOutput = isOutput;
        this.maxTransferRate = (int) Math.min(Math.pow(4, tier) * 16, Integer.MAX_VALUE);
        this.transferRate = this.maxTransferRate;
        this.enderEnergyHandler.setInputAmps(this.isOutput ? 0 : 2)
                .setOutputVoltage(this.isOutput ? (long) (Math.pow(4, tier) * 8) : 0)
                .setInputVoltage(this.isOutput ? 0 : (long) (Math.pow(4, tier) * 8))
                .setOutputAmps(this.isOutput ? 4 : 0);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityEnderEnergyHatch(this.metaTileEntityId, this.getTier(), this.isOutput);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        String tierName = GAValues.VN[this.getTier()];
        if (this.isOutput) {
            tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_out", this.enderEnergyHandler.getOutputVoltage(), TJValues.VCC[this.getTier()], tierName));
            tooltip.add(I18n.format("gregtech.universal.tooltip.amperage_out_till", this.enderEnergyHandler.getOutputAmperage()));
        } else {
            tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_in", this.enderEnergyHandler.getInputVoltage(), TJValues.VCC[this.getTier()], tierName));
            tooltip.add(I18n.format("gregtech.universal.tooltip.amperage_in_till", this.enderEnergyHandler.getInputAmperage()));
        }
        tooltip.add(I18n.format("gregtech.universal.enabled"));
    }

    @Override
    protected TJSimpleOverlayRenderer getOverlay() {
        return TJTextures.PORTAL_OVERLAY;
    }

    @Override
    protected Map<String, EnderCoverProfile<BasicEnergyHandler>> getPlayerMap() {
        return EnderWorldData.getINSTANCE().getEnergyContainerPlayerMap();
    }

    @Override
    protected int getPortalColor() {
        return 0x9fff2c;
    }

    @Override
    protected void addWidgets(Consumer<Widget> widget) {
        widget.accept(new ProgressWidget(this::getEnergyStored, 7, 38, 18, 18) {
            private long energyStored;
            private long energyCapacity;

            @Override
            public void drawInForeground(int mouseX, int mouseY) {
                if(isMouseOverElement(mouseX, mouseY)) {
                    List<String> hoverList = Collections.singletonList(I18n.format("machine.universal.energy.stored", this.energyStored, this.energyCapacity));
                    this.drawHoveringText(ItemStack.EMPTY, hoverList, 300, mouseX, mouseY);
                }
            }

            @Override
            public void detectAndSendChanges() {
                super.detectAndSendChanges();
                if (handler != null) {
                    long energyStored = handler.getEnergyStored();
                    long energyCapacity = handler.getEnergyCapacity();
                    this.writeUpdateInfo(1, buffer -> buffer.writeLong(energyStored));
                    this.writeUpdateInfo(2, buffer -> buffer.writeLong(energyCapacity));
                }
            }

            @Override
            public void readUpdateInfo(int id, PacketBuffer buffer) {
                super.readUpdateInfo(id, buffer);
                if (id == 1) {
                    this.energyStored = buffer.readLong();
                } else if (id == 2) {
                    this.energyCapacity = buffer.readLong();
                }
            }
        }.setProgressBar(BAR_STEEL, BAR_HEAT, VERTICAL));
    }

    private double getEnergyStored() {
        return this.handler != null ? (double) this.handler.getEnergyStored() / this.handler.getEnergyCapacity() : 0;
    }

    @Override
    protected BasicEnergyHandler createHandler() {
        return new BasicEnergyHandler(this.capacity);
    }

    @Override
    protected void addChannelText(ITextComponent keyEntry, String key, BasicEnergyHandler value) {
        keyEntry.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new TextComponentString(net.minecraft.util.text.translation.I18n.translateToLocalFormatted("machine.universal.energy.stored", value.getEnergyStored(), value.getEnergyCapacity()))));
    }

    @Override
    public MultiblockAbility<IEnergyContainer> getAbility() {
        return this.isOutput ? MultiblockAbility.OUTPUT_ENERGY : MultiblockAbility.INPUT_ENERGY;
    }

    @Override
    public void registerAbilities(List<IEnergyContainer> list) {
        list.add(this.enderEnergyHandler);
    }

    @Override
    public void setHandler(BasicEnergyHandler handler) {
        super.setHandler(handler);
        this.enderEnergyHandler.setBasicEnergyHandler(this.handler != null ? this.handler : TJValues.DUMMY_ENERGY);
    }

    @Override
    public void setChannel(String lastEntry) {
        super.setChannel(lastEntry);
        this.enderEnergyHandler.setBasicEnergyHandler(this.handler != null ? this.handler : TJValues.DUMMY_ENERGY);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.enderEnergyHandler.setBasicEnergyHandler(this.handler != null ? this.handler : TJValues.DUMMY_ENERGY);
    }
}
