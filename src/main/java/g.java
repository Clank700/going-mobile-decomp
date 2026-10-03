import java.io.*;
import java.util.*;
import javax.microedition.lcdui.*;

/*
 * g - Game Canvas.
 *
 * The in-game Canvas and main loop (run()): game-state machine, player/enemy/
 * projectile updates, collision, camera, HUD and in-game menu rendering, level
 * select, weapon select, key input and save-data (storeSavedWeapon)serialisation.
 *
 * Naming: this is reconstructed, output-exact Java source (not recovered original source).
 * Member names are descriptive source names; config/d-preobf.map maps every one of them
 * back to its retail (obfuscated) runtime name. Renames are always group-complete (all
 * members sharing a retail name), which reproduces ProGuard's retail constant-pool
 * ordering; see cleanup/docs/NAMING_SCHEME.md. Library overrides keep their API names,
 * classes keep their retail names, and reconstruction devices keep their opus* names.
 */
public final class g extends javax.microedition.lcdui.Canvas implements Runnable {
    /** Screen width (default 176, replaced by getWidth()). */
    public static short screenWidth = 176;
    /** Screen height (default 220, replaced by getHeight()). */
    public static short screenHeight = 220;
    /** Small plain font (body text). */
    public static final Font smallFont = Font.getFont(0, 0, 8);
    /** Small bold font (highlighted entries). */
    public static final Font boldFont = Font.getFont(0, 1, 8);
    /** Medium bold font (titles, soft-key labels). */
    public static final Font titleFont = Font.getFont(0, 1, 0);
    /** Line height of the small font. */
    public static final int lineHeight = smallFont.getHeight();
    /** Owning MIDlet. */
    public ratchetandclank app;
    /** The MIDlet's Display. */
    private Display display;
    /** Game thread (see run()). */
    private java.lang.Thread gameThread;
    /**
     * Game screen state:
     * 0 playing (16 / 17 / 24 = playing with letterbox: cut-scene, dialogue, arena intro),
     * 1 arena list, 2 game won, 3 level select, 4 pause menu, 5 game settings, 6 choose save slot,
     * 7 really quit game?, 8 quit to main menu?, 9 save over data?, 10 game saved, 11 weapon store,
     * 12 buy weapon / ammo, 13 not enough bolts, 14 arena description, 15 arena reward,
     * 18 stage summary, 19 you fail, 21 save / replay, 22 weapon select wheel, 23 challenge mode?
     */
    public byte gameState;
    public byte stateBeforePause;
    /** Repaint requested. */
    public boolean repaintRequested;
    /** HUD redraw requested. */
    public boolean hudDirty;
    /** Invulnerability: the player takes no damage while set. */
    public static boolean invulnerable = false;
    public boolean bossFightStarted;
    public byte bossCutsceneStep;
    public byte bossCutsceneJumps;
    public byte maxFrame = 0;
    public byte tutorialStep;
    /** X position of the scrolling marquee text. */
    public int marqueeX = screenWidth;
    public java.util.Vector arenaItems;
    public java.util.Vector arenaDescription;
    public java.util.Vector storeItems;
    public java.util.Vector buyItems;
    public java.util.Vector summaryLines;
    /** Busy loading: painting and key input are suspended. */
    public boolean loading;
    /** Debug overlay mode (cycled with key 0 on the about screen; 2 = show frame rate). */
    public byte debugOverlay;
    /** Mission objective shown in the pause menu (string 213 + objectiveIndex). */
    public byte objectiveIndex;
    public byte winFrame;
    public byte winTimer;
    /** Secret key sequence on the about screen was entered. */
    public boolean debugKeysEnabled;
    public byte cutsceneStep;
    public boolean cutsceneJumping;
    public int endingExplosionCount;
    /** Smallest horizontal camera offset (screen width - 28 tiles). */
    public static short minCamX;
    /** Smallest vertical camera offset (screen height - 18 tiles). */
    public static short minCamY;
    /** Current horizontal camera offset (pixels, <= 0). */
    public static int camX = 0;
    /** Current vertical camera offset (pixels, <= 0). */
    public static int camY = 0;
    public static boolean cameraLocked = false;
    public static boolean grappleAvailable = false;
    public static boolean onTeleporter = false;
    public static byte teleporterTarget;
    public static boolean teleporterFlag = false;
    public static int cameraLockX = 0;
    /** Tile width in pixels. */
    public static byte tileWidth = 42;
    /** Tile height in pixels. */
    public static byte tileHeight = 28;
    /** HUD height: top of the play area (at least one text line). */
    public static byte hudHeight = 20;
    /** Y position of the dialogue text box (a whole number of tiles above the bottom, + 12). */
    public int dialogueBoxY;
    /** Actor sprite cell width. */
    public static byte cellWidth = 44;
    /** Actor sprite cell height. */
    public static byte cellHeight = 44;
    /** Vertical draw offset of the player sprite. */
    public static byte playerDrawOffsetY = -16;
    /** Level-select map: node reached with Up from each node. */
    public static final byte[] mapUp = { 6, 0, 0, 1, 2, 4, 5, 3, 13, 13, 9, 10, 9, 16, 12, 11, 8, 11, 19, 18 };
    /** Level-select map: node reached with Down from each node. */
    public static final byte[] mapDown = { 2, 3, 4, 7, 5, 6, 0, 5, 16, 12, 11, 17, 14, 10, 15, 13, 13, 15, 4, 18 };
    /** Level-select map: node reached with Left from each node. */
    public static final byte[] mapLeft = { 19, 0, 18, 2, 12, 4, 5, 4, 7, 10, 3, 8, 11, 1, 15, 6, 6, 16, 9, 13 };
    /**
     * Level-select map: node reached with Right from each node (18 = arenas, 19 = weapon store).
     */
    public static final byte[] mapRight = { 1, 13, 3, 10, 7, 7, 16, 8, 11, 18, 9, 12, 4, 19, 12, 14, 17, 14, 2, 0 };
    /** Weapon select wheel: next weapon clockwise. */
    public static final byte[] wheelNext = { 0, 2, 3, 5, 1, 7, 4, 6 };
    /** Weapon select wheel: next weapon anticlockwise. */
    public static final byte[] wheelPrev = { 0, 4, 1, 2, 6, 3, 7, 5 };
    public static final byte[] titaniumPerLevel = { 4, 3, 2, 3, 4, 4, 2, 3, 3, 2, 0, 0, 0 };
    /** Current level / tile map. */
    public c levelMap;
    /** Random number generator. */
    public Random random;
    /** Current room of the level (index into the c tables). */
    public short currentRoom = -1;
    /** Room being entered (neighbour lookup in changeRoom(int)). */
    public short targetRoom = -1;
    /** Current level: 0 = tutorial level, 1-10 story levels, 11 = arena, 12 = boss level. */
    public byte levelIndex = 1;
    public boolean challengeMode = false;
    public byte challengeMultiplier = 1;
    public byte saveSlot;
    public byte checkpointColumn;
    public byte checkpointRow;
    public short checkpointCamX;
    public short checkpointCamY;
    public short checkpointRoom = 0;
    /** Enemy slots (enemySlotCount of them). */
    public d[] enemies;
    /** The player. */
    public a player;
    /** Player projectile / effect slots. */
    public h[] playerShots;
    /** Enemy and boss projectile slots. */
    public h[] enemyShots;
    public static Image imgArrows;
    public static Image imgHud;
    public static Image imgArrowRight;
    public static Image imgDigits;
    public static Image imgPanelBottom;
    public static Image imgMapPanelTop;
    public static Image imgPanelTop;
    public static Image imgMapIcons;
    public static Image imgMenuHeader;
    public static Image imgPortraits;
    public static javax.microedition.lcdui.Image imgSpike;
    public static Image imgMaximillian;
    public static Image imgPayola;
    public static javax.microedition.lcdui.Image imgBounceBot;
    public static javax.microedition.lcdui.Image imgFlameBot;
    public static Image imgWinPicture;
    public static Image imgWeaponWheel;
    public static Image imgDoors;
    public static Image imgPlatform;
    public static Image imgIcons;
    public static javax.microedition.lcdui.Image[] imgTiles;
    public static Image[] imgPlayerFrames;
    public static javax.microedition.lcdui.Image[][] imgEnemyFrames;
    public static Image imgWeapons;
    public static javax.microedition.lcdui.Image imgCrates;
    public static javax.microedition.lcdui.Image imgStartPoint;
    public static javax.microedition.lcdui.Image imgBolts;
    public static javax.microedition.lcdui.Image imgExplosion;
    public static Image imgCannonBase;
    public static javax.microedition.lcdui.Image imgCannonShot;
    public static Image imgCannonBarrel;
    public byte tilesetIndex;
    /** Tile-set / background resources: Agent Clank levels, arenas, OS levels, menu. */
    public static final String[] tilesetFiles = { "/bg_agclnk.dat", "/bg_arena.dat", "/bg_os.dat", "/bg_menu.png" };
    public byte drawActorType;
    public byte drawActorAnim;
    public int drawActorFrame;
    public byte drawTransform;
    /** Bolts (currency). */
    public int bolts;
    public int savedBolts;
    /** Ammo price per weapon in the store. */
    public int[] ammoPrices;
    /** Number of enemy slots. */
    public static byte enemySlotCount = 10;
    public static byte playerActorId = enemySlotCount;
    /** Grind rail start x (4 rails). */
    public short[] railX1;
    /** Grind rail start y. */
    public short[] railY1;
    /** Grind rail end x. */
    public short[] railX2;
    /** Grind rail end y. */
    public short[] railY2;
    /** Grind rail horizontal length. */
    public short[] railDx;
    /** Grind rail vertical rise (8.8). */
    public int[] railDy;
    /** Grind rail line offset (8.8) for y = railBase + x * railDy / railDx. */
    public int[] railBase;
    /** Spike hazard x (8 slots, -1 = none). */
    public short[] spikeX;
    /** Spike hazard y. */
    public short[] spikeY;
    /** Crate x (pixels), 50 slots. */
    public short[] crateX;
    /** Crate y (pixels). */
    public short[] crateY;
    /** Crate type / sprite frame in box.png (-1 = empty slot or destroyed). */
    public byte[] crateType;
    /** Crate fall offset (pixels) while dropping. */
    public short[] crateFall;
    /** Crate landing y. */
    public short[] crateLandY;
    /** Crate stacked on top of this one (-1 = none). */
    public short[] crateAbove;
    /** Crate this one stands on (-1 = none). */
    public short[] crateBelow;
    /** Crate is falling. */
    public boolean[] crateFalling;
    /** Per room: respawn all crates and enemies on the next entry. */
    public boolean[] roomRespawn;
    /** Crates still intact, one bit per room slot (room * 50 + crate index). */
    public int[] crateIntactBits;
    /** Enemies still alive, one bit per room slot (room * 10 + spawn index). */
    public int[] enemyAliveBits;
    /** Door / gate unlock bits (tested by c.isSolid(int, int)). */
    public byte doorBits;
    /**
     * Bolt-lock gates still closed, levels 1-5 (6 room bits per level); copied into door bit 0 of doorBits on room entry.
     */
    public int gateBitsA;
    /** Bolt-lock gates still closed, levels 6-10 (6 room bits per level). */
    public int gateBitsB;
    public byte savedDoorBits;
    public int savedGateBitsA;
    public int savedGateBitsB;
    /** Door / gate kind bit per door slot (3 slots; -128 = special gate). */
    public byte[] doorKind;
    /** Door tile column. */
    public byte[] doorColumn;
    /** Door tile row. */
    public byte[] doorRow;
    /** Bolt-lock (wrench switch) tile column, 3 slots (-1 = none). */
    public byte[] lockColumn;
    /** Bolt-lock tile row. */
    public byte[] lockRow;
    public static final byte[][] cannonFrames = { { 26, 26, 0, 3 }, { 26, 26, 26, 3 }, { 20, 20, 52, 8 }, { 20, 20, 72, 8 } };
    /** Boss cannon state per cannon (0 idle, 1 hit, 2 destroyed, 3 core hit). */
    public byte[] cannonState;
    /** Boss cannon hit-flash timer. */
    public byte[] cannonFlash;
    /** Boss hit points: four cannons [0-3] and the core [4]. */
    public int[] bossHealth;
    /** Boss turret aim direction (0-7). */
    public byte turretDir = 0;
    /** Boss fire timer. */
    public byte bossFireTimer = 0;
    /** Boss attack pattern step. */
    public byte bossPattern = 0;
    public static final byte[] cannonColumns = { 14, 14, 13, 13 };
    public static final byte[] cannonRows = { 8, 9, 8, 9 };
    /** Titanium bolt tile column in this room (-1 = none). */
    public byte boltColumn;
    /** Titanium bolt tile row. */
    public byte boltRow;
    /** Number of titanium bolts needed for the R.Y.N.O. */
    public static byte boltsForRyno = 30;
    public static byte boltBitCount = 60;
    /** Titanium bolts not yet collected, levels 1-5 (6 bits per level). */
    public int boltBitsA;
    /** Titanium bolts not yet collected, levels 6-10 (6 bits per level). */
    public int boltBitsB;
    public int savedBoltBitsA;
    public int savedBoltBitsB;
    /** Clank arm-module pick-up tile column (-1 = none). */
    public byte armModuleColumn;
    /** Clank arm-module pick-up tile row. */
    public byte armModuleRow;
    /** The Clank arm-module pick-up is still available. */
    public boolean armModuleAvailable = true;
    /** Payola pick-up tile column (-1 = none). */
    public byte payolaColumn;
    /** Payola pick-up tile row. */
    public byte payolaRow;
    public byte bounceBotColumn;
    public byte bounceBotRow;
    public byte bounceBotFrame;
    public byte bounceBotTimer;
    /** Loose pick-up x (8.8), 12 slots. */
    public int[] pickupX;
    /** Loose pick-up y (8.8). */
    public int[] pickupY;
    /** Loose pick-up horizontal speed when pulled toward the player. */
    public short[] pickupVx;
    public short[] pickupVy;
    /** Loose pick-up type: 0/1 bolt, 2 nanotech (health), 3 ammo; -1 = none. */
    public byte[] pickupType;
    /** Bolt reward per arena. */
    public static final int[] arenaRewards = { 200, 400, 400, 600, 600, 600, 800, 800, 800, 1200, 1200, 2000 };
    /** Moving platform base x (4 slots). */
    public short[] platformX;
    /** Moving platform base y. */
    public short[] platformY;
    /** Moving platform x offset. */
    public short[] platformDx;
    /** Moving platform y offset. */
    public short[] platformDy;
    /** Moving platform direction state (0 right, 1 down, 2 left, 3 up; -1 = none). */
    public byte[] platformDir;
    /** Total play time (ms), saved at offset 195 of a save slot. */
    public int playTimeMs;
    /** Frame start time (ms). */
    public long frameStartMs;
    /** Previous frame start time (ms). */
    public long lastFrameStartMs;
    /** Menu cursor of the current in-game screen. */
    public byte menuCursor;
    private byte selectedItem;
    private byte entryEdge = 0;
    private byte bossCutsceneTimer = 0;
    /** Held direction (game action) driving the player; 0 = none. */
    public int heldAction;
    /** Buffered direction (game action) for the player. */
    public int bufferedAction = 0;
    /** Fire button state (bit 7 = fire held). */
    public int fireState = 0;
    public long fireTime = 0L;
    /** Number of lines of each scripted dialogue. */
    public static final byte[] dialogueLineCounts = { 2, 4, 2, 13, 1, 1, 1, 1, 3, 1, 1, 1, 5, 4, 3, 3, 6, 5, 0, 0, 6, 1, 5, 1, 1, 1, 1, 1, 5, 1, 3, 7, 4, 3, 4, 3, 1, 1, 1, 1 };
    /** First string id of each scripted dialogue (story conversations and tutorial hints). */
    public static final short[] dialogueFirstString = { 104, 106, 110, 112, 125, 126, 127, 128, 129, 132, 133, 134, 135, 140, 144, 147, 150, 156, 0, 0, 161, 167, 168, 173, 174, 175, 176, 177, 178, 183, 184, 187, 194, 199, 202, 206, 209, 209, 210, 211 };
    public static final byte[] speakerPortrait = { 1, 0, 1, 0, 1, 0, 1, 0, 1, 2, 1, 2, 0, 1, 2, 0, 1, 2, 1, 0, 2, -1, -1, -1, -1, 0, 1, 0, -1, -1, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 1, 0, 0, 1, 0, 1, 2, 1, 0, 2, -1, 1, 0, 2, 0, 1, 0, 1, 0, 1, 0, 1, -1, 1, 0, 1, 0, 1, -1, -1, -1, -1, -1, 0, 1, 0, 1, 0, -1, 4, 0, 4, 3, 0, 3, 0, 3, 1, 3, 0, 1, 0, 1, -1, 0, 1, 0, 0, 1, 0, 1, 0, 1, 0, -1, -1, 0, 1 };
    /** The text box shows a dialogue with speaker portraits. */
    public boolean dialoguePortraits = false;
    /** The text box is open (game logic paused). */
    public boolean textBoxOpen = false;
    /** Text shown in the text box. */
    public String textBoxText;
    /** Character position of the current text page (-1 = finished). */
    public int textPos = -1;
    /** Character position of the next text page. */
    public int nextTextPos = -1;
    /** String id of the current text box line. */
    public int textId;
    /** Dialogue lines remaining. */
    public byte dialogueLinesLeft = 0;
    /** Dialogues 0-19 not yet seen (bit set = still pending). */
    public int dialogueBitsA;
    /** Dialogues 20-39 not yet seen (bit set = still pending). */
    public int dialogueBitsB;
    /** Level-select map nodes unlocked (bits 0-20) and arenas unlocked (bits 21+). */
    public int mapUnlockBits;
    public int savedDialogueBitsA;
    public int savedDialogueBitsB;
    public int savedMapUnlockBits;
    public short savedRoom;
    /** Level started by each level-select map node. */
    public byte[] mapNodeLevel = { 1, 1, 2, 2, 3, 3, 3, 4, 4, 5, 6, 6, 7, 8, 9, 9, 10, 12, 11, 13 };
    /** Start room for each level-select map node. */
    public short[] mapNodeRoom = { 0, 2, 0, 1, 0, 2, 4, 0, 1, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0 };
    public boolean fallingDeath = false;
    public boolean deadlyPit = false;
    public byte storeSavedWeapon;
    /** Arena challenge in progress. */
    public boolean arenaActive = false;
    /** Arena rule: sleeping gas (health decreases over time). */
    public boolean ruleGas = false;
    /** Arena rule: one-hit kills. */
    public boolean ruleOneHit = false;
    public byte gasTimer;
    /** Arena rule: only one weapon may be used. */
    public boolean ruleOneWeapon = false;
    /** Owned weapons saved while an arena restricts them. */
    public byte savedWeapons;
    public byte enemiesLeft;
    public int[] menuItemY;
    /** Level-select map geometry from /mapData.txt (62 x 3), drawn by drawLevelSelect(Graphics). */
    public short[][] mapGeometryA;
    /** Level-select map geometry from /mapData.txt (43 x 2), drawn by drawLevelSelect(Graphics). */
    public byte[][] mapGeometryB;
    public final byte[] pathNode = { 2, 18, 4, 7, 5, 1, 13, 10, 9, 12, 6, 15, 11, 8, 16, 17 };
    public final byte[] pathDotCount = { 1, 1, 2, 1, 3, 8, 1, 2, 3, 3, 6, 2, 5, 2, 1, 2 };
    public final byte[][] pathDots = { { 0 }, { 1 }, { 2, 3 }, { 4 }, { 5, 6, 7 }, { 8, 9, 10, 11, 12, 13, 14, 15 }, { 16 }, { 17, 18 }, { 19, 20, 21 }, { 22, 23, 24 }, { 25, 26, 27, 28, 29, 30 }, { 31, 32 }, { 33, 34, 35, 36, 37 }, { 38, 39 }, { 40 }, { 41, 42 } };
    /** Level-select map nodes highlighted as the next destination. */
    public int mapHighlightBits;
    public byte menuAnimTick;
    public static byte arenaListVariant;
    public int savedMapHighlightBits;
    /** Deaths in the current stage. */
    public static int deaths;
    public static int enemiesSkipped;
    /** Enemy kills in the current stage. */
    public static int kills;
    public static int killScore;
    /** Bolts collected in the current stage. */
    public static int boltsCollected;
    /** Titanium bolts collected in the current stage. */
    public static int titaniumCollected;
    public static int boltScore;
    public static int titaniumScore;
    public static int boarKills;
    public static int timeBonus;
    /** Time spent in the current stage (ms). */
    public static long stageTimeMs;
    /** Par time (ms) per stage, used for the time bonus on the stage summary. */
    public static final long[] parTimesMs = { 160000L, 120000L, 160000L, 130000L, 160000L, 130000L, 100000L, 140000L, 220000L, 180000L, 180000L, 110000L, 140000L, 140000L, 300000L, 150000L, 30000L };
    /** Stage index for the par-time table parTimesMs. */
    public static byte parIndex;
    /** Stage score (stage summary total). */
    public static int stageScore;
    /** Total score (all stages). */
    public static int totalScore;
    public static byte summaryVariant;
    /** String id of the reward / unlock message shown on the stage summary. */
    public int summaryMessageId;
    public static int shotsFired;
    public static int shotsHit;
    /** Perfect bonus earned. */
    public static boolean perfectBonus;
    /** Pacifist bonus earned (no enemy killed). */
    public static boolean pacifistBonus;
    /** Boar bonus earned (every kill made with the Boar-Zooka). */
    public static boolean boarBonus;
    public static boolean wasHit;
    public static int[] roomEnemyCounts;
    public static int[] roomKillCounts;
    public static boolean[] roomCleared;
    public String mapCaption;
    public int mapCaptionNode = -1;
    public int mapCaptionWidth = -1;
    public int mapCaptionX;
    public boolean marqueeScrollingLeft;
    public static final byte[][] winPictureFrames = { { 41, 36, 21, 36, 22, 8 }, { 41, 0, 22, 36, 21, 8 }, { 62, 36, 26, 36, 17, 8 }, { 63, 0, 29, 36, 14, 8 }, { 0, 39, 41, 36, 2, 8 }, { 41, 72, 43, 36, 0, 8 }, { 88, 37, 42, 36, 1, 8 }, { 84, 73, 41, 36, 2, 8 }, { 82, 109, 42, 36, 1, 8 }, { 0, 0, 41, 39, 1, 5 }, { 41, 108, 41, 39, 1, 5 }, { 0, 75, 41, 39, 1, 5 }, { 92, 0, 40, 37, 1, 7 }, { 0, 114, 40, 37, 1, 7 }, { 82, -111, 43, 38, 1, 6 }, { 40, -109, 41, 38, 1, 6 } };
    public int wheelIconSize;
    public int wheelBoxWidth;
    public int wheelCentreY;
    public static short[][] wheelSlotPositions = new short[7][2];
    public String wheelCaption = null;
    public int wheelCaptionWeapon = -1;
    public int wheelCaptionWidth = -1;
    public int wheelCaptionX;
    public static final byte[] octagonOuterX = { 0, 0, 6, 17, 23, 23, 17, 6 };
    public static final byte[] octagonOuterY = { 17, 6, 0, 0, 6, 17, 23, 23 };
    public static final byte[] octagonInnerX = { 2, 2, 6, 17, 21, 21, 17, 6 };
    public static final byte[] octagonInnerY = { 17, 6, 2, 2, 6, 17, 21, 21 };
    public int pendingLevel = -1;
    public int pendingSlot = -1;
    /** Game paused because the canvas was hidden. */
    public boolean paused = false;
    /** Resume time; key presses are ignored for 1 s after it. */
    public long resumeTime = 0L;
    public boolean skipShowNotify;
    public long frameEndMs = 0L;
    /** Duration of the last frame (ms), for the frame-rate overlay. */
    public int frameMs;
    public int sleepMs;
    /** Leave the game and return to the main menu at the end of this frame. */
    public boolean exitToMenu = false;


    /** Adds a spike hazard at tile (x, y) (up to 8). */
    public final void addSpike(int x, int y) {
        for (int i = 0; i < 8; i++) {
            if (spikeX[i] == -1) {
                spikeX[i] = (short)(x*tileWidth+(tileWidth>>1));
                spikeY[i] = (short)(y*tileHeight+(tileHeight>>1));
                return;
            }
        }
    }
    /** Draws the spike hazards. */
    public final void drawSpikes(Graphics g) {
        int x, y;
        g.setClip(0, hudHeight, screenWidth, screenHeight - hudHeight);
        for (int i = 0; i < 8; i++) {
            if (spikeX[i] == -1) {
                return;
            }
            x = spikeX[i] + camX;
            y = spikeY[i] + camY;
            if (x < screenWidth && y < screenHeight && x + 22 >= 0 && y + 22 >= 0) {
                g.drawImage(imgSpike, x, y, 3);
            }
        }
    }

    /** Spike collision: bounces the player up and away. */
    public final void spikeCollision() {
        int x = this.player.pixelX();
        int y = this.player.pixelY() + playerDrawOffsetY;
        int i3 = 0;
        int i4 = 0;
        int i5 = 0;
        int i6 = 0;
        int n;
        int hw = imgSpike.getWidth() >> 1;
        for (n = 0; n < 8; n++) {
            if (this.spikeX[n] == -1) {
                return;
            }
            int bx = this.spikeX[n];
            int by = this.spikeY[n];
            if (this.boxesOverlap(x - 11, y + 7, 18, 37, bx - hw + 10, by - hw, (hw << 1) - 20, hw << 1)) {
                this.player.speedX = 0;
                this.player.speedY = (short)-a.runSpeed;
                this.player.setAnimation((byte)3);
                this.player.platformIgnoreTimer = 10;
                this.player.jumpState = 0;
                return;
            }
        }
    }
    /** Draws the Clank arm-module pick-up (tile armModuleColumn, armModuleRow) if present. */
    public final void drawArmModule(Graphics g) {
        if (armModuleColumn == -1) {
            return;
        }
        int u0 = 0;
        int u1 = 0;
        int x = armModuleColumn * tileWidth + camX + (tileWidth - 19 >> 1);
        int y = armModuleRow * tileHeight + camY + (tileHeight - 19 >> 1);
        if (x >= screenWidth || x + 19 < 0 || y >= screenHeight || y + 19 < 0) {
            return;
        }
        int cut = 0;
        if (y < hudHeight) {
            cut = hudHeight - y;
            y = hudHeight;
        }
        if (cut < 19 && cut >= 0) {
            g.setClip(x, y, 19, 19 - cut);
            g.drawImage(imgBolts, x, y - 247 - cut, 0);
        }
    }
    /** Collects the Clank arm-module pick-up and starts its dialogue. */
    public final void collectArmModule() {
        if (armModuleColumn == -1) {
            return;
        }
        int u1 = 0;
        int u2 = 0;
        int x = armModuleColumn * tileWidth;
        int y = armModuleRow * tileHeight;
        if (boxesOverlap(player.pixelX() - 11, player.pixelY() + playerDrawOffsetY + 7, 18, 37, x, y, 19, 19)) {
            armModuleColumn = armModuleRow = -1;
            armModuleAvailable = false;
            dialoguePortraits = true;
            dialogueLinesLeft = dialogueLineCounts[34];
            openTextBox(dialogueFirstString[34]);
        }
    }
    /** Draws the bounce-bot (imgBounceBot, two frames) at tile (bounceBotColumn, bounceBotRow). */
    public final void drawBounceBot(Graphics g) {
        if (bounceBotColumn == -1) {
            return;
        }
        int w = imgBounceBot.getWidth();
        int h = imgBounceBot.getHeight() / 2;
        int oy = playerDrawOffsetY;
        int x = bounceBotColumn * tileWidth + camX;
        int y = bounceBotRow * tileHeight + camY + oy;
        if (x >= screenWidth || x + w < 0 || y >= screenHeight || y + h < 0) {
            return;
        }
        int cut = 0;
        if (y < hudHeight) {
            cut = hudHeight - y;
            y = hudHeight;
        }
        if (cut < h && cut >= 0) {
            g.setClip(x, y, w, h - cut);
            g.drawImage(imgBounceBot, x, y - bounceBotFrame * h - cut, 0);
        }
    }
    /** Animates the bounce-bot. */
    public final void animateBounceBot() {
            --this.bounceBotTimer;
            if (this.bounceBotTimer == 0) {
                this.bounceBotTimer = 36;
            }
            if (this.bounceBotFrame == 1) {
                if (this.bounceBotTimer % 18 == 0) {
                    this.bounceBotFrame = 0;
                    return;
                }
            } else if (this.bounceBotTimer % 36 == 0) {
                this.bounceBotFrame = 1;
            }
        }
    /** Resets the stage statistics (kills, bolts, time, bonuses). */
    public final void resetStageStats() {
        killScore = 10000;
        enemiesSkipped = 0;
        deaths = 0;
        kills = 0;
        boarKills = 0;
        boltsCollected = 0;
        titaniumCollected = 0;
        timeBonus = 0;
        stageTimeMs = 0L;
        stageScore = 0;
        summaryVariant = 0;
        shotsFired = 0;
        shotsHit = 0;
        perfectBonus = false;
        pacifistBonus = false;
        wasHit = false;
        boarBonus = false;
        clearRoomCounters();
    }
    /** Number of live enemies, not counting type 4. */
    public final int liveEnemyCount() {
            int count = 0;
            for (int i = this.enemies.length - 1; i >= 0; --i) {
                if (this.enemies[i].actorKind == 4 || this.enemies[i].actorKind == -1) {
                    continue;
                }
                ++count;
            }
            return count;
        }
    /** Records the live-enemy count of room {@code room} (the second argument is an unused call-site tag). */
    public final void recordRoomEnemies(int room, int callSite) {
        roomEnemyCounts[room] = liveEnemyCount();
    }
    /** Clears the per-room enemy counters. */
    public final void clearRoomCounters() {
        for (int i = roomEnemyCounts.length - 1; i >= 0; i--) {
            roomCleared[i] = false;
            roomEnemyCounts[i] = 0;
            roomKillCounts[i] = 0;
        }
    }
    /**
     * Stage score calculation: mode 1 = base score, bonus flags and time bonus; mode 2 = add the bonuses.
     */
    public static final void computeScore(byte mode) {
        int i;
        switch (mode) {
            case 0:
                return;
            case 1:
                if (!wasHit && shotsFired != 0 && shotsFired == shotsHit) {
                    perfectBonus = true;
                }
                if (kills == 0) {
                    pacifistBonus = true;
                }
                for (i = roomEnemyCounts.length - 1; i >= 0; i--) {
                    if (roomEnemyCounts[i] > 0) {
                        enemiesSkipped += roomEnemyCounts[i];
                    }
                }
                if (boarKills == kills && kills > 0 && enemiesSkipped == 0) {
                    boarBonus = true;
                }
                killScore = kills * 100;
                stageScore += killScore;
                boltScore = boltsCollected * 1;
                titaniumScore = titaniumCollected * 1000;
                stageScore += boltScore;
                stageScore += titaniumScore;
                if (stageTimeMs < parTimesMs[parIndex]) {
                    long t = (parTimesMs[parIndex] - stageTimeMs) / 1000;
                    timeBonus = (int) (t * 10);
                }
                stageScore += timeBonus;
                return;
            case 2:
                if (pacifistBonus) {
                    stageScore += 100000;
                }
                if (perfectBonus) {
                    stageScore += 100000;
                }
                if (boarBonus) {
                    stageScore += 10000;
                }
                break;
        }
    }

    /**
     * Loads all shared game graphics (advancing the menu's loading bar), sizes the screen
     * and camera limits, and creates the level, the player, the enemy and projectile pools
     * and the per-level work arrays.
     */
    public g(ratchetandclank midlet) {
        this.setFullScreenMode(true);
        this.app = midlet;
        this.display = Display.getDisplay(this.app);
        this.random = new Random();
        arenaListVariant = 0;
        try {
            imgHud = Image.createImage("/hud.png");
            this.app.menu.advanceLoading(2);
            imgDigits = Image.createImage("/digits.png");
            imgArrowRight = Image.createImage("/ar_r.png");
            this.app.menu.advanceLoading(2);
            imgPanelBottom = Image.createImage("/diap0.png");
            this.app.menu.advanceLoading(2);
            imgMapPanelTop = Image.createImage("/diap1.png");
            this.app.menu.advanceLoading(2);
            imgPanelTop = Image.createImage("/diap2.png");
            this.app.menu.advanceLoading(2);
            imgIcons = Image.createImage("/icons.png");
            this.app.menu.advanceLoading(2);
            imgDoors = Image.createImage("/doors.png");
            this.app.menu.advanceLoading(2);
            imgExplosion = Image.createImage("/explod.png");
            this.app.menu.advanceLoading(2);
            imgCrates = Image.createImage("/box.png");
            this.app.menu.advanceLoading(2);
            imgWeapons = Image.createImage("/weapon.png");
            this.app.menu.advanceLoading(2);
            imgBolts = Image.createImage("/bolts.png");
            this.app.menu.advanceLoading(2);
            imgPlatform = Image.createImage("/pltfrm.png");
            this.app.menu.advanceLoading(2);
            imgStartPoint = Image.createImage("/strtpt.png");
            this.app.menu.advanceLoading(2);
            imgMapIcons = Image.createImage("/mpicns.png");
            this.app.menu.advanceLoading(2);
            imgFlameBot = Image.createImage("/flmbot.png");
            this.app.menu.advanceLoading(2);
            imgWeaponWheel = Image.createImage("/wpn6angl.png");
            this.app.menu.advanceLoading(4);
            imgSpike = Image.createImage("/spike.png");
            this.app.menu.advanceLoading(2);
            imgPayola = Image.createImage("/payola.png");
            this.app.menu.advanceLoading(2);
            this.app.menu.advanceLoading(2);
            imgMenuHeader = Image.createImage("/menuhd.png");
            this.app.menu.advanceLoading(2);
            try {
                int n;
                InputStream in = this.getClass().getResourceAsStream("/ratchet.dat");
                imgPlayerFrames = new Image[28];
                for (n = 0; n < imgPlayerFrames.length; n++) {
                    imgPlayerFrames[n] = readPackedImage(in);
                    this.app.menu.advanceLoading(1);
                }
                in.close();
                System.gc();
                sleep(20);
            } catch (Exception ex) {
            }
            try {
                int n;
                InputStream in = this.getClass().getResourceAsStream("/enemy.dat");
                imgEnemyFrames = new Image[5][];
                imgEnemyFrames[0] = new Image[6];
                for (n = 0; n < imgEnemyFrames[0].length; n++) {
                    imgEnemyFrames[0][n] = readPackedImage(in);
                    this.app.menu.advanceLoading(1);
                }
                imgEnemyFrames[1] = new Image[14];
                for (n = 0; n < imgEnemyFrames[1].length; n++) {
                    imgEnemyFrames[1][n] = readPackedImage(in);
                    this.app.menu.advanceLoading(1);
                }
                imgEnemyFrames[2] = new Image[14];
                for (n = 0; n < imgEnemyFrames[2].length; n++) {
                    imgEnemyFrames[2][n] = readPackedImage(in);
                    this.app.menu.advanceLoading(1);
                }
                imgEnemyFrames[3] = new Image[4];
                for (n = 0; n < imgEnemyFrames[3].length; n++) {
                    imgEnemyFrames[3][n] = readPackedImage(in);
                    this.app.menu.advanceLoading(1);
                }
                imgEnemyFrames[4] = new Image[5];
                for (n = 0; n < imgEnemyFrames[4].length; n++) {
                    imgEnemyFrames[4][n] = readPackedImage(in);
                    this.app.menu.advanceLoading(1);
                }
                in.close();
                System.gc();
                sleep(20);
            } catch (Exception ex) {
            }
        } catch (IOException ex) {
            System.out.println("" + ex + ". percentLoaded=" + f.loadingProgress);
        }
        screenWidth = (short)this.getWidth();
        screenHeight = (short)this.getHeight();
        this.dialogueBoxY = 0;
        while (this.dialogueBoxY < screenHeight - tileHeight * 2) {
            this.dialogueBoxY += tileHeight;
        }
        this.dialogueBoxY += 12;
        titleFont.getHeight();
        this.debugKeysEnabled = false;
        totalScore = 0;
        minCamX = (short)(28 * -tileWidth + screenWidth);
        minCamY = (short)(18 * -tileHeight + screenHeight);
        if (lineHeight > hudHeight) {
            hudHeight = (byte)lineHeight;
        }
        this.layoutWeaponWheel();
        this.levelMap = new c(this);
        this.app.menu.advanceLoading(7);
        this.player = new a(this);
        this.player.initAnimations();
        this.app.menu.advanceLoading(5);
        this.enemies = new d[enemySlotCount];
        for (byte n = (byte)(enemySlotCount - 1); n >= 0; n--) {
            this.enemies[n] = new d(this);
        }
        this.enemies[0].initTables();
        this.app.menu.advanceLoading(5);
        this.playerShots = new h[10];
        for (int k = 9; k >= 0; k--) {
            this.playerShots[k] = new h(this);
        }
        this.enemyShots = new h[10];
        for (int m = 9; m >= 0; m--) {
            this.enemyShots[m] = new h(this);
        }
        this.app.menu.advanceLoading(2);
        cannonState = new byte[4];
        cannonFlash = new byte[4];
        bossHealth = new int[5];
        lockColumn = new byte[3];
        lockRow = new byte[3];
        doorKind = new byte[3];
        doorColumn = new byte[3];
        doorRow = new byte[3];
        railX1 = new short[4];
        railY1 = new short[4];
        railX2 = new short[4];
        railY2 = new short[4];
        railDx = new short[4];
        railDy = new int[4];
        railBase = new int[4];
        spikeX = new short[8];
        spikeY = new short[8];
        crateX = new short[50];
        crateY = new short[50];
        crateType = new byte[50];
        crateFall = new short[50];
        crateLandY = new short[50];
        crateIntactBits = new int[10];
        crateAbove = new short[50];
        crateFalling = new boolean[50];
        crateBelow = new short[50];
        roomRespawn = new boolean[6];
        enemyAliveBits = new int[2];
        pickupX = new int[12];
        pickupY = new int[12];
        pickupType = new byte[12];
        pickupVx = new short[12];
        pickupVy = new short[12];
        platformX = new short[4];
        platformY = new short[4];
        platformDx = new short[4];
        platformDy = new short[4];
        platformDir = new byte[4];
        roomEnemyCounts = new int[12];
        roomKillCounts = new int[12];
        roomCleared = new boolean[12];
        menuItemY = new int[13];
        mapGeometryA = new short[62][3];
        mapGeometryB = new byte[43][2];
        ammoPrices = new int[8];
        this.app.menu.advanceLoading(5);
        this.parseMapData();
        this.tilesetIndex = 3;
        this.app.menu.advanceLoading(5);
    }
    /**
     * Maps keys 2/4/5/6/8 and game actions, then dispatches to the handler of the current game state.
     */
    public final synchronized void keyPressed(int key) {
        if (loading) {
            return;
        }
        if (resumeTime > 0L && System.currentTimeMillis() - resumeTime < 1000L) {
            return;
        }
        int act = 0;
        if (key == 50) {
            act = 1;
        } else if (key == 56) {
            act = 6;
        } else if (key == 52) {
            act = 2;
        } else if (key == 54) {
            act = 5;
        } else if (key == 53) {
            act = 8;
        } else if (key == 8) {
            act = key;
        } else if (key != -6 && key != -7) {
            try {
                act = getGameAction(key);
            } catch (Exception e) {
                return;
            }
        }
        repaintRequested = true;
        switch (gameState) {
            case 0:
            case 16:
            case 17:
            case 24:
                handlePlayKey(key, act);
                break;
            case 4:
                pauseKeys(key, act);
                break;
            case 5:
                settingsKeys(key, act);
                break;
            case 6:
                newGameSlotKeys(key, act);
                break;
            case 7:
                quitGameKeys(key, act);
                break;
            case 8:
                quitToMenuKeys(key, act);
                break;
            case 9:
                saveOverKeys(key, act);
                break;
            case 10:
                savedScreenKeys(key, act);
                break;
            case 11:
                storeKeys(key, act);
                break;
            case 3:
                mapKeys(key, act);
                break;
            case 12:
                buyKeys(key, act);
                break;
            case 13:
                notEnoughBoltsKeys(key, act);
                break;
            case 1:
                arenaListKeys(key, act);
                break;
            case 2:
                gameWonKeys(key, act);
                break;
            case 14:
                arenaDescriptionKeys(key, act);
                break;
            case 15:
            case 19:
                failOrRewardKeys(key, act);
                break;
            case 18:
                summaryKeys(key, act);
                break;
            case 21:
                saveReplayKeys(key, act);
                break;
            case 22:
                weaponWheelKeys(key, act);
                break;
            case 23:
                challengeQuestionKeys(key, act);
                break;
        }
    }
    /** Key release (1/3 count as left/right): handled by handleKeyRelease(int, int). */
    public final synchronized void keyReleased(int key) {
        if (loading) {
            return;
        }
        int act = 0;
        if (key == 50) {
            act = 1;
        } else if (key == 56) {
            act = 6;
        } else if (key == 52 || key == 49) {
            act = 2;
        } else if (key == 54 || key == 51) {
            act = 5;
        } else if (key == 53) {
            act = 8;
        } else if (key == 8) {
            act = key;
        } else if (key != -6 && key != -7) {
            try {
                act = getGameAction(key);
            } catch (Exception e) {
                return;
            }
        }
        handleKeyRelease(key, act);
    }

    /** Draws a scrolling list of items e with its scroll bar; item id selectedId is highlighted. */
    public final void drawItemList(Graphics graphics, Vector items, int top, boolean centred, int barX, int barY, int barHeight, int selectedId) {
        if (items == null) {
            return;
        }
        if (items.size() == 0) {
            return;
        }
        int yy = top;
        app.menu.visibleLines = (175 - top) / lineHeight;
        if (app.menu.visibleLines < app.menu.lineCount) {
            int bh = app.menu.visibleLines * barHeight / app.menu.lineCount;
            int by = barY + app.menu.firstVisibleLine * (barHeight - bh) / (app.menu.lineCount - app.menu.visibleLines);
            graphics.setColor(0xDCDCFF);
            graphics.drawRect(barX, barY, 5, barHeight);
            graphics.fillRect(barX, by, 5, bh);
        }
        for (int n = app.menu.firstVisibleLine; n < app.menu.lineCount && n < app.menu.firstVisibleLine + app.menu.visibleLines; n++, yy += lineHeight) {
            e it = (e)items.elementAt(n);
            graphics.setFont(it.id == selectedId || it.id == -2 ? boldFont : smallFont);
            if (this.gameState == 12) {
                if (it.id == selectedId) {
                    app.menu.highlightedText = it.text;
                    app.menu.drawSelectionBox(graphics, tileWidth + (tileWidth >> 1), yy);
                    yy += 4;
                } else {
                    graphics.setColor(it.id == -2 ? 0x11EDEF : 0xDCDCFF);
                    graphics.drawString(it.text, screenWidth >> 1, yy, 17);
                    if (it.id >= 0) {
                        yy += 4;
                    }
                }
            } else {
                if (selectedId == -3) {
                    graphics.setColor(0xC0C0C0);
                } else {
                    graphics.setColor(it.id == selectedId || it.id == -2 ? (selectedId == -2 ? 0xFFFFFF : 0x11EDEF) : 0xDCDCFF);
                }
                if (!centred) {
                    graphics.drawString(it.text, 17, yy, 20);
                } else {
                    graphics.drawString(it.text, screenWidth >> 1, yy, 17);
                }
            }
        }
    }

    /** Draws the pause menu (continue, settings, main menu, exit) and the mission objective. */
    public final void drawPauseMenu(Graphics graphics) {
        int y;
        int x0 = this.maxOf(44, titleFont.stringWidth(ratchetandclank.strings[7]) + 5);
        int x1 = screenWidth - this.maxOf(44, titleFont.stringWidth(ratchetandclank.strings[8]) + 5);
        byte full = x0 == 44 && x1 == screenWidth - 44 ? (byte)1 : (byte)0;
        if (x1 - x0 < 60) {
            full = 0;
        }
        app.menu.drawBackground(graphics, full);
        graphics.setClip(0, 0, this.getWidth(), this.getHeight());
        y = this.drawTitle(graphics, ratchetandclank.strings[38]);
        graphics.setFont(smallFont);
        y = y + smallFont.getHeight();
        this.menuItemY[0] = y = 2 + y;
        this.menuItemY[1] = y = 2 + this.drawEntry(graphics, ratchetandclank.strings[39], tileWidth + (tileWidth >> 1), y, 0, this.menuCursor == 0);
        this.menuItemY[2] = y = 2 + this.drawEntry(graphics, ratchetandclank.strings[3], tileWidth + (tileWidth >> 1), y, 0, this.menuCursor == 1);
        this.menuItemY[3] = y = 2 + this.drawEntry(graphics, ratchetandclank.strings[41], tileWidth + (tileWidth >> 1), y, 0, this.menuCursor == 2);
        this.drawEntry(graphics, ratchetandclank.strings[42], tileWidth + (tileWidth >> 1), y, 0, this.menuCursor == 3);
        app.menu.drawSoftKeys(graphics, 7, 8, this);
        app.menu.drawSelectionBox(graphics, tileWidth >> 1, this.menuItemY[this.menuCursor]);
        int hh = Math.max(16, lineHeight + 1);
        if (full == 0 && x1 - x0 >= 60) {
            graphics.setColor(0);
            graphics.fillRoundRect(x0 - 2, screenHeight - hh - 2, x1 - x0 + 4, hh - 2, 5, 5);
            graphics.fillRect(x0 - 2, screenHeight - hh - 2 + 7, x1 - x0 + 4, hh);
        }
        graphics.setColor(255, 255, 255);
        if (x1 - x0 < 60) {
            this.drawMarquee(graphics, ratchetandclank.strings[213 + this.objectiveIndex], this.dialogueBoxY - lineHeight, 20, screenWidth - 20);
            return;
        }
        this.drawMarquee(graphics, ratchetandclank.strings[213 + this.objectiveIndex], screenHeight - (lineHeight + 1), x0, x1);
    }
    /** Draws the framed text panel used by the in-game menus. */
    public final void drawPanel(Graphics gr) {
        int u2;
        int r;
        int u4;
        int u5;
        int w = (r = screenWidth - 10) - 10 + 1;
        gr.setColor(36, 86, 100);
        gr.fillRect(10, 75 + imgPanelTop.getHeight(), w - 1, 106 - imgPanelBottom.getHeight() - imgPanelTop.getHeight());
        gr.fillRect(10 + imgPanelBottom.getWidth(), 181 - imgPanelBottom.getHeight(), w - imgPanelBottom.getWidth() * 2, imgPanelBottom.getHeight());
        gr.fillRect(10 + imgPanelBottom.getWidth(), 78, w - imgPanelBottom.getWidth() * 2, 9);
        gr.setColor(23, 186, 204);
        gr.drawLine(10 + imgPanelTop.getWidth(), 79, r - imgPanelTop.getWidth(), 79);
        gr.setColor(36, 138, 156);
        gr.drawLine(10 + imgPanelBottom.getWidth(), 179, r - imgPanelBottom.getWidth(), 179);
        gr.drawLine(11, 75 + imgPanelTop.getHeight(), 11, 181 - imgPanelBottom.getHeight());
        gr.drawLine(r - 2, 75 + imgPanelTop.getHeight(), r - 2, 181 - imgPanelBottom.getHeight());
        gr.drawImage(imgPanelBottom, 10, 181, 36);
        gr.drawRegion(imgPanelBottom, 0, 0, imgPanelBottom.getWidth(), imgPanelBottom.getHeight(), 2, r, 181, 40);
        gr.drawImage(imgPanelTop, 10, 75, 20);
        gr.drawRegion(imgPanelTop, 0, 0, imgPanelTop.getWidth(), imgPanelTop.getHeight(), 2, r, 75, 24);
    }

    /** Draws the in-game settings screen (sound on/off). */
    public final void drawGameSettings(Graphics graphics) {
        int i2;
        app.menu.drawBackground(graphics, (byte)0);
        graphics.setClip(0, 0, this.getWidth(), this.getHeight());
        this.drawPanel(graphics);
        this.drawTitle(graphics, ratchetandclank.strings[27]);
        graphics.setFont(smallFont);
        this.menuItemY[0] = 100;
        this.drawEntry(graphics, app.soundEnabled == true ? ratchetandclank.strings[28] : ratchetandclank.strings[29], tileWidth + (tileWidth >> 1), 100, 0, this.menuCursor == 0);
        app.menu.drawSoftKeys(graphics, 7, 8, this);
        app.menu.drawSelectionBox(graphics, tileWidth >> 1, this.menuItemY[this.menuCursor]);
    }

    /** Draws the save-slot chooser. */
    public final void drawSlotChooser(Graphics graphics) {
        int y;
        app.menu.drawBackground(graphics, (byte)0);
        graphics.setClip(0, 0, this.getWidth(), this.getHeight());
        this.drawPanel(graphics);
        this.drawTitle(graphics, ratchetandclank.strings[283]);
        graphics.setFont(smallFont);
        this.menuItemY[0] = 104;
        this.menuItemY[1] = y = 4 + this.drawSlotEntry(graphics, (byte)0, tileWidth + (tileWidth >> 1), 104, 0, this.menuCursor == 0);
        this.menuItemY[2] = y = 4 + this.drawSlotEntry(graphics, (byte)1, tileWidth + (tileWidth >> 1), y, 0, this.menuCursor == 1);
        this.drawSlotEntry(graphics, (byte)2, tileWidth + (tileWidth >> 1), y, 0, this.menuCursor == 2);
        app.menu.drawSoftKeys(graphics, 7, 8, this);
        app.menu.drawSelectionBox(graphics, tileWidth >> 1, this.menuItemY[this.menuCursor]);
    }

    /** Draws the buy-weapon / buy-ammo screen for the selected store item. */
    public final void drawBuy(Graphics graphics) {
        int i2 = 0;
        app.menu.drawBackground(graphics, (byte)0);
        graphics.setClip(0, 0, screenWidth, screenHeight);
        this.drawPanel(graphics);
        this.drawTitle(graphics, ratchetandclank.strings[47]);
        graphics.setFont(smallFont);
        if (this.selectedItem != 7) {
            this.player.weapon = (byte)(this.selectedItem + 1);
        }
        this.player.draw(graphics, 0, 0, -20);
        graphics.setClip(0, 0, this.getWidth(), this.getHeight());
        if (this.selectedItem == 7) {
            if (this.buyItems == null) {
                int wd = screenWidth - 36;
                this.buyItems = new Vector();
                f.appendItems(this.buyItems, ratchetandclank.wrapText(ratchetandclank.strings[58], wd), -1);
                f.appendItems(this.buyItems, ratchetandclank.wrapText(ratchetandclank.strings[295], wd), -2);
                Object[] args = { new Integer(this.bolts) };
                String s = this.format(ratchetandclank.strings[56], args);
                f.appendItems(this.buyItems, ratchetandclank.wrapText(s, wd), -1);
                args[0] = new Integer(this.player.allAmmoPrice);
                s = this.format(ratchetandclank.strings[57], args);
                f.appendItems(this.buyItems, ratchetandclank.wrapText(s, wd), -1);
                f.appendItem(this.buyItems, "", -1);
                f.appendItem(this.buyItems, ratchetandclank.strings[11], 0);
                f.appendItem(this.buyItems, ratchetandclank.strings[10], 1);
                app.menu.lineCount = this.buyItems.size();
                app.menu.firstVisibleLine = 0;
            }
            this.drawItemList(graphics, this.buyItems, 85, true, screenWidth - 18, 89, 80, this.menuCursor);
        } else if ((this.player.ownedWeapons & 1 << this.selectedItem + 1) != 0) {
            if (this.buyItems == null) {
                int wd = screenWidth - 36;
                this.buyItems = new Vector();
                f.appendItems(this.buyItems, ratchetandclank.wrapText(ratchetandclank.strings[58], wd), -1);
                f.appendItems(this.buyItems, ratchetandclank.wrapText(ratchetandclank.strings[49 + this.selectedItem], wd), -2);
                Object[] args = { new Integer(this.bolts) };
                String s = this.format(ratchetandclank.strings[56], args);
                f.appendItems(this.buyItems, ratchetandclank.wrapText(s, wd), -1);
                args[0] = new Integer(this.ammoPrices[this.selectedItem + 1]);
                s = this.format(ratchetandclank.strings[57], args);
                f.appendItems(this.buyItems, ratchetandclank.wrapText(s, wd), -1);
                f.appendItem(this.buyItems, "", -1);
                f.appendItem(this.buyItems, ratchetandclank.strings[11], 0);
                f.appendItem(this.buyItems, ratchetandclank.strings[10], 1);
                app.menu.lineCount = this.buyItems.size();
                app.menu.firstVisibleLine = 0;
            }
            this.drawItemList(graphics, this.buyItems, 85, true, screenWidth - 18, 89, 80, this.menuCursor);
        } else {
            if (this.buyItems == null) {
                int wd = screenWidth - 36;
                this.buyItems = new Vector();
                f.appendItems(this.buyItems, ratchetandclank.wrapText(ratchetandclank.strings[49 + this.selectedItem], wd), -2);
                Object[] args = { new Integer(this.bolts) };
                String s = this.format(ratchetandclank.strings[56], args);
                f.appendItems(this.buyItems, ratchetandclank.wrapText(s, wd), -1);
                args[0] = new Integer(a.weaponPrices[this.selectedItem]);
                s = this.format(ratchetandclank.strings[57], args);
                f.appendItems(this.buyItems, ratchetandclank.wrapText(s, wd), -1);
                f.appendItem(this.buyItems, "", -1);
                f.appendItem(this.buyItems, ratchetandclank.strings[11], 0);
                f.appendItem(this.buyItems, ratchetandclank.strings[10], 1);
                app.menu.lineCount = this.buyItems.size();
                app.menu.firstVisibleLine = 0;
            }
            this.drawItemList(graphics, this.buyItems, 85, true, screenWidth - 18, 89, 80, this.menuCursor);
        }
        app.menu.drawSoftKeys(graphics, 7, 8, this);
    }

    /** Draws the weapon store (weapon icons and the selection). */
    public final void drawStore(Graphics graphics) {
        int y = 0;
        int sel = 0;
        int ih = imgIcons.getHeight() / 12;
        int iw = imgIcons.getWidth();
        int bx = 52 + (screenWidth == 240 ? 32 : (screenWidth == 208 ? 16 : 0));
        int u7 = 0;
        app.menu.drawBackground(graphics, (byte)0);
        graphics.setClip(0, 0, screenWidth, screenHeight);
        this.drawTitle(graphics, ratchetandclank.strings[26]);
        graphics.setFont(smallFont);
        this.drawPanel(graphics);
        y = 85 + (ih + 4);
        if (this.menuCursor != 7) {
            this.player.weapon = (byte)(this.menuCursor + 1);
        }
        this.player.draw(graphics, 0, 0, -20);
        graphics.setClip(0, 0, this.getWidth(), this.getHeight());
        if (this.menuCursor == 2 && this.dialoguePending(3) && this.dialoguePending(8) || this.menuCursor == 3 && this.dialoguePending(8)) {
            if (this.storeItems == null) {
                int wd = screenWidth - 36;
                this.storeItems = new Vector();
                int w1 = smallFont.stringWidth(ratchetandclank.strings[49 + this.menuCursor]);
                int w2 = boldFont.stringWidth(ratchetandclank.strings[49 + this.menuCursor]);
                f.appendItems(this.storeItems, ratchetandclank.wrapText(ratchetandclank.strings[49 + this.menuCursor], wd - (w2 - w1)), -2);
                Object[] args = { new Integer(this.bolts) };
                String s = this.format(ratchetandclank.strings[56], args);
                f.appendItems(this.storeItems, ratchetandclank.wrapText(s, wd), -1);
                f.appendItems(this.storeItems, ratchetandclank.wrapText(ratchetandclank.strings[253], wd), -1);
                app.menu.lineCount = this.storeItems.size();
                app.menu.firstVisibleLine = 0;
            }
            this.drawItemList(graphics, this.storeItems, y, true, screenWidth - 18, 89, 80, -3);
            graphics.setColor(0xDCDCFF);
        } else if (this.menuCursor == 7) {
            if (this.storeItems == null) {
                int wd = screenWidth - 36;
                this.storeItems = new Vector();
                f.appendItems(this.storeItems, ratchetandclank.wrapText(ratchetandclank.strings[58], wd), -1);
                int w1 = smallFont.stringWidth(ratchetandclank.strings[295]);
                int w2 = boldFont.stringWidth(ratchetandclank.strings[295]);
                f.appendItems(this.storeItems, ratchetandclank.wrapText(ratchetandclank.strings[295], wd - (w2 - w1)), -2);
                Object[] args = { new Integer(this.bolts) };
                String s = this.format(ratchetandclank.strings[56], args);
                f.appendItems(this.storeItems, ratchetandclank.wrapText(s, wd), -1);
                args[0] = new Integer(this.player.allAmmoPrice);
                s = this.format(ratchetandclank.strings[57], args);
                f.appendItems(this.storeItems, ratchetandclank.wrapText(s, wd), -1);
                if (this.player.allAmmoPrice <= 0) {
                    f.appendItems(this.storeItems, ratchetandclank.wrapText(ratchetandclank.strings[257], wd), -1);
                }
                app.menu.lineCount = this.storeItems.size();
                app.menu.firstVisibleLine = 0;
            }
            this.drawItemList(graphics, this.storeItems, y, true, screenWidth - 18, 89, 80, -2);
        } else if ((this.player.ownedWeapons & 1 << this.menuCursor + 1) > 0) {
            if (this.storeItems == null) {
                int wd = screenWidth - 36;
                this.storeItems = new Vector();
                f.appendItems(this.storeItems, ratchetandclank.wrapText(ratchetandclank.strings[58], wd), -1);
                int w1 = smallFont.stringWidth(ratchetandclank.strings[49 + this.menuCursor]);
                int w2 = boldFont.stringWidth(ratchetandclank.strings[49 + this.menuCursor]);
                f.appendItems(this.storeItems, ratchetandclank.wrapText(ratchetandclank.strings[49 + this.menuCursor], wd - (w2 - w1)), -2);
                Object[] args = { new Integer(this.bolts) };
                String s = this.format(ratchetandclank.strings[56], args);
                f.appendItems(this.storeItems, ratchetandclank.wrapText(s, wd), -1);
                args[0] = new Integer(this.ammoPrices[this.menuCursor + 1]);
                s = this.format(ratchetandclank.strings[57], args);
                f.appendItems(this.storeItems, ratchetandclank.wrapText(s, wd), -1);
                if (this.player.ammo[this.menuCursor + 1] < a.maxAmmo[3 * (this.menuCursor + 1) + this.player.weaponLevels[this.menuCursor + 1]]) {
                    Object[] a2 = { new Integer(this.player.ammo[this.menuCursor + 1]), new Integer(a.maxAmmo[3 * (this.menuCursor + 1) + this.player.weaponLevels[this.menuCursor + 1]]) };
                    s = this.format(ratchetandclank.strings[255], a2);
                    f.appendItems(this.storeItems, ratchetandclank.wrapText(s, wd), -1);
                } else {
                    f.appendItems(this.storeItems, ratchetandclank.wrapText(ratchetandclank.strings[257], wd), -1);
                }
                app.menu.lineCount = this.storeItems.size();
                app.menu.firstVisibleLine = 0;
            }
            this.drawItemList(graphics, this.storeItems, y, true, screenWidth - 18, 89, 80, -2);
        } else {
            if (this.storeItems == null) {
                int wd = screenWidth - 36;
                this.storeItems = new Vector();
                int w1 = smallFont.stringWidth(ratchetandclank.strings[49 + this.menuCursor]);
                int w2 = boldFont.stringWidth(ratchetandclank.strings[49 + this.menuCursor]);
                f.appendItems(this.storeItems, ratchetandclank.wrapText(ratchetandclank.strings[49 + this.menuCursor], wd - (w2 - w1)), -2);
                Object[] args = { new Integer(this.bolts) };
                String s = this.format(ratchetandclank.strings[56], args);
                f.appendItems(this.storeItems, ratchetandclank.wrapText(s, wd), -1);
                args[0] = new Integer(a.weaponPrices[this.menuCursor]);
                s = this.format(ratchetandclank.strings[57], args);
                f.appendItems(this.storeItems, ratchetandclank.wrapText(s, wd), -1);
                app.menu.lineCount = this.storeItems.size();
                app.menu.firstVisibleLine = 0;
            }
            this.drawItemList(graphics, this.storeItems, y, true, screenWidth - 18, 89, 80, -2);
        }
        if ((sel = this.menuCursor + 1) == 7) {
            sel--;
            if ((this.player.ownedWeapons & 32) == 0) {
                sel--;
            }
        }
        if (this.menuCursor == 7) {
            sel--;
            if ((this.player.ownedWeapons & 128) == 0) {
                sel--;
            }
            if ((this.player.ownedWeapons & 32) == 0) {
                sel--;
            }
        }
        this.drawPageIndicator(graphics, sel, 5 + ((this.player.ownedWeapons & 32) > 0 ? 1 : 0) + ((this.player.ownedWeapons & 128) > 0 ? 1 : 0));
        app.menu.drawSoftKeys(graphics, 254, 8, this);
        graphics.setClip(0, 85, screenWidth, ih);
        if (this.menuAnimTick > 6) {
            graphics.drawRegion(imgArrowRight, 0, 0, imgArrowRight.getWidth(), imgArrowRight.getHeight(), 2, bx - 15, 86, 20);
        }
        for (int k = 0; k < 4 + ((this.player.ownedWeapons & 32) > 0 ? 1 : 0); k++) {
            graphics.drawImage(imgIcons, bx, 85 - k * ih, 0);
            bx += iw;
        }
        if ((this.player.ownedWeapons & 32) > 0) {
        } else {
            bx += iw;
        }
        if ((this.player.ownedWeapons & 128) > 0) {
            graphics.drawImage(imgIcons, bx, 85 - 6 * ih, 0);
        }
        bx += iw;
        graphics.drawImage(imgIcons, bx, 85 - 11 * ih, 0);
        if (this.menuAnimTick > 6) {
            graphics.drawImage(imgArrowRight, bx + iw + 9, 86, 20);
        }
        graphics.setColor(0xFFFFFF);
        bx = 52 + (screenWidth == 240 ? 32 : (screenWidth == 208 ? 16 : 0));
        if (this.menuCursor > 5) {
            graphics.drawRect(bx + (this.menuCursor - 1) * iw, 86, iw, ih - 2);
            return;
        }
        graphics.drawRect(bx + this.menuCursor * iw, 86, iw, ih - 2);
    }

    /** Builds the stage summary text (kills, bolts, titanium bolts, time, par, bonuses, score). */
    public final void buildSummary() {
        this.summaryLines = new Vector();
        int wd = screenWidth - 36;
        this.summaryLines = new Vector();
        String blank = new String("");
        f.appendItems(this.summaryLines, ratchetandclank.wrapText(ratchetandclank.strings[this.summaryMessageId], wd), -1);
        f.appendItem(this.summaryLines, blank, -1);
        f.appendItem(this.summaryLines, blank, -1);
        computeScore((byte)1);
        f.appendItems(this.summaryLines, ratchetandclank.wrapText(ratchetandclank.strings[297] + " " + kills, wd), -1);
        f.appendItems(this.summaryLines, ratchetandclank.wrapText(ratchetandclank.strings[298] + " " + boltsCollected, wd), -1);
        f.appendItems(this.summaryLines, ratchetandclank.wrapText(ratchetandclank.strings[299] + " " + titaniumCollected, wd), -1);
        f.appendItems(this.summaryLines, ratchetandclank.wrapText(ratchetandclank.strings[300] + " " + app.menu.formatPlayTime((int)stageTimeMs), wd), -1);
        f.appendItems(this.summaryLines, ratchetandclank.wrapText(ratchetandclank.strings[301] + " " + app.menu.formatPlayTime((int)parTimesMs[parIndex]), wd), -1);
        f.appendItem(this.summaryLines, blank, -1);
        computeScore((byte)2);
        if (pacifistBonus) {
            f.appendItems(this.summaryLines, ratchetandclank.wrapText(ratchetandclank.strings[302], wd), -2);
        }
        if (perfectBonus) {
            f.appendItems(this.summaryLines, ratchetandclank.wrapText(ratchetandclank.strings[303], wd), -2);
        }
        if (boarBonus) {
            f.appendItems(this.summaryLines, ratchetandclank.wrapText(ratchetandclank.strings[304], wd), -2);
        }
        if (pacifistBonus || perfectBonus || boarBonus) {
            f.appendItem(this.summaryLines, blank, -1);
        }
        f.appendItem(this.summaryLines, blank, -1);
        f.appendItems(this.summaryLines, ratchetandclank.wrapText(ratchetandclank.strings[305] + " " + stageScore, wd), -2);
        app.menu.lineCount = this.summaryLines.size();
        app.menu.firstVisibleLine = 0;
    }

    /** Draws the stage summary screen. */
    public final void drawSummary(Graphics graphics, byte variant) {
        int i3 = 0;
        int i4 = 0;
        int i5 = 0;
        int i6, i7, i8, i9;
        app.menu.drawBackground(graphics, (byte)0);
        graphics.setClip(0, 0, this.getWidth(), this.getHeight());
        this.drawPanel(graphics);
        this.drawTitle(graphics, ratchetandclank.strings[296]);
        graphics.setColor(0xFFFFFF);
        graphics.setFont(smallFont);
        graphics.getFont();
        this.drawItemList(graphics, this.summaryLines, 87, true, screenWidth - 18, 89, 80, 0);
        app.menu.drawSoftKeys(graphics, 9, -1, this);
    }

    /** Draws the level select map. */
    public final void drawLevelSelect(Graphics graphics) {
        try {
            int x;
            int y;
            int i;
            int lv;
            int fr;
            int u7;
            app.menu.drawBackground(graphics, (byte)0);
            graphics.setClip(0, 0, this.getWidth(), this.getHeight());
            this.drawTitle(graphics, ratchetandclank.strings[12]);
            int r = screenWidth - 11;
            int rb = r - 11 + 1;
            int l = 11 + imgPanelBottom.getWidth();
            int w = rb - imgPanelBottom.getWidth() * 2;
            int u12;
            int top = 180 - boldFont.getHeight() - 3 - 5;
            graphics.setColor(36, 86, 100);
            graphics.fillRect(11, top + imgMapPanelTop.getHeight(), rb - 1, 180 - top - imgPanelBottom.getHeight() - imgMapPanelTop.getHeight());
            graphics.fillRect(l, 180 - imgPanelBottom.getHeight(), w, imgPanelBottom.getHeight());
            graphics.drawLine(11 + imgMapPanelTop.getWidth(), top + 2, r - imgMapPanelTop.getWidth(), top + 2);
            graphics.setColor(23, 186, 204);
            graphics.drawLine(11 + imgMapPanelTop.getWidth(), top + 3, r - imgMapPanelTop.getWidth(), top + 3);
            graphics.setColor(36, 138, 156);
            graphics.drawLine(l, 178, r - imgPanelBottom.getWidth(), 178);
            graphics.drawLine(12, top + imgMapPanelTop.getHeight(), 12, 180 - imgPanelBottom.getHeight());
            graphics.drawLine(r - 2, top + imgMapPanelTop.getHeight(), r - 2, 180 - imgPanelBottom.getHeight());
            graphics.drawImage(imgPanelBottom, 11, 180, 36);
            graphics.drawRegion(imgPanelBottom, 0, 0, imgPanelBottom.getWidth(), imgPanelBottom.getHeight(), 2, r, 180, 40);
            graphics.drawImage(imgMapPanelTop, 11, top, 20);
            graphics.drawRegion(imgMapPanelTop, 0, 0, imgMapPanelTop.getWidth(), imgMapPanelTop.getHeight(), 2, r, top, 24);
            if (this.mapCaptionNode != this.menuCursor) {
                this.mapCaptionNode = this.menuCursor;
                this.mapCaption = ratchetandclank.strings[14 + this.mapNodeLevel[this.menuCursor] - 1];
                this.mapCaptionWidth = boldFont.stringWidth(this.mapCaption);
                this.mapCaptionX = l;
                this.marqueeScrollingLeft = true;
            }
            graphics.setFont(boldFont);
            graphics.setColor(0xFFFFFF);
            if (this.mapCaptionWidth <= w) {
                graphics.drawString(this.mapCaption, screenWidth >> 1, top + 6, 17);
            } else {
                graphics.setClip(l, top + 6, w, boldFont.getHeight());
                graphics.drawString(this.mapCaption, this.mapCaptionX, top + 6, 20);
                graphics.setClip(0, 0, screenWidth, screenHeight);
                if (this.marqueeScrollingLeft) {
                    this.mapCaptionX--;
                    if (this.mapCaptionX + this.mapCaptionWidth < l + w - 5) {
                        this.marqueeScrollingLeft = false;
                    }
                } else {
                    this.mapCaptionX++;
                    if (this.mapCaptionX >= l + 5) {
                        this.marqueeScrollingLeft = true;
                    }
                }
            }
            for (i = 0; i < 16; i++) {
                if ((this.mapUnlockBits & 1 << this.pathNode[i]) == 0) {
                    continue;
                }
                for (fr = 0; fr < this.pathDotCount[i]; fr++) {
                    x = this.mapGeometryB[this.pathDots[i][fr]][0];
                    y = this.mapGeometryB[this.pathDots[i][fr]][1];
                    int x0 = this.mapGeometryA[x][1];
                    int y0 = this.mapGeometryA[x][2];
                    int x1 = this.mapGeometryA[y][1];
                    int y1 = this.mapGeometryA[y][2];
                    graphics.setColor(20, 186, 204);
                    graphics.drawLine(x0, y0, x1, y1);
                    graphics.setColor(36, 86, 100);
                    if (this.mapGeometryA[x][1] == this.mapGeometryA[y][1]) {
                        graphics.drawLine(x0 - 1, y0, x1 - 1, y1);
                        graphics.drawLine(x0 + 1, y0, x1 + 1, y1);
                    } else {
                        graphics.drawLine(x0, y0 - 1, x1, y1 - 1);
                        graphics.drawLine(x0, y0 + 1, x1, y1 + 1);
                    }
                }
            }
            for (i = 0; i <= 19; i++) {
                lv = this.mapGeometryA[i][0];
                x = this.mapGeometryA[i][1];
                y = this.mapGeometryA[i][2];
                if (lv >= 0 && lv <= 16 || (lv >= 17 && lv <= 18 || lv == 19 && this.mapUnlockBits != 1572865) && (this.mapUnlockBits & 1 << lv) > 0) {
                    fr = 1;
                    if (lv >= 0 && lv <= 16 && (this.mapUnlockBits & 1 << lv) > 0) {
                        fr = 0;
                    }
                    if (lv == 17) {
                        fr = 6;
                    } else if (lv == 18) {
                        fr = 2;
                    } else if (lv == 19) {
                        fr = 4;
                    }
                    if (lv == 17 && this.menuAnimTick > 10) {
                        fr++;
                    }
                    if ((this.mapHighlightBits & 1 << lv) > 0 && this.menuAnimTick > 5) {
                        graphics.setColor(255, 0, 0);
                        graphics.setClip(0, 0, screenWidth, screenHeight);
                        if (lv == 19 || lv == 17) {
                            graphics.fillArc(x - 1, y - 1, 21, 21, 0, 360);
                        } else {
                            graphics.fillArc(x + 4, y + 4, 11, 11, 0, 360);
                        }
                    }
                    graphics.setClip(x, y, 19, 19);
                    graphics.drawImage(imgMapIcons, x, y - fr * 19, 0);
                    if (this.menuCursor == lv) {
                        graphics.setColor(255, 255, 255);
                        graphics.setClip(0, 0, screenWidth, screenHeight);
                        if (lv == 19 || lv == 17 || lv == 18) {
                            graphics.drawLine(x, y, x + 8, y);
                            graphics.drawLine(x, y, x, y + 8);
                            graphics.drawLine(x + 19, y, x + 11, y);
                            graphics.drawLine(x + 19, y, x + 19, y + 8);
                            graphics.drawLine(x, y + 19, x + 8, y + 19);
                            graphics.drawLine(x, y + 19, x, y + 11);
                            graphics.drawLine(x + 19, y + 19, x + 11, y + 19);
                            graphics.drawLine(x + 19, y + 19, x + 19, y + 11);
                        } else {
                            graphics.drawLine(x + 4, y + 4, x + 15, y + 4);
                            graphics.drawLine(x + 4, y + 15, x + 15, y + 15);
                            graphics.drawLine(x + 4, y + 15, x + 4, y + 4);
                            graphics.drawLine(x + 15, y + 15, x + 15, y + 4);
                        }
                    }
                }
            }
            graphics.setClip(0, 0, this.getWidth(), this.getHeight());
            if (this.mapNodeLevel[this.menuCursor] < 11) {
                graphics.setFont(smallFont);
                int mx = titaniumPerLevel[this.mapNodeLevel[this.menuCursor] - 1];
                String s = this.titaniumCollectedInLevel(this.mapNodeLevel[this.menuCursor]) + "/" + mx;
                int ih = imgIcons.getHeight() / 12;
                int iw = imgIcons.getWidth();
                int bw = smallFont.stringWidth(s) + 7;
                int bh = lineHeight + ih + 3 + 4;
                int u20;
                int by = top - 2 - bh;
                graphics.setColor(36, 86, 100);
                graphics.fillRoundRect(11, by, bw, bh, 3, 3);
                graphics.setColor(23, 186, 204);
                graphics.drawRoundRect(12, by + 1, bw - 3 - 1, bh - 3 - 1, 3, 3);
                graphics.setColor(0x14EAEC);
                graphics.drawString(s, 15, by + 3 + ih + 1, 20);
                graphics.setClip(11 + (bw - iw >> 1), by + 3, iw, ih);
                graphics.drawImage(imgIcons, 11 + (bw - iw >> 1), by + 3 - ih * 9, 20);
            }
        } catch (Exception ex) {
            System.out.println("ERROR. drawLevelSelect: " + ex);
        }
        app.menu.drawSoftKeys(graphics, 7, -1, this);
    }

    /** Draws the "game saved" screen. */
    public final void drawGameSaved(Graphics graphics) {
        int i2;
        app.menu.drawBackground(graphics, (byte)0);

        graphics.setClip(0, 0, screenWidth, screenHeight);
        this.drawPanel(graphics);
        this.drawTitle(graphics, ratchetandclank.strings[43]);
        graphics.setFont(smallFont);
        this.drawWrappedTo(graphics, ratchetandclank.strings[285], tileWidth >> 1, 110, 17, screenWidth - (tileWidth >> 1));
        app.menu.drawSoftKeys(graphics, 9, -1, this);
    }

    /** Draws the "you win" screen with the reward picture. */
    public final void drawGameWon(Graphics graphics) {
        int y = 0;
        int cx = screenWidth >> 1;
        int i4, i5, i6;
        int i7 = 0;
        graphics.setClip(0, 0, screenWidth, screenHeight);
        graphics.setColor(0);
        graphics.fillRect(0, 0, screenWidth, screenHeight);
        app.menu.drawBackground(graphics, (byte)0);
        graphics.setClip(0, 0, screenWidth, screenHeight);
        y = this.drawTitle(graphics, ratchetandclank.strings[75]);
        y += graphics.getFont().getHeight() >> 1;
        byte[] r = winPictureFrames[this.winFrame];
        graphics.drawRegion(imgWinPicture, r[0] & 255, r[1] & 255, r[2], r[3], 0, cx - 22 + r[4], y + r[5], 20);
        graphics.setClip(0, 0, screenWidth, screenHeight);
        this.drawPanel(graphics);
        graphics.setColor(0xFFFFFF);
        graphics.setFont(smallFont);
        this.drawItemList(graphics, this.summaryLines, 85, true, screenWidth - 18, 89, 80, 0);
        app.menu.drawSoftKeys(graphics, 9, -1, this);
    }

    /** Draws the "you fail" screen. */
    public final void drawYouFail(Graphics graphics) {
        int i2;
        app.menu.drawBackground(graphics, (byte)0);

        graphics.setClip(0, 0, this.getWidth(), this.getHeight());
        this.drawPanel(graphics);
        this.drawTitle(graphics, ratchetandclank.strings[308]);
        graphics.setFont(smallFont);
        this.drawWrapped(graphics, ratchetandclank.strings[309], screenWidth >> 1, 131, 33);
        app.menu.drawSoftKeys(graphics, 9, -1, this);
    }

    /** Draws the arena reward screen (bolts won, next arena or Boar-Zooka unlocked). */
    public final void drawArenaReward(Graphics graphics) {
        int y;
        app.menu.drawBackground(graphics, (byte)0);
        graphics.setClip(0, 0, this.getWidth(), this.getHeight());
        this.drawPanel(graphics);
        this.drawTitle(graphics, ratchetandclank.strings[75]);
        graphics.setFont(smallFont);
        if (this.currentRoom < 11) {
            y = this.drawWrappedTo(graphics, ratchetandclank.strings[251], 20, 87, 0, screenWidth - 10);
        } else {
            y = this.drawWrappedTo(graphics, ratchetandclank.strings[252], 20, 87, 0, screenWidth - 10);
        }
        Object[] args = { new Integer(arenaRewards[this.currentRoom]) };
        this.textBoxText = null;
        this.textBoxText = this.format(ratchetandclank.strings[250], args);
        this.drawWrappedTo(graphics, this.textBoxText, 20, y, 0, screenWidth - 10);
        app.menu.drawSoftKeys(graphics, 9, -1, this);
    }

    /** Draws the arena description. */
    public final void drawArenaDescription(Graphics graphics) {
        int i2;
        app.menu.drawBackground(graphics, (byte)0);

        graphics.setClip(0, 0, this.getWidth(), this.getHeight());
        this.drawPanel(graphics);
        this.drawTitle(graphics, ratchetandclank.strings[24]);
        graphics.setFont(smallFont);
        app.menu.drawTextList(graphics, this.arenaDescription, 85, false, screenWidth - 18, 89, 80);
        app.menu.drawSoftKeys(graphics, 9, 8, this);
    }

    /** Draws the arena list. */
    public final void drawArenaList(Graphics graphics, byte variant) {
        int i3, i4, i5, i6, i7;
        graphics.setClip(0, 0, screenWidth, screenHeight);
        graphics.setColor(0);
        graphics.fillRect(0, 0, screenWidth, screenHeight);
        app.menu.drawBackground(graphics, (byte)0);
        graphics.setClip(0, 0, screenWidth, screenHeight);
        this.drawPanel(graphics);
        this.drawTitle(graphics, ratchetandclank.strings[24]);
        this.drawItemList(graphics, this.arenaItems, 85, true, screenWidth - 18, 89, 80, this.menuCursor);
        app.menu.drawSoftKeys(graphics, 7, 8, this);
    }

    /** Draws the "play again in challenge mode?" question. */
    public final void drawChallengeQuestion(Graphics graphics) {
        int y;
        app.menu.drawBackground(graphics, (byte)0);
        graphics.setClip(0, 0, this.getWidth(), this.getHeight());
        this.drawPanel(graphics);
        this.drawTitle(graphics, ratchetandclank.strings[282]);
        graphics.setFont(smallFont);
        this.menuItemY[0] = 100;
        this.menuItemY[1] = y = 4 + this.drawEntry(graphics, ratchetandclank.strings[11], tileWidth + (tileWidth >> 1), 100, 0, this.menuCursor == 0);
        this.drawEntry(graphics, ratchetandclank.strings[10], tileWidth + (tileWidth >> 1), y, 0, this.menuCursor == 1);
        app.menu.drawSoftKeys(graphics, 7, -1, this);
        app.menu.drawSelectionBox(graphics, tileWidth >> 1, this.menuItemY[this.menuCursor]);
    }
    /** Computes the positions of the seven slots of the weapon select wheel. */
    public final void layoutWeaponWheel() {
        int cx = screenWidth >> 1;
        wheelIconSize = imgWeaponWheel.getWidth();
        int hw = wheelIconSize >> 1;
        wheelCentreY = hudHeight + (screenHeight - (80 + wheelIconSize / 2 + 5 + lineHeight * 3) >> 1) + 40;
        wheelSlotPositions[0][0] = (short) (cx - 28 - hw);
        wheelSlotPositions[0][1] = (short) (wheelCentreY - 24 - hw);
        wheelSlotPositions[1][0] = (short) (cx - hw);
        wheelSlotPositions[1][1] = (short) (wheelCentreY - 40 - hw);
        wheelSlotPositions[2][0] = (short) (cx + 28 - hw);
        wheelSlotPositions[2][1] = (short) (wheelCentreY - 24 - hw);
        wheelSlotPositions[3][0] = (short) (cx - 40 - hw);
        wheelSlotPositions[3][1] = (short) (wheelCentreY - 5);
        wheelSlotPositions[4][0] = (short) (cx + 40 - hw);
        wheelSlotPositions[4][1] = (short) (wheelCentreY - 5);
        wheelSlotPositions[5][0] = (short) (cx - 28);
        wheelSlotPositions[5][1] = (short) (wheelCentreY + 32 - hw);
        wheelSlotPositions[6][0] = (short) (cx + 28 - wheelIconSize);
        wheelSlotPositions[6][1] = (short) (wheelCentreY + 32 - hw);
    }

    /** Draws the weapon select wheel with the weapon level and ammo. */
    public final void drawWeaponWheel(Graphics graphics) {
        int y;
        int iw = imgIcons.getWidth();
        int ih = imgIcons.getHeight() / 12;
        int hs = hudHeight >> 1;
        graphics.setClip(0, 0, screenWidth, screenHeight);
        graphics.setColor(0);
        graphics.setFont(smallFont);
        int cx = screenWidth >> 1;
        int cy = this.wheelCentreY;
        if (this.levelIndex == 11) {
            cy = cy - hs;
        }
        graphics.setFont(smallFont);
        graphics.setColor(34, 85, 102);
        graphics.fillArc(cx - 40, cy - 40, 80, 80, 0, 360);
        graphics.setColor(33, 129, 151);
        graphics.drawArc(cx - 40, cy - 40, 80, 80, 0, 360);
        graphics.setColor(15, 51, 63);
        graphics.drawArc(cx - 40 + 1, cy - 40 + 1, 79, 79, 0, 360);
        graphics.setColor(26, 70, 85);
        graphics.drawArc(cx - 40 + 2, cy - 40 + 2, 78, 78, 0, 360);
        for (int n = 1; n <= 7; n++) {
            graphics.drawImage(imgWeaponWheel, wheelSlotPositions[n - 1][0], wheelSlotPositions[n - 1][1] - (this.levelIndex == 11 ? hs : 0), 20);
            if ((this.player.ownedWeapons & 1 << n) > 0) {
                int ox = imgWeaponWheel.getWidth() - iw >> 1;
                int oy = imgWeaponWheel.getHeight() - ih >> 1;
                graphics.setClip(wheelSlotPositions[n - 1][0] + ox, wheelSlotPositions[n - 1][1] - (this.levelIndex == 11 ? hs : 0) + oy, iw, ih);
                graphics.drawImage(imgIcons, wheelSlotPositions[n - 1][0] + ox, wheelSlotPositions[n - 1][1] - (this.levelIndex == 11 ? hs : 0) + oy - ih * (n - 1), 0);
                graphics.setClip(0, 0, screenWidth, screenHeight);
            }
            if (n == this.player.weapon) {
                this.drawOctagon(graphics, wheelSlotPositions[n - 1][0], wheelSlotPositions[n - 1][1] - (this.levelIndex == 11 ? hs : 0));
            }
        }
        y = cy + 40 + 5;
        String name = ratchetandclank.strings[48 + this.player.weapon];
        String lv = ratchetandclank.strings[310] + " " + (this.player.weaponLevels[this.player.weapon] + 1);
        String am = this.player.weapon != 6 ? ratchetandclank.strings[232] + ": " + this.player.ammo[this.player.weapon] : "";
        int w;
        if ((w = Math.max(Math.max(smallFont.stringWidth(name), smallFont.stringWidth(lv)), smallFont.stringWidth(am))) >= screenWidth) {
            if (this.wheelCaptionWeapon != this.player.weapon) {
                this.wheelCaptionWeapon = this.player.weapon;
                this.wheelCaption = name;
                this.wheelCaptionWidth = smallFont.stringWidth(name);
                this.wheelCaptionX = 0;
                this.marqueeScrollingLeft = true;
            }
            w = screenWidth;
        } else {
            this.wheelCaption = null;
            this.wheelCaptionWeapon = -1;
        }
        if (w > this.wheelBoxWidth) {
            this.wheelBoxWidth = w;
        }
        graphics.setColor(34, 85, 102);
        graphics.fillRoundRect(screenWidth - (this.wheelBoxWidth + 10) >> 1, y, this.wheelBoxWidth + 10, lineHeight * 3 + 4, 5, 5);
        graphics.setColor(0xFFFFFF);
        if (this.wheelCaption == null) {
            y = this.drawWrapped(graphics, name, tileWidth + (tileWidth >> 1), y + 2, 17);
        } else {
            graphics.drawString(name, this.wheelCaptionX, y + 2, 20);
            graphics.setClip(0, 0, screenWidth, screenHeight);
            if (this.marqueeScrollingLeft) {
                this.wheelCaptionX--;
                if (this.wheelCaptionX + this.wheelCaptionWidth < screenWidth - 5) {
                    this.marqueeScrollingLeft = false;
                }
            } else {
                this.wheelCaptionX++;
                if (this.wheelCaptionX >= 5) {
                    this.marqueeScrollingLeft = true;
                }
            }
            y = y + (lineHeight + 2);
        }
        y = this.drawWrapped(graphics, lv, tileWidth + (tileWidth >> 1), y, 17);
        if (this.player.weapon != 6) {
            this.drawWrapped(graphics, am, tileWidth + (tileWidth >> 1), y, 17);
        }
    }
    /** Draws the octagonal selection frame at (x, y). */
    public final void drawOctagon(Graphics gr, int x, int y) {
        int i = 0;
        gr.setColor(0x12EEEE);
        for (i = 0; i < 7; i++) {
            gr.drawLine(octagonOuterX[i] + x, octagonOuterY[i] + y, octagonOuterX[i + 1] + x, octagonOuterY[i + 1] + y);
            gr.drawLine(octagonInnerX[i] + x, octagonInnerY[i] + y, octagonInnerX[i + 1] + x, octagonInnerY[i + 1] + y);
        }
        gr.drawLine(octagonOuterX[7] + x, octagonOuterY[7] + y, octagonOuterX[0] + x, octagonOuterY[0] + y);
        gr.drawLine(octagonInnerX[7] + x, octagonInnerY[7] + y, octagonInnerX[0] + x, octagonInnerY[0] + y);
    }

    /** Draws the "not enough bolts" screen. */
    public final void drawNotEnoughBolts(Graphics graphics) {
        int i2;
        app.menu.drawBackground(graphics, (byte)0);

        graphics.setClip(0, 0, screenWidth, screenHeight);
        this.drawPanel(graphics);
        this.drawTitle(graphics, ratchetandclank.strings[59]);
        graphics.setFont(smallFont);
        smallFont.getHeight();
        app.menu.drawSoftKeys(graphics, -1, 8, this);
    }

    /** Draws the "save over data?" question. */
    public final void drawSaveOverQuestion(Graphics graphics) {
        int y;
        app.menu.drawBackground(graphics, (byte)0);
        graphics.setClip(0, 0, this.getWidth(), this.getHeight());
        this.drawPanel(graphics);
        this.drawTitle(graphics, ratchetandclank.strings[44]);
        graphics.setFont(smallFont);
        this.menuItemY[0] = 100;
        this.menuItemY[1] = y = 4 + this.drawEntry(graphics, ratchetandclank.strings[11], tileWidth + (tileWidth >> 1), 100, 0, this.menuCursor == 0);
        this.drawEntry(graphics, ratchetandclank.strings[10], tileWidth + (tileWidth >> 1), y, 0, this.menuCursor == 1);
        app.menu.drawSoftKeys(graphics, 7, 8, this);
        app.menu.drawSelectionBox(graphics, tileWidth >> 1, this.menuItemY[this.menuCursor]);
    }

    /** Draws the save / replay level choice. */
    public final void drawSaveReplay(Graphics graphics) {
        int y;
        app.menu.drawBackground(graphics, (byte)0);
        graphics.setClip(0, 0, this.getWidth(), this.getHeight());
        this.drawPanel(graphics);
        this.drawTitle(graphics, ratchetandclank.strings[281]);
        graphics.setFont(smallFont);
        this.menuItemY[0] = 100;
        this.menuItemY[1] = y = this.drawEntry(graphics, ratchetandclank.strings[40], tileWidth + (tileWidth >> 1), 100, 0, this.menuCursor == 0) + 5;
        this.drawEntry(graphics, ratchetandclank.strings[280], tileWidth + (tileWidth >> 1), y, 0, this.menuCursor == 1);
        app.menu.drawSoftKeys(graphics, 7, -1, this);
        app.menu.drawSelectionBox(graphics, tileWidth >> 1, this.menuItemY[this.menuCursor]);
    }

    /** Draws the "quit to main menu?" question. */
    public final void drawQuitToMenuQuestion(Graphics graphics) {
        int y;
        app.menu.drawBackground(graphics, (byte)0);
        graphics.setClip(0, 0, this.getWidth(), this.getHeight());
        this.drawPanel(graphics);
        this.drawTitle(graphics, ratchetandclank.strings[45]);
        graphics.setFont(smallFont);
        this.menuItemY[0] = 100;
        this.menuItemY[1] = y = 4 + this.drawEntry(graphics, ratchetandclank.strings[11], tileWidth + (tileWidth >> 1), 100, 0, this.menuCursor == 0);
        this.drawEntry(graphics, ratchetandclank.strings[10], tileWidth + (tileWidth >> 1), y, 0, this.menuCursor == 1);
        app.menu.drawSoftKeys(graphics, 7, 8, this);
        app.menu.drawSelectionBox(graphics, tileWidth >> 1, this.menuItemY[this.menuCursor]);
    }

    /** Draws the "really quit game?" question. */
    public final void drawQuitGameQuestion(Graphics graphics) {
        int y;
        app.menu.drawBackground(graphics, (byte)0);
        graphics.setClip(0, 0, this.getWidth(), this.getHeight());
        this.drawPanel(graphics);
        this.drawTitle(graphics, ratchetandclank.strings[46]);
        graphics.setFont(smallFont);
        this.menuItemY[0] = 100;
        this.menuItemY[1] = y = 4 + this.drawEntry(graphics, ratchetandclank.strings[11], tileWidth + (tileWidth >> 1), 100, 0, this.menuCursor == 0);
        this.drawEntry(graphics, ratchetandclank.strings[10], tileWidth + (tileWidth >> 1), y, 0, this.menuCursor == 1);
        app.menu.drawSoftKeys(graphics, 7, 8, this);
        app.menu.drawSelectionBox(graphics, tileWidth >> 1, this.menuItemY[this.menuCursor]);
    }

    /**
     * Paints the current state: menu screens through their painters; otherwise the play field
     * (tiles, objects, actors, projectiles, HUD, dialogue box and overlays).
     */
    public final synchronized void paint(Graphics graphics) {
        if (this.loading) {
            return;
        }
        if (this.pendingLevel != -1 || this.pendingSlot != -1 || this.exitToMenu) {
            graphics.setColor(0);
            graphics.fillRect(0, 0, screenWidth, screenHeight);
            this.repaintRequested = true;
            System.gc();
            return;
        }
        switch (this.gameState) {
            case 4:
                this.drawPauseMenu(graphics);
                return;
            case 5:
                this.drawGameSettings(graphics);
                return;
            case 6:
                this.drawSlotChooser(graphics);
                return;
            case 7:
                this.drawQuitGameQuestion(graphics);
                return;
            case 8:
                this.drawQuitToMenuQuestion(graphics);
                return;
            case 9:
                this.drawSaveOverQuestion(graphics);
                return;
            case 10:
                this.drawGameSaved(graphics);
                return;
            case 11:
                this.drawStore(graphics);
                return;
            case 3:
                this.drawLevelSelect(graphics);
                return;
            case 12:
                this.drawBuy(graphics);
                return;
            case 13:
                this.drawNotEnoughBolts(graphics);
                return;
            case 1:
                this.drawArenaList(graphics, arenaListVariant);
                return;
            case 2:
                this.drawGameWon(graphics);
                return;
            case 14:
                this.drawArenaDescription(graphics);
                return;
            case 15:
                this.drawArenaReward(graphics);
                return;
            case 19:
                this.drawYouFail(graphics);
                return;
            case 18:
                this.drawSummary(graphics, summaryVariant);
                return;
            case 21:
                this.drawSaveReplay(graphics);
                return;
            case 22:
                this.drawWeaponWheel(graphics);
                return;
            case 23:
                this.drawChallengeQuestion(graphics);
                return;
        }
        if (this.fallingDeath == true) {
            this.player.blinkTimer = 1;
        }
        this.levelMap.draw(graphics);
        this.drawDoors(graphics);
        this.drawRails(graphics);
        this.drawCrates(graphics);
        this.drawPickups(graphics);
        this.drawPlatforms(graphics);
        if (this.armModuleColumn != -1) {
            this.drawArmModule(graphics);
        }
        if (this.payolaColumn != -1) {
            this.drawPayola(graphics);
        }
        if (this.bounceBotColumn != -1) {
            this.drawBounceBot(graphics);
        }
        this.drawActors(graphics);
        this.drawSpikes(graphics);
        if (this.gameState == 16 && this.bossCutsceneStep == 4) {
            this.drawMaximillian(graphics, this.maxFrame);
        }
        if (this.levelIndex == 12) {
            this.drawBoss(graphics);
        }
        if (this.gameState == 16 || this.gameState == 17 || this.gameState == 24) {
            if (this.gameState == 24) {
                this.drawBoss(graphics);
                for (int n = 9; n >= 0; n--) {
                    this.playerShots[n].draw(graphics);
                }
            }
            graphics.setClip(0, 0, screenWidth, hudHeight);
            graphics.setColor(0);
            graphics.fillRect(0, 0, screenWidth, hudHeight);
            graphics.setClip(0, screenHeight - hudHeight, screenWidth, hudHeight);
            graphics.setColor(0);
            graphics.fillRect(0, screenHeight - hudHeight, screenWidth, hudHeight);
            app.menu.drawSoftKeys(graphics, -1, -1, this);
        }
        if (this.levelIndex == 11) {
            graphics.setClip(0, screenHeight - hudHeight, screenWidth, hudHeight);
            graphics.setColor(0);
            graphics.fillRect(0, screenHeight - hudHeight, screenWidth, hudHeight);
            graphics.setColor(0x1CBACC);
            graphics.drawString(ratchetandclank.strings[311] + " " + String.valueOf(this.enemiesLeft), screenWidth >> 1, screenHeight - hudHeight + 3, 17);
        }
        if (this.hudDirty == true) {
            this.drawHud(graphics);
            this.hudDirty = false;
        }
        if (this.textBoxOpen == true) {
            this.drawTextBox(graphics);
            this.nextTextPos = this.drawTextBoxPage(graphics, this.textPos);
        }
        if (this.debugOverlay == 2 && this.frameMs > 0) {
            graphics.setColor(0xFFFFFF);
            graphics.setFont(smallFont);
            graphics.drawString("" + 1000 / this.frameMs + "." + 100000 / this.frameMs % 100, 0, hudHeight, 20);
        }
    }

    /** Scripted sequence step {@code step} of the boss-level cut-scene (state 16). */
    public final void bossCutsceneTick(byte step) {
        switch (step) {
            case 0:
                this.player.animate();
                this.repaintRequested = true;
                this.player.posX += 1536;
                if ((this.player.posX >> 8) > 9 * tileWidth) {
                    this.bossCutsceneStep++;
                    this.player.speedY = this.player.opusK;
                    this.player.speedX = 512;
                }
                break;
            case 1:
                this.player.animate();
                this.player.setAnimation((byte)2);
                this.repaintRequested = true;
                this.player.speedY += 256;
                this.player.rowOffset += this.player.speedY;
                this.player.posX += this.player.speedX;
                this.player.normaliseRow();
                int gy = this.player.floorHeight(false);
                if (this.player.pixelY() >= gy && this.player.speedY > 0) {
                    this.player.setAnimation((byte)4);
                    this.player.rowOffset = 0;
                    this.player.speedY = this.player.opusK;
                    this.bossCutsceneStep++;
                }
                break;
            case 2:
                if (this.bossCutsceneJumps < 4) {
                    this.player.animate();
                    this.player.setAnimation((byte)2);
                    this.repaintRequested = true;
                    this.player.speedY += 256;
                    this.player.rowOffset += this.player.speedY;
                    this.player.normaliseRow();
                    gy = this.player.floorHeight(false);
                    if (this.player.pixelY() >= gy && this.player.speedY > 0) {
                        this.player.setAnimation((byte)4);
                        this.player.rowOffset = 0;
                        this.player.speedY = this.player.opusK;
                        this.player.animMode = 0;
                        this.bossCutsceneJumps++;
                    }
                } else {
                    this.bossCutsceneStep++;
                    this.player.setAnimation((byte)1);
                }
                break;
            case 3:
                this.player.animate();
                this.repaintRequested = true;
                this.player.posX += 1536;
                if ((this.player.posX >> 8) > 11 * tileWidth + (tileWidth >> 1)) {
                    this.bossCutsceneStep++;
                    this.player.setAnimation((byte)0);
                    this.player.animMode = 0;
                    this.player.animate();
                }
        }
    }

    /** Scripted sequence step {@code step} of the arena-intro cut-scene (state 24). */
    public final void arenaCutsceneStep(byte step) {
        switch (step) {
            case 0:
                this.spawnCutsceneExplosion();
                this.player.animate();
                if (this.player.jumpState != -1) {
                    int gy0 = this.player.floorHeight(false);
                    this.repaintRequested = true;
                    this.player.posX += this.player.speedX;
                    this.player.speedY += 256;
                    this.player.rowOffset += this.player.speedY;
                    this.player.normaliseRow();
                    if (this.player.pixelY() >= gy0) {
                        this.player.setAnimation((byte)4);
                        this.player.rowOffset = 0;
                        this.player.jumpState = -1;
                    }
                } else if (this.player.tileRow <= 8 && (this.player.posX >> 8) < 13 * tileWidth) {
                    if ((this.player.posX >> 8) > 11 * tileWidth + (tileWidth >> 1)) {
                        this.player.setAnimation((byte)0);
                        this.player.animMode = 0;
                        this.cutsceneStep = 4;
                        return;
                    }
                    this.player.setAnimation((byte)1);
                    this.player.animMode = 0;
                    this.cutsceneStep = 3;
                    return;
                } else if ((this.player.posX >> 8) < 10 * tileWidth + (tileWidth >> 1)) {
                    this.cutsceneStep = 2;
                    this.player.speedY = this.player.opusK;
                    return;
                } else {
                    this.player.setAnimation((byte)1);
                    this.player.animMode = 0;
                    this.player.facingRight = false;
                    this.player.speedX = 0;
                    this.player.speedY = 0;
                    this.cutsceneJumping = false;
                    this.cutsceneStep++;
                }
                this.spawnCutsceneExplosion();
                break;
            case 1:
                int gy = this.player.floorHeight(false);
                this.player.animate();
                this.repaintRequested = true;
                if (this.cutsceneJumping) {
                    gy = this.player.floorHeight(false);
                    this.repaintRequested = true;
                    this.player.posX += this.player.speedX;
                    this.player.speedY += 256;
                    this.player.rowOffset += this.player.speedY;
                    this.player.normaliseRow();
                    if (this.player.pixelY() >= gy) {
                        this.player.setAnimation((byte)1);
                        this.player.rowOffset = 0;
                        this.cutsceneJumping = false;
                    }
                } else {
                    if (this.player.pixelY() < gy) {
                        this.player.setAnimation((byte)3);
                        this.cutsceneJumping = true;
                    }
                    this.player.posX -= 1536;
                    if ((this.player.posX >> 8) < 10 * tileWidth + (tileWidth >> 1)) {
                        this.player.setAnimation((byte)0);
                        this.cutsceneStep++;
                        this.player.speedY = this.player.opusK;
                    }
                }
                this.spawnCutsceneExplosion();
                break;
            case 2:
                this.player.animate();
                this.player.setAnimation((byte)2);
                this.repaintRequested = true;
                this.player.speedY += 256;
                this.player.rowOffset += this.player.speedY;
                this.player.normaliseRow();
                gy = this.player.floorHeight(false);
                if (this.player.pixelY() >= gy && this.player.speedY > 0) {
                    this.player.setAnimation((byte)4);
                    this.player.rowOffset = 0;
                    this.player.speedY = this.player.opusK;
                    this.player.animMode = 0;
                    if (this.player.tileRow <= 8) {
                        this.cutsceneStep++;
                        this.player.facingRight = true;
                        this.player.setAnimation((byte)1);
                    }
                }
                this.spawnCutsceneExplosion();
                break;
            case 3:
                this.player.animate();
                this.repaintRequested = true;
                this.player.posX += 1536;
                if ((this.player.posX >> 8) > 11 * tileWidth + (tileWidth >> 1)) {
                    this.player.setAnimation((byte)0);
                    this.player.animMode = 0;
                    this.cutsceneStep++;
                }
                this.spawnCutsceneExplosion();
                break;
            case 4:
                if (this.endingExplosionCount < 50) {
                    this.endingExplosionCount++;
                    this.spawnCutsceneExplosion();
                } else {
                    this.bossHealth[4] = 0;
                    c.rawTiles[c.roomLayer[this.currentRoom]][14][8] = -126;
                    c.rawTiles[c.roomLayer[this.currentRoom]][14][9] = -125;
                    this.levelMap.roomTiles[14][9] = 22;
                    this.levelMap.roomTiles[13][9] = 22;
                    this.levelMap.roomTiles[12][9] = 20;
                    this.levelMap.roomTiles[15][9] = 21;
                    c.solidMasks[12] |= 512;
                    c.solidMasks[13] |= 512;
                    c.solidMasks[14] |= 512;
                    c.solidMasks[15] |= 512;
                    this.cutsceneStep++;
                }
                break;
            case 5:
                this.dialoguePortraits = true;
                this.dialogueLinesLeft = dialogueLineCounts[32];
                this.openTextBox(dialogueFirstString[32]);
                this.player.speedY = this.player.opusKa;
                this.player.speedX = 512;
                this.cutsceneStep++;
                break;
            case 6:
                if (!this.textBoxOpen) {
                    this.player.animate();
                    this.player.setAnimation((byte)2);
                    this.repaintRequested = true;
                    this.player.speedY += 256;
                    this.player.rowOffset += this.player.speedY;
                    this.player.posX += this.player.speedX;
                    this.player.normaliseRow();
                    gy = this.player.floorHeight(false);
                    if (this.player.pixelY() >= gy && this.player.speedY > 0) {
                        this.player.setAnimation((byte)1);
                        this.player.animMode = 0;
                        this.player.rowOffset = 0;
                        this.player.speedY = this.player.opusK;
                        this.cutsceneStep++;
                    }
                }
                break;
            case 7:
                this.player.animate();
                this.repaintRequested = true;
                this.player.posX += 1536;
                if ((this.player.posX >> 8) > 14 * tileWidth + (tileWidth >> 1)) {
                    this.player.setAnimation((byte)0);
                    this.cutsceneStep++;
                }
                break;
            case 8:
                this.winFrame = 0;
                imgCannonBase = null;
                imgCannonShot = null;
                imgCannonBarrel = null;
                imgMaximillian = null;
                System.gc();
                sleep(20);
                this.useMenuBackground();
                if (imgWinPicture == null) {
                    try {
                        imgWinPicture = Image.createImage("/clank.png");
                    } catch (Exception ex) {
                    }
                }
                totalScore += stageScore;
                this.gameState = 2;
                this.summaryLines = new Vector();
                int wd = screenWidth - 36;
                this.summaryLines = new Vector();
                String blank = new String("");
                f.appendItems(this.summaryLines, ratchetandclank.wrapText(ratchetandclank.strings[306] + " " + this.app.menu.formatPlayTime(this.playTimeMs), wd), -1);
                f.appendItems(this.summaryLines, ratchetandclank.wrapText(ratchetandclank.strings[299] + " " + this.titaniumCollectedCount(), wd), -1);
                f.appendItem(this.summaryLines, blank, -1);
                f.appendItems(this.summaryLines, ratchetandclank.wrapText(ratchetandclank.strings[307], wd), -2);
                f.appendItems(this.summaryLines, ratchetandclank.wrapText("" + totalScore, wd), -2);
                this.app.menu.lineCount = this.summaryLines.size();
                this.app.menu.firstVisibleLine = 0;
                this.player.facingRight = true;
                this.player.weapon = 1;
                this.player.setAnimation((byte)0);
                this.player.animMode = 0;
                this.player.tileRow = 1;
                this.player.rowOffset = 1792;
                this.player.posX = this.getWidth() << 7;
                this.player.speedY = 0;
                this.player.speedX = 0;
                this.player.blinkTimer = 0;
                this.cutsceneStep++;
        }
    }
    /** Spawns an explosion at a random position near the cut-scene centre. */
    private void spawnCutsceneExplosion() {
        int x0 = tileWidth * 14 - 42;
        int xr = 2 * tileWidth;
        int y0 = tileHeight * 9 - tileHeight;
        int yr = 2 * tileHeight;
        spawnPlayerShot((x0 + abs(random.nextInt()) % xr) << 8, (y0 + abs(random.nextInt()) % yr) << 8, 30);
    }

    /** Scripted sequence step {@code step} of the tutorial cut-scene (state 17). */
    public final void tutorialCutsceneStep(byte step) {
        switch (step) {
            case 0:
                this.tutorialStep++;
                break;
            case 1:
                this.dialoguePortraits = true;
                this.dialogueLinesLeft = dialogueLineCounts[20];
                this.openTextBox(dialogueFirstString[20]);
                this.tutorialStep++;
                break;
            case 2:
                if (!this.textBoxOpen) {
                    this.spawnEnemy(3, 2, (byte)0, -1);
                    this.enemies[9].facingRight = false;
                    this.tutorialStep++;
                }
                break;
            case 3:
                this.enemies[9].animate();
                this.repaintRequested = true;
                int gy = this.enemies[9].floorHeight(false);
                this.enemies[9].speedY += 256;
                this.enemies[9].rowOffset += this.enemies[9].speedY;
                this.enemies[9].normaliseRow();
                if (this.enemies[9].feetY() + 2 * tileHeight >= gy && this.enemies[9].speedY > 0) {
                    this.enemies[9].setAnimation((byte)0);
                    this.enemies[9].animMode = 0;
                    this.gameState = 0;
                    this.dialoguePortraits = true;
                    this.dialogueLinesLeft = dialogueLineCounts[21];
                    this.openTextBox(dialogueFirstString[21]);
                    this.hudDirty = true;
                    this.tutorialStep++;
                }
                break;
            case 4:
                this.player.animate();
                this.repaintRequested = true;
                this.player.posX += 1536;
                if ((this.player.posX >> 8) > 18 * tileWidth - (tileWidth >> 3)) {
                    this.player.setAnimation((byte)0);
                    this.player.speedX = 1280;
                    this.player.rowOffset = 0;
                    this.player.speedY = this.player.opusK;
                    this.tutorialStep++;
                }
                break;
            case 5:
                this.dialoguePortraits = true;
                this.dialogueLinesLeft = dialogueLineCounts[15];
                this.openTextBox(dialogueFirstString[15]);
                this.tutorialStep++;
                break;
            case 6:
                if (!this.textBoxOpen) {
                    this.player.animate();
                    this.player.setAnimation((byte)2);
                    this.repaintRequested = true;
                    this.player.speedY += 384;
                    this.player.rowOffset += this.player.speedY;
                    this.player.posX += this.player.speedX;
                    this.player.normaliseRow();
                    if (this.player.pixelY() >= 5 * tileHeight + (tileHeight >> 1) && this.player.speedY > 0) {
                        this.player.setAnimation((byte)6);
                        int sp = (this.abs(this.railX2[0] - this.railX1[0]) << 8) / 2304;
                        this.player.moveStepX = (this.railDx[0] << 8) / sp;
                        this.player.moveStepY = this.railDy[0] / sp;
                        this.player.moveEndY = this.railY2[0];
                        this.tutorialStep++;
                    }
                } else {
                    this.player.setAnimation((byte)0);
                }
                break;
            case 7:
                this.player.updateSlide();
                if (this.player.posX >= tileWidth * 25 << 8) {
                    this.tutorialStep++;
                    this.player.speedY = 0;
                    this.player.speedX = 0;
                    this.player.setAnimation((byte)3);
                }
                break;
            case 8:
                this.player.animate();
                this.repaintRequested = true;
                gy = this.player.floorHeight(false);
                this.player.speedY += 256;
                if (this.player.pixelY() + this.player.speedY > gy << 8) {
                    this.player.speedY = (short)((gy << 8) - this.player.pixelY());
                }
                this.player.rowOffset += this.player.speedY;
                this.player.normaliseRow();
                if (this.player.pixelY() >= gy && this.player.speedY > 0) {
                    this.player.setAnimation((byte)4);
                    this.player.rowOffset = 0;
                    this.player.speedY = 0;
                    this.player.speedX = 0;
                    this.tutorialStep++;
                }
                break;
            case 9:
                this.player.setAnimation((byte)0);
                this.hudDirty = true;
                this.spawnEnemy(3, 2, (byte)0, -1);
                this.lastFrameStartMs = System.currentTimeMillis();
                this.gameState = 0;
                this.tutorialStep++;
        }
    }
    /** Moves the moving platforms back and forth (direction state platformDir). */
    public final void movePlatforms() {
        int maxX = tileWidth << 1;
        int maxY = tileHeight << 1;
        for (int i = 3; i >= 0; i--) {
            if (platformDir[i] == -1) {
                continue;
            }
            int u = 0;
            switch (platformDir[i]) {
                case 0:
                    platformDx[i] += 2;
                    if (platformDx[i] > maxX) {
                        platformDx[i] -= 2;
                        platformDir[i] += 2;
                    }
                    break;
                case 1:
                    platformDy[i] += 2;
                    if (platformDy[i] > maxY) {
                        platformDy[i] -= 2;
                        platformDir[i] += 2;
                    }
                    break;
                case 2:
                    platformDx[i] -= 2;
                    if (platformDx[i] < 0) {
                        platformDx[i] += 2;
                        platformDir[i] -= 2;
                    }
                    break;
                case 3:
                    platformDy[i] -= 2;
                    if (platformDy[i] < 0) {
                        platformDy[i] += 2;
                        platformDir[i] -= 2;
                    }
                    break;
            }
        }
    }
    /** Absolute value. */
    public final int abs(int value) {
        if (value < 0) {
            return value * -1;
        }
        return value;
    }
    /** Sleeps millis ms. */
    public static final void sleep(int millis) {
        try {
            Thread.sleep((long)millis);
        } catch (Exception exception) {
        }
    }

    /**
     * Shows the game canvas and starts the game thread: level >= 0 starts level {@code level}, slot >= 0 loads save slot {@code slot}.
     */
    public final void startGame(int level, int slot) {
        pendingLevel = level;
        pendingSlot = slot;
        gameThread = new Thread(this);
        gameThread.start();
        skipShowNotify = true;
        display.setCurrent(this);
    }
    /** Stops the game thread and switches to the pause menu. */
    public final void stopToPauseMenu() {
        this.arenaItems = this.arenaDescription = this.storeItems = this.summaryLines = this.buyItems = null;
        System.gc();
        sleep(20);
        this.gameThread = null;
        this.stateBeforePause = this.gameState;
        this.gameState = 4;
        this.heldAction = 0;
        this.menuCursor = 0;
        System.gc();
    }

    /** Pause (canvas hidden): stops the sound and opens the pause menu if playing. */
    public final void pauseHidden() {
        if (!paused) {
            app.stopEffect();
            repaintRequested = true;
            paused = true;
            resumeTime = 0L;
            heldAction = 0;
            bufferedAction = 0;
            if (gameState == 0 || gameState == 17 || gameState == 16 || gameState == 24) {
                menuCursor = 0;
                stateBeforePause = gameState;
                gameState = 4;
                imgTiles = null;
            }
            System.gc();
        }
    }

    /** Resume after a pause. */
    public final void resumeFromHide() {
        if (paused) {
            if (imgTiles == null) {
                useMenuBackground();
            }
            repaintRequested = true;
            paused = false;
            resumeTime = System.currentTimeMillis();
        }
    }
    public final void showNotify() {
            if (this.skipShowNotify) {
                this.skipShowNotify = false;
                return;
            }
            System.gc();
            sleep(1000);
        }
    /**
     * Game thread: starts the requested level or save, then runs the 70 ms frame loop:
     * cut-scenes, player/enemy/projectile updates, collisions, camera, arena and boss logic, repaint.
     */
    public final void run() {
        if (this.pendingLevel != -1) {
            this.startNewGame(this.pendingLevel);
            this.pendingLevel = -1;
            this.repaintRequested = true;
            System.currentTimeMillis();
        } else if (this.pendingSlot != -1) {
            app.loadSlot(this.pendingSlot);
            this.pendingSlot = -1;
            this.repaintRequested = true;
            System.currentTimeMillis();
        }
        while (this.gameThread != null && !this.exitToMenu) {
            this.frameStartMs = System.currentTimeMillis();
            if (!this.isShown()) {
                if (!this.paused) {
                    this.pauseHidden();
                }
                sleep(100);
                continue;
            }
            if (this.paused) {
                this.resumeFromHide();
            }
            this.frameHousekeeping();
            if (this.gameState == 17) {
                this.tutorialCutsceneStep(this.tutorialStep);
                if (!this.textBoxOpen) {
                    this.updateCamera();
                }
            }
            if (this.gameState == 24) {
                this.arenaCutsceneStep(this.cutsceneStep);
                this.updateCamera();
                for (int n = 9; n >= 0; n--) {
                    this.playerShots[n].update(false);
                }
            }
            if (this.gameState == 16) {
                for (int n = 9; n >= 0; n--) {
                    this.playerShots[n].update(false);
                }
                this.bossCutsceneTick(this.bossCutsceneStep);
                this.updateCamera();
            }
            if (this.gameState == 2) {
                this.winTimer++;
                if (this.winTimer == 25) {
                    this.winTimer = 80;
                }
                if (this.winTimer > 84) {
                    this.winTimer = 80;
                    if (this.winFrame < 15) {
                        this.winFrame++;
                    } else {
                        this.winFrame--;
                    }
                }
            } else {
                this.winTimer = 0;
            }
            if (this.gameState == 11 || this.gameState == 12) {
                this.player.animate();
            }
            if (this.gameState == 3 || this.gameState == 1) {
                stageTimeMs = 0L;
            }
            if (this.gameState == 0 && !this.textBoxOpen) {
                stageTimeMs += this.frameStartMs - this.lastFrameStartMs;
                this.playTimeMs += this.frameStartMs - this.lastFrameStartMs;
                this.dropPickups();
                this.updateFallingCrates();
                this.movePlatforms();
                this.animateBounceBot();
                this.collectPayola();
                this.collectArmModule();
                this.player.updatePlayer();
                this.player.animate();
                if (this.player.blinkTimer <= 0) {
                    this.enemyShotsHitPlayer();
                } else {
                    this.player.blinkTimer--;
                }
                this.updateCamera();
                this.collectTitaniumBolt();
                this.spikeCollision();
                int n;
                for (n = 9; n >= 0; n--) {
                    this.playerShots[n].update(false);
                    if (this.playerShots[n].shotType != -1) {
                        this.shotHitsCrates(n);
                    }
                }
                for (n = 9; n >= 0; n--) {
                    this.enemyShots[n].update(true);
                    if (this.enemyShots[n].shotType != -1) {
                        this.enemyShotHitsCrates(n);
                    }
                }
                this.enemiesLeft = 0;
                for (n = enemySlotCount - 1; n >= 0; n--) {
                    if (this.enemies[n].actorKind != -1) {
                        if (this.levelIndex == 12 && (this.enemies[n].actorKind == 1 || this.enemies[n].actorKind == 4)) {
                            if (this.enemies[n].turnTimer > 140) {
                                this.enemies[n].actorKind = -1;
                                continue;
                            }
                            this.enemies[n].turnTimer++;
                            if (this.enemies[n].turnTimer == 1) {
                                this.enemies[n].setAnimation((byte)6);
                                this.enemies[n].animMode = 1;
                            } else if (this.enemies[n].turnTimer == 137) {
                                this.enemies[n].setAnimation((byte)7);
                                this.enemies[n].animMode = 1;
                            }
                        }
                        if (this.enemies[n].animId != 5 && this.player.animId != 10) {
                            this.shotsHitEnemy(n);
                        }
                        this.enemies[n].updateAi();
                        this.enemies[n].animate();
                        if (this.levelIndex == 11 && this.enemies[n].actorKind != 4) {
                            this.enemiesLeft++;
                        }
                    }
                }
                if (this.levelIndex == 0 && this.enemies[9].actorKind == -1 && this.player.pixelY() >= this.player.floorHeight(false)) {
                    this.gameState = 17;
                    this.hudDirty = false;
                    this.doorKind[0] = 0;
                    this.player.setAnimation((byte)1);
                    this.player.animMode = 0;
                    this.player.facingRight = true;
                }
                if (this.levelIndex == 12) {
                    if (!this.bossFightStarted && (this.player.posX >> 8) >= 6 * tileWidth && (this.player.posX >> 8) <= 9 * tileWidth && this.player.pixelY() >= this.player.floorHeight(false)) {
                        this.gameState = 16;
                        this.hudDirty = false;
                        this.player.setAnimation((byte)1);
                        this.player.animMode = 0;
                    }
                    if (this.bossFightStarted) {
                        this.updateBoss();
                    }
                } else if (this.arenaActive == true) {
                    this.updateArenaRules();
                }
            }
            this.lastFrameStartMs = this.frameStartMs;
            if (this.repaintRequested) {
                this.repaintRequested = false;
                this.repaint();
                this.serviceRepaints();
            }
            this.frameEndMs = System.currentTimeMillis();
            this.frameMs = (int)(this.frameEndMs - this.frameStartMs);
            this.sleepMs = 70 - this.frameMs;
            if (this.sleepMs < 1 || this.sleepMs > 100) {
                this.sleepMs = 1;
            }
            sleep(this.sleepMs);
        }
        if (this.exitToMenu) {
            this.repaint();
            this.serviceRepaints();
            sleep(20);
            this.exitToMenu = false;
            app.returnToMenu();
        }
    }

    /**
     * Camera: follows the player (leading in the facing direction), clamped to the 28 x 18 tile room.
     */
    public final void updateCamera() {
        int tx = 0;
        int ty = 0;
        int d;
        int x = this.player.pixelX();
        int y = this.player.pixelY();
        if (!cameraLocked) {
            if (this.player.facingRight == true) {
                tx = -(x - tileWidth);
            } else {
                tx = -(x + tileWidth - screenWidth);
            }
        } else {
            tx = -(cameraLockX - (screenWidth >> 1));
            if (x > cameraLockX + (tileWidth >> 1) || x < cameraLockX - (tileWidth >> 1) || this.player.pixelY() == this.player.floorHeight(false)) {
                cameraLocked = false;
            }
        }
        if (grappleAvailable == true) {
            ty = -(y + cellHeight - screenHeight);
        } else {
            ty = -(y + cellHeight + (tileHeight << 1) - screenHeight);
        }
        d = ty - camY;
        camY = camY + (d >> 1);
        if (camX > tx) {
            camX = camX - 10;
            if (camX < tx) {
                camX = tx;
            }
        } else if (camX < tx) {
            camX = camX + 10;
            if (camX > tx) {
                camX = tx;
            }
        }
        if (camX > 0) {
            camX = 0;
        }
        if (camX < -(28 * tileWidth - screenWidth)) {
            camX = -(28 * tileWidth - screenWidth);
        }
        if (camY > 0) {
            camY = 0;
        }
        if (camY < -(18 * tileHeight - screenHeight)) {
            camY = -(18 * tileHeight - screenHeight);
        }
    }
    /** Parses /mapData.txt: 62 x 3 map table mapGeometryA and 43 x 2 table do. */
    public final void parseMapData() {
        short i;
        short j;
        int idx;
        int pos = 0;
        int u5;
        String tok;
        String data = readTextResource("/mapData.txt");
        for (i = 0; i < 62; i++) {
            for (j = 0; j < 3; j++) {
                idx = data.indexOf(",", pos);
                tok = data.substring(pos, idx).trim();
                pos = idx + 1;
                mapGeometryA[i][j] = Short.parseShort(tok);
            }
            idx = data.indexOf("\n", pos);
            pos = idx + 1;
        }
        for (i = 0; i < 43; i++) {
            for (j = 0; j < 2; j++) {
                idx = data.indexOf(",", pos);
                tok = data.substring(pos, idx).trim();
                pos = idx + 1;
                mapGeometryB[i][j] = Byte.parseByte(tok);
            }
            idx = data.indexOf("\n", pos);
            pos = idx + 1;
        }
    }
    /**
     * Starts a new game (lvl 0 = tutorial level): resets progress and enters the tutorial cut-scene.
     */
    public final void startNewGame(int lvl) {
        player.grappleState = -2;
        fallingDeath = false;
        textBoxOpen = false;
        armModuleAvailable = true;
        objectiveIndex = 13;
        tutorialStep = 0;
        summaryMessageId = 0;
        challengeMultiplier = 1;
        challengeMode = false;
        resetProgress();
        if (lvl == 0) {
            levelIndex = (byte) lvl;
            levelMap.loadLevel(lvl);
            currentRoom = c.startRoom;
            mapHighlightBits = 1;
            targetRoom = currentRoom;
            levelMap.enterRoom(currentRoom, true);
            checkpointColumn = 1;
            checkpointRow = 5;
            player.posX = checkpointColumn * tileWidth + (cellWidth >> 1) << 8;
            player.tileRow = player.aheadRow = checkpointRow;
            player.animFrame = 1;
            player.animTimer = 0;
            player.animId = 0;
            player.animMode = 0;
            player.rowOffset = 0;
            player.actorKind = 0;
            player.drawLayer = 1;
            player.health = 20;
            playTimeMs = 0;
            lastFrameStartMs = System.currentTimeMillis();
            recordRoomEnemies(currentRoom, 6275);
            player.blinkTimer = 0;
            player.facingRight = true;
            repaintRequested = true;
            bolts = 0;
            player.setAnimation((byte) 0);
            player.animMode = 0;
            bounceBotFrame = 1;
            bounceBotTimer = 18;
            player.ammo[1] = 0;
            camX = -22;
            camY = -32;
        }
        hudDirty = true;
        gameState = 17;
    }

    /** Resets all game progress to a new game (two weapons, all bits pending). */
    public final void resetProgress() {
        int n;
        this.resetStageStats();
        this.doorBits = -1;
        this.gateBitsA = -1;
        this.gateBitsB = -1;
        this.boltBitsA = -1;
        this.boltBitsB = -1;
        this.dialogueBitsA = -1;
        this.dialogueBitsB = -1;
        this.mapUnlockBits = 1572865;
        this.player.ownedWeapons = 3;
        this.player.weapon = 1;
        for (n = 7; n >= 0; n--) {
            this.player.weaponXp[n] = 0;
            this.player.weaponLevels[n] = 0;
            this.player.ammo[n] = a.startAmmo[n];
        }
        for (n = 0; n < 10; n++) {
            this.crateIntactBits[n] = -1;
        }
        for (n = 0; n < 6; n++) {
            this.roomRespawn[n] = false;
        }
        for (n = 0; n < 2; n++) {
            this.enemyAliveBits[n] = -1;
        }
        this.menuCursor = 0;
    }

    /** Starts level {@code level} in room {@code room}, keeping a snapshot of the progress for "replay level". */
    public final void startLevel(int level, short room) {
        this.loading = true;
        try {
            this.savedDoorBits = this.doorBits;
            this.savedGateBitsA = this.gateBitsA;
            this.savedGateBitsB = this.gateBitsB;
            this.savedBoltBitsA = this.boltBitsA;
            this.savedBoltBitsB = this.boltBitsB;
            this.savedDialogueBitsA = this.dialogueBitsA;
            this.savedDialogueBitsB = this.dialogueBitsB;
            this.savedMapUnlockBits = this.mapUnlockBits;
            this.savedMapHighlightBits = this.mapHighlightBits;
            this.savedBolts = this.bolts;
            this.player.savedWeapon = this.player.weapon;
            this.savedWeapons = this.player.ownedWeapons;
            this.savedRoom = room;
            int n;
            for (n = 0; n < 8; n++) {
                this.player.savedWeaponXp[n] = this.player.weaponXp[n];
                this.player.savedWeaponLevels[n] = this.player.weaponLevels[n];
                this.player.savedAmmo[n] = this.player.ammo[n];
            }
            this.player.grappleState = -2;
            this.challengeMultiplier = 1;
            this.fallingDeath = false;
            this.textBoxOpen = false;
            for (n = 0; n < 10; n++) {
                this.crateIntactBits[n] = -1;
            }
            for (n = 0; n < 2; n++) {
                this.enemyAliveBits[n] = -1;
            }
            this.summaryMessageId = 0;
            this.lastFrameStartMs = System.currentTimeMillis();
            this.levelIndex = (byte)level;
            this.levelMap.loadLevel(level);
            this.player.facingRight = true;
            this.currentRoom = room;
            this.targetRoom = this.currentRoom;
            this.player.blinkTimer = 10;
            this.repaintRequested = true;
            this.recordRoomEnemies(this.currentRoom, 6452);
            this.levelMap.enterRoom(this.currentRoom, true);
            if (this.levelIndex == 12) {
                this.loadBoss();
            }
            this.player.posX = this.checkpointColumn * tileWidth + (cellWidth >> 1) << 8;
            this.player.tileRow = this.player.aheadRow = this.checkpointRow;
            this.player.animFrame = 1;
            this.player.animTimer = 0;
            this.player.animId = 0;
            this.player.animMode = 0;
            this.player.rowOffset = 0;
            this.player.actorKind = 0;
            this.player.drawLayer = 1;
            this.player.health = 20;
            this.lastFrameStartMs = System.currentTimeMillis();
            this.hudDirty = true;
            this.menuCursor = 0;
            camX = this.checkpointCamX;
            camY = this.checkpointCamY;
            this.resetStageStats();
            this.heldAction = this.bufferedAction = 0;
            this.gameState = 0;
        } catch (Exception ex) {
            System.out.println("ERROR. reInit: " + ex);
        }
        this.loading = false;
    }
    /** Marks dialogue {@code dialogue} as seen. */
    public final void markDialogueSeen(int dialogue) {
        if (dialogue >= 0 && dialogue <= 19) {
            dialogueBitsA &= ~(1 << dialogue);
            if (dialogue == 4 || dialogue == 5 || dialogue == 6 || dialogue == 7 || dialogue == 9 || dialogue == 10) {
                if (!dialoguePending(4) && !dialoguePending(5) && !dialoguePending(6) && !dialoguePending(7) && !dialoguePending(9) && !dialoguePending(10)) {
                    dialogueBitsA |= 8;
                }
            }
        } else if (dialogue >= 20 && dialogue <= 39) {
            dialogueBitsB &= ~(1 << dialogue - 20);
        }
    }
    /** True if dialogue {@code dialogue} has not been seen yet. */
    public final boolean dialoguePending(int dialogue) {
        if (dialogue >= 0 && dialogue <= 19) {
            return (this.dialogueBitsA & 1 << dialogue) != 0;
        }
        if (dialogue >= 20 && dialogue <= 39) {
            return (this.dialogueBitsB & 1 << dialogue - 20) != 0;
        }
        return false;
    }
    /** Copies the bolt-lock gate flag of {@code room} in {@code level} into door bit 0 of doorBits (room entry). */
    public final void applyGateFlag(int level, int room) {
        int n3;
        int n4;
        if (level > 0 && level <= 5) {
            n3 = (level - 1) * 6 + room;
            n4 = this.gateBitsA;
        } else if (level >= 6 && level <= 10) {
            n3 = (level - 5) * 6 + room;
            n4 = this.gateBitsB;
        } else {
            this.doorBits |= 1;
            return;
        }
        if ((n4 & 1 << n3) == 0) {
            this.doorBits &= 254;
            return;
        }
        this.doorBits |= 1;
    }
    /** Opens the bolt-lock gate of {@code room} in {@code level} (clears its flag). */
    public final void openGate(int level, int room) {
            if (level > 0 && level <= 5) {
                int n3 = (level - 1) * 6 + room;
                this.gateBitsA &= ~(1 << n3);
                return;
            }
            if (level >= 6 && level <= 10) {
                int n3 = (level - 5) * 6 + room;
                this.gateBitsB &= ~(1 << n3);
            }
        }
    /** True if the titanium bolt of {@code room} in {@code level} has not been collected. */
    public final boolean titaniumPending(int level, int room) {
        int n3;
        int n4;
        if (level > 0 && level <= 5) {
            n3 = (level - 1) * 6 + room;
            n4 = this.boltBitsA;
        } else if (level >= 6 && level <= 10) {
            n3 = (level - 6) * 6 + room;
            n4 = this.boltBitsB;
        } else {
            return false;
        }
        if ((n4 & 1 << n3) == 0) {
            return false;
        }
        return true;
    }
    /** Number of titanium bolts collected. */
    public final int titaniumCollectedCount() {
        int i;
        int n = 0;
        for (i = (boltBitCount >> 1) - 1; i >= 0; i--) {
            if ((boltBitsA & 1 << i) == 0) {
                n++;
            }
            if ((boltBitsB & 1 << i) == 0) {
                n++;
            }
        }
        return n;
    }
    /** Number of titanium bolts collected in level {@code level}. */
    public final int titaniumCollectedInLevel(int level) {
            int n2;
            int n3;
            int n4 = 0;
            if (level > 0 && level <= 5) {
                n3 = (level - 1) * 6;
                for (n2 = 0; n2 < 6; ++n2) {
                    if ((this.boltBitsA & 1 << n3 + n2) == 0) {
                        ++n4;
                    }
                }
            } else if (level >= 6 && level <= 10) {
                n3 = (level - 6) * 6;
                for (n2 = 0; n2 < 6; ++n2) {
                    if ((this.boltBitsB & 1 << n3 + n2) == 0) {
                        ++n4;
                    }
                }
            }
            return n4;
        }
    /**
     * Collects the titanium bolt of {@code room} in {@code level} and shows the message (first bolt, count, or the
     * R.Y.N.O. reward).
     */
    public final void awardTitaniumBolt(int level, int room) {
        if (level > 0 && level <= 5) {
            int bit = (level - 1) * 6 + room;
            this.boltBitsA &= ~(1 << bit);
        } else if (level >= 6 && level <= 10) {
            int bit = (level - 6) * 6 + room;
            this.boltBitsB &= ~(1 << bit);
        }
        if (this.titaniumCollectedCount() >= boltsForRyno) {
            this.dialoguePortraits = false;
            this.openTextBox(72);
            this.player.ownedWeapons |= 128;
            this.savedWeapons |= 128;
            return;
        }
        if (this.titaniumCollectedCount() == 1) {
            this.dialoguePortraits = false;
            this.openTextBox(74);
            return;
        }
        this.dialoguePortraits = false;
        this.openTextBox(227);
    }
    /** Respawns the player at the last checkpoint after death. */
    public final void respawnAtCheckpoint() {
        this.player.grappleState = -2;
        for (byte n = 0; n < 6; n++) {
            this.roomRespawn[n] = true;
        }
        this.currentRoom = this.checkpointRoom;
        this.recordRoomEnemies(this.currentRoom, 6821);
        this.levelMap.enterRoom(this.checkpointRoom, false);
        this.player.blinkTimer = 10;
        this.player.posX = this.checkpointColumn * tileWidth + (cellWidth >> 1) << 8;
        this.player.tileRow = this.checkpointRow;
        camX = this.checkpointCamX;
        camY = this.checkpointCamY;
        this.player.rowOffset = 0;
        this.player.health = 20;
        this.player.speedX = this.player.speedY = 0;
        this.player.setAnimation((byte)0);
        this.player.animMode = 0;
        this.hudDirty = true;
        if (this.levelIndex == 12) {
            this.cannonFlash[0] = this.cannonFlash[1] = this.cannonFlash[2] = this.cannonFlash[3] = 0;
            this.cannonState[0] = this.cannonState[1] = this.cannonState[2] = this.cannonState[3] = 0;
            this.bossHealth[0] = this.bossHealth[1] = this.bossHealth[2] = this.bossHealth[3] = 100;
            this.bossHealth[4] = 200;
        }
    }

    /** level == -1: leaves the level for the level-select map (or the arena list after an arena). */
    public final void leaveLevel(int level, boolean ignored) {
        int i3, i4, i5;
        int i6 = 0;
        if (level == -1) {
            this.selectedItem = this.menuCursor = 0;
            this.selectMapHighlight();
            app.saveToSlot(this.saveSlot);
            this.useMenuBackground();
            this.gameState = 3;
            if (this.arenaActive == true) {
                this.enemyAliveBits[0] = -1;
                this.enemyAliveBits[1] = -1;
                this.arenaActive = false;
                this.buildArenaList();
                this.gameState = 1;
                this.player.ownedWeapons = this.savedWeapons;
            }
            return;
        }
    }

    /** Loads the level's tile set if it differs from the current one. */
    public final void ensureTileset() {
        if (tilesetIndex != c.levelTileset) {
            tilesetIndex = c.levelTileset;
            try {
                imgTiles = null;
                System.gc();
                sleep(30);
                loadTileset();
            } catch (Exception e) {
                System.out.println("ERROR. swapTileset. currentTileset=" + tilesetIndex + ". :" + e);
            }
        }
    }

    /*
     * Reconstruction device (retained; removed from final class output).
     * Unused method whose only purpose is that javac creates the constant-pool reference to
     * c.b(II)Z here, early in method order. ProGuard 3.2 shrinking removes the method but
     * keeps javac's creation order for the surviving NameAndType b:(II)Z, which reproduces
     * the retail constant-pool order of g.class. Do not delete, rename or move.
     */
    private void opusProbe() {
        this.levelMap.isSolid(0, 0);
    }
    /** Loads the 35 tile images of tile set tilesetIndex. */
    public final void loadTileset() throws IOException {
        InputStream in;
        if ((in = getClass().getResourceAsStream(tilesetFiles[tilesetIndex])) == null) {
            return;
        }
        imgTiles = new Image[35];
        for (int i = 0; i < imgTiles.length; i++) {
            imgTiles[i] = readPackedImage(in);
        }
        in.close();
        System.gc();
        sleep(20);
    }
    /** Reads one length-prefixed PNG from a packed image file (null for length 0). */
    public static final Image readPackedImage(java.io.InputStream inputStream) throws java.io.IOException {
            int n = inputStream.read() << 8;
            if ((n |= inputStream.read()) == 0) {
                return null;
            }
            byte[] byArray = new byte[n];
            inputStream.read(byArray);
            return Image.createImage(byArray, 0, n);
        }
    /** Switches the shared tile images to the menu background. */
    public final void useMenuBackground() {
        try {
            imgTiles = null;
            System.gc();
            sleep(20);
            imgTiles = new Image[1];
            imgTiles[0] = Image.createImage(tilesetFiles[3]);
        } catch (IOException e) {
        }
    }
    /** Reloads the level tile set (after a menu). */
    public final void reloadTileset() {
        try {
            imgTiles = null;
            System.gc();
            sleep(20);
            loadTileset();
        } catch (IOException e) {
        }
    }

    /** Starts arena {@code arena} with full ammo. */
    private void startArena(int arena) {
        int n;
        if (arena < 0 || arena > 11) {
            return;
        }
        this.fallingDeath = false;
        this.textBoxOpen = false;
        this.menuCursor = 0;
        this.levelIndex = 11;
        this.tilesetIndex = 3;
        this.levelMap.loadLevel(11);
        this.recordRoomEnemies(this.currentRoom, 7130);
        for (n = 0; n < 10; n++) {
            this.crateIntactBits[n] = -1;
        }
        for (n = 0; n < 6; n++) {
            this.roomRespawn[n] = false;
        }
        for (n = 0; n < 2; n++) {
            this.enemyAliveBits[n] = -1;
        }
        this.levelMap.enterRoom(arena, true);
        this.tilesetIndex = 1;
        this.player.posX = this.checkpointColumn * tileWidth + (cellWidth >> 1) << 8;
        this.player.tileRow = this.player.aheadRow = this.checkpointRow;
        this.player.animFrame = 1;
        this.player.animTimer = 0;
        this.player.animId = 0;
        this.player.animMode = 0;
        this.player.rowOffset = 0;
        this.player.actorKind = 0;
        this.player.drawLayer = 1;
        this.player.health = 20;
        this.lastFrameStartMs = System.currentTimeMillis();
        this.hudDirty = true;
        this.player.facingRight = true;
        this.repaintRequested = true;
        camX = this.checkpointCamX;
        camY = this.checkpointCamY;
        for (n = 0; n < 8; n++) {
            this.player.ammo[n] = a.maxAmmo[n * 3 + this.player.weaponLevels[n]];
        }
        this.gameState = 0;
    }

    /**
     * Arena rules each frame (gas, one hit, weapon) and the arena result: fail, or reward and unlock the next arena.
     */
    public final void updateArenaRules() {
        if (this.ruleOneHit == true && this.player.health != 20) {
            this.player.health = 0;
            return;
        }
        if (this.ruleGas == true && this.gasTimer++ > 80) {
            if (!invulnerable) {
                this.player.health--;
            }
            this.gasTimer = 0;
            this.hudDirty = true;
        }
        if (this.ruleOneWeapon == true && this.player.ownedWeapons != 1 && this.player.ammo[this.player.weapon] <= 0) {
            this.useMenuBackground();
            this.gameState = 19;
        } else {
            for (int n = enemySlotCount - 1; n >= 0; n--) {
                if (this.enemies[n].health > 0 && this.enemies[n].actorKind != 4 && this.enemies[n].actorKind != -1) {
                    return;
                }
            }
        }
        this.ruleGas = false;
        this.ruleOneHit = false;
        this.gasTimer = 0;
        int n;
        for (n = 0; n < 10; n++) {
            this.crateIntactBits[n] = -1;
        }
        for (n = 0; n < 2; n++) {
            this.enemyAliveBits[n] = -1;
        }
        this.menuCursor = 0;
        this.arenaActive = false;
        this.player.ownedWeapons = this.savedWeapons;
        if (this.gameState != 19) {
            this.useMenuBackground();
            this.gameState = 15;
            this.bolts += arenaRewards[this.currentRoom];
            if (this.bolts > 999999) {
                this.bolts = 999999;
            }
            if (this.currentRoom < 11) {
                this.mapUnlockBits |= 1 << 21 + this.currentRoom;
                return;
            }
            this.player.ownedWeapons |= 64;
        }
    }
    /** Room transition through edge dir (1 up, 6 down, 2 left, 5 right) to the linked room. */
    public final void changeRoom(int dir) {
        switch (dir) {
            case 1:
                entryEdge = 6;
                targetRoom = c.roomLinks[currentRoom][0];
                break;
            case 6:
                entryEdge = 1;
                targetRoom = c.roomLinks[currentRoom][1];
                break;
            case 5:
                entryEdge = 2;
                targetRoom = c.roomLinks[currentRoom][2];
                break;
            case 2:
                entryEdge = 5;
                targetRoom = c.roomLinks[currentRoom][3];
                break;
        }
        if (targetRoom == -1) {
            return;
        }
        currentRoom = targetRoom;
        enterRoomAtEdge();
    }
    /** Loads the entered room and places the player at the opposite edge. */
    private void enterRoomAtEdge() {
        loading = true;
        recordRoomEnemies(currentRoom, 7356);
        levelMap.enterRoom(currentRoom, false);
        switch (entryEdge) {
            case 1:
                player.tileRow = player.aheadRow = 1;
                camY = 0;
                break;
            case 6:
                player.tileRow = player.aheadRow = 16;
                camY = minCamY;
                break;
            case 2:
                player.posX = (1 * tileWidth + (cellWidth >> 1)) << 8;
                camX = 0;
                break;
            case 5:
                player.posX = (26 * tileWidth + (cellWidth >> 1)) << 8;
                camX = minCamX;
                break;
        }
        loading = false;
    }
    /** Fires Clank's grapple at grapple point (column, row) of tile code tile (97 = pull-to point). */
    public final void fireGrappleAt(int column, int row, int tile) {
        this.player.anchorX = column * tileWidth + (tileWidth >> 1) + (this.player.facingRight ? -5 : 5) << 8;
        this.player.anchorY = row * tileHeight + (tileHeight >> 1) - 2 << 8;
        int dx = 4 * tileWidth / 44;
        if (!this.player.facingRight) {
            dx = -dx;
        }
        int dy = 10 * tileHeight / 44;
        this.player.hookTipX = (this.player.posX >> 8) + dx << 8;
        this.player.hookTipY = this.player.tileRow * tileHeight + (this.player.rowOffset >> 8) + dy << 8;
        this.player.hookStepX = (this.player.anchorX - this.player.hookTipX) / 6;
        this.player.hookStepY = (this.player.anchorY - this.player.hookTipY) / 6;
        if (this.player.anchorY < this.player.hookTipY) {
            this.player.grappleTile = (byte)tile;
            this.player.grappleState = 0;
            this.player.setAnimation((byte)5);
            this.player.animMode = 2;
        }
    }
    /** Floor height (pixels) under the point (x, y). */
    public final int floorBelow(int x, int y, int unused) {
        int n4 = 18 * tileHeight;
        int n5 = y / tileHeight;
        if ((((c)null).solidMasks[x / tileWidth] & 1 << n5 + 1) > 0) {
            n4 = (n5 + 1) * tileHeight;
        }
        return n4;
    }
    /** Top of the moving platform under the player (sets the player's ride speed platformRideSpeed). */
    public final short platformTopUnderPlayer() {
        short best = (short)(18 * tileHeight);
        int v = 0;
        if (this.player.platformIgnoreTimer != 0) {
            return best;
        }
        for (int n = 3; n >= 0; n--) {
            if (this.platformDir[n] == -1) {
                continue;
            }
            if (this.platformX[n] + this.platformDx[n] > (this.player.posX >> 8) + 8 || this.platformX[n] + this.platformDx[n] + tileWidth < (this.player.posX >> 8) - 8) {
                continue;
            }
            if ((v = this.platformY[n] + this.platformDy[n]) < best && v > this.player.pixelY()) {
                best = (short)v;
                this.player.platformRideSpeed = 0;
                if (this.platformDir[n] == 0) {
                    this.player.platformRideSpeed = 2;
                } else if (this.platformDir[n] == 2) {
                    this.player.platformRideSpeed = -2;
                }
            }
        }
        return (short)(best - tileHeight);
    }
    /** Top of the crate under the player. */
    public final short crateTopUnderPlayer() {
        short y = (short) (18 * tileHeight);
        int top = player.pixelY();
        int left = player.posX >> 8;
        int unused = 0;
        int width = imgCrates.getWidth();
        for (int i = 49; i >= 0; i--) {
            if (crateType[i] == -1) {
                continue;
            }
            if (crateY[i] >= top) {
                if (crateX[i] <= left + 8) {
                    if (crateX[i] + width < left - 8) {
                        continue;
                    }
                    if (crateY[i] < y) {
                        y = crateY[i];
                    }
                }
            }
        }
        return (short) (y - tileHeight);
    }
    /** True if the player runs into a crate. */
    public final boolean playerHitsCrate() {
        int width = imgCrates.getWidth();
        int height = imgCrates.getHeight() >> 3;
        byte q = tileWidth;
        byte r = tileHeight;
        boolean unused = false;
        int left = player.posX >> 8;
        int top = player.pixelY();
        boolean unused2 = false;
        int reach = r - 1;
        int i = 49;
        for (; i >= 0; --i) {
            if (crateType[i] < 0) {
                continue;
            }
            if (abs(crateX[i] - left) <= q) {
                if (abs(crateY[i] - top) > r) {
                    continue;
                }
                if (boxesOverlap(crateX[i], crateY[i], width, height, left - 8, top, 16, reach)) {
                    return true;
                }
            }
        }
        return false;
    }
    /** True if enemy {@code enemy} overlaps the player. */
    public final boolean enemyOverlapsPlayer(int enemy) {
        int type;
        int ex;
        int ey;
        int ox;
        int oy;
        int w;
        int h;
        int px = player.pixelX();
        int py = player.pixelY() + playerDrawOffsetY;
        int u0 = 0;
        int u1 = 0;
        int u2 = 0;
        int u3 = 0;
        if ((type = enemies[enemy].actorKind) == -1 || type == 4) {
            return false;
        }
        if (abs(enemies[enemy].posX - player.posX) > tileWidth << 8 || abs(enemies[enemy].tileRow - player.tileRow) > 3 || enemies[enemy].animId == 5) {
            return false;
        }
        ex = enemies[enemy].pixelX();
        ey = enemies[enemy].feetY();
        ox = d.bodyBoxX[type];
        oy = d.bodyBoxY[type];
        w = d.bodyBoxW[type];
        h = d.bodyBoxH[type];
        if (boxesOverlap(ex - ox, ey + oy, w, h, px - 11, py + 7, 18, 37)) {
            return true;
        }
        return false;
    }
    /** True if a crate is within wrench reach in front of the player. */
    public final boolean crateInWrenchReach() {
        int width = imgCrates.getWidth();
        byte q = tileWidth;
        int left = player.posX >> 8;
        int top = player.pixelY();
        int centre = 0;
        for (int i = 49; i >= 0; i--) {
            if (crateType[i] < 0) {
                continue;
            }
            if ((centre = crateX[i] + (width >> 1)) > left && !player.facingRight) {
                continue;
            }
            if (centre < left && player.facingRight) {
                continue;
            }
            if (abs(centre - left) > q) {
                continue;
            }
            if (crateY[i] == top) {
                return true;
            }
        }
        return false;
    }
    /** True if an enemy is within wrench reach in front of the player. */
    public final boolean enemyInWrenchReach() {
        int type;
        for (int i = enemySlotCount - 1; i >= 0; i--) {
            if ((type = enemies[i].actorKind) == -1 || type == 4) {
                continue;
            }
            if (enemies[i].posX > player.posX && !player.facingRight) {
                continue;
            }
            if (enemies[i].posX < player.posX && player.facingRight) {
                continue;
            }
            if (abs(enemies[i].posX - player.posX) > tileWidth << 8 || enemies[i].animId == 5) {
                continue;
            }
            if (player.tileRow == enemies[i].tileRow) {
                return true;
            }
        }
        return false;
    }
    /** Index of the enemy overlapping the player, or -1. */
    public final int enemyTouchingPlayer() {
        int type;
        int ex;
        int ey;
        int ox;
        int oy;
        int w;
        int h;
        int px = player.pixelX();
        int py = player.pixelY() + playerDrawOffsetY;
        int u0 = 0;
        int u1 = 0;
        int u2 = 0;
        int u3 = 0;
        for (int i = enemySlotCount - 1; i >= 0; i--) {
            if ((type = enemies[i].actorKind) == -1 || type == 4) {
                continue;
            }
            if (abs(enemies[i].posX - player.posX) > tileWidth << 8 || abs(enemies[i].tileRow - player.tileRow) > 3 || enemies[i].animId == 5) {
                continue;
            }
            ex = enemies[i].pixelX();
            ey = enemies[i].feetY();
            ox = d.bodyBoxX[type];
            oy = d.bodyBoxY[type];
            w = d.bodyBoxW[type];
            h = d.bodyBoxH[type];
            if (boxesOverlap(ex - ox, ey + oy, w, h, px - 11, py + 7, 18, 37)) {
                return i;
            }
        }
        return -1;
    }
    /** Pause menu keys: continue, settings, quit to main menu, exit game. */
    public final void pauseKeys(int key, int act) {
        if (act == 1 || key == 50) {
            menuCursor = cursorUp(menuCursor, (byte) 0, (byte) 3);
            return;
        }
        if (act == 6 || key == 56) {
            menuCursor = cursorDown(menuCursor, (byte) 3, (byte) 0);
            return;
        }
        if (key == 53 || act == 8 || key == -6) {
            app.playEffect(3);
            if (menuCursor == 0) {
                if (stateBeforePause == 0 || stateBeforePause == 17 || stateBeforePause == 16 || stateBeforePause == 24) {
                    lastFrameStartMs = System.currentTimeMillis();
                    reloadTileset();
                }
                gameState = stateBeforePause;
                repaintRequested = true;
                hudDirty = true;
            } else if (menuCursor == 1) {
                gameState = 5;
            } else if (menuCursor == 2) {
                gameState = 8;
            } else if (menuCursor == 3) {
                gameState = 7;
            }
            menuCursor = 0;
        } else if (key == -7) {
            reloadTileset();
            lastFrameStartMs = System.currentTimeMillis();
            gameState = 0;
            repaintRequested = true;
            hudDirty = true;
        }
    }
    /** In-game settings keys (sound on/off). */
    public final void settingsKeys(int key, int act) {
        if (key == 53 || act == 8 || key == -6) {
            if (menuCursor == 0) {
                if (app.soundEnabled == true) {
                    app.soundEnabled = false;
                } else {
                    app.soundEnabled = true;
                }
                app.storeSoundSetting((byte) (app.soundEnabled ? 1 : 0));
            }
        } else if (key == -7) {
            menuCursor = 0;
            gameState = 4;
            app.playEffect(3);
            app.storeSoundSetting((byte) (app.soundEnabled ? 1 : 0));
        }
    }
    /** New game save-slot keys (a used slot asks before overwriting). */
    public final void newGameSlotKeys(int key, int act) {
        if (act == 1 || key == 50) {
            menuCursor = cursorUp(menuCursor, (byte) 0, (byte) 2);
            return;
        }
        if (act == 6 || key == 56) {
            menuCursor = cursorDown(menuCursor, (byte) 2, (byte) 0);
            return;
        }
        if (key == 53 || act == 8 || key == -6) {
            selectedItem = menuCursor;
            app.playEffect(3);
            if (app.slotSummaries[menuCursor] != 0) {
                gameState = 9;
            } else {
                startNewGame(0);
                saveSlot = selectedItem;
                app.readSlotSummaries();
            }
            menuCursor = 0;
            return;
        }
        if (key == -7) {
            exitToMenu = true;
        }
    }
    /** Weapon store keys: choose a weapon or all-ammo, then buy. */
    public final void storeKeys(int key, int act) {
        if (key == -7) {
            app.playEffect(3);
            storeItems = null;
            selectedItem = menuCursor = 0;
            selectMapHighlight();
            player.weapon = storeSavedWeapon;
            gameState = 3;
        } else if (key == 52 || act == 2) {
            if (--menuCursor < 0) {
                menuCursor = 7;
            }
            while (menuCursor > 3 && (player.ownedWeapons & 1 << menuCursor + 1) == 0 || menuCursor == 5) {
                if (menuCursor == 7) {
                    break;
                }
                if (--menuCursor < 0) {
                    menuCursor = 7;
                }
            }
            storeItems = null;
        } else if (key == 54 || act == 5) {
            if (++menuCursor > 7) {
                menuCursor = 0;
            }
            while (menuCursor > 3 && (player.ownedWeapons & 1 << menuCursor + 1) == 0 || menuCursor == 5) {
                if (menuCursor == 7) {
                    break;
                }
                if (++menuCursor > 7) {
                    menuCursor = 0;
                }
            }
            storeItems = null;
        } else if (key == 53 || act == 8 || key == -6) {
            app.playEffect(3);
            if (menuCursor != 2 || !dialoguePending(3)) {
                if (menuCursor == 3 && dialoguePending(8)) {
                    return;
                }
                if (menuCursor == 7 && player.allAmmoPrice <= 0) {
                    return;
                }
                if (menuCursor != 7 && (player.ownedWeapons & 1 << menuCursor + 1) == 0) {
                    gameState = 12;
                    selectedItem = menuCursor;
                    menuCursor = 0;
                    storeItems = null;
                    return;
                }
                if (menuCursor != 7 && player.ammo[menuCursor + 1] >= a.maxAmmo[3 * (menuCursor + 1) + player.weaponLevels[menuCursor + 1]]) {
                    return;
                }
                gameState = 12;
                selectedItem = menuCursor;
                menuCursor = 0;
                storeItems = null;
            }
        } else if (act == 6) {
            if (app.menu.firstVisibleLine + app.menu.visibleLines < app.menu.lineCount) {
                app.menu.firstVisibleLine++;
            }
        } else if (act == 1 && app.menu.firstVisibleLine > 0) {
            app.menu.firstVisibleLine--;
        }
    }
    /** Buy screen keys: pays bolts for a weapon, its ammo or all ammo. */
    public final void buyKeys(int key, int act) {
        if (act == 6) {
            if (app.menu.firstVisibleLine + app.menu.visibleLines < app.menu.lineCount) {
                app.menu.firstVisibleLine++;
            } else if (menuCursor == 0) {
                menuCursor = 1;
            }
        } else if (act == 1) {
            if (menuCursor == 1) {
                menuCursor = 0;
            } else if (app.menu.firstVisibleLine > 0) {
                app.menu.firstVisibleLine--;
            }
        } else if (key == 53 || act == 8 || key == -6) {
            app.playEffect(3);
            if (menuCursor == 1) {
                if (selectedItem == 7) {
                    if (bolts >= player.allAmmoPrice) {
                        bolts -= player.allAmmoPrice;
                        for (int n = 1; n < 8; n++) {
                            if ((player.ownedWeapons & 1 << n) > 0) {
                                player.ammo[n] = a.maxAmmo[n * 3 + player.weaponLevels[n]];
                            }
                        }
                    } else {
                        menuCursor = 0;
                        gameState = 13;
                        buyItems = null;
                        return;
                    }
                } else if ((player.ownedWeapons & 1 << selectedItem + 1) == 0) {
                    if (bolts >= a.weaponPrices[selectedItem]) {
                        bolts -= a.weaponPrices[selectedItem];
                        player.ownedWeapons |= 1 << selectedItem + 1;
                    } else {
                        menuCursor = 0;
                        gameState = 13;
                        buyItems = null;
                        return;
                    }
                } else if (bolts >= ammoPrices[selectedItem + 1]) {
                    bolts -= ammoPrices[selectedItem + 1];
                    player.ammo[selectedItem + 1] += a.maxAmmo[(selectedItem + 1) * 3 + player.weaponLevels[selectedItem + 1]];
                    if (player.ammo[selectedItem + 1] > a.maxAmmo[(selectedItem + 1) * 3 + player.weaponLevels[selectedItem + 1]]) {
                        player.ammo[selectedItem + 1] = a.maxAmmo[(selectedItem + 1) * 3 + player.weaponLevels[selectedItem + 1]];
                    }
                } else {
                    menuCursor = 0;
                    gameState = 13;
                    buyItems = null;
                    return;
                }
            }
            menuCursor = selectedItem;
            gameState = 11;
            player.allAmmoPrice = computeAmmoPrices();
            buyItems = null;
        } else if (key == -7) {
            app.playEffect(3);
            menuCursor = selectedItem;
            gameState = 11;
            player.allAmmoPrice = computeAmmoPrices();
            buyItems = null;
        }
    }
    /**
     * Level-select map keys: moves between unlocked nodes and starts the selected level, arenas or store.
     */
    public final void mapKeys(int key, int act) {
        byte old = this.menuCursor;
        int n = 0;
        if (act == 1 || key == 50) {
            this.menuCursor = mapUp[this.menuCursor];
            while ((this.mapUnlockBits & 1 << this.menuCursor) == 0) {
                this.menuCursor = mapUp[this.menuCursor];
            }
            if (this.menuCursor == 19 && this.mapUnlockBits == 1572865) {
                this.menuCursor = old;
            }
        } else if (act == 6 || key == 56) {
            this.menuCursor = mapDown[this.menuCursor];
            while ((this.mapUnlockBits & 1 << this.menuCursor) == 0) {
                this.menuCursor = mapDown[this.menuCursor];
            }
            if (this.menuCursor == 19 && this.mapUnlockBits == 1572865) {
                this.menuCursor = old;
            }
        } else if (act == 5 || key == 54) {
            this.menuCursor = mapRight[this.menuCursor];
            while ((this.mapUnlockBits & 1 << this.menuCursor) == 0) {
                this.menuCursor = mapRight[this.menuCursor];
            }
            if (this.menuCursor == 19 && this.mapUnlockBits == 1572865) {
                this.menuCursor = old;
            }
        } else if (act == 2 || key == 52) {
            this.menuCursor = mapLeft[this.menuCursor];
            while ((this.mapUnlockBits & 1 << this.menuCursor) == 0) {
                this.menuCursor = mapLeft[this.menuCursor];
            }
            if (this.menuCursor == 19 && this.mapUnlockBits == 1572865) {
                this.menuCursor = old;
            }
        } else if (key == 53 || act == 8 || key == -6) {
            if (this.menuCursor == 19) {
                if (this.mapUnlockBits != 1572865) {
                    this.player.facingRight = true;
                    this.player.setAnimation((byte)0);
                    this.player.animMode = 0;
                    this.player.tileRow = 2;
                    this.player.posX = this.getWidth() << 7;
                    this.player.speedY = 0;
                    this.player.speedX = 0;
                    this.player.blinkTimer = 0;
                    this.player.rowOffset = 0;
                    this.gameState = 11;
                    this.storeSavedWeapon = this.player.weapon;
                    this.player.allAmmoPrice = this.computeAmmoPrices();
                    this.menuCursor = 0;
                }
            } else if (this.menuCursor == 18) {
                this.enemyAliveBits[0] = -1;
                this.enemyAliveBits[1] = -1;
                this.buildArenaList();
                this.gameState = 1;
                for (n = 0; n < 8; n++) {
                    this.player.savedAmmo[n] = this.player.ammo[n];
                }
            } else {
                if (this.menuCursor == 0 && this.dialoguePending(0)) {
                    this.objectiveIndex = 0;
                } else if (this.menuCursor == 1 && this.dialoguePending(4)) {
                    this.objectiveIndex = 5;
                } else if (this.menuCursor == 2 && this.dialoguePending(1)) {
                    this.objectiveIndex = 0;
                } else if (this.menuCursor == 3 && this.dialoguePending(5)) {
                    this.objectiveIndex = 5;
                } else if (this.menuCursor == 4 && this.dialoguePending(2)) {
                    this.objectiveIndex = 1;
                } else if (this.menuCursor == 5 && (this.doorBits & 2) != 0) {
                    this.objectiveIndex = 3;
                } else if (this.menuCursor == 6 && this.dialoguePending(3)) {
                    this.objectiveIndex = 3;
                } else if (this.menuCursor == 7 && (this.doorBits & 4) != 0) {
                    this.objectiveIndex = 2;
                } else if (this.menuCursor == 8 && this.dialoguePending(13)) {
                    this.objectiveIndex = 0;
                } else if (this.menuCursor == 9 && this.dialoguePending(8)) {
                    this.objectiveIndex = 0;
                } else if (this.menuCursor == 10 && this.dialoguePending(8)) {
                    this.objectiveIndex = 5;
                } else if (this.menuCursor == 11 && this.dialoguePending(12)) {
                    this.objectiveIndex = 0;
                } else if (this.menuCursor == 12 && this.dialoguePending(9)) {
                    this.objectiveIndex = 5;
                } else if (this.menuCursor == 13 && this.dialoguePending(7)) {
                    this.objectiveIndex = 5;
                } else if (this.menuCursor == 14 && this.dialoguePending(10)) {
                    this.objectiveIndex = 5;
                } else if (this.menuCursor == 15 && this.dialoguePending(11)) {
                    this.objectiveIndex = 4;
                } else if (this.menuCursor == 16 && this.dialoguePending(14)) {
                    this.objectiveIndex = 0;
                } else if (this.menuCursor == 17) {
                    this.objectiveIndex = 6;
                } else {
                    this.objectiveIndex = -1;
                }
                this.tilesetIndex = 3;
                this.startLevel(this.mapNodeLevel[this.menuCursor], this.mapNodeRoom[this.menuCursor]);
                this.menuCursor = 0;
            }
        }
    }
    /** Stage summary keys. */
    public final void summaryKeys(int key, int act) {
        if (key == 53 || act == 8 || key == -6) {
            selectedItem = menuCursor = 0;
            gameState = 21;
            summaryLines = null;
            summaryVariant = 0;
            return;
        }
        if (act == 6) {
            if (app.menu.firstVisibleLine + app.menu.visibleLines < app.menu.lineCount) {
                app.menu.firstVisibleLine++;
            }
        } else if (act == 1 && app.menu.firstVisibleLine > 0) {
            app.menu.firstVisibleLine--;
        }
    }
    /** Game-saved screen keys: back to the map or the arena list. */
    public final void savedScreenKeys(int key, int act) {
        if (key == 53 || act == 8 || key == -6) {
            selectedItem = menuCursor = 0;
            if (stateBeforePause == 21) {
                selectMapHighlight();
                gameState = 3;
            } else if (stateBeforePause == 15) {
                buildArenaList();
                gameState = 1;
            }
            app.playEffect(3);
        }
    }
    /** Not-enough-bolts screen keys: back to the store. */
    public final void notEnoughBoltsKeys(int key, int act) {
        menuCursor = 0;
        gameState = 11;
        player.allAmmoPrice = computeAmmoPrices();
        app.playEffect(3);
    }
    /** Game-won screen keys (offers challenge mode once). */
    public final void gameWonKeys(int key, int act) {
        if (key == 53 || act == 8 || key == -6) {
            imgWinPicture = null;
            System.gc();
            sleep(20);
            if (!challengeMode) {
                menuCursor = 1;
                gameState = 23;
                return;
            }
            exitToMenu = true;
            if (totalScore > 0) {
                return;
            }
        } else if (act == 6) {
            if (app.menu.firstVisibleLine + app.menu.visibleLines < app.menu.lineCount) {
                app.menu.firstVisibleLine++;
            }
        } else if (act == 1 && app.menu.firstVisibleLine > 0) {
            app.menu.firstVisibleLine--;
        }
    }
    /** You-fail / arena-reward screen keys. */
    public final void failOrRewardKeys(int key, int act) {
        if (key == 53 || act == 8 || key == -6) {
            enemyAliveBits[0] = -1;
            enemyAliveBits[1] = -1;
            useMenuBackground();
            if (gameState == 19) {
                buildArenaList();
                gameState = 1;
            } else if (gameState == 15) {
                stateBeforePause = gameState;
                gameState = 10;
                app.saveToSlot(saveSlot);
            }
            hudDirty = true;
            repaintRequested = true;
        }
    }
    /** Arena description keys: start the arena or go back. */
    public final void arenaDescriptionKeys(int key, int act) {
        if (key == 53 || act == 8 || key == -6) {
            arenaDescription = null;
            startArena(selectedItem);
            hudDirty = true;
            repaintRequested = true;
            return;
        }
        if (key == -7) {
            arenaDescription = null;
            buildArenaList();
            gameState = 1;
            hudDirty = true;
            repaintRequested = true;
            player.ownedWeapons = savedWeapons;
            arenaActive = false;
            return;
        }
        if (act == 6) {
            if (app.menu.firstVisibleLine + app.menu.visibleLines < app.menu.lineCount) {
                app.menu.firstVisibleLine++;
            }
        } else if (act == 1 && app.menu.firstVisibleLine > 0) {
            app.menu.firstVisibleLine--;
        }
    }
    /** Arena list keys: selects an unlocked arena and sets up its rules. */
    public final void arenaListKeys(int key, int act) {
        if (act == 6 || key == 56) {
            if ((this.mapUnlockBits & 1 << this.menuCursor + 21) == 0 || this.menuCursor == 11) {
                return;
            }
            this.menuCursor++;
            int n;
            e it = null;
            for (n = 0; n < this.arenaItems.size(); n++) {
                if ((it = (e)this.arenaItems.elementAt(n)).id == this.menuCursor) {
                    break;
                }
            }
            if (n - app.menu.firstVisibleLine + it.rows > app.menu.visibleLines) {
                app.menu.firstVisibleLine += it.rows;
            }
        } else if (act == 1 || key == 50) {
            if (this.menuCursor == 0) {
                return;
            }
            this.menuCursor--;
            int n;
            e it = null;
            for (n = this.arenaItems.size() - 1; n >= 0; n--) {
                if ((it = (e)this.arenaItems.elementAt(n)).id == this.menuCursor) {
                    break;
                }
            }
            if (n < app.menu.firstVisibleLine) {
                app.menu.firstVisibleLine -= it.rows;
            }
        } else if (key == 53 || act == 8 || key == -6) {
            this.savedWeapons = this.player.ownedWeapons;
            this.arenaActive = true;
            this.ruleGas = false;
            this.ruleOneHit = false;
            this.ruleOneWeapon = false;
            this.gasTimer = 0;
            switch (this.menuCursor) {
                case 0:
                    this.objectiveIndex = 7;
                    break;
                case 1:
                    this.objectiveIndex = 7;
                    break;
                case 2:
                    this.ruleOneWeapon = true;
                    this.player.ownedWeapons = 2;
                    this.player.weapon = 1;
                    this.objectiveIndex = 12;
                    break;
                case 3:
                    this.objectiveIndex = 7;
                    break;
                case 4:
                    this.ruleOneWeapon = true;
                    this.player.ownedWeapons = 1;
                    this.player.weapon = 0;
                    this.objectiveIndex = 8;
                    break;
                case 5:
                    this.ruleOneWeapon = true;
                    this.player.ownedWeapons = 2;
                    this.player.weapon = 1;
                    this.objectiveIndex = 12;
                    break;
                case 6:
                    this.ruleGas = true;
                    this.objectiveIndex = 9;
                    break;
                case 7:
                    this.ruleOneHit = true;
                    this.objectiveIndex = 10;
                    break;
                case 8:
                    this.objectiveIndex = 7;
                    break;
                case 9:
                    this.ruleOneHit = true;
                    this.objectiveIndex = 10;
                    break;
                case 10:
                    this.ruleGas = true;
                    this.objectiveIndex = 9;
                    break;
                case 11:
                    this.ruleOneWeapon = true;
                    this.player.ownedWeapons = 64;
                    this.player.weapon = 6;
                    this.objectiveIndex = 11;
            }
            this.currentRoom = (short)this.menuCursor;
            this.selectedItem = this.menuCursor;
            this.gameState = 14;
            if (this.arenaDescription == null) {
                this.arenaDescription = new Vector();
                int wd = screenWidth - 34;
                String blank = new String("");
                blank = null;
                if (this.ruleGas == true) {
                    f.appendAll(this.arenaDescription, ratchetandclank.wrapText(ratchetandclank.strings[228], wd));
                }
                if (this.ruleOneHit == true) {
                    f.appendAll(this.arenaDescription, ratchetandclank.wrapText(ratchetandclank.strings[230], wd));
                }
                f.appendAll(this.arenaDescription, ratchetandclank.wrapText(ratchetandclank.strings[229], wd));
                if (this.ruleOneWeapon == true) {
                    Object[] args = { new String(ratchetandclank.strings[this.player.weapon + 48]) };
                    this.textBoxText = null;
                    this.textBoxText = this.format(ratchetandclank.strings[231], args);
                    f.appendAll(this.arenaDescription, ratchetandclank.wrapText(this.textBoxText, wd));
                }
                Object[] args = { new Integer(arenaRewards[this.currentRoom]) };
                this.textBoxText = null;
                this.textBoxText = this.format(ratchetandclank.strings[249], args);
                f.appendAll(this.arenaDescription, ratchetandclank.wrapText(this.textBoxText, wd));
            }
            app.menu.lineCount = this.arenaDescription.size();
            app.menu.firstVisibleLine = 0;
        } else if (key == -7) {
            this.selectedItem = this.menuCursor = 0;
            this.selectMapHighlight();
            this.gameState = 3;
            if ((this.player.ownedWeapons & 1 << this.player.weapon) == 0 || this.player.weapon == 0) {
                this.player.weapon = 1;
            }
            for (int n = 0; n < 8; n++) {
                this.player.ammo[n] = this.player.savedAmmo[n];
            }
            app.playEffect(3);
        }
    }
    /** Challenge-mode question keys. */
    public final void challengeQuestionKeys(int key, int act) {
        if (act == 1 || key == 50) {
            menuCursor = cursorUp(menuCursor, (byte) 0, (byte) 1);
            return;
        }
        if (act == 6 || key == 56) {
            menuCursor = cursorDown(menuCursor, (byte) 1, (byte) 0);
            return;
        }
        if (key == 53 || act == 8 || key == -6) {
            app.playEffect(3);
            if (menuCursor == 0) {
                exitToMenu = true;
                if (totalScore > 0) {
                    return;
                }
            } else if (menuCursor == 1) {
                startChallengeMode();
            }
        }
    }
    /** Weapon select wheel keys. */
    public final void weaponWheelKeys(int key, int act) {
        int n = 0;
        if (key == 54 || key == 50 || act == 5 || act == 1) {
            player.weapon = wheelNext[player.weapon];
            while ((player.ownedWeapons & 1 << player.weapon) == 0 && n < 8) {
                player.weapon = wheelNext[player.weapon];
                n++;
            }
        } else if (key == 52 || key == 56 || act == 2 || act == 6) {
            player.weapon = wheelPrev[player.weapon];
            while ((player.ownedWeapons & 1 << player.weapon) == 0 && n < 8) {
                player.weapon = wheelPrev[player.weapon];
                n++;
            }
        } else if (act == 8 || key == 53) {
            selectedItem = menuCursor = 0;
            lastFrameStartMs = System.currentTimeMillis();
            gameState = 0;
            hudDirty = true;
        }
    }
    /** Save-over-data question keys. */
    public final void saveOverKeys(int key, int act) {
        if (act == 1 || key == 50) {
            menuCursor = cursorUp(menuCursor, (byte) 0, (byte) 1);
            return;
        }
        if (act == 6 || key == 56) {
            menuCursor = cursorDown(menuCursor, (byte) 1, (byte) 0);
            return;
        }
        if (key == 53 || act == 8 || key == -6) {
            app.playEffect(3);
            if (menuCursor == 0) {
                app.readSlotSummaries();
                gameState = 6;
            } else if (menuCursor == 1) {
                saveSlot = selectedItem;
                startNewGame(0);
            }
            menuCursor = 0;
            return;
        }
        if (key == -7) {
            app.playEffect(3);
            menuCursor = 0;
            gameState = 6;
        }
    }
    /** Save / replay keys: save the game, or restore the snapshot and replay the level. */
    public final void saveReplayKeys(int key, int act) {
        if (act == 1 || key == 50) {
            menuCursor = cursorUp(menuCursor, (byte) 0, (byte) 1);
            return;
        }
        if (act == 6 || key == 56) {
            menuCursor = cursorDown(menuCursor, (byte) 1, (byte) 0);
            return;
        }
        if (key == 53 || act == 8 || key == -6) {
            app.playEffect(3);
            if (menuCursor == 0) {
                stateBeforePause = gameState;
                gameState = 10;
                totalScore += stageScore;
                app.saveToSlot(saveSlot);
            } else if (menuCursor == 1) {
                doorBits = savedDoorBits;
                gateBitsA = savedGateBitsA;
                gateBitsB = savedGateBitsB;
                boltBitsA = savedBoltBitsA;
                boltBitsB = savedBoltBitsB;
                dialogueBitsA = savedDialogueBitsA;
                dialogueBitsB = savedDialogueBitsB;
                mapUnlockBits = savedMapUnlockBits;
                mapHighlightBits = savedMapHighlightBits;
                bolts = savedBolts;
                player.weapon = player.savedWeapon;
                player.ownedWeapons = savedWeapons;
                for (int n = 0; n < 8; n++) {
                    player.weaponXp[n] = player.savedWeaponXp[n];
                    player.weaponLevels[n] = player.savedWeaponLevels[n];
                    player.ammo[n] = player.savedAmmo[n];
                }
                if (levelIndex == 1) {
                    armModuleAvailable = true;
                }
                tilesetIndex = 3;
                startLevel(levelIndex, savedRoom);
            }
        }
    }
    /** Quit-to-main-menu question keys. */
    public final void quitToMenuKeys(int key, int act) {
        if (act == 1 || key == 50) {
            menuCursor = cursorUp(menuCursor, (byte) 0, (byte) 1);
            return;
        }
        if (act == 6 || key == 56) {
            menuCursor = cursorDown(menuCursor, (byte) 1, (byte) 0);
            return;
        }
        if (key == 53 || act == 8 || key == -6) {
            if (menuCursor == 0) {
                gameState = 4;
                menuCursor = 0;
                app.playEffect(3);
            } else if (menuCursor == 1) {
                arenaActive = true;
                player.ownedWeapons = savedWeapons;
                player.weapon = 1;
                arenaActive = false;
                app.saveToSlot(saveSlot);
                exitToMenu = true;
            }
        } else if (key == -7) {
            app.playEffect(3);
            menuCursor = 0;
            gameState = 4;
        }
    }
    /** Really-quit-game question keys (ends the MIDlet). */
    public final void quitGameKeys(int key, int act) {
        if (act == 1 || key == 50) {
            menuCursor = cursorUp(menuCursor, (byte) 0, (byte) 1);
            return;
        }
        if (act == 6 || key == 56) {
            menuCursor = cursorDown(menuCursor, (byte) 1, (byte) 0);
            return;
        }
        if (key == 53 || act == 8 || key == -6) {
            if (menuCursor == 0) {
                gameState = 4;
                menuCursor = 0;
                app.playEffect(3);
            } else if (menuCursor == 1) {
                arenaActive = true;
                player.ownedWeapons = savedWeapons;
                player.weapon = 1;
                arenaActive = false;
                app.saveToSlot(saveSlot);
                app.stopMusic();
                app.notifyDestroyed();
            }
        } else if (key == -7) {
            menuCursor = 0;
            gameState = 4;
            app.playEffect(3);
        }
    }
    /**
     * Play keys: dialogue advance, soft keys (pause), movement, jump / wall jump / glide / grapple,
     * fire (5), wrench (*), weapon wheel (#), side jumps (1, 3); with the secret sequence entered,
     * debug keys 7 (finish level), 9 (titanium bolt) and 0 (invulnerability, all weapons, all levels, doors).
     */
    public final void handlePlayKey(int key, int act) {
        if (this.textBoxOpen == true) {
            if (act == 8 || key == 53) {
                if (this.textPos >= 0) {
                    this.textPos = this.nextTextPos;
                }
                if (this.textPos < 0) {
                    this.closeTextBox();
                    this.nextTextPos = 0;
                }
            }
            return;
        }
        if (this.gameState == 16 || this.gameState == 17 || this.gameState == 24) {
            return;
        }
        if (key == -6 || key == -7) {
            this.menuCursor = 0;
            this.stateBeforePause = this.gameState;
            this.gameState = 4;
            this.useMenuBackground();
            return;
        }
        if (this.player.animId == 10) {
            this.heldAction = this.bufferedAction = 0;
            this.fireState &= -129;
            this.fireTime = 0L;
            return;
        }
        if (act == 1) {
            if (this.player.animId == 11) {
                return;
            }
            int dir = 0;
            if (this.player.grappleState == -1) {
                this.player.fireGrapple();
            } else if (this.player.jumpState >= 0) {
                if (this.player.noFloorLeft() && !this.player.facingRight) {
                    if (!cameraLocked) {
                        cameraLocked = true;
                        cameraLockX = this.player.pixelX() - 8 + (tileWidth >> 1);
                    }
                    dir = -1;
                    this.player.speedX = 2500;
                    this.player.jumpState = 0;
                    this.player.facingRight = true;
                    this.player.setAnimation((byte)4);
                    this.bufferedAction = this.heldAction = 0;
                } else if (this.player.noFloorRight() && this.player.facingRight == true) {
                    if (!cameraLocked) {
                        cameraLocked = true;
                        cameraLockX = this.player.pixelX() + 8 - (tileWidth >> 1);
                    }
                    dir = 1;
                    this.player.speedX = -2500;
                    this.player.jumpState = 0;
                    this.player.facingRight = false;
                    this.player.setAnimation((byte)4);
                    this.bufferedAction = this.heldAction = 0;
                }
            }
            if (this.player.jumpState < 1 || dir != 0) {
                this.heldAction = act;
            }
        } else if (act == 6) {
            if (this.player.animId == 5) {
                return;
            }
            if (this.player.jumpState == 2) {
                this.player.setAnimation((byte)3);
                this.player.speedX = 0;
                this.player.speedY = 0;
                this.player.jumpState = 1;
                if ((this.player.ownedWeapons & 1) > 0) {
                    this.bufferedAction = 0;
                    this.player.setAnimation((byte)14);
                    this.player.animMode = 1;
                    this.player.attackKind = 1;
                }
            } else if ((this.player.ownedWeapons & 1) > 0 && this.player.jumpState != -1 && this.player.animId != 11 && this.player.animId != 14) {
                this.bufferedAction = 0;
                this.player.setAnimation((byte)14);
                this.player.animMode = 1;
                this.player.attackKind = 1;
            } else if (onTeleporter == true) {
                this.leaveLevel(teleporterTarget, teleporterFlag);
            } else if (this.player.onPlatform == true) {
                this.player.onPlatform = false;
                this.player.tileRow++;
                this.player.startJump();
                this.player.platformIgnoreTimer = 10;
                if ((this.player.ownedWeapons & 1) > 0 && this.player.animId != 14) {
                    this.bufferedAction = 0;
                    this.player.setAnimation((byte)14);
                    this.player.animMode = 1;
                    this.player.attackKind = 1;
                }
            } else if (this.player.onClimbTile() && this.player.speedY == 0 && this.player.animId != 14 && this.player.animId != 11) {
                this.player.startJump();
                this.player.tileRow++;
                this.player.rowOffset = tileHeight - 2;
                if ((this.player.ownedWeapons & 1) > 0) {
                    this.bufferedAction = 0;
                    this.player.setAnimation((byte)14);
                    this.player.animMode = 1;
                    this.player.attackKind = 1;
                }
            }
        } else if (act == 2) {
            this.bufferedAction = this.heldAction = act;
            if (cameraLocked == true && this.player.pixelX() < cameraLockX + (tileWidth >> 1) && this.player.speedX > 0) {
                this.bufferedAction = this.heldAction = 0;
            }
        } else if (act == 5) {
            this.bufferedAction = this.heldAction = act;
            if (cameraLocked == true && this.player.pixelX() > cameraLockX - (tileWidth >> 1) && this.player.speedX < 0) {
                this.bufferedAction = this.heldAction = 0;
            }
        } else if (key == 53 || act == 8) {
            byte col = this.player.tileColumn();
            byte row = (byte)((this.player.pixelY() + cellHeight) / tileHeight);
            int tile;
            int bit = tile = this.levelMap.rawTile(col, row - 1);
            bit -= 36;
            bit = 1 << bit;
            if ((this.player.ownedWeapons & 1) > 0 && this.player.jumpState == -1 && this.player.animId != 11 && this.player.animId != 14 && this.player.animId != 8 && (this.enemyInWrenchReach() || this.crateInWrenchReach() || this.player.ammo[this.player.weapon] <= 0 || (this.doorBits & bit) != 0 && tile >= 36 && tile <= 42)) {
                this.bufferedAction = 0;
                this.player.attackKind = 0;
                this.player.wrenchAttack();
                return;
            }
            if (this.player.ammo[this.player.weapon] > 0 && this.fireState == 0) {
                this.player.fireWeapon();
                this.fireState = 129;
                shotsFired++;
                this.fireTime = System.currentTimeMillis();
            }
        } else if (key == 42) {
            this.bufferedAction = 0;
            if ((this.player.ownedWeapons & 1) > 0 && this.player.jumpState == -1) {
                this.player.attackKind = 0;
                this.player.wrenchAttack();
            }
        } else if (key == 35) {
            this.menuCursor = 0;
            this.gameState = 22;
            if (this.player.weapon != 0) {
                if (++this.player.weapon > 7) {
                    this.player.weapon = 1;
                }
                while ((this.player.ownedWeapons & 1 << this.player.weapon) == 0) {
                    if (++this.player.weapon > 7) {
                        this.player.weapon = 1;
                    }
                }
                this.hudDirty = true;
            }
        } else if (key == 55) {
            if (!this.debugKeysEnabled) {
                return;
            }
            if (this.levelIndex == 12) {
                this.bossHealth[4] = 0;
                return;
            }
            this.dialoguePortraits = true;
            this.dialogueLinesLeft = dialogueLineCounts[this.levelIndex - 1];
            this.openTextBox(dialogueFirstString[this.levelIndex - 1]);
            this.markDialogueSeen(this.levelIndex - 1);
            if (dialogueFirstString[this.levelIndex - 1] == 112 && (this.doorBits & 2) == 0) {
                this.summaryMessageId = 248;
                parIndex = 16;
                return;
            }
            this.summaryMessageId = 233 + (this.levelIndex - 1);
            parIndex = (byte)(this.levelIndex - 1);
        } else if (key == 49) {
            if (cameraLocked || this.player.animId == 11) {
                return;
            }
            if (this.player.grappleState >= 0 || this.player.animId == 5) {
                return;
            }
            if (this.player.animId != 12) {
                this.player.setAnimation((byte)0);
            }
            if (this.player.jumpState < 0) {
                this.player.speedY = this.player.opusKb;
                this.player.jumpState++;
            } else if (this.player.jumpState < 1) {
                this.player.speedY = this.player.opusKc;
                this.player.jumpState++;
                this.player.animMode = 1;
                this.player.setAnimation((byte)13);
            }
            this.heldAction = 2;
        } else if (key == 51) {
            if (cameraLocked || this.player.animId == 11) {
                return;
            }
            if (this.player.grappleState >= 0 || this.player.animId == 5) {
                return;
            }
            if (this.player.animId != 12) {
                this.player.setAnimation((byte)0);
            }
            if (this.player.jumpState < 0) {
                this.player.speedY = this.player.opusKb;
                this.player.jumpState++;
            } else if (this.player.jumpState < 1) {
                this.player.speedY = this.player.opusKc;
                this.player.jumpState++;
                this.player.animMode = 1;
                this.player.setAnimation((byte)13);
            }
            this.heldAction = 5;
        } else if (key == 57) {
            if (!this.debugKeysEnabled) {
                return;
            }
            this.awardTitaniumBolt(this.levelIndex, this.currentRoom);
        } else if (key == 48) {
            if (!this.debugKeysEnabled) {
                return;
            }
            if (!invulnerable) {
                invulnerable = true;
                System.out.println("lifeCheat=" + invulnerable);
                return;
            }
            if (this.mapUnlockBits == -1) {
                this.gateBitsA = 0;
                this.gateBitsB = 0;
                this.doorBits = 0;
                return;
            }
            if (this.player.ownedWeapons == -1) {
                this.mapUnlockBits = -1;
                return;
            }
            this.player.ownedWeapons = -1;
        }
    }
    /** Key release: stops firing and clears the held / buffered direction. */
    public final void handleKeyRelease(int key, int act) {
        if (key == 53 || act == 8) {
            fireState &= -129;
            if (player.animId != 8) {
                player.endOneShotAnimation();
            }
            fireTime = 0L;
        } else if ((fireState & 128) != 0 && fireTime != 0L && System.currentTimeMillis() - fireTime > 5000L) {
            fireState &= -129;
            if (player.animId != 8) {
                player.endOneShotAnimation();
            }
            fireTime = 0L;
        }
        if (act == heldAction) {
            heldAction = 0;
        }
        if (act == bufferedAction) {
            bufferedAction = 0;
        }
    }
    /** Enemy projectile shot against crates. */
    public final void enemyShotHitsCrates(int shot) {
        int w = imgCrates.getWidth();
        int h = imgCrates.getHeight() >> 3;
        int type;
        if ((type = enemyShots[shot].shotType) == -1 || type == 30 || type >= 15 && type <= 17) {
            return;
        }
        int ex = (enemyShots[shot].posX >> 8) - enemyShots[shot].hitHalfWidth;
        int ey = (enemyShots[shot].posY >> 8) - enemyShots[shot].hitHalfHeight;
        int ew = enemyShots[shot].hitHalfWidth << 1;
        int eh = enemyShots[shot].hitHalfHeight << 1;
        for (int i = 49; i >= 0; i--) {
            if (crateType[i] == -1) {
                continue;
            }
            if (crateX[i] + camX > screenWidth || crateX[i] + w + camX < 0 || crateY[i] + camY > screenHeight || crateY[i] + h + camY < 0) {
                continue;
            }
            if (boxesOverlap(crateX[i], crateY[i], w, h, ex, ey, ew, eh)) {
                enemyShots[shot].impact(true);
            }
        }
    }
    /** Player projectile shot against crates; shot == -1: wrench swing against crates. */
    public final void shotHitsCrates(int shot) {
        int iw = imgCrates.getWidth();
        int ih = imgCrates.getHeight() >> 3;
        if (shot == -1) {
            for (int n = 49; n >= 0; n--) {
                if (this.crateType[n] == -1) {
                    continue;
                }
                if (this.abs(this.crateX[n] - (this.player.posX >> 8)) > 3 * tileWidth >> 1 || this.abs(this.crateY[n] - this.player.pixelY()) > tileHeight * 2) {
                    continue;
                }
                if (this.boxesOverlap(this.crateX[n], this.crateY[n] + this.crateFall[n], imgCrates.getWidth(), imgCrates.getHeight() >> 2, (this.player.posX >> 8) + (this.player.facingRight ? a.wrenchBoxX[this.player.attackKind] : -a.wrenchBoxX[this.player.attackKind] - a.wrenchBoxW[this.player.attackKind]), this.player.pixelY() + a.wrenchBoxY[this.player.attackKind], a.wrenchBoxW[this.player.attackKind], a.wrenchBoxH[this.player.attackKind])) {
                    app.playEffect(4);
                    this.removeCrate(n);
                    shotsHit++;
                    if (this.crateType[n] == 0) {
                        this.spawnPickup(this.crateX[n] + (iw >> 1), this.crateY[n] + this.crateFall[n], 0);
                        this.spawnPickup(this.crateX[n] + (iw >> 1), this.crateY[n] + this.crateFall[n], 0);
                    } else {
                        this.spawnPickup(this.crateX[n] + (iw >> 1), this.crateY[n] + this.crateFall[n], this.crateType[n]);
                    }
                    this.spawnPlayerShot(this.crateX[n] + (iw >> 1) << 8, this.crateY[n] + (ih >> 1) + this.crateFall[n] << 8, 30);
                    this.crateType[n] = -1;
                }
            }
            return;
        }
        int t;
        if ((t = this.playerShots[shot].shotType) == -1 || t == 30 || t >= 15 && t <= 17) {
            return;
        }
        int x0 = (this.playerShots[shot].posX >> 8) - this.playerShots[shot].hitHalfWidth;
        int y0 = (this.playerShots[shot].posY >> 8) - this.playerShots[shot].hitHalfHeight;
        int w0 = this.playerShots[shot].hitHalfWidth << 1;
        int h0 = this.playerShots[shot].hitHalfHeight << 1;
        for (int n = 49; n >= 0; n--) {
            if (this.crateType[n] == -1) {
                continue;
            }
            if (this.crateX[n] + camX > screenWidth || this.crateX[n] + iw + camX < 0 || this.crateY[n] + camY > screenHeight || this.crateY[n] + ih + camY < 0) {
                continue;
            }
            if (this.boxesOverlap(this.crateX[n], this.crateY[n] + this.crateFall[n], iw, ih, x0, y0, w0, h0)) {
                this.removeCrate(n);
                shotsHit++;
                if (this.crateType[n] == 0) {
                    this.spawnPickup(this.crateX[n] + (iw >> 1), this.crateY[n] + this.crateFall[n], 0);
                    this.spawnPickup(this.crateX[n] + (iw >> 1), this.crateY[n] + this.crateFall[n], 0);
                } else {
                    this.spawnPickup(this.crateX[n] + (iw >> 1), this.crateY[n] + this.crateFall[n], this.crateType[n]);
                }
                this.spawnPlayerShot(this.crateX[n] + (iw >> 1) << 8, this.crateY[n] + (ih >> 1) + this.crateFall[n] << 8, 30);
                this.crateType[n] = -1;
                this.playerShots[shot].impact(false);
            }
        }
    }
    /** Lets loose pick-ups fall to the floor. */
    public final void dropPickups() {
        int u0 = 0;
        int step = tileHeight >> 3;
        int u1 = 0;
        for (int i = 11; i >= 0; i--) {
            if (pickupType[i] != -1) {
                pickupY[i] += step << 8;
                int floor = floorBelow((pickupX[i] >> 8) + 9, pickupY[i] >> 8, tileHeight - 19);
                if ((pickupY[i] >> 8) + 19 >= floor) {
                    pickupY[i] = floor - 19 << 8;
                }
            }
        }
    }
    /** Collects the room's titanium bolt when touched. */
    public final void collectTitaniumBolt() {
        if (boltColumn == -1) {
            return;
        }
        int u1 = 0;
        int u2 = 0;
        int x = boltColumn * tileWidth;
        int y = boltRow * tileHeight;
        if (boxesOverlap(player.pixelX() - 11, player.pixelY() + playerDrawOffsetY + 7, 18, 37, x, y, 19, 19)) {
            boltColumn = boltRow = -1;
            awardTitaniumBolt(levelIndex, currentRoom);
            titaniumCollected++;
        }
    }

    /** Draws the HUD: weapon icon and ammo, health bar, bolts and the grapple indicator. */
    public final void drawHud(Graphics graphics) {
        graphics.setFont(smallFont);
        graphics.setClip(0, 0, screenWidth, hudHeight);
        graphics.setColor(0);
        graphics.fillRect(0, 0, screenWidth, hudHeight);
        int i2 = 0;
        int i3 = 0;
        int ox = 51 + (screenWidth == 240 ? 32 : (screenWidth == 208 ? 16 : 0));
        int i5 = 0;
        int i6 = 0;
        int i7 = 0;
        int sx = screenWidth - (tileWidth >> 1) + 2;
        int i9 = 0;
        int ih = imgIcons.getHeight() / 12;
        int iw = imgIcons.getWidth();
        if (this.player.health < 0) {
            this.player.health = 0;
        }
        int hp = 20 - this.player.health;
        byte k;
        int bx;
        int by;
        int cell = ih >> 1;
        graphics.drawImage(imgHud, 0, 0, 0);
        graphics.setColor(255, 255, 255);
        if ((k = this.player.weapon) != 6 && k != 0) {
            int digits = 1;
            int v = this.player.ammo[k] / 10;
            while (v > 0) {
                v /= 10;
                digits++;
            }
            v = this.player.ammo[k];
            int dx = 3 + iw + 6 + (digits - 1) * 6;
            for (int d = 0; d < digits; d++) {
                graphics.drawRegion(imgDigits, v % 10 * 5, 0, 5, 10, 0, dx, 4, 20);
                dx -= 6;
                v /= 10;
            }
        }
        int digits = 1;
        int v = this.bolts / 10;
        while (v > 0) {
            v /= 10;
            digits++;
        }
        v = this.bolts;
        int dx = sx - 6;
        for (int d = 0; d < digits; d++) {
            graphics.drawRegion(imgDigits, v % 10 * 5, 0, 5, 10, 0, dx, 4, 20);
            dx -= 6;
            v /= 10;
        }
        graphics.setColor(37, 84, 106);
        for (k = 0; k < hp; k++) {
            bx = (k >> 1) * cell;
            by = k % 2 * cell;
            graphics.fillRect(ox + bx, 4 + by, cell, cell);
        }
        if (grappleAvailable == true) {
            graphics.setClip(screenWidth - iw >> 1, hudHeight, iw, ih);
            graphics.drawImage(imgIcons, screenWidth - iw >> 1, hudHeight - 7 * ih, 0);
        }
        if (this.player.weapon != 0) {
            graphics.setClip(3, 4, iw, ih);
            graphics.drawImage(imgIcons, 3, 4 - (this.player.weapon - 1) * ih, 0);
        }
    }
    /** Draws the grind rails. */
    public final void drawRails(Graphics gr) {
        int ox = camX;
        int oy = camY + playerDrawOffsetY;
        for (int i = 3; i >= 0; i--) {
            if (railX1[i] != -1) {
                int x1 = railX1[i] + ox;
                int y1 = railY1[i] + oy;
                int x2 = railX2[i] + ox;
                int y2 = railY2[i] + oy;
                if (x1 > screenWidth && x2 > screenWidth || x1 < 0 && x2 < 0 || y1 > screenHeight && y2 > screenHeight || y1 < 0 && y2 < 0) {
                    continue;
                }
                gr.setClip(0, hudHeight, screenWidth, screenHeight - hudHeight);
                gr.setColor(0xEEEEEE);
                gr.drawLine(x1, y1 - 2, x2, y2 - 2);
                gr.setColor(0xBBBBBB);
                gr.drawLine(x1, y1 - 1, x2, y2 - 1);
                gr.setColor(0x888888);
                gr.drawLine(x1, y1, x2, y2);
                gr.setColor(0x555555);
                gr.drawLine(x1, y1 + 1, x2, y2 + 1);
                gr.setColor(0x222222);
                gr.drawLine(x1, y1 + 2, x2, y2 + 2);
            }
        }
    }
    /** Draws the moving platforms. */
    public final void drawPlatforms(Graphics g) {
        int w = tileWidth;
        int h = tilesetIndex <= 1 ? 14 : 16;
        int cut;
        for (int i = 3; i >= 0; i--) {
            if (platformDir[i] < 0) {
                continue;
            }
            int x = platformX[i] + platformDx[i] + camX;
            int y = platformY[i] + platformDy[i] + camY;
            if (x >= screenWidth || x + w < 0 || y >= screenHeight || y + h < 0) {
                continue;
            }
            cut = 0;
            if (y < hudHeight) {
                cut = hudHeight - y;
                y = hudHeight;
            }
            if (cut < h && cut >= 0) {
                int off = tilesetIndex * 14;
                g.setClip(x, y, w, h - cut);
                g.drawImage(imgPlatform, x, y - cut - off, 0);
            }
        }
    }
    /** Falling crates: break on the player or an enemy, otherwise land. */
    public final void updateFallingCrates() {
        int i;
        int j;
        int w = imgCrates.getWidth();
        int h = imgCrates.getHeight() >> 3;
        int dx = 8;
        int px = player.posX >> 8;
        int py = player.pixelY();
        int dy = 16;
        int v = cellHeight;
        for (i = 49; i >= 0; i--) {
            if (crateFalling[i] == true && crateType[i] != -1) {
                if (crateBelow[i] == -1 || !crateFalling[crateBelow[i]]) {
                    if (boxesOverlap(crateX[i], crateY[i] + crateFall[i], w, h, px - dx, py, dy, v)) {
                        if (crateType[i] == 0) {
                            spawnPickup(crateX[i] + (w >> 1), crateY[i] + crateFall[i], 0);
                            spawnPickup(crateX[i] + (w >> 1), crateY[i] + crateFall[i], 0);
                        } else {
                            spawnPickup(crateX[i] + (w >> 1), crateY[i] + crateFall[i], crateType[i]);
                        }
                        removeCrate(i);
                        spawnPlayerShot(crateX[i] + (w >> 1) << 8, crateY[i] + (h >> 1) + crateFall[i] << 8, 30);
                        crateType[i] = -1;
                        continue;
                    }
                    dx = 12;
                    dy = 24;
                    for (j = enemySlotCount - 1; j >= 0; j--) {
                        if (enemies[j].actorKind < 0) {
                            continue;
                        }
                        px = enemies[j].posX >> 8;
                        py = enemies[j].feetY();
                        if (px + 12 < crateX[i] || px - 12 > crateX[i] + w) {
                            continue;
                        }
                        if (boxesOverlap(crateX[i], crateY[i] + crateFall[i], w, h, px - 12, py, 24, v)) {
                            removeCrate(i);
                            crateFalling[i] = false;
                            if (crateType[i] == 0) {
                                spawnPickup(crateX[i] + (w >> 1), crateY[i] + crateFall[i], 0);
                                spawnPickup(crateX[i] + (w >> 1), crateY[i] + crateFall[i], 0);
                            } else {
                                spawnPickup(crateX[i] + (w >> 1), crateY[i] + crateFall[i], crateType[i]);
                            }
                            spawnPlayerShot(crateX[i] + (w >> 1) << 8, crateY[i] + (h >> 1) + crateFall[i] << 8, 30);
                            crateType[i] = -1;
                            break;
                        }
                    }
                }
                crateFall[i] += 4;
                if (crateType[i] != -1 && crateFall[i] + crateY[i] >= crateLandY[i]) {
                    crateY[i] = crateLandY[i];
                    crateFall[i] = 0;
                    crateFalling[i] = false;
                }
            }
        }
    }
    /** Removes crate {@code crate}: marks it destroyed and lets the crates stacked on it fall. */
    public final void removeCrate(int crate) {
        int cur = crateAbove[crate];
        short prev = 0;
        short tmp;
        boolean flag = false;
        int lvl = currentRoom;
        if (arenaActive == true) {
            lvl = 0;
        }
        crateIntactBits[crate + lvl * 50 >> 5] &= ~(1 << crate - (crate >> 5 << 5));
        if (crateFalling[crate] == true || cur != -1 && crateFalling[cur] == true) {
            flag = true;
        }
        while (cur != -1) {
            crateFalling[cur] = true;
            if (flag == true) {
                if (crateBelow[cur] == crate) {
                    prev = crateLandY[cur];
                    crateLandY[cur] = crateLandY[crate];
                } else {
                    tmp = crateLandY[cur];
                    crateLandY[cur] = prev;
                    prev = tmp;
                }
            } else {
                crateLandY[cur] = crateY[crateBelow[cur]];
            }
            cur = crateAbove[cur];
        }
        if (crateAbove[crate] != -1) {
            crateBelow[crateAbove[crate]] = crateBelow[crate];
        }
        if (crateBelow[crate] != -1) {
            crateAbove[crateBelow[crate]] = crateAbove[crate];
        }
        crateAbove[crate] = -1;
        crateBelow[crate] = -1;
    }
    /** Draws the crates. */
    public final void drawCrates(Graphics g) {
        int width = imgCrates.getWidth();
        int height = imgCrates.getHeight() >> 3;
        int ox = camX;
        int oy = camY;
        for (int i = 0; i < 50; i++) {
            if (crateType[i] < 0) {
                continue;
            }
            int cut;
            int sx = crateX[i] + ox;
            int sy = crateY[i] + crateFall[i] + oy;
            if (sx >= screenWidth || sx + width < 0 || sy >= screenHeight || sy + height < 0) {
                continue;
            }
            cut = 0;
            if (sy < hudHeight) {
                cut = hudHeight - sy;
                sy = hudHeight;
            }
            if (cut < height && cut >= 0) {
                g.setClip(sx, sy, width, height - cut);
                g.drawImage(imgCrates, sx, sy - crateType[i] * height - cut, 0);
            }
        }
    }
    /** Draws the loose pick-ups. */
    public final void drawPickups(Graphics g) {
        int u0 = 0;
        int u1 = 0;
        int ox = camX;
        int oy = camY;
        int cut;
        for (int i = 11; i >= 0; i--) {
            if (pickupType[i] < 0) {
                continue;
            }
            int x = (pickupX[i] >> 8) + ox;
            int y = (pickupY[i] >> 8) + oy;
            if (x >= screenWidth || x + 19 < 0 || y >= screenHeight || y + 19 < 0) {
                continue;
            }
            cut = 0;
            if (y < hudHeight) {
                cut = hudHeight - y;
                y = hudHeight;
            }
            if (cut < 19 && cut >= 0) {
                g.setClip(x, y, 19, 19 - cut);
                g.drawImage(imgBolts, x, y - pickupType[i] * 19 - cut, 0);
            }
        }
    }
    /** Draws the payola pick-up. */
    public final void drawPayola(Graphics g) {
        int w = imgPayola.getWidth();
        int h = imgPayola.getHeight();
        int cut;
        int x = payolaColumn * tileWidth + camX;
        int y = payolaRow * tileHeight + camY;
        if (x >= screenWidth || x + w < 0 || y >= screenHeight || y + h < 0) {
            return;
        }
        cut = 0;
        if (y < hudHeight) {
            cut = hudHeight - y;
            y = hudHeight;
        }
        if (cut < h && cut >= 0) {
            g.setClip(x, y, w, h - cut);
            g.drawImage(imgPayola, x, y - cut, 0);
        }
    }
    /** Collects the payola pick-up (dialogue and "bribe the bouncer" message). */
    public final void collectPayola() {
        if (payolaColumn == -1) {
            return;
        }
        int h = imgPayola.getHeight();
        int w = imgPayola.getWidth();
        int x = payolaColumn * tileWidth;
        int y = payolaRow * tileHeight;
        if (boxesOverlap(player.pixelX() - 11, player.pixelY() + playerDrawOffsetY + 7, 18, 37, x, y, h, w)) {
            payolaColumn = payolaRow = -1;
            dialoguePortraits = true;
            dialogueLinesLeft = dialogueLineCounts[11];
            openTextBox(dialogueFirstString[11]);
            markDialogueSeen(11);
            summaryMessageId = 261;
            parIndex = 11;
        }
    }

    /** Draws the enemies, the player and all projectiles. */
    public final void drawActors(Graphics graphics) {
        int i;
        int j;
        int k;
        graphics.setClip(0, hudHeight, screenWidth, screenHeight - hudHeight);
        for (i = enemySlotCount - 1; i >= 0; i--) {
            if (this.enemies[i].actorKind != -1) {
                this.enemies[i].draw(graphics, i, this.enemies[i].drawLayer, camX, camY);
            }
        }
        this.player.draw(graphics, this.player.drawLayer, camX, camY);
        for (j = 9; j >= 0; j--) {
            this.enemyShots[j].draw(graphics);
        }
        for (k = 9; k >= 0; k--) {
            this.playerShots[k].draw(graphics);
        }
    }
    /** Boss: fires a projectile (type 31) in aim direction turretDir. */
    public final void bossFire() {
        for (int i = 9; i >= 0; i--) {
            if (enemyShots[i].shotType != -1) {
                continue;
            }
            int u2 = 0;
            enemyShots[i].posX = tileWidth * 14 << 8;
            enemyShots[i].posY = tileHeight * 9 << 8;
            enemyShots[i].shotType = 31;
            enemyShots[i].tick = 0;
            enemyShots[i].sourceVariant = 25;
            enemyShots[i].hitHalfWidth = h.halfWidths[31];
            enemyShots[i].hitHalfHeight = h.halfHeights[31];
            if (turretDir == 0 || turretDir == 1 || turretDir == 7) {
                enemyShots[i].speedX = h.speeds[31] << 8;
            } else if (turretDir == 2 || turretDir == 6) {
                enemyShots[i].speedX = 0;
            } else {
                enemyShots[i].speedX = -(h.speeds[31] << 8);
            }
            if (turretDir == 2 || turretDir == 1 || turretDir == 3) {
                enemyShots[i].speedY = -(h.speeds[31] << 8);
            } else if (turretDir == 0 || turretDir == 4) {
                enemyShots[i].speedY = 0;
            } else {
                enemyShots[i].speedY = h.speeds[31] << 8;
            }
            return;
        }
    }

    /** Spawns an enemy on the nearest standable tile to (x, y) (8.8). */
    public final void spawnEnemyNear(int x, int y) {
        int col = (x >> 8) / tileWidth;
        int row = (y >> 8) / tileHeight;
        if (!levelMap.isSolid(col, row)) {
            if (levelMap.isSolid(col, row - 1) == true) {
                row--;
            } else if (levelMap.isSolid(col, row + 1) == true) {
                row++;
            } else if (levelMap.isSolid(col + 1, row) == true) {
                col++;
            } else if (levelMap.isSolid(col - 1, row) == true) {
                col--;
            } else {
                return;
            }
        }
        spawnEnemy(col, row, (byte) 1, -1);
    }
    /**
     * Boss fight update: aiming, firing pattern, cannon damage; the core's destruction starts the ending.
     */
    public final void updateBoss() {
        if (bossHealth[4] <= 0) {
            app.markGameWon();
            levelIndex = 100;
            menuCursor = 0;
            gameState = 24;
            bossHealth[4] = 1;
            player.speedY = 256;
            int i;
            for (i = 0; i < enemySlotCount; i++) {
                enemies[i].actorKind = -1;
            }
            int j;
            for (j = 9; j >= 0; j--) {
                enemyShots[j].shotType = -1;
            }
            int k;
            for (k = 9; k >= 0; k--) {
                enemyShots[k].shotType = -1;
            }
            int m;
            for (m = 11; m >= 0; m--) {
                pickupType[m] = -1;
            }
            return;
        }
        int px = player.pixelX();
        int py = player.pixelY() + playerDrawOffsetY + (cellHeight >> 1);
        int ox = tileWidth * 14;
        int oy = tileHeight * 9;
        int dx = px - ox;
        int dy = py - oy;
        int u7 = 0;
        int r = tileHeight;
        if (bossPattern <= 2) {
            if (dx > r) {
                if (dy > r) {
                    turretDir = 7;
                } else if (dy < -r) {
                    turretDir = 1;
                } else {
                    turretDir = 0;
                }
            } else if (dx < -r) {
                if (dy > r) {
                    turretDir = 5;
                } else if (dy < -r) {
                    turretDir = 3;
                } else {
                    turretDir = 4;
                }
            } else {
                turretDir = 2;
                if (dy > 0) {
                    turretDir = 6;
                }
            }
        }
        if (++bossFireTimer > 30) {
            bossFireTimer = 0;
            if (++bossPattern > 12) {
                bossPattern = 0;
            }
            if (bossPattern > 2) {
                bossFireTimer = 7;
                if (++turretDir > 7) {
                    turretDir = 0;
                }
            }
            bossFire();
        }
        for (int i = 0; i < 4; i++) {
            if (cannonState[i] != 0 && bossHealth[i] > 0) {
                if (--cannonFlash[i] < 0) {
                    cannonState[i] = 0;
                }
            } else if (cannonState[i] != 2 && bossHealth[i] <= 0) {
                if (--cannonFlash[i] < 0) {
                    cannonState[i] = 2;
                }
            }
            shotsHitBossPart(i);
        }
        if (bossHealth[0] + bossHealth[1] + bossHealth[2] + bossHealth[3] == 0) {
            shotsHitBossPart(4);
        }
    }
    /** Loads the boss graphics and resets the boss state. */
    public final void loadBoss() {
        try {
            if (imgCannonBase == null) {
                imgCannonBase = Image.createImage("/cnnbse.png");
            }
            if (imgCannonShot == null) {
                imgCannonShot = Image.createImage("/cnnprj.png");
            }
            if (imgCannonBarrel == null) {
                imgCannonBarrel = Image.createImage("/cnnbrl.png");
            }
            if (imgMaximillian == null) {
                imgMaximillian = Image.createImage("/maxmil.png");
            }
        } catch (Exception e) {
        }
        bossCutsceneStep = 0;
        bossCutsceneJumps = 0;
        bossFightStarted = false;
        cutsceneStep = 0;
        endingExplosionCount = 0;
        turretDir = 4;
        cannonFlash[0] = cannonFlash[1] = cannonFlash[2] = cannonFlash[3] = 0;
        cannonState[0] = cannonState[1] = cannonState[2] = cannonState[3] = 0;
        bossHealth[0] = bossHealth[1] = bossHealth[2] = bossHealth[3] = 100;
        bossHealth[4] = 200;
        bossPattern = 0;
    }
    /** Starts challenge mode: a new run of level 1 keeping the weapons. */
    public final void startChallengeMode() {
        if (challengeMode) {
            exitToMenu = true;
            return;
        }
        objectiveIndex = -1;
        player.grappleState = -2;
        summaryMessageId = 0;
        fallingDeath = false;
        levelIndex = 1;
        armModuleAvailable = false;
        tilesetIndex = 3;
        levelMap.loadLevel(levelIndex);
        currentRoom = c.startRoom;
        mapHighlightBits = 1;
        targetRoom = currentRoom;
        levelMap.enterRoom(currentRoom, true);
        player.posX = checkpointColumn * tileWidth + (cellWidth >> 1) << 8;
        player.tileRow = player.aheadRow = checkpointRow;
        player.animFrame = 1;
        player.animTimer = 0;
        player.animId = 0;
        player.animMode = 0;
        player.rowOffset = 0;
        player.jumpState = -1;
        player.actorKind = 0;
        player.drawLayer = 1;
        player.health = 20;
        camX = checkpointCamX;
        camY = checkpointCamY;
        lastFrameStartMs = System.currentTimeMillis();
        recordRoomEnemies(currentRoom, 11229);
        player.blinkTimer = 0;
        player.facingRight = true;
        repaintRequested = true;
        hudDirty = true;
        player.setAnimation((byte) 0);
        player.animMode = 0;
        bounceBotFrame = 1;
        bounceBotTimer = 18;
        player.ownedWeapons &= -33;
        challengeMode = true;
        doorBits = -1;
        gateBitsA = -1;
        gateBitsB = -1;
        dialogueBitsA = -1;
        dialogueBitsB = -1;
        dialogueBitsB &= -221;
        mapUnlockBits = 1572865;
        player.weapon = 1;
        gameState = 0;
        heldAction = bufferedAction = 0;
    }
    /** Player projectiles against boss part {@code part} (0-3 cannons, 4 core). */
    public final void shotsHitBossPart(int part) {
        int x;
        int y;
        int w = tileWidth;
        int hgt = tileHeight;
        if (bossHealth[part] <= 0) {
            bossHealth[part] = 0;
            return;
        }
        if (part == 4) {
            w = tileWidth << 1;
            hgt = tileHeight << 1;
            x = cannonColumns[2] * tileWidth;
            y = cannonRows[2] * tileHeight;
        } else {
            x = cannonColumns[part] * tileWidth;
            y = cannonRows[part] * tileHeight;
        }
        if (x + w + camX < 0 || x + camX > screenWidth || y + hgt + camY < 0 || y + camY > screenHeight) {
            return;
        }
        for (int n = 9; n >= 0; n--) {
            int t;
            if ((t = playerShots[n].shotType) < 0 || t == 30 || t == 15 || t == 16 || t == 17) {
                continue;
            }
            int dx;
            int dy;
            int q = playerShots[n].hitHalfWidth;
            int r = playerShots[n].hitHalfHeight;
            dx = (playerShots[n].posX >> 8) - q;
            dy = (playerShots[n].posY >> 8) - r;
            if (boxesOverlap(x, y, w, hgt, dx, dy, q << 1, r << 1)) {
                bossHealth[part] -= h.damage[t];
                if (part == 4) {
                    cannonFlash[0] = cannonFlash[1] = cannonFlash[2] = cannonFlash[3] = 5;
                    cannonState[0] = cannonState[1] = cannonState[2] = cannonState[3] = 3;
                } else {
                    cannonFlash[part] = 5;
                    cannonState[part] = 1;
                }
                playerShots[n].impact(false);
                return;
            }
        }
    }
    /** Draws the boss turret and cannons. */
    public final void drawBoss(Graphics gr) {
        if (bossHealth[4] <= 0) {
            return;
        }
        int x;
        int y;
        int u4;
        int u5;
        int u6 = 0;
        int u7 = 0;
        int u8 = 0;
        int u9 = 0;
        u4 = 0;
        u5 = 0;
        x = tileWidth * 14 + camX;
        y = tileHeight * 9 + camY;
        gr.setClip(0, hudHeight, screenWidth, screenHeight - hudHeight);
        if (x < screenWidth && y < screenHeight && x + 22 >= 0 && y + 22 >= 0) {
            if (turretDir == 0) {
                x += tileWidth - 22;
                y = y - 11;
                gr.drawRegion(imgCannonBarrel, 0, 0, 22, 22, 0, x, y, 20);
            } else if (turretDir == 1) {
                x += tileWidth - 22 >> 1;
                y -= tileWidth + 22 >> 1;
                gr.drawRegion(imgCannonBarrel, 0, 22, 22, 22, 0, x, y, 20);
            } else if (turretDir == 2) {
                x = x - 11;
                y -= tileWidth;
                gr.drawRegion(imgCannonBarrel, 0, 0, 22, 22, 6, x, y, 20);
            } else if (turretDir == 3) {
                x -= tileWidth + 22 >> 1;
                y -= tileWidth + 22 >> 1;
                gr.drawRegion(imgCannonBarrel, 0, 22, 22, 22, 2, x, y, 20);
            } else if (turretDir == 4) {
                x -= tileWidth;
                y = y - 11;
                gr.drawRegion(imgCannonBarrel, 0, 0, 22, 22, 2, x, y, 20);
            } else if (turretDir == 5) {
                x -= tileWidth + 22 >> 1;
                y += tileWidth - 22 >> 1;
                gr.drawRegion(imgCannonBarrel, 0, 22, 22, 22, 3, x, y, 20);
            } else if (turretDir == 6) {
                x = x - 11;
                y += tileWidth - 22;
                gr.drawRegion(imgCannonBarrel, 0, 0, 22, 22, 5, x, y, 20);
            } else {
                x += tileWidth - 22 >> 1;
                y += tileWidth - 22 >> 1;
                gr.drawRegion(imgCannonBarrel, 0, 22, 22, 22, 1, x, y, 20);
            }
        }
        u5 = tileHeight;
        for (int k = 0; k < 4; k++) {
            x = cannonColumns[k] * tileWidth + camX;
            if ((y = cannonRows[k] * tileHeight + camY) + u5 <= hudHeight || y >= screenHeight || x >= screenWidth) {
                continue;
            }
            int t = cannonState[k];
            if (k == 0) {
                y += cannonFrames[t][3];
                gr.drawRegion(imgCannonBase, 0, cannonFrames[t][2], cannonFrames[t][0], cannonFrames[t][1], 0, x, y, 20);
            } else if (k == 1) {
                y += tileHeight - cannonFrames[t][1] - cannonFrames[t][3];
                gr.drawRegion(imgCannonBase, 0, cannonFrames[t][2], cannonFrames[t][0], cannonFrames[t][1], 1, x, y, 20);
            } else if (k == 2) {
                x += tileWidth - cannonFrames[t][0];
                y += cannonFrames[t][3];
                gr.drawRegion(imgCannonBase, 0, cannonFrames[t][2], cannonFrames[t][0], cannonFrames[t][1], 2, x, y, 20);
            } else if (k == 3) {
                x += tileWidth - cannonFrames[t][0];
                y += tileHeight - cannonFrames[t][1] - cannonFrames[t][3];
                gr.drawRegion(imgCannonBase, 0, cannonFrames[t][2], cannonFrames[t][0], cannonFrames[t][1], 3, x, y, 20);
            }
        }
    }

    /** Draws Maximillian (frame {@code frame}) during the boss cut-scene and starts his dialogue. */
    public final void drawMaximillian(Graphics graphics, byte frame) {
        int w = imgMaximillian.getWidth();
        int h = imgMaximillian.getHeight() / 5;
        int x = tileWidth * 14 + camX;
        int y = tileHeight * 7 + camY;
        this.player.animate();
        if (x - 3 < screenWidth && y < screenHeight && x - 3 + w >= 0 && y + h >= 0) {
            graphics.setClip(x - (tileWidth >> 1), y, w, h);
            graphics.drawImage(imgMaximillian, x - 3, y - h * frame, 17);
        }
        if (!this.textBoxOpen && frame == 4 && !this.bossFightStarted) {
            this.dialogueLinesLeft = dialogueLineCounts[31];
            this.dialoguePortraits = true;
            this.openTextBox(dialogueFirstString[31]);
        }
    }

    /**
     * Sets the sprite transform drawTransform for actor {@code actor} (playerActorId = the player) from its facing and wall orientation.
     */
    public final void setDrawTransform(int actor) {
        if (actor == playerActorId) {
            drawActorType = player.actorKind;
            drawActorAnim = player.animId;
            drawActorFrame = player.animFrame;
            if (player.facingRight) {
                drawTransform = 0;
                return;
            }
            drawTransform = 5;
            return;
        }
        if (actor >= 0) {
            drawActorType = enemies[actor].actorKind;
            drawActorAnim = enemies[actor].animId;
            drawActorFrame = enemies[actor].animFrame;
            if (enemies[actor].orientation != 0) {
                if (enemies[actor].orientation == 1) {
                    if (enemies[actor].facingRight) {
                        drawTransform = 4;
                        return;
                    }
                    drawTransform = 2;
                    return;
                }
                if (enemies[actor].orientation == 2) {
                    if (enemies[actor].facingRight) {
                        drawTransform = 6;
                        return;
                    }
                    drawTransform = 3;
                    return;
                }
                if (enemies[actor].orientation == 3) {
                    if (enemies[actor].facingRight) {
                        drawTransform = 1;
                        return;
                    }
                    drawTransform = 7;
                    return;
                }
            } else {
                if (enemies[actor].facingRight) {
                    drawTransform = 0;
                    return;
                }
                drawTransform = 5;
            }
        }
    }
    /** True if value lies in [start, start + length]. */
    private boolean inRange(int value, int start, int length) {
        return value >= start && value <= start + length;
    }

    /** Axis-aligned box overlap of (x, y, w, h) boxes (x1..h1) and (x2..h2). */
    public final boolean boxesOverlap(int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2) {
        return !(!this.inRange(x2, x1, w1) && !this.inRange(x1, x2, w2) || !this.inRange(y2, y1, h1) && !this.inRange(y1, y2, h2));
    }
    /** Sets the start (start == true) or end point of grind rail {@code rail} at tile (col, row). */
    public final void setRailPoint(int col, int row, boolean start, int rail) {
        short x = (short) (col * tileWidth + (tileWidth >> 1));
        short y = (short) (row * tileHeight + (tileHeight >> 1));
        if (start) {
            railX1[rail] = x;
            railY1[rail] = y;
        } else {
            railX2[rail] = x;
            railY2[rail] = y;
        }
        if (railX1[rail] != -1 && railX2[rail] != -1) {
            railDy[rail] = railY2[rail] - railY1[rail] << 8;
            railDx[rail] = (short) (railX2[rail] - railX1[rail]);
            railBase[rail] = (railY1[rail] << 8) - railDy[rail] * railX1[rail] / railDx[rail];
        }
    }
    /**
     * Places crate slot at tile (col, row) unless already destroyed (mode = horizontal placement).
     */
    public final void placeCrate(int col, int row, int type, int mode, int slot) {
        int lvl = currentRoom;
        if (arenaActive == true) {
            lvl = 0;
        }
        if (roomRespawn[lvl] == true && type > 0) {
            crateIntactBits[slot + lvl * 50 >> 5] |= 1 << slot - (slot >> 5 << 5);
        }
        if ((crateIntactBits[slot + lvl * 50 >> 5] & 1 << slot - (slot >> 5 << 5)) == 0) {
            return;
        }
        crateType[slot] = (byte) type;
        if (mode == 2) {
            crateX[slot] = (short) (col * tileWidth + (imgCrates.getWidth() >> 1));
        } else {
            crateX[slot] = (short) (col * tileWidth + mode * imgCrates.getWidth());
        }
        crateY[slot] = (short) (row * tileHeight);
        crateLandY[slot] = crateY[slot];
        crateFall[slot] = 0;
    }
    /** Drops every crate down to the floor or onto the crate below. */
    public final void dropCrates() {
        short j;
        short i;
        short tile;
        int y;
        int row;
        int col;
        boolean falling = false;
        for (i = 0; i < 50; i++) {
            if (crateType[i] == -1) {
                continue;
            }
            falling = true;
            row = (y = crateY[i]) / tileHeight;
            col = crateX[i] / tileWidth;
            while (falling) {
                if ((tile = levelMap.roomTiles[col][row + 1]) >= 19 && tile <= 27) {
                    break;
                }
                for (j = 0; j < 50; j++) {
                    if (crateType[j] != -1) {
                        if (j == i) {
                            continue;
                        }
                        if (crateX[j] == crateX[i] && crateY[j] == y + tileHeight) {
                            falling = false;
                            break;
                        }
                    }
                }
                if (falling == true) {
                    crateY[i] += tileHeight;
                    y += tileHeight;
                    row++;
                }
            }
        }
    }
    /** Settles the crates and links the stacks (crateAbove / crateBelow). */
    public final void linkCrateStacks() {
        short j;
        short i;
        int y;
        dropCrates();
        for (i = 49; i >= 0; i--) {
            if (crateType[i] == -1) {
                continue;
            }
            y = crateY[i];
            crateAbove[i] = crateBelow[i] = -1;
            for (j = 49; j >= 0; j--) {
                if (crateType[j] == -1) {
                    continue;
                }
                if (crateX[j] == crateX[i]) {
                    if (crateY[j] == y - tileHeight) {
                        crateAbove[i] = j;
                        crateBelow[j] = i;
                    } else if (crateY[j] == y + tileHeight) {
                        crateBelow[i] = j;
                        crateAbove[j] = i;
                    }
                }
            }
        }
    }
    /** Spawns a loose pick-up at (x, y): kind 0 = random bolt, 1 = nanotech, else ammo. */
    public final void spawnPickup(int x, int y, int kind) {
        x -= 9;
        for (int i = 11; i >= 0; i--) {
            if (pickupType[i] != -1) {
                continue;
            }
            if (kind == 0) {
                pickupType[i] = (byte) (abs(random.nextInt()) % 2);
            } else if (kind == 1) {
                pickupType[i] = 2;
            } else {
                pickupType[i] = 3;
            }
            pickupX[i] = x << 8;
            pickupY[i] = y << 8;
            pickupVx[i] = 0;
            pickupVy[i] = 768;
            return;
        }
    }
    /** Adds a moving platform at tile (col, row) with start direction kind. */
    public final void addPlatform(int col, int row, int kind) {
        for (int i = 3; i >= 0; i--) {
            if (platformDir[i] != -1) {
                continue;
            }
            platformDir[i] = (byte) kind;
            if (kind == 0 || kind == 1) {
                platformX[i] = (short) (col * tileWidth);
                platformY[i] = (short) (row * tileHeight);
                platformDx[i] = 0;
                platformDy[i] = 0;
                return;
            }
            if (kind == 2) {
                platformX[i] = (short) ((col - 2) * tileWidth);
                platformY[i] = (short) (row * tileHeight);
                platformDx[i] = (short) (tileWidth << 1);
                platformDy[i] = 0;
                return;
            }
            platformX[i] = (short) (col * tileWidth);
            platformY[i] = (short) ((row - 2) * tileHeight);
            platformDx[i] = 0;
            platformDy[i] = (short) (tileHeight << 1);
            return;
        }
    }

    /** Spawns player projectile / effect type image at (x, y) (8.8), using ammo for real shots. */
    public final void spawnPlayerShot(int x, int y, int image) {
        for (int n = 9; n >= 0; n--) {
            if (this.playerShots[n].shotType != -1) {
                continue;
            }
            if (image >= 0 && image <= 14 || image >= 18 && image <= 20) {
                this.player.ammo[this.player.weapon]--;
                this.hudDirty = true;
            }
            if (image >= 9 && image <= 11 || image >= 15 && image <= 17) {
                this.playerShots[n].hitHalfWidth = 84;
                this.playerShots[n].hitHalfHeight = 11;
                x += (this.player.facingRight ? 1 : -1) * 84 << 8;
            } else {
                this.playerShots[n].hitHalfWidth = h.halfWidths[image];
                this.playerShots[n].hitHalfHeight = h.halfHeights[image];
            }
            this.playerShots[n].pathX2 = this.playerShots[n].pathX1 = this.playerShots[n].posX = x;
            this.playerShots[n].pathY2 = this.playerShots[n].pathY1 = this.playerShots[n].posY = y;
            this.playerShots[n].shotType = (byte)image;
            this.playerShots[n].speedX = (this.player.facingRight ? 1 : -1) * h.speeds[image] << 8;
            this.playerShots[n].speedY = 0;
            this.playerShots[n].tick = 0;
            this.playerShots[n].beamOffset = this.player.facingRight ? x - this.player.posX : this.player.posX - x;
            if (image >= 3 && image <= 5) {
                this.playerShots[n].speedY = 768;
            }
            break;
        }
    }

    /** Spawns enemy projectile type t at (x, y) for enemy variant s, aimed by facing fl / dir. */
    public final void spawnEnemyShot(int x, int y, int t, boolean fl, boolean vert, int dir, byte s) {
        for (int i = 9; i >= 0; i--) {
            if (enemyShots[i].shotType != -1) {
                continue;
            }
            enemyShots[i].posX = x;
            enemyShots[i].posY = y;
            enemyShots[i].shotType = (byte) t;
            enemyShots[i].tick = 0;
            enemyShots[i].sourceVariant = s;
            enemyShots[i].hitHalfWidth = h.halfWidths[t];
            enemyShots[i].hitHalfHeight = h.halfHeights[t];
            if (dir == 0 || dir == 1) {
                if (vert) {
                    enemyShots[i].speedX = 0;
                    if (s == 17 || s == 20 || s == 23) {
                        enemyShots[i].speedY = (short) ((dir == 0 ? 1 : -1) * h.speeds[t] << 8);
                    } else {
                        enemyShots[i].speedY = (short) ((dir == 0 ? -1 : 1) * h.speeds[t] << 8);
                    }
                } else {
                    enemyShots[i].speedX = (short) ((fl ? 1 : -1) * h.speeds[t] << 8);
                    enemyShots[i].speedY = 0;
                }
            } else if (vert) {
                enemyShots[i].speedX = (short) ((dir == 3 ? -1 : 1) * h.speeds[t] << 8);
                enemyShots[i].speedY = 0;
            } else {
                enemyShots[i].speedX = 0;
                enemyShots[i].speedY = (short) ((fl ? -1 : 1) * h.speeds[t] << 8);
            }
            if (t == 32) {
                enemyShots[i].pathX1 = x;
                enemyShots[i].pathY1 = y;
                enemyShots[i].pathX2 = x;
                enemyShots[i].pathY2 = y - (4 * tileHeight << 8);
            }
            break;
        }
    }
    /**
     * Spawns enemy variant type at tile (tx, ty) in slot (-1 = any free slot) unless already killed.
     */
    public final void spawnEnemy(int tx, int ty, byte type, int slot) {
        if (slot == -1) {
            for (slot = enemySlotCount - 1; slot >= 0; slot--) {
                if (enemies[slot].actorKind == -1) {
                    break;
                }
            }
            if (slot == -1) {
                return;
            }
        } else {
            int lvl = currentRoom;
            if (arenaActive == true) {
                lvl = 0;
            }
            int w = slot + lvl * 10 >> 5;
            if (roomRespawn[lvl] == true) {
                enemyAliveBits[w] |= 1 << slot + lvl * 10 - (w << 5);
            }
            if ((enemyAliveBits[w] & 1 << slot + lvl * 10 - (w << 5)) == 0) {
                return;
            }
        }
        if (type >= 0 && type <= 2) {
            enemies[slot].actorKind = 1;
        } else if (type >= 3 && type <= 5) {
            enemies[slot].actorKind = 0;
        } else if (type >= 6 && type <= 13) {
            enemies[slot].actorKind = 2;
        } else {
            enemies[slot].actorKind = 3;
        }
        enemies[slot].posX = tx * tileWidth + (tileWidth >> 1) << 8;
        enemies[slot].tileRow = (byte) ty;
        enemies[slot].rowOffset = enemies[slot].speedX = enemies[slot].speedY = 0;
        enemies[slot].health = d.healthPerVariant[type];
        enemies[slot].orientation = 0;
        enemies[slot].facingRight = true;
        if (levelIndex == 12 && player.posX < enemies[slot].posX) {
            enemies[slot].facingRight = false;
        }
        enemies[slot].hitBySpin = false;
        enemies[slot].altAim = false;
        enemies[slot].turnTimer = 0;
        enemies[slot].spawnIndex = (byte) slot;
        enemies[slot].knockback = 0;
        enemies[slot].shooterState = 0;
        enemies[slot].turnTimer = 0;
        enemies[slot].homeX = enemies[slot].posX;
        if (enemies[slot].actorKind == 1) {
            enemies[slot].homeX = 0;
        }
        if (enemies[slot].actorKind == 3) {
            enemies[slot].facingRight = false;
            int t;
            if ((t = levelMap.rawTile(tx + 1, ty)) >= 19 && t <= 34) {
                enemies[slot].orientation = 3;
            }
            if ((t = levelMap.rawTile(tx - 1, ty)) >= 19 && t <= 34) {
                enemies[slot].orientation = 2;
            }
            if ((t = levelMap.rawTile(tx, ty - 1)) >= 19 && t <= 34) {
                enemies[slot].rowOffset = 3072;
                enemies[slot].orientation = 1;
            }
        }
        enemies[slot].variant = type;
        enemies[slot].setAnimation((byte) 0);
        enemies[slot].animMode = 1;
        enemies[slot].drawLayer = 1;
    }
    /** Adds a bolt-lock switch at tile (column, row). */
    public final void addBoltLock(byte column, byte row) {
        int n = 0;
        for (n = 0; n < 3; ++n) {
            if (this.lockColumn[n] != -1) {
                continue;
            }
            this.lockColumn[n] = column;
            this.lockRow[n] = row;
            return;
        }
    }
    /** Adds a door at tile (x, y); tile code kind (70-76, -119) selects its unlock bit. */
    public final void addDoor(byte x, byte y, int kind) {
        int i = 0;
        for (i = 0; i < 3; i++) {
            if (doorKind[i] != 0) {
                continue;
            }
            switch (kind) {
                case 70:
                    doorKind[i] = 1;
                    break;
                case 71:
                    doorKind[i] = 2;
                    break;
                case 72:
                    doorKind[i] = 4;
                    break;
                case 73:
                    doorKind[i] = 8;
                    break;
                case 74:
                    doorKind[i] = 16;
                    break;
                case 75:
                    doorKind[i] = 32;
                    break;
                case 76:
                    doorKind[i] = 64;
                    break;
                case -119:
                    doorKind[i] = -128;
                    break;
            }
            doorColumn[i] = x;
            doorRow[i] = (byte) (y - 1);
            return;
        }
    }
    /** Draws the doors / gates and the bolt-lock switches. */
    public final void drawDoors(Graphics gr) {
        int i;
        int d_;
        int x;
        int y;
        int h = imgDoors.getHeight() >> 2;
        int w = imgDoors.getWidth();
        int off = 0;
        for (i = 0; i < 3; i++) {
            if (doorKind[i] != -128 && (doorKind[i] == 0 || (doorBits & doorKind[i]) == 0)) {
                continue;
            }
            x = doorColumn[i] * tileWidth + camX + (tileWidth - w >> 1);
            y = doorRow[i] * tileHeight + camY;
            if (x + w < 0 || x >= screenWidth || y + h < 0 || y >= screenHeight) {
                continue;
            }
            if (doorKind[i] >= 2 && doorKind[i] <= 64) {
                off = h;
            } else if (doorKind[i] == -128) {
                off = h * 3;
                if ((doorBits & -128) != 0) {
                    off = h << 1;
                }
            }
            d_ = 0;
            if (y < hudHeight) {
                d_ = hudHeight - y;
                y = hudHeight;
            }
            if (d_ < h && d_ >= 0) {
                gr.setClip(x, y, w, h - d_);
                gr.drawImage(imgDoors, x, y - d_ - off, 0);
            }
        }
        for (i = 0; i < 3; i++) {
            if (lockColumn[i] == -1) {
                continue;
            }
            int m = levelMap.rawTile(lockColumn[i], lockRow[i]);
            m -= 36;
            m = 1 << m;
            x = lockColumn[i] * tileWidth + camX + (tileWidth - 19 >> 1);
            y = lockRow[i] * tileHeight + camY + tileHeight - 19;
            if (x + 19 < 0 || x >= screenWidth || y + 38 < 0 || y >= screenHeight) {
                continue;
            }
            d_ = 0;
            if (y < hudHeight) {
                d_ = hudHeight - y;
                y = hudHeight;
            }
            if (d_ < 38 && d_ >= 0) {
                gr.setClip(x, y, 19, 38 - d_);
                gr.drawImage(imgBolts, x, y - d_ - ((doorBits & m) == 0 ? 38 : 0) - 133, 0);
            }
        }
    }

    /**
     * Player projectiles against enemy {@code enemy}: damage (beam falls off with distance), kills, the
     * Boar-Zooka's pig conversion, kill credit and weapon level-ups (20 kills per level).
     */
    public final void shotsHitEnemy(int enemy) {
        if (this.enemies[enemy].actorKind == -1 || this.enemies[enemy].animId == 5) {
            return;
        }
        int kind = 0;
        int x = this.enemies[enemy].pixelX();
        int y = this.enemies[enemy].feetY();
        int t = this.enemies[enemy].actorKind;
        int ox = d.bodyBoxX[t];
        int oy = d.bodyBoxY[t];
        int w = d.bodyBoxW[t];
        int hh = d.bodyBoxH[t];
        if (x - ox + w + camX < 0 || x - ox + camX > screenWidth || y + oy + hh + camY < 0 || y + oy + camY > screenHeight) {
            return;
        }
        for (int n = 9; n >= 0; n--) {
            int k;
            if ((k = this.playerShots[n].shotType) < 0 || k == 30) {
                continue;
            }
            if (!(this.playerShots[n].isLive() || k >= 9 && k <= 11 || k >= 15 && k <= 17 || k == 2 || k >= 21 && k <= 29)) {
                continue;
            }
            int hx = (this.playerShots[n].posX >> 8) - this.playerShots[n].hitHalfWidth;
            int hy = (this.playerShots[n].posY >> 8) - this.playerShots[n].hitHalfHeight;
            if (this.boxesOverlap(x - ox, y + oy, w, hh, hx, hy, this.playerShots[n].hitHalfWidth << 1, this.playerShots[n].hitHalfHeight << 1)) {
                kind = this.playerShots[n].creditedWeapon();
                if ((t == 4 || t == 3) && kind == 6) {
                    continue;
                }
                if (k >= 9 && k <= 11) {
                    int dist;
                    if ((dist = this.abs(this.player.pixelX() - x)) < tileWidth) {
                        this.enemies[enemy].health -= h.damage[k];
                    } else if (dist < tileWidth * 2 || k > 10) {
                        this.enemies[enemy].health -= h.damage[k] >> 1;
                    } else {
                        this.enemies[enemy].health -= h.damage[k] >> 2;
                    }
                } else {
                    this.enemies[enemy].health -= h.damage[k];
                }
                this.playerShots[n].impact(false);
                this.spawnPlayerShot(x << 8, (y << 8) + (cellHeight << 7), 30);
                shotsHit++;
                if (this.enemies[enemy].health <= 0 && this.enemies[enemy].animId != 5) {
                    int lv = this.currentRoom;
                    if (this.arenaActive == true) {
                        lv = 0;
                    }
                    int idx = enemy + lv * 10 >> 5;
                    this.enemyAliveBits[idx] &= ~(1 << enemy + lv * 10 - (idx << 5));
                    if (kind == 6 && t != 4) {
                        boarKills++;
                        for (int m = 0; m < d.dropsPerVariant[this.enemies[enemy].variant]; m++) {
                            this.spawnPickup((short)(this.enemies[enemy].posX >> 8), this.enemies[enemy].feetY(), 0);
                        }
                        this.enemies[enemy].actorKind = 4;
                        this.enemies[enemy].variant = 24;
                        this.enemies[enemy].health = d.healthPerVariant[24];
                        if (this.player.weaponLevels[6] < 2) {
                            this.player.weaponXp[6]++;
                        }
                        if (this.player.weaponXp[6] >= 20) {
                            this.player.weaponLevels[6]++;
                            this.player.weaponXp[6] = 0;
                            this.dialoguePortraits = false;
                            this.openTextBox(73);
                            kills++;
                            if (this.challengeMode && ++this.challengeMultiplier > 10) {
                                this.challengeMultiplier = 10;
                            }
                            roomKillCounts[this.currentRoom]++;
                        }
                    } else {
                        kills++;
                        if (this.challengeMode && ++this.challengeMultiplier > 10) {
                            this.challengeMultiplier = 10;
                        }
                        roomKillCounts[this.currentRoom]++;
                        this.enemies[enemy].setAnimation((byte)5);
                        this.enemies[enemy].animMode = 1;
                        if (t != 4) {
                            for (int m = 0; m < d.dropsPerVariant[this.enemies[enemy].variant]; m++) {
                                this.spawnPickup((short)(this.enemies[enemy].posX >> 8), this.enemies[enemy].feetY(), 0);
                            }
                            if (this.player.weaponLevels[kind] < 2) {
                                this.player.weaponXp[kind]++;
                            }
                            if (this.player.weaponXp[kind] >= 20) {
                                this.player.weaponLevels[kind]++;
                                this.player.weaponXp[kind] = 0;
                                this.dialoguePortraits = false;
                                this.openTextBox(73);
                            }
                        }
                    }
                } else if (this.enemies[enemy].animId != 2) {
                    this.enemies[enemy].setAnimation((byte)4);
                    this.enemies[enemy].stateTimer = 0;
                }
                return;
            }
        }
    }
    /** Enemy projectiles against the player. */
    public final void enemyShotsHitPlayer() {
        int px = player.pixelX();
        int py = player.pixelY() + playerDrawOffsetY;
        int u3 = 0;
        int u4 = 0;
        int u5 = 0;
        int u6 = 0;
        for (int n = 9; n >= 0; n--) {
            if (player.animId == 10) {
                return;
            }
            int type;
            if ((type = enemyShots[n].shotType) == -1 || type == 30) {
                continue;
            }
            if (!enemyShots[n].isLive()) {
                continue;
            }
            int dx = (enemyShots[n].posX >> 8) - enemyShots[n].hitHalfWidth;
            int dy = (enemyShots[n].posY >> 8) - enemyShots[n].hitHalfHeight;
            if (boxesOverlap(px - 11, py + 7, 18, 37, dx, dy, enemyShots[n].hitHalfWidth << 1, enemyShots[n].hitHalfHeight << 1)) {
                int s = enemyShots[n].sourceVariant;
                if (type != 3 && type != 6 && type != 18) {
                    if (!invulnerable) {
                        player.health -= d.playerDamage[s];
                    }
                    challengeMultiplier = 1;
                    wasHit = true;
                    hudDirty = true;
                    player.setAnimation((byte) 9);
                    player.stateTimer = 0;
                    if (player.jumpState == 2) {
                        player.jumpState = 1;
                    }
                    spawnPlayerShot(px << 8, (py << 8) + (cellHeight << 7), 30);
                }
                if (type == 32) {
                    player.blinkTimer = 10;
                }
                enemyShots[n].impact(true);
            }
        }
    }
    /** Minimum. */
    public final int minOf(int left, int right) {
        return left < right ? left : right;
    }
    /** Maximum. */
    public final int maxOf(int left, int right) {
        return left > right ? left : right;
    }
    /** Draws the text box (with the speaker portrait for dialogues). */
    public final void drawTextBox(Graphics gr) {
        int u2 = 0;
        gr.setFont(smallFont);
        int y;
        int h = lineHeight * 3 + 6;
        if (dialoguePortraits && speakerPortrait[textId - 104] != -1 && h < imgPortraits.getHeight() / 5 + 6) {
            h = imgPortraits.getHeight() / 5 + 6;
        }
        y = getHeight() - h;
        gr.setClip(0, 0, getWidth(), getHeight());
        gr.setColor(0xFFFFFF);
        gr.fillRect(0, y, getWidth(), h);
        if (dialoguePortraits && speakerPortrait[textId - 104] != -1) {
            int ih = imgPortraits.getHeight() / 5;
            int iw = imgPortraits.getWidth();
            gr.setColor(52, 86, 92);
            gr.drawRoundRect(1, y + 1, screenWidth - 3 - 1, h - 3 - 1, 4, 4);
            y += h - ih >> 1;
            gr.setClip(3, y, imgPortraits.getWidth(), ih);
            gr.drawImage(imgPortraits, 3, y - ih * speakerPortrait[textId - 104], 20);
        }
    }
    /** Draws one wrapped line of s starting at character start; returns the next start. */
    public final int drawWrappedLine(Graphics gr, int start, String s, int x, int y, int anchor, int maxW) {
        int lineW = 0;
        int w = 0;
        int last = 0;
        int from;
        int pos;
        char ch;
        char[] chars = new char[s.length()];
        s.getChars(0, s.length(), chars, 0);
        lineW = 0;
        pos = from = start;
        ch = 0;
        boolean sp = false;
        do {
            w = 0;
            last = pos - 1;
            boolean end = false;
            boolean over = false;
            int hy = -1;
            do {
                if (pos >= s.length()) {
                    end = true;
                    break;
                }
                if ((ch = s.charAt(pos)) == ' ') {
                    w += smallFont.charWidth(ch);
                    pos++;
                    if (lineW + w > maxW && !sp) {
                        over = true;
                        break;
                    }
                    sp = true;
                    break;
                }
                if (ch == '-') {
                    hy = pos;
                }
                int cw = smallFont.charWidth(ch);
                if (lineW + w + cw > maxW && !sp) {
                    over = true;
                    break;
                }
                w += cw;
                pos++;
            } while (true);
            if (lineW + w > maxW || end || over) {
                if (s.charAt(from) == ' ') {
                    from++;
                }
                if (lineW + w > maxW) {
                    if (!over) {
                        pos = last;
                    } else if (hy >= 0) {
                        pos = hy + 1;
                    }
                } else if (over && !sp && hy >= 0) {
                    pos = hy + 1;
                }
                if ((anchor & 1) > 0) {
                    if (pos - from > 0) {
                        gr.drawChars(chars, from, pos - from, screenWidth >> 1, y, anchor);
                    }
                    return pos + (over ? 0 : 1);
                }
                if (pos - from > 0) {
                    gr.drawChars(chars, from, pos - from, x, y, anchor);
                }
                return pos + (over ? 0 : 1);
            }
            lineW += w;
        } while (true);
    }
    /**
     * Draws up to three lines of the text box from position pos; returns the next page position or -1.
     */
    public final int drawTextBoxPage(Graphics gr, int pos) {
        int x = 3;
        int y;
        int anchor = 17;
        if (dialoguePortraits && speakerPortrait[textId - 104] != -1) {
            x = imgPortraits.getWidth() + 3 + 3;
            anchor = 20;
        }
        gr.setColor(0);
        gr.setClip(0, 0, screenWidth, screenHeight);
        gr.setFont(smallFont);
        int len = textBoxText.length();
        if (dialoguePortraits && speakerPortrait[textId - 104] != -1) {
            int m = Math.max(imgPortraits.getHeight() / 5, lineHeight * 3);
            y = getHeight() - ((m - lineHeight * 3 >> 1) + lineHeight * 3 + 3);
        } else {
            y = getHeight() - (lineHeight * 3 + 3);
        }
        if ((pos = drawWrappedLine(gr, pos, textBoxText, x, y, anchor, screenWidth - x - 3)) >= len) {
            return -1;
        }
        if ((pos = drawWrappedLine(gr, pos, textBoxText, x, y + lineHeight, anchor, screenWidth - x - 3)) >= len) {
            return -1;
        }
        if ((pos = drawWrappedLine(gr, pos, textBoxText, x, y + lineHeight * 2, anchor, screenWidth - x - 3)) >= len) {
            return -1;
        }
        return pos;
    }
    /** Opens the text box with string id (formats MCGuFIN-part and titanium-bolt counters). */
    public final void openTextBox(int id) {
        player.blinkTimer = 0;
        textPos = 0;
        textId = id;
        if (id == 112 && !dialoguePending(4) && !dialoguePending(5) && !dialoguePending(6) && !dialoguePending(7) && !dialoguePending(9) && !dialoguePending(10)) {
            dialogueLinesLeft = dialogueLineCounts[16];
            textId = 150;
            textBoxText = null;
            textBoxText = ratchetandclank.strings[150];
        } else if ((id == 125 || id == 126 || id == 127 || id == 128 || id == 132 || id == 133) && dialoguePending(4) && dialoguePending(5) && dialoguePending(6) && dialoguePending(7) && dialoguePending(9) && dialoguePending(10)) {
            dialogueLinesLeft = dialogueLineCounts[17];
            textId = 156;
            textBoxText = null;
            textBoxText = ratchetandclank.strings[156];
        } else if (id == 125 || id == 126 || id == 127 || id == 128 || id == 132 || id == 133) {
            int n = 1;
            if (!dialoguePending(4)) {
                n++;
            }
            if (!dialoguePending(5)) {
                n++;
            }
            if (!dialoguePending(6)) {
                n++;
            }
            if (!dialoguePending(7)) {
                n++;
            }
            if (!dialoguePending(9)) {
                n++;
            }
            if (!dialoguePending(10)) {
                n++;
            }
            Object[] args = { new Integer(n) };
            textBoxText = null;
            textBoxText = format(ratchetandclank.strings[id], args);
        } else if (id == 227) {
            Object[] args = { new Integer(titaniumCollectedCount()), new Integer(boltsForRyno) };
            textBoxText = null;
            textBoxText = format(ratchetandclank.strings[id], args);
        } else {
            textBoxText = ratchetandclank.strings[id];
        }
        if (textBoxText == "") {
            textPos = -1;
        }
        textBoxOpen = true;
        if (dialoguePortraits && imgPortraits == null) {
            try {
                imgPortraits = Image.createImage("/prtrts.png");
            } catch (Exception e) {
            }
        }
    }

    /**
     * Text box closed: applies the story consequences of the line (map unlocks, doors, items) and advances the dialogue.
     */
    public final void closeTextBox() {
        this.textBoxOpen = false;
        if (this.textId == 104) {
            this.mapHighlightBits = 524288;
            this.mapUnlockBits |= 4;
        } else if (this.textId == 106) {
            this.mapHighlightBits = 16;
            this.mapUnlockBits |= 16;
            this.mapUnlockBits |= 262144;
        } else if (this.textId == 110) {
            this.mapHighlightBits = 128;
            this.mapUnlockBits |= 128;
        } else if (this.textId == 112) {
            this.mapHighlightBits = 10;
            this.mapUnlockBits |= 10;
            this.doorBits &= -3;
        } else if (this.textId == 150) {
            this.mapUnlockBits |= 32768;
            this.mapHighlightBits = 32768;
            this.player.ownedWeapons |= 32;
        } else if (this.textId == 125) {
            this.mapHighlightBits &= -3;
            this.mapHighlightBits |= 8192;
            this.mapUnlockBits |= 8192;
        } else if (this.textId == 126) {
            this.mapHighlightBits &= -9;
            this.mapHighlightBits |= 1024;
            this.mapUnlockBits |= 1024;
        } else if (this.textId == 127) {
            this.mapHighlightBits &= -1025;
            if ((this.dialogueBitsA & 128) == 0) {
                this.mapHighlightBits |= 512;
                this.mapUnlockBits |= 512;
            }
        } else if (this.textId == 128) {
            this.mapHighlightBits &= -8193;
            if ((this.dialogueBitsA & 64) == 0) {
                this.mapHighlightBits |= 512;
                this.mapUnlockBits |= 512;
            }
        } else if (this.textId == 129) {
            this.mapHighlightBits = 20480;
            this.mapUnlockBits |= 4096;
            this.mapUnlockBits |= 16384;
        } else if (this.textId == 132) {
            this.mapHighlightBits &= -4097;
            if (!this.dialoguePending(4) && !this.dialoguePending(5) && !this.dialoguePending(6) && !this.dialoguePending(7) && !this.dialoguePending(9) && !this.dialoguePending(10)) {
                this.mapUnlockBits |= 64;
                this.mapHighlightBits = 64;
            }
        } else if (this.textId == 133) {
            this.mapHighlightBits &= -16385;
            if (!this.dialoguePending(4) && !this.dialoguePending(5) && !this.dialoguePending(6) && !this.dialoguePending(7) && !this.dialoguePending(9) && !this.dialoguePending(10)) {
                this.mapUnlockBits |= 64;
                this.mapHighlightBits = 64;
            }
        } else if (this.textId == 134) {
            this.mapUnlockBits |= 2048;
            this.mapHighlightBits = 2048;
            this.doorBits &= -9;
        } else if (this.textId == 135) {
            this.mapUnlockBits |= 256;
            this.mapHighlightBits = 256;
            this.doorBits &= -129;
        } else if (this.textId == 140) {
            this.mapHighlightBits = 65536;
            this.mapUnlockBits |= 65536;
        } else if (this.textId == 144) {
            this.mapHighlightBits = 131072;
            this.mapUnlockBits |= 131072;
        } else if (this.textId == 156) {
            if (!this.dialoguePending(4)) {
                this.mapHighlightBits &= -3;
                this.mapHighlightBits |= 8192;
                this.mapUnlockBits |= 8192;
            } else {
                this.mapHighlightBits &= -9;
                this.mapHighlightBits |= 1024;
                this.mapUnlockBits |= 1024;
            }
        }
        if (--this.dialogueLinesLeft > 0) {
            this.openTextBox(this.textId + 1);
            return;
        }
        imgPortraits = null;
        this.dialoguePortraits = false;
        this.dialogueLinesLeft = 0;
        if (this.textId >= 104 && this.textId <= 156 + dialogueLineCounts[17] && this.levelIndex != 0) {
            this.useMenuBackground();
            computeScore(summaryVariant);
            this.gameState = 18;
            this.buildSummary();
        }
        if (this.textId + 1 == dialogueFirstString[33] + dialogueLineCounts[33]) {
            this.useMenuBackground();
            computeScore(summaryVariant);
            this.gameState = 18;
            this.buildSummary();
        }
        if (this.textId + 1 == dialogueFirstString[31] + dialogueLineCounts[31]) {
            this.bossFightStarted = true;
        }
        if (this.textId + 1 == dialogueFirstString[32] + dialogueLineCounts[32]) {
            this.player.setAnimation((byte)1);
            this.player.animMode = 0;
        }
    }

    /**
     * Serialises the game into a 214-byte save slot:
     * [0] 1 = in progress / 2 = new game, [1] map unlocks mapUnlockBits, [5] [9] titanium bolts boltBitsA / boltBitsB,
     * [13] dialogues dialogueBitsA, [17] owned weapons, [18..25] weapon levels, [26..41] weapon XP,
     * [42..57] ammo, [58] bolts, [62] door bits, [63] [67] door flags, [195] play time,
     * [199] arm module, [200] map highlight, [204] dialogues dialogueBitsB, [208] total score,
     * [212] challenge mode, [213] save slot.
     */
    public final void storeGame(byte[] bytes) {
        bytes[0] = (byte)(this.levelIndex == 0 ? 2 : 1);
        app.writeInt(this.mapUnlockBits, bytes, 1);
        app.writeInt(this.boltBitsA, bytes, 5);
        app.writeInt(this.boltBitsB, bytes, 9);
        app.writeInt(this.dialogueBitsA, bytes, 13);
        bytes[17] = this.player.ownedWeapons;
        for (int n = 0; n < 8; n++) {
            bytes[18 + n] = this.player.weaponLevels[n];
            app.writeShort(this.player.weaponXp[n], bytes, 26 + n * 2);
            app.writeShort(this.player.ammo[n], bytes, 42 + n * 2);
        }
        app.writeInt(this.bolts, bytes, 58);
        bytes[62] = this.doorBits;
        app.writeInt(this.gateBitsA, bytes, 63);
        app.writeInt(this.gateBitsB, bytes, 67);
        app.writeInt(this.playTimeMs, bytes, 195);
        bytes[199] = (byte)(this.armModuleAvailable ? 1 : 0);
        app.writeInt(this.mapHighlightBits, bytes, 200);
        app.writeInt(this.dialogueBitsB, bytes, 204);
        app.writeInt(totalScore, bytes, 208);
        bytes[212] = (byte)(this.challengeMode ? 1 : 0);
        bytes[213] = this.saveSlot;
    }

    /** Restores the game from a save slot (see storeGame(byte[])) and opens the level-select map. */
    public final void restoreGame(byte[] bytes) {
        if (bytes[0] == 2) {
            this.startNewGame(0);
            return;
        }
        this.mapUnlockBits = app.readInt(bytes, 1);
        this.boltBitsA = app.readInt(bytes, 5);
        this.boltBitsB = app.readInt(bytes, 9);
        this.dialogueBitsA = app.readInt(bytes, 13);
        this.player.ownedWeapons = bytes[17];
        for (int n = 0; n < 8; n++) {
            this.player.weaponLevels[n] = bytes[18 + n];
            this.player.weaponXp[n] = app.readShort(bytes, 26 + n * 2);
            this.player.ammo[n] = app.readShort(bytes, 42 + n * 2);
        }
        this.bolts = app.readInt(bytes, 58);
        this.doorBits = bytes[62];
        this.gateBitsA = app.readInt(bytes, 63);
        this.gateBitsB = app.readInt(bytes, 67);
        this.playTimeMs = app.readInt(bytes, 195);
        this.armModuleAvailable = bytes[199] == 1;
        this.mapHighlightBits = app.readInt(bytes, 200);
        this.dialogueBitsB = app.readInt(bytes, 204);
        totalScore = app.readInt(bytes, 208);
        this.challengeMode = bytes[212] == 0 ? false : true;
        this.saveSlot = bytes[213];
        this.selectedItem = this.menuCursor = 0;
        this.selectMapHighlight();
        this.gameState = 3;
    }

    /**
     * Per-frame housekeeping: weapon fire rate, boss cut-scene animation and menu animation ticks.
     */
    public final void frameHousekeeping() {
        if (this.gameState == 0) {
            int t = this.fireState & 63;
            int k = this.player.weaponLevels[this.player.weapon];
            if (t > 0) {
                this.fireState++;
                if (t >= a.fireDelays[k][this.player.weapon]) {
                    if ((this.fireState & 128) != 0) {
                        this.fireState = 129;
                        this.player.fireWeapon();
                    } else {
                        this.fireState = 0;
                    }
                }
            }
        }
        if (this.gameState == 11) {
            if ((this.mapHighlightBits & 524288) > 0) {
                this.mapHighlightBits = 4;
            }
        } else if (this.gameState == 16 && this.bossCutsceneStep == 4) {
            this.bossCutsceneTimer++;
            if (this.bossCutsceneTimer > 20) {
                if (this.bossFightStarted && this.maxFrame == 0) {
                    this.gameState = 0;
                    this.hudDirty = true;
                }
                if (this.bossFightStarted) {
                    this.maxFrame--;
                } else {
                    this.maxFrame++;
                }
                if (this.maxFrame > 4) {
                    this.maxFrame = 4;
                }
                this.bossCutsceneTimer = 0;
            }
            this.repaintRequested = true;
        }
        if (this.gameState != 0) {
            if (this.menuAnimTick++ > 12) {
                this.menuAnimTick = 0;
            }
            this.repaintRequested = true;
        }
    }
    /** Draws a centred menu entry (highlighted when sel); returns the next y. */
    public final int drawEntry(Graphics gr, String s, int x, int y, int anchor, boolean sel) {
        int res = 0;
        gr.setFont(sel ? boldFont : smallFont);
        gr.setColor(sel == true ? 0xFFFFFF : 0xDCDCFF);
        if (sel) {
            app.menu.highlightedText = s;
        }
        res = drawWrapped(gr, s, x, y, 17);
        return res;
    }
    /** Draws a menu entry with anchor (highlighted when sel); returns the next y. */
    public final int drawEntryAnchored(Graphics gr, String s, int x, int y, int anchor, boolean sel) {
        int res = 0;
        gr.setFont(sel ? boldFont : smallFont);
        if (sel) {
            app.menu.highlightedText = s;
        }
        gr.setColor(sel == true ? 0xFFFFFF : 0xDCDCFF);
        res = drawWrapped(gr, s, x, y, anchor);
        return res;
    }
    /** Draws save-slot entry slot: "slot.(empty)" or "slot. <play time>". */
    public final int drawSlotEntry(Graphics gr, byte slot, int x, int y, int anchor, boolean selected) {
        int res = 0;
        if (app.slotSummaries[slot] == 0) {
            res = drawEntryAnchored(gr, String.valueOf(slot + 1) + ratchetandclank.strings[31], x, y, 17, selected);
        } else {
            Object[] args = { new Integer(slot + 1), new String(formatTime(app.slotPlayTimes[slot])) };
            String s = format(ratchetandclank.strings[32], args);
            res = drawEntry(gr, s, x, y, anchor, selected);
        }
        return res;
    }
    /** Draws a screen title; returns the next y. */
    public final int drawTitle(Graphics gr, String s) {
        gr.setFont(titleFont);
        gr.setColor(0xDCDCFF);
        return drawWrapped(gr, s, getWidth() >> 1, 5, 17);
    }
    /** Draws the "Page page/pages" indicator. */
    private void drawPageIndicator(Graphics gr, int page, int pages) {
        Object[] args = { new Integer(page), new Integer(pages) };
        String s = format(ratchetandclank.strings[36], args);
        gr.drawString(s, screenWidth >> 1, screenHeight - 1, 33);
    }
    /** Formats a time in ms as H:MM:SS. */
    public final String formatTime(int ms) {
        int sec = ms / 1000;
        int min = (ms /= 60000) / 60;
        ms %= 60;
        sec %= 60;
        return String.valueOf(min) + (ms < 10 ? ":0" : ":") + String.valueOf(ms) + (sec < 10 ? ":0" : ":") + String.valueOf(sec);
    }
    /** Cursor up with wrap-around. */
    private byte cursorUp(byte value, byte min, byte wrap) {
        if (value > min) {
            value = (byte)(value - 1);
            return value;
        }
        return wrap;
    }
    /** Cursor down with wrap-around. */
    private byte cursorDown(byte value, byte max, byte wrap) {
        if (value < max) {
            value = (byte)(value + 1);
            return value;
        }
        return wrap;
    }
    /** Formats s, replacing %0-%9 with args. */
    public final String format(String s, Object[] args) {
        char ch;
        char nc;
        int idx = 0;
        int j;
        boolean digit = false;
        String result;
        StringBuffer sb = new StringBuffer(s.length());
        char[] chars = new char[s.length()];
        s.getChars(0, s.length(), chars, 0);
        for (int i = 0; i < chars.length; i++) {
            if ((ch = chars[i]) == '%' && (j = i + 1) < chars.length && (digit = Character.isDigit(nc = chars[j])) == true) {
                idx = Character.digit(nc, 10);
            }
            if (digit == true) {
                digit = false;
                sb.append(args[idx]);
                i = i + 1;
            } else {
                sb.append(ch);
            }
        }
        result = sb.toString();
        return result;
    }
    /** Draws s as a marquee scrolling between left and right; returns the line height. */
    public final int drawMarquee(Graphics gr, String s, int y, int left, int right) {
        int w = 0;
        int h;
        Font font;
        h = (font = gr.getFont()).getHeight();
        w = font.stringWidth(s);
        gr.setClip(left, 0, right - left, screenHeight);
        gr.drawString(s, marqueeX, y, 20);
        marqueeX -= 2;
        if (marqueeX + w <= left) {
            marqueeX = right;
        }
        return h;
    }



    /** Puts the level-select cursor on the highlighted destination. */
    private void selectMapHighlight() {
            this.menuCursor = 0;
            for (int n = 0; n < 19; ++n) {
                if ((this.mapHighlightBits & 1 << n) > 0) {
                    return;
                }
                ++this.menuCursor;
            }
        }
    /** Builds the arena list from the unlocked arenas. */
    private void buildArenaList() {
        menuCursor = 0;
        arenaListVariant = 0;
        int w = screenWidth - 36;
        arenaItems = new Vector();
        for (int i = 0; i < 12; i++) {
            if ((mapUnlockBits & 1 << i + 20) != 0) {
                int w1 = smallFont.stringWidth(ratchetandclank.strings[60 + i]);
                int w2 = boldFont.stringWidth(ratchetandclank.strings[60 + i]);
                f.appendItems(arenaItems, ratchetandclank.wrapText(ratchetandclank.strings[60 + i], w - (w2 - w1)), i);
            }
        }
        app.menu.lineCount = arenaItems.size();
        app.menu.firstVisibleLine = 0;
        if ((player.ownedWeapons & 64) > 0) {
            return;
        }
    }

    /** Computes the ammo refill prices ammoPrices and returns the price of refilling all weapons. */
    private int computeAmmoPrices() {
        int[] d;
        int sum = 0;
        d = new int[8];
        for (int n = 0; n < 8; n++) {
            d[n] = a.maxAmmo[n * 3 + this.player.weaponLevels[n]] - this.player.ammo[n];
            switch (n) {
                case 0:
                    this.ammoPrices[n] = 0;
                    break;
                case 1:
                    sum += d[n];
                    this.ammoPrices[n] = d[n];
                    break;
                case 2:
                    if ((this.player.ownedWeapons & 1 << n) > 0) {
                        sum += d[n];
                        this.ammoPrices[n] = d[n];
                    }
                    break;
                case 3:
                    if ((this.player.ownedWeapons & 1 << n) > 0) {
                        sum += d[n] * 3;
                        this.ammoPrices[n] = d[n] * 3;
                    }
                    break;
                case 4:
                    if ((this.player.ownedWeapons & 1 << n) > 0) {
                        sum += d[n] * 3;
                        this.ammoPrices[n] = d[n] * 3;
                    }
                    break;
                case 5:
                    if ((this.player.ownedWeapons & 1 << n) > 0) {
                        sum += d[n] * 2;
                        this.ammoPrices[n] = d[n] * 2;
                    }
                    break;
                case 6:
                    this.ammoPrices[n] = 0;
                    break;
                case 7:
                    if ((this.player.ownedWeapons & 1 << n) > 0) {
                        sum += d[n] * 5;
                        this.ammoPrices[n] = d[n] * 5;
                    }
                    break;
            }
        }
        return sum;
    }
    /** Draws s word-wrapped to the screen width; returns the y below it. */
    public final int drawWrapped(Graphics gr, String s, int x, int y, int anchor) {
        int curX = 0;
        int w = 0;
        int brk = 0;
        int maxW;
        int start;
        int i;
        int lineH;
        char ch;
        Font font;
        char[] chars;
        boolean sp;
        boolean end;
        boolean over;
        int punct;
        int cw;
        lineH = (font = gr.getFont()).getHeight();
        chars = new char[s.length()];
        s.getChars(0, s.length(), chars, 0);
        if ((anchor & 1) > 0) {
            curX = x = 0;
            maxW = screenWidth - 34;
        } else {
            curX = x;
            maxW = screenWidth - 17;
        }
        i = start = 0;
        ch = 0;
        sp = false;
        do {
            w = 0;
            end = false;
            over = false;
            punct = -1;
            brk = i;
            do {
                if (i >= chars.length) {
                    end = true;
                    break;
                }
                if ((ch = chars[i]) == '.' || ch == '/') {
                    punct = i;
                }
                if (ch == ' ') {
                    w += font.charWidth(ch);
                    i++;
                    if (curX + w > maxW && !sp) {
                        over = true;
                        break;
                    }
                    sp = true;
                    break;
                }
                cw = font.charWidth(ch);
                if (curX + w + cw > maxW && !sp) {
                    over = true;
                    break;
                }
                w += cw;
                i++;
            } while (true);
            if (curX + w > maxW || end || over) {
                if (curX + w > maxW) {
                    if (!over) {
                        i = brk;
                    } else if (punct > 0) {
                        i = punct;
                    }
                } else if (over && punct > 0) {
                    i = punct;
                }
                if (i - start > 0) {
                    if ((anchor & 1) > 0) {
                        if (i - start > 0) {
                            gr.drawChars(chars, start, i - start, screenWidth >> 1, y, anchor);
                        }
                    } else if (i - start > 0) {
                        gr.drawChars(chars, start, i - start, x, y, anchor);
                    }
                }
                y += lineH;
                curX = x;
                sp = false;
                start = i;
            } else {
                curX += w;
            }
        } while (i < chars.length);
        return y;
    }
    /** Draws s word-wrapped to maxW; returns the y below it. */
    public final int drawWrappedTo(Graphics gr, String s, int x, int y, int anchor, int maxW) {
        int curX = 0;
        int w = 0;
        int brk = 0;
        int start;
        int i;
        int lineH;
        char ch;
        Font font;
        char[] chars;
        boolean sp;
        boolean end;
        boolean over;
        int punct;
        int cw;
        lineH = (font = gr.getFont()).getHeight();
        chars = new char[s.length()];
        s.getChars(0, s.length(), chars, 0);
        if ((anchor & 1) > 0) {
            curX = x = 0;
        } else {
            curX = x;
        }
        i = start = 0;
        ch = 0;
        sp = false;
        do {
            w = 0;
            end = false;
            over = false;
            punct = -1;
            brk = i;
            do {
                if (i >= chars.length) {
                    end = true;
                    break;
                }
                if ((ch = chars[i]) == '.' || ch == '/') {
                    punct = i;
                }
                if (ch == ' ') {
                    w += font.charWidth(ch);
                    i++;
                    if (curX + w > maxW && !sp) {
                        over = true;
                        break;
                    }
                    sp = true;
                    break;
                }
                cw = font.charWidth(ch);
                if (curX + w + cw > maxW && !sp) {
                    over = true;
                    break;
                }
                w += cw;
                i++;
            } while (true);
            if (curX + w > maxW || end || over) {
                if (curX + w > maxW) {
                    if (!over) {
                        i = brk;
                    } else if (punct > 0) {
                        i = punct;
                    }
                } else if (over && punct > 0) {
                    i = punct;
                }
                if (i - start > 0) {
                    if ((anchor & 1) > 0) {
                        if (i - start > 0) {
                            gr.drawChars(chars, start, i - start, screenWidth >> 1, y, anchor);
                        }
                    } else if (i - start > 0) {
                        gr.drawChars(chars, start, i - start, x, y, anchor);
                    }
                }
                y += lineH;
                curX = x;
                sp = false;
                start = i;
            } else {
                curX += w;
            }
        } while (i < chars.length);
        return y;
    }
    /** Reads a whole text resource (empty string if missing). */
    public static final String readTextResource(String name) {
        int ch;
        char cc;
        StringBuffer sb = new StringBuffer();
        String result = "";
        try {
            InputStream in;
            if ((in = result.getClass().getResourceAsStream(name)) == null) {
                return "";
            }
            InputStreamReader reader = new InputStreamReader(in);
            while ((ch = reader.read()) != -1) {
                cc = (char) ch;
                sb.append(cc);
            }
            result = sb.toString();
            reader.close();
            in.close();
        } catch (IOException e) {
        }
        System.gc();
        return result;
    }
}
