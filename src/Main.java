import controller.ChestController;
import controller.CombatController;
import controller.InventoryController;
import controller.ItemController;
import io.socket.emitter.Emitter;
import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.algorithm.PathUtils;
import jsclub.codefest.sdk.base.Node;
import jsclub.codefest.sdk.model.Element;
import jsclub.codefest.sdk.model.ElementType;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.*;
import jsclub.codefest.sdk.model.healing_items.HealingItem;
import jsclub.codefest.sdk.model.obstacles.Obstacle;
import jsclub.codefest.sdk.model.players.Player;
import jsclub.codefest.sdk.model.weapon.Weapon;
import util.ItemStatComparator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static jsclub.codefest.sdk.algorithm.PathUtils.distance;

public class Main {
    private static final String SERVER_URL = "https://cf25-server.jsclub.dev";
    private static final String GAME_ID = "177280";
    private static final String PLAYER_NAME = "Nam";
    private static final String SECRET_KEY = "sk-rAiD_741RwOf-mMiP--IXw:7IC491OHP-m2TIA_9L3YLsyKQ-NTfXn2AFFI80rN4VnPtAicns1GXUxcXhR9QtZO9AX3zJcms7ifir_aGOkXVA";


    public static void main(String[] args) throws IOException {
        Hero hero = new Hero(GAME_ID, PLAYER_NAME, SECRET_KEY);
        Emitter.Listener onMapUpdate = new MapUpdateListener(hero);

        hero.setOnMapUpdate(onMapUpdate);
        hero.start(SERVER_URL);
    }
}

class MapUpdateListener implements Emitter.Listener {
    private final Hero hero;
    private final ChestController chestController;
    private  final CombatController combatController;
    private  final ItemController itemController;
    private final InventoryController inventoryController;

    public MapUpdateListener(Hero hero) {
        this.hero = hero;
        this.chestController = new ChestController(hero);
        this.combatController = new CombatController(hero);
        this.itemController = new ItemController(hero);
        this.inventoryController = new InventoryController(hero.getInventory());
    }

    @Override
    public void call(Object... args) {
        try {
            if (args == null || args.length == 0) return;

            GameMap gameMap = hero.getGameMap();
            gameMap.updateOnUpdateMap(args[0]);
            Player player = gameMap.getCurrentPlayer();

            if (player == null || player.getHealth() == 0) {
                System.out.println("Player is dead or data is not available.");
                return;
            }
            List<Node> nodesToAvoid = getNodesToAvoid(gameMap);
            Obstacle nearestChest = chestController.getNearestChest();
            Player nearestPlayer = combatController.getNearestPlayer(gameMap);


            //LOGIC HIỆN TẠI ,
            handleRecover(player, 70);
            if (itemController.handleSearchAroundItems(2)) return;
            else if (PathUtils.distance(player, nearestChest) < PathUtils.distance(player, nearestPlayer)){
                chestController.moveToNearChestAndBreak(nodesToAvoid);
            }else{
                combatController.engageNearestEnemy();
            }
            //
        } catch (Exception e) {
            System.err.println("Critical error in call method: " + e.getMessage());
            e.printStackTrace();
        }
    }



    public void handleRecover(Player player, float minimum) throws IOException {
        System.out.println("🎒 Current HP: " + player.getHealth());
        if (player.getHealth() < minimum){
            List<HealingItem> healingItems = hero.getInventory().getListHealingItem();
            if (healingItems != null && !healingItems.isEmpty()){
                hero.useItem(healingItems.getFirst().getId());
                System.out.println("🎒 Using HealingItem: " + healingItems.getFirst().getId() + " and heal " + healingItems.getFirst().getHealingHP() + "HP!!");
            }
        }
    }

    private List<Node> getNodesToAvoid(GameMap gameMap) throws  IOException{
        List<Node> nodes = new ArrayList<>(gameMap.getListIndestructibles());
        nodes.removeAll(gameMap.getObstaclesByTag("CAN_GO_THROUGH"));
        nodes.addAll(gameMap.getListTraps());
        nodes.addAll(gameMap.getListEnemies());
        nodes.addAll(gameMap.getOtherPlayerInfo());
        return nodes;
    }

}
