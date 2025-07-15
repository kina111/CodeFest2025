package util;

import jsclub.codefest.sdk.model.Element;
import jsclub.codefest.sdk.model.ElementType;
import jsclub.codefest.sdk.model.armors.Armor;
import jsclub.codefest.sdk.model.support_items.SupportItem;
import jsclub.codefest.sdk.model.weapon.Weapon;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemStatComparator {

    // ==COMPARE WEAPON==
    private static final Map<String, Integer> compareMelee;
    private static final Map<String, Integer> compareGun;
    private static final Map<String, Integer> compareThrowable;
    private static final Map<String, Integer> compareSpecial;
    // COMPARE HEALING-ITEM
    private static final Map<String, Integer> compareSupportItem;
    // COMPARE ARMOR
    private static final Map<String, Integer> compareArmor;

    // độ ưu tiên càng thấp càng khỏe.
    static {
        compareMelee = new HashMap<>();
        compareGun = new HashMap<>();
        compareThrowable = new HashMap<>();
        compareSpecial = new HashMap<>();
        compareSupportItem = new HashMap<>();
        compareArmor = new HashMap<>();
        compareMelee.put("AXE", 2);
        compareMelee.put("KNIFE", 1);
        compareMelee.put("TREE_BRANCH", 3);
        compareMelee.put("MACE", 3);
        compareMelee.put("BONE", 3);
        compareMelee.put("HAND", 4);
        compareGun.put("SHOTGUN", 1);
        compareGun.put("CROSSBOW", 2);
        compareGun.put("SCEPTER", 3);
        compareGun.put("RUBBER_GUN", 4);
        compareThrowable.put("CRYSTAL", 1);
        compareThrowable.put("BANANA", 2);
        compareThrowable.put("METEORITE_FRAGMENT", 2);
        compareThrowable.put("SEED", 3);
        compareThrowable.put("SMOKE", 4);
        compareSpecial.put("BELL", 1);
        compareSpecial.put("ROPE", 2);
        compareSpecial.put("SAHUR_BAT", 3);
        compareSupportItem.put("ELIXIR_OF_LIFE", 1);
        compareSupportItem.put("UNICORN_BLOOD", 2);
        compareSupportItem.put("MAGIC", 3);
        compareSupportItem.put("PHOENIX_FEATHERS", 4);
        compareSupportItem.put("MERMAID_TAIL", 5);
        compareSupportItem.put("COMPASS", 6);
        compareSupportItem.put("SPIRIT_TEAR", 6);
        compareSupportItem.put("GOD_LEAF", 7);
        compareSupportItem.put("ELIXIR", 8);
        compareArmor.put("MAGIC_ARMOR", 1);
        compareArmor.put("MAGIC_HELMET", 2);
        compareArmor.put("ARMOR", 3);
        compareArmor.put("WOODEN_HELMET", 4);
    }

    // ====== VŨ KHÍ ======
    public static boolean canChangeWeapon(Weapon newW, Weapon oldW) {
        if (newW == null)
            return false;
        if (oldW == null)
            return true;

        ElementType commonType = newW.getType();
        return switch (commonType) {
            case GUN -> canChangeGun(newW, oldW);
            case SPECIAL -> canChangeSpecial(newW, oldW);
            case MELEE -> canChangeMelee(newW, oldW);
            case THROWABLE -> canChangeThrowable(newW, oldW);
            default -> false;
        };
    }

    public static boolean canChangeMelee(Weapon newItem, Weapon oldItem) {
        if (newItem == null)
            return false;
        if (oldItem == null)
            return true;
        return compareMelee.get(newItem.getId()) <= compareMelee.get(oldItem.getId());
    }

    public static boolean canChangeGun(Weapon newItem, Weapon oldItem) {
        if (newItem == null)
            return false;
        if (oldItem == null)
            return true;
        return compareGun.get(newItem.getId()) <= compareGun.get(oldItem.getId());
    }

    public static boolean canChangeThrowable(Weapon newItem, Weapon oldItem) {
        if (newItem == null)
            return false;
        if (oldItem == null)
            return true;
        return compareThrowable.get(newItem.getId()) <= compareThrowable.get(oldItem.getId());
    }

    public static boolean canChangeSpecial(Weapon newItem, Weapon oldItem) {
        if (newItem == null)
            return false;
        if (oldItem == null)
            return true;
        return compareSpecial.get(newItem.getId()) <= compareSpecial.get(oldItem.getId());
    }

    // ====== GIÁP & MŨ ======
    public static boolean isBetterArmor(Armor newArmor, Armor oldArmor) {
        if (newArmor == null)
            return false;
        if (oldArmor == null)
            return true;

        return compareArmor.get(newArmor.getId()) < compareArmor.get(oldArmor.getId());
    }

    // ====== HỒI MÁU ======
    public static boolean isBetterSupportItem(SupportItem newItem, SupportItem oldItem) {
        if (newItem == null)
            return false;
        if (oldItem == null)
            return true;
        return compareSupportItem.get(newItem.getId()) < compareSupportItem.get(oldItem.getId());
    }

    // ====== GỘP TẤT CẢ ======
    public static boolean isBetterItem(Element newItem, Element currentItem) {
        if (newItem == null)
            return false;
        if (currentItem == null)
            return true;

        ElementType type = newItem.getType();

        try {
            switch (type) {
                case GUN:
                case MELEE:
                case THROWABLE:
                case SPECIAL:
                    System.out.println("🎒 Comparing Weapon" + newItem.getId() + "\n" + currentItem.getId());
                    System.out
                            .println("🎒 Is Better weapon: " + canChangeWeapon((Weapon) newItem, (Weapon) currentItem));
                    return canChangeWeapon((Weapon) newItem, (Weapon) currentItem);

                case ARMOR:
                case HELMET:
                    System.out.println("🎒 Comparing Armor" + newItem.getId() + "\n" + currentItem.getId());
                    System.out.println("🎒 Is Better Armor: " + isBetterArmor((Armor) newItem, (Armor) currentItem));

                    return isBetterArmor((Armor) newItem, (Armor) currentItem);

                case SUPPORT_ITEM:
                    System.out.println("🎒 Comparing SupportItem: " + newItem.getId() + "\n" + currentItem.getId());
                    System.out.println("🎒 Is Better SupportItem: "
                            + isBetterSupportItem((SupportItem) newItem, (SupportItem) currentItem));

                    return isBetterSupportItem((SupportItem) newItem, (SupportItem) currentItem);

                default:
                    return false;
            }
        } catch (ClassCastException e) {
            System.err.println("❗ Wrong element casting: " + currentItem.getType() + ">>>><<<<" + newItem.getType());
            return false;
        }
    }

}
