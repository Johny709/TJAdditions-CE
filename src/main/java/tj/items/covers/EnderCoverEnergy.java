package tj.items.covers;

import gregicadditions.GAValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.common.covers.CoverPump;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.HoverEvent;
import tj.capability.impl.workable.BasicEnergyHandler;
import tj.textures.TJSimpleOverlayRenderer;
import tj.util.EnderWorldData;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static gregtech.api.capability.GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER;
import static gregtech.api.gui.widgets.ProgressWidget.MoveType.VERTICAL;
import static tj.gui.TJGuiTextures.BAR_HEAT;
import static tj.gui.TJGuiTextures.BAR_STEEL;
import static tj.textures.TJTextures.PORTAL_OVERLAY;

public class EnderCoverEnergy extends AbstractEnderCover<BasicEnergyHandler> {

    private final IEnergyContainer energyContainer;
    private final long capacity;
    private final int tier;

    public EnderCoverEnergy(ICoverable coverHolder, EnumFacing attachedSide, int tier) {
        super(coverHolder, attachedSide);
        this.tier = tier;
        this.energyContainer = this.coverHolder.getCapability(CAPABILITY_ENERGY_CONTAINER, null);
        this.capacity = (long) (Math.pow(4, tier) * 1000);
        this.maxTransferRate = (int) Math.min(Math.pow(4, tier) * 16, Integer.MAX_VALUE);
        this.transferRate = this.maxTransferRate;
    }

    @Override
    public boolean canAttach() {
        return this.coverHolder.getCapability(CAPABILITY_ENERGY_CONTAINER, this.attachedSide) != null;
    }

    @Override
    protected TJSimpleOverlayRenderer getOverlay() {
        return PORTAL_OVERLAY;
    }

    @Override
    protected int getPortalColor() {
        return 0x9fff2c;
    }

    @Override
    public int getTier() {
        return this.tier;
    }

    @Override
    protected Map<String, EnderCoverProfile<BasicEnergyHandler>> getPlayerMap() {
        return EnderWorldData.getINSTANCE().getEnergyContainerPlayerMap();
    }

    @Override
    protected String getName() {
        return "metaitem.ender_energy_cover_" + GAValues.VN[this.tier].toLowerCase() + ".name";
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
    public void update() {
        if (this.isWorkingEnabled && this.handler != null) {
            if (this.pumpMode == CoverPump.PumpMode.IMPORT) {
                this.importEnergy(this.handler);
            } else {
                this.exportEnergy(this.handler);
            }
        }
    }

    private void importEnergy(BasicEnergyHandler enderEnergyContainer) {
        long energyRemainingToFill = enderEnergyContainer.getEnergyCapacity() - enderEnergyContainer.getEnergyStored();
        if (enderEnergyContainer.getEnergyStored() < 1 || energyRemainingToFill != 0) {
            long energyExtracted = this.energyContainer.removeEnergy(Math.min(energyRemainingToFill, this.transferRate));
            enderEnergyContainer.addEnergy(Math.abs(energyExtracted));
        }
    }

    private void exportEnergy(BasicEnergyHandler enderEnergyContainer) {
        long energyRemainingToFill = this.energyContainer.getEnergyCapacity() - this.energyContainer.getEnergyStored();
        if (this.energyContainer.getEnergyStored() < 1 || energyRemainingToFill != 0) {
            long energyExtracted = Math.abs(enderEnergyContainer.removeEnergy(Math.min(energyRemainingToFill, this.transferRate)));
            if (energyExtracted > this.energyContainer.getInputVoltage() * 2)
                this.energyContainer.acceptEnergyFromNetwork(this.attachedSide, energyExtracted, 1);
            else this.energyContainer.addEnergy(energyExtracted);
        }
    }
}
