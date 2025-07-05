package controller;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.algorithm.PathUtils;
import jsclub.codefest.sdk.base.Node;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.obstacles.Obstacle;
import jsclub.codefest.sdk.model.obstacles.ObstacleTag;
import jsclub.codefest.sdk.model.players.Player;
import java.io.IOException;
import java.util.List;

public class ChestController {
    private final Hero hero;

    public ChestController(Hero hero) {
        this.hero = hero;
    }

    public boolean moveToNearChestAndBreak(List<Node> nodesToAvoid) throws IOException {
        GameMap gameMap = hero.getGameMap();
        Player player = hero.getGameMap().getCurrentPlayer();

        String pathToChest = findPathToChest(nodesToAvoid);

        if (pathToChest != null) {
            if (pathToChest.length() > 1) {
                hero.move(pathToChest);
                System.out.println("Found chest!! Move to chest");
            } else if (pathToChest.length() == 1) {
                hero.attack(String.valueOf(pathToChest.charAt(0)));
                System.out.println("Found chest!! Attacking chest.");
            }
            return true;
        }
        return false;
    }

    //get the path to nearest chest without through Dark-area
    private String findPathToChest(List<Node> nodesToAvoid) {
        GameMap gameMap = hero.getGameMap();
        Player player = hero.getGameMap().getCurrentPlayer();

        Obstacle nearestChest = getNearestChest();
        if (nearestChest == null) return null;
        return PathUtils.getShortestPath(gameMap, nodesToAvoid, player, nearestChest, true);
    }

    //get the nearest chest within safe-zone
    public Obstacle getNearestChest() {
        GameMap gameMap = hero.getGameMap();
        Player player = hero.getGameMap().getCurrentPlayer();

        List<Obstacle> chests = gameMap.getObstaclesByTag(String.valueOf(ObstacleTag.DESTRUCTIBLE));
        Obstacle nearestChest = null;
        double minDistance = Double.MAX_VALUE;

        for (Obstacle chest : chests) {
            if (!PathUtils.checkInsideSafeArea(chest, gameMap.getSafeZone(), gameMap.getMapSize())) continue;

            double distance = PathUtils.distance(player, chest);
            if (distance < minDistance) {
                minDistance = distance;
                nearestChest = chest;
            }
        }
        return nearestChest;
    }
}
