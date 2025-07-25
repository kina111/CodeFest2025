package util;

import jsclub.codefest.sdk.model.Element;
import jsclub.codefest.sdk.model.ElementType;
import jsclub.codefest.sdk.model.armors.Armor;
import jsclub.codefest.sdk.model.effects.Effect;
import jsclub.codefest.sdk.model.healing_items.HealingItem;
import jsclub.codefest.sdk.model.weapon.Weapon;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemStatComparator {

    //==COMPARE WEAPON==
    private static final Map<String, Integer> compareMelee;
    private static final Map<String, Integer> compareGun;
    private static final Map<String, Integer> compareThrowable;
    private static final Map<String, Integer> compareSpecial;
    //COMPARE HEALING-ITEM
    private static final Map<String, Integer> compareHealingItem;
    //COMPARE ARMOR
    private static final Map<String, Integer> compareArmor;

    //độ ưu tiên càng thấp càng khỏe.
    static{
        compareMelee = new HashMap<>();
        compareGun = new HashMap<>();
        compareThrowable = new HashMap<>();
        compareSpecial = new HashMap<>();
        compareHealingItem = new HashMap<>();
        compareArmor = new HashMap<>();
        compareMelee.put("MACE", 1); compareMelee.put("KNIFE", 2); compareMelee.put("TREE_BRANCH", 2); compareMelee.put("AXE", 2); compareMelee.put("BONE", 3); compareMelee.put("HAND", 4);
        compareGun.put("SHOTGUN", 1); compareGun.put("CROSSBOW", 2); compareGun.put("SCEPTER", 2); compareGun.put("RUBBER_GUN", 3);
        compareThrowable.put("CRYSTAL", 1); compareThrowable.put("METEORITE_FRAGMENT", 2); compareThrowable.put("BANANA", 2); compareThrowable.put("SEED", 3); compareThrowable.put("SMOKE", 4);
        compareSpecial.put("BELL", 1); compareSpecial.put("SAHUR_BAT", 2); compareSpecial.put("ROPE", 2);
        compareHealingItem.put("ELIXIR_OF_LIFE", 1); compareHealingItem.put("COMPASS", 2); compareHealingItem.put("MAGIC", 3); compareHealingItem.put("UNICORN_BLOOD", 3); compareHealingItem.put("PHOENIX_FEATHERS", 4); compareHealingItem.put("MERMAID_TAIL", 5); compareHealingItem.put("SPIRIT_TEAR", 6); compareHealingItem.put("GOD_LEAF", 7); compareHealingItem.put("ELIXIR", 8);
        compareArmor.put("MAGIC_ARMOR", 1); compareArmor.put("MAGIC_HELMET", 2); compareArmor.put("ARMOR", 3); compareArmor.put("WOODEN_HELMET", 4);
    }
    // ====== VŨ KHÍ ======
    public static boolean canChangeWeapon(Weapon newW, Weapon oldW) {
        if (newW == null) return false;
        if (oldW == null) return true;

        ElementType commonType = newW.getType();
        return switch (commonType){
            case ElementType.GUN -> canChangeGun(newW, oldW);
            case ElementType.SPECIAL -> canChangeSpecial(newW, oldW);
            case ElementType.MELEE -> canChangeMelee(newW, oldW);
            case ElementType.THROWABLE -> canChangeThrowable(newW, oldW);
            default -> false;
        };
    }

    public static boolean canChangeMelee(Weapon newItem, Weapon oldItem) {
        if (newItem == null) return false;
        if (oldItem == null) return true;
        return compareMelee.get(newItem.getId()) <= compareMelee.get(oldItem.getId());
    }
    public static boolean canChangeGun(Weapon newItem, Weapon oldItem) {
        if (newItem == null) return false;
        if (oldItem == null) return true;
        return compareGun.get(newItem.getId()) <= compareGun.get(oldItem.getId());
    }
    public static boolean canChangeThrowable(Weapon newItem, Weapon oldItem) {
        if (newItem == null) return false;
        if (oldItem == null) return true;
        return compareThrowable.get(newItem.getId()) <= compareThrowable.get(oldItem.getId());
    }
    public static boolean canChangeSpecial(Weapon newItem, Weapon oldItem) {
        if (newItem == null) return false;
        if (oldItem == null) return true;
        return compareSpecial.get(newItem.getId()) <= compareSpecial.get(oldItem.getId());
    }

    // ====== GIÁP & MŨ ======
    public static boolean isBetterArmor(Armor newArmor, Armor oldArmor) {
        if (newArmor == null) return false;
        if (oldArmor == null) return true;

        return compareArmor.get(newArmor.getId()) < compareArmor.get(oldArmor.getId());
    }

    // ====== HỒI MÁU ======
    public static boolean isBetterHealingItem(HealingItem newItem, HealingItem oldItem) {
        if (newItem == null) return false;
        if (oldItem == null) return true;
        return compareHealingItem.get(newItem.getId()) < compareHealingItem.get(oldItem.getId());
    }

    // ====== GỘP TẤT CẢ ======
    public static boolean isBetterItem(Element newItem, Element currentItem) {
        if (newItem == null) return false;
        if (currentItem == null) return true;

        ElementType type = newItem.getType();

        try {
            switch (type) {
                case GUN:
                case MELEE:
                case THROWABLE:
                case SPECIAL:
                    System.out.println("🎒 Comparing Weapon" + newItem.getId() + "\n" + currentItem.getId());
                    System.out.println("🎒 Is Better weapon: " + canChangeWeapon((Weapon) newItem, (Weapon) currentItem));
                    return canChangeWeapon((Weapon) newItem, (Weapon) currentItem);

                case ARMOR:
                case HELMET:
                    System.out.println("🎒 Comparing Armor" + newItem.getId() + "\n" + currentItem.getId());
                    System.out.println("🎒 Is Better Armor: " + isBetterArmor((Armor) newItem, (Armor) currentItem));

                    return isBetterArmor((Armor) newItem, (Armor) currentItem);

                case HEALING_ITEM:
                    System.out.println("🎒 Comparing HealingItem: " + newItem.getId() + "\n" + currentItem.getId());
                    System.out.println("🎒 Is Better HealingItem: " +  isBetterHealingItem((HealingItem) newItem, (HealingItem) currentItem));

                    return isBetterHealingItem((HealingItem) newItem, (HealingItem) currentItem);

                default:
                    return false;
            }
        } catch (ClassCastException e) {
            System.err.println("❗ Wrong element casting: " + currentItem.getType() + ">>>><<<<" + newItem.getType());
            return false;
        }
    }

}
