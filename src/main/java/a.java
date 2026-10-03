import java.io.InputStream;
import javax.microedition.lcdui.Graphics;

/*
 * a - Player (Ratchet).
 *
 * Extends the shared actor base i. Owns the weapon inventory (current weapon,
 * per-weapon ammo, level and experience), player animation tables, the player
 * sprite set and player-specific movement/collision/drawing.
 *
 * Naming: this is reconstructed, output-exact Java source (not recovered original source).
 * Member names are descriptive source names; config/d-preobf.map maps every one of them
 * back to its retail (obfuscated) runtime name. Renames are always group-complete (all
 * members sharing a retail name), which reproduces ProGuard's retail constant-pool
 * ordering; see cleanup/docs/NAMING_SCHEME.md. Library overrides keep their API names,
 * classes keep their retail names, and reconstruction devices keep their opus* names.
 */
public final class a extends i {
    /** Weapon-in-hand x offset per player sprite frame (44 = weapon not drawn). */
    public static final byte[] weaponOffsetX = { -2, -2, -2, 2, 0, -1, 1, -1, -2, 0, 0, -1, 0, 44, 44, 44, 44, 44, 44, -2, -3, 0, 0, 0, 44, 44, 44, 44 };
    /** Weapon-in-hand y offset per player sprite frame (44 = weapon not drawn). */
    public static final byte[] weaponOffsetY = { 15, 13, 13, 12, 13, 14, 15, 7, 7, 12, 17, 20, 12, 44, 44, 44, 44, 44, 44, 14, 15, 12, 13, 13, 44, 44, 44, 44 };
    public static final byte[][] frameBoxes = { { 0, 0, 20, 35, 11, 9 }, { 0, 119, 20, 36, 11, 8 }, { 20, 119, 20, 36, 11, 8 }, { 20, 0, 25, 35, 8, 9 }, { 0, -101, 30, 31, 5, 8 }, { 108, 0, 27, 37, 7, 7 }, { 57, 120, 23, 34, 9, 10 }, { 0, 79, 20, 40, 10, 1 }, { 30, -101, 25, 31, 9, 4 }, { -121, 0, 23, 39, 7, 5 }, { 68, -102, 23, 29, 10, 15 }, { 40, 119, 17, 36, 13, 8 }, { 91, 35, 20, 43, 10, 1 }, { 0, 35, 30, 44, 0, 0 }, { 111, 39, 44, 42, 0, 2 }, { 108, 121, 43, 28, 1, 16 }, { 125, 81, 24, 36, 8, 2 }, { 20, 79, 44, 40, 0, 0 }, { 64, 79, 36, 41, 7, 3 }, { 45, 0, 26, 35, 9, 9 }, { 80, 120, 28, 34, 7, 10 }, { 100, 81, 25, 40, 6, 3 }, { 30, 35, 31, 44, 1, 0 }, { 61, 35, 31, 44, 1, 0 }, { 91, -102, 24, 33, 11, 9 }, { 71, 0, 37, 35, 7, 9 }, { 115, -107, 41, 27, 2, 16 }, { 115, -80, 40, 11, 1, 33 } };
    /** Held-weapon sprite strips in g.imgWeapons per weapon 1-7: {sheet y, height, y offset}. */
    public static final byte[][] weaponStrips = { { 0, 12, 5 }, { 12, 10, 8 }, { 22, 13, 5 }, { 35, 12, 7 }, { 47, 17, 4 }, { 64, 14, 5 }, { 78, 13, 6 } };
    /** Maximum ammo per weapon and weapon level: index weapon * 3 + level. */
    public static final short[] maxAmmo = { 0, 0, 0, 100, 150, 200, 35, 45, 60, 25, 30, 40, 40, 55, 70, 200, 250, 300, 1, 1, 1, 70, 90, 110 };
    public static final short[] startAmmo = { 0, 50, 20, 15, 20, 100, 1, 30 };
    /** Ammo restored per ammo pick-up, per weapon. */
    public static final short[] ammoPerPickup = { 0, 15, 7, 5, 8, 30, 1, 10 };
    public static final byte[][] fireDelays = { { 0, 4, 16, 20, 16, 5, 4, 5 }, { 0, 4, 16, 20, 16, 5, 4, 5 }, { 0, 4, 16, 20, 16, 5, 4, 5 } };
    public static final int[] weaponPrices = { 50, 800, 2500, 18000, 0 };
    /** Wrench damage per wrench level (weaponLevels[0]; the charged swing uses weaponLevels[1]). */
    public static final byte[] wrenchDamage = { 3, 8, 8 };
    /** Owning game canvas. */
    private g game;
    /** Tile width (copied from g.tileWidth). */
    public static byte tileWidth;
    /** Tile height (copied from g.tileHeight). */
    public static byte tileHeight;
    /** Top of the play area below the HUD (copied from g.hudHeight). */
    public static short hudHeight;
    /** Maximum fall speed (half a tile per frame, 8.8). */
    public static short maxFallSpeed;
    /**
     * Current horizontal run speed (8.8): 1536 normal, 768 while in jump state 2, 384 while attacking.
     */
    public static short runSpeed;
    /** Wrench hit box x offset per attack kind attackKind (0 = swing, 1 = charged). */
    public static final byte[] wrenchBoxX = { 14, -11 };
    /** Wrench hit box y offset per attack kind attackKind. */
    public static final byte[] wrenchBoxY = { 4, 17 };
    /** Wrench hit box width per attack kind attackKind. */
    public static final byte[] wrenchBoxW = { 10, 20 };
    /** Wrench hit box height per attack kind attackKind. */
    public static final byte[] wrenchBoxH = { 10, 50 };
    /**
     * Jump state: -1 = on the ground, 0 = first jump, 1 = rising, 2 = second (glide/double) jump.
     */
    public byte jumpState;
    public byte platformIgnoreTimer;
    /**
     * Swingshot state: -2 idle, -1 released, 0 hook flying, 1 pulled to target, 2-5 swinging around the anchor.
     */
    public byte grappleState;
    /** Swingshot / special-move timer (frames). */
    public byte grappleTimer;
    /** Swingshot anchor x (8.8). */
    public int anchorX;
    /** Swingshot anchor y (8.8). */
    public int anchorY;
    /** Swingshot hook tip x (8.8). */
    public int hookTipX;
    /** Swingshot hook tip y (8.8). */
    public int hookTipY;
    /** Swingshot hook / pull x step per frame (8.8). */
    public int hookStepX;
    /** Swingshot hook / pull y step per frame (8.8). */
    public int hookStepY;
    /** Attack combo timer (-1 = idle; advanced by tickCombo()). */
    public byte comboTimer;
    /** Wrench attack kind: 0 = normal swing, 1 = charged / spin attack. */
    public byte attackKind;
    /** Invulnerability blink counter (sprite drawn on even values). */
    public byte blinkTimer;
    /** Raw tile code of the swingshot target (97 = pull-to target). */
    public byte grappleTile;
    /** Standing on a moving platform (set by floorHeight(boolean)). */
    public boolean onPlatform;
    public short platformRideSpeed;
    /** Reached a room edge this frame; checkTileInteractions() then requests the room change. */
    private boolean atRoomEdge;
    /** Animation frame sequences [0][animation][step] -> sprite frame (from /player.bin). */
    public static byte[][][] animFrames;
    /** Animation length (steps) [0][animation]. */
    public static byte[][] animLengths;
    /** Animation step delay (frames) [0][animation]. */
    public static byte[][] animDelays;
    /** Current weapon: 0 Wrench, 1 Lancer, 2 Gravity Bomb, 3 Mini Rocket, 4 Defragmenter, 5 Circuit Jammer, 6 Boar-Zooka, 7 R.Y.N.O. */
    public byte weapon;
    /** Ammo per weapon. */
    public short[] ammo;
    public short[] weaponXp;
    /** Owned-weapon bitmask (bit n = weapon n). */
    public byte ownedWeapons;
    /** Upgrade level (0-2) per weapon. */
    public byte[] weaponLevels;
    public int allAmmoPrice;
    public byte savedWeapon;
    public short[] savedWeaponXp;
    public short[] savedAmmo;
    public byte[] savedWeaponLevels;
    public int moveStepX;
    public int moveStepY;
    public int moveEndY;
    /*
     * Reconstruction devices (retained; removed from final class output).
     * Four instance compile-time constants, one per distinct value. When g reads one of
     * them through an instance (this.player.opusK; retail field g.as), Sun javac 1.4.1 emits
     * 'getfield as; getClass; pop; sipush K', the exact null-check fingerprint found at
     * 13 sites in retail g.b(B)V, g.c(B)V, g.d(B)V and g.A(II)V. javac does not fold
     * arithmetic on instance-qualified constants, hence one field per value. The
     * ProGuard 3.2 optimizer removes these write-only fields (and their constructor
     * stores), so they never appear in the shipped a.class. Do not delete or inline.
     */
    final short opusK = -3584;
    final short opusKa = -3172;
    final short opusKb = 3584;
    final short opusKc = 3072;

    /** Caches tile/HUD dimensions and initialises the player (full health 20, facing right). */
    public a(g owner) {
        super();
        this.game = owner;
        tileWidth = g.tileWidth;
        tileHeight = g.tileHeight;
        hudHeight = g.hudHeight;
        runSpeed = 1536;
        this.jumpState = -1;
        this.grappleState = -2;
        this.comboTimer = -1;
        this.attackKind = 0;
        this.onPlatform = false;
        this.platformIgnoreTimer = 0;
        this.platformRideSpeed = 0;
        this.atRoomEdge = false;
        this.ammo = new short[8];
        this.weaponXp = new short[8];
        this.weaponLevels = new byte[8];
        this.savedAmmo = new short[8];
        this.savedWeaponXp = new short[8];
        this.savedWeaponLevels = new byte[8];
        this.speedY = 0;
        this.speedX = 0;
        this.orientation = 0;
        this.facingRight = true;
        this.health = 20;
        maxFallSpeed = (short)(tileHeight >> 1 << 8);
    }

    /** Allocates the animation tables and loads /player.bin. */
    public final void initAnimations() {
        if (animFrames == null) {
            animFrames = new byte[1][15][4];
        }
        if (animLengths == null) {
            animLengths = new byte[1][15];
        }
        if (animDelays == null) {
            animDelays = new byte[1][15];
        }
        this.loadAnimationFile("/player.bin");
    }

    /** X position in pixels. */
    public final short pixelX() {
        return (short)(this.posX >> 8);
    }

    /** Y position in pixels (top of the player's tile cell). */
    public final short pixelY() {
        return (short)(this.tileRow * tileHeight + (this.rowOffset >> 8));
    }

    /** Tile column of the right edge (x + 8). */
    public final byte rightEdgeColumn() {
        return (byte)(((this.posX >> 8) + 8) / tileWidth);
    }

    /** Tile column of the left edge (x - 8). */
    public final byte leftEdgeColumn() {
        return (byte)(((this.posX >> 8) - 8) / tileWidth);
    }

    /**
     * Floor height (pixels) under the player, including moving platforms (sets H when a platform is higher).
     */
    public final short floorHeight(boolean hanging) {
        int y = 17 * tileHeight;
        int h = 4 * tileHeight / 44;
        if (hanging) {
            h = 18 * tileHeight / 44;
        }
        if (this.rowOffset > h << 8) {
            if ((c.solidMasks[this.rightEdgeColumn()] & 1 << this.tileRow + 2) > 0 || (c.solidMasks[this.leftEdgeColumn()] & 1 << this.tileRow + 2) > 0) {
                y = (this.tileRow + 1) * tileHeight;
            }
        } else if ((c.solidMasks[this.rightEdgeColumn()] & 1 << this.tileRow + 1) > 0 || (c.solidMasks[this.leftEdgeColumn()] & 1 << this.tileRow + 1) > 0) {
            y = this.tileRow * tileHeight;
        }
        int cam = this.game.crateTopUnderPlayer();
        short lim = (short)this.game.minOf(y, cam);
        short top = this.game.platformTopUnderPlayer();
        this.onPlatform = false;
        if (top < lim) {
            this.onPlatform = true;
            return top;
        }
        return lim;
    }

    /** True if the player stands on ladder/climb tile 19 (with edge tolerance). */
    public final boolean onClimbTile() {
        int x;
        int col = (x = this.posX >> 8) / tileWidth;
        int rem;
        if ((rem = x % tileWidth) < 10) {
            if (this.game.levelMap.roomTiles[col - 1][this.tileRow + 1] != 19) {
                return false;
            }
        } else if (rem > tileWidth - 10 && this.game.levelMap.roomTiles[col + 1][this.tileRow + 1] != 19) {
            return false;
        }
        return this.game.levelMap.roomTiles[col][this.tileRow + 1] == 19;
    }

    /** True if there is no floor ahead on the left (about to step off a ledge). */
    public final boolean noFloorLeft() {
        return !this.game.levelMap.isSolid(this.tileColumn() - 1, this.tileRow + 1) && ((this.posX >> 8) - 8) % tileWidth < 8;
    }

    /** True if there is no floor ahead on the right (about to step off a ledge). */
    public final boolean noFloorRight() {
        return !this.game.levelMap.isSolid(this.tileColumn() + 1, this.tileRow + 1) && ((this.posX >> 8) + 8) % tileWidth > tileWidth - 8;
    }

    /**
     * Slide state (animation 6): follows the slide vector until it lands; jump input cancels into a jump.
     */
    public final void updateSlide() {
        if (this.game.heldAction == 1) {
            this.setAnimation((byte)2);
            this.game.heldAction = this.facingRight ? 5 : 2;
            this.speedY = 3584;
            this.speedX = 0;
            this.jumpState = 0;
            this.game.handleKeyRelease(0, 1);
            return;
        }
        this.posX += this.moveStepX;
        this.rowOffset += this.moveStepY;
        this.normaliseRow();
        if (this.pixelY() + g.cellHeight > this.moveEndY) {
            this.setAnimation((byte)3);
            this.speedX = 0;
            this.game.bufferedAction = this.game.heldAction = 0;
        }
    }

    /** Swingshot update: hook flight, pull-to target, and swinging around the anchor (state grappleState). */
    public final void updateGrapple() {
        if (this.grappleState == 0) {
            int lim = this.floorHeight(true);
            this.speedY -= 128;
            this.rowOffset -= this.speedY;
            this.normaliseRow();
            if (this.pixelY() >= lim) {
                this.tileRow = (byte)(lim / tileHeight);
                this.rowOffset = (short)(lim % tileHeight);
            }
            this.hookTipX += this.hookStepX;
            this.hookTipY += this.hookStepY;
            if (this.hookTipY < this.anchorY || this.abs(this.hookTipX - this.anchorX) < 1280 && this.abs(this.hookTipY - this.anchorY) < 1280) {
                if (this.grappleTile == 97) {
                    this.grappleState = 1;
                } else {
                    this.grappleState = 3;
                    if (this.jumpState == -1) {
                        this.speedY = (short)(1320 * tileHeight / 44);
                        this.grappleState = 2;
                    }
                }
                int dx = 9 * tileWidth / 44;
                if (!this.facingRight) {
                    dx = -dx;
                }
                int dy = 6 * tileHeight / 44;
                int sx = (this.posX >> 8) + dx << 8;
                int sy = this.tileRow * tileHeight + (this.rowOffset >> 8) + dy << 8;
                this.hookStepX = (this.anchorX - sx) / 10;
                this.hookStepY = (this.anchorY - sy) / 10;
            }
        } else if (this.grappleState == 1) {
            this.animFrame = 1;
            this.posX += this.hookStepX;
            this.rowOffset += this.hookStepY;
            this.normaliseRow();
            this.speedY = 0;
            if (this.pixelY() << 8 < this.anchorY || this.abs((this.pixelX() << 8) - this.anchorX) < 1280 && this.abs((this.pixelY() << 8) - this.anchorY) < 1280) {
                this.setAnimation((byte)3);
                this.grappleState = -2;
                this.grappleTimer = 0;
                g.grappleAvailable = false;
                this.game.bufferedAction = this.game.heldAction = 0;
            }
        } else if (this.grappleState == 2) {
            this.animFrame = 1;
            this.speedY -= 128;
            this.rowOffset -= this.speedY;
            this.normaliseRow();
            if (this.speedY < 0) {
                this.grappleState = 3;
            }
        } else if (this.grappleState == 3) {
            int ddy = (this.anchorY >> 8) - this.pixelY();
            int ddx = (this.anchorX >> 8) - this.pixelX();
            int r2 = (tileWidth + (tileWidth >> 1)) * (tileWidth + (tileWidth >> 1)) + (tileHeight + (tileHeight >> 1)) * (tileHeight + (tileHeight >> 1));
            this.grappleState = 5;
            this.speedX = 0;
            this.speedY = 0;
            if (ddx * ddx + ddy * ddy > r2) {
                this.speedX = (short)(ddx << 4);
                this.speedY = (short)(ddy << 4);
                this.grappleState = 4;
            }
        } else if (this.grappleState == 4) {
            int ddy = (this.anchorY >> 8) - this.pixelY();
            int ddx = (this.anchorX >> 8) - this.pixelX();
            int r2 = (tileWidth + (tileWidth >> 1)) * (tileWidth + (tileWidth >> 1)) + (tileHeight + (tileHeight >> 1)) * (tileHeight + (tileHeight >> 1));
            if (ddx * ddx + ddy * ddy > r2) {
                this.posX += this.speedX;
                this.rowOffset += this.speedY;
                this.normaliseRow();
                return;
            }
            this.grappleState = 5;
            this.speedY = 0;
            this.speedX = 0;
        } else if (this.grappleState == 5) {
            this.animFrame = 1;
            int mx = 0;
            int my = 0;
            int ry = this.pixelY() - (this.anchorY >> 8) + 6 * tileHeight / 44;
            int rx;
            int slope;
            if ((rx = this.facingRight ? (this.anchorX >> 8) - (this.pixelX() + 9) : this.pixelX() - 9 - (this.anchorX >> 8)) == 0) {
                slope = 2000;
            } else {
                slope = (ry << 8) / rx;
            }
            if (slope < -1945 || slope > 1945) {
                mx = 2880;
                my = 0;
            } else if (slope > 616) {
                mx = 2781;
                my = 447;
            } else if (slope > 333) {
                mx = 2494;
                my = 864;
            } else if (slope > -10) {
                mx = 2036;
                my = 1222;
            } else if (slope > -334) {
                mx = 2036;
                my = -1222;
            } else if (slope > -617) {
                mx = 2494;
                my = -864;
            } else if (slope > -1946) {
                mx = 2781;
                my = -447;
            }
            if (this.facingRight) {
                this.posX += mx;
            } else {
                this.posX -= mx;
            }
            this.rowOffset += my;
            this.speedY = 447;
            this.normaliseRow();
            if (slope < 0 && slope > -150) {
                this.setAnimation((byte)2);
                this.animMode = 0;
                this.grappleState = -1;
                this.grappleTimer = 15;
                this.game.bufferedAction = this.game.heldAction = 0;
            }
        }
    }

    /** Starts a jump (animation 2). */
    public final void startJump() {
        this.setAnimation((byte)2);
        this.jumpState = 0;
        this.orientation = 0;
    }

    /**
     * Main per-frame player update: death, input-driven running and jumping (g.heldAction / g.bufferedAction hold
     * the current and buffered direction codes), attacks, wall collision and room-edge detection.
     */
    public final void updatePlayer() {
        this.game.repaintRequested = true;
        if (this.speedY <= -maxFallSpeed) {
            this.speedY = (short)(-maxFallSpeed);
        }
        if (this.animId == 10 && this.animMode == 2) {
            this.game.respawnAtCheckpoint();
            if (this.game.fallingDeath == true) {
                this.game.fallingDeath = false;
            }
            g.deaths++;
            return;
        }
        if (this.animId == 10) {
            this.game.bufferedAction = this.game.heldAction = 0;
            this.game.fireState &= -129;
            return;
        }
        if (this.health <= 0 && this.animId != 10) {
            this.game.app.playEffect(0);
            this.health = 0;
            this.animMode = 1;
            this.setAnimation((byte)10);
            g.deaths++;
            return;
        }
        if (this.grappleState >= 0) {
            this.updateGrapple();
            return;
        }
        if (this.animId == 6) {
            this.updateSlide();
            this.checkTileInteractions();
            return;
        }
        if (this.animId == 11) {
            this.game.bufferedAction = this.game.heldAction = 0;
            this.speedX = 0;
            this.attackKind = 1;
            this.wrenchAttack();
        } else if (this.animId == 9) {
            if (++this.stateTimer > 6) {
                if (this.jumpState == -1) {
                    this.setAnimation((byte)0);
                } else {
                    this.speedX = 0;
                    this.speedY = 0;
                    this.setAnimation((byte)3);
                    this.jumpState = 0;
                }
            } else {
                this.game.heldAction = 0;
            }
        }
        if (this.game.heldAction == 5 || this.game.bufferedAction == 5 || this.animId == 8 && this.facingRight) {
            if (this.animId == 4) {
                this.game.heldAction = 0;
            }
            if (!this.facingRight || this.animId != 1) {
                if (this.jumpState == 2) {
                    runSpeed = 768;
                } else if (this.animId == 8) {
                    runSpeed = 384;
                } else if (this.jumpState > -1) {
                    runSpeed = 1536;
                } else {
                    this.setAnimation((byte)1);
                    runSpeed = 1536;
                    this.animMode = 0;
                }
                this.speedX = runSpeed;
                this.facingRight = true;
            }
        } else if (this.game.heldAction == 2 || this.game.bufferedAction == 2 || this.animId == 8 && !this.facingRight) {
            if (this.animId == 4) {
                this.game.heldAction = 0;
            }
            if (this.facingRight || this.animId != 1) {
                if (this.jumpState == 2) {
                    runSpeed = 768;
                } else if (this.animId == 8) {
                    runSpeed = 384;
                } else if (this.jumpState > -1) {
                    runSpeed = 1536;
                } else {
                    this.setAnimation((byte)1);
                    runSpeed = 1536;
                    this.animMode = 0;
                }
                this.speedX = (short)(-runSpeed);
                this.facingRight = false;
            }
        } else if (this.game.heldAction == 0) {
            if (this.jumpState == 2) {
                this.speedX = this.facingRight ? (short)768 : (short)-768;
            } else if (this.jumpState >= 0) {
                if (this.speedX > 0) {
                    this.speedX -= 196;
                } else if (this.speedX < 0) {
                    this.speedX += 196;
                }
                if (this.speedX <= 196 && this.speedX >= -196) {
                    this.speedX = 0;
                }
            } else {
                this.speedX = 0;
            }
            if (this.animId == 1) {
                this.setAnimation((byte)0);
            }
        }
        if (this.game.heldAction == 1) {
            if (this.jumpState == -1) {
                this.speedY = 3584;
                this.jumpState++;
            } else if (this.jumpState == 0) {
                if (g.cameraLocked == true) {
                    this.speedY = 3584;
                } else {
                    this.speedY = 3072;
                    this.animMode = 1;
                    this.setAnimation((byte)13);
                }
                this.jumpState++;
            }
            this.game.handleKeyRelease(0, 1);
        }
        if (this.animId == 4 && this.jumpState >= 0) {
            if (this.animTimer > 2) {
                this.setAnimation((byte)2);
            }
            this.animMode = 0;
            return;
        }
        if (this.speedX != 0) {
            this.aheadColumn = this.facingRight ? this.rightEdgeColumn() : this.leftEdgeColumn();
            if (this.rowOffset < runSpeed) {
                this.aheadRow = this.tileRow;
            } else {
                this.aheadRow = (byte)(this.tileRow + 1);
            }
            if (this.aheadColumn >= 0 && this.aheadColumn <= 27 && this.game.levelMap.isSolid(this.aheadColumn, this.aheadRow) && (this.tileRow < 17 || this.tileRow == 17 && this.rowOffset == 0)) {
                int oldx = this.posX;
                this.posX += this.speedX;
                this.aheadColumn = this.facingRight ? this.rightEdgeColumn() : this.leftEdgeColumn();
                if (this.game.enemyTouchingPlayer() != -1) {
                    this.posX -= this.speedX * 3 / 2;
                    if (!this.game.levelMap.isSolid((this.posX >> 8) / tileWidth, this.aheadRow)) {
                        this.posX = oldx;
                    }
                } else if (this.game.playerHitsCrate()) {
                    this.posX -= this.speedX;
                } else if (!this.game.levelMap.isSolid(this.aheadColumn, this.aheadRow)) {
                    this.posX -= this.speedX;
                }
            } else {
                this.speedX = 0;
                if (this.facingRight) {
                    if (this.rightEdgeColumn() == this.aheadColumn) {
                        this.posX = this.aheadColumn * tileWidth - 8 - 1 << 8;
                    }
                } else if (this.leftEdgeColumn() == this.aheadColumn) {
                    this.posX = this.aheadColumn * tileWidth + tileWidth + 8 + 1 << 8;
                }
            }
            if (this.aheadColumn >= 27) {
                this.atRoomEdge = true;
            }
            if (this.aheadColumn == 0) {
                this.atRoomEdge = true;
            }
        }
        if (this.comboTimer >= 0 && this.animId != 11) {
            this.tickCombo();
        }
        int lim;
        if ((lim = this.floorHeight(false)) - this.pixelY() > tileHeight >> 1) {
            this.onPlatform = false;
        }
        if (this.onPlatform && this.jumpState == -1) {
            this.tileRow = (byte)(lim / tileHeight);
            this.rowOffset = (short)(lim % tileHeight << 8);
            this.posX += this.platformRideSpeed << 8;
            this.speedY = 0;
        } else {
            if ((this.jumpState >= 0 || this.pixelY() < lim) && this.orientation == 0) {
                if (this.jumpState == 2) {
                    this.speedY = -768;
                } else {
                    this.speedY -= 384;
                }
                this.rowOffset -= this.speedY;
                if (this.speedY > 0 && (!this.game.levelMap.isSolid(this.rightEdgeColumn(), this.tileRow) || !this.game.levelMap.isSolid(this.leftEdgeColumn(), this.tileRow))) {
                    this.rowOffset += this.speedY;
                    this.speedY = 0;
                }
                if (this.rowOffset < 0) {
                    this.rowOffset += tileHeight << 8;
                    this.tileRow--;
                    if (this.tileRow < 0) {
                        this.tileRow = 0;
                        this.rowOffset = 0;
                    }
                    if (this.tileRow == 0) {
                        this.atRoomEdge = true;
                    }
                } else if (this.rowOffset > tileHeight << 8) {
                    this.rowOffset -= tileHeight << 8;
                    this.tileRow++;
                }
                if (this.jumpState == -1) {
                    this.jumpState = 0;
                }
                if (this.animId != 11 && this.animId != 14) {
                    if (this.jumpState == 0) {
                        this.setAnimation((byte)2);
                    }
                    if (this.game.heldAction == 2 || !this.facingRight) {
                        this.facingRight = false;
                    } else if (this.game.heldAction == 5 || this.facingRight) {
                        this.facingRight = true;
                    }
                    if (this.speedY <= 0 && this.jumpState == 1 && !g.cameraLocked && this.animId == 12) {
                        this.jumpState = 2;
                    } else if (this.speedY <= -1024 && this.jumpState != 2) {
                        this.setAnimation((byte)3);
                    }
                }
            }
            if (this.orientation == 0) {
                if (this.animId != 11 && this.speedY < 0 && this.animId != 10) {
                    for (int n = 0; n < g.enemySlotCount; n++) {
                        if (this.game.enemies[n].animId != 2 && this.game.enemyOverlapsPlayer(n) && this.game.enemies[n].feetY() > this.pixelY()) {
                            this.setAnimation((byte)3);
                            if (this.pixelX() > this.game.enemies[n].pixelX()) {
                                this.game.enemies[n].knockback = -10;
                                if (this.game.levelMap.isSolid((this.posX >> 8) / tileWidth + 1, this.tileRow + 1) && this.game.levelMap.isSolid((this.posX >> 8) / tileWidth, this.tileRow + 1)) {
                                    this.speedY = 0;
                                    this.speedX = 0;
                                    this.jumpState = 0;
                                    if (this.game.levelMap.isSolid((this.posX >> 8) / tileWidth + 1, this.tileRow)) {
                                        this.posX += 510;
                                    }
                                    if (!this.game.levelMap.isSolid((this.posX >> 8) / tileWidth + 1, this.tileRow)) {
                                        this.posX -= 510;
                                    }
                                } else {
                                    this.jumpState = -1;
                                }
                            } else {
                                this.game.enemies[n].knockback = 10;
                                if (this.game.levelMap.isSolid((this.posX >> 8) / tileWidth - 1, this.tileRow + 1) && this.game.levelMap.isSolid((this.posX >> 8) / tileWidth, this.tileRow + 1)) {
                                    this.speedY = 0;
                                    this.speedX = 0;
                                    this.jumpState = 0;
                                    if (this.game.levelMap.isSolid((this.posX >> 8) / tileWidth - 1, this.tileRow)) {
                                        this.posX -= 510;
                                    }
                                    if (!this.game.levelMap.isSolid((this.posX >> 8) / tileWidth - 1, this.tileRow)) {
                                        this.posX += 510;
                                    }
                                } else {
                                    this.jumpState = -1;
                                }
                            }
                        }
                    }
                }
                if (this.pixelY() >= lim && this.speedY < 0) {
                    this.tileRow = (byte)(lim / tileHeight);
                    this.rowOffset = (short)(lim % tileHeight << 8);
                    this.atRoomEdge = true;
                    this.speedY = 0;
                    this.jumpState = -1;
                    if (this.animId == 3 || this.animId == 2 || this.animId >= 11 && this.animId <= 14) {
                        if (this.jumpState == 2) {
                            this.jumpState = 1;
                        }
                        this.setAnimation((byte)4);
                        this.animMode = 1;
                    }
                }
            }
        }
        if (this.animId == 10) {
            return;
        }
        this.magnetPickups();
        if (this.platformIgnoreTimer == 0) {
            int px = this.posX >> 8;
            if (this.jumpState >= 0 && this.speedY < 0) {
                for (int n = 3; n >= 0; n--) {
                    if (this.game.railX1[n] != -1) {
                        boolean fwd;
                        if ((fwd = this.game.railX2[n] - this.game.railX1[n] > 0) && px > this.game.railX1[n] && px < this.game.railX2[n] || !fwd && px < this.game.railX1[n] && px > this.game.railX2[n]) {
                            int ly;
                            int dd = (ly = this.game.railDy[n] * px / this.game.railDx[n] + this.game.railBase[n]) - (this.pixelY() + (g.cellHeight >> 1) << 8);
                            if (this.abs(dd) < -this.speedY) {
                                ly -= g.cellHeight << 8;
                                this.tileRow = (byte)((ly >> 8) / tileHeight);
                                this.rowOffset = (short)((ly >> 8) % tileHeight << 8);
                                this.jumpState = -1;
                                this.setAnimation((byte)6);
                                this.speedX = this.speedY = 0;
                                this.facingRight = fwd ? true : false;
                                int s = (this.abs(this.game.railX2[n] - this.game.railX1[n]) << 8) / 2304;
                                this.moveStepX = (this.game.railDx[n] << 8) / s;
                                this.moveStepY = this.game.railDy[n] / s;
                                this.moveEndY = this.game.railY2[n];
                                break;
                            }
                        }
                    }
                }
            }
        } else {
            this.platformIgnoreTimer--;
        }
        this.checkTileInteractions();
    }

    /**
     * Tile interactions at the player's position: damaging tile 26, swing hooks 56-61, the
     * respawn marker -125, checkpoints 99/100, dialogue triggers (77-96 = dialogues 0-19,
     * -118..-99 = dialogues 20-39, -94 = dialogue 30) which also set the stage-summary message,
     * falling out of the room, and room transitions through the edges (g.changeRoom(direction)).
     */
    public final void checkTileInteractions() {
        byte cx = this.tileColumn();
        byte cy = (byte)((this.pixelY() + g.cellHeight) / tileHeight);
        if (this.grappleTimer > 0) {
            this.game.hudDirty = true;
            g.grappleAvailable = true;
            if (--this.grappleTimer <= 0) {
                this.grappleState = -2;
                this.grappleTimer = 0;
                g.grappleAvailable = false;
            }
        }
        short tile = this.game.levelMap.rawTile(cx, cy);
        g.onTeleporter = false;
        if (tile >= 56 && tile <= 61) {
            this.grappleState = -1;
            this.grappleTimer = 10;
        } else if (tile == 26) {
            this.speedY = 3584;
            this.jumpState = 0;
            this.game.challengeMultiplier = 1;
            this.animMode = 1;
            this.setAnimation((byte)2);
            this.game.spawnPlayerShot(this.posX, this.pixelY() << 8, 30);
            if (!g.invulnerable) {
                this.health -= 4;
            }
            this.grappleState = -2;
            this.grappleTimer = 0;
            g.grappleAvailable = false;
            this.game.hudDirty = true;
        } else if (tile == -125) {
            g.onTeleporter = true;
            g.teleporterFlag = false;
            g.teleporterTarget = -1;
        } else {
            for (byte yy = (byte)(--cy + 2); yy >= cy; yy--) {
                if ((tile = this.game.levelMap.rawTile(cx, yy)) == 99 || tile == 100) {
                    this.game.checkpointRoom = this.game.currentRoom;
                    this.game.checkpointColumn = cx;
                    this.game.checkpointRow = cy;
                    if (cx < 1) {
                        this.game.checkpointCamX = 0;
                    } else {
                        this.game.checkpointCamX = (short)(-tileWidth * (cx - 1));
                    }
                    if (cy < 6) {
                        this.game.checkpointCamY = 0;
                    } else {
                        this.game.checkpointCamY = (short)(-tileHeight * (cy - 5));
                    }
                    break;
                }
                if (tile >= 77 && tile <= 96 && this.game.dialoguePending(tile - 77)) {
                    this.game.dialoguePortraits = true;
                    this.game.dialogueLinesLeft = g.dialogueLineCounts[tile - 77];
                    this.game.openTextBox(g.dialogueFirstString[tile - 77]);
                    this.game.markDialogueSeen(tile - 77);
                    if (g.dialogueFirstString[tile - 77] == 112 && (this.game.doorBits & 2) == 0) {
                        this.game.summaryMessageId = 248;
                        g.parIndex = 16;
                        return;
                    }
                    this.game.summaryMessageId = 233 + (tile - 77);
                    g.parIndex = (byte)(tile - 77);
                    return;
                }
                if (tile >= -118 && tile <= -99 && this.game.dialoguePending(tile - -118 + 20)) {
                    if (tile - -118 == 18 && (this.ownedWeapons & 32) == 0) {
                        return;
                    }
                    if (tile - -118 == 19 && (this.game.dialoguePending(4) || this.game.dialoguePending(5) || this.game.dialoguePending(6) || this.game.dialoguePending(7) || this.game.dialoguePending(9) || this.game.dialoguePending(10))) {
                        return;
                    }
                    this.game.dialoguePortraits = true;
                    this.game.dialogueLinesLeft = g.dialogueLineCounts[tile - -118 + 20];
                    this.game.openTextBox(g.dialogueFirstString[tile - -118 + 20]);
                    this.game.markDialogueSeen(tile - -118 + 20);
                    return;
                }
                if (tile == -94 && !this.game.dialoguePending(11) && this.game.dialoguePending(30)) {
                    this.game.dialoguePortraits = true;
                    this.game.dialogueLinesLeft = g.dialogueLineCounts[30];
                    this.game.openTextBox(g.dialogueFirstString[30]);
                    this.game.markDialogueSeen(30);
                    return;
                }
            }
        }
        if (this.tileRow >= 17 && this.game.deadlyPit == true) {
            this.game.fallingDeath = true;
            this.setAnimation((byte)10);
            this.animMode = 1;
            return;
        }
        if (this.atRoomEdge) {
            this.atRoomEdge = false;
            if (this.rightEdgeColumn() == 0 && !this.facingRight) {
                this.game.changeRoom(2);
                return;
            }
            if (this.leftEdgeColumn() >= 27 && this.facingRight) {
                this.game.changeRoom(5);
                return;
            }
            if (this.tileRow >= 17 && this.speedY <= 0) {
                this.game.changeRoom(6);
                return;
            }
            if (this.tileRow == 0 && this.rowOffset < 1280) {
                this.game.changeRoom(1);
            }
        }
    }

    /**
     * Advances the animation timer/frame; one-shot animations end in state animMode == 2 and call endOneShotAnimation().
     */
    public final void animate() {
        if (this.animMode == 2) {
            return;
        }
        if (this.animId == 14) {
            this.speedY = 0;
        }
        this.animTimer++;
        if (this.animTimer > animDelays[this.actorKind][this.animId]) {
            this.animTimer = 0;
            this.animFrame++;
            if (this.animFrame >= animLengths[this.actorKind][this.animId]) {
                if (this.animMode == 0) {
                    this.animFrame = 0;
                } else {
                    this.animFrame--;
                    this.animMode = 2;
                    this.endOneShotAnimation();
                }
            }
            this.game.repaintRequested = true;
        }
    }

    /**
     * Draws the swingshot cable, the player sprite (mirrored when facing left) and the held weapon.
     */
    public final void draw(Graphics graphics, int layer, int camX, int camY) {
        g.camX = (short)camX;
        g.camY = (short)camY;
        int x = (this.posX >> 8) - (g.cellWidth >> 1) + g.camX;
        int y = this.tileRow * tileHeight + (this.rowOffset >> 8) + g.camY + g.playerDrawOffsetY;
        if (this.animId == 6) {
            y += tileHeight >> 1;
        }
        int cut;
        int w;
        int u9;
        int cy;
        if (this.grappleState >= 0) {
            u9 = 0;
            w = 0;
            int dx = 26 * tileWidth / 44;
            if (!this.facingRight) {
                dx = tileWidth - dx;
            }
            int dy = 10 * tileHeight / 44;
            int sx = x + dx - 9 << 8;
            int sy = y + dy - 9 << 8;
            int tx = this.hookTipX + (g.camX << 8);
            int ty = this.hookTipY + (g.camY << 8);
            if (this.grappleState == 0) {
                sy += 2304;
            }
            if (this.facingRight) {
                tx -= 1216;
            } else {
                tx -= 2432;
            }
            int stx = (tx - sx) / 19;
            int sty = (ty - sy) / 19;
            int n = 0;
            for (n = 0; n < 20; n++) {
                cut = 0;
                if ((cy = sy) >> 8 < hudHeight) {
                    cut = hudHeight - (cy >> 8);
                    cy = hudHeight << 8;
                }
                int px = sx >> 8;
                int py = cy >> 8;
                if (cut < 19 && cut >= 0 && px < g.screenWidth && py < g.screenHeight && px + 19 >= 0 && py + 19 >= 0) {
                    graphics.setClip(px, py, 19, 19 - cut);
                    graphics.drawImage(g.imgBolts, px, py - 114 - cut, 0);
                }
                sx += stx;
                sy += sty;
            }
            int px = sx - stx >> 8;
            int py = sy - sty >> 8;
            cut = 0;
            if ((cy = py - 9) < hudHeight) {
                cut = hudHeight - cy;
                cy = hudHeight;
            }
            if (cut < 19 && cut >= 0 && px < g.screenWidth && cy < g.screenHeight && px + 19 >= 0 && cy + 19 >= 0) {
                graphics.setClip(px, cy, 19, 19 - cut);
                if (this.facingRight) {
                    graphics.drawImage(g.imgBolts, px, cy - 95 - cut, 0);
                } else {
                    graphics.drawRegion(g.imgBolts, 0, 95, 19, 19, 2, px, cy - cut, 20);
                }
            }
        }
        if (y + g.cellHeight > hudHeight && y < g.screenHeight && this.blinkTimer % 2 == 0) {
            int fr = animFrames[this.actorKind][this.animId][this.animFrame];
            byte[] r = frameBoxes[fr];
            graphics.setClip(0, hudHeight, g.screenWidth, g.screenHeight - hudHeight);
            if (this.facingRight) {
                graphics.drawImage(g.imgPlayerFrames[fr], x + r[4], y + r[5], 20);
            } else {
                x += g.cellWidth - r[4] - r[2];
                y += r[5];
                graphics.drawRegion(g.imgPlayerFrames[fr], 0, 0, r[2], r[3], 2, x, y, 20);
            }
        }
        if (this.weapon == 0) {
            return;
        }
        int ox = weaponOffsetX[animFrames[this.actorKind][this.animId][this.animFrame]];
        int oy = weaponOffsetY[animFrames[this.actorKind][this.animId][this.animFrame]];
        if (this.animId == 6) {
            oy += tileHeight >> 1;
        }
        if (ox == 44 || oy == 44) {
            return;
        }
        w = g.imgWeapons.getWidth();
        if (this.facingRight) {
            x = (this.posX >> 8) + g.camX + ox;
        } else {
            x = (this.posX >> 8) + g.camX - ox - w;
        }
        y = this.tileRow * tileHeight + (this.rowOffset >> 8) + g.camY + g.playerDrawOffsetY + oy;
        int wi = this.weapon - 1;
        if (y + weaponStrips[wi][2] + weaponStrips[wi][1] > hudHeight) {
            graphics.drawRegion(g.imgWeapons, 0, weaponStrips[wi][0], w, weaponStrips[wi][1], this.facingRight ? 0 : 2, x, y + weaponStrips[wi][2], 20);
        }
    }

    /** Fires Clank's grapple at the nearest grapple point (tile codes 97/98) within reach in front of the player. */
    public final void fireGrapple() {
        if (this.facingRight) {
            int c0 = this.rightEdgeColumn();
            if ((this.posX >> 8) % tileWidth > tileWidth >> 1) {
                c0++;
            }
            for (int x = c0; x < c0 + 3; x++) {
                for (int y = this.tileRow; y >= this.tileRow - 3; y--) {
                    int t;
                    if (x >= 0 && y >= 0 && ((t = this.game.levelMap.rawTile(x, y)) == 97 || t == 98)) {
                        this.game.fireGrappleAt(x, y, t);
                    }
                }
            }
        } else {
            int c0 = this.leftEdgeColumn();
            if ((this.posX >> 8) % tileWidth < tileWidth >> 1) {
                c0--;
            }
            for (int x = c0; x > c0 - 3; x--) {
                for (int y = this.tileRow; y >= this.tileRow - 3; y--) {
                    int t;
                    if (x >= 0 && y >= 0 && ((t = this.game.levelMap.rawTile(x, y)) == 97 || t == 98)) {
                        this.game.fireGrappleAt(x, y, t);
                    }
                }
            }
        }
    }

    /**
     * Fires the current weapon: plays the shot sound and spawns the projectile type for weapon and level.
     */
    public final void fireWeapon() {
        int q = this.weaponLevels[this.weapon];
        if (this.animId != 6 && this.animId != 7 && this.animId != 12 && this.animId != 13) {
            this.setAnimation((byte)7);
            this.animMode = 0;
        }
        if (this.ammo[this.weapon] <= 0) {
            return;
        }
        if (this.game.app.gameActive) {
            this.game.app.playEffect(2);
        }
        switch (this.weapon) {
            case 0:
                break;
            case 1:
                this.game.spawnPlayerShot(this.posX + (this.facingRight == true ? 4608 : -4608), (this.pixelY() << 8) + 2048, 0 + q);
                break;
            case 2:
                this.game.spawnPlayerShot(this.posX + (this.facingRight == true ? 4608 : -4608), (this.pixelY() << 8) + 2048, 3 + q);
                break;
            case 3:
                this.game.spawnPlayerShot(this.posX + (this.facingRight == true ? 4608 : -4608), (this.pixelY() << 8) + 2048, 6 + q);
                break;
            case 4:
                this.game.spawnPlayerShot(this.posX + (this.facingRight == true ? 4608 : -4608), (this.pixelY() << 8) + 2048, 9 + q);
                if (this.ammo[this.weapon] > 0 && q == 2) {
                    this.game.spawnPlayerShot(this.posX + (this.facingRight == true ? 4608 : -4608), (this.pixelY() << 8) + 2048, 9 + q);
                }
                break;
            case 5:
                this.game.spawnPlayerShot(this.posX + (this.facingRight == true ? 4608 : -4608), (this.pixelY() << 8) + 2048 + this.ammo[this.weapon] % 2 * 1024, 12 + q);
                break;
            case 6:
                this.game.spawnPlayerShot(this.posX + (this.facingRight == true ? 4608 : -4608), (this.pixelY() << 8) + 2048, 15 + q);
                break;
            case 7:
                this.game.spawnPlayerShot(this.posX + (this.facingRight == true ? 4608 : -4608), (this.pixelY() << 8) + 2048, 18 + q);
                break;
        }
    }

    /**
     * Wrench attack: hits switches (tile codes 36+) and enemies in the swing box; kills award score and bolts.
     */
    public final void wrenchAttack() {
        if (this.animId >= 4 && this.animId != 11) {
            return;
        }
        this.game.shotHitsCrates(-1);
        if (this.animId != 11) {
            this.animMode = 1;
            this.setAnimation((byte)8);
        }
        int tx;
        int ty;
        int tw = 0;
        int th = 0;
        int st;
        int n;
        int kind;
        for (n = 2; n >= 0; n--) {
            if (this.game.lockColumn[n] == -1) {
                continue;
            }
            tx = this.game.lockColumn[n] * tileWidth + (tileWidth - 19 >> 1);
            ty = this.game.lockRow[n] * tileHeight;
            if (this.abs(tx - this.pixelX()) > 3 * tileWidth >> 1 || this.abs(ty - this.pixelY()) > tileHeight) {
                continue;
            }
            if (this.game.boxesOverlap(tx, ty, 19, 19, (this.posX >> 8) + (this.facingRight ? wrenchBoxX[this.attackKind] : -wrenchBoxX[this.attackKind] - wrenchBoxW[this.attackKind]), this.pixelY() + wrenchBoxY[this.attackKind], wrenchBoxW[this.attackKind], wrenchBoxH[this.attackKind])) {
                kind = this.game.levelMap.rawTile(this.game.lockColumn[n], this.game.lockRow[n]);
                kind -= 36;
                if (kind == 2 && (this.game.doorBits & 1 << kind) > 0) {
                    this.game.dialoguePortraits = true;
                    this.game.mapHighlightBits = 32;
                    this.game.mapUnlockBits |= 32;
                    this.game.dialogueLinesLeft = g.dialogueLineCounts[33];
                    this.game.openTextBox(g.dialogueFirstString[33]);
                    this.game.summaryMessageId = 256;
                    g.parIndex = 15;
                }
                this.game.doorBits &= ~(1 << kind);
                if (kind == 0) {
                    this.game.openGate(this.game.levelIndex, this.game.currentRoom);
                }
            }
        }
        for (n = g.enemySlotCount - 1; n >= 0; n--) {
            if ((st = this.game.enemies[n].actorKind) != -1) {
                if (this.game.enemies[n].animId == 5) {
                    continue;
                }
                tx = this.game.enemies[n].pixelX() - d.bodyBoxX[st];
                ty = this.game.enemies[n].feetY() + d.bodyBoxY[st];
                tw = d.bodyBoxW[st];
                th = d.bodyBoxH[st];
                if (this.abs(tx - this.pixelX()) > 3 * tileWidth >> 1 || this.abs(ty - this.pixelY()) > tileHeight) {
                    continue;
                }
                if (this.game.boxesOverlap(tx, ty, tw, th, (this.posX >> 8) + (this.facingRight ? wrenchBoxX[this.attackKind] : -wrenchBoxX[this.attackKind] - wrenchBoxW[this.attackKind]), this.pixelY() + wrenchBoxY[this.attackKind], wrenchBoxW[this.attackKind], wrenchBoxH[this.attackKind])) {
                    if (this.game.enemies[n].hitBySpin == true && this.animId == 11) {
                        continue;
                    }
                    if (this.animId == 11) {
                        this.game.enemies[n].hitBySpin = true;
                    }
                    if (this.animId == 11) {
                        this.game.enemies[n].health -= wrenchDamage[this.weaponLevels[1]];
                    } else {
                        this.game.enemies[n].health -= wrenchDamage[this.weaponLevels[0]];
                    }
                    if (this.game.enemies[n].animId != 2) {
                        if (this.game.enemies[n].posX > this.posX) {
                            this.game.enemies[n].knockback = 10;
                        } else {
                            this.game.enemies[n].knockback = -10;
                        }
                    }
                    if (this.game.enemies[n].health <= 0) {
                        int lvl = this.game.currentRoom;
                        if (this.game.arenaActive == true) {
                            lvl = 0;
                        }
                        int wi = n + lvl * 10 >> 5;
                        this.game.enemyAliveBits[wi] &= ~(1 << n + lvl * 10 - (wi << 5));
                        this.game.enemies[n].setAnimation((byte)5);
                        this.game.enemies[n].animMode = 1;
                        if (st != 4) {
                            g.kills++;
                            if (this.game.challengeMode && ++this.game.challengeMultiplier > 10) {
                                this.game.challengeMultiplier = 10;
                            }
                            if (this.game.levelIndex != 0) {
                                for (int hit = 0; hit < d.dropsPerVariant[this.game.enemies[n].variant]; hit++) {
                                    this.game.spawnPickup(this.game.enemies[n].pixelX(), this.game.enemies[n].feetY(), 0);
                                }
                            }
                        }
                    } else if (this.game.enemies[n].animId != 2) {
                        this.game.enemies[n].setAnimation((byte)4);
                        this.game.enemies[n].stateTimer = 0;
                    }
                }
        
            }
        }
        if (this.jumpState == -1) {
            this.game.heldAction = 0;
        }
    }

    /** End of a one-shot animation: returns to the follow-up animation. */
    public final void endOneShotAnimation() {
        this.attackKind = 0;
        if (this.animId == 7 || this.animId == 8 || this.animId == 9 || this.animId == 0 || this.animId == 4) {
            this.animMode = 0;
            this.setAnimation((byte)0);
        } else if (this.animId == 13) {
            this.animMode = 0;
            this.setAnimation((byte)12);
        } else if (this.animId == 14) {
            this.animMode = 0;
            this.setAnimation((byte)11);
        }
    }

    /** Advances the attack combo timer D. */
    public final void tickCombo() {
        if (++this.comboTimer > 15) {
            this.comboTimer = -1;
            this.attackKind = 0;
        }
    }

    /**
     * Bolt / pick-up magnet: pulls nearby loose pick-ups toward the player and collects them (score, health, ammo).
     */
    public final void magnetPickups() {
        int py = this.pixelY() + 7 + 37;
        for (int n = 11; n >= 0; n--) {
            int type = this.game.pickupType[n];
            int x = this.game.pickupX[n];
            short vx = this.game.pickupVx[n];
            int range = 0;
            if (type == -1) {
                continue;
            }
            if (this.health == 20 && type == 2) {
                continue;
            }
            range = tileWidth * 5 << 7;
            if (vx == 0 && (this.abs(x - this.posX) > range || this.abs((this.game.pickupY[n] >> 8) - py) > g.cellHeight)) {
                continue;
            }
            if (x > this.posX) {
                vx -= 256;
            } else {
                vx += 256;
            }
            if ((x += vx) >= tileWidth * 27 + (tileWidth >> 1) << 8) {
                x = tileWidth * 27 + (tileWidth >> 1) << 8;
            }
            int d_;
            if ((d_ = this.abs(x - this.posX)) <= tileWidth << 6) {
                this.game.hudDirty = true;
                if (type <= 1) {
                    this.game.bolts += 10 * this.game.challengeMultiplier;
                    g.boltsCollected += 10 * this.game.challengeMultiplier;
                    if (this.game.bolts > 999999) {
                        this.game.bolts = 999999;
                    }
                } else if (type == 2) {
                    this.health += 4;
                    if (this.health > 20) {
                        this.health = 20;
                    }
                } else {
                    this.refillAmmo(1);
                }
                this.game.pickupType[n] = -1;
                return;
            }
            int dy = this.game.pickupY[n] - (this.pixelY() + (tileHeight >> 1) << 8);
            if (d_ != 0) {
                this.game.pickupY[n] += dy * vx * (vx >= 0 ? -1 : 1) / d_;
            }
            this.game.pickupX[n] = x;
            this.game.pickupVx[n] = vx;
        }
    }

    /**
     * Ammo pick-up: refills the owned gun with the least ammo by pickups pick-ups (capped at its maximum).
     */
    private void refillAmmo(int pickups) {
        int best = 1;
        int min = 9999;
        for (int n = 1; n < 8; n++) {
            if (n != 6 && (this.ownedWeapons & 1 << n) > 0 && this.ammo[n] < min && this.ammo[n] != maxAmmo[n * 3 + this.weaponLevels[n]]) {
                min = this.ammo[n];
                best = n;
            }
        }
        this.ammo[best] += pickups * ammoPerPickup[best];
        if (this.ammo[best] > maxAmmo[best * 3 + this.weaponLevels[best]]) {
            this.ammo[best] = maxAmmo[best * 3 + this.weaponLevels[best]];
        }
    }

    /** Loads the animation tables animFrames, animLengths, animDelays from resource {@code resource}. */
    private void loadAnimationFile(String resource) {
        byte first;
        byte second;
        byte[] bytes = new byte[4];
        try {
            InputStream stream = bytes.getClass().getResourceAsStream(resource);
            int n = 0;
            while (n < 15) {
                first = (byte)stream.read();
                second = (byte)stream.read();
                animLengths[0][n] = first;
                animDelays[0][n] = second;
                this.readBytes(stream, bytes, first);
                System.arraycopy(bytes, 0, animFrames[0][n], 0, first);
                ++n;
            }
            stream.close();
        } catch (java.io.IOException exception) {
        }
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
