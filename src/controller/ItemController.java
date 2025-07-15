package controller;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.algorithm.PathUtils;
import jsclub.codefest.sdk.base.Node;
import jsclub.codefest.sdk.model.Element;
import jsclub.codefest.sdk.model.ElementType;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.players.Player;
import jsclub.codefest.sdk.model.support_items.SupportItem;
import jsclub.codefest.sdk.model.weapon.Weapon;
import service.InventoryService;
import util.ItemStatComparator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ItemController {

    private final Hero hero;
    private final InventoryService inventoryService;

    public ItemController(Hero hero) {
        this.hero = hero;
        this.inventoryService = new InventoryService(hero.getInventory());
    }

    public boolean handleSearchAroundItems(int radius, List<Node> nodesToAvoid) throws IOException {
        //tìm item
        Element item = searchBetterItemAround(radius);
        //nếu đổi đồ k có tác dụng gì
        if (item == null) return false;

        //nếu đổi đồ có thể thêm điểm hoặc mạnh hơn
        GameMap gameMap = hero.getGameMap();
        String pathToItem = PathUtils.getShortestPath(gameMap, nodesToAvoid, gameMap.getCurrentPlayer(), item, true);
        if (pathToItem == null) return false;

        Element currentElement = inventoryService.getElementByType(item.getType());
        //nếu đang full đồ trong Inventory, sử dụng luôn hoặc vứt đi
        if (currentElement != null && !currentElement.getId().equals("HAND")){
            System.out.println("🎒🎒 UsedItem: " + currentElement.getId());
            if (currentElement.getType() == ElementType.SUPPORT_ITEM){
                SupportItem currentSupportItem = (SupportItem) currentElement;
                hero.useItem(currentSupportItem.getId());
                System.out.println("🎒 Using SupportItem: " + currentElement.getId() + "and get " + currentSupportItem.getPoint() + "points!!");
            }
            else if (currentElement.getType() == ElementType.SPECIAL) {
                Weapon currentSpecial = (Weapon) currentElement;
                hero.revokeItem(currentSpecial.getId());
                System.out.println("🎒 Revoking SpecialItem: " + currentElement.getId());
            }
            else if (currentElement.getType() == ElementType.THROWABLE){
                Weapon currentWeapon = (Weapon) currentElement;
                hero.revokeItem(currentWeapon.getId());
                System.out.println("🎒 Revoking ThrowableItem: " + currentElement.getId());
            }
            else {
                hero.revokeItem(currentElement.getId());
                System.out.println("🎒 Revoking" + currentElement.getId());
            }
            //nếu đang đứng tại vị trí item
        } else if (pathToItem.isEmpty()) {
            hero.pickupItem();
            System.out.println("🎒 Looting " + item.getType() + " --- " + item.getId());
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

        List<Node> nearbyNodes = new ArrayList<>();

        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                int nx = px + dx;
                int ny = py + dy;

                if (nx < 0 || ny < 0 || nx >= mapSize || ny >= mapSize) continue;
                //if (dx == 0 && dy == 0) continue; // bỏ qua ô hiện tại

                nearbyNodes.add(new Node(nx, ny));
            }
        }

        // Sắp xếp theo khoảng cách đến người chơi
        nearbyNodes.sort(Comparator.comparingDouble(a -> PathUtils.distance(player, a)));

        for (Node node : nearbyNodes) {
            Element foundItem = gameMap.getElementByIndex(node.getX(), node.getY());
            if (foundItem == null) continue;

            ElementType type = foundItem.getType();
            if (isPickable(type)) {
                Element currentItem = inventoryService.getElementByType(type);
                if (ItemStatComparator.isBetterItem(foundItem, currentItem)) {
                    return foundItem;
                }
            }
        }

        return null;
    }

    private boolean isPickable(ElementType type) {
        return type == ElementType.GUN ||
                type == ElementType.MELEE ||
                type == ElementType.THROWABLE ||
                type == ElementType.SPECIAL ||
                type == ElementType.ARMOR ||
                type == ElementType.HELMET ||
                type == ElementType.SUPPORT_ITEM;
    }

    public boolean handleSearchForGun(List<Node> nodesToAvoid) throws IOException {
        System.out.println("No gun found. Searching for a gun.");
        GameMap gameMap = hero.getGameMap();
        String pathToGun = findPathToGun(nodesToAvoid);

        if (pathToGun != null) {
            if (pathToGun.isEmpty()) {
                hero.pickupItem();
            } else {
                hero.move(pathToGun);
            }
            return true;
        }
        return false;
    }

    public String findPathToGun(List<Node> nodesToAvoid) {
        GameMap gameMap = hero.getGameMap();
        Player player = gameMap.getCurrentPlayer();
        Weapon nearestGun = getNearestGun();
        return nearestGun == null ? null : PathUtils.getShortestPath(gameMap, nodesToAvoid, player, nearestGun, true);
    }

    public Weapon getNearestGun() {
        GameMap gameMap = hero.getGameMap();
        Player player = gameMap.getCurrentPlayer();
        List<Weapon> guns = gameMap.getAllGun();

        Weapon nearestGun = null;
        double minDistance = Double.MAX_VALUE;

        for (Weapon gun : guns) {
            if (!PathUtils.checkInsideSafeArea(gun, gameMap.getSafeZone(), gameMap.getMapSize())) continue;
            double distance = PathUtils.distance(player, gun);
            if (distance < minDistance || (!nearestGun.getId().equals("SHOTGUN") && gun.getId().equals("SHOTGUN") && distance < 2 * minDistance)) {
                minDistance = distance;
                nearestGun = gun;
            }
        }
        return nearestGun;
    }
}
