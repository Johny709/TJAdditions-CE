package tj.capability;

import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.common.ConfigHolder;
import net.minecraft.nbt.*;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.capabilities.Capability;
import tj.TJConfig;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.Arrays;
import java.util.function.BiConsumer;

public abstract class AbstractParallelWorkableHandler<H extends IMachineHandler> extends MTETrait implements IMultipleWorkable {

    protected final H handler;
    protected BiConsumer<Boolean, Integer> activeConsumer;
    protected BiConsumer<Boolean, Integer> problemConsumer;
    protected BiConsumer<Boolean, Integer> workingConsumer;
    protected boolean[] isWorking = new boolean[1];
    protected boolean[] isActive = new boolean[1];
    protected boolean[] hasProblem = new boolean[1];
    protected boolean[] wasActiveAndNeedsUpdate = new boolean[1];
    protected long[] energyPerTick = new long[1];
    protected int[] progress = new int[1];
    protected int[] maxProgress = new int[1];
    protected int[] parallel = new int[1];
    protected int[] lastInputIndex = new int[1];
    protected int[] sleepTimer = new int[1];
    protected int[] sleepTime = new int[1];
    protected int[] failCount = new int[1];
    protected boolean isDistinct;
    protected int busCount;
    protected int size = 1;

    public AbstractParallelWorkableHandler(MetaTileEntity metaTileEntity) {
        super(metaTileEntity);
        Arrays.fill(this.isWorking, true);
        Arrays.fill(this.sleepTime, 1);
        Arrays.fill(this.parallel, 1);
        if (metaTileEntity instanceof IMachineHandler) {
            this.handler = (H) metaTileEntity;
        } else throw new IllegalArgumentException("MetaTileEntity must implement IMachineHandler to use this workable handler");
    }

    public void setWorkingConsumer(BiConsumer<Boolean, Integer> workingConsumer) {
        this.workingConsumer = workingConsumer;
    }

    public void setActiveConsumer(BiConsumer<Boolean, Integer> activeConsumer) {
        this.activeConsumer = activeConsumer;
    }

    public void setProblemConsumer(BiConsumer<Boolean, Integer> problemConsumer) {
        this.problemConsumer = problemConsumer;
    }

    public void setLayer(int i, boolean remove) {
        this.size = i;
        this.isWorking = Arrays.copyOf(this.isWorking, this.size);
        this.isActive = Arrays.copyOf(this.isActive, this.size);
        this.hasProblem = Arrays.copyOf(this.hasProblem, this.size);
        this.wasActiveAndNeedsUpdate = Arrays.copyOf(this.wasActiveAndNeedsUpdate, this.size);
        this.energyPerTick = Arrays.copyOf(this.energyPerTick, this.size);
        this.progress = Arrays.copyOf(this.progress, this.size);
        this.maxProgress = Arrays.copyOf(this.maxProgress, this.size);
        this.parallel = Arrays.copyOf(this.parallel, this.size);
        this.lastInputIndex = Arrays.copyOf(this.lastInputIndex, this.size);
        this.sleepTimer = Arrays.copyOf(this.sleepTimer, this.size);
        this.sleepTime = Arrays.copyOf(this.sleepTime, this.size);
        this.failCount = Arrays.copyOf(this.failCount, this.size);
        if (!remove) {
            this.sleepTime[this.size -1] = 1;
            this.isWorking[this.size -1] = true;
            this.parallel[this.size -1] = 1;
        }
    }

    public void invalidate() {}

    /**
     * this should be called to initialize some stuff before this workable handler starts running!
     * @param busCount amount of item input buses
     */
    public void initialize(int busCount) {
        Arrays.fill(this.lastInputIndex, 0);
        this.busCount = busCount;
    }

    public void update(int i) {
        if (!this.isWorking[i]) {
            this.stopRecipe(i);
            return;
        }
        if (this.wasActiveAndNeedsUpdate[i] && this.isActive[i])
            this.setActive(false, i);
        if (this.progress[i] > this.maxProgress[i]) {
            if (this.completeRecipe(i)) {
                this.progress[i] = 0;
                this.energyPerTick[i] = 0;
                if (this.handler.hasMaintenanceHatch())
                    this.handler.calculateMaintenance(this.maxProgress[i]);
                if (this.hasProblem[i])
                    this.setProblem(false, i);
            } else {
                this.progress[i] = 1;
                this.maxProgress[i] = TJConfig.machines.recipeCooldown;
                this.energyPerTick[i] = 0;
                if (!this.hasProblem[i])
                    this.setProblem(true, i);
            }
        }
        if (this.progress[i] < 1) {
            if (this.sleepTimer[i] > 1) {
                this.sleepRecipe(i);
                return;
            }
            boolean canStart = this.startRecipe(i);
            if (canStart) {
                this.sleepTime[i] = 1;
                this.progress[i] = 1;
                this.progressRecipe(this.progress[i], i);
                if (!this.isActive[i])
                    this.setActive(true, i);
            } else this.failRecipe(i);
            this.wasActiveAndNeedsUpdate[i] = !canStart;
        } else this.progressRecipe(this.progress[i], i);
    }

    /**
     * @return true if the recipe can start. Calls {@link #failRecipe(int i)} if the recipe cannot start.
     */
    protected boolean startRecipe(int i) {
        return false;
    }

    /**
     * For every tick the workable handler is on sleep timer
     */
    @OverridingMethodsMustInvokeSuper
    protected void sleepRecipe(int i) {
        this.sleepTimer[i]--;
    }

    /**
     * For every tick the workable handler is stopped
     */
    protected void stopRecipe(int i) {}

    /**
     * This gets called if recipe wasn't able to start, {@link #startRecipe(int i)} returns false.
     * Recommended to invoke super method to utilize sleep timer for performance
     */
    @OverridingMethodsMustInvokeSuper
    protected void failRecipe(int i) {
        if (this.failCount[i] > 4) {
            this.sleepTime[i] = Math.min(this.sleepTime[i] * 2, Math.min(ConfigHolder.maxSleepTime, 400));
            this.failCount[i] = 0;
        } else this.failCount[i]++;
        this.sleepTimer[i] = this.sleepTime[i];
    }

    protected void progressRecipe(int progress, int i) {
        if (this.handler.getInputEnergyContainer().removeEnergy(this.energyPerTick[i]) == -this.energyPerTick[i]) {
            this.progress[i]++;
        } else if (this.progress[i] > 1)
            this.progress[i]--;
    }

    /**
     * @return true if the recipe can be completed and then will try to start the next recipe.
     */
    protected boolean completeRecipe(int i) {
        return false;
    }

    protected int calculateOverclock(long baseEnergy, int duration, float multiplier, int i) {
        long voltage = this.handler.getMaxVoltage();
        baseEnergy *= 4;
        while (duration > 1 && baseEnergy <= voltage) {
            duration /= multiplier;
            baseEnergy *= 4;
        }
        this.energyPerTick[i] = baseEnergy / 4;
        return Math.max(1, duration);
    }

    @Override
    public void receiveCustomData(int id, PacketBuffer buffer) {
        switch (id) {
            case 1: this.isActive[buffer.readInt()] = buffer.readBoolean(); break;
            case 2: this.hasProblem[buffer.readInt()] = buffer.readBoolean(); break;
            case 3: this.isWorking[buffer.readInt()] = buffer.readBoolean(); break;
        }
        this.metaTileEntity.scheduleRenderUpdate();
    }

    @Override
    public void writeInitialData(PacketBuffer buffer) {
        this.writeBoolArrayToBuffer(buffer, this.isActive);
        this.writeBoolArrayToBuffer(buffer, this.hasProblem);
        this.writeBoolArrayToBuffer(buffer, this.isWorking);
    }

    @Override
    public void receiveInitialData(PacketBuffer buffer) {
        this.isActive = this.readBoolArrayFromBuffer(buffer);
        this.hasProblem = this.readBoolArrayFromBuffer(buffer);
        this.isWorking = this.readBoolArrayFromBuffer(buffer);
    }

    private void writeBoolArrayToBuffer(PacketBuffer buffer, boolean[] booleans) {
        buffer.writeInt(booleans.length);
        for (boolean bool : booleans)
            buffer.writeBoolean(bool);
    }

    private boolean[] readBoolArrayFromBuffer(PacketBuffer buffer) {
        int size = buffer.readInt();
        boolean[] booleans = new boolean[size];
        for (int i = 0; i < booleans.length; i++)
            booleans[i] = buffer.readBoolean();
        return booleans;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        NBTTagList isWorkingList = new NBTTagList();
        NBTTagList isActiveList = new NBTTagList();
        NBTTagList hasProblemList = new NBTTagList();
        NBTTagList wasActiveAndNeedsUpdateList = new NBTTagList();
        NBTTagList energyPerTickList = new NBTTagList();
        NBTTagList progressList = new NBTTagList();
        NBTTagList maxProgressList = new NBTTagList();
        NBTTagList parallelList = new NBTTagList();
        for (boolean isWorking : this.isWorking)
            isWorkingList.appendTag(new NBTTagByte((byte) (isWorking ? 1 : 0)));
        for (boolean isActive : this.isActive)
            isActiveList.appendTag(new NBTTagByte((byte) (isActive ? 1 : 0)));
        for (boolean hasProblem : this.hasProblem)
            hasProblemList.appendTag(new NBTTagByte((byte) (hasProblem ? 1 : 0)));
        for (boolean wasActiveAndNeedsUpdate : this.wasActiveAndNeedsUpdate)
            wasActiveAndNeedsUpdateList.appendTag(new NBTTagByte((byte) (wasActiveAndNeedsUpdate ? 1 : 0)));
        for (long energyPerTick : this.energyPerTick)
            energyPerTickList.appendTag(new NBTTagLong(energyPerTick));
        for (int progress : this.progress)
            progressList.appendTag(new NBTTagInt(progress));
        for (int maxProgress : this.maxProgress)
            maxProgressList.appendTag(new NBTTagInt(maxProgress));
        for (int parallel : this.parallel)
            parallelList.appendTag(new NBTTagInt(parallel));
        compound.setTag("isWorking", isWorkingList);
        compound.setTag("isActive", isActiveList);
        compound.setTag("hasProblem", hasProblemList);
        compound.setTag("wasActiveAndNeedsUpdate", wasActiveAndNeedsUpdateList);
        compound.setTag("energyPerTick", energyPerTickList);
        compound.setTag("progress", progressList);
        compound.setTag("maxProgress", maxProgressList);
        compound.setTag("parallel", parallelList);
        compound.setInteger("size", this.size);
        compound.setBoolean("distinct", this.isDistinct);
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        NBTTagList isWorkingList = compound.getTagList("isWorking", 1);
        NBTTagList isActiveList = compound.getTagList("isActive", 1);
        NBTTagList hasProblemList = compound.getTagList("hasProblem", 1);
        NBTTagList wasActiveAndNeedsUpdate = compound.getTagList("wasActiveAndNeedsUpdate", 1);
        NBTTagList energyPerTickList = compound.getTagList("energyPerTick", 4);
        NBTTagList progressList = compound.getTagList("progress", 3);
        NBTTagList maxProgressList = compound.getTagList("maxProgress", 3);
        NBTTagList parallelList = compound.getTagList("parallel", 3);
        this.size = compound.getInteger("size");
        this.isWorking = new boolean[this.size];
        this.isActive = new boolean[this.size];
        this.hasProblem = new boolean[this.size];
        this.wasActiveAndNeedsUpdate = new boolean[this.size];
        this.energyPerTick = new long[this.size];
        this.progress = new int[this.size];
        this.maxProgress = new int[this.size];
        this.parallel = new int[this.size];
        this.lastInputIndex = new int[this.size];
        this.sleepTime = new int[this.size];
        this.sleepTimer = new int[this.size];
        this.failCount = new int[this.size];
        for (int i = 0; i < isWorkingList.tagCount(); i++)
            this.isWorking[i] = ((NBTTagByte) isWorkingList.get(i)).getByte() == 1;
        for (int i = 0; i < isActiveList.tagCount(); i++)
            this.isActive[i] = ((NBTTagByte) isActiveList.get(i)).getByte() == 1;
        for (int i = 0; i < hasProblemList.tagCount(); i++)
            this.hasProblem[i] = ((NBTTagByte) hasProblemList.get(i)).getByte() == 1;
        for (int i = 0; i < wasActiveAndNeedsUpdate.tagCount(); i++)
            this.wasActiveAndNeedsUpdate[i] = ((NBTTagByte) wasActiveAndNeedsUpdate.get(i)).getByte() == 1;
        for (int i = 0; i < energyPerTickList.tagCount(); i++)
            this.energyPerTick[i] = ((NBTTagLong) energyPerTickList.get(i)).getLong();
        for (int i = 0; i < progressList.tagCount(); i++)
            this.progress[i] = progressList.getIntAt(i);
        for (int i = 0; i < maxProgressList.tagCount(); i++)
            this.maxProgress[i] = maxProgressList.getIntAt(i);
        for (int i = 0; i < parallelList.tagCount(); i++)
            this.parallel[i] = parallelList.getIntAt(i);
        this.isDistinct = compound.getBoolean("distinct");
        Arrays.fill(this.sleepTime, 1);
    }

    @Override
    public <T> T getCapability(Capability<T> capability) {
        if (capability == TJCapabilities.CAPABILITY_MULTI_CONTROLLABLE)
            return TJCapabilities.CAPABILITY_MULTI_CONTROLLABLE.cast(this);
        return capability == TJCapabilities.CAPABILITY_MULTIPLE_WORKABLE ? TJCapabilities.CAPABILITY_MULTIPLE_WORKABLE.cast(this) : null;
    }

    public void setDistinct(boolean distinct) {
        if (distinct && this.busCount < 1) return;
        this.isDistinct = distinct;
        this.metaTileEntity.markDirty();
    }

    public boolean isDistinct() {
        return this.isDistinct;
    }

    @Override
    public String getName() {
        return "RecipeWorkable";
    }

    @Override
    public int getNetworkID() {
        return TraitNetworkIds.TRAIT_ID_WORKABLE;
    }

    @Override
    public long getRecipeEUt(int i) {
        return this.energyPerTick[i];
    }

    @Override
    public int getParallel(int i) {
        return this.parallel[i];
    }

    @Override
    public int getProgress(int i) {
        return this.progress[i];
    }

    public void setMaxProgress(int maxProgress, int i) {
        this.maxProgress[i] = Math.max(1, maxProgress);
        this.metaTileEntity.markDirty();
    }

    @Override
    public int getMaxProgress(int i) {
        return this.maxProgress[i];
    }

    public double getProgressPercent(int i) {
        return this.getMaxProgress(i) == 0 ? 0.0 : this.getProgress(i) / (this.getMaxProgress(i) * 1.0);
    }

    public void setActive(boolean isActive, int i) {
        this.isActive[i] = isActive;
        if (!this.metaTileEntity.getWorld().isRemote) {
            if (this.activeConsumer != null)
                this.activeConsumer.accept(isActive, i);
            this.writeCustomData(1, buffer -> {
                buffer.writeInt(i);
                buffer.writeBoolean(isActive);
            });
        }
        this.metaTileEntity.markDirty();
    }

    @Override
    public boolean isInstanceActive(int i) {
        return this.isActive[i];
    }

    public void setProblem(boolean hasProblem, int i) {
        this.hasProblem[i] = hasProblem;
        if (!this.metaTileEntity.getWorld().isRemote) {
            if (this.problemConsumer != null)
                this.problemConsumer.accept(hasProblem, i);
            this.writeCustomData(2, buffer -> {
                buffer.writeInt(i);
                buffer.writeBoolean(hasProblem);
            });
        }
        this.metaTileEntity.markDirty();
    }

    @Override
    public boolean hasProblems(int i) {
        return this.hasProblem[i];
    }

    @Override
    public void setWorkingEnabled(boolean isActivationAllowed, int i) {
        this.isWorking[i] = isActivationAllowed;
        if (!this.metaTileEntity.getWorld().isRemote) {
            if (this.workingConsumer != null)
                this.workingConsumer.accept(isActivationAllowed, i);
            this.writeCustomData(3, buffer -> {
                buffer.writeInt(i);
                buffer.writeBoolean(isActivationAllowed);
            });
        }
        this.metaTileEntity.markDirty();
    }

    @Override
    public boolean isWorkingEnabled(int i) {
        return this.isWorking[i];
    }

    public boolean isActive() {
        for (int i = 0; i < this.size; i++)
            if (this.isInstanceActive(i))
                return true;
        return false;
    }

    @Override
    public int getSize() {
        return this.size;
    }
}
