import java.io.InputStream;
import javax.microedition.lcdui.Graphics;

/*
 * c - Level (tile map).
 *
 * Loads and holds the current level: tile grid, tile flags/collision queries
 * (getTile/isSolid style lookups), per-level static tables, and draws the visible
 * part of the tile map.
 *
 * Naming: this is reconstructed, output-exact Java source (not recovered original source).
 * Member names are descriptive source names; config/d-preobf.map maps every one of them
 * back to its retail (obfuscated) runtime name. Renames are always group-complete (all
 * members sharing a retail name), which reproduces ProGuard's retail constant-pool
 * ordering; see docs/NAMING_SCHEME.md. Library overrides keep their API names,
 * classes keep their retail names, and reconstruction devices keep their opus* names.
 */
public final class c {
    /** Start room of the loaded level (always 0; read by g when a level starts). */
    public static byte startRoom;
    /** Tile set of the level (index into g.tilesetFiles; last byte of the layout record). */
    public static byte levelTileset;
    /** Owning game canvas. */
    public g game;
    /**
     * Raw tile codes of every map layer of the level: [layer][column 0-27][row 0-17] (file byte - 32).
     */
    public static byte[][][] rawTiles;
    /**
     * Solidity bitmask per column of the current room: bit r set = row r is solid (tiles 19-34).
     */
    public static int[] solidMasks;
    /**
     * Current room's display/collision tiles [column][row]: 0-34 = tile image index, > 34 = empty (black).
     */
    public byte[][] roomTiles;
    /** Number of rooms in the current level. */
    public static byte roomCount;
    /** Room links per room: 4 neighbouring room indices (-1 = none). */
    public static byte[][] roomLinks;
    /** Map layer used by each room (index into rawTiles). */
    public static byte[] roomLayer;
    /** Tile width in pixels (copied from g.tileWidth). */
    public static byte tileWidth;
    /** Tile height in pixels (copied from g.tileHeight). */
    public static byte tileHeight;
    /** Top of the play area below the HUD (copied from g.hudHeight). */
    public static byte hudHeight;
    /** Tile animation tick (0-11). */
    public static byte animTick;
    /** Enemy type spawned by tile codes 102-113. */
    public static final byte[] spawnTypesA = new byte[]{15, 15, 16, 17, 18, 18, 19, 20, 21, 21, 22, 23};
    /** Enemy type spawned by tile codes 114-127. */
    public static final byte[] spawnTypesB = new byte[]{3, 4, 5, 0, 1, 2, 6, 7, 7, 9, 10, 10, 12, 13};
    /** Object variant for tile codes 43-46. */
    public static final byte[] objectVariants = new byte[]{0, 2, 1, 3};
    /**
     * Level layout records (one per level): room count, 4 links per room, map layer per room, start value.
     */
    public static final byte[][] levelLayouts = new byte[][]{
        new byte[]{1, -1, -1, -1, -1, 0, 2},
        new byte[]{6, -1, -1, 1, -1, -1, -1, 2, 0, -1, -1, 3, 1, -1, -1, 4, 2, -1, -1, 5, 3, -1, -1, -1, 4, 0, 1, 2, 3, 4, 5, 0},
        new byte[]{4, -1, -1, 1, -1, -1, -1, 2, 0, -1, -1, 3, 1, -1, -1, -1, 2, 0, 1, 2, 3, 1},
        new byte[]{5, -1, -1, 1, -1, -1, -1, 2, 0, -1, -1, 3, 1, -1, -1, 4, 2, -1, -1, -1, 3, 4, 0, 1, 2, 3, 2},
        new byte[]{6, -1, -1, 1, -1, -1, 2, 3, 0, 1, -1, 4, -1, -1, 4, -1, 1, 3, -1, 5, 2, -1, -1, -1, 4, 0, 1, 3, 2, 4, 5, 2},
        new byte[]{5, -1, -1, 1, -1, -1, -1, 2, 0, -1, -1, 3, 1, -1, -1, 4, 2, -1, -1, -1, 3, 2, 1, 0, 3, 4, 2},
        new byte[]{5, -1, -1, 1, -1, -1, -1, 2, 0, -1, -1, 3, 1, -1, -1, 4, 2, -1, -1, -1, 3, 0, 1, 2, 3, 4, 0},
        new byte[]{3, -1, -1, 1, -1, -1, -1, 2, 0, -1, -1, -1, 1, 0, 2, 1, 0},
        new byte[]{4, -1, -1, 1, -1, -1, -1, 2, 0, -1, -1, 3, 1, -1, -1, -1, 2, 0, 1, 2, 3, 1},
        new byte[]{6, -1, 1, 2, -1, 0, -1, 3, -1, -1, 3, 4, 0, 2, -1, -1, 1, -1, -1, 5, 2, -1, -1, -1, 4, 0, 4, 3, 5, 1, 2, 1},
        new byte[]{6, -1, -1, 1, -1, -1, -1, 2, 0, -1, -1, 3, 1, -1, -1, 4, 2, -1, -1, 5, 3, -1, -1, -1, 4, 0, 1, 2, 3, 5, 4, 2},
        new byte[]{12, -1, 1, 8, -1, 0, 2, 9, -1, 1, 3, 10, -1, 2, 4, 11, -1, 3, 5, -1, -1, 4, 6, -1, -1, 5, 7, -1, -1, 6, -1, -1, -1, -1, 9, -1, 0, 8, 10, -1, 1, 9, 11, -1, 2, 10, -1, -1, 3, 0, 1, 2, 3, 0, 4, 1, 2, 5, 3, 4, 5, 1},
        new byte[]{1, -1, -1, -1, -1, 0, 2}
    };

    /** Allocates the level tables (up to 12 rooms of 28 x 18 tiles). */
    public c(g owner) {
        this.game = owner;
        tileWidth = owner.tileWidth;
        tileHeight = owner.tileHeight;
        hudHeight = owner.hudHeight;
        animTick = 0;
        roomLinks = new byte[12][4];
        roomLayer = new byte[12];
        rawTiles = new byte[12][28][18];
        solidMasks = new int[28];
        this.roomTiles = new byte[28][18];
    }

    /** Raw tile code at (column, row) of the current room; -1 outside the 28 x 18 map. */
    public final short rawTile(int column, int row) {
        if (column >= 0 && column < 28 && row >= 0 && row < 18) {
            short value = (short)rawTiles[roomLayer[this.game.currentRoom]][column][row];
            return value;
        }
        return -1;
    }

    /** Selects level layout level: room count, room links and per-room map layers. */
    public final void selectLayout(int level) {
        byte[] data = levelLayouts[level];
        int row;
        int column;
        int cursor;
        cursor = 0;
        roomCount = data[cursor++];
        row = 0;
        while (row < roomCount) {
            column = 0;
            while (column < 4) {
                roomLinks[row][column] = data[cursor++];
                ++column;
            }
            ++row;
        }
        row = 0;
        while (row < roomCount) {
            roomLayer[row] = data[cursor++];
            ++row;
        }
        startRoom = 0;
        levelTileset = data[cursor];
    }

    /** Loads level {@code level}: layout record plus the tile layers from /level<level>.bin. */
    public final void loadLevel(int level) {
        this.selectLayout(level);
        this.game.ensureTileset();
        try {
            InputStream input = this.getClass().getResourceAsStream("/level" + level + ".bin");
            int layer = 0;
            while (layer < roomCount) {
                int row = 0;
                while (row < 28) {
                    int column = 0;
                    while (column < 18) {
                        int tile = input.read() - 32;
                        rawTiles[layer][row][column] = (byte)tile;
                        ++column;
                    }
                    ++row;
                }
                ++layer;
            }
            input.close();
        } catch (Exception ex) {
        }
    }

    /**
     * Enters room {@code room} of the current level: clears all entities and level objects, then scans
     * the raw tile codes to build the display/collision grid roomTiles and spawn objects (platforms,
     * doors, switches, collectibles, crates, enemies, the bounce-bot). With start == true the
     * player start marker (code -125) sets the respawn position.
     */
    public final void enterRoom(int room, boolean start) {
        int n2 = 0;
        int n3 = 0;
        short s;
        short s2;
        boolean bl2 = false;
        boolean bl3 = false;
        boolean bl4 = false;
        this.game.currentRoom = (short)room;
        this.game.applyGateFlag(this.game.levelIndex, room);
        int n4 = 3;
        while (n4 >= 0) {
            this.game.platformDir[n4] = -1;
            --n4;
        }
        n4 = 49;
        while (n4 >= 0) {
            this.game.crateAbove[n4] = (this.game.crateBelow[n4] = -1);
            this.game.crateFall[n4] = 0;
            this.game.crateFalling[n4] = false;
            this.game.crateType[n4] = -1;
            --n4;
        }
        n4 = this.game.enemySlotCount - 1;
        while (n4 >= 0) {
            this.game.enemies[n4].actorKind = (byte)-1;
            --n4;
        }
        this.game.boltColumn = (byte)-1;
        this.game.boltRow = (byte)-1;
        n4 = 3;
        while (n4 >= 0) {
            this.game.railX1[n4] = -1;
            this.game.railX2[n4] = -1;
            --n4;
        }
        n4 = 11;
        while (n4 >= 0) {
            this.game.pickupType[n4] = -1;
            --n4;
        }
        n4 = 2;
        while (n4 >= 0) {
            this.game.doorKind[n4] = 0;
            this.game.lockColumn[n4] = -1;
            --n4;
        }
        n4 = 9;
        while (n4 >= 0) {
            this.game.enemyShots[n4].shotType = (byte)-1;
            --n4;
        }
        n4 = 9;
        while (n4 >= 0) {
            this.game.playerShots[n4].shotType = (byte)-1;
            --n4;
        }
        this.game.bounceBotColumn = (byte)-1;
        this.game.bounceBotRow = (byte)-1;
        this.game.payolaColumn = (byte)-1;
        this.game.payolaRow = (byte)-1;
        this.game.armModuleColumn = (byte)-1;
        this.game.armModuleRow = (byte)-1;
        this.game.imgBounceBot = null;
        System.gc();
        this.game.sleep(20);
        this.game.deadlyPit = true;
        if (roomLinks[room][1] != -1) {
            this.game.deadlyPit = false;
        }
        n4 = 7;
        while (n4 >= 0) {
            this.game.spikeX[n4] = -1;
            --n4;
        }
        n4 = 27;
        while (n4 >= 0) {
            int n5 = 17;
            while (n5 >= 0) {
                int n6;
                s = this.rawTile(n4, n5);
                if (bl2) {
                    this.roomTiles[n4][n5 + 1] = (byte)s;
                } else if (bl3) {
                    if (s % 2 == 0 && s < 12) {
                        this.roomTiles[n4][n5 + 1] = (byte)(s + 1);
                    } else {
                        this.roomTiles[n4][n5 + 1] = (byte)s;
                    }
                }
                if (bl4) {
                    s2 = s;
                    if (this.roomTiles[n4][n5 + 2] >= 19) {
                        n6 = 1;
                        while (s2 >= 47 && s2 <= 55) {
                            s2 = this.rawTile(n4, n5 - n6);
                            ++n6;
                        }
                        if (s2 % 2 == 0 && s2 < 12) {
                            this.roomTiles[n4][n5 + 1] = (byte)(s2 + 1);
                        } else {
                            this.roomTiles[n4][n5 + 1] = (byte)s2;
                        }
                    } else if (s2 >= 47 && s2 <= 55) {
                        n6 = 1;
                        while (s2 >= 47 && s2 <= 55) {
                            s2 = this.rawTile(n4, n5 - n6);
                            ++n6;
                        }
                        this.roomTiles[n4][n5 + 1] = (byte)s2;
                    }
                }
                bl2 = false;
                bl3 = false;
                bl4 = false;
                if (s >= 47 && s <= 55) {
                    bl2 = true;
                    bl4 = true;
                    switch (s) {
                        case 47: {
                            this.game.placeCrate(n4, n5, 0, 0, n2);
                            break;
                        }
                        case 48: {
                            this.game.placeCrate(n4, n5, 2, 0, n2);
                            break;
                        }
                        case 49: {
                            this.game.placeCrate(n4, n5, 1, 0, n2);
                            break;
                        }
                        case 50: {
                            this.game.placeCrate(n4, n5, 0, 0, n2++);
                            this.game.placeCrate(n4, n5, 0, 1, n2);
                            break;
                        }
                        case 51: {
                            this.game.placeCrate(n4, n5, 0, 0, n2++);
                            this.game.placeCrate(n4, n5, 2, 1, n2);
                            break;
                        }
                        case 52: {
                            this.game.placeCrate(n4, n5, 0, 0, n2++);
                            this.game.placeCrate(n4, n5, 1, 1, n2);
                            break;
                        }
                        case 53: {
                            this.game.placeCrate(n4, n5, 0, 2, n2);
                            break;
                        }
                        case 54: {
                            this.game.placeCrate(n4, n5, 2, 2, n2);
                            break;
                        }
                        case 55: {
                            this.game.placeCrate(n4, n5, 1, 2, n2);
                        }
                    }
                    ++n2;
                } else if (s == -97 || s == -96 || s == -95) {
                    this.roomTiles[n4][n5] = 1;
                } else if (s == -98) {
                    this.roomTiles[n4][n5] = 0;
                    this.game.addSpike(n4, n5);
                } else if (s == 35) {
                    if (this.game.titaniumPending(this.game.levelIndex, this.game.currentRoom)) {
                        this.game.boltColumn = (byte)n4;
                        this.game.boltRow = (byte)n5;
                    }
                    bl2 = true;
                } else if (s >= -118 && s <= -99) {
                    bl3 = true;
                } else if (s == -93) {
                    if (this.game.dialoguePending(11)) {
                        this.game.payolaColumn = (byte)n4;
                        this.game.payolaRow = (byte)n5;
                    }
                    bl3 = true;
                } else if (s == -94) {
                    this.game.bounceBotColumn = (byte)n4;
                    this.game.bounceBotRow = (byte)n5;
                    if (this.game.imgBounceBot == null) {
                        try {
                            this.game.imgBounceBot = javax.microedition.lcdui.Image.createImage((String)"/bncbot.png");
                        }
                        catch (Exception exception) {}
                    }
                    bl3 = true;
                } else if (s == -92) {
                    if (this.game.armModuleAvailable == true) {
                        this.game.armModuleColumn = (byte)n4;
                        this.game.armModuleRow = (byte)n5;
                    }
                    bl3 = true;
                } else if (s == 99) {
                    bl3 = true;
                } else if (s == 100) {
                    this.roomTiles[n4][n5] = this.roomTiles[n4 + 1][n5];
                } else if (s == 56) {
                    this.roomTiles[n4][n5] = 27;
                } else if (s == 57) {
                    this.roomTiles[n4][n5] = 22;
                } else if (s == 58) {
                    this.roomTiles[n4][n5] = 20;
                } else if (s == 59) {
                    this.roomTiles[n4][n5] = 21;
                } else if (s == 60) {
                    this.roomTiles[n4][n5] = 22;
                } else if (s == 61) {
                    this.roomTiles[n4][n5] = 22;
                } else if (s == -125) {
                    if (start) {
                        this.game.checkpointRoom = (byte)room;
                        this.game.checkpointColumn = (byte)n4;
                        this.game.checkpointRow = (byte)(n5 - 1);
                        if (n4 < 1) {
                            this.game.checkpointCamX = (short)0;
                        } else {
                            this.game.checkpointCamX = (short)(-tileWidth * (n4 - 1));
                        }
                        if (n5 < 6) {
                            this.game.checkpointCamY = (short)0;
                        } else {
                            this.game.checkpointCamY = (short)(-tileHeight * (n5 - 5));
                        }
                    }
                    this.roomTiles[n4][n5] = 22;
                } else if (s == -127) {
                    this.roomTiles[n4][n5] = 0;
                } else if (s == -126) {
                    this.roomTiles[n4][n5] = 1;
                } else if (s >= 62 && s <= 69) {
                    bl2 = true;
                    n6 = s - 62;
                    this.game.setRailPoint(n4, n5, n6 % 2 == 0, n6 >> 1);
                } else if (s == 97 || s == 98) {
                    bl2 = true;
                } else if (s == -124) {
                    bl3 = true;
                } else if (s >= 43 && s <= 46) {
                    bl2 = true;
                    if (this.rawTile(n4, n5 + 1) >= 19) {
                        bl3 = true;
                        bl2 = false;
                    }
                    this.game.addPlatform(n4, n5, objectVariants[s - 43]);
                } else if (s >= 114 && s <= 127) {
                    bl3 = true;
                    this.game.spawnEnemy(n4, n5, spawnTypesB[s - 114], n3++);
                } else if (s == -128) {
                    bl3 = true;
                    this.game.spawnEnemy(n4, n5, (byte)13, n3++);
                } else if (s >= 102 && s <= 113) {
                    if ((s - 102) % 2 == 0) {
                        bl2 = true;
                    } else {
                        this.roomTiles[n4][n5] = this.roomTiles[n4 + 1][n5];
                    }
                    this.game.spawnEnemy(n4, n5, this.spawnTypesA[s - 102], n3++);
                } else if (s >= 70 && s <= 76 || s == -119) {
                    this.roomTiles[n4][n5] = 1;
                    this.game.addDoor((byte)n4, (byte)n5, (int)s);
                } else if (s >= 36 && s <= 42) {
                    bl3 = true;
                    this.game.addBoltLock((byte)n4, (byte)n5);
                } else if (s >= 77 && s <= 96) {
                    bl3 = true;
                } else if (s == 101) {
                    bl2 = true;
                    n6 = n4 * tileWidth + (tileWidth >> 1);
                    int n7 = (n5 + 1) * tileHeight + (tileHeight >> 1);
                    this.game.spawnEnemyShot(n6 << 8, n7 << 8, 32, true, true, 0, (byte)26);
                } else if (s > 34) {
                    bl2 = true;
                } else {
                    this.roomTiles[n4][n5] = (byte)s;
                }
                --n5;
            }
            --n4;
        }
        this.rebuildSolidMasks();
        this.game.linkCrateStacks();
        if (this.game.arenaActive == true) {
            this.game.roomRespawn[0] = false;
            return;
        }
        this.game.roomRespawn[this.game.currentRoom] = false;
    }

    /** Rebuilds the per-column solidity masks e from the display grid. */
    public final void rebuildSolidMasks() {
        short value;
        int row;
        int column;
        row = 27;
        while (row >= 0) {
            solidMasks[row] = 0;
            column = 0;
            while (column < 18) {
                if ((value = this.roomTiles[row][column]) >= 19 && value <= 27 || value >= 19 && value <= 34) {
                    solidMasks[row] |= 1 << column;
                }
                column = (byte)(column + 1);
            }
            row = (byte)(row - 1);
        }
    }

    /**
     * Draws the visible part of the room (with animated tiles) and the tile-bound overlay objects.
     */
    public final void draw(Graphics graphics) {
        int n = this.game.camX;
        int n2 = this.game.camY;
        int row = (byte)(-n / this.tileWidth);
        int column = (byte)(-n2 / this.tileHeight);
        int rowEnd = row;
        int columnEnd = column;
        int x = row * this.tileWidth + n;
        int y = column * this.tileHeight + n2;
        int n11;
        int n12;
        int n13;
        int n14;
        int drawX = x;
        int drawY = y;
        short tile;
        short s;
        int sourceY;
        int clip;
        rowEnd = (byte)(rowEnd + (this.game.screenWidth / this.tileWidth + 2));
        columnEnd = (byte)(columnEnd + (this.game.screenHeight / this.tileHeight + 2));
        if (rowEnd > 28) {
            rowEnd = 28;
        }
        if (columnEnd > 18) {
            columnEnd = 18;
        }
        if ((this.animTick = (byte)(this.animTick + 1)) >= 12) {
            this.animTick = 0;
        }
        try {
            graphics.setClip(0, (int)this.hudHeight, (int)this.game.screenWidth, this.game.screenHeight - this.hudHeight);
            n11 = row;
            while (n11 < rowEnd) {
                n12 = column;
                while (n12 < columnEnd) {
                    if (drawX >= this.game.screenWidth || drawY >= this.game.screenHeight) {
                        drawY += this.tileHeight;
                    } else {
                        tile = this.roomTiles[n11][n12];
                        if (tile <= 34) {
                            sourceY = 0;
                            clip = 0;
                            if (drawY < this.hudHeight) {
                                clip = this.hudHeight - drawY;
                            }
                            if (clip < this.tileHeight && clip >= 0) {
                                if (this.animTick >> 1 > 2) {
                                    if (tile == 15 || tile == 17) {
                                        tile = (short)(tile + 1);
                                    } else if (tile == 16 || tile == 18) {
                                        tile = (short)(tile - 1);
                                    }
                                }
                                if (this.game.tilesetIndex != 0 && tile == 26) {
                                    if (this.animTick < 6) {
                                tile = (short)13;
                            } else {
                                tile = (short)14;
                            }
                                }
                                graphics.drawImage(this.game.imgTiles[tile], drawX, drawY, 0);
                            }
                        } else {
                            graphics.setColor(0);
                            graphics.fillRect(drawX, drawY, (int)this.tileWidth, (int)this.tileHeight);
                        }
                        if ((s = this.rawTile(n11, n12)) > -127 && s <= -125) {
                            clip = 0;
                            n13 = this.game.imgStartPoint.getWidth();
                            n14 = 0;
                            sourceY = drawY;
                            int frame = s - -126;
                            if (this.animTick >> 1 > 2) {
                                frame += 2;
                            }
                            switch (frame) {
                                case 0:
                                    sourceY += 5;
                                    n14 = 23;
                                    break;
                                case 1:
                                    n14 = 11;
                                    frame = 23;
                                    break;
                                case 2:
                                    sourceY += 5;
                                    n14 = 23;
                                    frame = 34;
                                    break;
                                default:
                                    n14 = 23;
                                    frame = 57;
                                    break;
                            }
                            if (sourceY < this.hudHeight) {
                                clip = this.hudHeight - sourceY;
                                sourceY = this.hudHeight;
                            }
                            if (clip < n14 && clip >= 0) {
                                graphics.setClip(drawX + 1, sourceY, n13, n14 - clip);
                                graphics.drawImage(this.game.imgStartPoint, drawX + 1, sourceY - clip - frame, 0);
                                graphics.setClip(0, (int)this.hudHeight, (int)this.game.screenWidth, this.game.screenHeight - this.hudHeight);
                            }
                        } else if (s == 97 || s == 98) {
                            clip = 0;
                            sourceY = drawY + (this.tileHeight - 19 >> 1);
                            if (sourceY < this.hudHeight) {
                                clip = this.hudHeight - sourceY;
                                sourceY = this.hudHeight;
                            }
                            if (clip < 19 && clip >= 0) {
                                graphics.setClip(drawX + (this.tileWidth - 19 >> 1), sourceY, 19, 19 - clip);
                                graphics.drawImage(this.game.imgBolts, drawX + (this.tileWidth - 19 >> 1), sourceY - clip - 209, 0);
                                graphics.setClip(0, (int)this.hudHeight, (int)this.game.screenWidth, this.game.screenHeight - this.hudHeight);
                            }
                        } else if (s == 35 && this.game.titaniumPending(this.game.levelIndex, this.game.currentRoom)) {
                            clip = 0;
                            sourceY = drawY;
                            if (sourceY < this.hudHeight) {
                                clip = this.hudHeight - sourceY;
                                sourceY = this.hudHeight;
                            }
                            if (clip < 19 && clip >= 0) {
                                graphics.setClip(drawX + (this.tileWidth - 19 >> 1), sourceY, 19, 19 - clip);
                                graphics.drawImage(this.game.imgBolts, drawX + (this.tileWidth - 19 >> 1), sourceY - clip - 76, 0);
                                graphics.setClip(0, (int)this.hudHeight, (int)this.game.screenWidth, this.game.screenHeight - this.hudHeight);
                            }
                        } else if (s >= 77 && s <= 96) {
                            clip = 0;
                            n13 = this.game.imgCrates.getWidth();
                            n14 = this.game.imgCrates.getHeight() >> 3;
                            sourceY = drawY + this.tileHeight - n14;
                            int frame = this.game.dialoguePending(s - 77) ? 3 * n14 : 4 * n14;
                            if (s >= 81 && s <= 87 && s != 85) {
                                frame = this.game.dialoguePending(s - 77) ? 7 * n14 : 8 * n14;
                            }
                            if (s == 80) {
                                frame += 2 * n14;
                            }
                            if (sourceY < this.hudHeight) {
                                clip = this.hudHeight - sourceY;
                                sourceY = this.hudHeight;
                            }
                            if (clip < n14 && clip >= 0) {
                                graphics.setClip(drawX + (this.tileWidth - n13 >> 1), sourceY, n13, n14 - clip);
                                graphics.drawImage(this.game.imgCrates, drawX + (this.tileWidth - n13 >> 1), sourceY - clip - frame, 0);
                                graphics.setClip(0, (int)this.hudHeight, (int)this.game.screenWidth, this.game.screenHeight - this.hudHeight);
                            }
                        }
                        drawY += this.tileHeight;
                    }
                    n12 = (byte)(n12 + 1);
                }
                drawY = y;
                drawX += this.tileWidth;
                n11 = (byte)(n11 + 1);
            }
            return;
        } catch (Exception ex) {
            System.out.println("ERROR1. " + ex);
            return;
        }
    }

    /**
     * True if the tile at (column, row) can be stood on / blocks movement (doors depend on unlocked bits).
     */
    public final boolean isSolid(int column, int row) {
        if (column < 0 || column >= 28 || row < 0 || row >= 18) {
            return false;
        }
        byte cell = this.roomTiles[column][row];
        short value = this.rawTile(column, row);
        short next = this.rawTile(column, row + 1);
        int mask = value - 70;
        int nextMask = next - 70;
        mask = 1 << mask;
        nextMask = 1 << nextMask;
        if (value >= 70 && value <= 76 && (this.game.doorBits & mask) != 0 ||
                next >= 70 && next <= 76 && (this.game.doorBits & nextMask) != 0) {
            return false;
        }
        if ((value == -119 || next == -119) && (this.game.doorBits & 128) != 0) {
            return false;
        }
        return cell >= 0 && cell <= 19;
    }
}
