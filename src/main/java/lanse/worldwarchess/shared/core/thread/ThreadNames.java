package lanse.worldwarchess.shared.core.thread;

/** Written by Lanse, taken from Bonk Universe
 * <br><br>
 * This class is the center of all Multithreading in Bonk Universe. Each thread's name is tracked here, and can be added to
 * the Flame Graph Tester list to test for lag. **/
public class ThreadNames {

//    public static final String DATA_SAVER = "SaveData";
//    public static final String PLAYER_SAVING = "PlayerSaving";
//    public static final String RETURNER = "Returner";
//    public static final String CRAFTING_STARTUP = "craftingStartup";
//    public static final String CELESTIAL_IMAGE_GENERATION = "CelestialImageGeneration";
//    public static final String ENTITY_IMAGE_GENERATION = "EntityImageGeneration";
//    public static final String INITIALIZATION = "initialization";
//    public static final String MAIN_MENU_CONSTRUCTOR = "mainMenuConstructor";
//    public static final String MAIN_MENU_BACKGROUND = "MainMenuBackgroundLoop";
//    public static String MAIN = "";
//    public static final String DRAWING_AND_EVENT_QUEUE = "AWT-EventQueue-0";
//    public static final String CENTRAL_TICK = "pool-1-thread-1";
//    public static final String START_GAME = "startGame";
//    public static final String PACKET_QUEUE = "PacketQueue";
//    public static final String BACKGROUND_MUSIC = "BackgroundMusic";
//    public static final String CHUNK_IMAGE_GENERATION = "ChunkImageGeneration";
//    public static final String MULTIPLAYER_CHUNK_SERVER_OVERSEER_MANAGER = "MCSOM";
//    public static final String MULTIPLAYER_CENTRAL_TICK = "MCT";
//
    public static final String FLAME_GRAPH = "FlameSampler"; //dont put in the flame graph init, just here for centrality
//
//    public static final boolean TERRAIN_GENERATION = false;

    public static final String NPC_MOVER = "NPCMover";

    public static void init(){
//        FlameGraphLagTester.INCLUDED_THREADS.add(DATA_SAVER);
//        FlameGraphLagTester.INCLUDED_THREADS.add(PLAYER_SAVING);
//        FlameGraphLagTester.INCLUDED_THREADS.add(RETURNER);
//        FlameGraphLagTester.INCLUDED_THREADS.add(CRAFTING_STARTUP);
//        FlameGraphLagTester.INCLUDED_THREADS.add(CELESTIAL_IMAGE_GENERATION);
//        FlameGraphLagTester.INCLUDED_THREADS.add(MAIN);
//        FlameGraphLagTester.INCLUDED_THREADS.add(INITIALIZATION);
//        FlameGraphLagTester.INCLUDED_THREADS.add(MAIN_MENU_CONSTRUCTOR);
//        FlameGraphLagTester.INCLUDED_THREADS.add(MAIN_MENU_BACKGROUND);
//        FlameGraphLagTester.INCLUDED_THREADS.add(DRAWING_AND_EVENT_QUEUE);
//        FlameGraphLagTester.INCLUDED_THREADS.add(CENTRAL_TICK);
//        FlameGraphLagTester.INCLUDED_THREADS.add(START_GAME);
//        FlameGraphLagTester.INCLUDED_THREADS.add(PACKET_QUEUE);
//        FlameGraphLagTester.INCLUDED_THREADS.add(CHUNK_IMAGE_GENERATION);
//        FlameGraphLagTester.INCLUDED_THREADS.add(MULTIPLAYER_CENTRAL_TICK);
        FlameGraphLagTester.INCLUDED_THREADS.add(NPC_MOVER);
    }
}
