import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;

/**
 * b - Sound effects and menu music (MMAPI).
 *
 * Five WAV effects are pre-loaded and pre-realised as Players. Because a Player is closed
 * after use, a background thread re-creates spent Players and starts the effect requested
 * through requestEffect(int). Menu music is a looping MIDI Player (startMenuMusic(boolean) /
 * stopMenuMusic()).
 */
public final class b implements Runnable, PlayerListener {
    /** Sound-effect resource names (/<name>.wav); the index is the effect id. */
    public static final String[] effectNames = new String[]{"death", "bubble", "shoot", "msound", "box"};
    /** Sound thread keeps running while true. */
    public boolean running;
    /** An effect is currently playing. */
    public boolean effectPlaying;
    /** Looping menu music Player (null when stopped). */
    public Player musicPlayer;
    /** Raw WAV data per effect (kept to re-create Players). */
    public byte[][] wavData;
    /** Ready (realised) Player per effect; null once consumed. */
    public Player[] readyPlayers;
    /** Effect Player currently playing. */
    public Player currentEffect;
    /** Effect requested for the sound thread to start (-1 = none). */
    public int requestedEffect;
    /** Start time of the current effect (ms); used for the 2 s anti-spam window. */
    public long effectStartTime;

    /*
     * Reconstruction device (retained; removed from final class output).
     * Unused method placed before the constructor so that javac creates the constant-pool
     * NameAndType entries a:()V and b:()V before the constructor creates b:Z /
     * a:[String / a:(String)[B. ProGuard 3.2 shrinking removes this method but keeps the
     * surviving entries in javac creation order, which reproduces the retail constant-pool
     * order of b.class. Do not delete, rename or move.
     */
    private void opusGhostB() {
        this.stopEffect();
        this.stopMenuMusic();
    }

    /** Loads and realises all effect Players, then starts the sound thread. */
    public b() {
        this.running = true;
        this.requestedEffect = -1;
        this.wavData = new byte[5][];
        this.readyPlayers = new Player[5];
        int n = 0;
        while (n < this.wavData.length) {
            this.wavData[n] = this.readResource("/" + effectNames[n] + ".wav");
            try {
                this.readyPlayers[n] = Manager.createPlayer(new ByteArrayInputStream(this.wavData[n]), "audio/x-wav");
                this.readyPlayers[n].realize();
            } catch (Exception exception) {
            }
            ++n;
        }
        new Thread(this).start();
    }

    /** Reads a whole resource into a byte array (null on error). */
    public final byte[] readResource(String resource) {
        ByteArrayOutputStream byteArrayOutputStream = null;
        InputStream inputStream = null;
        byte[] byArray = null;
        try {
            inputStream = this.getClass().getResourceAsStream(resource);
            byteArrayOutputStream = new ByteArrayOutputStream();
            int n = inputStream.read();
            while (n >= 0) {
                byteArrayOutputStream.write(n);
                n = inputStream.read();
            }
            byArray = byteArrayOutputStream.toByteArray();
        } catch (Exception exception) {
            byArray = null;
        } finally {
            try {
                inputStream.close();
                byteArrayOutputStream.close();
            } catch (Exception exception) {
            }
        }
        return byArray;
    }

    /** Sound thread: starts the requested effect and re-creates consumed Players. */
    public final void run() {
        while (this.running) {
            try {
                Thread.sleep(50L);
            } catch (Exception exception) {
            }
            if (this.requestedEffect != -1) {
                synchronized (this) {
                    if (this.readyPlayers[this.requestedEffect] != null) {
                        try {
                            if (this.currentEffect != null) {
                                this.currentEffect.close();
                                this.currentEffect = null;
                            }
                            this.currentEffect = this.readyPlayers[this.requestedEffect];
                            this.readyPlayers[this.requestedEffect] = null;
                            this.currentEffect.addPlayerListener(this);
                            this.currentEffect.prefetch();
                            this.currentEffect.start();
                            this.effectStartTime = System.currentTimeMillis();
                            this.effectPlaying = true;
                        } catch (Exception exception) {
                        }
                    }
                    this.requestedEffect = -1;
                    continue;
                }
            }
            int n = 0;
            while (n < this.wavData.length) {
                if (this.readyPlayers[n] == null) {
                    try {
                        this.readyPlayers[n] = Manager.createPlayer(new ByteArrayInputStream(this.wavData[n]), "audio/x-wav");
                        this.readyPlayers[n].realize();
                    } catch (Exception exception) {
                    }
                }
                ++n;
            }
        }
    }

    /** Requests effect {@code effect}; while another effect plays it is only restarted after 2 seconds. */
    public final synchronized void requestEffect(int effect) {
        if (effect < 0 || effect >= 5) {
            return;
        }
        if (this.effectPlaying) {
            if (this.effectStartTime > 0L && System.currentTimeMillis() - this.effectStartTime > 2000L) {
                this.stopEffect();
                this.requestedEffect = effect;
            }
            return;
        }
        this.requestedEffect = effect;
    }

    /** Stops and closes the current effect. */
    public final void stopEffect() {
        try {
            if (this.currentEffect != null) {
                this.currentEffect.close();
                this.currentEffect = null;
            }
            this.effectStartTime = 0L;
            this.effectPlaying = false;
            return;
        } catch (Exception exception) {
            return;
        }
    }

    public final void playerUpdate(Player player, String string, Object object) {
        if (string.equals("stopped") || string.equals("closed")) {
            this.effectPlaying = false;
            this.effectStartTime = 0L;
            return;
        }
        if (string.equals("endOfMedia")) {
            if (this.currentEffect != null) {
                this.currentEffect.close();
                this.currentEffect = null;
            }
            this.effectPlaying = false;
            this.effectStartTime = 0L;
        }
    }

    /** Starts the looping menu music (/menu.mid); with restart == false only if not already playing. */
    public final void startMenuMusic(boolean restart) {
        if (!restart && this.musicPlayer != null) {
            return;
        }
        this.stopMenuMusic();
        try {
            this.musicPlayer = Manager.createPlayer(this.getClass().getResourceAsStream("/menu.mid"), "audio/midi");
            this.musicPlayer.prefetch();
            this.musicPlayer.setLoopCount(-1);
            this.musicPlayer.start();
            return;
        } catch (Exception exception) {
            return;
        }
    }

    /** Stops the menu music. */
    public final void stopMenuMusic() {
        if (this.musicPlayer != null) {
            this.musicPlayer.close();
            this.musicPlayer = null;
        }
    }
}
