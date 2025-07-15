package controller;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.algorithm.PathUtils;
import jsclub.codefest.sdk.base.Node;
import jsclub.codefest.sdk.factory.WeaponFactory;
import jsclub.codefest.sdk.model.Element;
import jsclub.codefest.sdk.model.ElementType;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.Inventory;
import jsclub.codefest.sdk.model.players.Player;
import jsclub.codefest.sdk.model.weapon.Weapon;
import service.InventoryService;
import service.WeaponService;
import util.ItemStatComparator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CombatController {

    private final Hero hero;
    private final Inventory inventory;
    private final InventoryService inventoryService;
    private final WeaponService weaponService = WeaponService.getInstance();

    public CombatController(Hero hero) {
        this.hero = hero;
        this.inventory = hero.getInventory();
        this.inventoryService = new  InventoryService(hero.getInventory());
    }

    public boolean engageNearestEnemy(List<Node> nodesToAvoid) throws IOException {
        GameMap gameMap = hero.getGameMap();
        Player me = gameMap.getCurrentPlayer();
        long currentStep = gameMap.getStepNumber();

        Player target = getNearestPlayer();
        Weapon best = getBestWeaponCanUse(me, target, currentStep);

        if (best != null) {
            System.out.println("✅ Using weapon: " + best.getId());
            String direction = getDirection(best, me, target);
            if (best.getId().equals("SHOTGUN")){
                if (inventoryService.isOnlyShotgunAndThrow()) weaponService.markUsed(best.getId(), currentStep);
                System.out.println("Using shotgun: " + inventoryService.isOnlyShotgunAndThrow());
            }else{
                weaponService.markUsed(best.getId(), currentStep);
            }
            useWeapon(best, direction);
        } else {
            System.out.println("➡️ Moving toward enemy.");
            moveTowards(target, nodesToAvoid);
        }
        return  true;
    }


    public Weapon getBestWeaponCanUse(Player player, Player target, long currentStep) {
        //special case: nếu có thể kết liễu bằng melee, tránh tốn đạn
        if (target.getHealth() <= 0) return null;
        Weapon handOnly = WeaponFactory.getWeaponById("HAND");
        Weapon[] priority = {
                inventory.getMelee(),
                inventory.getGun(),
                inventory.getThrowable(),
                inventory.getSpecial(),
                handOnly
        };

        for (int i = 0; i < 5; i++) {
            Weapon weapon = priority[i];
            if (weapon == null || (i == 0 && weapon.getId().equals("HAND"))) continue;
            boolean usable = canUse(weapon, player, target) &&
                    weaponService.canUse(weapon, currentStep);

            if (usable) {
                System.out.println("🎯 Weapon chosen: " + weapon.getId());
                return weapon;
            }
        }

        return null;
    }

    private void useWeapon(Weapon weapon, String direction) throws IOException {
        ElementType type = weapon.getType();
        switch (type) {
            case GUN -> hero.shoot(direction);
            case THROWABLE -> hero.throwItem(direction);
            case SPECIAL -> hero.useSpecial(direction);
            default -> hero.attack(direction);
        }
    }

    private boolean canUse(Weapon weapon, Player player, Player target) {
        if (weapon == null) return false;

        int dx = Math.abs(player.getX() - target.getX());
        int dy = Math.abs(player.getY() - target.getY());

        String id = weapon.getId();
        int range = weapon.getRange().length > 1 ? weapon.getRange()[1] : 1;

        return switch (id) {
            case "HAND", "SHOTGUN" -> (dx == 1 && dy == 0) || (dx == 0 && dy == 1);
            case "KNIFE", "TREE_BRANCH", "AXE" -> dx <= 1 && dy <= 1;
            case "MACE" -> dx <= 2 && dy <= 2;
            case "BELL" -> dx <= 6 && dy <= 6;
            case "COMPASS" -> dx <= 8 && dy <= 8;
            case "BANANA", "METEORITE_FRAGMENT", "CRYSTAL", "SEED", "SMOKE" ->
                    (dx <= 1 && Math.abs(dy - range) <= 1) || (dy <= 1 && Math.abs(dx - range) <= 1);
            default -> isStraightLineAttackPossible(player, target, range);
        };
    }

    private boolean isStraightLineAttackPossible(Player player, Player target, int range) {
        GameMap gameMap = hero.getGameMap();

        int px = player.getX(), py = player.getY();
        int tx = target.getX(), ty = target.getY();

        if (px == tx && Math.abs(py - ty) <= range) {
            for (int y = Math.min(py, ty) + 1; y < Math.max(py, ty); y++) {
                Element temp = gameMap.getElementByIndex(px, y);
                if (temp != null && "INDESTRUCTIBLE".equals(temp.getId())) return false;
            }
            return true;
        } else if (py == ty && Math.abs(px - tx) <= range) {
            for (int x = Math.min(px, tx) + 1; x < Math.max(px, tx); x++) {
                Element temp = gameMap.getElementByIndex(x, py);
                if (temp != null && "INDESTRUCTIBLE".equals(temp.getId())) return false;
            }
            return true;
        }

        return false;
    }


    private String getDirection(Weapon weapon, Player player, Player target) {
        //special cases
        if (weapon == null) return null;
        int difInX = player.getX() - target.getX();
        int difInY = player.getY() - target.getY();
        String direction = null;
        switch (weapon.getId()) {
            case "KNIFE", "TREE_BRANCH", "MACE", "AXE", "HAND"-> {
                if (difInX < 0) return "r";
                else if (difInX > 0) return "l";
                else if (difInY < 0) return "u";
                return "d";
            }
            case "BELL" -> {
                return "r";
            }
            default -> {
                int dx = Math.abs(difInX), dy = Math.abs(difInY);
                if (difInX < 0 && dy <= 1) return "r";
                if (difInX > 0 && dy <= 1) return "l";
                if (dx <= 1 && difInY < 0) return "u";
                if (dx <= 1 && difInY > 0) return "d";
            }
        }
        return direction;
    }

    private void moveTowards(Player target,  List<Node> nodesToAvoid) throws IOException {
        GameMap gameMap = hero.getGameMap();
        Player me = gameMap.getCurrentPlayer();
        Node from = new Node(me.getX(), me.getY());
        Node to = new Node(target.getX(), target.getY());

        String path = PathUtils.getShortestPath(gameMap, nodesToAvoid, from, to, true);
        if (path != null) {
            hero.move(path.substring(0, 1));
        }
    }

    public boolean isArmed() {
        return inventory.getGun() != null
                || inventory.getThrowable() != null
                || inventory.getSpecial() != null
                || (inventory.getMelee() != null && !"HAND".equals(inventory.getMelee().getId()));
    }

    public Player getNearestPlayer() {
        GameMap gameMap = hero.getGameMap();
        Player me = gameMap.getCurrentPlayer();
        List<Player> others = gameMap.getOtherPlayerInfo();

        Player nearest = null;
        int minDistance = Integer.MAX_VALUE;

        for (Player p : others) {
            if (p.getHealth() <= 0 || !PathUtils.checkInsideSafeArea(p, gameMap.getSafeZone(), gameMap.getMapSize())) continue;
            int distance = PathUtils.distance(me, p);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = p;
            }
        }

        return nearest;
    }
}
