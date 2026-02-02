package tj.integration.appeng.items.materials;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Upgrades;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.implementations.items.IItemGroup;
import appeng.api.implementations.items.IStorageComponent;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.implementations.tiles.ISegmentedInventory;
import appeng.api.parts.IPartHost;
import appeng.api.parts.SelectedPart;
import appeng.api.storage.ICellWorkbenchItem;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appeng.core.features.IStackSrc;
import appeng.items.AEBaseItem;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.AdaptorItemHandler;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.oredict.OreDictionary;
import tj.integration.appeng.items.TJAE2MaterialStackSrc;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TJItemMaterial extends AEBaseItem implements IStorageComponent, IUpgradeModule, ICellWorkbenchItem {

    public static TJItemMaterial INSTANCE;

    private static final int KILO_SCALAR = 1024;

    private final Int2ObjectMap<TJAE2MaterialType> dmgToMaterial = new Int2ObjectOpenHashMap<>();

    public TJItemMaterial() {
        this.setHasSubtypes(true);
        INSTANCE = this;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addCheckedInformation(final ItemStack stack, final World world, final List<String> lines, final ITooltipFlag advancedTooltips) {
        super.addCheckedInformation(stack, world, lines, advancedTooltips);

        final TJAE2MaterialType mt = this.getTypeByStack(stack);
        if (mt == null) {
            return;
        }

//        if (mt == MaterialType.NAME_PRESS) {
//            final NBTTagCompound c = Platform.openNbtData(stack);
//            lines.add(c.getString("InscribeName"));
//        }
//
//        if (mt == MaterialType.CARD_MAGNET) {
//            final NBTTagCompound c = Platform.openNbtData(stack);
//            if (!c.hasKey("enabled") || c.getBoolean("enabled")) {
//                lines.add(I18n.translateToLocal("gui.tooltips.appliedenergistics2.Enable"));
//            } else {
//                lines.add(I18n.translateToLocal("gui.tooltips.appliedenergistics2.Disabled"));
//            }
//            lines.add(I18n.translateToLocal("item.appliedenergistics2.material.card_magnet.usage"));
//            lines.add(I18n.translateToLocal("item.appliedenergistics2.material.card_magnet.partition"));
//        }

        final Upgrades u = this.getType(stack);
        if (u != null) {
            final List<String> textList = new ArrayList<>();
            for (final Map.Entry<ItemStack, Integer> j : u.getSupported().entrySet()) {
                String name = null;

                final int limit = j.getValue();

                if (j.getKey().getItem() instanceof IItemGroup) {
                    final IItemGroup ig = (IItemGroup) j.getKey().getItem();
                    final String str = ig.getUnlocalizedGroupName(u.getSupported().keySet(), j.getKey());
                    if (str != null) {
                        name = Platform.gui_localize(str) + (limit > 1 ? " (" + limit + ')' : "");
                    }
                }

                if (name == null) {
                    name = j.getKey().getDisplayName() + (limit > 1 ? " (" + limit + ')' : "");
                }

                if (!textList.contains(name)) {
                    textList.add(name);
                }
            }

            final Pattern p = Pattern.compile("(\\d+)[^\\d]");
            final SlightlyBetterSort s = new SlightlyBetterSort(p);
            Collections.sort(textList, s);
            lines.addAll(textList);
        }
    }

    public TJAE2MaterialType getTypeByStack(final ItemStack is) {
        TJAE2MaterialType type = this.dmgToMaterial.get(is.getItemDamage());
        return (type != null) ? type : TJAE2MaterialType.INVALID_TYPE;
    }

    @Override
    public Upgrades getType(final ItemStack itemstack) {
        return null;
    }

    public IStackSrc createMaterial(final TJAE2MaterialType mat) {
        Preconditions.checkState(!mat.isRegistered(), "Cannot create the same material twice.");

        boolean enabled = true;

        for (final AEFeature f : mat.getFeature()) {
            enabled = enabled && AEConfig.instance().isFeatureEnabled(f);
        }

        mat.setStackSrc(new TJAE2MaterialStackSrc(mat, enabled));

        if (enabled) {
            mat.setItemInstance(this);
            mat.markReady();
            final int newMaterialNum = mat.getDamageValue();

            if (this.dmgToMaterial.get(newMaterialNum) == null) {
                this.dmgToMaterial.put(newMaterialNum, mat);
            } else {
                throw new IllegalStateException("Meta Overlap detected.");
            }
        }

        return mat.getStackSrc();
    }

    public void registerOredicts() {
        for (final TJAE2MaterialType mt : ImmutableSet.copyOf(this.dmgToMaterial.values())) {
            if (mt.getOreName() != null) {
                final String[] names = mt.getOreName().split(",");

                for (final String name : names) {
                    OreDictionary.registerOre(name, mt.stack(1));
                }
            }
        }
    }

    @Override
    @Nonnull
    public String getTranslationKey(final ItemStack is) {
        return "item.appliedenergistics2.material." + this.nameOf(is).toLowerCase();
    }

    @Override
    protected void getCheckedSubItems(final CreativeTabs creativeTab, final NonNullList<ItemStack> itemStacks) {
        final List<TJAE2MaterialType> types = Arrays.asList(TJAE2MaterialType.values());
        Collections.sort(types, (o1, o2) -> o1.name().compareTo(o2.name()));

        for (final TJAE2MaterialType mat : types) {
            if (mat.getDamageValue() >= 0 && mat.isRegistered() && mat.getItemInstance() == this) {
                itemStacks.add(new ItemStack(this, 1, mat.getDamageValue()));
            }
        }
    }

    @Override
    public EnumActionResult onItemUseFirst(final EntityPlayer player, final World world, final BlockPos pos, final EnumFacing side, final float hitX, final float hitY, final float hitZ, final EnumHand hand) {
        if (player.isSneaking()) {
            final TileEntity te = world.getTileEntity(pos);
            IItemHandler upgrades = null;

            if (te instanceof IPartHost) {
                final SelectedPart sp = ((IPartHost) te).selectPart(new Vec3d(hitX, hitY, hitZ));
                if (sp.part instanceof IUpgradeableHost) {
                    upgrades = ((ISegmentedInventory) sp.part).getInventoryByName("upgrades");
                }
            } else if (te instanceof IUpgradeableHost) {
                upgrades = ((ISegmentedInventory) te).getInventoryByName("upgrades");
            }

            if (upgrades != null && !player.getHeldItem(hand).isEmpty() && player.getHeldItem(hand).getItem() instanceof IUpgradeModule) {
                final IUpgradeModule um = (IUpgradeModule) player.getHeldItem(hand).getItem();
                final Upgrades u = um.getType(player.getHeldItem(hand));

                if (u != null) {
                    if (player.world.isRemote) {
                        return EnumActionResult.PASS;
                    }

                    final InventoryAdaptor ad = new AdaptorItemHandler(upgrades);
                    player.setHeldItem(hand, ad.addItems(player.getHeldItem(hand)));
                    return EnumActionResult.SUCCESS;
                }
            }
        }

        return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
    }

    @Override
    public boolean hasCustomEntity(final ItemStack is) {
        return this.getTypeByStack(is).hasCustomEntity();
    }

    @Override
    public Entity createEntity(final World w, final Entity location, final ItemStack itemstack) {
        final Class<? extends Entity> droppedEntity = this.getTypeByStack(itemstack).getCustomEntityClass();
        final Entity eqi;

        try {
            eqi = droppedEntity.getConstructor(World.class, double.class, double.class, double.class, ItemStack.class)
                    .newInstance(w, location.posX,
                            location.posY, location.posZ, itemstack);
        } catch (final Throwable t) {
            throw new IllegalStateException(t);
        }

        eqi.motionX = location.motionX;
        eqi.motionY = location.motionY;
        eqi.motionZ = location.motionZ;

        if (location instanceof EntityItem && eqi instanceof EntityItem) {
            ((EntityItem) eqi).setDefaultPickupDelay();
        }

        return eqi;
    }

    private String nameOf(final ItemStack is) {
        if (is.isEmpty()) {
            return "null";
        }

        final TJAE2MaterialType mt = this.getTypeByStack(is);
        if (mt == null) {
            return "null";
        }

        return mt.name();
    }

    @Override
    public int getBytes(final ItemStack is) {
        switch (this.getTypeByStack(is)) {
            case CELL_65M_PART:
                return KILO_SCALAR * 65_536;
            case CELL_262M_PART:
                return KILO_SCALAR * 262_144;
            case CELL_1048M_PART:
                return KILO_SCALAR * 1_048_576;
            case ITEM_CELL_DIGITAL_SINGULARITY:
                return Integer.MAX_VALUE;
            default: return 0;
        }
    }

    @Override
    public boolean isStorageComponent(final ItemStack is) {
        switch (this.getTypeByStack(is)) {
            case CELL_65M_PART:
            case CELL_262M_PART:
            case CELL_1048M_PART:
            case ITEM_CELL_DIGITAL_SINGULARITY:
                return true;
            default: return false;
        }
    }

    @Override
    public boolean isEditable(ItemStack is) {
        return is.isItemEqual(AEApi.instance().definitions().materials().cardMagnet().maybeStack(1).get());
    }

    @Override
    public IItemHandler getUpgradesInventory(final ItemStack is) {
        return new CellUpgrades(is, 2);
    }

    @Override
    public IItemHandler getConfigInventory(final ItemStack is) {
        return new CellConfig(is);
    }

    @Override
    public FuzzyMode getFuzzyMode(final ItemStack is) {
        final String fz = Platform.openNbtData(is).getString("FuzzyMode");
        try {
            return FuzzyMode.valueOf(fz);
        } catch (final Throwable t) {
            return FuzzyMode.IGNORE_ALL;
        }
    }

    @Override
    public void setFuzzyMode(final ItemStack is, final FuzzyMode fzMode) {
        Platform.openNbtData(is).setString("FuzzyMode", fzMode.name());
    }

    private static class SlightlyBetterSort implements Comparator<String> {
        private final Pattern pattern;

        public SlightlyBetterSort(final Pattern pattern) {
            this.pattern = pattern;
        }

        @Override
        public int compare(final String o1, final String o2) {
            try {
                final Matcher a = this.pattern.matcher(o1);
                final Matcher b = this.pattern.matcher(o2);
                if (a.find() && b.find()) {
                    final int ia = Integer.parseInt(a.group(1));
                    final int ib = Integer.parseInt(b.group(1));
                    return Integer.compare(ia, ib);
                }
            } catch (final Throwable t) {
                // ek!
            }
            return o1.compareTo(o2);
        }
    }
}
