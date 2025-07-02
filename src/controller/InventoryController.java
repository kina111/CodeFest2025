package controller;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.model.Element;
import jsclub.codefest.sdk.model.ElementType;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.Inventory;
import jsclub.codefest.sdk.model.armors.Armor;
import jsclub.codefest.sdk.model.healing_items.HealingItem;
import jsclub.codefest.sdk.model.players.Player;
import jsclub.codefest.sdk.model.weapon.Weapon;
import util.ItemStatComparator;

import java.util.ArrayList;
import java.util.List;

public class InventoryController {
    private final Inventory inventory;
    private final static int MAX_HEALINGITEM = 4;

    public InventoryController(Inventory inventory) {
        this.inventory = inventory;
    }

    public Element getElementByType(ElementType type){
        return switch (type) {
            case GUN -> inventory.getGun();
            case MELEE -> inventory.getMelee();
            case THROWABLE -> inventory.getThrowable();
            case SPECIAL -> inventory.getSpecial();
            case ARMOR -> inventory.getArmor();
            case HELMET -> inventory.getHelmet();
            case HEALING_ITEM -> getWorstHealingItem();
            default -> null;
        };
    }

    public HealingItem getWorstHealingItem(){
        List<HealingItem> healingItems = inventory.getListHealingItem();
        if (healingItems.size() != MAX_HEALINGITEM) return null;

        HealingItem worst = healingItems.get(0);

        for (HealingItem healingItem : healingItems) {
            if (!ItemStatComparator.isBetterHealingItem(healingItem, worst)) {
                worst = healingItem;
            }
        }
        return worst;
    }


}
