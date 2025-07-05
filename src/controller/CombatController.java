package controller;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.algorithm.PathUtils;
import jsclub.codefest.sdk.base.Node;
import jsclub.codefest.sdk.model.ElementType;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.Inventory;
import jsclub.codefest.sdk.model.players.Player;
import jsclub.codefest.sdk.model.weapon.Weapon;
import service.InventoryService;
import service.WeaponService;

import java.io.IOException;
import java.util.List;

public class CombatController {

    private final Hero hero;
    private final Inventory inventory;
    private final WeaponService weaponService = WeaponService.getInstance();

    public CombatController(Hero hero) {
        this.hero = hero;
        this.inventory = hero.getInventory();
        new InventoryService(hero.getInventory());
    }

    public void engageNearestEnemy(List<Node> nodesToAvoid) throws IOException {
        GameMap gameMap = hero.getGameMap();
        Player me = gameMap.getCurrentPlayer();
        long currentStep = gameMap.getStepNumber();

        Player target = getNearestPlayer();
        if (target.getHealth() <= 0) return;

        Weapon best = getBestWeaponCanUse(me, target, currentStep);

        if (best != null) {
            System.out.println("✅ Using weapon: " + best.getId());
            String direction = getDirection(best, me, target);
            useWeapon(best, direction);
            weaponService.markUsed(best.getId(), currentStep);
        } else {
            System.out.println("➡️ Moving toward enemy.");
            moveTowards(target, nodesToAvoid);
        }

    }

    public Weapon getBestWeaponCanUse(Player player, Player target, long currentStep) {
        //special case: nếu có thể kết liễu bằng melee, tránh tốn đạn
        Weapon melee = inventory.getMelee();
        if (canUse(melee, player, target) && weaponService.canUse(melee, currentStep) && target.getHealth() <= melee.getDamage()){
            return melee;
        }


        Weapon[] priority = {
                inventory.getGun(),
                inventory.getThrowable(),
                inventory.getSpecial(),
                inventory.getMelee()
        };

        for (Weapon weapon : priority) {
            if (weapon == null) continue;
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

        return switch (weapon.getId()) {
            case "HAND" -> (dx == 1 && dy == 0) || (dx == 0 && dy == 1);
            case "KNIFE", "TREE_BRANCH", "AXE" -> dx <= 1 && dy <= 1;
            case "MACE" -> dx <= 2 && dy <= 2;
            case "BELL" -> dx <= 6 && dy <= 6;
            case "BANANA", "METEORITE_FRAGMENT", "CRYSTAL", "SEED", "SMOKE" ->
                    (dx <= 1 && Math.abs(dy - weapon.getRange()[1]) <= 1)
                            || (dy <= 1 && Math.abs(dx - weapon.getRange()[1]) <= 1);
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
        return direction;
    }

    private void moveTowards(Player target,  List<Node> nodesToAvoid) throws IOException {
        GameMap gameMap = hero.getGameMap();
        Player me = gameMap.getCurrentPlayer();
        Node from = new Node(me.getX(), me.getY());
        Node to = new Node(target.getX(), target.getY());

        String path = PathUtils.getShortestPath(gameMap, nodesToAvoid, from, to, true);
        if (!path.isEmpty()) {
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
            int distance = PathUtils.distance(me, p);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = p;
            }
        }

        return nearest;
    }

}
