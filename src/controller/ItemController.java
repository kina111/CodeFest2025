package controller;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.algorithm.PathUtils;
import jsclub.codefest.sdk.base.Node;
import jsclub.codefest.sdk.model.Element;
import jsclub.codefest.sdk.model.ElementType;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.healing_items.HealingItem;
import jsclub.codefest.sdk.model.players.Player;
import jsclub.codefest.sdk.model.weapon.Weapon;
import util.ItemStatComparator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ItemController {

    private final Hero hero;
    private final InventoryController inventoryController;

    public ItemController(Hero hero) {
        this.hero = hero;
        this.inventoryController = new InventoryController(hero.getInventory());
    }

    public boolean handleSearchAroundItems(int radius) throws IOException {
        //tìm item
        Element item = searchBetterItemAround(radius);
        //nếu đổi đồ k có tác dụng gì
        if (item == null) return false;

        //nếu đổi đồ có thể thêm điểm hoặc mạnh hơn
        GameMap gameMap = hero.getGameMap();
        String pathToItem = PathUtils.getShortestPath(gameMap, getNodesToAvoid(gameMap), gameMap.getCurrentPlayer(), item, true);
        if (pathToItem == null) return false;

        Element currentElement = inventoryController.getElementByType(item.getType());
        //nếu đang full đồ trong Inventory, sử dụng luôn hoặc vứt đi
        if (currentElement != null && !currentElement.getId().equals("HAND")){
            if (currentElement.getType() == ElementType.HEALING_ITEM){
                HealingItem currentSpecial = (HealingItem) currentElement;
                hero.useItem(currentElement.getId());
                System.out.println("🎒 Using HealingItem: " + currentElement.getId() + "and get " + currentSpecial.getPoint() + "points!!");
            }
            else if (currentElement.getType() == ElementType.SPECIAL) {
                Weapon currentSpecial = (Weapon) currentElement;
                hero.revokeItem(currentSpecial.getId());
                System.out.println("🎒 Using SpecialItem: " + currentElement.getId());
            }
            else if (currentElement.getType() == ElementType.THROWABLE){
                Weapon currentWeapon = (Weapon) currentElement;
                hero.throwItem("l", currentWeapon.getRange());
                System.out.println("🎒 Using ThrowableItem: " + currentElement.getId());
            }
            else {
                hero.revokeItem(currentElement.getId());
                System.out.println("🎒 Revoking" + currentElement.getId());
            }
            //nếu đang đứng tại vị trí item
        } else if (pathToItem.isEmpty()) {
            hero.pickupItem();
            System.out.println("🎒 Looting " + item.getType() + "---" + item.getId());
            //nếu chưa đến nơi
        } else {
            hero.move(pathToItem.substring(0, Math.min(3, pathToItem.length())));
        }
        return true;
    }

    public Element searchBetterItemAround(int radius) throws IOException {
        GameMap gameMap = hero.getGameMap();
        Player player = gameMap.getCurrentPlayer();
        int px = player.getX();
        int py = player.getY();
        int mapSize = gameMap.getMapSize();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                int nx = px + dx;
                int ny = py + dy;

                if (nx < 0 || ny < 0 || nx >= mapSize || ny >= mapSize) continue;

                Element foundItem = gameMap.getElementByIndex(nx, ny);
                if (foundItem == null) continue;

                //get current element in Inventory by type: Weapon, Armor, HealingItem
                ElementType type = foundItem.getType();
                if (isPickable(type)) {
                    Element currentItem = inventoryController.getElementByType(type);
                    if (ItemStatComparator.isBetterItem(foundItem, currentItem)) {
                        return foundItem;
                    }
                }
            }
        }

        return null; // không tìm thấy item tốt hơn
    }

    private boolean isPickable(ElementType type) {
        return type == ElementType.GUN ||
                type == ElementType.MELEE ||
                type == ElementType.THROWABLE ||
                type == ElementType.SPECIAL ||
                type == ElementType.ARMOR ||
                type == ElementType.HELMET ||
                type == ElementType.HEALING_ITEM;
    }

    private List<Node> getNodesToAvoid(GameMap gameMap) {
        List<Node> nodes = new ArrayList<>(gameMap.getListIndestructibles());
        nodes.removeAll(gameMap.getObstaclesByTag("CAN_GO_THROUGH"));
        nodes.addAll(gameMap.getListTraps());
        nodes.addAll(gameMap.getOtherPlayerInfo());
        return nodes;
    }
}
