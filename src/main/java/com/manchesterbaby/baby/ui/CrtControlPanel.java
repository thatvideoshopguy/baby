package com.manchesterbaby.baby.ui;

import javax.swing.JPanel;

import com.manchesterbaby.baby.ui.components.InterlockingPushButton;
import com.manchesterbaby.baby.ui.components.KeySwitch;
import com.manchesterbaby.baby.ui.components.ToggleSwitch;
import com.manchesterbaby.baby.utils.AppSettings;

import javax.swing.ButtonGroup;

import java.awt.Rectangle;

import static com.manchesterbaby.baby.utils.CallbackUtils.runCallback;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CrtControlPanel extends JPanel {
    private Runnable onStopRunChange;

    private Runnable onDisplayControlChange;
    private Runnable onDisplayAccumulatorChange;
    private Runnable onDisplayStoreChange;

    private Runnable onKspPressed;
    private Runnable onKlcPressed;
    private Runnable onKlcReleased;
    private Runnable onKscPressed;
    private Runnable onKscReleased;
    private Runnable onKacPressed;
    private Runnable onKacReleased;
    private Runnable onKccPressed;
    private Runnable onKccReleased;

    private final ToggleSwitch stopRunSwitch;
    private final ToggleSwitch writeErase;

    public InterlockingPushButton displayControlButton;
    public InterlockingPushButton displayAccumulatorButton;
    public InterlockingPushButton displayStoreButton;

    private final KeySwitch kspSwitch;

    // Design coordinates of the CRT control panel, as positions on the machine
    public static final int DESIGN_WIDTH = 700;
    public static final int DESIGN_HEIGHT = 113;

    private static final int DISPLAY_BUTTON_Y = 23;
    private static final int DISPLAY_BUTTON_WIDTH = 40;
    private static final int DISPLAY_BUTTON_HEIGHT = 35;

    private static final int KEY_SWITCH_Y = 59;
    private static final int KEY_SWITCH_WIDTH = 63;
    private static final int KEY_SWITCH_HEIGHT = 56;

    private static final int STOP_RUN_X = 120;
    private static final int STOP_RUN_Y = 67;
    private static final int STOP_RUN_WIDTH = 50;
    private static final int STOP_RUN_HEIGHT = 30;

    private static final int WRITE_ERASE_X = 623;
    private static final int WRITE_ERASE_Y = 20;
    private static final int WRITE_ERASE_WIDTH = 34;
    private static final int WRITE_ERASE_HEIGHT = 37;

    private void placeDisplayButton(InterlockingPushButton button, int x) {
        add(button, new Rectangle(x, DISPLAY_BUTTON_Y,
                DISPLAY_BUTTON_WIDTH, DISPLAY_BUTTON_HEIGHT));
    }

    // x is the switch's place along the black panel, left to right
    private void placeKeySwitch(KeySwitch keySwitch, int x) {
        add(keySwitch, new Rectangle(x, KEY_SWITCH_Y,
                KEY_SWITCH_WIDTH, KEY_SWITCH_HEIGHT));
    }

    public CrtControlPanel() {
        setOpaque(false);
        setLayout(new ScaledLayout(DESIGN_WIDTH, DESIGN_HEIGHT));

        // NOTE: The CS switch is called the stopRunSwitch switch
        stopRunSwitch = new ToggleSwitch("If switched down then continually executes instructions. (Press F5/shift+F5)");
        stopRunSwitch.addActionListener(e -> runCallback(onStopRunChange));
        add(stopRunSwitch, new Rectangle(STOP_RUN_X, STOP_RUN_Y, STOP_RUN_WIDTH, STOP_RUN_HEIGHT));

        // displayControlButtons
        displayControlButton = new InterlockingPushButton("Displays the control on the monitor. (Press Ctrl+Alt+C)");
        displayControlButton.addActionListener(e -> runCallback(onDisplayControlChange));
        placeDisplayButton(displayControlButton, 85);

        displayAccumulatorButton = new InterlockingPushButton("Displays the accumulator on the monitor. (Press Ctrl+Alt+A)");
        displayAccumulatorButton.addActionListener(e -> runCallback(onDisplayAccumulatorChange));
        placeDisplayButton(displayAccumulatorButton, 110);

        displayStoreButton = new InterlockingPushButton("Displays the store on the monitor. (Press Ctrl+Alt+S)");
        displayStoreButton.addActionListener(e -> runCallback(onDisplayStoreChange));
        placeDisplayButton(displayStoreButton, 135);

        // Group display buttons to ensure mutual exclusivity
        ButtonGroup displayButtonGroup = new ButtonGroup();
        displayButtonGroup.add(displayControlButton);
        displayButtonGroup.add(displayAccumulatorButton);
        displayButtonGroup.add(displayStoreButton);

        kspSwitch = new KeySwitch("Executes a single instruction. (Press F10)", KeySwitch.KeyColour.GREY);    // KC == KSP
        kspSwitch.addActionListener(e -> runCallback(onKspPressed));
        placeKeySwitch(kspSwitch, 228);

        KeySwitch klcSwitch = new KeySwitch("Clears the current action line of the store.", KeySwitch.KeyColour.WHITE);
        klcSwitch.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                runCallback(onKlcPressed);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                runCallback(onKlcReleased);
            }
        });
        placeKeySwitch(klcSwitch, 301);

        KeySwitch kscSwitch = new KeySwitch("Clears the store.", KeySwitch.KeyColour.WHITE);
        kscSwitch.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                runCallback(onKscPressed);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                runCallback(onKscReleased);
            }
        });
        placeKeySwitch(kscSwitch, 338);

        KeySwitch kacSwitch = new KeySwitch("Clears the accumulator.", KeySwitch.KeyColour.WHITE);
        kacSwitch.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                runCallback(onKacPressed);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                runCallback(onKacReleased);
            }
        });
        placeKeySwitch(kacSwitch, 373);

        KeySwitch kccSwitch = new KeySwitch("Clears the CI, PI and Accumulator.", KeySwitch.KeyColour.GREY);
        kccSwitch.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                runCallback(onKccPressed);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                runCallback(onKccReleased);
            }
        });
        placeKeySwitch(kccSwitch, 449);

        // These buttons are not connected as per the original hardware
        KeySwitch kbcSwitch = new KeySwitch("Not connected", KeySwitch.KeyColour.GREY);
        KeySwitch kecSwitch = new KeySwitch("Not connected", KeySwitch.KeyColour.GREY);
        KeySwitch kmcSwitch = new KeySwitch("Not connected", KeySwitch.KeyColour.GREY);
        placeKeySwitch(kbcSwitch, 412);
        placeKeySwitch(kecSwitch, 482);
        placeKeySwitch(kmcSwitch, 520);

        boolean showDisconnected = AppSettings.getInstance().isShowDisconnectedButtons();
        kbcSwitch.setVisible(showDisconnected);
        kecSwitch.setVisible(showDisconnected);
        kmcSwitch.setVisible(showDisconnected);

        writeErase = new ToggleSwitch("Selects whether typewriter erases or writes bits.");
        add(writeErase, new Rectangle(WRITE_ERASE_X, WRITE_ERASE_Y, WRITE_ERASE_WIDTH, WRITE_ERASE_HEIGHT));

        // set up default settings
        displayStoreButton.setSelected(true);    // default to display store on monitor
        writeErase.setSelected(false);    // default to write

    }


    // true is pulse, false is pre
    public boolean getStopRun() {
        return stopRunSwitch.isSelected();
    }

    // true is pulse, false is pre
    public void setStopRun(boolean value) {
        stopRunSwitch.setSelected(value);
    }


    public boolean getWriteErase() {
        return !writeErase.isSelected();
    }

    public void setWriteErase(boolean value) {
        writeErase.setSelected(value);
    }


    public void setOnStopRunChange(Runnable callback) {
        this.onStopRunChange = callback;
    }

    public void setOnDisplayControlChange(Runnable callback) {
        this.onDisplayControlChange = callback;
    }

    public void setOnDisplayAccumulatorChange(Runnable callback) {
        this.onDisplayAccumulatorChange = callback;
    }

    public void setOnDisplayStoreChange(Runnable callback) {
        this.onDisplayStoreChange = callback;
    }

    public void setOnKspPressed(Runnable callback) {
        this.onKspPressed = callback;
    }

    public void setOnKlcPressed(Runnable callback) {
        this.onKlcPressed = callback;
    }

    public void setOnKlcReleased(Runnable callback) {
        this.onKlcReleased = callback;
    }

    public void setOnKscPressed(Runnable callback) {
        this.onKscPressed = callback;
    }

    public void setOnKscReleased(Runnable callback) {
        this.onKscReleased = callback;
    }

    public void setOnKacPressed(Runnable callback) {
        this.onKacPressed = callback;
    }

    public void setOnKacReleased(Runnable callback) {
        this.onKacReleased = callback;
    }

    public void setOnKccPressed(Runnable callback) {
        this.onKccPressed = callback;
    }

    public void setOnKccReleased(Runnable callback) {
        this.onKccReleased = callback;
    }

    public KeySwitch getKspSwitch() {
        return kspSwitch;
    }

    public ToggleSwitch getStopRunSwitch() {
        return stopRunSwitch;
    }

    public InterlockingPushButton getDisplayControlButton() {
        return displayControlButton;
    }

    public InterlockingPushButton getDisplayAccumulatorButton() {
        return displayAccumulatorButton;
    }

    public InterlockingPushButton getDisplayStoreButton() {
        return displayStoreButton;
    }

}