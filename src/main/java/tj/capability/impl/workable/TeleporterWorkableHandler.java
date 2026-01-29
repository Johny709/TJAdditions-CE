package tj.capability.impl.workable;

import gregtech.api.metatileentity.MetaTileEntity;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import tj.builder.multicontrollers.TJMultiblockControllerBase;
import tj.capability.AbstractWorkableHandler;
import tj.capability.IMachineHandler;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

public class TeleporterWorkableHandler extends AbstractWorkableHandler<IMachineHandler> {

    private static final Random random = new Random();
    private final Object2ObjectMap<String, Pair<Integer, BlockPos>> posMap = new Object2ObjectOpenHashMap<>();
    private final Queue<Triple<Entity, Integer, BlockPos>> queueTeleport = new ArrayDeque<>();
    private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
    private NBTTagCompound linkData;
    private String selectedPosName;

    public TeleporterWorkableHandler(MetaTileEntity metaTileEntity) {
        super(metaTileEntity);
    }

    @Override
    public void initialize(int busCount) {
        super.initialize(busCount);
        this.energyPerTick = (long) (Math.pow(4, this.handler.getTier()) * 8);
    }

    @Override
    protected boolean startRecipe() {
        if (!this.queueTeleport.isEmpty())
            this.transportEntity();
        return true;
    }

    @Override
    protected boolean completeRecipe() {
        Pair<Integer, BlockPos> worldPos = this.posMap.get(this.selectedPosName);
        if (worldPos != null && this.queueTeleport.isEmpty()) {
            int worldID = worldPos.getLeft();
            World world = DimensionManager.getWorld(worldID);
            if (world == null) {
                DimensionManager.initDimension(worldID);
                DimensionManager.keepDimensionLoaded(worldID, true);
            }
            if (world != null) {
                BlockPos targetPos = worldPos.getValue();
                this.pos.setPos(this.metaTileEntity.getPos().getX(), this.metaTileEntity.getPos().getY() + 1, this.metaTileEntity.getPos().getZ());
                ClassInheritanceMultiMap<Entity>[] entityLists = this.metaTileEntity.getWorld().getChunk(this.pos).getEntityLists();
                for (ClassInheritanceMultiMap<Entity> entities : entityLists) {
                    for (Entity entity : entities) {
                        BlockPos entityPos = entity.getPosition();
                        int entityX = entityPos.getX();
                        int entityY = entityPos.getY();
                        int entityZ = entityPos.getZ();
                        if (entityX == this.pos.getX() && entityY == this.pos.getY() && entityZ == this.pos.getZ())
                            this.queueTeleport.add(new ImmutableTriple<>(entity, worldID, targetPos));
                    }
                }
            }
        }
        if (this.metaTileEntity instanceof TJMultiblockControllerBase)
            ((TJMultiblockControllerBase) this.metaTileEntity).calculateMaintenance(this.maxProgress);
        return true;
    }

    private void transportEntity() {
        Triple<Entity, Integer, BlockPos> entityPos = this.queueTeleport.poll();
        Entity entity = entityPos.getLeft();
        WorldServer world = DimensionManager.getWorld(entityPos.getMiddle());
        if (entity == null || world == null)
            return;
        BlockPos pos = entityPos.getRight();
        int worldID = world.provider.getDimension();
        long energyX = Math.abs(pos.getX() - this.metaTileEntity.getPos().getX()) * 1000L;
        long energyY = Math.abs(pos.getY() - this.metaTileEntity.getPos().getY()) * 1000L;
        long energyZ = Math.abs(pos.getZ() - this.metaTileEntity.getPos().getZ()) * 1000L;
        boolean interDimensional = false;
        if (worldID != this.metaTileEntity.getWorld().provider.getDimension()) {
            interDimensional = true;
            energyX = Math.abs(pos.getX() * 1000);
            energyY = Math.abs(pos.getY() * 1000);
            energyZ = Math.abs(pos.getZ() * 1000);
        }

        long totalEnergyConsumption = 1000000 + energyX + energyY + energyZ;
        if (this.handler.getInputEnergyContainer().removeEnergy(totalEnergyConsumption) != -totalEnergyConsumption) {
            entity.sendMessage(new TextComponentString(I18n.translateToLocal("gregtech.multiblock.not_enough_energy") + "\n" + I18n.translateToLocal("tj.multiblock.teleporter.fail")));
            return;
        }
        this.generateParticles(this.metaTileEntity.getWorld(), entity, this.metaTileEntity.getPos().getX(), this.metaTileEntity.getPos().getY(), this.metaTileEntity.getPos().getZ());

        if (interDimensional) {
            entity.setWorld(world);
            entity.changeDimension(worldID, new Teleporter(world) {
                @Override
                public void placeEntity(World world, Entity entity, float yaw) {
                    entity.setLocationAndAngles(pos.getX(), pos.getY(), pos.getZ(), entity.rotationYaw, 0.0F);
                    entity.motionX = 0.0D;
                    entity.motionY = 0.0D;
                    entity.motionZ = 0.0D;
                }
            });
        } else entity.setPositionAndUpdate(pos.getX(), pos.getY(), pos.getZ());
        if (entity instanceof EntityPlayer)
            ((EntityPlayer) entity).closeScreen();
        this.generateParticles(world, entity, pos.getX(), pos.getY(), pos.getZ());
        entity.sendMessage(new TextComponentTranslation("tj.multiblock.teleporter.success"));

        DimensionManager.keepDimensionLoaded(worldID, false);
    }

    private void generateParticles(World world, Entity entity, int posX, int posY, int posZ) {
        for (int j = 0; j < 128; ++j) {
            double d6 = (double) j / 127.0D;
            float f = (random.nextFloat() - 0.5F) * 0.2F;
            float f1 = (random.nextFloat() - 0.5F) * 0.2F;
            float f2 = (random.nextFloat() - 0.5F) * 0.2F;
            double d3 = (double) posX + (posX - (double) posX) * d6 + (random.nextDouble() - 0.5D) * (double) entity.width * 2.0D;
            double d4 = (double) posY + (posY - (double) posY) * d6 + random.nextDouble() * (double) entity.height;
            double d5 = (double) posZ + (posZ - (double) posZ) * d6 + (random.nextDouble() - 0.5D) * (double) entity.width * 2.0D;
            world.spawnParticle(EnumParticleTypes.PORTAL, d3, d4, d5, f, f1, f2);
        }
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound data = super.serializeNBT();
        NBTTagList posList = new NBTTagList();
        for (Map.Entry<String, Pair<Integer, BlockPos>> posEntry : this.posMap.entrySet()) {
            String name = posEntry.getKey();
            int worldID = posEntry.getValue().getLeft();
            int x = posEntry.getValue().getRight().getX();
            int y = posEntry.getValue().getRight().getY();
            int z = posEntry.getValue().getRight().getZ();

            NBTTagCompound posTag = new NBTTagCompound();
            posTag.setString("name", name);
            posTag.setInteger("world", worldID);
            posTag.setInteger("x", x);
            posTag.setInteger("y", y);
            posTag.setInteger("z", z);
            posList.appendTag(posTag);
        }
        data.setTag("posList", posList);
        if (this.selectedPosName != null)
            data.setString("selectedPos", this.selectedPosName);
        if (this.linkData != null)
            data.setTag("link.XYZ", this.linkData);
        return data;
    }

    @Override
    public void deserializeNBT(NBTTagCompound data) {
        super.deserializeNBT(data);
        this.selectedPosName = data.getString("selectedPos");
        if (data.hasKey("link.XYZ"))
            this.linkData = data.getCompoundTag("link.XYZ");
        if (!data.hasKey("posList"))
            return;
        NBTTagList posList = data.getTagList("posList", Constants.NBT.TAG_COMPOUND);
        for (NBTBase compound : posList) {
            NBTTagCompound tag = (NBTTagCompound) compound;
            String name = tag.getString("name");
            int worldID = tag.getInteger("worldId");
            BlockPos pos = new BlockPos(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z"));
            this.posMap.put(name, new ImmutablePair<>(worldID, pos));
        }
    }

    public void renameLink(String name, String id) {
        Pair<Integer, BlockPos> pos = this.posMap.get(id);
        this.posMap.remove(id);
        this.posMap.put(name, pos);
        this.metaTileEntity.markDirty();
    }

    public void setReset(boolean reset) {
        this.posMap.clear();
        this.linkData.setInteger("I", 1);
    }

    public void setLinkData(NBTTagCompound linkData) {
        this.linkData = linkData;
        this.metaTileEntity.markDirty();
    }

    public NBTTagCompound getLinkData() {
        return this.linkData;
    }

    public Object2ObjectMap<String, Pair<Integer, BlockPos>> getPosMap() {
        return this.posMap;
    }

    public Queue<Triple<Entity, Integer, BlockPos>> getQueueTeleport() {
        return this.queueTeleport;
    }

    public void setSelectedPosName(String selectedPosName) {
        this.selectedPosName = selectedPosName;
    }

    public String getSelectedPosName() {
        return this.selectedPosName;
    }
}
