import java.io.*;
import java.util.*;
import javax.microedition.lcdui.*;
import javax.microedition.media.*;

/*
 * f - Menu / front-end Canvas.
 *
 * Title and menu screens, scrolling text pages, the player-name entry Form,
 * soft-key labels and the attract/intro sequences.
 *
 * Naming: this is reconstructed, output-exact Java source (not recovered original source).
 * Member names are descriptive source names; config/d-preobf.map maps every one of them
 * back to its retail (obfuscated) runtime name. Renames are always group-complete (all
 * members sharing a retail name), which reproduces ProGuard's retail constant-pool
 * ordering; see cleanup/docs/NAMING_SCHEME.md. Library overrides keep their API names,
 * classes keep their retail names, and reconstruction devices keep their opus* names.
 */
public final class f extends javax.microedition.lcdui.Canvas implements java.lang.Runnable, javax.microedition.lcdui.CommandListener {
    /** Secret key sequence on the about screen: 1, 3, 1, 3 (sets g.debugKeysEnabled). */
    public static final int[] secretSequence = { 49, 51, 49, 51 };
    /** Loading progress (out of 137) drawn as the bar on the splash screen. */
    public static int loadingProgress;
    /** Language entry is shown in the settings menu. */
    public boolean languageMenuVisible = false;
    /**
     * Menu background frame layouts [variant][column 0-4][row 0-7] -> tile index in g.imgTiles[0] (-1 = empty).
     */
    public static final byte[][][] frameLayouts = { { { 0, 0, 1, 2, 2, 2, 3, 5 }, { -1, -1, -1, -1, -1, -1, 4, 5 }, { -1, -1, -1, -1, -1, -1, 4, 5 }, { -1, -1, -1, -1, -1, -1, 4, 5 }, { 6, 6, 7, 8, 8, 8, 9, 5 } }, { { 0, 0, 1, 2, 2, 2, 3, 5 }, { -1, -1, -1, -1, -1, -1, 10, 11 }, { -1, -1, -1, -1, -1, -1, 12, 13 }, { -1, -1, -1, -1, -1, -1, 14, 15 }, { 6, 6, 7, 8, 8, 8, 9, 5 } } };
    /** Menu thread (null = stop). */
    private Thread menuThread;
    /** Splash images: publisher, developer, title. */
    private Image[] splashImages;
    public static byte demoTicks;
    /** Tile width (copied from g.tileWidth). */
    public static byte tileWidth;
    /** Tile height (copied from g.tileHeight). */
    public static byte tileHeight;
    /** Screen width. */
    public static short screenWidth;
    /** Screen height. */
    public static short screenHeight;
    /** The MIDlet's Display. */
    private Display menuDisplay;
    /**
     * Current menu screen state:
     * 0 splash (publisher / developer / title images), 1 main menu, 2 settings, 4 load game,
     * 5 help pages, 6 clear-save slot list, 7 yes/no confirmation, 8 about / credits,
     * 9 exit confirmation, 10 blank, 11 new game: choose save slot, 15 multiplayer-name Form,
     * 16 skin information (game not won yet), 17 skin unlock codes, 20 language selection.
     */
    public byte menuScreen;
    /** Splash stage 0-2. */
    private byte splashStage;
    /** Time the current splash stage started (ms). */
    private long splashStartTime;
    /** Repaint request flags (bit 0 = repaint). */
    public byte repaintFlags;
    /** Y position of each menu entry (for the selection highlight). */
    public int[] menuItemY;
    /** Cursor: selected entry / help page. */
    private byte menuSelection;
    /** Save slot awaiting confirmation (-1 = none). */
    private byte confirmSlot;
    /** The pending confirmation overwrites a slot for a new game (true) or clears it (false). */
    public boolean confirmIsNewGame;
    /** Wrapped lines of the about / credits text. */
    public Vector aboutLines;
    public Vector helpLines;
    /** Wrapped lines of the skin information / unlock-code screens. */
    public Vector skinLines;
    /** Line index where the first skin code row is drawn (-1 = none). */
    public int skinCodeRow1 = -1;
    /** Line index where the second skin code row is drawn (-1 = none). */
    public int skinCodeRow2 = -1;
    /** Number of lines in the scrolling text. */
    public int lineCount;
    /** First visible line of the scrolling text. */
    public int firstVisibleLine;
    /** Number of visible lines. */
    public int visibleLines;
    private byte demoTimer;
    private byte demoTimer2;
    /** Last four keys pressed on the about screen (compared with secretSequence). */
    private int[] recentKeys = { 52, 54, 52, 54 };
    /** Owning MIDlet. */
    private ratchetandclank app;
    /** Text of the highlighted entry (redrawn inside the selection box). */
    public String highlightedText;
    /** Main menu shows "Get Ratchet Skin" (JAD property Unlock-Code, default true). */
    public boolean skinMenuEnabled = true;
    /** Multiplayer-name entry Form (state 15). */
    public Form nameForm;
    /** Multiplayer-name text field. */
    public TextField nameField;
    /** Entered multiplayer name. */
    public String playerName = "";
    /** Time the name Form was shown; commands within 1 s are ignored. */
    public long formShownTime;
    /**
     * Main menu entries as string ids: New Game, Load Game, [Get Ratchet Skin], Settings, Help, About, Exit.
     */
    public short[] mainMenuItems;
    /** Menu is shutting down: paint only clears the screen. */
    public boolean shuttingDown;
    /** Paused (canvas hidden). */
    public boolean menuPaused = false;
    /** Skip the next showNotify() (set when the canvas is shown on purpose). */
    public boolean skipNextShowNotify = false;
    /** Frames until the menu music restarts after a resume. */
    public int musicRestartFrames = 0;
    /** Resume time; key presses are ignored for 1 s after it. */
    public long menuResumeTime = 0L;
    /** Frame start time (ms). */
    public long menuFrameStart;
    /** Frame end time (ms). */
    public long menuFrameEnd = 0L;
    /** Frame duration (ms). */
    public int menuFrameMs;
    /** Sleep time to the 70 ms frame period. */
    public int frameSleepMs;
    /** Hash multipliers of the two skin unlock codes. */
    public int[] codeMultipliers = { 652482873, 766492548 };
    /** The two skin unlock codes: 8 arrow digits (0-3) each. */
    public int[][] skinCodes = new int[2][8];
    /** Positive-command soft key is active (commandAction forwards it as key -6). */
    public static int softKeyPositive = 0;
    /** Back-command soft key is active (commandAction forwards it as key -7). */
    public static int softKeyBack = 0;

    /**
     * Full-screen menu canvas: loads strings, reads the Unlock-Code property, builds the main menu and shows the publisher splash.
     */
    public f(ratchetandclank midlet) {
        this.setFullScreenMode(true);
        this.app = midlet;
        this.languageMenuVisible = true;
        this.app.reloadStrings();
        tileWidth = g.tileWidth;
        tileHeight = g.tileHeight;
        screenWidth = (short)this.getWidth();
        screenHeight = (short)this.getHeight();
        this.menuDisplay = Display.getDisplay(midlet);
        this.menuScreen = 0;
        this.splashStage = 0;
        this.repaintFlags = 3;
        this.menuItemY = new int[8];
        System.currentTimeMillis();
        this.menuSelection = 0;
        this.confirmSlot = 0;
        String s;
        if ((s = this.app.getAppProperty("Unlock-Code")) != null) {
            this.skinMenuEnabled = s.equals("true");
        }
        this.mainMenuItems = new short[this.skinMenuEnabled ? 7 : 6];
        int n = 0;
        this.mainMenuItems[n++] = 1;
        this.mainMenuItems[n++] = 2;
        if (this.skinMenuEnabled) {
            this.mainMenuItems[n++] = 286;
        }
        this.mainMenuItems[n++] = 3;
        this.mainMenuItems[n++] = 4;
        this.mainMenuItems[n++] = 5;
        this.mainMenuItems[n] = 6;
        try {
            this.splashImages = new Image[3];
            this.splashImages[0] = Image.createImage("/intro_publisher.png");
        } catch (IOException ex) {
            System.err.println("error");
        }
        this.repaint();
        this.serviceRepaints();
    }

    /** Starts the menu thread. */
    public final void startMenuThread() {
        this.menuThread = new Thread(this);
        this.menuThread.start();
    }

    /**
     * Returns to the main menu from the game: reloads menu graphics and restarts the menu thread.
     */
    public final void returnFromGame() {
        this.repaintFlags = 3;
        this.splashStage = 0;
        this.menuSelection = 0;
        if (g.imgMenuHeader == null) {
            try {
                g.imgMenuHeader = Image.createImage("/menuhd.png");
            } catch (Exception ex) {
            }
        }
        this.app.game.tilesetIndex = 3;
        try {
            g.imgTiles = null;
            System.gc();
            g.sleep(20);
            g.imgTiles = new Image[1];
            g.imgTiles[0] = Image.createImage(g.tilesetFiles[3]);
        } catch (IOException ex) {
        }
        this.menuThread = new Thread(this);
        this.menuThread.start();
        this.skipNextShowNotify = true;
        System.currentTimeMillis();
        this.showScreen((byte)1);
    }

    /** Stops the menu and releases its resources (releaseTiles: also release the shared menu tiles). */
    public final void stopMenu(boolean releaseTiles) {
        if (releaseTiles) {
            this.app.game.tilesetIndex = -1;
            g.imgTiles = null;
        }
        g.imgMenuHeader = null;
        this.aboutLines = null;
        this.helpLines = null;
        System.gc();
        g.sleep(20);
        this.shuttingDown = true;
        this.menuThread = null;
    }

    /** Pause: stops the music. */
    public final void pauseMenu() {
        this.app.stopMusic();
        this.repaintFlags |= 1;
        this.menuPaused = true;
        this.menuResumeTime = 0L;
    }

    /** Resume after a pause (music restarts a few frames later). */
    public final void resumeMenu() {
        if (this.menuPaused) {
            if (this.menuScreen > 0) {
                this.musicRestartFrames = 5;
            }
            this.repaintFlags |= 1;
            this.menuPaused = false;
            this.menuResumeTime = System.currentTimeMillis();
        }
    }

    public final void showNotify() {
        if (this.skipNextShowNotify) {
            this.skipNextShowNotify = false;
            return;
        }
        System.gc();
        g.sleep(1000);
    }

    /**
     * Menu thread: creates the game canvas on first run, then ticks and repaints at a 70 ms frame period.
     */
    public final void run() {
        if (this.app.game == null) {
            this.splashStartTime = System.currentTimeMillis();
            this.app.game = new g(this.app);
            this.repaintFlags = 3;
            System.currentTimeMillis();
        }
        while (this.menuThread != null) {
            this.menuFrameStart = System.currentTimeMillis();
            if (!this.isShown()) {
                if (!this.menuPaused) {
                    this.pauseMenu();
                }
                g.sleep(100);
                continue;
            }
            if (this.menuPaused) {
                this.resumeMenu();
            }
            if (this.musicRestartFrames > 0 && --this.musicRestartFrames == 0) {
                this.app.startMenuMusic(false);
            }
            this.tickMenu();
            if (this.menuThread != null && (this.repaintFlags & 1) > 0) {
                this.repaintFlags &= -2;
                this.repaint();
                this.serviceRepaints();
            }
            this.menuFrameEnd = System.currentTimeMillis();
            this.menuFrameMs = (int)(this.menuFrameEnd - this.menuFrameStart);
            this.frameSleepMs = 70 - this.menuFrameMs;
            if (this.frameSleepMs < 1 || this.frameSleepMs > 100) {
                this.frameSleepMs = 1;
            }
            g.sleep(this.frameSleepMs);
        }
        this.repaint();
        this.serviceRepaints();
        this.shuttingDown = false;
    }

    /**
     * Computes skin unlock code {@code code} from the multiplayer name: weighted character sum * codeMultipliers[code], split into 8 digits 0-3.
     */
    public final void computeSkinCode(String name, int code) {
        String up = name.toUpperCase();
        int hash;
        int sum = 0;
        int ch;
        int len = up.length();
        int n;
        for (n = 0; n < len; n++) {
            ch = up.charAt(n);
            sum += ch * (n + 1);
        }
        hash = sum * this.codeMultipliers[code];
        for (n = 0; n < 8; n++) {
            int sh = n * 4;
            int v;
            int mask = 15 << sh;
            v = (hash & mask) >> sh;
            this.skinCodes[code][n] = this.nibbleToArrow(v);
        }
    }

    /** Maps a 4-bit nibble to an arrow digit 0-3. */
    public final int nibbleToArrow(int nibble) {
        if (nibble >= 0 && nibble <= 3) {
            return 0;
        }
        if (nibble >= 4 && nibble <= 7) {
            return 1;
        }
        if (nibble >= 8 && nibble <= 11) {
            return 2;
        }
        return 3;
    }

    /** Moves all elements of source to the end of target. */
    public static final void appendAll(Vector target, Vector source) {
        while (source.size() > 0) {
            Object o = source.firstElement();
            target.addElement(o);
            source.removeElement(o);
        }
    }

    /** Moves all elements of source to target as list items e with id {@code id}. */
    public static final void appendItems(Vector target, Vector source, int id) {
        int n = 0;
        int size = source.size();
        while (source.size() > 0) {
            Object o = source.firstElement();
            target.addElement(new e(o, id, size, n++));
            source.removeElement(o);
        }
    }

    /** Appends a one-line list item e with id {@code id}. */
    public static final void appendItem(Vector target, String text, int id) {
        target.addElement(new e(text, id, 1, 0));
    }

    /** Switches to menu screen {@code screen} and prepares it (demo actors, text pages, Form, etc.). */
    public final void showScreen(byte screen) {
        this.repaintFlags = 3;
        if (screen == 1) {
            this.app.game.player.facingRight = true;
            demoTicks = 90;
            this.app.game.player.weapon = 1;
            this.app.game.player.setAnimation((byte)1);
            this.app.game.player.animMode = 0;
            this.app.game.player.tileRow = 6;
            this.app.game.player.posX = -30720;
            this.app.game.player.speedY = 0;
            this.app.game.player.speedX = 1536;
            this.app.game.player.blinkTimer = 0;
            this.app.game.player.rowOffset = -2048;
            this.app.game.enemies[0].actorKind = 1;
            this.app.game.enemies[0].variant = 0;
            this.app.game.enemies[0].facingRight = true;
            this.app.game.enemies[0].altAim = false;
            this.app.game.enemies[0].setAnimation((byte)1);
            this.app.game.enemies[0].animMode = 0;
            this.app.game.enemies[0].tileRow = 6;
            this.app.game.enemies[0].posX = -10752;
            this.app.game.enemies[0].speedY = 0;
            this.app.game.enemies[0].speedX = 1536;
            this.app.game.enemies[0].health = 1;
            this.app.game.enemies[0].drawLayer = 1;
            this.app.game.enemies[0].orientation = 0;
            this.app.game.enemies[0].rowOffset = -2048;
            this.app.game.enemies[1].actorKind = 1;
            this.app.game.enemies[1].variant = 0;
            this.app.game.enemies[1].facingRight = false;
            this.app.game.enemies[1].altAim = false;
            this.app.game.enemies[1].setAnimation((byte)1);
            this.app.game.enemies[1].animMode = 0;
            this.app.game.enemies[1].tileRow = 6;
            this.app.game.enemies[1].posX = 9 * tileWidth << 8;
            this.app.game.enemies[1].speedY = 0;
            this.app.game.enemies[1].speedX = 0;
            this.app.game.enemies[1].health = 1;
            this.app.game.enemies[1].drawLayer = 1;
            this.app.game.enemies[1].orientation = 0;
            this.app.game.enemies[1].rowOffset = -2048;
            this.app.game.enemies[2].actorKind = 1;
            this.app.game.enemies[2].variant = 0;
            this.app.game.enemies[2].facingRight = false;
            this.app.game.enemies[2].altAim = false;
            this.app.game.enemies[2].setAnimation((byte)1);
            this.app.game.enemies[2].animMode = 0;
            this.app.game.enemies[2].tileRow = 6;
            this.app.game.enemies[2].posX = 10 * tileWidth << 8;
            this.app.game.enemies[2].speedY = 0;
            this.app.game.enemies[2].speedX = 0;
            this.app.game.enemies[2].health = 1;
            this.app.game.enemies[2].drawLayer = 1;
            this.app.game.enemies[2].orientation = 0;
            this.app.game.enemies[2].rowOffset = -2048;
            this.app.game.enemies[3].actorKind = 1;
            this.app.game.enemies[3].variant = 0;
            this.app.game.enemies[3].facingRight = false;
            this.app.game.enemies[3].altAim = false;
            this.app.game.enemies[3].setAnimation((byte)1);
            this.app.game.enemies[3].animMode = 0;
            this.app.game.enemies[3].tileRow = 6;
            this.app.game.enemies[3].posX = 11 * tileWidth << 8;
            this.app.game.enemies[3].speedY = 0;
            this.app.game.enemies[3].speedX = 0;
            this.app.game.enemies[3].health = 1;
            this.app.game.enemies[3].drawLayer = 1;
            this.app.game.enemies[3].orientation = 0;
            this.app.game.enemies[3].rowOffset = -2048;
            this.musicRestartFrames = 3;
            this.lineCount = this.mainMenuItems.length;
            this.firstVisibleLine = 0;
        }
        if (screen == 5) {
            for (int n = 0; n < g.enemySlotCount; n++) {
                this.app.game.enemies[n].actorKind = -1;
            }
            this.app.game.player.facingRight = true;
            this.app.game.player.weapon = 0;
            this.app.game.player.setAnimation((byte)8);
            this.app.game.player.animMode = 1;
            this.app.game.player.drawLayer = 1;
            this.app.game.player.posX = screenWidth >> 1 << 8;
            this.app.game.player.speedY = 0;
            this.app.game.player.speedX = 0;
            this.app.game.player.blinkTimer = 0;
            this.app.game.player.tileRow = 2;
            this.app.game.player.rowOffset = -1280;
            this.app.game.player.ownedWeapons = -1;
            this.demoTimer = 0;
        }
        if (screen == 15) {
            this.formShownTime = 0L;
            this.nameForm = new Form("");
            this.nameForm.addCommand(new Command(ratchetandclank.strings[39], 4, 1));
            this.nameForm.addCommand(new Command(ratchetandclank.strings[8], 3, 1));
            this.nameForm.setCommandListener(this);
            this.nameField = new TextField(ratchetandclank.strings[289], this.playerName, 15, 0);
            this.nameForm.append(this.nameField);
            this.menuDisplay.setCurrent(this.nameForm);
            this.formShownTime = System.currentTimeMillis();
        }
        if (screen == 16) {
            int tw = screenWidth - 34;
            String blank = new String("");
            this.skinLines = new Vector();
            appendAll(this.skinLines, ratchetandclank.wrapText(ratchetandclank.strings[287], tw));
            this.skinLines.addElement(blank);
            appendAll(this.skinLines, ratchetandclank.wrapText(ratchetandclank.strings[288], tw));
            this.lineCount = this.skinLines.size();
            this.firstVisibleLine = 0;
        }
        if (screen == 17) {
            this.computeSkinCode(this.playerName, 0);
            this.computeSkinCode(this.playerName, 1);
            if (g.imgArrows == null) {
                try {
                    g.imgArrows = Image.createImage("/arrows.png");
                } catch (Exception ex) {
                }
            }
            int tw = screenWidth - 34;
            String blank = new String("");
            this.skinLines = new Vector();
            appendAll(this.skinLines, ratchetandclank.wrapText(ratchetandclank.strings[290], tw));
            appendAll(this.skinLines, ratchetandclank.wrapText(this.playerName, tw));
            this.skinLines.addElement(blank);
            appendAll(this.skinLines, ratchetandclank.wrapText(ratchetandclank.strings[313], tw));
            this.skinCodeRow1 = this.skinLines.size();
            this.skinLines.addElement(blank);
            this.skinLines.addElement(blank);
            appendAll(this.skinLines, ratchetandclank.wrapText(ratchetandclank.strings[312], tw));
            this.skinCodeRow2 = this.skinLines.size();
            this.skinLines.addElement(blank);
            this.skinLines.addElement(blank);
            appendAll(this.skinLines, ratchetandclank.wrapText(ratchetandclank.strings[288], tw));
            this.lineCount = this.skinLines.size();
            this.firstVisibleLine = 0;
        } else {
            g.imgArrows = null;
        }
        this.helpLines = null;
        if (screen == 8) {
            if (this.aboutLines == null) {
                this.aboutLines = new Vector();
                int tw = screenWidth - 34;
                String blank = new String("");
                appendAll(this.aboutLines, ratchetandclank.wrapText(ratchetandclank.strings[284], tw));
                this.aboutLines.addElement(blank);
                appendAll(this.aboutLines, ratchetandclank.wrapText(ratchetandclank.strings[86], tw));
                appendAll(this.aboutLines, ratchetandclank.wrapText(ratchetandclank.strings[87], tw));
                this.aboutLines.addElement(blank);
                appendAll(this.aboutLines, ratchetandclank.wrapText(ratchetandclank.strings[88], tw));
                this.aboutLines.addElement(blank);
                appendAll(this.aboutLines, ratchetandclank.wrapText(ratchetandclank.strings[89], tw));
                appendAll(this.aboutLines, ratchetandclank.wrapText(ratchetandclank.strings[90], tw));
                this.aboutLines.addElement(blank);
                appendAll(this.aboutLines, ratchetandclank.wrapText(ratchetandclank.strings[91], tw));
                appendAll(this.aboutLines, ratchetandclank.wrapText(ratchetandclank.strings[92], tw));
                this.aboutLines.addElement(blank);
                appendAll(this.aboutLines, ratchetandclank.wrapText(ratchetandclank.strings[93], tw));
                appendAll(this.aboutLines, ratchetandclank.wrapText(ratchetandclank.strings[94], tw));
                this.aboutLines.addElement(blank);
                String s4 = ratchetandclank.strings[95];
                if (g.smallFont.stringWidth(s4) <= tw) {
                    int k5;
                    if ((k5 = s4.indexOf(45)) > 0) {
                        appendAll(this.aboutLines, ratchetandclank.wrapText(s4.substring(0, k5) + s4.substring(k5 + 1), tw));
                    } else {
                        appendAll(this.aboutLines, ratchetandclank.wrapText(s4, tw));
                    }
                } else {
                    appendAll(this.aboutLines, ratchetandclank.wrapText(s4, tw));
                }
                appendAll(this.aboutLines, ratchetandclank.wrapText(ratchetandclank.strings[96], tw));
                this.aboutLines.addElement(blank);
                appendAll(this.aboutLines, ratchetandclank.wrapText(ratchetandclank.strings[97], tw));
                appendAll(this.aboutLines, ratchetandclank.wrapText(ratchetandclank.strings[98], tw));
                this.aboutLines.addElement(blank);
                appendAll(this.aboutLines, ratchetandclank.wrapText(ratchetandclank.strings[99], tw));
                appendAll(this.aboutLines, ratchetandclank.wrapText(ratchetandclank.strings[100], tw));
                this.aboutLines.addElement(blank);
                String s5 = ratchetandclank.strings[101];
                if (g.smallFont.stringWidth(s5) <= tw) {
                    int k6;
                    if ((k6 = s5.indexOf(45)) > 0) {
                        appendAll(this.aboutLines, ratchetandclank.wrapText(s5.substring(0, k6) + s5.substring(k6 + 1), tw));
                    } else {
                        appendAll(this.aboutLines, ratchetandclank.wrapText(s5, tw));
                    }
                } else {
                    appendAll(this.aboutLines, ratchetandclank.wrapText(s5, tw));
                }
                appendAll(this.aboutLines, ratchetandclank.wrapText(ratchetandclank.strings[102], tw));
                appendAll(this.aboutLines, ratchetandclank.wrapText(ratchetandclank.strings[103], tw));
                this.aboutLines.addElement(blank);
                appendAll(this.aboutLines, ratchetandclank.wrapText(ratchetandclank.strings[84], tw));
                String s6 = ratchetandclank.strings[85];
                int k7;
                String v8;
                if ((k7 = s6.indexOf("??")) > 0 && (v8 = this.app.getAppProperty("MIDlet-Version")) != null) {
                    s6 = s6.substring(0, k7) + v8;
                }
                appendAll(this.aboutLines, ratchetandclank.wrapText(s6, tw));
            }
            this.lineCount = this.aboutLines.size();
            this.firstVisibleLine = 0;
        }
        this.menuScreen = screen;
        this.menuSelection = 0;
        if (screen == 20) {
            if (ratchetandclank.language == -1) {
                this.menuSelection = 0;
            } else {
                this.menuSelection = (byte)ratchetandclank.language;
            }
        }
        if (screen == 4 || screen == 6 || screen == 11) {
            this.app.readSlotSummaries();
        }
    }

    /**
     * Name Form commands: OK validates the name (4-15 characters) and shows the codes; Back returns to the menu.
     */
    public final void nameFormCommand(Command command) {
        if (this.formShownTime == 0L) {
            return;
        }
        if (System.currentTimeMillis() - this.formShownTime < 1000L) {
            return;
        }
        if (command.getCommandType() == 4) {
            String s;
            int len;
            if ((len = (s = this.nameField.getString().trim()).length()) < 4) {
                this.showError(ratchetandclank.strings[292], ratchetandclank.strings[293], 1500);
                return;
            }
            if (len >= 16) {
                this.showError(ratchetandclank.strings[292], ratchetandclank.strings[294], 1500);
                return;
            }
            this.playerName = s;
            this.showScreen((byte)17);
        } else {
            this.showScreen((byte)1);
        }
        this.nameForm = null;
        this.nameField = null;
        System.gc();
        this.skipNextShowNotify = true;
        this.menuDisplay.setCurrent(this);
    }

    /** Shows an error Alert for timeout ms. */
    public final void showError(String title, String message, int timeout) {
        Alert al = new Alert(title, message, null, AlertType.ERROR);
        al.setTimeout(timeout);
        this.menuDisplay.setCurrent(al);
    }

    /** Draws the tiled menu background frame (layout variant {@code variant}). */
    public final void drawBackground(Graphics graphics, byte variant) {
        graphics.setClip(0, 0, this.getWidth(), this.getHeight());
        graphics.setColor(0);
        graphics.fillRect(0, 0, screenWidth, screenHeight);
        int col;
        int row = 0;
        int x;
        int y;
        for (row = 0, y = 0; row < 8; row++, y += tileHeight) {
            for (col = 0, x = screenWidth - tileWidth * 5 >> 1; col < 5; col++, x += tileWidth) {
                graphics.setClip(x, y, tileWidth, tileHeight);
                if (frameLayouts[variant][col][row] >= 0) {
                    graphics.drawImage(g.imgTiles[0], x, y - frameLayouts[variant][col][row] * tileHeight, 0);
                }
            }
        }
        graphics.setClip(0, 0, this.getWidth(), this.getHeight());
    }

    /**
     * Maps keys 2/4/5/6/8 and game actions, then dispatches to the handler of the current screen.
     */
    public final synchronized void keyPressed(int keyCode) {
        if (this.menuThread == null) {
            return;
        }
        if (this.menuResumeTime > 0L && System.currentTimeMillis() - this.menuResumeTime < 1000L) {
            return;
        }
        int act = 0;
        if (keyCode == 50) {
            act = 1;
        } else if (keyCode == 56) {
            act = 6;
        } else if (keyCode == 52) {
            act = 2;
        } else if (keyCode == 54) {
            act = 5;
        } else if (keyCode == 53) {
            act = 8;
        } else if (keyCode != -6 && keyCode != -7) {
            try {
                act = this.getGameAction(keyCode);
            } catch (Exception ex) {
                return;
            }
        }
        switch (this.menuScreen) {
            case 0:
                if (this.app.game != null) {
                    this.splashStartTime = 0L;
                    this.repaintFlags |= 1;
                }
                break;
            case 1:
                this.mainMenuKeys(keyCode, act);
                break;
            case 2:
                this.settingsKeys(keyCode, act);
                break;
            case 4:
                this.loadGameKeys(keyCode, act);
                break;
            case 8:
                this.aboutKeys(keyCode, act);
                break;
            case 15:
                break;
            case 16:
                this.skinInfoKeys(keyCode, act);
                break;
            case 17:
                this.skinCodeKeys(keyCode, act);
                break;
            case 5:
                this.helpKeys(keyCode, act);
                break;
            case 6:
                this.clearSaveKeys(keyCode, act);
                break;
            case 9:
                this.exitKeys(keyCode, act);
                break;
            case 7:
                this.confirmKeys(keyCode, act);
                break;
            case 11:
                this.newGameSlotKeys(keyCode, act);
                break;
            case 20:
                this.languageKeys(keyCode, act);
                break;
        }
    }

    /** Main menu keys. */
    private void mainMenuKeys(int key, int action) {
        if (action == 6) {
            if (this.visibleLines < this.mainMenuItems.length) {
                if (this.menuSelection < this.mainMenuItems.length - 1) {
                    this.menuSelection++;
                    if (this.firstVisibleLine + this.visibleLines <= this.menuSelection) {
                        this.firstVisibleLine++;
                    }
                }
            } else {
                this.menuSelection = this.cursorDown(this.menuSelection, (byte)(this.mainMenuItems.length - 1), (byte)0);
            }
            this.repaintFlags |= 3;
            return;
        }
        if (action == 1) {
            if (this.visibleLines < this.mainMenuItems.length) {
                if (this.menuSelection > 0) {
                    this.menuSelection--;
                    if (this.firstVisibleLine > 0) {
                        this.firstVisibleLine--;
                    }
                }
            } else {
                this.menuSelection = this.cursorUp(this.menuSelection, (byte)0, (byte)(this.mainMenuItems.length - 1));
            }
            this.repaintFlags |= 3;
            return;
        }
        if (key == 0) {
            this.repaintFlags |= 3;
            this.showScreen((byte)9);
            return;
        }
        if (key == 53 || action == 8 || key == -6) {
            if (this.menuSelection == 0) {
                this.showScreen((byte)11);
                return;
            }
            if (this.menuSelection == 1) {
                this.showScreen((byte)4);
                return;
            }
            if (this.menuSelection == 2 && this.skinMenuEnabled) {
                if (this.app.isGameWon()) {
                    this.showScreen((byte)15);
                    return;
                }
                this.showScreen((byte)16);
                return;
            }
            if (this.menuSelection == 2 && !this.skinMenuEnabled || this.menuSelection == 3 && this.skinMenuEnabled) {
                this.showScreen((byte)2);
                return;
            }
            if (this.menuSelection == 3 && !this.skinMenuEnabled || this.menuSelection == 4 && this.skinMenuEnabled) {
                this.showScreen((byte)5);
                return;
            }
            if (this.menuSelection == 4 && !this.skinMenuEnabled || this.menuSelection == 5 && this.skinMenuEnabled) {
                for (int n = 0; n < 4; n++) {
                    this.recentKeys[n] = 52;
                }
                this.showScreen((byte)8);
                return;
            }
            if (this.menuSelection == 5 && !this.skinMenuEnabled || this.menuSelection == 6 && this.skinMenuEnabled) {
                this.showScreen((byte)9);
            }
        }
    }

    /** Load game keys: select a used slot and continue it. */
    private void loadGameKeys(int key, int action) {
        if (action == 1 || key == 50) {
            this.menuSelection = this.cursorUp(this.menuSelection, (byte)0, (byte)2);
            this.repaintFlags |= 3;
            return;
        }
        if (action == 6 || key == 56) {
            this.menuSelection = this.cursorDown(this.menuSelection, (byte)2, (byte)0);
            this.repaintFlags |= 3;
            return;
        }
        if (key == 53 || action == 8 || key == -6) {
            if (this.app.slotSummaries[this.menuSelection] == 0) {
                return;
            }
            this.app.continueGame(this.menuSelection);
            return;
        }
        if (key == -7 || key == 0) {
            this.showScreen((byte)1);
        }
    }

    /** Settings keys: sound on/off, clear save, language. */
    private void settingsKeys(int key, int action) {
        if (key == -7 || key == 0) {
            this.showScreen((byte)1);
            this.app.storeSoundSetting((byte)(this.app.soundEnabled ? 1 : 0));
            return;
        }
        if (action == 1 || key == 50) {
            this.menuSelection = this.cursorUp(this.menuSelection, (byte)0, (byte)(this.languageMenuVisible ? 2 : 1));
            this.repaintFlags |= 3;
            return;
        }
        if (action == 6 || key == 56) {
            this.menuSelection = this.cursorDown(this.menuSelection, (byte)(this.languageMenuVisible ? 2 : 1), (byte)0);
            this.repaintFlags |= 3;
            return;
        }
        if (key == 53 || action == 8 || key == -6) {
            if (this.menuSelection == 0) {
                if (this.app.soundEnabled == true) {
                    this.app.stopMusic();
                    this.app.soundEnabled = false;
                } else {
                    this.app.soundEnabled = true;
                    this.app.startMenuMusic(false);
                }
                this.repaintFlags |= 3;
                return;
            }
            if (this.menuSelection == 1) {
                this.showScreen((byte)6);
                return;
            }
            if (this.menuSelection == 2 && this.languageMenuVisible) {
                this.showScreen((byte)20);
            }
        }
    }

    /** Language selection keys (reloads the string table). */
    private void languageKeys(int key, int action) {
        if (key == -7 || key == 0) {
            if (ratchetandclank.language != -1) {
                this.showScreen((byte)2);
            }
        } else if (action == 1 || key == 50) {
            this.menuSelection = this.cursorUp(this.menuSelection, (byte)0, (byte)4);
            this.repaintFlags |= 3;
        } else if (action == 6 || key == 56) {
            this.menuSelection = this.cursorDown(this.menuSelection, (byte)4, (byte)0);
            this.repaintFlags |= 3;
        } else if (key == 53 || action == 8 || key == -6) {
            if (this.menuSelection != ratchetandclank.language) {
                boolean first = ratchetandclank.language == -1;
                ratchetandclank.language = this.menuSelection;
                this.app.storeSoundSetting((byte)(this.app.soundEnabled ? 1 : 0));
                this.aboutLines = null;
                this.helpLines = null;
                this.skinLines = null;
                this.app.game.arenaItems = null;
                this.app.game.arenaDescription = null;
                this.app.game.storeItems = null;
                this.app.game.buyItems = null;
                this.app.game.summaryLines = null;
                System.gc();
                g.sleep(20);
                this.app.reloadStrings();
                this.repaintFlags |= 3;
                if (first) {
                    this.showScreen((byte)1);
                }
            }
        }
    }

    /** About screen keys: scrolling, key 0 cycles g.debugOverlay, and the secret sequence check. */
    private void aboutKeys(int key, int action) {
        if (action == 6) {
            if (this.firstVisibleLine + this.visibleLines < this.lineCount) {
                this.firstVisibleLine++;
                this.repaintFlags |= 3;
            }
            this.app.game.debugOverlay = 0;
        } else if (action == 1) {
            if (this.firstVisibleLine > 0) {
                this.firstVisibleLine--;
                this.repaintFlags |= 3;
            }
            this.app.game.debugOverlay = 0;
        } else if (key == -7 || key == 0) {
            this.showScreen((byte)1);
        } else if (key == 48) {
            if (this.app.game.debugOverlay == 2) {
                this.app.game.debugOverlay = 0;
            } else {
                this.app.game.debugOverlay++;
            }
        }
        if (!this.app.game.debugKeysEnabled) {
            this.pushKey(key);
            this.app.game.debugKeysEnabled = this.secretSequenceEntered();
        }
    }

    /** Skin unlock-code screen keys. */
    public final void skinCodeKeys(int key, int action) {
        if (key == -7 || key == 0) {
            this.skinLines = null;
            this.skinCodeRow1 = this.skinCodeRow2 = -1;
            this.showScreen((byte)15);
        } else if (key == -6) {
            this.skinLines = null;
            this.skinCodeRow1 = this.skinCodeRow2 = -1;
            this.showScreen((byte)1);
        }
        if (action == 6) {
            if (this.firstVisibleLine + this.visibleLines < this.lineCount) {
                this.firstVisibleLine++;
                this.repaintFlags |= 3;
            }
        } else if (action == 1 && this.firstVisibleLine > 0) {
            this.firstVisibleLine--;
            this.repaintFlags |= 3;
        }
    }

    /** Skin information screen keys. */
    public final void skinInfoKeys(int key, int action) {
        if (key == -7 || key == 0) {
            this.skinLines = null;
            this.showScreen((byte)1);
        }
        if (action == 6) {
            if (this.firstVisibleLine + this.visibleLines < this.lineCount) {
                this.firstVisibleLine++;
                this.repaintFlags |= 3;
            }
        } else if (action == 1 && this.firstVisibleLine > 0) {
            this.firstVisibleLine--;
            this.repaintFlags |= 3;
        }
    }

    /** Help keys: page through the help pages (weapon pages drive the demo player). */
    private void helpKeys(int key, int action) {
        if (key == 52 || action == 2 || key == -7) {
            if (this.menuSelection > 0) {
                this.menuSelection--;
                if (this.menuSelection < 3) {
                    this.menuSelection = 0;
                }
                this.helpLines = null;
            if (this.menuSelection > 3 && this.menuSelection < 11) {
                this.app.game.player.weapon--;
            } else if (this.menuSelection == 3) {
                this.app.game.player.weapon = 0;
                this.app.game.player.setAnimation((byte)8);
                this.app.game.player.animMode = 1;
            } else if (this.menuSelection == 11) {
                this.app.game.player.weapon = 8;
            }
            for (int n = 9; n >= 0; n--) {
                this.app.game.playerShots[n].release();
            }
            this.demoTimer2 = 0;
            this.demoTimer = 0;
            this.repaintFlags |= 3;
            } else {
                this.showScreen((byte)1);
            }
        } else if (key == 54 || action == 5 || key == -6) {
            if (this.menuSelection < 13) {
                this.menuSelection++;
                if (this.menuSelection < 3) {
                    this.menuSelection = 3;
                }
                this.helpLines = null;
            if (this.menuSelection > 3 && this.menuSelection < 11) {
                this.app.game.player.weapon++;
            } else if (this.menuSelection == 3) {
                this.app.game.player.weapon = 0;
                this.app.game.player.setAnimation((byte)8);
                this.app.game.player.animMode = 1;
            } else if (this.menuSelection == 11) {
                this.app.game.player.weapon = 8;
            }
            for (int n = 9; n >= 0; n--) {
                this.app.game.playerShots[n].release();
            }
            this.demoTimer2 = 0;
            this.demoTimer = 0;
            this.repaintFlags |= 3;
            } else {
                this.showScreen((byte)1);
            }
        } else if (key == 53 || action == 8) {
            this.showScreen((byte)1);
        }
        if (action == 6) {
            if (this.firstVisibleLine + this.visibleLines < this.lineCount) {
                this.firstVisibleLine++;
                this.repaintFlags |= 3;
            }
        } else if (action == 1 && this.firstVisibleLine > 0) {
            this.firstVisibleLine--;
            this.repaintFlags |= 3;
        }
    }

    /** Exit confirmation keys (Yes ends the MIDlet). */
    private void exitKeys(int key, int action) {
        if (key == -7 || key == 0) {
            this.showScreen((byte)1);
            return;
        }
        if (action == 6 || key == 56) {
            this.menuSelection = this.cursorUp(this.menuSelection, (byte)0, (byte)1);
            this.repaintFlags |= 3;
            return;
        }
        if (action == 1 || key == 50) {
            this.menuSelection = this.cursorDown(this.menuSelection, (byte)1, (byte)0);
            this.repaintFlags |= 3;
            return;
        }
        if (key == 53 || action == 8 || key == -6) {
            if (this.menuSelection == 0) {
                this.showScreen((byte)1);
                return;
            }
            if (this.menuSelection == 1) {
                this.app.stopMusic();
                this.app.notifyDestroyed();
            }
        }
    }

    /** Yes/No confirmation keys: overwrite slot for a new game, or clear the slot. */
    private void confirmKeys(int key, int action) {
        if (key == -7 || key == 0) {
            if (this.confirmIsNewGame) {
                this.showScreen((byte)11);
                return;
            }
            this.showScreen((byte)6);
            return;
        }
        if (action == 1 || key == 50) {
            this.menuSelection = this.cursorUp(this.menuSelection, (byte)0, (byte)1);
            this.repaintFlags |= 3;
            return;
        }
        if (action == 6 || key == 56) {
            this.menuSelection = this.cursorDown(this.menuSelection, (byte)1, (byte)0);
            this.repaintFlags |= 3;
            return;
        }
        if (key == 53 || action == 8 || key == -6) {
            if (this.menuSelection == 0) {
                if (this.confirmIsNewGame) {
                    this.showScreen((byte)11);
                    return;
                }
                this.showScreen((byte)6);
                return;
            }
            if (this.menuSelection == 1) {
                if (this.confirmIsNewGame) {
                    this.app.game.saveSlot = this.confirmSlot;
                    this.confirmSlot = -1;
                    this.app.newGame(0);
                    return;
                }
                this.app.clearSlot(this.confirmSlot);
                this.confirmSlot = -1;
                this.app.readSlotSummaries();
                this.repaintFlags |= 3;
                this.showScreen((byte)6);
            }
        }
    }

    /** Clear-save slot list keys. */
    private void clearSaveKeys(int key, int action) {
        if (key == -7 || key == 0) {
            this.showScreen((byte)2);
            return;
        }
        if (action == 1 || key == 50) {
            this.menuSelection = this.cursorUp(this.menuSelection, (byte)0, (byte)2);
            this.repaintFlags |= 3;
            return;
        }
        if (action == 6 || key == 56) {
            this.menuSelection = this.cursorDown(this.menuSelection, (byte)2, (byte)0);
            this.repaintFlags |= 3;
            return;
        }
        if (key == 53 || action == 8 || key == -6) {
            if (this.app.slotSummaries[this.menuSelection] == 0) {
                return;
            }
            this.confirmSlot = this.menuSelection;
            this.confirmIsNewGame = false;
            this.showScreen((byte)7);
        }
    }

    /** New game slot list keys (a used slot asks for confirmation). */
    private void newGameSlotKeys(int key, int action) {
        if (key == -7 || key == 0) {
            this.showScreen((byte)1);
            return;
        }
        if (action == 1 || key == 50) {
            this.menuSelection = this.cursorUp(this.menuSelection, (byte)0, (byte)2);
            this.repaintFlags |= 3;
            return;
        }
        if (action == 6 || key == 56) {
            this.menuSelection = this.cursorDown(this.menuSelection, (byte)2, (byte)0);
            this.repaintFlags |= 3;
            return;
        }
        if (key == 53 || action == 8 || key == -6) {
            if (this.app.slotSummaries[this.menuSelection] == 0) {
                this.app.game.saveSlot = this.menuSelection;
                this.app.newGame(0);
                return;
            }
            this.confirmSlot = this.menuSelection;
            this.confirmIsNewGame = true;
            this.showScreen((byte)7);
        }
    }

    /** Paints the current menu screen. */
    public final synchronized void paint(Graphics graphics) {
        if (this.menuScreen != 0) {
            graphics.setClip(0, 0, g.screenWidth, g.screenHeight);
        } else {
            graphics.setClip(0, 0, this.getWidth(), this.getHeight());
        }
        if (this.shuttingDown) {
            graphics.setColor(0);
            graphics.fillRect(0, 0, g.screenWidth, g.screenHeight);
            return;
        }
        switch (this.menuScreen) {
            case 0:
                this.drawSplash(graphics);
                break;
            case 1:
                this.drawMainMenu(graphics);
                break;
            case 2:
                this.drawSettings(graphics);
                break;
            case 4:
                this.drawLoadGame(graphics);
                break;
            case 5:
                this.drawHelpPage(graphics, this.menuSelection);
                break;
            case 8:
                this.drawAbout(graphics, this.menuSelection);
                break;
            case 15:
                break;
            case 16:
                this.drawSkinInfo(graphics);
                break;
            case 17:
                this.drawSkinCodes(graphics);
                break;
            case 7:
                this.drawConfirm(graphics);
                break;
            case 6:
                this.drawClearSaveList(graphics);
                break;
            case 9:
                this.drawExitQuestion(graphics);
                break;
            case 10:
                graphics.setClip(0, 0, screenWidth, screenHeight);
                graphics.setColor(0);
                graphics.fillRect(0, 0, screenWidth, screenHeight);
                break;
            case 11:
                this.drawNewGameSlots(graphics);
                break;
            case 20:
                this.drawLanguageSelect(graphics);
                break;
        }
    }

    /** Per-frame menu logic (only the splash sequence needs ticking). */
    private void tickMenu() {
        switch (this.menuScreen) {
            case 0:
                this.splashSequence();
                break;
            case 1:
                if (demoTicks > 100) {
                    this.app.game.player.animate();
                    this.app.game.enemies[0].animate();
                    this.app.game.enemies[1].animate();
                    this.app.game.enemies[2].animate();
                    this.app.game.enemies[3].animate();
                    this.repaintFlags |= 1;
                    this.app.game.player.posX += this.app.game.player.speedX;
                    this.app.game.enemies[0].posX += this.app.game.enemies[0].speedX;
                    this.app.game.enemies[1].posX += this.app.game.enemies[1].speedX;
                    this.app.game.enemies[2].posX += this.app.game.enemies[2].speedX;
                    this.app.game.enemies[3].posX += this.app.game.enemies[3].speedX;
                    if ((this.app.game.player.posX >> 8) > 7 * tileWidth && this.app.game.player.facingRight) {
                        this.app.game.player.facingRight = false;
                        this.app.game.player.speedX *= -1;
                        this.app.game.enemies[0].facingRight = false;
                        this.app.game.enemies[0].speedX *= -1;
                        this.app.game.enemies[0].posX = 12 * tileWidth << 8;
                        this.app.game.enemies[1].speedX = -1536;
                        this.app.game.enemies[2].speedX = -1536;
                        this.app.game.enemies[3].speedX = -1536;
                    } else if ((this.app.game.enemies[0].posX >> 8) < 0 && !this.app.game.enemies[0].facingRight) {
                        demoTicks = 0;
                        this.app.game.player.facingRight = true;
                        this.app.game.player.speedX *= -1;
                        this.app.game.player.posX = -30720;
                        this.app.game.enemies[0].speedX *= -1;
                        this.app.game.enemies[0].facingRight = true;
                        this.app.game.enemies[0].posX = -10752;
                        this.app.game.enemies[1].posX = 9 * tileWidth << 8;
                        this.app.game.enemies[1].speedX = 0;
                        this.app.game.enemies[2].posX = 10 * tileWidth << 8;
                        this.app.game.enemies[2].speedX = 0;
                        this.app.game.enemies[3].posX = 11 * tileWidth << 8;
                        this.app.game.enemies[3].speedX = 0;
                    }
                } else {
                    demoTicks++;
                }
                break;
            case 5:
                this.repaintFlags |= 1;
                this.app.game.player.animate();
                if (this.menuSelection == 3) {
                    if (this.demoTimer > 70) {
                        this.app.game.player.setAnimation((byte)0);
                        this.app.game.player.animMode = 0;
                        this.demoTimer = 0;
                    } else if (this.demoTimer == 1 || this.demoTimer == 10 || this.demoTimer == 20) {
                        this.app.game.player.setAnimation((byte)8);
                        this.app.game.player.animMode = 1;
                    }
                    this.demoTimer++;
                    break;
                }
                if (this.demoTimer2 < 3) {
                    if (this.menuSelection > 3 && this.menuSelection < 11 && this.demoTimer > 10) {
                        this.app.game.player.fireWeapon();
                        this.demoTimer = 0;
                        this.demoTimer2++;
                        for (int n = 0; n < 8; n++) {
                            if (this.app.game.player.ammo[n] == 0) {
                                this.app.game.player.ammo[n] = 30;
                            }
                        }
                    }
                } else if (this.demoTimer > 50) {
                    this.demoTimer2 = 0;
                    this.demoTimer = 0;
                } else if (this.demoTimer == 1) {
                    this.app.game.player.setAnimation((byte)0);
                    this.app.game.player.animMode = 0;
                }
                this.demoTimer++;
                for (int n = 9; n >= 0; n--) {
                    this.moveDemoShot(this.app.game.playerShots[n]);
                }
                break;
        }
    }

    /**
     * Splash sequence: three 2-second images, then the main menu (or language selection on first start).
     */
    private void splashSequence() {
        if (this.splashStartTime == 0L || this.splashStartTime + 2000L < System.currentTimeMillis()) {
            if (this.splashStage == 0) {
                this.splashStage = 1;
                this.repaintFlags |= 1;
                this.splashStartTime = System.currentTimeMillis();
                return;
            }
            if (this.splashStage == 1) {
                this.splashStage = 2;
                this.repaintFlags |= 1;
                this.splashStartTime = System.currentTimeMillis();
                return;
            }
            if (this.splashStage == 2) {
                this.repaintFlags |= 1;
                this.splashStartTime = System.currentTimeMillis();
                this.splashImages[2] = null;
                this.splashImages[1] = null;
                this.splashImages[0] = null;
                System.gc();
                Thread.yield();
                if (g.imgTiles == null) {
                    try {
                        this.app.game.tilesetIndex = 3;
                        g.imgTiles = new Image[1];
                        g.imgTiles[0] = Image.createImage(g.tilesetFiles[3]);
                    } catch (IOException ex) {
                    }
                }
                if (ratchetandclank.language >= 0) {
                    this.showScreen((byte)1);
                    return;
                }
                this.showScreen((byte)20);
            }
        } else if (this.splashImages[this.splashStage] == null) {
            switch (this.splashStage) {
                case 0:
                    try {
                        if (this.splashImages[1] == null) {
                            this.splashImages[1] = Image.createImage("/intro_handheld.png");
                        }
                    } catch (Exception ex) {
                    }
                    break;
                case 1:
                    try {
                        if (this.splashImages[2] == null) {
                            this.splashImages[2] = Image.createImage("/intro_ratchet.png");
                        }
                    } catch (Exception ex) {
                    }
            }
        }
    }

    /** Forwards Form commands to nameFormCommand(Command) and soft-key commands to keyPressed. */
    public final synchronized void commandAction(Command command, Displayable displayable) {
        if (displayable == this.nameForm) {
            this.nameFormCommand(command);
            return;
        }
        if (command.getCommandType() == 4 && softKeyPositive >= 0) {
            this.keyPressed(-6);
            return;
        }
        if (command.getCommandType() == 2 && softKeyBack >= 0) {
            this.keyPressed(-7);
        }
    }

    /** Draws the soft-key labels: string leftLabel on the left, rightLabel on the right (-1 = none). */
    public final void drawSoftKeys(Graphics graphics, int leftLabel, int rightLabel, Canvas canvas) {
        graphics.setClip(0, 0, g.screenWidth, g.screenHeight);
        graphics.setFont(g.titleFont);
        if (leftLabel >= 0) {
            String s = ratchetandclank.strings[leftLabel];
            int fh = g.titleFont.getHeight();
            int y = g.screenHeight - fh;
            int w = g.titleFont.stringWidth(s);
            graphics.setColor(0x6467FF);
            graphics.setColor(0xDCDCFF);
            graphics.drawString(s, 1, y, 20);
        }
        if (rightLabel >= 0) {
            String s = ratchetandclank.strings[rightLabel];
            int fh = g.titleFont.getHeight();
            int y = g.screenHeight - fh;
            int w = g.titleFont.stringWidth(s);
            graphics.setColor(0x6467FF);
            graphics.setColor(0xDCDCFF);
            graphics.drawString(s, g.screenWidth - w - 1, y, 20);
        }
    }

    /** Draws a centred menu entry (highlighted when selected); returns the next y. */
    public final int drawEntry(Graphics graphics, String text, int x, int y, int anchor, boolean selected) {
        int res = 0;
        graphics.setFont(selected ? g.boldFont : g.smallFont);
        graphics.setColor(selected == true ? 0x11EDEF : 0xDCDCFF);
        if (selected) {
            this.highlightedText = text;
        }
        res = this.app.game.drawWrapped(graphics, text, x, y, 17);
        return res;
    }

    /** Draws a menu entry with anchor {@code anchor} (highlighted when selected); returns the next y. */
    public final int drawEntryAnchored(Graphics graphics, String text, int x, int y, int anchor, boolean selected) {
        int res = 0;
        graphics.setFont(selected ? g.boldFont : g.smallFont);
        graphics.setColor(selected == true ? 0x11EDEF : 0xDCDCFF);
        if (selected) {
            this.highlightedText = text;
        }
        res = this.app.game.drawWrapped(graphics, text, x, y, anchor);
        return res;
    }

    /** Draws save-slot entry slot: "n.(empty)" or "n. <play time>". */
    public final int drawSlotEntry(Graphics graphics, byte slot, int x, int y, int anchor, boolean selected) {
        int res = 0;
        if (this.app.slotSummaries[slot] == 0) {
            res = this.drawEntryAnchored(graphics, String.valueOf(slot + 1) + ratchetandclank.strings[31], x, y, 17, selected);
        } else {
            Object[] args = { new Integer(slot + 1), new String(this.formatPlayTime(this.app.slotPlayTimes[slot])) };
            String s = this.app.game.format(ratchetandclank.strings[32], args);
            res = this.drawEntry(graphics, s, x, y, anchor, selected);
        }
        return res;
    }

    /** Draws a screen title; returns the next y. */
    public final int drawTitle(Graphics graphics, String title) {
        graphics.setFont(g.titleFont);
        graphics.setColor(0xDCDCFF);
        return this.app.game.drawWrapped(graphics, title, this.getWidth() >> 1, this.menuScreen == 5 ? 0 : 5, 17);
    }

    /** Draws the selection box around the highlighted entry at y boxY. */
    public final void drawSelectionBox(Graphics graphics, int textX, int boxY) {
        graphics.setClip(0, 0, screenWidth, screenHeight);
        graphics.setFont(g.boldFont);
        int w = 138 + (screenWidth - 176);
        int x = screenWidth - w >> 1;
        graphics.setColor(36, 86, 100);
        int ht = g.boldFont.getHeight() + 4;
        if (g.boldFont.stringWidth(this.highlightedText) > screenWidth - 34) {
            ht = g.boldFont.getHeight() * 2 + 4;
        }
        graphics.fillRoundRect(x, boxY - 3, w, ht, 4, 4);
        graphics.setColor(23, 186, 204);
        graphics.drawRoundRect(x, boxY - 3, w - 1, ht - 1, 4, 4);
        graphics.setColor(0x11EDEF);
        this.app.game.drawWrapped(graphics, this.highlightedText, textX, boxY - 1, 17);
    }

    /** Draws the "Page page/pages" indicator between the soft keys. */
    private void drawPageIndicator(Graphics graphics, int page, int pages) {
        Object[] args = { new Integer(page), new Integer(pages) };
        String s = this.app.game.format(ratchetandclank.strings[36], args);
        graphics.setFont(g.smallFont);
        int w9 = g.titleFont.stringWidth(ratchetandclank.strings[9]);
        int w8 = g.titleFont.stringWidth(ratchetandclank.strings[8]);
        int avail = screenWidth - w9 - w8;
        int sw;
        if ((sw = g.smallFont.stringWidth(s)) < avail) {
            graphics.drawString(s, w9 + (avail - sw >> 1), g.screenHeight - g.lineHeight - 1, 20);
            return;
        }
        graphics.drawString(s, w9 + (avail - sw >> 1), screenHeight - 1 - g.lineHeight - g.titleFont.getHeight(), 20);
    }

    /** Formats a play time in ms as H:MM:SS. */
    public final String formatPlayTime(int millis) {
        int sec = millis / 1000;
        int min = (millis /= 60000) / 60;
        millis %= 60;
        sec %= 60;
        return String.valueOf(min) + (millis < 10 ? ":0" : ":") + String.valueOf(millis) + (sec < 10 ? ":0" : ":") + String.valueOf(sec);
    }

    /** Cursor up with wrap-around: value - 1 while above min, else wrap. */
    private byte cursorUp(byte value, byte min, byte wrap) {
        if (value > min) {
            return --value;
        }
        return wrap;
    }

    /** Cursor down with wrap-around: value + 1 while below max, else wrap. */
    private byte cursorDown(byte value, byte max, byte wrap) {
        if (value < max) {
            return ++value;
        }
        return wrap;
    }

    /**
     * Draws the splash screen (publisher image with the loading bar, then the other splash images).
     */
    public final void drawSplash(Graphics graphics) {
        int ww = this.getWidth();
        int hh = this.getHeight();
        if (this.splashStage == 0) {
            if ((this.repaintFlags & 1) != 0) {
                try {
                    if (this.splashImages[0] == null) {
                        this.splashImages[0] = Image.createImage("/intro_publisher.png");
                        g.sleep(30);
                    }
                } catch (Exception ex) {
                }
                graphics.setColor(0);
                graphics.fillRect(0, 0, ww, hh);
                graphics.drawImage(this.splashImages[this.splashStage], ww >> 1, hh - this.splashImages[this.splashStage].getHeight() >> 1, 17);
                this.splashImages[this.splashStage] = null;
                System.gc();
                g.sleep(20);
                this.repaintFlags = 0;
            }
            graphics.setClip(0, hh >> 1, ww, hh >> 1);
            graphics.setColor(0xCC0000);
            graphics.fillRect((ww - 100 >> 1) + 1, hh - 14, loadingProgress * 100 / 137, 6);
            graphics.setColor(0xFFFFFF);
            graphics.drawRect(ww - 100 >> 1, hh - 14, 102, 6);
            return;
        }
        if (this.splashImages[this.splashStage] == null) {
            return;
        }
        int col = 0;
        if (this.splashStage == 1) {
            col = 0xFFFFFF;
        }
        graphics.setClip(0, 0, ww, hh);
        graphics.setColor(col);
        graphics.fillRect(0, 0, ww, hh);
        graphics.setClip((ww >> 1) - (this.splashImages[this.splashStage].getWidth() >> 1), (hh >> 1) - (this.splashImages[this.splashStage].getHeight() >> 1), this.splashImages[this.splashStage].getWidth(), this.splashImages[this.splashStage].getHeight());
        graphics.drawImage(this.splashImages[this.splashStage], (ww >> 1) - (this.splashImages[this.splashStage].getWidth() >> 1), (hh >> 1) - (this.splashImages[this.splashStage].getHeight() >> 1), 20);
        this.splashImages[0] = null;
        this.splashImages[this.splashStage] = null;
    }

    /** Advances the loading bar by amount and repaints immediately. */
    public final void advanceLoading(int amount) {
        loadingProgress += amount;
        System.gc();
        this.repaint();
        this.serviceRepaints();
        g.sleep(10);
        try {
            Thread.yield();
        } catch (Exception ex) {
        }
        if (this.splashImages[1] == null) {
            try {
                this.splashImages[1] = Image.createImage("/intro_handheld.png");
            } catch (Exception ex) {
            }
        }
    }

    /** Draws the main menu with the running attract-mode actors. */
    private void drawMainMenu(Graphics graphics) {
        int y = 0;
        this.drawBackground(graphics, (byte)0);
        this.app.game.player.draw(graphics, this.app.game.player.drawLayer, 0, -4);
        graphics.setClip(0, 0, g.screenWidth, g.screenHeight);
        this.app.game.enemies[0].draw(graphics, 0, this.app.game.enemies[0].drawLayer, 0, -4);
        this.app.game.enemies[1].draw(graphics, 1, this.app.game.enemies[1].drawLayer, 0, -4);
        this.app.game.enemies[2].draw(graphics, 1, this.app.game.enemies[1].drawLayer, 0, -4);
        this.app.game.enemies[3].draw(graphics, 1, this.app.game.enemies[1].drawLayer, 0, -4);
        graphics.setClip(0, 0, this.getWidth(), this.getHeight());
        graphics.drawImage(g.imgMenuHeader, screenWidth >> 1, 4, 17);
        y = 4 + g.imgMenuHeader.getHeight() + 4;
        graphics.setFont(g.smallFont);
        this.visibleLines = (screenHeight - tileHeight * 2 - y) / (g.lineHeight + 2);
        int yy = y;
        for (int n = this.firstVisibleLine; n < this.lineCount && n < this.firstVisibleLine + this.visibleLines; n++) {
            int ny = 2 + this.drawEntry(graphics, ratchetandclank.strings[this.mainMenuItems[n]], 0 + tileWidth + (tileWidth >> 1), yy, 0, this.menuSelection == n);
            if (n == this.menuSelection) {
                this.drawSelectionBox(graphics, 0 + tileWidth + (tileWidth >> 1), yy);
            }
            yy = ny;
        }
        if ((System.currentTimeMillis() / 1000L & 1L) > 0L) {
            if (this.firstVisibleLine > 0) {
                graphics.drawRegion(g.imgArrowRight, 0, 0, g.imgArrowRight.getWidth(), g.imgArrowRight.getHeight(), 6, 7, 80, 20);
                graphics.drawRegion(g.imgArrowRight, 0, 0, g.imgArrowRight.getWidth(), g.imgArrowRight.getHeight(), 6, screenWidth - 7 - g.imgArrowRight.getHeight(), 80, 20);
            }
            if (this.firstVisibleLine + this.visibleLines < this.mainMenuItems.length) {
                graphics.drawRegion(g.imgArrowRight, 0, 0, g.imgArrowRight.getWidth(), g.imgArrowRight.getHeight(), 5, 7, 170, 20);
                graphics.drawRegion(g.imgArrowRight, 0, 0, g.imgArrowRight.getWidth(), g.imgArrowRight.getHeight(), 5, screenWidth - 7 - g.imgArrowRight.getHeight(), 170, 20);
            }
        }
        this.drawSoftKeys(graphics, 7, -1, this);
    }

    private void drawLoadGame(Graphics graphics) {
        this.drawBackground(graphics, (byte)0);
        graphics.setClip(0, 0, this.getWidth(), this.getHeight());
        this.app.game.drawPanel(graphics);
        int y;
        int t = this.drawTitle(graphics, ratchetandclank.strings[2]);
        graphics.setFont(g.smallFont);
        this.menuItemY[0] = 102;
        this.menuItemY[1] = y = 2 + this.drawSlotEntry(graphics, (byte)0, 0 + tileWidth + (tileWidth >> 1), 102, 0, this.menuSelection == 0);
        this.menuItemY[2] = y = 2 + this.drawSlotEntry(graphics, (byte)1, 0 + tileWidth + (tileWidth >> 1), y, 0, this.menuSelection == 1);
        t = this.drawSlotEntry(graphics, (byte)2, 0 + tileWidth + (tileWidth >> 1), y, 0, this.menuSelection == 2);
        this.drawSoftKeys(graphics, 7, 8, this);
        this.drawSelectionBox(graphics, 0 + tileWidth + (tileWidth >> 1), this.menuItemY[this.menuSelection]);
    }

    /** Draws the about / credits screen. */
    private void drawAbout(Graphics graphics, int page) {
        int y = 0;
        this.drawBackground(graphics, (byte)0);
        graphics.setClip(0, 0, this.getWidth(), this.getHeight());
        y = this.drawTitle(graphics, ratchetandclank.strings[5]);
        graphics.setFont(g.smallFont);
        y += g.smallFont.getHeight();
        this.drawTextList(graphics, this.aboutLines, y, true, screenWidth - 14, 83, 89);
        this.drawSoftKeys(graphics, -1, 8, this);
    }

    /**
     * Draws a scrolling text list with its scroll bar; skin code rows q and r are drawn as arrows.
     */
    public final void drawTextList(Graphics graphics, Vector lines, int top, boolean centred, int barX, int barY, int barHeight) {
        if (lines == null) {
            return;
        }
        if (lines.size() == 0) {
            return;
        }
        int yy = top;
        this.visibleLines = (175 - top) / g.lineHeight;
        if (this.visibleLines < this.lineCount) {
            int bh = this.visibleLines * barHeight / this.lineCount;
            int by = barY + this.firstVisibleLine * (barHeight - bh) / (this.lineCount - this.visibleLines);
            graphics.drawRect(barX, barY, 5, barHeight);
            graphics.fillRect(barX, by, 5, bh);
        }
        for (int n = this.firstVisibleLine; n < this.lineCount && n < this.firstVisibleLine + this.visibleLines; n++, yy += g.lineHeight) {
            String str = (String)lines.elementAt(n);
            if (!centred) {
                graphics.drawString(str, 17, yy, 20);
            } else if (n == this.skinCodeRow1) {
                this.drawSkinCode(graphics, 1, yy);
            } else if (n == this.skinCodeRow2) {
                this.drawSkinCode(graphics, 0, yy);
            } else if (n == this.skinCodeRow1 - 1 || n == this.skinCodeRow2 - 1) {
                graphics.setFont(g.titleFont);
                graphics.drawString(str, screenWidth >> 1, yy, 17);
                yy += g.titleFont.getHeight() - g.lineHeight;
                graphics.setFont(g.smallFont);
            } else {
                graphics.drawString(str, screenWidth >> 1, yy, 17);
            }
        }
    }

    /** Draws skin code {@code code} as 8 arrow images at y {@code y}. */
    private int drawSkinCode(Graphics graphics, int code, int y) {
        int x = screenWidth - 110 >> 1;
        for (int n = 0; n < 8; n++, x += 14) {
            graphics.setClip(x, y, 12, 12);
            graphics.drawImage(g.imgArrows, x - this.skinCodes[code][n] * 12, y, 20);
        }
        graphics.setClip(0, 0, this.getWidth(), this.getHeight());
        return y + 14;
    }

    /** Draws the skin unlock-code screen. */
    private void drawSkinCodes(Graphics graphics) {
        this.drawBackground(graphics, (byte)0);
        graphics.setClip(0, 0, this.getWidth(), this.getHeight());
        int y = this.drawTitle(graphics, ratchetandclank.strings[286]);
        y += g.smallFont.getHeight() / 2;
        this.drawTextList(graphics, this.skinLines, y, true, screenWidth - 14, 83, 89);
        this.drawSoftKeys(graphics, 39, 8, this);
    }

    /** Draws the skin information screen. */
    private void drawSkinInfo(Graphics graphics) {
        this.drawBackground(graphics, (byte)0);
        graphics.setClip(0, 0, this.getWidth(), this.getHeight());
        int y = this.drawTitle(graphics, ratchetandclank.strings[286]);
        graphics.setFont(g.smallFont);
        y += graphics.getFont().getHeight();
        this.drawTextList(graphics, this.skinLines, y, true, screenWidth - 14, 83, 89);
        this.drawSoftKeys(graphics, -1, 8, this);
    }

    /** Draws the settings screen. */
    private void drawSettings(Graphics graphics) {
        this.drawBackground(graphics, (byte)0);
        graphics.setClip(0, 0, this.getWidth(), this.getHeight());
        this.app.game.drawPanel(graphics);
        this.drawTitle(graphics, ratchetandclank.strings[27]);
        graphics.setFont(g.smallFont);
        this.menuItemY[0] = 102;
        int y;
        this.menuItemY[1] = y = 2 + this.drawEntry(graphics, this.app.soundEnabled == true ? ratchetandclank.strings[28] : ratchetandclank.strings[29], 0 + tileWidth + (tileWidth >> 1), 102, 0, this.menuSelection == 0);
        y = 2 + this.drawEntry(graphics, ratchetandclank.strings[33], 0 + tileWidth + (tileWidth >> 1), y, 0, this.menuSelection == 1);
        this.menuItemY[2] = y;
        this.drawEntry(graphics, ratchetandclank.strings[314], screenWidth >> 1, y, 17, this.menuSelection == 2);
        this.drawSoftKeys(graphics, 7, 8, this);
        this.drawSelectionBox(graphics, 0 + tileWidth + (tileWidth >> 1), this.menuItemY[this.menuSelection]);
    }

    /** Draws the language selection screen. */
    private void drawLanguageSelect(Graphics graphics) {
        this.drawBackground(graphics, (byte)0);
        String title = ratchetandclank.language == -1 ? ratchetandclank.strings[7] + " " + ratchetandclank.strings[314] : ratchetandclank.strings[314];
        int y = this.drawTitle(graphics, title);
        y += 28;
        this.menuItemY[0] = y;
        this.menuItemY[1] = y = 2 + this.drawEntry(graphics, ratchetandclank.strings[315], 0, y, 0, this.menuSelection == 0);
        this.menuItemY[2] = y = 2 + this.drawEntry(graphics, ratchetandclank.strings[316], 0, y, 0, this.menuSelection == 1);
        this.menuItemY[3] = y = 2 + this.drawEntry(graphics, ratchetandclank.strings[317], 0, y, 0, this.menuSelection == 2);
        this.menuItemY[4] = y = 2 + this.drawEntry(graphics, ratchetandclank.strings[318], 0, y, 0, this.menuSelection == 3);
        this.drawEntry(graphics, ratchetandclank.strings[319], 0, y, 0, this.menuSelection == 4);
        this.drawSoftKeys(graphics, 7, ratchetandclank.language == -1 ? -1 : 8, this);
        this.drawSelectionBox(graphics, 0, this.menuItemY[this.menuSelection]);
    }

    /** Draws help page {@code page} (controls, weapons with a live demo, crates). */
    public final void drawHelpPage(Graphics graphics, int page) {
        int y = 0;
        int u4 = 0;
        int u5 = 0;
        int iw = g.imgCrates.getWidth();
        int ih = g.imgCrates.getHeight() >> 3;
        this.drawBackground(graphics, (byte)0);
        graphics.setClip(0, 0, this.getWidth(), this.getHeight());
        this.app.game.drawPanel(graphics);
        y = this.drawTitle(graphics, ratchetandclank.strings[4]);
        graphics.setFont(g.smallFont);
        if (page < 3) {
            this.drawEntry(graphics, ratchetandclank.strings[76], 0 + (tileWidth >> 1), y, 0, false);
        } else if (page < 11) {
            this.drawEntry(graphics, ratchetandclank.strings[262], screenWidth >> 1, y, 17, false);
        } else {
            this.drawEntry(graphics, ratchetandclank.strings[271], screenWidth >> 1, y, 17, false);
        }
        if (this.helpLines == null) {
            int tw = screenWidth - 36;
            String blank = new String("");
            this.helpLines = new Vector();
            if (page < 3) {
                appendAll(this.helpLines, ratchetandclank.wrapText(ratchetandclank.strings[77], tw));
                appendAll(this.helpLines, ratchetandclank.wrapText(ratchetandclank.strings[78], tw));
                appendAll(this.helpLines, ratchetandclank.wrapText(ratchetandclank.strings[79], tw));
                appendAll(this.helpLines, ratchetandclank.wrapText(ratchetandclank.strings[80], tw));
                appendAll(this.helpLines, ratchetandclank.wrapText(ratchetandclank.strings[258], tw));
                appendAll(this.helpLines, ratchetandclank.wrapText(ratchetandclank.strings[259], tw));
                appendAll(this.helpLines, ratchetandclank.wrapText(ratchetandclank.strings[81], tw));
                appendAll(this.helpLines, ratchetandclank.wrapText(ratchetandclank.strings[82], tw));
                appendAll(this.helpLines, ratchetandclank.wrapText(ratchetandclank.strings[83], tw));
                appendAll(this.helpLines, ratchetandclank.wrapText(ratchetandclank.strings[260], tw));
            } else if (page == 3) {
                appendAll(this.helpLines, ratchetandclank.wrapText(ratchetandclank.strings[48], tw));
                this.helpLines.addElement(blank);
                appendAll(this.helpLines, ratchetandclank.wrapText(ratchetandclank.strings[263], tw));
            } else if (page == 4) {
                appendAll(this.helpLines, ratchetandclank.wrapText(ratchetandclank.strings[49], tw));
                this.helpLines.addElement(blank);
                appendAll(this.helpLines, ratchetandclank.wrapText(ratchetandclank.strings[264], tw));
            } else if (page == 5) {
                appendAll(this.helpLines, ratchetandclank.wrapText(ratchetandclank.strings[50], tw));
                this.helpLines.addElement(blank);
                appendAll(this.helpLines, ratchetandclank.wrapText(ratchetandclank.strings[265], tw));
            } else if (page == 6) {
                appendAll(this.helpLines, ratchetandclank.wrapText(ratchetandclank.strings[51], tw));
                this.helpLines.addElement(blank);
                appendAll(this.helpLines, ratchetandclank.wrapText(ratchetandclank.strings[266], tw));
            } else if (page == 7) {
                appendAll(this.helpLines, ratchetandclank.wrapText(ratchetandclank.strings[52], tw));
                this.helpLines.addElement(blank);
                appendAll(this.helpLines, ratchetandclank.wrapText(ratchetandclank.strings[267], tw));
            } else if (page == 8) {
                appendAll(this.helpLines, ratchetandclank.wrapText(ratchetandclank.strings[53], tw));
                this.helpLines.addElement(blank);
                appendAll(this.helpLines, ratchetandclank.wrapText(ratchetandclank.strings[268], tw));
            } else if (page == 9) {
                appendAll(this.helpLines, ratchetandclank.wrapText(ratchetandclank.strings[54], tw));
                this.helpLines.addElement(blank);
                appendAll(this.helpLines, ratchetandclank.wrapText(ratchetandclank.strings[269], tw));
            } else if (page == 10) {
                appendAll(this.helpLines, ratchetandclank.wrapText(ratchetandclank.strings[55], tw));
                this.helpLines.addElement(blank);
                appendAll(this.helpLines, ratchetandclank.wrapText(ratchetandclank.strings[270], tw));
            } else if (page == 11) {
                appendAll(this.helpLines, ratchetandclank.wrapText(ratchetandclank.strings[272], tw));
                this.helpLines.addElement(blank);
                appendAll(this.helpLines, ratchetandclank.wrapText(ratchetandclank.strings[275], tw));
            } else if (page == 12) {
                appendAll(this.helpLines, ratchetandclank.wrapText(ratchetandclank.strings[273], tw));
                this.helpLines.addElement(blank);
                appendAll(this.helpLines, ratchetandclank.wrapText(ratchetandclank.strings[276], tw));
            } else if (page == 13) {
                appendAll(this.helpLines, ratchetandclank.wrapText(ratchetandclank.strings[274], tw));
                this.helpLines.addElement(blank);
                appendAll(this.helpLines, ratchetandclank.wrapText(ratchetandclank.strings[277], tw));
            }
            this.lineCount = this.helpLines.size();
            this.firstVisibleLine = 0;
        }
        int iy = 2 * tileHeight - (tileHeight >> 1) + 2;
        if (page == 11) {
            graphics.setClip(screenWidth - iw >> 1, iy, iw, ih);
            graphics.drawImage(g.imgCrates, screenWidth - iw >> 1, iy, 0);
            graphics.setClip(0, 0, screenWidth, screenHeight);
        } else if (page == 12) {
            graphics.setClip(screenWidth - iw >> 1, iy, iw, ih);
            graphics.drawImage(g.imgCrates, screenWidth - iw >> 1, iy - ih, 0);
            graphics.setClip(0, 0, screenWidth, screenHeight);
        } else if (page == 13) {
            graphics.setClip(screenWidth - iw >> 1, iy, iw, ih);
            graphics.drawImage(g.imgCrates, screenWidth - iw >> 1, iy - 2 * ih, 0);
            graphics.setClip(0, 0, screenWidth, screenHeight);
        }
        this.drawTextList(graphics, this.helpLines, 85, page >= 3, screenWidth - 17, 89, 80);
        this.drawPageIndicator(graphics, page < 3 ? 1 : page - 1, 12);
        this.drawSoftKeys(graphics, 9, 8, this);
        if (page > 2 && page < 11) {
            this.app.game.player.draw(graphics, this.app.game.player.drawLayer, 0, -4);
            if (page > 3) {
                for (int n = 9; n >= 0; n--) {
                    this.app.game.playerShots[n].draw(graphics);
                }
            }
        }
    }

    private void drawExitQuestion(Graphics graphics) {
        this.drawBackground(graphics, (byte)0);
        graphics.setClip(0, 0, this.getWidth(), this.getHeight());
        this.app.game.drawPanel(graphics);
        int y;
        int t = this.drawTitle(graphics, ratchetandclank.strings[35]);
        graphics.setFont(g.smallFont);
        this.menuItemY[0] = 104;
        this.menuItemY[1] = y = 4 + this.drawEntry(graphics, ratchetandclank.strings[11], 0 + tileWidth + (tileWidth >> 1), 104, 0, this.menuSelection == 0);
        t = this.drawEntry(graphics, ratchetandclank.strings[10], 0 + tileWidth + (tileWidth >> 1), y, 0, this.menuSelection == 1);
        this.drawSoftKeys(graphics, 7, 8, this);
        this.drawSelectionBox(graphics, 0 + tileWidth + (tileWidth >> 1), this.menuItemY[this.menuSelection]);
    }

    private void drawClearSaveList(Graphics graphics) {
        this.drawBackground(graphics, (byte)0);
        graphics.setClip(0, 0, this.getWidth(), this.getHeight());
        this.app.game.drawPanel(graphics);
        int y;
        int t = this.drawTitle(graphics, ratchetandclank.strings[33]);
        graphics.setFont(g.smallFont);
        this.menuItemY[0] = 102;
        this.menuItemY[1] = y = 2 + this.drawSlotEntry(graphics, (byte)0, 0 + tileWidth + (tileWidth >> 1), 102, 0, this.menuSelection == 0);
        this.menuItemY[2] = y = 2 + this.drawSlotEntry(graphics, (byte)1, 0 + tileWidth + (tileWidth >> 1), y, 0, this.menuSelection == 1);
        t = this.drawSlotEntry(graphics, (byte)2, 0 + tileWidth + (tileWidth >> 1), y, 0, this.menuSelection == 2);
        this.drawSoftKeys(graphics, 7, 8, this);
        this.drawSelectionBox(graphics, 0 + tileWidth + (tileWidth >> 1), this.menuItemY[this.menuSelection]);
    }

    private void drawConfirm(Graphics graphics) {
        this.drawBackground(graphics, (byte)0);
        graphics.setClip(0, 0, this.getWidth(), this.getHeight());
        this.app.game.drawPanel(graphics);
        graphics.setFont(g.titleFont);
        graphics.setColor(0xDCDCFF);
        int y;
        int t = this.app.game.drawWrappedTo(graphics, ratchetandclank.strings[34], tileWidth + (tileWidth >> 3), 0, 17, screenWidth - tileWidth);
        graphics.setFont(g.smallFont);
        this.menuItemY[0] = 104;
        this.menuItemY[1] = y = 4 + this.drawEntry(graphics, ratchetandclank.strings[11], 0 + tileWidth + (tileWidth >> 1), 104, 0, this.menuSelection == 0);
        t = this.drawEntry(graphics, ratchetandclank.strings[10], 0 + tileWidth + (tileWidth >> 1), y, 0, this.menuSelection == 1);
        this.drawSoftKeys(graphics, 7, 8, this);
        this.drawSelectionBox(graphics, 0 + tileWidth + (tileWidth >> 1), this.menuItemY[this.menuSelection]);
    }

    public final void drawNewGameSlots(Graphics graphics) {
        this.drawBackground(graphics, (byte)0);
        graphics.setClip(0, 0, this.getWidth(), this.getHeight());
        this.app.game.drawPanel(graphics);
        this.drawTitle(graphics, ratchetandclank.strings[283]);
        graphics.setFont(g.smallFont);
        this.menuItemY[0] = 102;
        int y;
        this.menuItemY[1] = y = 2 + this.drawSlotEntry(graphics, (byte)0, 0 + tileWidth + (tileWidth >> 1), 102, 0, this.menuSelection == 0);
        this.menuItemY[2] = y = 2 + this.drawSlotEntry(graphics, (byte)1, 0 + tileWidth + (tileWidth >> 1), y, 0, this.menuSelection == 1);
        this.drawSlotEntry(graphics, (byte)2, 0 + tileWidth + (tileWidth >> 1), y, 0, this.menuSelection == 2);
        this.app.menu.drawSoftKeys(graphics, 7, 8, this);
        this.app.menu.drawSelectionBox(graphics, tileWidth >> 1, this.menuItemY[this.menuSelection]);
    }

    /** Moves a demo projectile on the help screen (simplified per-type motion). */
    public final void moveDemoShot(h shot) {
        if (shot.shotType == -1) {
            return;
        }
        if (shot.shotType >= 6 && shot.shotType <= 8) {
            shot.posX += shot.speedX;
            int x = shot.posX >> 8;
            shot.posY += shot.speedY;
            int y = 0;
            if (x > screenWidth) {
                shot.release();
            }
        } else if (shot.shotType >= 0 && shot.shotType <= 2) {
            shot.posX += shot.speedX;
            int x = shot.posX >> 8;
            shot.posY += shot.speedY;
            int y = 0;
            if (x > screenWidth) {
                shot.release();
            }
        } else if (shot.shotType >= 3 && shot.shotType <= 5) {
            shot.posX += shot.speedX;
            int x = shot.posX >> 8;
            int y = shot.posY >> 8;
            if (y > screenHeight || x > screenWidth) {
                shot.release();
            }
            shot.posY -= shot.speedY;
            shot.speedY -= 256;
        } else if (shot.shotType >= 9 && shot.shotType <= 11) {
            if (shot.tick++ == 2) {
                shot.release();
            }
        } else if (shot.shotType >= 12 && shot.shotType <= 14) {
            shot.steerHoming(1);
            int x = shot.posX >> 8;
            int y = 0;
            if (x > screenWidth) {
                shot.release();
            }
        } else if (shot.shotType >= 15 && shot.shotType <= 17) {
            if (shot.tick++ == 3) {
                shot.release();
            }
        } else if (shot.shotType >= 18 && shot.shotType <= 20) {
            shot.steerHoming(4);
            int x = shot.posX >> 8;
            int y = 0;
            if (x > screenWidth) {
                shot.release();
            }
        }
    }

    /** Pushes key keyCode into the last-four-keys buffer Y. */
    private void pushKey(int keyCode) {
        this.recentKeys[0] = this.recentKeys[1];
        this.recentKeys[1] = this.recentKeys[2];
        this.recentKeys[2] = this.recentKeys[3];
        this.recentKeys[3] = keyCode;
    }

    /** True if the last four keys equal the secret sequence secretSequence. */
    private boolean secretSequenceEntered() {
        for (int n = 0; n < 4; n++) {
            if (this.recentKeys[n] != secretSequence[n]) {
                return false;
            }
        }
        return true;
    }
}
