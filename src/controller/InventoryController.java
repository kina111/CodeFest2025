package controller;

import jsclub.codefest.sdk.model.Element;
import jsclub.codefest.sdk.model.ElementType;
import jsclub.codefest.sdk.model.Inventory;
import jsclub.codefest.sdk.model.support_items.SupportItem;
import util.ItemStatComparator;
import java.util.List;

public class InventoryController {
    private final Inventory inventory;
    private final static int MAX_SupportItem = 4;

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
            case SUPPORT_ITEM -> getWorstSupportItem();
            default -> null;
        };
    }

    public SupportItem getWorstSupportItem(){
        List<SupportItem> supportItems = inventory.getListSupportItem();
        if (supportItems.size() < MAX_SupportItem) return null;
        for (SupportItem si : supportItems){
            System.out.println("!!! Having " + si.getId());
        }
        SupportItem worst = supportItems.get(0);

        for (SupportItem supportItem : supportItems) {
            if (!ItemStatComparator.isBetterSupportItem(supportItem, worst)) {
                worst = supportItem;
            }
        }
        return worst;
    }

//    public SupportItem getTheMostAppropriateSupportItem(float maxHP, float currentHP){
//        List<SupportItem> supportItems = inventory.getListSupportItem();
//        if (supportItems == null) return null;
//
//        float missingHP = maxHP - currentHP;
//        float minDiffer = 100;
//
//        SupportItem bestItems = supportItems.get(0);
//        for (SupportItem supportItem : supportItems) {
//            if (supportItem.getId().equals("MAGIC") || supportItem.getId().equals("COMPASS")) {
//                bestItems = supportItem;
//                break;
//            }
//            if (supportItem.getHealingHP())
//        }
//    }
}
