package com.manchesterbaby.baby.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lays out children at fixed positions in a design coordinate space, scaled uniformly to
 * fit the container and centred within it.
 *
 * <p>The front panel is a set of controls sitting on a photograph of the machine, so every
 * control has one true position: the place on that photograph where the real control is.
 * This layout keeps that relationship at any window size. The scale is uniform and the
 * result is centred, which means the panel is letterboxed rather than stretched when the
 * window's aspect ratio does not match the design - controls can never drift off the
 * features they belong to.</p>
 *
 * <p>Children are added with a {@link Rectangle} constraint giving their bounds in design
 * coordinates. A child that uses a ScaledLayout of its own is given this one's transform
 * so that nesting stays exact; see {@link #inheritTransform}.</p>
 */
public class ScaledLayout implements LayoutManager2 {

    /** Smallest usable fraction of the design size, used for the minimum layout size. */
    private static final double MINIMUM_SCALE = 0.5;

    private final int designWidth;
    private final int designHeight;
    private final Map<Component, Rectangle> designBounds = new LinkedHashMap<>();

    // Null for the outermost layout, which works out the transform for everything below it.
    private ScaledLayout root;
    // Where this container sits, in the root's design coordinates and in its actual ones.
    private int designOffsetX;
    private int designOffsetY;
    private int shiftX;
    private int shiftY;

    private double scale = 1;
    private double originX;
    private double originY;
    private Rectangle contentBounds = new Rectangle();

    /**
     * @param designWidth  width of the design coordinate space
     * @param designHeight height of the design coordinate space
     */
    public ScaledLayout(int designWidth, int designHeight) {
        if (designWidth <= 0 || designHeight <= 0) {
            throw new IllegalArgumentException(
                    "Design size must be positive: " + designWidth + "x" + designHeight);
        }
        this.designWidth = designWidth;
        this.designHeight = designHeight;
    }

    /** The scale currently being applied to design coordinates. */
    public double getScale() {
        return scale;
    }

    /**
     * The area the design space currently occupies, in container coordinates. This is what
     * the background photograph should be drawn into so that it stays registered with the
     * controls.
     */
    public Rectangle getContentBounds() {
        return new Rectangle(contentBounds);
    }

    @Override
    public void addLayoutComponent(Component component, Object constraints) {
        if (!(constraints instanceof Rectangle)) {
            throw new IllegalArgumentException(
                    "ScaledLayout needs a Rectangle of design coordinates, got: " + constraints);
        }
        designBounds.put(component, new Rectangle((Rectangle) constraints));
    }

    @Override
    public void addLayoutComponent(String name, Component component) {
        throw new UnsupportedOperationException("ScaledLayout needs a Rectangle constraint");
    }

    @Override
    public void removeLayoutComponent(Component component) {
        designBounds.remove(component);
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        Insets insets = parent.getInsets();
        return new Dimension(
                designWidth + insets.left + insets.right,
                designHeight + insets.top + insets.bottom);
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        Insets insets = parent.getInsets();
        return new Dimension(
                (int) (designWidth * MINIMUM_SCALE) + insets.left + insets.right,
                (int) (designHeight * MINIMUM_SCALE) + insets.top + insets.bottom);
    }

    @Override
    public Dimension maximumLayoutSize(Container target) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public float getLayoutAlignmentX(Container target) {
        return 0.5f;
    }

    @Override
    public float getLayoutAlignmentY(Container target) {
        return 0.5f;
    }

    @Override
    public void invalidateLayout(Container target) {
        // nothing cached that a child could invalidate
    }

    /**
     * Hands this layout the transform of the panel it sits on, so that a nested panel of
     * controls positions its children from exactly the same arithmetic the outermost
     * layout would have used. Deriving a second transform from already-rounded bounds
     * instead would let a control land a pixel away from where it belongs, which shows up
     * as uneven spacing across a grid of identical controls.
     *
     * @param root          the outermost layout, which owns the scale and origin
     * @param designOffsetX this container's left edge in the root's design coordinates
     * @param designOffsetY this container's top edge in the root's design coordinates
     * @param shiftX        this container's laid-out left edge, in the root's coordinates
     * @param shiftY        this container's laid-out top edge, in the root's coordinates
     */
    private void inheritTransform(ScaledLayout root, int designOffsetX, int designOffsetY,
                                  int shiftX, int shiftY) {
        this.root = root;
        this.designOffsetX = designOffsetX;
        this.designOffsetY = designOffsetY;
        this.shiftX = shiftX;
        this.shiftY = shiftY;
    }

    // Design coordinate to this container's own coordinates, via the root's space
    private int mapX(int designX) {
        return (int) Math.round(originX + designX * scale) - shiftX;
    }

    private int mapY(int designY) {
        return (int) Math.round(originY + designY * scale) - shiftY;
    }

    @Override
    public void layoutContainer(Container parent) {
        if (root != null) {
            scale = root.scale;
            originX = root.originX;
            originY = root.originY;
        } else {
            Insets insets = parent.getInsets();
            int width = parent.getWidth() - insets.left - insets.right;
            int height = parent.getHeight() - insets.top - insets.bottom;

            scale = Math.min(width / (double) designWidth, height / (double) designHeight);
            if (scale < 0) {
                scale = 0;
            }
            originX = insets.left + (width - designWidth * scale) / 2;
            originY = insets.top + (height - designHeight * scale) / 2;
        }

        // map both edges rather than the size, so neighbouring controls stay flush
        int contentLeft = mapX(designOffsetX);
        int contentTop = mapY(designOffsetY);
        contentBounds = new Rectangle(contentLeft, contentTop,
                mapX(designOffsetX + designWidth) - contentLeft,
                mapY(designOffsetY + designHeight) - contentTop);

        ScaledLayout transform = root != null ? root : this;

        for (Map.Entry<Component, Rectangle> entry : designBounds.entrySet()) {
            Rectangle bounds = entry.getValue();
            int left = mapX(designOffsetX + bounds.x);
            int top = mapY(designOffsetY + bounds.y);
            int right = mapX(designOffsetX + bounds.x + bounds.width);
            int bottom = mapY(designOffsetY + bounds.y + bounds.height);

            Component child = entry.getKey();
            child.setBounds(left, top, right - left, bottom - top);

            if (child instanceof Container && ((Container) child).getLayout() instanceof ScaledLayout) {
                ((ScaledLayout) ((Container) child).getLayout()).inheritTransform(transform,
                        designOffsetX + bounds.x, designOffsetY + bounds.y,
                        left + shiftX, top + shiftY);
            }
        }
    }
}
