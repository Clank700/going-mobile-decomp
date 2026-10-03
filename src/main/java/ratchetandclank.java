/*
 * ratchetandclank - MIDlet entry point.
 *
 * Application lifecycle (startApp/pauseApp/destroyApp), the Display, the two
 * Canvases (menu f and game g), the localised string table, text wrapping, sound,
 * vibration and record-store (save) helpers.
 *
 * Naming: this is reconstructed, output-exact Java source (not recovered original source).
 * Member names are descriptive source names; config/d-preobf.map maps every one of them
 * back to its retail (obfuscated) runtime name. Renames are always group-complete (all
 * members sharing a retail name), which reproduces ProGuard's retail constant-pool
 * ordering; see cleanup/docs/NAMING_SCHEME.md. Library overrides keep their API names,
 * classes keep their retail names, and reconstruction devices keep their opus* names.
 */
import java.io.DataInputStream;
import java.io.InputStream;
import java.util.Vector;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotOpenException;

public class ratchetandclank extends MIDlet {
    /** The MIDlet's Display. */
    public Display display;
    /** Menu / front-end canvas. */
    public f menu;
    /** Game canvas (in-game screens and main loop). */
    public g game;
    /** True while the game canvas is active (in a level) rather than the menus. */
    public boolean gameActive;
    /** True once startApp() has run; distinguishes first start from a resume. */
    public boolean started;
    /** Sound enabled (settings record byte 0 > 0). */
    public boolean soundEnabled;
    /** Sound-effect and music player. */
    public b sound;
    /** Per save slot: first byte of the slot record (slot summary shown in the slot list). */
    public byte[] slotSummaries;
    /** Per save slot: int stored at offset 195 of the slot record (accumulated play time, ms). */
    public int[] slotPlayTimes;
    /** Localised string table, loaded from /txt_<language>.mu8 (see loadStrings(String)). */
    public static String[] strings;
    /** Record ids in the "RatchetJavBig" record store: 1-3 = save slots, 4 = settings. */
    public static final int[] recordIds = {1, 2, 3, 4};
    /** 214-byte save-slot buffer (layout written by g.storeGame(byte[]), read by g.restoreGame(byte[])). */
    public byte[] slotBuffer;
    /** 3-byte settings record: [0] sound setting, [1] language index, [2] game-won flag. */
    private byte[] settings;
    /** Settings record already read into settings. */
    public boolean settingsLoaded;
    /** Selected language index into languageSuffixes (-1 = not chosen yet). */
    public static int language = -1;
    /** Language suffixes of the string-table resources. */
    public static final String[] languageSuffixes = {"_en.mu8", "_fr.mu8", "_it.mu8", "_gr.mu8", "_sp.mu8"};

    /**
     * Creates the Display, validates/creates the record store, reads the language,
     * initialises sound and creates the menu canvas.
     */
    public ratchetandclank() {
        super();
        this.gameActive = false;
        this.started = false;
        this.slotBuffer = new byte[214];
        this.settings = new byte[3];
        this.settingsLoaded = false;
        System.gc();
        this.display = Display.getDisplay(this);
        this.validateRecordStore();
        language = this.languageSetting();
        this.slotSummaries = new byte[3];
        this.slotPlayTimes = new int[3];
        this.initSound();
        this.menu = new f(this);
    }

    /** Creates the sound player; sound is enabled when the stored sound setting is > 0. */
    public final void initSound() {
        this.sound = new b();
        if (this.storedSoundSetting() > 0) {
            this.soundEnabled = true;
            return;
        }
        this.soundEnabled = false;
    }

    /** First start shows the menu canvas; later calls resume the active canvas. */
    public final void startApp() throws MIDletStateChangeException {
        if (!this.started) {
            this.started = true;
            this.menu.skipNextShowNotify = true;
            this.display.setCurrent((Displayable)this.menu);
            this.menu.startMenuThread();
            return;
        }
        if (this.gameActive) {
            this.game.resumeFromHide();
            return;
        }
        this.menu.resumeMenu();
    }

    /** Pauses whichever canvas (game or menu) is active. */
    public final void pauseApp() {
        if (this.gameActive) {
            this.game.pauseHidden();
            return;
        }
        this.menu.pauseMenu();
    }

    public final void destroyApp(boolean unconditional) throws MIDletStateChangeException {
        this.menu = null;
        this.game = null;
    }

    /** Leaves the menus and enters the game canvas: g.startGame(level, -1) (new game, argument level). */
    public final void newGame(int level) {
        this.stopMusic();
        this.readSlotSummaries();
        this.menu.stopMenu(true);
        this.game.menuCursor = 0;
        this.game.startGame(level, -1);
        this.gameActive = true;
    }

    /**
     * Leaves the menus and enters the game canvas: g.startGame(-1, slot) (argument slot as second parameter).
     */
    public final void continueGame(int slot) {
        this.stopMusic();
        this.menu.stopMenu(false);
        this.game.startGame(-1, slot);
        this.gameActive = true;
    }

    /** Returns from the game to the menu canvas. */
    public final void returnToMenu() {
        this.stopEffect();
        this.game.stopToPauseMenu();
        this.gameActive = false;
        this.menu.returnFromGame();
        this.display.setCurrent((Displayable)this.menu);
    }

    /** Plays sound effect {@code effect} (index into b.effectNames) when sound is enabled. */
    public final void playEffect(int effect) {
        if (!this.soundEnabled) {
            return;
        }
        this.sound.requestEffect(effect);
    }

    /** Starts the looping menu music when sound is enabled (see b.startMenuMusic(boolean)). */
    public final void startMenuMusic(boolean restart) {
        if (!this.soundEnabled) {
            return;
        }
        this.sound.startMenuMusic(restart);
    }

    /** Stops the menu music. */
    public final void stopMusic() {
        this.sound.stopMenuMusic();
    }

    /** Stops the current sound effect. */
    public final void stopEffect() {
        this.sound.stopEffect();
    }

    /** Reads all three save slots and caches their summary byte and play time (slotSummaries, slotPlayTimes). */
    public final void readSlotSummaries() {
        int index = 0;
        while (index < 3) {
            this.readSlot(index);
            this.slotSummaries[index] = this.slotBuffer[0];
            this.slotPlayTimes[index] = this.readInt(this.slotBuffer, 195);
            ++index;
        }
    }

    /** Writes an int big-endian into bytes[offset..offset+3]. */
    public final void writeInt(int value, byte[] bytes, int offset) {
        bytes[0 + offset] = (byte)(value >> 24 & 255);
        bytes[1 + offset] = (byte)(value >> 16 & 255);
        bytes[2 + offset] = (byte)(value >> 8 & 255);
        bytes[3 + offset] = (byte)(value & 255);
    }

    /** Reads a big-endian int from bytes[offset..offset+3]. */
    public final int readInt(byte[] bytes, int offset) {
        int first = bytes[0 + offset] < 0 ? bytes[0 + offset] + 256 : bytes[0 + offset];
        int second = bytes[1 + offset] < 0 ? bytes[1 + offset] + 256 : bytes[1 + offset];
        int third = bytes[2 + offset] < 0 ? bytes[2 + offset] + 256 : bytes[2 + offset];
        int fourth = bytes[3 + offset] < 0 ? bytes[3 + offset] + 256 : bytes[3 + offset];
        return first << 24 | second << 16 | third << 8 | fourth;
    }

    /** Writes a short big-endian into bytes[offset..offset+1]. */
    public final void writeShort(short value, byte[] bytes, int offset) {
        bytes[offset++] = (byte)(value >> 8);
        bytes[offset] = (byte)value;
    }

    /** Reads a big-endian short from bytes[offset..offset+1]. */
    public final short readShort(byte[] bytes, int offset) {
        return (short)((short)(bytes[0 + offset] < 0 ? bytes[0 + offset] + 256 : bytes[0 + offset]) << 8 | (short)(bytes[1 + offset] < 0 ? bytes[1 + offset] + 256 : bytes[1 + offset]));
    }

    /**
     * Validates the record store layout (3 x 214-byte slots + 3-byte settings); recreates it if invalid.
     */
    public final void validateRecordStore() {
        try {
            RecordStore store = RecordStore.openRecordStore("RatchetJavBig", true);
            int count = store.getNumRecords();
            if (count < 4) {
                store.closeRecordStore();
                this.resetRecordStore();
                return;
            }
            int index = 0;
            while (index < 4) {
                if (index < 3 && store.getRecordSize(recordIds[index]) != 214 || index == 3 && store.getRecordSize(recordIds[index]) != 3) {
                    store.closeRecordStore();
                    this.resetRecordStore();
                    return;
                }
                ++index;
            }
            store.closeRecordStore();
            return;
        } catch (Exception exception) {
            return;
        }
    }

    /**
     * Recreates the record store with three empty slots and default settings (sound on, no language, not won).
     */
    public final void resetRecordStore() {
        this.settingsLoaded = false;
        try {
            RecordStore store = RecordStore.openRecordStore("RatchetJavBig", true);
            int count = store.getNumRecords();
            int index = 0;
            while (index < count && index < 4) {
                store.deleteRecord(recordIds[index]);
                ++index;
            }
            store.addRecord(this.slotBuffer, 0, 214);
            store.addRecord(this.slotBuffer, 0, 214);
            store.addRecord(this.slotBuffer, 0, 214);
            this.settings[0] = 1;
            this.settings[1] = -1;
            this.settings[2] = 0;
            store.addRecord(this.settings, 0, 3);
            store.closeRecordStore();
            return;
        } catch (Exception exception) {
            return;
        }
    }

    /** Saves the current game (g.storeGame(byte[])) into save slot index. */
    public final void saveToSlot(int index) {
        try {
            RecordStore store = RecordStore.openRecordStore("RatchetJavBig", false);
            this.game.storeGame(this.slotBuffer);
            store.setRecord(recordIds[index], this.slotBuffer, 0, 214);
            store.closeRecordStore();
            return;
        } catch (Exception exception) {
            return;
        }
    }

    /** Loads save slot index into the game (g.restoreGame(byte[])). */
    public final void loadSlot(int index) {
        try {
            RecordStore store = RecordStore.openRecordStore("RatchetJavBig", false);
            store.getRecord(recordIds[index], this.slotBuffer, 0);
            store.closeRecordStore();
            this.game.restoreGame(this.slotBuffer);
            return;
        } catch (Exception exception) {
            System.err.println("loadGame: " + exception.toString());
            return;
        }
    }

    /** Clears save slot index. */
    public final void clearSlot(int index) {
        try {
            RecordStore store = RecordStore.openRecordStore("RatchetJavBig", false);
            int count = 0;
            while (count < 214) {
                this.slotBuffer[count] = 0;
                ++count;
            }
            store.setRecord(recordIds[index], this.slotBuffer, 0, 214);
            store.closeRecordStore();
            return;
        } catch (Exception exception) {
            return;
        }
    }

    /** Reads save slot index into the buffer l. */
    public final void readSlot(int index) {
        try {
            RecordStore store = RecordStore.openRecordStore("RatchetJavBig", false);
            store.getRecord(recordIds[index], this.slotBuffer, 0);
            store.closeRecordStore();
            return;
        } catch (Exception exception) {
            return;
        }
    }

    /** Returns the stored sound setting (settings byte 0). */
    public final byte storedSoundSetting() {
        if (!this.settingsLoaded) {
            try {
                RecordStore store = RecordStore.openRecordStore("RatchetJavBig", false);
                store.getRecord(recordIds[3], this.settings, 0);
                this.settingsLoaded = true;
                store.closeRecordStore();
            } catch (Exception exception) {
            }
        }
        return this.settings[0];
    }

    /** Game-won flag; the JAD/manifest property "GameIsWon" overrides the stored value. */
    public final boolean isGameWon() {
        if (!this.settingsLoaded) {
            try {
                RecordStore store = RecordStore.openRecordStore("RatchetJavBig", false);
                store.getRecord(recordIds[3], this.settings, 0);
                this.settingsLoaded = true;
                store.closeRecordStore();
            } catch (Exception exception) {
            }
        }
        String property;
        if ((property = this.getAppProperty("GameIsWon")) != null) {
            return property.equals("true");
        }
        return this.settings[2] != 0;
    }

    /** Returns the stored language index (settings byte 1). */
    public final byte languageSetting() {
        if (!this.settingsLoaded) {
            try {
                RecordStore store = RecordStore.openRecordStore("RatchetJavBig", false);
                store.getRecord(recordIds[3], this.settings, 0);
                this.settingsLoaded = true;
                store.closeRecordStore();
            } catch (Exception exception) {
            }
        }
        return this.settings[1];
    }

    /** Stores the sound setting (and the current language) in the settings record. */
    public final void storeSoundSetting(byte soundSetting) {
        try {
            RecordStore store = RecordStore.openRecordStore("RatchetJavBig", false);
            store.getRecord(recordIds[3], this.settings, 0);
            this.settings[0] = soundSetting;
            this.settings[1] = (byte)language;
            store.setRecord(recordIds[3], this.settings, 0, 3);
            store.closeRecordStore();
            return;
        } catch (RecordStoreNotOpenException exception) {
            return;
        } catch (InvalidRecordIDException exception) {
            return;
        } catch (RecordStoreException exception) {
            return;
        }
    }

    /** Marks the game as won in the settings record. */
    public final void markGameWon() {
        try {
            RecordStore store = RecordStore.openRecordStore("RatchetJavBig", false);
            store.getRecord(recordIds[3], this.settings, 0);
            this.settings[2] = 1;
            store.setRecord(recordIds[3], this.settings, 0, 3);
            store.closeRecordStore();
            return;
        } catch (Exception exception) {
            return;
        }
    }

    /**
     * Word-wraps text to lines no wider than width pixels (small font).
     * '\u00a6' forces a line break; '^' marks an invisible break opportunity; lines prefer
     * to break at spaces, then after '.' or '/', then after '-'.
     */
    public static final Vector wrapText(String text, int width) {
        Vector result = new Vector();
        StringBuffer buffer = new StringBuffer();
        int unused = 0;
        int lineLength = 0;
        int lastSpace = 0;
        int lineWidth = 0;
        char character;
        int lastPunctuation = -1;
        int lastHyphen = -1;
        for (int index = 0; index <= text.length(); ++index) {
            if (index < text.length()) {
                character = text.charAt(index);
                if (character == '\u00a6') {
                    character = '\n';
                }
            } else {
                character = '\n';
            }
            if (character == ' ') {
                lastSpace = lineLength;
                lineWidth += g.smallFont.charWidth(character);
                buffer.append(character);
                ++lineLength;
            } else {
                if (character == '^') {
                    lastSpace = lineLength;
                    continue;
                }
                if (character != '\n') {
                    if (character == '.' || character == '/') {
                        lastPunctuation = lineLength;
                    } else if (character == '-') {
                        lastHyphen = lineLength;
                    }
                    lineWidth += g.smallFont.charWidth(character);
                    buffer.append(character);
                    ++lineLength;
                }
            }
            if (lineWidth >= width || character == '\n') {
                if (lineWidth >= width) {
                    if (lastSpace > unused) {
                        lineLength = lastSpace;
                    } else if (lastPunctuation > 0) {
                        lineLength = lastPunctuation;
                    } else if (lastHyphen > 0) {
                        lineLength = lastHyphen + 1;
                    } else {
                        --lineLength;
                    }
                }
                if (lineWidth == 0) {
                    result.addElement(new String(""));
                } else {
                    char[] chars = new char[lineLength - 0];
                    buffer.getChars(0, lineLength, chars, 0);
                    result.addElement(new String(chars));
                }
                if (lineWidth >= width) {
                    if (buffer.length() > lineLength) {
                        if (buffer.charAt(lineLength) == ' ') {
                            buffer.delete(0, lineLength + 1);
                        } else {
                            buffer.delete(0, lineLength);
                        }
                    } else {
                        buffer.delete(0, lineLength);
                    }
                    lastSpace = 0;
                    lastPunctuation = -1;
                    lastHyphen = -1;
                    lineLength = buffer.length();
                    lineWidth = g.smallFont.stringWidth(buffer.toString());
                } else {
                    buffer.setLength(0);
                    lastSpace = 0;
                    lastPunctuation = -1;
                    lastHyphen = -1;
                    lineLength = 0;
                    lineWidth = 0;
                }
            }
        }
        return result;
    }

    /**
     * Loads a localised UTF string table: <text><language suffix> = count (short) + UTF strings.
     */
    public static final String[] loadStrings(String text) {
        String[] result = null;
        try {
            InputStream input = "".getClass().getResourceAsStream(text + languageSuffixes[language == -1 ? 0 : language]);
            DataInputStream data = new DataInputStream(input);
            int count = data.readShort();
            result = new String[count];
            int index = 0;
            while (index < count) {
                result[index] = data.readUTF();
                ++index;
            }
            input.close();
        } catch (Exception exception) {
            System.out.println("loadUTF8File: " + exception + " " + text + languageSuffixes[language == -1 ? 0 : language]);
        }
        return result;
    }

    /** Reloads the string table j from /txt for the current language. */
    public final void reloadStrings() {
        strings = null;
        System.gc();
        ((g)null).sleep(20);
        strings = ratchetandclank.loadStrings("/txt");
    }
}
