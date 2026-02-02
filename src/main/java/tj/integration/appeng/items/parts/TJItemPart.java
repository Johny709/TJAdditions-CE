package tj.integration.appeng.items.parts;

import appeng.api.AEApi;
import appeng.api.implementations.items.IItemGroup;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.api.util.AEColor;
import appeng.core.features.ActivityState;
import appeng.core.features.ItemStackSrc;
import appeng.items.AEBaseItem;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class TJItemPart extends AEBaseItem implements IPartItem, IItemGroup {
    private static final int INITIAL_REGISTERED_CAPACITY = TJPartType.values().length;
    private static final Comparator<Map.Entry<Integer, PartTypeWithVariant>> REGISTERED_COMPARATOR = new RegisteredComparator();

    public static TJItemPart instance;
    private final Int2ObjectMap<PartTypeWithVariant> registered;

    public TJItemPart() {
        this.registered = new Int2ObjectOpenHashMap<>(INITIAL_REGISTERED_CAPACITY);
        this.setHasSubtypes(true);

        instance = this;
    }

    @Nonnull
    public final ItemStackSrc createPart(final TJPartType mat) {
        Preconditions.checkNotNull(mat);

        return this.createPart(mat, 0);
    }

    @Nonnull
    public ItemStackSrc createPart(final TJPartType mat, final AEColor color) {
        Preconditions.checkNotNull(mat);
        Preconditions.checkNotNull(color);

        final int varID = color.ordinal();

        return this.createPart(mat, varID);
    }

    @Nonnull
    private ItemStackSrc createPart(final TJPartType mat, final int varID) {
        assert mat != null;
        assert varID >= 0;

        // verify
        for (final PartTypeWithVariant p : this.registered.values()) {
            if (p.part == mat && p.variant == varID) {
                throw new IllegalStateException("Cannot create the same material twice...");
            }
        }

        boolean enabled = mat.isEnabled();

        final int partDamage = mat.getBaseDamage() + varID;
        final ActivityState state = ActivityState.from(enabled);
        final ItemStackSrc output = new ItemStackSrc(this, partDamage, state);

        final PartTypeWithVariant pti = new PartTypeWithVariant(mat, varID);

        this.processMetaOverlap(enabled, partDamage, mat, pti);

        return output;
    }

    private void processMetaOverlap(final boolean enabled, final int partDamage, final TJPartType mat, final PartTypeWithVariant pti) {
        assert partDamage >= 0;
        assert mat != null;
        assert pti != null;

        final PartTypeWithVariant registeredPartType = this.registered.get(partDamage);
        if (registeredPartType != null) {
            throw new IllegalStateException("Meta Overlap detected with type " + mat + " and damage " + partDamage + ". Found " + registeredPartType + " there already.");
        }

        if (enabled) {
            this.registered.put(partDamage, pti);
        }
    }

    public int getDamageByType(final TJPartType t) {
        Preconditions.checkNotNull(t);

        for (final Int2ObjectMap.Entry<PartTypeWithVariant> pt : this.registered.int2ObjectEntrySet()) {
            if (pt.getValue().part == t) {
                return pt.getKey();
            }
        }
        return -1;
    }

    @Override
    public EnumActionResult onItemUse(final EntityPlayer player, final World w, final BlockPos pos, final EnumHand hand, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        ItemStack heldItem = player.getHeldItem(hand);
        TJPartType typeByStack = getTypeByStack(heldItem);
        if (typeByStack == TJPartType.INVALID_TYPE) {
            return EnumActionResult.FAIL;
        }

//        if (player.isSneaking() && typeByStack == TJPartType.IDENTITY_ANNIHILATION_PLANE) {
//            ItemStack newPlane = new ItemStack(this, heldItem.getCount(), TJPartType.ANNIHILATION_PLANE.getBaseDamage());
//            newPlane.addEnchantment(Enchantments.SILK_TOUCH,1);
//
//            player.setHeldItem(hand, newPlane);
//            return EnumActionResult.SUCCESS;
//        }

        return AEApi.instance().partHelper().placeBus(player.getHeldItem(hand), pos, side, player, hand, w);
    }

    @Override
    @Nonnull
    public String getTranslationKey(final ItemStack is) {
        Preconditions.checkNotNull(is);
        return "item.appliedenergistics2.multi_part." + this.getTypeByStack(is).getUnlocalizedName().toLowerCase();
    }

    @Override
    public String getItemStackDisplayName(final ItemStack is) {
        final TJPartType pt = this.getTypeByStack(is);

        if (pt.isCable()) {
            final AEColor[] variants = AEColor.values();

            final int itemDamage = is.getItemDamage();
            final PartTypeWithVariant registeredPartType = this.registered.get(itemDamage);
            if (registeredPartType != null) {
                return super.getItemStackDisplayName(is) + " - " + variants[registeredPartType.variant].toString();
            }
        }

        if (pt.getExtraName() != null) {
            return super.getItemStackDisplayName(is) + " - " + pt.getExtraName().getLocal();
        }

        return super.getItemStackDisplayName(is);
    }

    @Override
    protected void getCheckedSubItems(final CreativeTabs creativeTab, final NonNullList<ItemStack> itemStacks) {
        final List<Map.Entry<Integer, PartTypeWithVariant>> types = new ArrayList<>(this.registered.entrySet());
        Collections.sort(types, REGISTERED_COMPARATOR);

        for (final Map.Entry<Integer, PartTypeWithVariant> part : types) {
            itemStacks.add(new ItemStack(this, 1, part.getKey()));
        }
    }

    @Override
    protected void addCheckedInformation(ItemStack stack, World world, List<String> lines, ITooltipFlag advancedTooltips) {
//        if (getTypeByStack(stack) == PartType.ANNIHILATION_PLANE) {
//            Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
//            if (enchantments.isEmpty()) {
//                lines.add(GuiText.CanBeEnchanted.getLocal());
//            }
//            else {
//                lines.add(GuiText.IncreasedEnergyUseFromEnchants.getLocal());
//            }
//        }

//        if (getTypeByStack(stack) == PartType.IDENTITY_ANNIHILATION_PLANE) {
//            lines.add(GuiText.Deprecated.getLocal());
//        }
    }

    @Override
    public int getItemEnchantability() {
        return 10;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false; // getTypeByStack(stack) == PartType.ANNIHILATION_PLANE;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment == Enchantments.UNBREAKING || enchantment == Enchantments.FORTUNE || enchantment == Enchantments.SILK_TOUCH || enchantment == Enchantments.EFFICIENCY;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false; // getTypeByStack(stack) == PartType.ANNIHILATION_PLANE;
    }

    @Nonnull
    public TJPartType getTypeByStack(final ItemStack is) {
        Preconditions.checkNotNull(is);

        final PartTypeWithVariant pt = this.registered.get(is.getItemDamage());
        if (pt != null) {
            return pt.part;
        }

        return TJPartType.INVALID_TYPE;
    }

    @Nullable
    @Override
    public IPart createPartFromItemStack(final ItemStack is) {
        final TJPartType type = this.getTypeByStack(is);
        final Class<? extends IPart> part = type.getPart();
        if (part == null) {
            return null;
        }

        try {
            if (type.getConstructor() == null) {
                type.setConstructor(part.getConstructor(ItemStack.class));
            }

            return type.getConstructor().newInstance(is);
        } catch (final InstantiationException e) {
            throw new IllegalStateException("Unable to construct IBusPart from IBusItem : " + part
                    .getName() + " ; Possibly didn't have correct constructor( ItemStack )", e);
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException("Unable to construct IBusPart from IBusItem : " + part
                    .getName() + " ; Possibly didn't have correct constructor( ItemStack )", e);
        } catch (final InvocationTargetException e) {
            throw new IllegalStateException("Unable to construct IBusPart from IBusItem : " + part
                    .getName() + " ; Possibly didn't have correct constructor( ItemStack )", e);
        } catch (final NoSuchMethodException e) {
            throw new IllegalStateException("Unable to construct IBusPart from IBusItem : " + part
                    .getName() + " ; Possibly didn't have correct constructor( ItemStack )", e);
        }
    }

    public int variantOf(final int itemDamage) {
        final PartTypeWithVariant registeredPartType = this.registered.get(itemDamage);
        if (registeredPartType != null) {
            return registeredPartType.variant;
        }

        return 0;
    }

    @Nullable
    @Override
    public String getUnlocalizedGroupName(final Set<ItemStack> others, final ItemStack is) {
        boolean importBus = false;
        boolean importBusFluids = false;
        boolean exportBus = false;
        boolean exportBusFluids = false;
        boolean group = false;

        final TJPartType u = this.getTypeByStack(is);

//        for (final ItemStack stack : others) {
//            if (stack.getItem() == this) {
//                final TJPartType pt = this.getTypeByStack(stack);
//                switch (pt) {
//                    case IMPORT_BUS:
//                        importBus = true;
//                        if (u == pt) {
//                            group = true;
//                        }
//                        break;
//                    case FLUID_IMPORT_BUS:
//                        importBusFluids = true;
//                        if (u == pt) {
//                            group = true;
//                        }
//                        break;
//                    case EXPORT_BUS:
//                        exportBus = true;
//                        if (u == pt) {
//                            group = true;
//                        }
//                        break;
//                    case FLUID_EXPORT_BUS:
//                        exportBusFluids = true;
//                        if (u == pt) {
//                            group = true;
//                        }
//                        break;
//                    default:
//                }
//            }
//        }

//        if (group && importBus && exportBus && (u == PartType.IMPORT_BUS || u == PartType.EXPORT_BUS)) {
//            return GuiText.IOBuses.getUnlocalized();
//        }
//        if (group && importBusFluids && exportBusFluids && (u == PartType.FLUID_IMPORT_BUS || u == PartType.FLUID_EXPORT_BUS)) {
//            return GuiText.IOBusesFluids.getUnlocalized();
//        }

        return null;
    }

    private static final class PartTypeWithVariant {
        private final TJPartType part;
        private final int variant;

        private PartTypeWithVariant(final TJPartType part, final int variant) {
            assert part != null;
            assert variant >= 0;

            this.part = part;
            this.variant = variant;
        }

        @Override
        public String toString() {
            return "PartTypeWithVariant{" + "part=" + this.part + ", variant=" + this.variant + '}';
        }
    }

    private static final class RegisteredComparator implements Comparator<Map.Entry<Integer, PartTypeWithVariant>> {
        @Override
        public int compare(final Map.Entry<Integer, PartTypeWithVariant> o1, final Map.Entry<Integer, PartTypeWithVariant> o2) {
            final String string1 = o1.getValue().part.name();
            final String string2 = o2.getValue().part.name();
            final int comparedString = string1.compareTo(string2);

            if (comparedString == 0) {
                return Integer.compare(o1.getKey(), o2.getKey());
            }

            return comparedString;
        }
    }

    public void registerOreDicts() {
        for (final PartTypeWithVariant mt : ImmutableSet.copyOf(this.registered.values())) {
            if (mt.part.getOreName() != null) {
                final String[] names = mt.part.getOreName().split(",");

                for (final String name : names) {
                    OreDictionary.registerOre(name, new ItemStack(this, 1, mt.part.getBaseDamage() + mt.variant));
                }
            }
        }
    }
}
