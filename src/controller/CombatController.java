package controller;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.algorithm.PathUtils;
import jsclub.codefest.sdk.base.Node;
import jsclub.codefest.sdk.model.ElementType;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.Inventory;
import jsclub.codefest.sdk.model.obstacles.ObstacleTag;
import jsclub.codefest.sdk.model.players.Player;
import jsclub.codefest.sdk.model.weapon.Weapon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CombatController {

    private final Hero hero;
    private final Inventory inventory;

    public CombatController(Hero hero) {
        this.hero = hero;
        this.inventory = hero.getInventory();
        InventoryController inventoryController = new InventoryController(hero.getInventory());
    }

    public boolean engageNearestEnemy() throws IOException {
        GameMap gameMap = hero.getGameMap();
        Player me = gameMap.getCurrentPlayer();

        Player target = getNearestPlayer(gameMap);
        if (target == null) {
            System.out.println("🎒 No enemies found.");
            return false;
        }

        Weapon best = getBestWeaponCanUse(me, target);

        if (best != null) {
            System.out.println("🎒 Best weapon is: " + best.getId());
            useWeapon(best, getDirection(best, me, target));
            return true;
        } else {
            System.out.println("🎒 Moving toward enemy.");
            moveTowards(target);
        }

        return false;
    }

    //tìm best weapon có thể sử dụng được, trả về null nếu không có
    public Weapon getBestWeaponCanUse(Player player, Player target) {
        Weapon best = null;
        //ưu tiên Melee nếu trong tầm ngắn
        if (canUse(inventory.getGun(), player, target)) best = inventory.getGun();
        else if (canUse(inventory.getThrowable(),player, target)) best =  inventory.getThrowable();
        else if (canUse(inventory.getSpecial(), player, target)) best =  inventory.getSpecial();
        else if (isArmed() && canUse(inventory.getMelee(), player, target)) best = inventory.getMelee();

        if (best != null) System.out.println("🎒 Choosing: " + best.getId().toUpperCase() + " to fight!!");
        return best;
    }

    private void useWeapon(Weapon weapon, String direction) throws IOException {
        ElementType type = weapon.getType();
        if (type == ElementType.GUN) {
            hero.shoot(direction);
        } else if (type == ElementType.THROWABLE) {
            hero.throwItem(direction);
        } else if (type == ElementType.SPECIAL) {
            hero.useSpecial(direction);
        } else {
            hero.attack(direction);
        }
    }

    //check if can use Weapon
    private boolean canUse(Weapon weapon, Player player, Player target) {
        if (weapon == null) return false;
        int difInX = player.getX() - target.getX();
        int difInY = player.getY() - target.getY();

        int dx = Math.abs(difInX), dy = Math.abs(difInY);
        return switch (weapon.getId()) {
            //case [1,1]
            case "HAND" -> (dx == 0 && dy == 1) || (dx == 1 && dy == 0);
            //case[3,1]
            case "KNIFE", "TREE_BRANCH", "AXE" -> dx <= 1 && dy <= 1;
            //case [3,3]
            case "MACE" -> dx <= 2 && dy <= 2;
            //case [7,7]
            case "BELL" -> dx <= 6 && dy <= 6;
            //case throwable
            case "BANANA", "METEORITE_FRAGMENT", "CRYSTAL", "SEED", "SMOKE" ->
                    (dx <= 1 && dy <= weapon.getRange()[1]+1 && dy >= weapon.getRange()[1]-1) ||
                            (dy <= 1 && dx <= weapon.getRange()[1]+1 && dx >= weapon.getRange()[1]-1);
            //normal cases
            default -> (dx == 0 && dy <= weapon.getRange()[1]) || (dy == 0 && dx <= weapon.getRange()[1]);
        };

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

//        if (difInX < 0){
//            direction = (difInY < 0) ? "u" : "d";
//        }else if (difInX == 0){
//            direction = (difInY < 0) ? "u" : "d";
//        }
//        else{
//            direction = (difInY < 0) ? "u" : "d";
//        }
        return direction;
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
        nodes.addAll(gameMap.getObstaclesByTag(String.valueOf(ObstacleTag.TRAP)));
        nodes.addAll(gameMap.getOtherPlayerInfo());
        return nodes;
    }
}