package com.manchesterbaby.baby.ui;

import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.ToolTipManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;

import com.manchesterbaby.baby.core.Control;
import com.manchesterbaby.baby.core.Store;
import com.manchesterbaby.baby.manager.AnimationManager;
import com.manchesterbaby.baby.ui.components.PanelIcons;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.Color;

public class CrtPanel extends JPanel {

    public enum DisplayType {
        STORE,
        CONTROL,
        ACCUMULATOR
    }

    // NOTE: This was 291x291 for raster area before circular border
    public static final int DEFAULT_CRT_WIDTH = 400;
    public static final int DEFAULT_CRT_HEIGHT = 290;

    public static final int DEFAULT_RASTER_OFFSET_X = 200; // Number of pixels vertically from left
    public static final int DEFAULT_RASTER_OFFSET_Y = 95; // Number of pixels horizontally from top

    public static final int DEFAULT_BIT_LENGTH = 6; // The length of a bit as appears on the crt
    public static final int DEFAULT_SPACING = 3;
    // A bit is drawn as a mark on the CRT: a short dot for 0, a long dash for 1, the
    // Williams tube convention. Geometry and palette are taken from the 7x3 bit GIFs
    // these replace; drawing them as fills instead keeps the raster sharp when scaled.
    private static final int BIT_TILE_WIDTH = 7;
    private static final int BIT_TILE_HEIGHT = 3;
    private static final int DOT_WIDTH = 1;
    private static final int DASH_WIDTH = 4;

    private static final Color PHOSPHOR_UNLIT = new Color(0x1E1606);
    private static final Color PHOSPHOR_GLOW = new Color(0x006600);
    private static final Color PHOSPHOR_CORE = new Color(0x82FF82);
    private static final Color PHOSPHOR_GLOW_BRIGHT = new Color(0x00CC00);
    private static final Color PHOSPHOR_CORE_BRIGHT = new Color(0xFFFFFF);

    private DisplayType currentDisplay = DisplayType.STORE; // The current display type
    private final boolean renderAccurately = true; // Whether to render the Control and Accumulator accurately
    private BufferedImage bufferedImage; // The image to be drawn
    private Graphics2D big;
    private double bufferScale;
    private boolean bufferBitmaps;
    private int actionLine = 0;

    private final Store store;
    private final Control control;

    private final boolean CLICK_ON_CRT_TO_MODIFY = false;

    /**
     * Creates a new instance of CrtPanel.
     *
     * @param store   the store to be displayed
     * @param control the control to be displayed
     */
    public CrtPanel(Store store, Control control) {
        this.store = store;
        this.control = control;

        // Enable tooltips
        ToolTipManager.sharedInstance().registerComponent(this);

        // Add mouse click listener
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                if (CLICK_ON_CRT_TO_MODIFY)
                {
                    int x = toLogicalX(e.getX());
                    int y = toLogicalY(e.getY());
                    int button = e.getButton();

                    // Check if within horizontal bounds of the drawn line
                    // Lines start at DEFAULT_RASTER_OFFSET_X + DEFAULT_SPACING
                    // and extend for 32 bits * DEFAULT_BIT_LENGTH
                    boolean withinHorizontalBounds = 
                        x >= (DEFAULT_RASTER_OFFSET_X + DEFAULT_SPACING) && 
                        x <= (DEFAULT_RASTER_OFFSET_X + DEFAULT_SPACING + (32 * DEFAULT_BIT_LENGTH));
                    
                    // Reverse calculate line number from y coordinate
                    int lineNumber = (y - DEFAULT_RASTER_OFFSET_Y - DEFAULT_SPACING) / DEFAULT_BIT_LENGTH;
                    int bitNumber = (x - DEFAULT_RASTER_OFFSET_X - DEFAULT_SPACING) / DEFAULT_BIT_LENGTH;
                    
                    // Only show dialog if click is within valid line range
                    if (lineNumber >= 0 && lineNumber < 32 && withinHorizontalBounds) {
                        
                        //                    JOptionPane.showMessageDialog(CrtPanel.this,
                        //                        "Line: " + lineNumber + ", bit:" + bitNumber + " (button:" + button + ")",
                        //                        "Line Information",
                        //                        JOptionPane.INFORMATION_MESSAGE);

                        if(currentDisplay == DisplayType.STORE)
                        {
                            if(button == 1)
                            {
                                // left click
                                store.setBit(lineNumber, bitNumber);
                            }
                            else
                            {
                                // middle or right click
                                store.clearBit(lineNumber, bitNumber);           
                            }
                            render();
                            efficientRepaint();
                        }
                    }   
                }
            }
        });

        // Add mouse motion listener for tooltips
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int x = toLogicalX(e.getX());
                int y = toLogicalY(e.getY());
                
                // Check if within horizontal bounds of the drawn line
                // Lines start at DEFAULT_RASTER_OFFSET_X + DEFAULT_SPACING
                // and extend for 32 bits * DEFAULT_BIT_LENGTH
                boolean withinHorizontalBounds = 
                    x >= (DEFAULT_RASTER_OFFSET_X + DEFAULT_SPACING) && 
                    x <= (DEFAULT_RASTER_OFFSET_X + DEFAULT_SPACING + (32 * DEFAULT_BIT_LENGTH));
                
                // Calculate line number
                int lineNumber = (y - DEFAULT_RASTER_OFFSET_Y - DEFAULT_SPACING) / DEFAULT_BIT_LENGTH;
                
                if (lineNumber >= 0 && lineNumber < 32 && withinHorizontalBounds) {
                    switch (currentDisplay) {
                        case STORE:
                            String tooltip = "Store line " + lineNumber + ": " + store.disassembleModern(store.getLine(lineNumber), false, false);
                            if(lineNumber == control.getControlInstruction())
                            {
                                tooltip += " [CI line; line *before* next instruction]";
                            }
                            setToolTipText(tooltip);
                            break;
                        case ACCUMULATOR :
                            int acc = control.getAccumulator();
                            setToolTipText("Accumulator: " +  acc + ", disassembled: " + store.disassembleModern(acc, false, false) );
                            break;
                        case CONTROL :
                            setToolTipText("Control Instruction: line " + control.getControlInstruction());
                            // can't meaningfully display PI as doesn't exist unless running and then always changing
                            break;
                    }
                } else {
                    setToolTipText(null);
                }
            }
        });

        setBufferScale(1.0);
        repaint();
    }

    /**
     * (Re)creates the backing buffer so that one buffer pixel maps to one device pixel at
     * the given scale, then redraws everything into it. Without this the raster would be a
     * fixed 400x290 bitmap stretched by the UI scale, which is exactly the blur the vector
     * bit drawing above is meant to avoid.
     *
     * @param scale the device scale to render at
     */
    private synchronized void setBufferScale(double scale) {
        bufferScale = scale;
        bufferBitmaps = PanelIcons.useBitmaps();
        bufferedImage = new BufferedImage(
                (int) Math.ceil(DEFAULT_CRT_WIDTH * scale),
                (int) Math.ceil(DEFAULT_CRT_HEIGHT * scale),
                BufferedImage.TYPE_INT_ARGB);
        big = bufferedImage.createGraphics();
        big.scale(scale, scale);

        for (int lineNumber = 0; lineNumber < 32; lineNumber++)
            store.isLineAltered[lineNumber] = true;

        render();
    }

    /**
     * The scale the CRT is currently drawn at, relative to its own coordinate space.
     */
    private double displayScale() {
        return getWidth() > 0 ? getWidth() / (double) DEFAULT_CRT_WIDTH : 1;
    }

    // Converts a mouse position on the panel back into the CRT's own coordinates
    private int toLogicalX(int x) {
        return (int) (x / displayScale());
    }

    // Package private so the round trip can be checked without dispatching mouse events.
    int toLogicalY(int y) {
        return (int) (y / displayScale());
    }

    /**
     * Change the display type of the CRT.
     *
     * @param display the new display type
     */
    public void setCrtDisplay(DisplayType display) {
        DisplayType beforeDisplay = currentDisplay;

        for (int x = 0; x < 32; x++)
            store.isLineAltered[x] = true;

        switch (display) {
            case STORE:
            case ACCUMULATOR:
            case CONTROL:
                currentDisplay = display;
                break;
            default:
                currentDisplay = DisplayType.STORE;
                break;
        }

        // if changed display then redraw
        if (currentDisplay != beforeDisplay) {

            // set so all lines need redrawing
            for (int lineNumber = 0; lineNumber < 32; lineNumber++)
                store.isLineAltered[lineNumber] = true;

            render();
            repaint();
        }
    }

    /**
     * <p>Paints the CRT panel by drawing the pre-rendered buffered image.</p>
     * <p>Calls {@code super.paintComponent} to clear the panel, then draws the image at (0,0).</p>
     *
     * @param graphicsIn the graphics context used for painting
     */
    @Override
    public void paintComponent(Graphics graphicsIn) {
        super.paintComponent(graphicsIn);

        Graphics2D graphics2D = (Graphics2D) graphicsIn;

        // render the buffer at the resolution it will actually be shown at, taking in both
        // the size the panel has been laid out to and the display's own scaling
        double scale = graphics2D.getTransform().getScaleX() * displayScale();
        if (scale > 0 && (Math.abs(scale - bufferScale) > 0.001
                || bufferBitmaps != PanelIcons.useBitmaps())) {
            setBufferScale(scale);
        }

        graphics2D.drawImage(bufferedImage, 0, 0, getWidth(), getHeight(), this);
    }

    /**
     * Repaints the amount of screen necessary.
     */
    public synchronized void efficientRepaint() {
        switch (currentDisplay) {
            // only redraw the store lines that have changed
            case STORE:
                double scale = displayScale();
                for (int lineNumber = 0; lineNumber < 32; lineNumber++) {
                    if (store.isLineAltered[lineNumber]) {
                        int y = DEFAULT_RASTER_OFFSET_Y + DEFAULT_SPACING + lineNumber * DEFAULT_BIT_LENGTH;
                        repaint((int) (DEFAULT_RASTER_OFFSET_X * scale), (int) (y * scale),
                                (int) Math.ceil(32 * DEFAULT_BIT_LENGTH * scale) + 2,
                                (int) Math.ceil(BIT_TILE_HEIGHT * scale) + 2);
                        store.isLineAltered[lineNumber] = false;
                    }
                }
                break;
            // redraw top line only
            case ACCUMULATOR:
                if (renderAccurately) repaint();
                else repaint(0, 0, DEFAULT_CRT_WIDTH, 3 + DEFAULT_BIT_LENGTH);
                break;
            // redraw top 2 lines only
            case CONTROL:
                if (renderAccurately) repaint();
                else repaint(0, 0, DEFAULT_CRT_WIDTH, 3 + DEFAULT_BIT_LENGTH + DEFAULT_BIT_LENGTH);
                break;
            // redraw entire display
            default:
                repaint();
                break;
        }
    }

    public void setActionLine(int newActionLine) {
        // negative values turn off the action line
        if (newActionLine < 0) {
            // wipe out old action line
            if (actionLine > 0 && actionLine < 32)
                store.isLineAltered[actionLine] = true;
            actionLine = newActionLine;
        } else {
            if (actionLine != newActionLine) {
                // old actionLine could be negative if turned off
                if (actionLine > 0 && actionLine < 32)
                    store.isLineAltered[actionLine] = true;
                // store new action line
                actionLine = newActionLine;
                // set to draw new action line
                store.isLineAltered[actionLine] = true;
                render();
                repaint();
            }
        }
    }

    // render the graphics to the buffered image
    private synchronized void render() {
        switch (currentDisplay) {
            case STORE:
                renderStore();
                break;
            case ACCUMULATOR:
                renderAccumulator();
                break;
            case CONTROL:
                renderControl();
                break;
            default:
                renderStore();
                break;
        }
    }

    // renders the Store to the buffered image
    public void renderStore() {

        for (int lineNumber = 0; lineNumber < 32; lineNumber++) {
            // for each line that has been changed since last redraw
            if (store.isLineAltered[lineNumber]) {
                // if redrawing the action line then draw it brighter
                if (actionLine == lineNumber) {
                    drawDataAtLine(store.getLine(lineNumber), lineNumber, big, true);
                }
                // otherwise draw a normal line
                else {
                    drawDataAtLine(store.getLine(lineNumber), lineNumber, big);
                }
            }
        }
    }

    // render control
    public void renderControl() {
        // if rendering accurately repeat display all the way down
        if (renderAccurately) {
            // if the baby is running then PI exists so draw it
            if (AnimationManager.animationRunning) {
                // if baby is running then CI and PI are displayed
                for (int line = 0; line < 32; line += 2) {
                    drawDataAtLine(control.getControlInstruction(), line, big);
                    drawDataAtLine(control.getPresentInstruction(), line + 1, big);
                }
            } else {
                // when not running PI doesn't exist so only display CI
                for (int line = 0; line < 32; line++) {
                    drawDataAtLine(control.getControlInstruction(), line, big);
                }
            }
        } else {
            // otherwise display both CI and PI but only in top two lines
            drawDataAtLine(control.getControlInstruction(), 0, big);
            drawDataAtLine(control.getPresentInstruction(), 1, big);
        }
    }

    // render accumulator
    public void renderAccumulator() {
        drawDataAtLine(control.getAccumulator(), 0, big);

        // if rendering accuraterly draw accumulator on all lines
        if (renderAccurately) {
            for (int line = 1; line < 32; line++)
                drawDataAtLine(control.getAccumulator(), line, big);
        }
    }

    // draw the given data for a line at the specified position on the specified graphics context
    private void drawDataAtLine(int data, int line, Graphics2D graphicsContext) {
        drawDataAtLine(data, line, graphicsContext, false);
    }

    private void drawDataAtLine(int data, int line, Graphics2D graphicsContext, boolean bright) {
        int y = DEFAULT_RASTER_OFFSET_Y + DEFAULT_SPACING + line * DEFAULT_BIT_LENGTH;
        for (int bitNumber = 31; bitNumber >= 0; bitNumber--) {
            int x = DEFAULT_RASTER_OFFSET_X + DEFAULT_SPACING + bitNumber * DEFAULT_BIT_LENGTH;
            drawBit(graphicsContext, x, y, ((data >> bitNumber) & 1) == 1, bright);
        }
    }

    /**
     * Draws one bit of the raster: an unlit tile carrying a glowing mark, short for 0 and
     * long for 1. Package private so it can be checked without standing up a whole panel.
     *
     * @param graphicsContext the context to draw on
     * @param x               left edge of the bit's tile
     * @param y               top edge of the bit's tile
     * @param one             true to draw a dash (1), false to draw a dot (0)
     * @param bright          true for the action line, which is drawn more intensely
     */
    static void drawBit(Graphics2D graphicsContext, int x, int y, boolean one, boolean bright) {
        // the original 7x3 GIFs carry their own unlit background, so they replace the
        // whole tile rather than being drawn over it
        Image sprite = PanelIcons.useBitmaps()
                ? PanelIcons.sprite("bit" + (one ? "1" : "0") + (bright ? "bright" : "") + ".gif")
                : null;
        if (sprite != null) {
            graphicsContext.drawImage(sprite, x, y, BIT_TILE_WIDTH, BIT_TILE_HEIGHT, null);
            return;
        }

        graphicsContext.setColor(PHOSPHOR_UNLIT);
        graphicsContext.fillRect(x, y, BIT_TILE_WIDTH, BIT_TILE_HEIGHT);

        int coreWidth = one ? DASH_WIDTH : DOT_WIDTH;

        // the mark's glow bleeds one pixel around its core
        graphicsContext.setColor(bright ? PHOSPHOR_GLOW_BRIGHT : PHOSPHOR_GLOW);
        graphicsContext.fillRect(x, y, coreWidth + 2, BIT_TILE_HEIGHT);

        graphicsContext.setColor(bright ? PHOSPHOR_CORE_BRIGHT : PHOSPHOR_CORE);
        graphicsContext.fillRect(x + 1, y + 1, coreWidth, 1);
    }

    public DisplayType getCurrentDisplay() {
        return currentDisplay;
    }

}