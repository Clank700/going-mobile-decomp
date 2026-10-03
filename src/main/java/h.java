import javax.microedition.lcdui.Graphics;

/*
 * h - Projectile / effect.
 *
 * Short-lived objects owned by g (two arrays of 10): fired shots and visual effects.
 * Type-indexed static tables give speed, hit-box size and damage per type.
 *
 * Naming: this is reconstructed, output-exact Java source (not recovered original source).
 * Member names are descriptive source names; config/d-preobf.map maps every one of them
 * back to its retail (obfuscated) runtime name. Renames are always group-complete (all
 * members sharing a retail name), which reproduces ProGuard's retail constant-pool
 * ordering; see docs/NAMING_SCHEME.md. Library overrides keep their API names,
 * classes keep their retail names, and reconstruction devices keep their opus* names.
 */
public final class h {
    /** Damage per projectile type. */
    public static final byte[] damage = new byte[]{1, 2, 4, 0, 0, 0, 0, 0, 0, 12, 12, 12, 1, 2, 3, 1, 2, 2, 0, 0, 0, 3, 5, 6, 6, 11, 17, 4, 7, 10, 0, 0, 0};
    /** Hit-box half width per projectile type. */
    public static final byte[] halfWidths = new byte[]{3, 3, 3, 3, 3, 3, 3, 3, 3, 0, 0, 0, 3, 3, 3, 0, 0, 0, 3, 3, 3, 45, 65, 65, 15, 15, 30, 15, 15, 15, 0, 12, 10};
    /** Hit-box half height per projectile type (also the ground-contact offset of lobbed shots). */
    public static final byte[] halfHeights = new byte[]{3, 3, 3, 3, 3, 3, 3, 3, 3, 0, 0, 0, 3, 3, 3, 0, 0, 0, 3, 3, 3, 45, 65, 65, 15, 15, 30, 15, 15, 15, 0, 12, 10};
    /** Speed per projectile type (pixels per frame; used by the homing steering). */
    public static final byte[] speeds = new byte[]{10, 16, 16, 5, 5, 5, 8, 8, 8, 1, 1, 1, 4, 4, 4, 1, 1, 1, 8, 8, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 10};
    /**
     * Sprite id per type (row into spriteRegions = id - 13); -1 = drawn with primitives (beams, lightning, pellets).
     */
    public static final short[] spriteIds = new short[]{13, 14, 15, 22, 23, 24, 19, 20, 21, -1, -1, -1, -1, -1, -1, -1, -1, -1, 16, 17, 18, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1};
    public static final short[][] spriteRegions = new short[][]{{0, 266, 9, 3, 5, 8}, {0, 270, 11, 3, 4, 8}, {0, 273, 11, 5, 4, 7}, {13, 274, 6, 4, 6, 7}, {12, 270, 7, 4, 6, 7}, {10, 266, 9, 4, 5, 7}, {0, 278, 9, 5, 6, 7}, {0, 284, 11, 5, 5, 7}, {0, 289, 11, 5, 5, 6}, {12, 292, 7, 6, 6, 6}, {12, 286, 7, 6, 6, 6}, {10, 278, 9, 8, 6, 5}};
    /**
     * Projectile / effect type (-1 = free slot):
     * 0-2 straight shot (Lancer), 3-5 lobbed bomb (Gravity Bomb), 6-8 exploding shot (Mini Rocket),
     * 9-11 short beam (Defragmenter), 12-14 homing lightning (Circuit Jammer; destroys breakable
     * tile 34), 15-17 beam (Boar-Zooka), 18-20 homing missile (R.Y.N.O.), 21-30 explosion
     * animation, 31 boss shot, 32 bobbing hazard. Player weapons use one type per weapon level
     * (base + level); enemies reuse types 0, 3, 6 and 18.
     */
    public byte shotType;
    /** X position, 8.8 fixed point. */
    public int posX;
    /** Y position, 8.8 fixed point. */
    public int posY;
    public int pathX1;
    public int pathY1;
    public int pathX2;
    public int pathY2;
    /** Horizontal speed, 8.8 fixed point (sign = direction). */
    public int speedX;
    /** Vertical speed, 8.8 fixed point. */
    public int speedY;
    /** Frame / tick counter. */
    public byte tick;
    public int hitHalfWidth;
    public int hitHalfHeight;
    /** Hit something; the slot is released (or the impact effect spawned) next update. */
    private boolean hasHit;
    /** Homing target: index into g.enemies, -1 = none. */
    private short homingTarget;
    public byte sourceVariant;
    public int beamOffset;
    /** Tile width (copied from g.tileWidth). */
    public static byte tileWidth;
    /** Tile height (copied from g.tileHeight). */
    public static byte tileHeight;
    /** Screen width (copied from g). */
    public static short screenWidth;
    /** Screen height (copied from g). */
    public static short screenHeight;
    /** Top of the play area below the HUD (copied from g.hudHeight). */
    public static short hudHeight;
    /** Owning game canvas. */
    private g game;
    /** Explosion animation frames in g.imgExplosion: {sheet y, width, height, x offset, y offset}. */
    public static final byte[][] explosionFrames = new byte[][]{{0, 28, 28, 10, 9}, {28, 43, 42, 1, 1}, {70, 30, 23, 8, 12}};

    /** Caches screen/tile dimensions from g and resets the slot. */
    public h(g owner) {
        tileWidth = ((g)null).tileWidth;
        tileHeight = ((g)null).tileHeight;
        hudHeight = (short)((g)null).hudHeight;
        screenWidth = ((g)null).screenWidth;
        screenHeight = ((g)null).screenHeight;
        this.game = owner;
        this.shotType = (byte)-1;
        this.posX = this.posY = 0;
        this.speedX = this.speedY = 0;
        this.tick = 0;
        this.hitHalfWidth = 0;
        this.hitHalfHeight = 0;
        this.hasHit = false;
        this.homingTarget = (short)-1;
        this.pathX2 = this.pathX1 = 0;
        this.pathY2 = this.pathY1 = 0;
        this.sourceVariant = 0;
    }

    /** Releases the slot (type -1) and clears all state. */
    public final void release() {
        this.shotType = (byte)-1;
        this.posX = this.posY = 0;
        this.speedX = this.speedY = 0;
        this.tick = 0;
        this.hitHalfWidth = 0;
        this.hitHalfHeight = 0;
        this.hasHit = false;
        this.homingTarget = (short)-1;
        this.pathX2 = this.pathX1 = 0;
        this.pathY2 = this.pathY1 = 0;
        this.sourceVariant = 0;
    }

    /** True while the projectile is still live (has not hit anything). */
    public final boolean isLive() {
        if (!this.hasHit) {
            return true;
        }
        return false;
    }

    /**
     * Impact: marks the projectile as hit and spawns the matching explosion/effect (types 3-8, 18-20, 31).
     */
    public final void impact(boolean enemyShot) {
        if (this.hasHit == true || this.shotType == 32) {
            return;
        }
        this.hasHit = true;
        switch (this.shotType) {
            case 3:
            case 4:
            case 5: {
                int index = this.shotType - 3;
                if (enemyShot) {
                    this.game.spawnEnemyShot(this.posX, this.posY, 21 + index, true, false, 0, this.sourceVariant);
                    return;
                }
                this.game.spawnPlayerShot(this.posX, this.posY, 21 + index);
                return;
            }
            case 6:
            case 7:
            case 8: {
                int index = this.shotType - 6;
                if (enemyShot) {
                    this.game.spawnEnemyShot(this.posX, this.posY, 24 + index, true, false, 0, this.sourceVariant);
                    return;
                }
                this.game.spawnPlayerShot(this.posX, this.posY, 24 + index);
                return;
            }
            case 18:
            case 19:
            case 20: {
                int index = this.shotType - 18;
                if (enemyShot) {
                    this.game.spawnEnemyShot(this.posX, this.posY, 27 + index, true, false, 0, this.sourceVariant);
                    return;
                }
                this.game.spawnPlayerShot(this.posX, this.posY, 27 + index);
                return;
            }
            case 31:
                if (!((g)null).wasHit) {
                    this.game.spawnEnemyNear(this.posX, this.posY);
                }
                ((g)null).wasHit = false;
                break;
        }
        return;
    }

    /** Weapon (1-7) that fired this projectile type, used to credit kills; 0 = none. */
    public final byte creditedWeapon() {
        if (this.shotType >= 0 && this.shotType <= 2) {
            return 1;
        }
        if (this.shotType >= 21 && this.shotType <= 23) {
            return 2;
        }
        if (this.shotType >= 24 && this.shotType <= 26) {
            return 3;
        }
        if (this.shotType >= 9 && this.shotType <= 11) {
            return 4;
        }
        if (this.shotType >= 12 && this.shotType <= 14) {
            return 5;
        }
        if (this.shotType >= 15 && this.shotType <= 17) {
            return 6;
        }
        if (this.shotType >= 27 && this.shotType <= 29) {
            return 7;
        }
        return 0;
    }

    /**
     * Per-frame update: movement by type, off-screen and wall collision, breakable-tile removal.
     */
    public final void update(boolean enemyShot) {
        int n2;
        int n;
        int screenX = ((g)null).camX;
        int screenY = ((g)null).camY;
        if (this.hasHit == true) {
            this.release();
            return;
        }
        if (this.shotType == -1) {
            return;
        }
        if (this.shotType == 32) {
            if (this.speedX != 0) {
                --this.speedX;
            }
            if (this.speedX == 0) {
                this.posY += this.speedY;
            }
            if (this.posY < this.pathY2 && this.speedX == 0) {
                this.speedY *= -1;
                this.speedX = 15;
            } else if (this.posY > this.pathY1 && this.speedX == 0) {
                this.speedY *= -1;
                this.speedX = 1;
            }
            ++this.tick;
            if (this.tick >= 3) {
                this.tick = 0;
            }
        } else if (this.shotType >= 6 && this.shotType <= 8) {
            this.posX += this.speedX;
            n2 = this.posX >> 8;
            this.posY += this.speedY;
            n = this.posY >> 8;
            if (n2 < -screenX - tileWidth || n2 > -screenX + screenWidth + tileWidth
                    || n < -screenY - tileHeight || n > -screenY + screenHeight + tileHeight) {
                this.impact(enemyShot);
            }
        } else if (this.shotType >= 0 && this.shotType <= 2) {
            this.posX += this.speedX;
            n2 = this.posX >> 8;
            this.posY += this.speedY;
            n = this.posY >> 8;
            if (n2 < -screenX - tileWidth || n2 > -screenX + screenWidth + tileWidth
                    || n < -screenY - tileHeight || n > -screenY + screenHeight + tileHeight) {
                this.release();
            }
        } else if (this.shotType >= 3 && this.shotType <= 5) {
            this.posX += this.speedX;
            n2 = this.posX >> 8;
            n = this.posY >> 8;
            if (this.posX < 0 || this.posY < 0 || n > 17 * tileHeight || n2 > 27 * tileWidth) {
                this.impact(enemyShot);
            } else if (n + halfHeights[this.shotType] >= this.game.floorBelow(n2, n, tileHeight - halfHeights[this.shotType])) {
                this.impact(enemyShot);
            }
            this.posY -= this.speedY;
            this.speedY -= 256;
        } else if (this.shotType >= 9 && this.shotType <= 11) {
            if (this.tick++ == 2) {
                this.release();
            }
        } else if (this.shotType >= 12 && this.shotType <= 14) {
            this.steerHoming(1);
            n2 = this.posX >> 8;
            n = this.posY >> 8;
            if (n2 < -screenX - tileWidth || n2 > -screenX + screenWidth + tileWidth
                    || n < -screenY - tileHeight || n > -screenY + screenHeight + tileHeight) {
                this.release();
            }
        } else if (this.shotType >= 15 && this.shotType <= 17) {
            if (this.tick++ == 3) {
                this.release();
            }
        } else if (this.shotType >= 18 && this.shotType <= 20) {
            if (enemyShot) {
                return;
            }
            this.steerHoming(4);
            n2 = this.posX >> 8;
            n = this.posY >> 8;
            if (n2 < -screenX - tileWidth || n2 > -screenX + screenWidth + tileWidth
                    || n < -screenY - tileHeight || n > -screenY + screenHeight + tileHeight) {
                this.impact(enemyShot);
            }
        } else if (this.shotType >= 21 && this.shotType <= 30) {
            if (++this.tick == 3) {
                this.release();
            }
        } else if (this.shotType == 31) {
            this.posX += this.speedX;
            this.posY += this.speedY;
        }
        if (((this.shotType != 15 || this.shotType != 16 || this.shotType != 17
                || this.shotType != 9 || this.shotType != 10 || this.shotType != 11)
                && this.shotType < 21) || this.shotType == 31) {
            n2 = (this.posX >> 8) / tileWidth;
            n = (this.posY >> 8) / tileHeight;
            if (!this.game.levelMap.isSolid(n2, n)) {
                this.impact(enemyShot);
                if (this.shotType >= 12 && this.shotType <= 14
                        && this.game.levelMap.rawTile(n2, n) == 34) {
                    this.game.levelMap.roomTiles[n2][n] = 0;
                    ((c)null).solidMasks[n2] &= ~(1 << n);
                }
            }
        }
    }

    /** Homing: picks the nearest enemy ahead within range (126 px) and steers toward it. */
    public final void steerHoming(int unused) {
        if (this.homingTarget == -1) {
            int index;
            short targetX;
            short targetY;
            int x = this.posX >> 8;
            int y = this.posY >> 8;
            index = ((g)null).enemySlotCount - 1;
            while (index >= 0) {
                if (this.game.enemies[index].actorKind != -1) {
                    if (this.game.enemies[index].animId == 5) {
                    } else {
                        targetX = this.game.enemies[index].pixelX();
                        targetY = this.game.enemies[index].feetY();
                        if ((targetX - x) * (targetX - x)
                                + (targetY - y) * (targetY - y) <= 15876) {
                            if (this.speedX > 0 ? targetX > x : targetX < x) {
                                this.homingTarget = (short)index;
                                break;
                            }
                        }
                    }
                }
                --index;
            }
        } else if (this.game.enemies[this.homingTarget].actorKind == -1 || this.game.enemies[this.homingTarget].animId == 5) {
            this.homingTarget = (short)-1;
        } else {
            short targetX = this.game.enemies[this.homingTarget].pixelX();
            int targetY = this.game.enemies[this.homingTarget].feetY()
                    + ((d)null).bodyBoxY[this.game.enemies[this.homingTarget].actorKind]
                    + (((d)null).bodyBoxH[this.game.enemies[this.homingTarget].actorKind] >> 1);
            int x = this.posX >> 8;
            int y = this.posY >> 8;
            int dx = targetX - x;
            int dy = targetY - y;
            int speed = speeds[this.shotType] << 8;
            if (this.shotType >= 18 && this.shotType <= 20) {
                if (dy > 0) {
                    this.speedY = speed;
                    if (dy << 8 < this.speedY) {
                        this.speedY = 0;
                    }
                } else {
                    this.speedY = -speed;
                    if (dy << 8 > this.speedY) {
                        this.speedY = 0;
                    }
                }
            } else if (this.tick >= 10) {
                this.pathX2 = this.pathX1;
                this.pathY2 = this.pathY1;
                this.pathX1 = this.posX;
                this.pathY1 = this.posY;
                if (this.game.abs(dx) > this.game.abs(dy)) {
                    if (dx > 0) {
                        this.speedX = speed;
                    } else {
                        this.speedX = -speed;
                    }
                    this.speedY = 0;
                } else {
                    if (dy > 0) {
                        this.speedY = speed;
                    } else {
                        this.speedY = -speed;
                    }
                    this.speedX = 0;
                }
                this.tick = 0;
            }
        }
        ++this.tick;
        this.posX += this.speedX;
        this.posY += this.speedY;
    }

    /** Draws the projectile/effect for its type (sprites, explosion frames or primitives). */
    public final void draw(Graphics graphics) {
        int n = ((g)null).camX;
        int n2 = ((g)null).camY;
        int n6;
        if (this.shotType < 0) {
            return;
        }
        if (this.shotType >= 21 && this.shotType <= 30) {
            int n3 = explosionFrames[this.tick][2];
            byte by = explosionFrames[this.tick][1];
            byte by2 = explosionFrames[this.tick][0];
            int n4 = (this.posX >> 8) + n - (by >> 1) + explosionFrames[this.tick][3];
            int n5 = (this.posY >> 8) + n2 - (n3 >> 1) + explosionFrames[this.tick][4];
            n6 = 0;
            if (n5 < hudHeight) {
                n6 = hudHeight - n5;
                n5 = hudHeight;
            }
            if (n6 < n3 && n6 >= 0 && n4 < screenWidth && n4 + by >= 0) {
                graphics.setClip(n4, n5, (int)by, n3 - n6);
                graphics.drawImage(((g)null).imgExplosion, n4, n5 - by2 - n6, 0);
                return;
            }
        } else if (this.shotType == 32) {
            int n7 = ((g)null).imgFlameBot.getHeight() / 3;
            int n8 = ((g)null).imgFlameBot.getWidth();
            int n9 = this.tick * n7;
            int n10 = (this.posX >> 8) + n - (n8 >> 1);
            int n11 = (this.posY >> 8) + n2 - (n7 >> 1);
            n6 = 0;
            if (n11 < hudHeight) {
                n6 = hudHeight - n11;
                n11 = hudHeight;
            }
            if (n6 < n7 && n6 >= 0 && n10 < screenWidth && n10 + n8 >= 0) {
                graphics.setClip(n10, n11, n8, n7 - n6);
                graphics.drawImage(((g)null).imgFlameBot, n10, n11 - n9 - n6, 0);
                return;
            }
        } else if (this.shotType == 31) {
            int n13 = (this.posX >> 8) + n - (((g)null).imgCannonShot.getWidth() >> 1);
            int n14 = (this.posY >> 8) + n2 - (((g)null).imgCannonShot.getHeight() >> 1);
            if (n13 < screenWidth && n13 + 12 >= 0 && n14 < screenHeight && n14 + 12 >= 0) {
                graphics.setClip(0, (int)hudHeight, (int)screenWidth, screenHeight - hudHeight);
                graphics.drawImage(((g)null).imgCannonShot, n13, n14, 0);
                return;
            }
        } else {
            boolean bl = false;
            boolean bl2 = false;
            int n15 = spriteIds[this.shotType] * 19;
            if (n15 < 0) {
                int n16 = (this.posX >> 8) + n;
                int n17 = (this.posY >> 8) + n2;
                if (this.shotType >= 9 && this.shotType <= 11) {
                    n16 -= this.speedX > 0 ? this.hitHalfWidth : -this.hitHalfWidth;
                    graphics.setClip(0, (int)hudHeight, (int)screenWidth, screenHeight - hudHeight);
                    if (this.tick == 0) {
                        graphics.setColor(255, 255, 255);
                    } else {
                        graphics.setColor(0, 0, 255);
                    }
                    graphics.drawLine(n16, n17, this.speedX > 0 ? n16 + 168 : n16 - 168, n17 + 11);
                    graphics.drawLine(n16, n17, this.speedX > 0 ? n16 + 168 : n16 - 168, n17 + 5);
                    graphics.drawLine(n16, n17, this.speedX > 0 ? n16 + 168 : n16 - 168, n17 - 5);
                    graphics.drawLine(n16, n17, this.speedX > 0 ? n16 + 168 : n16 - 168, n17 - 11);
                    return;
                }
                if (this.shotType >= 12 && this.shotType <= 14) {
                    int n18 = (this.pathX1 >> 8) + n;
                    int n19 = (this.pathY1 >> 8) + n2;
                    int n20 = (this.pathX2 >> 8) + n;
                    int n21 = (this.pathY2 >> 8) + n2;
                    graphics.setClip(0, (int)hudHeight, (int)screenWidth, screenHeight - hudHeight);
                    graphics.setColor(255, 255, 0);
                    graphics.drawLine(n16, n17, n18, n19);
                    graphics.drawLine(n18, n19, n20, n21);
                    graphics.fillRect(n16 - 1, n17 - 1, 3, 3);
                    return;
                }
                if (this.shotType >= 15 && this.shotType <= 17) {
                    if (this.game.player.facingRight) {
                        if (this.posX < this.game.player.posX + this.beamOffset) {
                            this.posX = this.game.player.posX + this.beamOffset;
                        }
                    } else if (this.posX > this.game.player.posX - this.beamOffset) {
                        this.posX = this.game.player.posX - this.beamOffset;
                    }
                    n16 = (this.posX >> 8) + n;
                    n17 = (this.posY >> 8) + n2;
                    n16 -= this.speedX > 0 ? this.hitHalfWidth : -this.hitHalfWidth;
                    graphics.setClip(0, (int)hudHeight, (int)screenWidth, screenHeight - hudHeight);
                    graphics.setColor(0, 0, 255);
                    if (this.speedX > 0) {
                        graphics.fillRoundRect(n16, n17 - 11, 168, 22, 12, 12);
                        graphics.setColor(255, 255, 255);
                        graphics.fillRect(n16 + 4, n17 - 11 + 10, 168, 2);
                        return;
                    }
                    graphics.fillRoundRect(n16 - 168, n17 - 11, 168, 22, 12, 12);
                    graphics.setColor(255, 255, 255);
                    graphics.fillRect(n16 - 4 - 168, n17 - 11 + 10, 168, 2);
                    return;
                }
                graphics.setClip(0, (int)hudHeight, (int)screenWidth, screenHeight - hudHeight);
                graphics.setColor(255, 255, 255);
                graphics.fillRect(n16 - 3, n17 - 3, 6, 6);
                return;
            }
            int n22 = (this.posX >> 8) + n - 9;
            int n23 = (this.posY >> 8) + n2 - 9;
            if (n22 >= screenWidth || n23 >= screenHeight || n22 + 19 < 0 || n23 + 19 < 0) {
                return;
            }
            if (n23 + 19 > hudHeight && n23 < ((g)null).screenHeight) {
                graphics.setClip(0, (int)hudHeight, (int)((g)null).screenWidth, ((g)null).screenHeight - hudHeight);
                short[] sArray = spriteRegions[spriteIds[this.shotType] - 13];
                if (this.speedX > 0) {
                    graphics.drawRegion(((g)null).imgBolts, (int)sArray[0], (int)sArray[1], (int)sArray[2], (int)sArray[3], 0, n22 + sArray[4], n23 + sArray[5], 20);
                    return;
                }
                if (this.speedX == 0) {
                    if (this.speedY < 0) {
                        short s = sArray[5];
                        int n24 = 19 - (sArray[4] + sArray[2]);
                        graphics.drawRegion(((g)null).imgBolts, (int)sArray[0], (int)sArray[1], (int)sArray[2], (int)sArray[3], 6, n22 + s, n23 + n24, 20);
                        return;
                    }
                    int n25 = 19 - (sArray[5] + sArray[3]);
                    short s = sArray[4];
                    graphics.drawRegion(((g)null).imgBolts, (int)sArray[0], (int)sArray[1], (int)sArray[2], (int)sArray[3], 5, n22 + n25, n23 + s, 20);
                    return;
                }
                n22 += 19 - sArray[4] - sArray[2];
                n23 += sArray[5];
                graphics.drawRegion(((g)null).imgBolts, (int)sArray[0], (int)sArray[1], (int)sArray[2], (int)sArray[3], 2, n22, n23, 20);
            }
        }
    }
}
