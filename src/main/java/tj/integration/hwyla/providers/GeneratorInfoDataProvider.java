package tj.integration.hwyla.providers;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
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
import tj.capability.IGeneratorInfo;
import tj.capability.TJCapabilities;

import javax.annotation.Nonnull;
import java.util.List;

public class GeneratorInfoDataProvider implements IWailaDataProvider {

    public static final GeneratorInfoDataProvider INSTANCE = new GeneratorInfoDataProvider();

    public void register(IWailaRegistrar registrar) {
        registrar.registerNBTProvider(this, TileEntity.class);
        registrar.registerBodyProvider(this, TileEntity.class);
        registrar.addConfig("TJ", "tj.generatorinfo");
    }

    @Nonnull
    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos) {
        if (!(te instanceof MetaTileEntityHolder))
            return tag;
        final MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) te).getMetaTileEntity();
        if (metaTileEntity == null)
            return tag;
        final IGeneratorInfo generatorInfo = metaTileEntity.getCapability(TJCapabilities.CAPABILITY_GENERATOR, null);
        if (generatorInfo == null)
            return tag;
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
        tag.setTag("tj.generatorinfo", compound);
        return tag;
    }

    @Nonnull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        if (!config.getConfig("tj.generatorinfo"))
            return tooltip;
        if (!(accessor.getTileEntity() instanceof MetaTileEntityHolder))
            return tooltip;
        final MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) accessor.getTileEntity()).getMetaTileEntity();
        if (metaTileEntity == null)
            return tooltip;
        final IGeneratorInfo generatorInfo = metaTileEntity.getCapability(TJCapabilities.CAPABILITY_GENERATOR, null);
        if (generatorInfo == null)
            return tooltip;
        final NBTTagCompound compound = accessor.getNBTData().getCompoundTag("tj.generatorinfo");
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
