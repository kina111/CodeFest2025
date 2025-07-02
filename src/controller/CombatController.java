package controller;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.algorithm.PathUtils;
import jsclub.codefest.sdk.base.Node;
import jsclub.codefest.sdk.model.ElementType;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.Inventory;
import jsclub.codefest.sdk.model.players.Player;
import jsclub.codefest.sdk.model.weapon.Weapon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CombatController {

    private final Hero hero;
    private final Inventory inventory;
    private final InventoryController inventoryController;

    public CombatController(Hero hero) {
        this.hero = hero;
        this.inventory = hero.getInventory();
        this.inventoryController = new InventoryController(hero.getInventory());
    }

    public boolean engageNearestEnemy() throws IOException {
        GameMap gameMap = hero.getGameMap();
        Player me = gameMap.getCurrentPlayer();

        Player target = getNearestPlayer(gameMap);
        if (target == null) {
            System.out.println("🎒 No enemies found.");
            return false;
        }

        int dx = me.getX() - target.getX();
        int dy = me.getY() - target.getY();
        int distance = Math.abs(dx) + Math.abs(dy);

        Weapon best = getBestWeapon(dx, dy);
        System.out.println("🎒 Best weapon is: " + best.getId());

        String direction = null;
        if (canUse(best, dx, dy)) direction = getDirection(best, dx, dy);

        if (direction != null) {
            useWeapon(best, direction, distance);
            return false;
        } else {
            System.out.println("🎒 Moving toward enemy.");
            moveTowards(target);
        }

        return true;
    }

    public Weapon getBestWeapon(int difInX, int difInY) {
        Weapon best = null;
        //ưu tiên Melee nếu trong tầm ngắn
        if (isArmed() && canUse(inventory.getMelee(), difInX, difInY)) best = inventory.getMelee();
        else if (canUse(inventory.getGun(), difInX, difInY)) best = inventory.getGun();
        else if (canUse(inventory.getThrowable(), difInX, difInY)) best =  inventory.getThrowable();
        else if (canUse(inventory.getSpecial(), difInX, difInY)) best =  inventory.getSpecial();
        else best = inventory.getMelee();

        System.out.println("🎒 Choosing: " + best.getId().toUpperCase() + " to fight!!");
        return best;//only HAND
    }

    private void useWeapon(Weapon weapon, String direction, int distance) throws IOException {
        ElementType type = weapon.getType();
        if (type == ElementType.GUN) {
            hero.shoot(direction);
        } else if (type == ElementType.THROWABLE) {
            hero.throwItem(direction, distance);
        } else if (type == ElementType.SPECIAL) {
            hero.useSpecial(direction);
        } else {
            hero.attack(direction);
        }
    }
    //check if can use Weapon
    private boolean canUse(Weapon weapon, int difInX, int difInY) {
        if (weapon == null) return false;
        //special case with x * y (with x differ 1)
        int dx = Math.abs(difInX), dy = Math.abs(difInY);
        return switch (weapon.getId()) {
            case "HAND" -> (dx == 0 && dy == 1) || (dx == 1 && dy == 0);
            case "KNIFE", "TREE_BRANCH", "AXE" -> dx <= 1 && dy <= 1;
            case "MACE" -> dx <= 2 && dy <= 2;
            case "BELL" -> dx <= 6 && dy <= 6;
            //normal cases
            default -> (dx == 0 && dy <= weapon.getRange()) || (dy == 0 && dx <= weapon.getRange());
        };

    }

    private String getDirection(Weapon weapon, int difInX, int difInY) {
        //special cases
        if (weapon == null) return null;
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
                if (difInX < 0 && difInY == 0) return "r";
                if (difInX > 0 && difInY == 0) return "l";
                if (difInX == 0 && difInY < 0) return "u";
                if (difInX == 0 && difInY > 0) return "d";
            }
        }
        return null;
    }

    private void moveTowards(Player target) throws IOException {
        GameMap gameMap = hero.getGameMap();
        Player me = gameMap.getCurrentPlayer();

        Node from = new Node(me.getX(), me.getY());
        Node to = new Node(target.getX(), target.getY());

        String path = PathUtils.getShortestPath(gameMap, getNodesToAvoid(gameMap), from, to, true);
        if (!path.isEmpty()) {
            hero.move(path.substring(0, 1));
        }
    }
    //check has weapon already
    public boolean isArmed() {
        return inventory.getGun() != null
                || inventory.getThrowable() != null
                || inventory.getSpecial() != null
                || (inventory.getMelee() != null && !"HAND".equals(inventory.getMelee().getId()));
    }

    public Player getNearestPlayer(GameMap gameMap) {
        Player me = gameMap.getCurrentPlayer();
        List<Player> others = gameMap.getOtherPlayerInfo();

        Player nearest = null;
        int minDistance = Integer.MAX_VALUE;

        for (Player p : others) {
            int distance = PathUtils.distance(me, p);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = p;
            }
        }

        return nearest;
    }

    private List<Node> getNodesToAvoid(GameMap gameMap) {
        List<Node> nodes = new ArrayList<>(gameMap.getListIndestructibles());
        nodes.removeAll(gameMap.getObstaclesByTag("CAN_GO_THROUGH"));
        nodes.addAll(gameMap.getListTraps());
        nodes.addAll(gameMap.getOtherPlayerInfo());
        return nodes;
    }
}