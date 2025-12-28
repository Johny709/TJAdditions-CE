package tj.machines.singleblock;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import tj.capability.IHeatInfo;
import tj.textures.TJTextures;
import gregtech.api.capability.IFuelInfo;
import gregtech.api.capability.IFuelable;
import gregtech.api.capability.IWorkable;
import gregtech.api.capability.impl.FilteredFluidHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemFuelInfo;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.recipes.ModHandler;
import gregtech.api.render.SimpleSidedCubeRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static tj.capability.TJCapabilities.CAPABILITY_HEAT;
import static tj.gui.TJGuiTextures.BAR_HEAT;
import static tj.machines.singleblock.BoilerType.BRONZE;
import static tj.machines.singleblock.BoilerType.STEEL;
import static gregtech.api.capability.GregtechCapabilities.CAPABILITY_FUELABLE;
import static gregtech.api.capability.GregtechTileCapabilities.CAPABILITY_WORKABLE;
import static gregtech.api.gui.widgets.ProgressWidget.MoveType.VERTICAL;
import static gregtech.api.unification.material.Materials.Steam;

public class MetaTileEntityCoalBoiler extends MetaTileEntity implements IWorkable, IFuelInfo, IFuelable, IHeatInfo {

    protected float temp;
    private boolean isActive;
    private boolean hadWater;
    protected int burnTime;
    protected int maxBurnTime;
    private boolean isWorking = true;

    protected final IFluidTank waterTank;
    protected final IFluidTank steamTank;
    protected final BoilerType boilerType;
    private static final EnumFacing[] STEAM_PUSH_DIRECTIONS = ArrayUtils.add(EnumFacing.HORIZONTALS, EnumFacing.UP);
    public static final int BASE_MODIFIER = 12;

    public MetaTileEntityCoalBoiler(ResourceLocation metaTileEntityId, BoilerType boilerType) {
        super(metaTileEntityId);
        this.boilerType = boilerType;
        this.waterTank = new FilteredFluidHandler(16000).setFillPredicate(ModHandler::isWater);
        this.steamTank = new FluidTank(16000);
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityCoalBoiler(metaTileEntityId, boilerType);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.steam_boiler.tooltip_produces", boilerType.getSteamProduction(), boilerType.getTicks()));
        tooltip.add(I18n.format("machine.steam_boiler.tooltip.heat", 100 + "%"));
        tooltip.add(I18n.format("machine.steam_boiler.tooltip.cooldown", boilerType.getCooldown() * 100 + "%"));
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new ItemStackHandler(2);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new ItemStackHandler(2);
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        return new FluidTankList(true, waterTank);
    }

    @Override
    protected FluidTankList createExportFluidHandler() {
        return new FluidTankList(true, steamTank);
    }

    protected void addWidgets(Consumer<Widget> widget) {
        widget.accept(new SlotWidget(importItems, 1, 115, 15)
                .setBackgroundTexture(boilerType.getSlot(), GuiTextures.FURNACE_OVERLAY));
        widget.accept(new SlotWidget(exportItems, 1, 115, 57, true, false)
                .setBackgroundTexture(boilerType.getSlot(), GuiTextures.DUST_OVERLAY));
        widget.accept(new ProgressWidget(this::getBurnPercent, 115, 35, 18, 18) {
            private int displayBurnTimeInSeconds;

            @Override
            public void drawInForeground(int mouseX, int mouseY) {
                if(isMouseOverElement(mouseX, mouseY)) {
                    List<String> hoverList = Arrays.asList(I18n.format("machine.boiler.display.burning", this.displayBurnTimeInSeconds),
                            I18n.format("machine.boiler.display.burning.info"));
                    drawHoveringText(ItemStack.EMPTY, hoverList, 300, mouseX, mouseY);
                }
            }

            @Override
            public void detectAndSendChanges() {
                super.detectAndSendChanges();
                int displayBurnTimeInSeconds = getProgress() / 20;
                writeUpdateInfo(1, buffer -> buffer.writeInt(displayBurnTimeInSeconds));
            }

            @Override
            public void readUpdateInfo(int id, PacketBuffer buffer) {
                super.readUpdateInfo(id, buffer);
                if (id == 1) {
                    this.displayBurnTimeInSeconds = buffer.readInt();
                }
            }
        }.setProgressBar(boilerType.getFuelEmpty(), boilerType.getFuelFull(), VERTICAL));
    }

    @Override
    protected ModularUI createUI(EntityPlayer player) {
        WidgetGroup widgetGroup = new WidgetGroup();
        addWidgets(widgetGroup::addWidget);
        widgetGroup.addWidget(new ProgressWidget(this::getTempPercent, 95, 15, 15, 60) {
            private float displayTemp;
            private int displaySteamGeneration;
            private boolean displayCanGenerateSteam;

            @Override
            public void drawInForeground(int mouseX, int mouseY) {
                if(isMouseOverElement(mouseX, mouseY)) {
                    List<String> hoverList = Arrays.asList(I18n.format("machine.boiler.display.temperature", this.displayTemp),
                            I18n.format("machine.boiler.display.steam", this.displayCanGenerateSteam ? this.displaySteamGeneration : 0, boilerType.getTicks()),
                            I18n.format("machine.boiler.display.steam.description"));
                    drawHoveringText(ItemStack.EMPTY, hoverList, 300, mouseX, mouseY);
                }
            }

            @Override
            public void detectAndSendChanges() {
                super.detectAndSendChanges();
                float displayTemp = (getTempPercent() * 5) * 100;
                int displaySteamGeneration = (int) (boilerType.getSteamProduction() * getTempPercent());
                boolean displayCanGenerateSteam = canGenerateSteam();
                writeUpdateInfo(1, buffer -> buffer.writeFloat(displayTemp));
                writeUpdateInfo(2, buffer -> buffer.writeInt(displaySteamGeneration));
                writeUpdateInfo(3, buffer -> buffer.writeBoolean(displayCanGenerateSteam));
            }

            @Override
            public void readUpdateInfo(int id, PacketBuffer buffer) {
                super.readUpdateInfo(id, buffer);
                if (id == 1) {
                    this.displayTemp = buffer.readFloat();
                }
                if (id == 2) {
                    this.displaySteamGeneration = buffer.readInt();
                }
                if (id == 3) {
                    this.displayCanGenerateSteam = buffer.readBoolean();
                }
            }

        }.setProgressBar(boilerType.getProgressBar(), BAR_HEAT, VERTICAL));
        widgetGroup.addWidget(new TankWidget(waterTank, 78, 15, 15, 60)
                .setBackgroundTexture(boilerType.getProgressBar()));
        widgetGroup.addWidget(new TankWidget(steamTank, 61, 15, 15, 60)
                .setBackgroundTexture(boilerType.getProgressBar()));
        widgetGroup.addWidget(new SlotWidget(importItems, 0, 38, 15)
                .setBackgroundTexture(boilerType.getSlot(), boilerType.getIn()));
        widgetGroup.addWidget(new SlotWidget(exportItems, 0, 38, 57, true, false)
                .setBackgroundTexture(boilerType.getSlot(), boilerType.getOut()));
        widgetGroup.addWidget(new ImageWidget(38, 35, 18, 18, boilerType.getCanister()));
        return ModularUI.builder(boilerType.getBackground(), 176, 167)
                .label(10, 5, getMetaFullName())
                .widget(widgetGroup)
                .bindPlayerInventory(player.inventory, boilerType.getSlot(), 7, 85)
                .build(getHolder(), player);
    }

    protected boolean canBurn() {
        if (burnTime > 0) {
            burnTime = MathHelper.clamp(burnTime - boilerType.getTicks(), 0, maxBurnTime);
            return true;
        }
        ItemStack stack = importItems.getStackInSlot(1);
        int burnValue = getBurnValue(stack);
        if (burnValue > 0) {
            stack.shrink(1);
            this.burnTime = burnValue;
            this.maxBurnTime = burnValue;
            return true;
        }
        return false;
    }

    private int getBurnValue(ItemStack stack) {
        return (TileEntityFurnace.getItemBurnTime(stack) * BASE_MODIFIER) /
                (boilerType == BRONZE ? 1
                        : boilerType == STEEL ? 2
                        : 4);
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote && getOffsetTimer() % boilerType.getTicks() == 0) {
            if (isWorking && canBurn()) {
                if (!isActive)
                    setActive(true);
                temp = MathHelper.clamp(temp + (1.00F * boilerType.getTicks()), 0, 12000);
            } else {
                if (isActive)
                    setActive(false);
                temp = MathHelper.clamp(temp - (boilerType.getCooldown() * boilerType.getTicks()), 0, 12000);
            }
            fillInternalTankFromFluidContainer(importItems, exportItems, 0, 0);
            pushFluidsIntoNearbyHandlers(STEAM_PUSH_DIRECTIONS);
            if (!canGenerateSteam()) {
                hadWater = false;
                return;
            }
            int waterToConsume = Math.round((float) boilerType.getSteamProduction() / 160);
            FluidStack waterStack = waterTank.drain(waterToConsume, false);
            boolean hasEnoughWater = waterStack != null && waterStack.amount == waterToConsume;
            if (hasEnoughWater && hadWater) {
                getWorld().setBlockToAir(this.getPos());
                getWorld().createExplosion(null,
                        getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5,
                        2.0f, true);
            } else {
                if (hasEnoughWater) {
                    steamTank.fill(Steam.getFluid((int) (boilerType.getSteamProduction() * getTempPercent())), true);
                    waterTank.drain(waterToConsume, true);
                } else
                    hadWater = true;
            }
        }
    }

    @Override
    public String getFuelName() {
        return importItems.getStackInSlot(1).getTranslationKey();
    }

    @Override
    public int getFuelRemaining() {
        return importItems.getStackInSlot(1).getCount();
    }

    @Override
    public int getFuelCapacity() {
        return importItems.getStackInSlot(1).getMaxStackSize();
    }

    @Override
    public int getFuelMinConsumed() {
        return 1;
    }

    @Override
    public int getFuelBurnTime() {
        ItemStack stack = importItems.getStackInSlot(1);
        return stack.getCount() * getBurnValue(stack);
    }

    @Override
    public Collection<IFuelInfo> getFuels() {
        ItemStack stack = importItems.getStackInSlot(1);
        if (stack.isEmpty())
            return Collections.emptyList();
        return Collections.singletonList(new ItemFuelInfo(stack, getFuelRemaining(), getFuelCapacity(), getFuelMinConsumed(), (long) getFuelBurnTime()));
    }

    public float getBurnPercent() {
        return burnTime / (maxBurnTime * 1.00F);
    }

    public float getTempPercent() {
        return temp / (12000 * 1.00F);
    }

    @Override
    public int getProgress() {
        return burnTime;
    }

    @Override
    public int getMaxProgress() {
        return maxBurnTime;
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    public boolean canGenerateSteam() {
        return temp >= 2400;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        getBaseRenderer().render(renderState, translation, pipeline);
        TJTextures.BOILER_OVERLAY.render(renderState, translation, pipeline, getFrontFacing(), isActive());
    }

    @SideOnly(Side.CLIENT)
    protected SimpleSidedCubeRenderer getBaseRenderer() {
        return boilerType.getCasing();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(getBaseRenderer().getParticleSprite(), getPaintingColor());
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == 1) {
            this.isActive = buf.readBoolean();
            scheduleRenderUpdate();
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isActive);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.isActive = buf.readBoolean();
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
        data.setFloat("Temp", temp);
        data.setBoolean("HadWater", hadWater);
        data.setBoolean("IsActive", isActive);
        data.setBoolean("IsWorking", isWorking);
        data.setInteger("BurnTime", burnTime);
        data.setInteger("MaxBurnTime", maxBurnTime);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.temp = data.getFloat("Temp");
        this.hadWater = data.getBoolean("HadWater");
        this.isActive = data.getBoolean("IsActive");
        this.maxBurnTime = data.getInteger("MaxBurnTime");
        this.burnTime = data.getInteger("BurnTime");
        if (data.hasKey("IsWorking"))
            this.isWorking = data.getBoolean("IsWorking");
    }

    @Override
    public boolean isWorkingEnabled() {
        return isWorking;
    }

    @Override
    public void setWorkingEnabled(boolean isWorking) {
        this.isWorking = isWorking;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == CAPABILITY_WORKABLE)
            return CAPABILITY_WORKABLE.cast(this);
        else if (capability == CAPABILITY_FUELABLE)
            return CAPABILITY_FUELABLE.cast(this);
        else if (capability == CAPABILITY_HEAT)
            return CAPABILITY_HEAT.cast(this);
        return super.getCapability(capability, side);
    }

    @Override
    public long heat() {
        return (long) temp / 24;
    }

    @Override
    public long maxHeat() {
        return 500;
    }
}
