import java.io.InputStream;
import javax.microedition.lcdui.Graphics;

/*
 * d - Enemy.
 *
 * Extends the shared actor base i. Enemy instances (an array owned by g): per-type
 * data tables, AI/movement, animation, damage and drawing.
 *
 * Naming: this is reconstructed, output-exact Java source (not recovered original source).
 * Member names are descriptive source names; config/d-preobf.map maps every one of them
 * back to its retail (obfuscated) runtime name. Renames are always group-complete (all
 * members sharing a retail name), which reproduces ProGuard's retail constant-pool
 * ordering; see cleanup/docs/NAMING_SCHEME.md. Library overrides keep their API names,
 * classes keep their retail names, and reconstruction devices keep their opus* names.
 */
public final class d extends i {
    /**
     * Per enemy type: body hit-box x offset (from /enemy_spr_box.bin, scaled to the tile width).
     */
    public static byte[] bodyBoxX;
    /** Per enemy type: body hit-box y offset (scaled to the tile height). */
    public static byte[] bodyBoxY;
    /** Per enemy type: body hit-box width. */
    public static byte[] bodyBoxW;
    /** Per enemy type: body hit-box height. */
    public static byte[] bodyBoxH;
    /** Per enemy type: melee attack box x offset (mirrored when facing left). */
    public static byte[] meleeBoxX;
    /** Per enemy type: melee attack box y offset. */
    public static byte[] meleeBoxY;
    /** Per enemy type: melee attack box width. */
    public static byte[] meleeBoxW;
    /** Per enemy type: melee attack box height. */
    public static byte[] meleeBoxH;
    public static final byte[] healthPerVariant = new byte[]{6, 10, 16, 5, 8, 10, 5, 5, 5, 15, 15, 15, 22, 22, 22, 5, 5, 5, 5, 5, 5, 5, 5, 5, 3, 0, 0};
    /** Damage dealt to the player per enemy variant. */
    public static final byte[] playerDamage = new byte[]{1, 3, 4, 4, 5, 6, 1, 3, 1, 1, 3, 1, 1, 3, 1, 1, 3, 4, 2, 3, 4, 2, 3, 4, 0, 2, 4};
    public static final byte[] dropsPerVariant = new byte[]{1, 2, 3, 1, 2, 3, 1, 1, 1, 2, 2, 2, 3, 3, 3, 1, 1, 1, 2, 2, 2, 3, 3, 3, 0};
    /** Shots per burst per enemy variant (shooting types). */
    public static final byte[] burstShots = new byte[]{0, 0, 0, 0, 0, 0, 3, 1, 1, 3, 1, 1, 3, 1, 1, 3, 1, 1, 3, 1, 1, 3, 1, 1};
    /** Knock-back duration (frames) per enemy type. */
    public static final byte[] knockbackFrames = new byte[]{10, 10, 10, 10, 10};
    /** Walking speed per enemy type (8.8 fixed point). */
    public static final short[] walkSpeeds = new short[]{1024, 512, 768, 0, 512};
    /** Attack / shot cooldown (frames) per enemy type. */
    public static final byte[] attackCooldowns = new byte[]{0, 20, 5, 5, 0};
    /** Animation frame sequences [type][animation][step] -> sprite frame (from /enemy.bin). */
    public static byte[][][] animFrames;
    /** Animation length (steps) [type][animation]. */
    public static byte[][] animLengths;
    /** Animation step delay (frames) [type][animation]. */
    public static byte[][] animDelays;
    /** Tile width (copied from g.tileWidth). */
    public static byte tileWidth;
    /** Tile height (copied from g.tileHeight). */
    public static byte tileHeight;
    /** Top of the play area below the HUD (copied from g.hudHeight). */
    public static short hudHeight;
    /** Alternate aim (type 3 turret: selects the +2 sprite frames and the shot direction). */
    public boolean altAim;
    /** Enemy variant (indexes the damage/burst tables playerDamage and burstShots and selects the projectile kind). */
    public byte variant;
    /** Owning game canvas. */
    private g game;
    /** Knock-back counter: signed remaining push frames (sign = direction). */
    public byte knockback;
    /** Blocked by a crate / object in the walking direction (set by physicsStep()). */
    public boolean blockedByCrate;
    /** Ground drops away ahead (edge in the walking direction; set by g_()). */
    public boolean edgeAhead;
    /** Wall or room boundary ahead (set by physicsStep()). */
    public boolean wallAhead;
    /** Raw tile code under the enemy (jump pads -95/-96/-97 trigger jumps). */
    public short tileUnder;
    /** Attack cooldown timer. */
    public short attackCooldown;
    /** Spawn index within the room; used to clear the enemy's bit in g.enemyAliveBits when it is destroyed. */
    public byte spawnIndex;
    /** Shooter state: 0 = patrol / idle, 1 = firing a burst. */
    public byte shooterState;
    /** Turn-around delay (walkers) or shots fired in the current burst (shooters). */
    public short turnTimer;
    /** Home x position (8.8) the patrolling shooter returns toward. */
    public int homeX;
    public boolean hitBySpin;
    /** Sprite frame boxes [type][frame] = {sheet x, sheet y, width, height, x offset, y offset}. */
    public static final byte[][][] spriteBoxes = new byte[][][]{
        new byte[][]{{0, -107, 15, 33, 13, 11}, {59, -81, 16, 25, 12, 19}, {-39, 80, 15, 36, 14, 5}, {-86, 43, 20, 37, 12, 3}, {-102, 42, 16, 35, 14, 3}, {0, 0, 21, 42, 13, 1}},
        new byte[][]{{15, -107, 24, 33, 10, 11}, {64, -112, 24, 31, 10, 13}, {75, -81, 33, 25, 6, 14}, {108, -85, 27, 29, 11, 15}, {-86, 117, 39, 24, 0, 14}, {-49, -88, 27, 29, 11, 15}, {-84, 0, 27, 43, 9, 1}, {-47, 117, 24, 27, 9, 17}, {110, 83, 38, 38, 6, 6}, {-114, 123, 28, 23, 12, 19}, {-119, -92, 29, 36, 8, 8}, {112, -112, 24, 27, 9, 17}, {88, -112, 24, 31, 10, 13}, {39, -107, 24, 33, 10, 11}},
        new byte[][]{{21, 0, 30, 42, 6, 2}, {51, 0, 30, 42, 9, 2}, {81, 0, 31, 42, 8, 2}, {0, 42, 36, 41, 3, 3}, {36, 42, 30, 41, 7, 3}, {112, 0, 30, 42, 7, 2}, {66, 42, 30, 41, 7, 3}, {-114, 0, 30, 42, 7, 2}, {-108, 83, 30, 38, 11, 6}, {0, 83, 34, 44, 3, 0}, {-90, -115, 32, 37, 8, 7}, {96, 42, 30, 41, 6, 3}, {126, 42, 28, 41, 5, 3}, {34, 83, 41, 44, 1, 0}},
        new byte[][]{{-66, 43, 43, 37, 0, 7}, {-78, 80, 39, 37, 0, 7}, {-57, 0, 35, 43, 0, 1}, {75, 83, 35, 39, 0, 5}},
        new byte[][]{{0, 127, 36, 22, 2, 22}, {36, 127, 35, 22, 3, 22}, {72, 122, 35, 22, 3, 22}, {-58, -112, 35, 24, 3, 20}, {107, 122, 35, 22, 3, 22}}
    };

    /** Caches tile/HUD dimensions from g and initialises a free enemy slot. */
    public d(g owner) {
        super();
        this.game = owner;
        this.tileWidth = g.tileWidth;
        this.tileHeight = g.tileHeight;
        this.hudHeight = (short)g.hudHeight;
        this.actorKind = -1;
        this.orientation = 0;
        this.turnTimer = 0;
        this.facingRight = true;
        this.altAim = false;
        this.blockedByCrate = false;
        this.edgeAhead = false;
        this.wallAhead = false;
        this.attackCooldown = 0;
    }

    /** Allocates the shared per-type tables and loads /enemy.bin and /enemy_spr_box.bin. */
    public final void initTables() {
        if (animFrames == null) {
            animFrames = new byte[5][8][5];
        }
        if (animLengths == null) {
            animLengths = new byte[5][8];
        }
        if (animDelays == null) {
            animDelays = new byte[5][8];
        }
        if (bodyBoxX == null) {
            bodyBoxX = new byte[5];
        }
        if (bodyBoxY == null) {
            bodyBoxY = new byte[5];
        }
        if (bodyBoxW == null) {
            bodyBoxW = new byte[5];
        }
        if (bodyBoxH == null) {
            bodyBoxH = new byte[5];
        }
        if (meleeBoxX == null) {
            meleeBoxX = new byte[5];
        }
        if (meleeBoxY == null) {
            meleeBoxY = new byte[5];
        }
        if (meleeBoxW == null) {
            meleeBoxW = new byte[5];
        }
        if (meleeBoxH == null) {
            meleeBoxH = new byte[5];
        }
        this.loadAnimations("/enemy.bin");
        this.loadHitBoxes("/enemy_spr_box.bin");
    }

    /** Screen-space y (pixels) of the enemy's feet, relative to the play area. */
    public final short feetY() {
        return (short)(this.tileRow * tileHeight + (this.rowOffset >> 8) + tileHeight - g.cellHeight);
    }

    /** X position in pixels. */
    public final short pixelX() {
        return (short)(this.posX >> 8);
    }

    /** Tile column just ahead to the right (x + 12). */
    public final byte aheadRightColumn() {
        return (byte)(((this.posX >> 8) + 12) / tileWidth);
    }

    /** Tile column just ahead to the left (x - 12). */
    public final byte aheadLeftColumn() {
        return (byte)(((this.posX >> 8) - 12) / tileWidth);
    }

    /**
     * Floor height (pixels) below the enemy: the first solid tile row underneath, or the top of
     * a crate standing in this column, whichever is higher. With recordTile == true also records
     * the raw tile code at the enemy's position in tileUnder.
     */
    public final int floorHeight(boolean recordTile) {
        int column = (this.posX >> 8) / tileWidth;
        int index;
        int row = 0;
        int width = g.imgCrates.getWidth();
        int stepped = 0;
        if (recordTile) {
            this.tileUnder = (this.game.levelMap).rawTile(column, this.tileRow);
        }
        index = this.tileRow + 1;
        for (; index < 18; ++index) {
            if ((((c)null).solidMasks[column] & 1 << index) > 0) {
                index *= tileHeight;
                stepped = 1;
                break;
            }
        }
        if (stepped == 0) {
            index = 18 * tileHeight;
        }
        column = this.posX >> 8;
        for (int scan = 0; scan < 50; ++scan) {
            if (this.game.crateType[scan] != -1) {
                if (this.game.crateX[scan] <= column + 8) {
                if (this.game.crateX[scan] + width < column - 8) {
                    continue;
                }
                if (this.game.crateY[scan] < index) {
                    if (this.game.crateY[scan] > this.tileRow * tileHeight) {
                        index = this.game.crateY[scan];
                    }
                }
                }
            }
        }
        return index;
    }

    /** True if the enemy overlaps a crate / box object on its own row. */
    public final boolean overlapsCrate() {
        int n = g.imgCrates.getWidth();
        int n2 = g.imgCrates.getHeight() >> 3;
        byte by = tileWidth;
        byte by2 = tileHeight;
        boolean bl = false;
        int n3 = this.posX >> 8;
        int n4 = this.feetY() + g.cellHeight - tileHeight;
        boolean bl2 = false;
        int n5 = by2 - 1;
        int n6 = 49;
        for (; n6 >= 0; --n6) {
            if (this.game.crateType[n6] < 0) {
                continue;
            }
            if (this.abs((int)(this.game.crateX[n6] - n3)) <= by) {
                if (this.game.crateY[n6] != n4) {
                    continue;
                }
                if (this.game.boxesOverlap(this.game.crateX[n6], this.game.crateY[n6], n, n2, n3 - 8, n4, 16, n5)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Physics step: gravity and landing, bouncing off the player, and probing ahead for walls, edges and crates.
     */
    public final void physicsStep() {
        int first = this.floorHeight(true);
        first /= tileHeight;
        this.wallAhead = false;
        this.blockedByCrate = false;
        this.edgeAhead = false;
        if ((this.tileRow + 1 < first && this.actorKind != 3) || this.animId == 2) {
            this.setAnimation((byte)2);
            this.speedY += 256;
            this.rowOffset += this.speedY;
            this.normaliseRow();
            if (this.tileRow + 1 == first && this.speedY > 0) {
                this.setAnimation((byte)0);
                this.speedY = 0;
                this.rowOffset = 0;
            }
            if (this.game.boxesOverlap(this.pixelX() - bodyBoxX[this.actorKind], this.feetY() + bodyBoxY[this.actorKind], bodyBoxW[this.actorKind], bodyBoxH[this.actorKind], this.game.player.pixelX() - 11, this.game.player.pixelY() + g.playerDrawOffsetY + 7, 18, 37)) {
                this.speedX = 0;
                if (this.speedY > 0) {
                    this.speedY = (short)(-this.speedY / 2);
                    if (this.game.player.pixelX() < this.pixelX()) {
                        this.speedX = 510;
                        this.facingRight = false;
                    } else {
                        this.speedX = -510;
                        this.facingRight = true;
                    }
                } else {
                    this.speedY = (short)-this.speedY;
                }
            }
        }
        if (this.speedX != 0) {
            this.aheadColumn = this.facingRight ? this.aheadRightColumn() : this.aheadLeftColumn();
            this.aheadRow = this.tileRow;
            int savedAb = this.posX;
            this.posX += this.facingRight ? tileWidth << 7 : -(tileWidth << 7);
            int second = this.floorHeight(false);
            second /= tileHeight;
            this.edgeAhead = false;
            if (second > first) {
                this.edgeAhead = true;
            }
            if (this.knockback != 0 && this.edgeAhead) {
                this.speedX = 0;
            }
            this.posX = savedAb;
            if (this.aheadColumn > 0 && this.aheadColumn < 27 && this.game.levelMap.isSolid(this.aheadColumn, this.aheadRow)) {
                this.posX += this.speedX;
                if (this.overlapsCrate()) {
                    this.posX -= this.speedX;
                    if (this.animId != 2) {
                        this.blockedByCrate = true;
                        return;
                    }
                }
            } else {
                this.wallAhead = true;
            }
        }
    }


    /**
     * Per-frame AI by enemy type: 0 = kamikaze walker (explodes next to the player),
     * 1 = melee jumper, 2 = patrolling shooter, 3 = stationary turret, 4 = boar (an enemy turned
     * into a pig by the Boar-Zooka; harmless, not counted as an enemy).
     * Inactive while the player is out of range.
     */
    public final void updateAi() {
        if (this.animId == 5) {
            if (this.animMode == 2) {
                this.actorKind = (byte)-1;
            }
            return;
        }
        if (this.animId == 4 && this.knockback == 0) {
            if (++this.stateTimer > knockbackFrames[this.actorKind]) {
                this.speedX = 0;
                this.animMode = (byte)2;
                this.setAnimation((byte)0);
            }
            return;
        }
        if (this.animId == 7 || this.animId == 6) {
            return;
        }
        if (this.abs(this.game.player.pixelX() - this.pixelX()) > 5 * tileWidth ||
            this.abs(this.game.player.pixelY() - this.feetY()) > 7 * tileHeight) {
            this.speedX = this.speedY = 0;
            return;
        }
        if (this.actorKind == 3 &&
            (this.abs(this.game.player.pixelX() - this.pixelX()) > 3 * tileWidth + (tileWidth >> 1) ||
             this.abs(this.game.player.pixelY() - this.feetY()) > 5 * tileHeight)) {
            return;
        }
        this.physicsStep();
        if (this.knockback != 0 && this.actorKind != 3) {
            this.speedX = (short)(this.knockback << 8);
            this.speedY = 0;
            this.setAnimation((byte)4);
            if (this.knockback > 0) {
                --this.knockback;
                this.facingRight = true;
            } else {
                ++this.knockback;
                this.facingRight = false;
            }
            if (this.wallAhead) {
                this.speedX = 0;
            }
            return;
        }
        this.hitBySpin = false;
        int n = this.game.player.pixelX() - this.pixelX();
        int n2 = this.game.player.pixelY() + tileHeight - g.cellHeight - this.feetY();
        int by = tileWidth;
        int n3 = tileWidth * 3;
        if (this.actorKind == 0) {
            if (this.blockedByCrate) {
                this.facingRight = !this.facingRight;
                this.turnTimer = (short)10;
            } else if (this.wallAhead) {
                if (this.tileUnder == -96) {
                    this.speedY = (short)-510;
                    this.setAnimation((byte)2);
                    return;
                }
                this.facingRight = !this.facingRight;
                this.turnTimer = (short)10;
            } else if (this.edgeAhead) {
                if (this.tileUnder == -97) {
                    this.speedY = (short)-768;
                    this.speedX = (short)(this.walkSpeeds[this.actorKind] << 2);
                    if (!this.facingRight) {
                        this.speedX *= -1;
                    }
                    this.setAnimation((byte)2);
                    return;
                }
                if (this.tileUnder != -96) {
                    this.facingRight = !this.facingRight;
                    this.turnTimer = (short)10;
                }
            }
            if (this.animId == 2) {
                return;
            }
            if (n < n3 && n > by && this.abs(n2) < tileHeight && this.turnTimer == 0) {
                if (this.animId != 1) {
                    this.setAnimation((byte)1);
                }
                this.speedX = this.walkSpeeds[this.actorKind];
                this.facingRight = true;
            } else if (n < -by && n > -n3 && this.abs(n2) < tileHeight && this.turnTimer == 0) {
                if (this.animId != 1) {
                    this.setAnimation((byte)1);
                }
                this.speedX = (short)(-this.walkSpeeds[this.actorKind]);
                this.facingRight = false;
            } else if (this.abs(n) <= by && n2 == 0) {
                this.speedX = 0;
                this.game.spawnEnemyShot(this.posX, this.feetY() + (tileHeight >> 1) << 8, 21,
                         this.facingRight, false, 0, this.variant);
                this.actorKind = (byte)-1;
                int ag = this.game.currentRoom;
                if (this.game.arenaActive == true) {
                    ag = 0;
                }
                int index = this.spawnIndex + ag * 10 >> 5;
                this.game.enemyAliveBits[index] &=
                    ~(1 << this.spawnIndex + ag * 10 - (index << 5));
            } else {
                this.startWalking();
            }
            if (this.turnTimer != 0) {
                --this.turnTimer;
                return;
            }
        } else if (this.actorKind == 1 && this.animId != 2) {
            if (this.tileUnder == -95) {
                if (n2 < 0 && this.abs(n) > tileWidth / 2) {
                    this.speedY = (short)-3584;
                    this.speedX = 0;
                    this.setAnimation((byte)2);
                }
            } else if (this.blockedByCrate) {
                this.facingRight = !this.facingRight;
            } else if (this.wallAhead) {
                if (this.tileUnder == -96 && this.abs(n) > tileWidth / 2) {
                    this.speedY = (short)-1700;
                    this.setAnimation((byte)2);
                    return;
                }
                this.facingRight = !this.facingRight;
            } else if (this.edgeAhead) {
                if (this.tileUnder == -97 && this.abs(n) > tileWidth / 2) {
                    this.speedY = (short)-3584;
                    this.speedX = (short)(this.walkSpeeds[this.actorKind] << 2);
                    if (!this.facingRight) {
                        this.speedX *= -1;
                    }
                    this.setAnimation((byte)2);
                    return;
                }
                if (this.tileUnder != -96) {
                    this.facingRight = !this.facingRight;
                }
            }
            if (this.animId == 2) {
                return;
            }
            if (this.abs(n) <= by && this.abs(n2) <= by &&
                this.abs(n2) < tileHeight) {
                this.speedX = 0;
                this.meleeAttack();
                return;
            }
            this.startWalking();
            return;
        } else if (this.actorKind == 2) {
            if (this.blockedByCrate) {
                this.speedX = 0;
                this.animMode = (byte)1;
                this.setAnimation((byte)0);
                this.facingRight = !this.facingRight;
            } else if (this.wallAhead) {
                this.speedX = 0;
                this.animMode = (byte)1;
                this.setAnimation((byte)0);
                this.facingRight = !this.facingRight;
            } else if (this.edgeAhead) {
                this.speedX = 0;
                this.animMode = (byte)1;
                this.setAnimation((byte)0);
                this.facingRight = !this.facingRight;
            }
            int n7 = this.abs(this.homeX - this.posX) >> 8;
            if (this.shooterState == 0) {
                if ((n > 0 && this.facingRight == true || n < 0 && !this.facingRight) &&
                    this.abs(n2) < tileHeight && this.abs(n) < tileWidth * 3) {
                    this.speedX = 0;
                    this.shooterState = (byte)1;
                    this.turnTimer = 0;
                    this.attackCooldown = 0;
                    return;
                }
                if ((this.homeX > this.posX && !this.facingRight ||
                     this.homeX < this.posX && this.facingRight == true) && n7 >= tileWidth * 3) {
                    this.speedX = 0;
                    this.animMode = (byte)1;
                    this.setAnimation((byte)0);
                    this.facingRight = !this.facingRight;
                    return;
                }
                if (this.animId != 0 || (this.animMode == 2 && this.animId == 0)) {
                    this.startWalking();
                }
            } else if (this.shooterState == 1) {
                if (this.turnTimer < burstShots[this.variant]) {
                    this.fireBurstShot();
                    return;
                }
                if (this.animMode == 2 && this.animId == 0) {
                    this.shooterState = 0;
                    return;
                }
                if (this.animId != 0) {
                    this.animMode = (byte)1;
                    this.setAnimation((byte)0);
                    return;
                }
            }
        } else if (this.actorKind == 3) {
            if (this.shooterState == 0) {
                if (this.turnTimer < burstShots[this.variant]) {
                    this.fireBurstShot();
                } else {
                    this.turnTimer = 0;
                    this.shooterState = (byte)1;
                }
            }
            if (this.shooterState == 1) {
                if (this.turnTimer++ > 10) {
                    if (this.facingRight && !this.altAim) {
                        this.altAim = true;
                    } else if (this.facingRight) {
                        this.altAim = this.facingRight = false;
                    } else {
                        this.facingRight = true;
                    }
                    this.shooterState = 0;
                    this.turnTimer = 0;
                }
            }
        } else if (this.actorKind == 4) {
            if (this.blockedByCrate) {
                this.facingRight = !this.facingRight;
            } else if (this.wallAhead) {
                this.facingRight = !this.facingRight;
            } else if (this.edgeAhead) {
                this.facingRight = !this.facingRight;
            }
            this.startWalking();
        }
    }

    /** Advances the animation timer/frame; one-shot animations end in state animMode == 2. */
    public final void animate() {
        if (this.animMode == 2) {
            return;
        }
        if (this.actorKind == -1) {
            this.animFrame = this.animTimer = 0;
            return;
        }
        ++this.animTimer;
        if (this.animTimer > animDelays[this.actorKind][this.animId]) {
            this.animTimer = 0;
            ++this.animFrame;
            if (this.animFrame >= animLengths[this.actorKind][this.animId]) {
                if (this.animMode == 0) {
                    this.animFrame = 0;
                } else {
                    --this.animFrame;
                    this.animMode = (byte)2;
                }
                if (this.animId == 6 || this.animId == 7) {
                    this.setAnimation((byte)0);
                }
                if (this.animId == 3) {
                    this.setAnimation((byte)0);
                    if (this.actorKind == 1) {
                        this.facingRight = !this.facingRight;
                        this.turnTimer = (short)20;
                    }
                }
            }
            this.game.repaintRequested = true;
        }
    }

    /**
     * Draws the enemy at camera offset (camX, camY), honouring the room rotation g.drawTransform via drawRegion transforms.
     */
    public final void draw(Graphics graphics, int slot, int layer, int camX, int camY) {
        int n5;
        int n6;
        boolean unused = false;
        int n7;
        byte[] array;
        int n11;
        int n12;
        g.camX = (short)camX;
        g.camY = (short)camY;
        n5 = this.pixelX() + g.camX - (g.cellWidth >> 1);
        n6 = this.feetY() + g.camY;
        if (n5 < -g.cellWidth || n5 >= g.screenWidth || n6 < -g.cellHeight || n6 >= g.screenHeight) {
            return;
        }
        if ((layer & 1) > 0 && n6 + g.cellHeight > hudHeight && n6 < g.screenHeight) {
            n7 = animFrames[this.actorKind][this.animId][this.animFrame];
            if (this.altAim) {
                n7 += 2;
            }
            array = spriteBoxes[this.actorKind][n7];
            if (this.orientation == 0 && this.facingRight) {
                graphics.drawImage(g.imgEnemyFrames[this.actorKind][n7], n5 + array[4], n6 + array[5], 20);
                return;
            }
            this.game.setDrawTransform(slot);
            switch (this.game.drawTransform) {
                case 1: {
                    n11 = array[5];
                    n12 = g.cellWidth - (array[4] + array[2]);
                    graphics.drawRegion(g.imgEnemyFrames[this.actorKind][n7], 0, 0, (int)array[2], (int)array[3], 6, n5 + n11, n6 + n12, 20);
                    return;
                }
                case 2: {
                    n5 += g.cellWidth - array[4] - array[2];
                    n6 += g.cellHeight - array[5] - array[3];
                    graphics.drawRegion(g.imgEnemyFrames[this.actorKind][n7], 0, 0, (int)array[2], (int)array[3], 3, n5, n6, 20);
                    return;
                }
                case 3: {
                    n11 = g.cellHeight - (array[5] + array[3]);
                    n12 = array[4];
                    graphics.drawRegion(g.imgEnemyFrames[this.actorKind][n7], 0, 0, (int)array[2], (int)array[3], 5, n5 + n11, n6 + n12, 20);
                    return;
                }
                case 4: {
                    n5 += array[4];
                    n6 += g.cellHeight - array[5] - array[3];
                    graphics.drawRegion(g.imgEnemyFrames[this.actorKind][n7], 0, 0, (int)array[2], (int)array[3], 1, n5, n6, 20);
                    return;
                }
                case 5: {
                    n5 += g.cellWidth - array[4] - array[2];
                    n6 += array[5];
                    graphics.drawRegion(g.imgEnemyFrames[this.actorKind][n7], 0, 0, (int)array[2], (int)array[3], 2, n5, n6, 20);
                    return;
                }
                case 6: {
                    n11 = g.cellHeight - (array[5] + array[3]);
                    n12 = g.cellWidth - (array[4] + array[2]);
                    graphics.drawRegion(g.imgEnemyFrames[this.actorKind][n7], 0, 0, (int)array[2], (int)array[3], 7, n5 + n11, n6 + n12, 20);
                    return;
                }
                case 7: {
                    n11 = array[5];
                    n12 = array[4];
                    graphics.drawRegion(g.imgEnemyFrames[this.actorKind][n7], 0, 0, (int)array[2], (int)array[3], 4, n5 + n11, n6 + n12, 20);
                    break;
                }
            }
        }
    }


    /** Starts walking in the facing direction at the type's speed. */
    public final void startWalking() {
        if (this.animId != 1) {
            this.setAnimation((byte)1);
        }
        this.animMode = 0;
        this.speedX = (short)(this.facingRight ? walkSpeeds[this.actorKind] : -walkSpeeds[this.actorKind]);
    }

    /**
     * Melee attack: faces the player and, when the cooldown expires, damages and knocks back the player on overlap.
     */
    public final void meleeAttack() {
        if (this.animId == 3) {
            return;
        }
        if (this.game.player.animId == 10) {
            return;
        }
        this.facingRight = false;
        if (this.game.player.pixelX() - this.pixelX() > 0) {
            this.facingRight = true;
        }
        if (--this.attackCooldown < 0) {
            this.attackCooldown = (short)attackCooldowns[this.actorKind];
            if (this.game.levelIndex != 0) {
                this.setAnimation((byte)3);
            }
            if (this.game.player.blinkTimer > 0) {
                return;
            }
            if (this.game.boxesOverlap(this.game.player.pixelX() - 11, this.game.player.pixelY() + 7 + g.playerDrawOffsetY, 18, 37, this.pixelX() + (this.facingRight ? meleeBoxX[this.actorKind] : -meleeBoxX[this.actorKind] - meleeBoxW[this.actorKind]), this.feetY() + meleeBoxY[this.actorKind], meleeBoxW[this.actorKind], meleeBoxH[this.actorKind]) && this.game.levelIndex != 0) {
                if (!g.invulnerable) {
                    this.game.player.health -= playerDamage[this.variant];
                }
                this.game.challengeMultiplier = 1;
                this.game.player.setAnimation((byte)9);
                this.game.player.stateTimer = 0;
                if (this.game.player.jumpState == 2) {
                    this.game.player.jumpState = 1;
                }
                this.game.hudDirty = true;
            }
        }
    }


    /** Fires one shot of the current burst; the projectile kind depends on the variant w. */
    public final void fireBurstShot() {
        if (--this.attackCooldown < 0) {
            this.setAnimation((byte)3);
            this.animMode = 1;
            if (this.variant == 6 || this.variant == 9 || this.variant == 12 || this.variant == 15 || this.variant == 18 || this.variant == 21) {
                this.game.spawnEnemyShot(this.posX, (this.feetY() + (g.cellHeight >> 1)) << 8, 0, this.facingRight, this.altAim, this.orientation, this.variant);
            } else if (this.variant == 7 || this.variant == 10 || this.variant == 13 || this.variant == 16 || this.variant == 19 || this.variant == 22) {
                this.game.spawnEnemyShot(this.posX, (this.feetY() + (g.cellHeight >> 1)) << 8, 6, this.facingRight, this.altAim, this.orientation, this.variant);
            } else if (this.variant == 7 || this.variant == 10 || this.variant == 13) {
                this.game.spawnEnemyShot(this.posX, (this.feetY() + (g.cellHeight >> 1)) << 8, 18, this.facingRight, this.altAim, this.orientation, this.variant);
            } else if (this.variant == 17 || this.variant == 20 || this.variant == 23) {
                this.game.spawnEnemyShot(this.posX, (this.feetY() + (g.cellHeight >> 1)) << 8, 3, this.facingRight, this.altAim, this.orientation, this.variant);
            }
            this.attackCooldown = (short)attackCooldowns[this.actorKind];
            ++this.turnTimer;
        }
    }

    /**
     * Reads length bytes scaled from the 44-pixel design size to the tile width (horizontal) or height.
     */
    private void readScaled(InputStream stream, byte[] bytes, int length, boolean horizontal) {
        byte scale;
        if (horizontal) {
            scale = ((g)null).cellWidth;
        } else {
            scale = ((g)null).cellHeight;
        }
        try {
            int index = 0;
            while (index < length) {
                bytes[index] = (byte)stream.read();
                bytes[index] = (byte)(bytes[index] * scale / 44);
                ++index;
            }
        } catch (java.io.IOException exception) {
        }
    }

    /** Loads the per-type body and attack hit boxes from resource {@code resource}. */
    public final void loadHitBoxes(String resource) {
        InputStream unused;
        byte[] bytes = new byte[5];
        try {
            InputStream stream = bytes.getClass().getResourceAsStream(resource);
            this.readScaled(stream, bytes, 5, true);
            System.arraycopy(bytes, 0, bodyBoxX, 0, bytes.length);
            this.readScaled(stream, bytes, 5, false);
            System.arraycopy(bytes, 0, bodyBoxY, 0, bytes.length);
            this.readScaled(stream, bytes, 5, true);
            System.arraycopy(bytes, 0, bodyBoxW, 0, bytes.length);
            this.readScaled(stream, bytes, 5, false);
            System.arraycopy(bytes, 0, bodyBoxH, 0, bytes.length);
            this.readScaled(stream, bytes, 5, true);
            System.arraycopy(bytes, 0, meleeBoxX, 0, bytes.length);
            this.readScaled(stream, bytes, 5, false);
            System.arraycopy(bytes, 0, meleeBoxY, 0, bytes.length);
            this.readScaled(stream, bytes, 5, true);
            System.arraycopy(bytes, 0, meleeBoxW, 0, bytes.length);
            this.readScaled(stream, bytes, 5, false);
            System.arraycopy(bytes, 0, meleeBoxH, 0, bytes.length);
            stream.close();
        } catch (java.io.IOException exception) { }
    }

    /** Loads the animation tables animFrames, animLengths, animDelays from resource {@code resource}. */
    private void loadAnimations(String resource) {
        byte first;
        byte second;
        byte[] bytes = new byte[5];
        try {
            InputStream stream = bytes.getClass().getResourceAsStream(resource);
            int outer = 0;
            while (outer < 5) {
                int inner = 0;
                while (inner < 8) {
                    first = (byte)stream.read();
                    second = (byte)stream.read();
                    animLengths[outer][inner] = first;
                    animDelays[outer][inner] = second;
                    this.readBytes(stream, bytes, first);
                    System.arraycopy(bytes, 0, animFrames[outer][inner], 0, first);
                    ++inner;
                }
                ++outer;
            }
            stream.close();
        } catch (java.io.IOException exception) { }
    }

    /** Reads length raw bytes from stream. */
    private void readBytes(InputStream stream, byte[] bytes, int length) {
        try {
            int index = 0;
            while (index < length) {
                bytes[index] = (byte)stream.read();
                index++;
            }
        } catch (java.io.IOException exception) {
        }
    }

    /** Absolute value. */
    public final int abs(int value) {
        if (value < 0) {
            return value * -1;
        }
        return value;
    }
}
