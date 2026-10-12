package tj.multiblockpart.ender;

import gregicadditions.GAValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.TJValues;
import tj.capability.impl.workable.BasicEnergyHandler;
import tj.capability.impl.workable.EnderEnergyHandlerWrapper;
import tj.items.covers.EnderCoverProfile;
import tj.textures.TJSimpleOverlayRenderer;
import tj.textures.TJTextures;
import tj.util.Color;
import tj.util.EnderWorldData;
import tj.util.TooltipHelper;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static gregtech.api.gui.widgets.ProgressWidget.MoveType.VERTICAL;
import static tj.mui.TJGuiTextures.BAR_HEAT;
import static tj.mui.TJGuiTextures.BAR_STEEL;

public class MetaTileEntityEnderEnergyHatch extends AbstractEnderHatch<IEnergyContainer, BasicEnergyHandler> implements IEnergyContainer {

    private final EnderEnergyHandlerWrapper enderEnergyHandlerWrapper = new EnderEnergyHandlerWrapper(TJValues.DUMMY_ENERGY);
    private final long capacity;
    private final boolean isOutput;
    private final int amps;

    public MetaTileEntityEnderEnergyHatch(ResourceLocation metaTileEntityId, int tier, int amps, boolean isOutput) {
        super(metaTileEntityId, tier);
        this.capacity = (long) (Math.pow(4, tier) * 1000);
        this.amps = amps;
        this.isOutput = isOutput;
        this.maxTransferRate = (int) Math.min(Math.pow(4, tier) * 16, Integer.MAX_VALUE);
        this.transferRate = this.maxTransferRate;
        this.enderEnergyHandlerWrapper.setInputAmps(this.isOutput ? 0 : amps)
                .setOutputVoltage(this.isOutput ? (long) (Math.pow(4, tier) * 8) : 0)
                .setInputVoltage(this.isOutput ? 0 : (long) (Math.pow(4, tier) * 8))
                .setOutputAmps(this.isOutput ? amps : 0);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityEnderEnergyHatch(this.metaTileEntityId, this.getTier(), this.amps, this.isOutput);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        final String tierName = GAValues.VN[this.getTier()];
        if (this.isOutput) {
            tooltip.add(TooltipHelper.blinkingText(Color.YELLOW, 20, "tj.machine.ender_energy_hatch.description",
                    this.enderEnergyHandlerWrapper.getOutputVoltage() * this.enderEnergyHandlerWrapper.getOutputAmperage()));
            tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_out",
                    this.enderEnergyHandlerWrapper.getOutputVoltage(), TJValues.VCC[this.getTier()], tierName));
            tooltip.add(I18n.format("gregtech.universal.tooltip.amperage_out_till",
                    this.enderEnergyHandlerWrapper.getOutputAmperage(), TJValues.VCC[this.getTier()]));
        } else {
            tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_in",
                    this.enderEnergyHandlerWrapper.getInputVoltage(), TJValues.VCC[this.getTier()], tierName));
            tooltip.add(I18n.format("gregtech.universal.tooltip.amperage_in_till",
                    this.enderEnergyHandlerWrapper.getInputAmperage(), TJValues.VCC[this.getTier()]));
        }
        tooltip.add(I18n.format("gregtech.universal.enabled"));
    }

    @Override
    protected TJSimpleOverlayRenderer getOverlay() {
        return TJTextures.PORTAL_OVERLAY;
    }

    @Override
    protected Object2ObjectMap<String, EnderCoverProfile<BasicEnergyHandler>> getPlayerMap() {
        return EnderWorldData.getINSTANCE().getEnergyContainerPlayerMap();
    }

    @Override
    protected int getPortalColor() {
        return 0x9fff2c;
    }

    @Override
    protected void addWidgets(Consumer<Widget> widget) {
        widget.accept(new ProgressWidget(this::getEnergyPercent, 7, 38, 18, 18) {
            private long energyStored;
            private long energyCapacity;

            @Override
            public void drawInForeground(int mouseX, int mouseY) {
                if (isMouseOverElement(mouseX, mouseY)) {
                    final List<String> hoverList = Collections.singletonList(I18n.format("machine.universal.energy.stored",
                            TJValues.thousandFormat.format(this.energyStored), TJValues.thousandFormat.format(this.energyCapacity)));
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

    @Override
    public BasicEnergyHandler createHandler() {
        return new BasicEnergyHandler(this.capacity);
    }

    @Override
    protected void addChannelText(ITextComponent keyEntry, String key, BasicEnergyHandler value) {
        keyEntry.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new TextComponentTranslation("machine.universal.energy.stored",
                        TJValues.thousandFormat.format(value.getEnergyStored()), TJValues.thousandFormat.format(value.getEnergyCapacity()))));
    }

    @Override
    public MultiblockAbility<IEnergyContainer> getAbility() {
        return this.isOutput ? MultiblockAbility.OUTPUT_ENERGY : MultiblockAbility.INPUT_ENERGY;
    }

    @Override
    public void registerAbilities(List<IEnergyContainer> list) {
        list.add(this);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER)
            return GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER.cast(this);
        return super.getCapability(capability, side);
    }

    @Override
    public void setHandler(BasicEnergyHandler handler) {
        super.setHandler(handler);
        this.enderEnergyHandlerWrapper.setBasicEnergyHandler(this.handler != null ? this.handler : TJValues.DUMMY_ENERGY);
    }

    @Override
    public void setChannel(String lastEntry) {
        super.setChannel(lastEntry);
        this.enderEnergyHandlerWrapper.setBasicEnergyHandler(this.handler != null ? this.handler : TJValues.DUMMY_ENERGY);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.enderEnergyHandlerWrapper.setBasicEnergyHandler(this.handler != null ? this.handler : TJValues.DUMMY_ENERGY);
    }

    private double getEnergyPercent() {
        return this.handler != null ? (double) this.handler.getEnergyStored() / this.handler.getEnergyCapacity() : 0;
    }

    @Override
    public long acceptEnergyFromNetwork(EnumFacing enumFacing, long l, long l1) {
        return this.enderEnergyHandlerWrapper.acceptEnergyFromNetwork(enumFacing, l, l1);
    }

    @Override
    public boolean inputsEnergy(EnumFacing enumFacing) {
        return true;
    }

    @Override
    public long addEnergy(long energyToAdd) {
        return this.enderEnergyHandlerWrapper.addEnergy(energyToAdd);
    }

    @Override
    public long removeEnergy(long energyToRemove) {
        return this.enderEnergyHandlerWrapper.removeEnergy(energyToRemove);
    }

    @Override
    public long changeEnergy(long l) {
        return this.enderEnergyHandlerWrapper.changeEnergy(l);
    }

    @Override
    public long getEnergyStored() {
        return this.enderEnergyHandlerWrapper.getEnergyStored();
    }

    @Override
    public long getEnergyCapacity() {
        return this.enderEnergyHandlerWrapper.getEnergyCapacity();
    }

    @Override
    public long getOutputAmperage() {
        return this.enderEnergyHandlerWrapper.getOutputAmperage();
    }

    @Override
    public long getInputAmperage() {
        return this.enderEnergyHandlerWrapper.getInputAmperage();
    }

    @Override
    public long getInputVoltage() {
        return this.enderEnergyHandlerWrapper.getInputVoltage();
    }

    @Override
    public long getOutputVoltage() {
        return this.enderEnergyHandlerWrapper.getOutputVoltage();
    }

    @Override
    public void addToMultiBlock(MultiblockControllerBase controller) {
        super.addToMultiBlock(controller);
        this.enderEnergyHandlerWrapper.reinitializeEnergyLimit();
    }

    @Override
    public void removeFromMultiBlock(MultiblockControllerBase controller) {
        super.removeFromMultiBlock(controller);
        this.enderEnergyHandlerWrapper.reinitializeEnergyLimit();
    }
}
