package tj.integration.hwyla.providers;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import tj.capability.IGeneratorInfo;
import tj.capability.TJCapabilities;

import javax.annotation.Nonnull;
import java.util.List;

public class GeneratorInfoDataProvider extends CapabilityInfoDataProvider<IGeneratorInfo> {

    public static final GeneratorInfoDataProvider INSTANCE = new GeneratorInfoDataProvider();

    @Override
    public void register(IWailaRegistrar registrar) {
        super.register(registrar);
        registrar.registerNBTProvider(this, TileEntity.class);
        registrar.registerBodyProvider(this, TileEntity.class);
    }

    @Override
    public Capability<IGeneratorInfo> getCapability() {
        return TJCapabilities.CAPABILITY_GENERATOR;
    }

    @Override
    public String getConfigName() {
        return "tj.generatorinfo";
    }

    @Nonnull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config, IGeneratorInfo generatorInfo) {
        final NBTTagCompound compound = accessor.getNBTData().getCompoundTag(this.getConfigName());
        final NBTTagList consumptionList = compound.getTagList("consumptionList", 8);
        final NBTTagList productionList = compound.getTagList("productionList", 8);
        final long consumption = compound.getLong("consumption");
        final long production = compound.getLong("production");
        this.pageInfo(consumption, tooltip, consumptionList.tagList.stream()
                .map(nbtBase -> (NBTTagString)nbtBase)
                .map(NBTTagString::getString)
                .toArray(String[]::new));
        this.pageInfo(production, tooltip, productionList.tagList.stream()
                .map(nbtBase -> (NBTTagString)nbtBase)
                .map(NBTTagString::getString)
                .toArray(String[]::new));
        return tooltip;
    }

    @Nonnull
    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos, IGeneratorInfo generatorInfo) {
        final NBTTagCompound compound = new NBTTagCompound();
        final NBTTagList consumptionList = new NBTTagList();
        final NBTTagList productionList = new NBTTagList();
        for (String str : generatorInfo.consumptionInfo())
            consumptionList.appendTag(new NBTTagString(str));
        for (String str : generatorInfo.productionInfo())
            productionList.appendTag(new NBTTagString(str));
        compound.setTag("consumptionList", consumptionList);
        compound.setTag("productionList", productionList);
        compound.setLong("consumption", generatorInfo.getConsumption());
        compound.setLong("production", generatorInfo.getProduction());
        tag.setTag(this.getConfigName(), compound);
        return tag;
    }

    private void pageInfo(long amount, List<String> tooltips, String[] info) {
        if (amount < 1 || info == null)
            return;
        final StringBuilder prefixBuilder = new StringBuilder(), suffixBuilder = new StringBuilder();
        boolean suffix = false;
        for (String text : info) {
            if (text == null) continue;
            if (text.equals("suffix")) {
                suffix = true;
                continue;
            }

            final String textInfo = text.startsWith("§") ? text
                    : text.startsWith(" ") ? " "
                    : I18n.format(text);

            if (!suffix) {
                prefixBuilder.append(textInfo);
            } else suffixBuilder.append(textInfo);
        }
        tooltips.add(prefixBuilder + String.format("%,d", amount) + suffixBuilder);
    }
}
