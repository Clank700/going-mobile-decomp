/*
 * i - Actor base class.
 *
 * Shared state of the player (a) and enemies (d): type, 8.8 fixed-point position
 * and speed, facing, health and the animation state machine.
 *
 * Naming: this is reconstructed, output-exact Java source (not recovered original source).
 * Member names are descriptive source names; config/d-preobf.map maps every one of them
 * back to its retail (obfuscated) runtime name. Renames are always group-complete (all
 * members sharing a retail name), which reproduces ProGuard's retail constant-pool
 * ordering; see cleanup/docs/NAMING_SCHEME.md. Library overrides keep their API names,
 * classes keep their retail names, and reconstruction devices keep their opus* names.
 */
public class i {
    /** Actor type (enemies: index into the d per-type tables; -1 = free slot). */
    public byte actorKind;
    /** Tile row of the actor (vertical position = tileRow * g.tileHeight + (rowOffset >> 8)). */
    public byte tileRow;
    /** Horizontal position in 8.8 fixed point (pixels << 8). */
    public int posX;
    /** Vertical speed (8.8 fixed point; gravity adds +256 per frame). */
    public short speedY;
    /** Horizontal speed (8.8 fixed point). */
    public short speedX;
    /** Tile column probed ahead in the walking direction (wall / edge checks). */
    public byte aheadColumn;
    /** Tile row probed ahead (wall / edge checks). */
    public byte aheadRow;
    /** Vertical offset inside tile row tileRow, 8.8 fixed point (normalised by normaliseRow()). */
    public int rowOffset;
    /** Draw flags passed to the actor's painter (always 1 in play). */
    public int drawLayer;
    /** Health / hit points (enemies die at <= 0). */
    public byte health;
    /** Animation frame index within the current animation. */
    public short animFrame;
    /** Animation frame timer. */
    public short animTimer;
    /** Current animation / action id (set through setAnimation(byte)). */
    public byte animId;
    /** Animation mode: 0 = loop, 1 = play once, 2 = one-shot finished. */
    public byte animMode;
    /** Frames spent in the current timed state (knock-back, hurt). */
    public byte stateTimer;
    /** Facing right. */
    public boolean facingRight;
    /**
     * Wall orientation of wall-mounted turrets (0 floor, 1 ceiling, 2 left wall, 3 right wall); drawn rotated.
     */
    public byte orientation;

    /** Tile column of the actor (x pixel / tile width). */
    public final byte tileColumn() {
        return (byte)((this.posX >> 8) / g.tileWidth);
    }

    /** Switches to animation/action {@code animation} and restarts its frame counters. */
    public final void setAnimation(byte animation) {
        this.animId = animation;
        this.animFrame = this.animTimer = 0;
    }

    /**
     * Renormalises the vertical position: carries rowOffset overflow into tileRow (clamped at row 0).
     */
    public final void normaliseRow() {
        if (this.rowOffset < 0) {
            this.rowOffset += g.tileHeight << 8;
            --this.tileRow;
            if (this.tileRow < 0) {
                this.tileRow = 0;
                this.rowOffset = 0;
                return;
            }
        } else if (this.rowOffset > (g.tileHeight << 8)) {
            this.rowOffset -= g.tileHeight << 8;
            ++this.tileRow;
        }
    }
}
