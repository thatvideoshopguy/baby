package com.manchesterbaby.baby.ui;

import javax.swing.JPanel;

import com.manchesterbaby.baby.ui.components.PanelIcons;
import com.manchesterbaby.baby.utils.ImageUtils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * A custom JPanel that displays the Main Panel texture.
 */
public class BackgroundPanel extends JPanel {

    // The design coordinate space for the whole front panel
    public static final int DESIGN_WIDTH = 700;
    public static final int DESIGN_HEIGHT = 890;

    /** Shown around the panel when the window's aspect ratio does not match the design. */
    private static final Color SURROUND = new Color(0x14150F);

    // Cached image
    private static final Image DEFAULT_PANEL = ImageUtils.loadImage("/images/main.jpg", 800000);

    // Natural size of the panel photograph. The texture is stretched to fill the component,
    // so anything drawn on top has to be scaled by the same factors to stay registered.
    private static final double PANEL_IMAGE_WIDTH = 825;
    private static final double PANEL_IMAGE_HEIGHT = 1077;

    // The stop lamp, in the panel photograph's own pixel coordinates
    private static final double LAMP_X = 104;
    private static final double LAMP_Y = 44;
    private static final double LAMP_WIDTH = 31;
    private static final double LAMP_BODY_HEIGHT = 28;
    private static final double LAMP_CAP_HEIGHT = 29;
    private static final double LAMP_FADE_HEIGHT = 5;

    // The dark holder the top of the tube disappears into.
    private static final Color LAMP_HOLDER = new Color(0x1B1916);

    // Across the tube: amber at the glass edges, filament white down the middle.
    private static final float[] LAMP_STOPS = {0f, 0.18f, 0.34f, 0.44f, 0.53f, 0.63f, 0.76f, 1f};
    private static final Color[] LAMP_COLOURS = {
            new Color(0x6C655F),
            new Color(0x9A5E3D),
            new Color(0xC87B45),
            new Color(0xE8C19A),
            new Color(0xF5EFE3),
            new Color(0xE5C193),
            new Color(0xB4652C),
            new Color(0x75533C)
    };

    private final ScaledLayout scaledLayout = new ScaledLayout(DESIGN_WIDTH, DESIGN_HEIGHT);
    private final BufferedImage panelTexture;
    private boolean lampOn;

    /**
     * Constructs a new BackgroundPanel with the default panel texture.
     */
    public BackgroundPanel() {
        if (DEFAULT_PANEL == null) {
            throw new IllegalArgumentException("Default panel image could not be loaded");
        }

        panelTexture = ImageUtils.toBufferedImage(DEFAULT_PANEL);
        setLayout(scaledLayout);
    }

    @Override
    public void doLayout() {
        super.doLayout();
        // grow the controls' artwork along with the panel they are mounted on
        PanelIcons.setScale(scaledLayout.getScale());
    }

    /**
     * Sets the texture based on the given state.
     *
     * @param isLampOn If true, lights the stop lamp on the panel.
     *                 If false, leaves the panel unlit.
     */
    public void setTexture(boolean isLampOn) {
        if (lampOn != isLampOn) {
            lampOn = isLampOn;
            repaint(); // Repaint to reflect the change immediately
        }
    }

    /**
     * Paints the component with the current texture.
     *
     * @param graphics The Graphics object to paint with.
     */
    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        // Ensure we have a valid Graphics2D object
        if (!(graphics instanceof Graphics2D)) {
            System.err.println("Graphics object is not an instance of Graphics2D.");
            return;
        }

        Graphics2D graphics2D = (Graphics2D) graphics;

        graphics2D.setColor(SURROUND);
        graphics2D.fillRect(0, 0, getWidth(), getHeight());

        // Stretch the panel photograph over the area the controls are laid out in
        Rectangle content = scaledLayout.getContentBounds();
        if (content.width <= 0 || content.height <= 0) {
            return;
        }

        Rectangle2D textureArea = new Rectangle2D.Double(
                content.x, content.y, content.width, content.height);
        graphics2D.setPaint(new TexturePaint(panelTexture, textureArea));
        graphics2D.fill(textureArea);

        if (lampOn) {
            paintStopLamp(graphics2D, content);
        }
    }

    /**
     * Draws the lit stop lamp over the panel. Painting it here rather than compositing it
     * into the texture keeps it sharp at any UI scale, and avoids rebuilding a full copy of
     * the panel image every time the stop flag changes.
     *
     * @param graphics2D the context to draw on
     * @param content    the area the photograph is drawn into
     */
    private void paintStopLamp(Graphics2D graphics2D, Rectangle content) {
        double scaleX = content.width / PANEL_IMAGE_WIDTH;
        double scaleY = content.height / PANEL_IMAGE_HEIGHT;

        if (scaleX <= 0 || scaleY <= 0) {
            return;
        }

        Graphics2D lamp = (Graphics2D) graphics2D.create();
        try {
            lamp.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // work in the photograph's coordinates, the same ones the texture is stretched from
            lamp.translate(content.x, content.y);
            lamp.scale(scaleX, scaleY);

            // the original artwork lit the lamp by compositing this overlay onto the panel
            // photograph at its top left, so drawing it here puts it in the same place
            Image overlay = PanelIcons.useBitmaps() ? PanelIcons.sprite("mainonlamp.png") : null;
            if (overlay != null) {
                lamp.drawImage(overlay, 0, 0, null);
                return;
            }

            // the tube itself: a straight body with a rounded free end, sharing one paint so
            // the two shapes meet without a seam
            lamp.setPaint(new LinearGradientPaint(
                    new Point2D.Double(LAMP_X, 0),
                    new Point2D.Double(LAMP_X + LAMP_WIDTH, 0),
                    LAMP_STOPS, LAMP_COLOURS));
            lamp.fill(new Rectangle2D.Double(LAMP_X, LAMP_Y, LAMP_WIDTH, LAMP_BODY_HEIGHT));
            lamp.fill(new Ellipse2D.Double(
                    LAMP_X, LAMP_Y + LAMP_BODY_HEIGHT - LAMP_CAP_HEIGHT / 2,
                    LAMP_WIDTH, LAMP_CAP_HEIGHT));

            // fade the top into the holder rather than ending on a hard edge
            lamp.setPaint(new LinearGradientPaint(
                    new Point2D.Double(0, LAMP_Y),
                    new Point2D.Double(0, LAMP_Y + LAMP_FADE_HEIGHT),
                    new float[]{0f, 1f},
                    new Color[]{LAMP_HOLDER, new Color(0x001B1916, true)}));
            lamp.fill(new Rectangle2D.Double(LAMP_X, LAMP_Y, LAMP_WIDTH, LAMP_FADE_HEIGHT));
        } finally {
            lamp.dispose();
        }
    }
}
