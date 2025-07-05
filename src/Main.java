import controller.ChestController;
import controller.CombatController;
import jsclub.codefest.sdk.model.weapon.Weapon;
import jsclub.codefest.sdk.socket.data.receive_data.Item;
import service.InventoryService;
import controller.ItemController;
import io.socket.emitter.Emitter;
import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.algorithm.PathUtils;
import jsclub.codefest.sdk.base.Node;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.obstacles.Obstacle;
import jsclub.codefest.sdk.model.obstacles.ObstacleTag;
import jsclub.codefest.sdk.model.players.Player;
import jsclub.codefest.sdk.model.support_items.SupportItem;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final String SERVER_URL = "https://cf25-server.jsclub.dev";
    private static final String GAME_ID = "164789";
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

    public MapUpdateListener(Hero hero) {
        this.hero = hero;
        this.chestController = new ChestController(hero);
        this.combatController = new CombatController(hero);
        this.itemController = new ItemController(hero);
        InventoryService inventoryService = new InventoryService(hero.getInventory());
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
            List<Node> nodesToAvoid = getNodesToAvoid();
            Obstacle nearestChest = chestController.getNearestChest();
            Player nearestPlayer = combatController.getNearestPlayer();
            Weapon nearestGun = itemController.getNearestGun();


            //nếu đang ngoài Bo
            if (!PathUtils.checkInsideSafeArea(player, gameMap.getSafeZone(), gameMap.getMapSize())){
                System.out.println("Ngoài BO!!!");
                moveToSafeZone();
            }
            //hồi máu nếu dưới MIN
            handleRecover(player, 70);

            // 1. Nếu có item gần, ưu tiên nhặt trước
            if (itemController.handleSearchAroundItems(2, nodesToAvoid)) return;

            // 2. Nếu không có vũ khí nào mạnh, nên ưu tiên trước
            if (!combatController.isArmed()) {
                if (nearestGun != null){
                    itemController.handleSearchForGun(nodesToAvoid);
                    return;
                }else{
                    chestController.moveToNearChestAndBreak(nodesToAvoid);
                }

            }

            // 3. So sánh khoảng cách để quyết định hành động
            if (nearestChest != null && 1.5*PathUtils.distance(player, nearestChest) < PathUtils.distance(player, nearestPlayer)) {
                chestController.moveToNearChestAndBreak(nodesToAvoid);
            } else if (nearestPlayer != null){
                combatController.engageNearestEnemy(nodesToAvoid);
            }

            //LOGIC HIỆN TẠI ,
//            handleRecover(player, 70);
//            if (itemController.handleSearchAroundItems(2, nodesToAvoid)) return;
//            if (nearestChest == null){
//                combatController.engageNearestEnemy(nodesToAvoid);
//            }else if (PathUtils.distance(player, nearestChest) < PathUtils.distance(player, nearestPlayer)){
//                chestController.moveToNearChestAndBreak(nodesToAvoid);
//            }else{
//                combatController.engageNearestEnemy(nodesToAvoid);
//            }
            //
        } catch (Exception e) {
            System.err.println("Critical error in call method: " + e.getMessage());
            e.printStackTrace();
        }
    }



    public void handleRecover(Player player, float minimum) throws IOException {
        System.out.println("🎒 Current HP: " + player.getHealth());
        if (player.getHealth() < minimum){
            List<SupportItem> supportItems = hero.getInventory().getListSupportItem();
            if (supportItems != null && !supportItems.isEmpty()){
                hero.useItem(supportItems.getFirst().getId());
                System.out.println("🎒 Using SupportItem: " + supportItems.getFirst().getId() + " and heal " + supportItems.getFirst().getHealingHP() + "HP!!");
            }
        }
    }

    private List<Node> getNodesToAvoid() throws  IOException{
        GameMap gameMap = hero.getGameMap();
        List<Node> nodes = new ArrayList<>(gameMap.getListIndestructibles());
        nodes.removeAll(gameMap.getObstaclesByTag("CAN_GO_THROUGH"));
        nodes.addAll(gameMap.getObstaclesByTag(String.valueOf(ObstacleTag.TRAP)));
        nodes.addAll(gameMap.getListEnemies());
        nodes.addAll(gameMap.getOtherPlayerInfo());
        return nodes;
    }

    public void moveToSafeZone() throws IOException {
        Player player = hero.getGameMap().getCurrentPlayer();
        int x = player.getX(), y = player.getY();
        if (x < 0) hero.move("r");
        else hero.move("l");
        if (y < 0) hero.move("u");
        else hero.move("d");
    }
}
