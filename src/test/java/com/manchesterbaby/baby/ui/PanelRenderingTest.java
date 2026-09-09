package com.manchesterbaby.baby.ui;

import com.manchesterbaby.baby.core.Store;
import com.manchesterbaby.baby.ui.components.InterlockingPushButton;
import com.manchesterbaby.baby.ui.components.KeySwitch;
import com.manchesterbaby.baby.ui.components.PanelIcons;
import com.manchesterbaby.baby.ui.components.PushButton;
import com.manchesterbaby.baby.ui.components.ToggleSwitch;

import org.junit.jupiter.api.Test;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Checks the Java2D panel artwork that replaced the sprite files: the icons must keep the
 * footprint the hand-tuned panel layouts were built around, must actually draw something,
 * and pressed/released states must be visibly different.
 */
class PanelRenderingTest {

    private static BufferedImage render(Icon icon) {
        BufferedImage image = new BufferedImage(
                icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        icon.paintIcon(null, g, 0, 0);
        g.dispose();
        return image;
    }

    private static int opaquePixels(BufferedImage image) {
        int count = 0;
        for (int y = 0; y < image.getHeight(); y++)
            for (int x = 0; x < image.getWidth(); x++)
                if (((image.getRGB(x, y) >>> 24) & 0xff) > 128) count++;
        return count;
    }

    private static void assertSize(Icon icon, int width, int height) {
        assertEquals(width, icon.getIconWidth(), "icon width");
        assertEquals(height, icon.getIconHeight(), "icon height");
    }

    @Test
    void iconsKeepTheFootprintOfTheSpritesTheyReplaced() {
        assertSize(PanelIcons.toggleUp(), 32, 33);
        assertSize(PanelIcons.toggleDown(), 32, 33);
        assertSize(PanelIcons.hole(), 32, 32);
        assertSize(PanelIcons.pushOut(), 30, 33);
        assertSize(PanelIcons.pushIn(), 30, 33);
        assertSize(PanelIcons.interlockOut(), 25, 29);
        assertSize(PanelIcons.interlockIn(), 25, 29);
        assertSize(PanelIcons.keyUp(PanelIcons.KEY_GREY), 29, 32);
        assertSize(PanelIcons.keyDown(PanelIcons.KEY_IVORY), 29, 32);
    }

    @Test
    void originalSpritesLoadAndKeepTheSameFootprint() {
        try {
            PanelIcons.setUseBitmaps(true);
            assertSize(PanelIcons.toggleUp(), 32, 33);
            assertSize(PanelIcons.pushIn(), 30, 33);
            assertSize(PanelIcons.keyUp(PanelIcons.KEY_IVORY), 29, 32);

            for (String sprite : new String[]{"toggle_switch_up.png", "toggle_switch_down.png",
                    "hole.gif", "pushin.gif", "pushout.gif", "ibuttonin.gif", "ibuttonout.gif",
                    "greyup.png", "greydown.png", "whiteup.png", "whitedown.png",
                    "bit0.gif", "bit1.gif", "bit0bright.gif", "bit1bright.gif", "mainonlamp.png"}) {
                assertNotNull(PanelIcons.sprite(sprite), sprite + " should load");
            }

            // the bitmap really is what gets painted, not the vector underneath it
            BufferedImage bitmap = render(PanelIcons.pushIn());
            PanelIcons.setUseBitmaps(false);
            BufferedImage vector = render(PanelIcons.pushIn());
            assertNotEquals(bitmap.getRGB(15, 17), vector.getRGB(15, 17),
                    "bitmap and vector artwork should differ");
        } finally {
            PanelIcons.setUseBitmaps(false);
        }
    }

    @Test
    void hoveringDoesNotChangeHowAControlLooks() {
        // GTK enables rollover and, with no rollover icon set, paints the selected icon
        // instead: hovering an off switch drew it on. The panel has no hover state, so no
        // control may react to the pointer.
        for (AbstractButton button : new AbstractButton[]{
                new ToggleSwitch("x"), new PushButton("x"), new InterlockingPushButton("x"),
                new KeySwitch("x", KeySwitch.KeyColour.GREY)}) {
            String name = button.getClass().getSimpleName();
            assertFalse(button.isRolloverEnabled(), name + " should not react to the pointer");

            button.setSize(48, 48);
            new JPanel(null).add(button);
            BufferedImage plain = paint(button);
            button.getModel().setRollover(true);
            assertEquals(0, difference(plain, paint(button)), name + " changed when hovered");
        }
    }

    private static BufferedImage paint(AbstractButton button) {
        BufferedImage image = new BufferedImage(
                button.getWidth(), button.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        button.paint(g);
        g.dispose();
        return image;
    }

    private static int difference(BufferedImage a, BufferedImage b) {
        int changed = 0;
        for (int y = 0; y < a.getHeight(); y++)
            for (int x = 0; x < a.getWidth(); x++)
                if (a.getRGB(x, y) != b.getRGB(x, y)) changed++;
        return changed;
    }

    @Test
    void iconsDrawSomething() {
        for (Icon icon : new Icon[]{
                PanelIcons.toggleUp(), PanelIcons.toggleDown(),
                PanelIcons.pushOut(), PanelIcons.pushIn(),
                PanelIcons.interlockOut(), PanelIcons.interlockIn(),
                PanelIcons.keyUp(PanelIcons.KEY_GREY), PanelIcons.keyDown(PanelIcons.KEY_IVORY)}) {
            assertTrue(opaquePixels(render(icon)) > 50, "icon painted almost nothing");
        }
    }

    @Test
    void holeIsEmptySoThePanelShowsThrough() {
        assertEquals(0, opaquePixels(render(PanelIcons.hole())));
    }

    @Test
    void pressedStatesLookDifferentFromReleased() {
        assertFalse(sameArtwork(PanelIcons.toggleUp(), PanelIcons.toggleDown()));
        assertFalse(sameArtwork(PanelIcons.pushOut(), PanelIcons.pushIn()));
        assertFalse(sameArtwork(PanelIcons.interlockOut(), PanelIcons.interlockIn()));
        assertFalse(sameArtwork(
                PanelIcons.keyUp(PanelIcons.KEY_GREY), PanelIcons.keyDown(PanelIcons.KEY_GREY)));
        // the two key colours must be distinguishable from each other too
        assertFalse(sameArtwork(
                PanelIcons.keyUp(PanelIcons.KEY_GREY), PanelIcons.keyUp(PanelIcons.KEY_IVORY)));
    }

    private static boolean sameArtwork(Icon a, Icon b) {
        return Arrays.equals(pixels(render(a)), pixels(render(b)));
    }

    private static int[] pixels(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        return image.getRGB(0, 0, w, h, null, 0, w);
    }

    /**
     * Controls sit at fixed places on a photograph of the machine, so scaling has to be
     * uniform and exact - a control that drifts off its hole is worse than one that is
     * simply small.
     */
    @Test
    void scaledLayoutKeepsControlsOnTheirPlaceOnThePanel() {
        JPanel parent = new JPanel(new ScaledLayout(700, 890));
        JPanel control = new JPanel();
        parent.add(control, new Rectangle(118, 392, 58, 51));

        parent.setSize(700, 890);
        parent.doLayout();
        assertEquals(new Rectangle(118, 392, 58, 51), control.getBounds(),
                "at the design size the layout should be 1:1");

        parent.setSize(1400, 1780);
        parent.doLayout();
        assertEquals(new Rectangle(236, 784, 116, 102), control.getBounds(),
                "doubling the panel should double the control");
    }

    @Test
    void scaledLayoutLetterboxesRatherThanStretching() {
        ScaledLayout layout = new ScaledLayout(700, 890);
        JPanel parent = new JPanel(layout);
        JPanel wholePanel = new JPanel();
        parent.add(wholePanel, new Rectangle(0, 0, 700, 890));

        parent.setSize(1100, 700); // far wider than the design
        parent.doLayout();

        Rectangle content = layout.getContentBounds();
        assertEquals(700, content.height, "should fill the tight dimension");
        assertEquals(550, content.width, "aspect ratio must survive");
        assertEquals((1100 - content.width) / 2, content.x, "should be centred");
        assertEquals(content, wholePanel.getBounds(),
                "the photograph and a full-panel child must land on the same pixels");
    }

    /**
     * A panel of controls nested inside the main panel must position its children from the
     * same transform the main panel uses. Working a second transform out from bounds that
     * have already been rounded puts controls a pixel from where they belong, which reads
     * as uneven spacing across a grid.
     */
    @Test
    void nestedPanelsUseTheSameTransformAsTheMainPanel() {
        ScaledLayout rootLayout = new ScaledLayout(700, 890);
        JPanel root = new JPanel(rootLayout);
        JPanel section = new JPanel(new ScaledLayout(700, 267));
        JPanel control = new JPanel();
        section.add(control, new Rectangle(118, 108, 58, 51));
        root.add(section, new Rectangle(0, 386, 700, 267));

        // a scale whose rounding lands exactly on a half pixel
        root.setSize(903, 1149);
        root.doLayout();
        section.doLayout();

        Point onPanel = SwingUtilities.convertPoint(section, control.getLocation(), root);
        assertEquals(152, onPanel.x, "nested control drifted horizontally");
        assertEquals(638, onPanel.y, "nested control drifted vertically");
        assertEquals(75, control.getWidth(), "nested control width");
        assertEquals(66, control.getHeight(), "nested control height");
    }

    /**
     * A click on the CRT has to land on the line the user aimed at. The raster is stretched
     * to fill the panel's height, but a mouse position is mapped back through the single
     * width-derived scale - which only holds while the panel keeps the CRT's aspect ratio.
     * Rounding makes the two ratios differ slightly, so check every line, not just one.
     */
    @Test
    void crtClicksMapBackToTheLineUnderThem() {
        CrtPanel crt = new CrtPanel(new Store(), null);
        int width = 516; // the size ScaledLayout gives the CRT on a panel scaled to 1.29x
        int height = 374;
        crt.setSize(width, height);

        for (int line = 0; line < 32; line++) {
            // middle of where this line is actually drawn, which follows the height
            double onScreen = (CrtPanel.DEFAULT_RASTER_OFFSET_Y + CrtPanel.DEFAULT_SPACING
                    + line * CrtPanel.DEFAULT_BIT_LENGTH + 1.5)
                    * height / (double) CrtPanel.DEFAULT_CRT_HEIGHT;

            int decoded = (crt.toLogicalY((int) onScreen)
                    - CrtPanel.DEFAULT_RASTER_OFFSET_Y - CrtPanel.DEFAULT_SPACING)
                    / CrtPanel.DEFAULT_BIT_LENGTH;

            assertEquals(line, decoded, "a click on line " + line + " decoded elsewhere");
        }
    }

    /**
     * A CRT bit is a dot for 0 and a longer dash for 1, drawn brighter on the action line.
     * Losing either distinction would make the store display unreadable.
     */
    @Test
    void crtBitsAreDotsAndDashes() {
        assertTrue(litWidth(true, false) > litWidth(false, false), "a 1 must be longer than a 0");
        assertTrue(litWidth(true, true) > litWidth(false, true), "a bright 1 must be longer than a bright 0");
        assertTrue(brightness(true, true) > brightness(true, false), "the action line must be brighter");
        assertTrue(brightness(false, true) > brightness(false, false), "the action line must be brighter");
    }

    // width of the lit mark along the middle row of a bit's tile
    private static int litWidth(boolean one, boolean bright) {
        BufferedImage tile = bitTile(one, bright);
        int unlit = tile.getRGB(6, 0); // far end of the tile is never part of the mark
        int lit = 0;
        for (int x = 0; x < tile.getWidth(); x++)
            if (tile.getRGB(x, 1) != unlit) lit++;
        assertTrue(lit > 0, "bit drew no mark at all");
        return lit;
    }

    private static int brightness(boolean one, boolean bright) {
        int rgb = bitTile(one, bright).getRGB(1, 1);
        return ((rgb >> 16) & 0xff) + ((rgb >> 8) & 0xff) + (rgb & 0xff);
    }

    private static BufferedImage bitTile(boolean one, boolean bright) {
        BufferedImage image = new BufferedImage(7, 3, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        CrtPanel.drawBit(g, 0, 0, one, bright);
        g.dispose();
        return image;
    }
}
