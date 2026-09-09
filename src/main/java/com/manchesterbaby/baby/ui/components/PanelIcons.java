package com.manchesterbaby.baby.ui.components;

import javax.swing.Icon;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.LinearGradientPaint;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.imageio.ImageIO;

/**
 * Java2D replacements for the front panel control sprites.
 *
 * <p>Shapes, sizes and colours are traced from the original photographic sprites, so the
 * hand-tuned pixel layout in {@link com.manchesterbaby.baby.ui.CrtControlPanel},
 * {@link com.manchesterbaby.baby.ui.StaticisorPanel} and
 * {@link com.manchesterbaby.baby.ui.TypewriterPanel} is unaffected. Because these paint
 * vectors instead of blitting a fixed bitmap they stay sharp at any
 * {@code sun.java2d.uiScale}.</p>
 */
public final class PanelIcons {

    /** Grey key switch top. */
    public static final Color KEY_GREY = new Color(0xADADAD);
    /** Aged ivory key switch top, the panel's "white" keys. */
    public static final Color KEY_IVORY = new Color(0xC9C28A);

    // The toggle is a metal bushing seen face on: concentric bands of a soft fringe, rim,
    // groove, collar, inner groove and centre boss, drawn outermost first. Radii, colours
    // and alpha are measured from the original sprites - for each band, the alpha-weighted
    // average across its top, middle and bottom thirds. The alpha matters as much as the
    // colour: the original's outer edge is only about a third covered, which is what stops
    // it reading as a solid grommet. Two tables, because flipping the bat changes the light.
    private static final double[] TOGGLE_BAND_RADIUS = {13.8, 12.4, 11.0, 8.6, 5.2, 2.8};
    private static final float[] TOGGLE_BAND_STOPS = {0.2f, 0.5f, 0.8f};
    // Unit vector towards the lit side: up, and about 18 degrees to the left. Lighting the
    // bands straight down instead leaves the right-hand side far too bright.
    private static final double TOGGLE_LIGHT_X = -0.309;
    private static final double TOGGLE_LIGHT_Y = -0.951;
    private static final int[][] TOGGLE_BANDS_UP = {
            {0x4D918C4D, 0x4E807C47, 0x98373526},   // fringe
            {0x9977734D, 0xD6403E2A, 0xF3201E18},   // rim
            {0xF63F3D34, 0xFE28261F, 0xFF2F2C22},   // groove
            {0xFF353026, 0xFD504C3B, 0xFA4F4B39},   // collar
            {0xFB342F22, 0xF82D271D, 0xFD383021},   // inner groove
            {0xFF4B432F, 0xFF433C2A, 0xFF30291D}    // centre boss
    };
    private static final int[][] TOGGLE_BANDS_DOWN = {
            {0x32918C50, 0x467E7A47, 0xB02C291E},
            {0x836E6B4A, 0xCB393827, 0xF8201D18},
            {0xF34A4A40, 0xFD27251F, 0xFF29261F},
            {0xFF5F5946, 0xFF585340, 0xFF3A3529},
            {0xFF645C49, 0xFF322B20, 0xFF2F2A1E},
            {0xFF38311F, 0xFF322C1E, 0xFF4F4832}
    };
    // A hard specular on the top of the rim, much stronger with the bat down because
    // nothing is in front of it. Averaging it into the rim band just greys the whole ring.
    private static final Color TOGGLE_SPECULAR_UP = new Color(0xC89D9C9A, true);
    private static final Color TOGGLE_SPECULAR_DOWN = new Color(0xEBDCD9D6, true);
    private static final Color TOGGLE_SPECULAR_FADE = new Color(0x00A09C90, true);
    private static final Color TOGGLE_TAIL_TOP = new Color(0x231E19);
    private static final Color TOGGLE_TAIL_TIP = new Color(0x0B0809);
    private static final Color BAT_MID = new Color(0x8A8168);
    private static final Color BAT_EDGE = new Color(0x4E4835);

    private static final Color RED_DARK = new Color(0x550702);
    private static final Color RED_MID = new Color(0xA8300E);
    private static final Color RED_HI = new Color(0xD1793D);
    private static final Color RED_RIM = new Color(0xC96A2E);

    private static final Color SHAFT_LO = new Color(0xB8B77E);
    private static final Color SHAFT_HI = new Color(0xFEFFBF);

    private static double scale = 1;

    // Which artwork the controls draw: the traced vectors, or the original sprites they
    // were traced from. Every sprite is the same size as the vector that replaced it, so
    // switching changes only the pixels, never the layout. Vectors by default;
    // -Dbaby.icons=bitmap starts on the originals and Settings > Original Artwork flips
    // between them while running, which is the only way to compare them honestly.
    private static boolean useBitmaps = "bitmap".equalsIgnoreCase(System.getProperty("baby.icons"));

    private static final Map<String, Image> SPRITES = new HashMap<>();

    private PanelIcons() {
    }

    /**
     * Sets the size these icons draw at, as a multiple of their design size. The panel
     * layout drives this so the controls grow and shrink with the machine they sit on.
     * There is only ever one front panel on screen, so one shared value is enough.
     *
     * @param newScale the scale to draw at
     */
    public static void setScale(double newScale) {
        scale = newScale > 0 ? newScale : 1;
    }

    /** True when the original bitmap sprites are being drawn instead of the vectors. */
    public static boolean useBitmaps() {
        return useBitmaps;
    }

    /**
     * Chooses between the original sprites and the vector artwork. Repaint to see it.
     *
     * @param bitmaps true to draw the original sprites
     */
    public static void setUseBitmaps(boolean bitmaps) {
        useBitmaps = bitmaps;
    }

    /**
     * The original sprite of the given file name, or null if it cannot be loaded. A
     * handful of small files, so they are read once and kept.
     *
     * @param name file name under /images
     * @return the sprite, or null
     */
    public static synchronized Image sprite(String name) {
        if (!SPRITES.containsKey(name)) {
            SPRITES.put(name, readSprite(name));
        }
        return SPRITES.get(name);
    }

    private static Image readSprite(String name) {
        try (InputStream in = PanelIcons.class.getResourceAsStream("/images/" + name)) {
            if (in == null) {
                System.err.println("Couldn't find sprite: " + name);
                return null;
            }
            return ImageIO.read(in);
        } catch (IOException e) {
            System.err.println("Error loading sprite " + name + ": " + e.getMessage());
            return null;
        }
    }

    /** Toggle switch with the bat up. Replaces the 32x33 toggle_switch_up.png. */
    public static Icon toggleUp() {
        return new PanelIcon("toggle_switch_up.png", 32, 33, g -> paintToggle(g, false));
    }

    /** Toggle switch with the bat down. Replaces the 32x33 toggle_switch_down.png. */
    public static Icon toggleDown() {
        return new PanelIcon("toggle_switch_down.png", 32, 33, g -> paintToggle(g, true));
    }

    /**
     * An empty switch hole, used for a disconnected toggle position. The original
     * hole.gif was fully transparent - the panel photograph shows through it - so this
     * draws nothing and exists only to reserve the same 32x32 of layout space.
     */
    public static Icon hole() {
        return new PanelIcon("hole.gif", 32, 32, g -> {
        });
    }

    /** Typewriter push button, released. Replaces the 30x33 pushout.gif. */
    public static Icon pushOut() {
        return new PanelIcon("pushout.gif", 30, 33, g -> paintPush(g, 15.0, 17.5, 13.0, false));
    }

    /** Typewriter push button, pressed. Replaces the 30x33 pushin.gif. */
    public static Icon pushIn() {
        return new PanelIcon("pushin.gif", 30, 33, g -> paintPush(g, 15.0, 13.5, 13.0, true));
    }

    /** Interlocking monitor-select button, out. Replaces the 25x29 ibuttonout.gif. */
    public static Icon interlockOut() {
        return new PanelIcon("ibuttonout.gif", 25, 29, g -> paintPush(g, 11.5, 17.0, 10.5, false));
    }

    /** Interlocking monitor-select button, in. Replaces the 25x29 ibuttonin.gif. */
    public static Icon interlockIn() {
        return new PanelIcon("ibuttonin.gif", 25, 29, g -> paintPush(g, 14.5, 14.0, 10.5, true));
    }

    /** Key switch at rest, only the rounded top showing. Replaces greyup.png / whiteup.png. */
    public static Icon keyUp(Color top) {
        return new PanelIcon(KEY_IVORY.equals(top) ? "whiteup.png" : "greyup.png", 29, 32, g -> paintKey(g, top, false));
    }

    /** Key switch pressed, dropped far enough to expose the shaft. Replaces greydown.png / whitedown.png. */
    public static Icon keyDown(Color top) {
        return new PanelIcon(KEY_IVORY.equals(top) ? "whitedown.png" : "greydown.png", 29, 32, g -> paintKey(g, top, true));
    }

    // Traced from the original sprite: a 25x24 bushing centred at (16, 15.5), with the bat
    // showing as a bright nub that sits high when up and low when down.
    private static void paintToggle(Graphics2D g, boolean down) {
        double cx = 16.0;
        double cy = 15.5;
        double squash = 0.96; // the footprint is a touch wider than it is tall
        int[][] bands = down ? TOGGLE_BANDS_DOWN : TOGGLE_BANDS_UP;

        for (int band = 0; band < TOGGLE_BAND_RADIUS.length; band++) {
            double r = TOGGLE_BAND_RADIUS[band];
            double ry = r * squash;
            g.setPaint(new LinearGradientPaint(
                    new Point2D.Double(cx + TOGGLE_LIGHT_X * r, cy + TOGGLE_LIGHT_Y * ry),
                    new Point2D.Double(cx - TOGGLE_LIGHT_X * r, cy - TOGGLE_LIGHT_Y * ry),
                    TOGGLE_BAND_STOPS,
                    new Color[]{new Color(bands[band][0], true), new Color(bands[band][1], true),
                            new Color(bands[band][2], true)}));
            g.fill(new Ellipse2D.Double(cx - r, cy - ry, r * 2, ry * 2));
        }

        if (down) {
            // the bat projects just below the bushing, tapering to a rounded point and
            // darkening into it rather than sitting on it as a hard wedge
            Path2D tail = new Path2D.Double();
            tail.moveTo(cx - 4.2, cy + 11.0);
            tail.curveTo(cx - 3.9, cy + 14.2, cx - 1.4, cy + 15.4, cx + 0.4, cy + 15.9);
            tail.curveTo(cx + 2.2, cy + 15.2, cx + 4.1, cy + 14.0, cx + 4.4, cy + 11.0);
            tail.closePath();
            g.setPaint(new LinearGradientPaint(
                    new Point2D.Double(cx, cy + 10.0), new Point2D.Double(cx, cy + 15.0),
                    new float[]{0f, 1f}, new Color[]{TOGGLE_TAIL_TOP, TOGGLE_TAIL_TIP}));
            g.fill(tail);
        }

        // hard highlight where the light catches the top of the rim
        double specX = down ? 18.0 : 18.5;
        double specY = down ? 5.7 : 5.0;
        double specW = down ? 5.6 : 4.6;
        double specH = down ? 3.6 : 3.0;
        g.setPaint(new RadialGradientPaint(
                new Point2D.Double(specX, specY), (float) (specW / 2),
                new float[]{0f, 1f},
                new Color[]{down ? TOGGLE_SPECULAR_DOWN : TOGGLE_SPECULAR_UP,
                        TOGGLE_SPECULAR_FADE}));
        g.fill(new Ellipse2D.Double(specX - specW / 2, specY - specH / 2, specW, specH));

        // the bat itself, catching the light
        double batX = down ? 16.2 : 16.5;
        double batY = down ? 20.9 : 11.6;
        double batW = down ? 3.8 : 3.2;
        double batH = 4.2;
        g.setPaint(new RadialGradientPaint(
                new Point2D.Double(batX, batY - 0.4), (float) batH,
                new float[]{0f, 0.34f, 1f},
                new Color[]{new Color(down ? 0xBDB8B6 : 0xC8C3BC), BAT_MID, BAT_EDGE}));
        g.fill(new Ellipse2D.Double(batX - batW / 2, batY - batH / 2, batW, batH));
    }

    // Red bakelite button sunk into the panel; shared by the typewriter keys and the
    // smaller interlocking monitor-select buttons, which differ only in size and offset.
    private static void paintPush(Graphics2D g, double cx, double cy, double r, boolean in) {
        g.setColor(RED_DARK);
        g.fill(new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));

        g.setPaint(new RadialGradientPaint(
                new Point2D.Double(cx - r * 0.35, cy - r * 0.5), (float) (r * 1.7),
                new float[]{0f, 0.5f, 1f},
                new Color[]{RED_HI, RED_MID, RED_DARK}));
        g.fill(new Ellipse2D.Double(cx - r + 0.5, cy - r + 0.5, r * 2 - 1, r * 2 - 1));

        // recessed centre, shallower once the button is pushed in
        double ir = r * (in ? 0.46 : 0.56);
        double iy = cy + (in ? 0.5 : 1.5);
        g.setPaint(new RadialGradientPaint(
                new Point2D.Double(cx, iy - ir * 0.4), (float) (ir * 1.8),
                new float[]{0f, 1f},
                new Color[]{in ? RED_MID : RED_DARK, RED_DARK}));
        g.fill(new Ellipse2D.Double(cx - ir, iy - ir, ir * 2, ir * 2));

        g.setColor(RED_RIM);
        g.setStroke(new BasicStroke(1f));
        g.draw(new Arc2D.Double(cx - r + 1, cy - r + 1, r * 2 - 2, r * 2 - 2, 40, 100, Arc2D.OPEN));
    }

    // Ball on a plunger: at rest only the top shows, pressed it drops and reveals the shaft.
    private static void paintKey(Graphics2D g, Color top, boolean down) {
        double cx = 14.0;
        double cy = down ? 22.5 : 11.5;
        double r = 7.5;

        if (down) {
            g.setPaint(new GradientPaint((float) cx - 2.5f, 0f, SHAFT_LO, (float) cx + 0.5f, 0f, SHAFT_HI));
            g.fill(new Rectangle2D.Double(cx - 2.5, 4, 5, cy - 4));
        }

        g.setPaint(new RadialGradientPaint(
                new Point2D.Double(cx - r * 0.35, cy - r * 0.4), (float) (r * 1.7),
                new float[]{0f, 0.55f, 1f},
                new Color[]{shade(top, 1.28), top, shade(top, 0.5)}));
        g.fill(new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));

        g.setColor(new Color(255, 255, 255, 110));
        g.fill(new Ellipse2D.Double(cx - r * 0.55, cy - r * 0.6, r * 0.5, r * 0.35));
    }

    private static Color shade(Color c, double factor) {
        return new Color(
                Math.min(255, (int) (c.getRed() * factor)),
                Math.min(255, (int) (c.getGreen() * factor)),
                Math.min(255, (int) (c.getBlue() * factor)));
    }

    /**
     * One control's artwork, at the current panel scale. Holds both renderings of it: the
     * vector painter and the name of the sprite it was traced from, and draws whichever
     * {@link #useBitmaps} currently selects. Both are the same design size, so the icon
     * measures the same either way and nothing around it moves when the choice changes.
     */
    private static final class PanelIcon implements Icon {
        private final String sprite;
        private final int width;
        private final int height;
        private final Consumer<Graphics2D> painter;

        PanelIcon(String sprite, int width, int height, Consumer<Graphics2D> painter) {
            this.sprite = sprite;
            this.width = width;
            this.height = height;
            this.painter = painter;
        }

        @Override
        public int getIconWidth() {
            return (int) Math.round(width * scale);
        }

        @Override
        public int getIconHeight() {
            return (int) Math.round(height * scale);
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Image bitmap = useBitmaps ? sprite(sprite) : null;
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                if (bitmap != null) {
                    // the originals are 25-33px sprites, so enlarging them is interpolation
                    // either way; bilinear at least keeps it soft rather than blocky
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g.drawImage(bitmap, x, y, getIconWidth(), getIconHeight(), component);
                } else {
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g.translate(x, y);
                    g.scale(scale, scale);
                    painter.accept(g);
                }
            } finally {
                g.dispose();
            }
        }
    }
}
