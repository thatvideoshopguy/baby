package com.manchesterbaby.baby.ui;

import javax.swing.*;

import com.manchesterbaby.baby.ui.components.PushButton;
import com.manchesterbaby.baby.utils.AppSettings;

import static com.manchesterbaby.baby.utils.CallbackUtils.runCallback;

import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TypewriterPanel extends JPanel {

    // 32 keys plus 8 unconnected
    public int CONNECTED_KEYS = 32;
    public int UNCONNECTED_KEYS = 8;
    public int TOTAL_KEYS = CONNECTED_KEYS + UNCONNECTED_KEYS;

    private final Runnable[] onKeyPressedCallbacks;
    private final Runnable[] onKeyReleasedCallbacks;

    private final PushButton[] numberKeys;

    // Design coordinates of the key grid on the typewriter panel
    public static final int DESIGN_WIDTH = 700;
    public static final int DESIGN_HEIGHT = 267;
    private static final int KEY_ORIGIN_X = 118;
    private static final int KEY_ORIGIN_Y = 6;
    private static final int KEY_WIDTH = 58;
    private static final int KEY_HEIGHT = 51;
    private static final int KEY_ROWS = 5;

    // 8 (across) x 5 (down) grid
    public TypewriterPanel() {

        setLayout(new ScaledLayout(DESIGN_WIDTH, DESIGN_HEIGHT));
        setOpaque(false);

        numberKeys = new PushButton[TOTAL_KEYS];
        onKeyPressedCallbacks = new Runnable[TOTAL_KEYS];
        onKeyReleasedCallbacks = new Runnable[TOTAL_KEYS];

        // Initialize and layout keys
        initialiseNumberKeys();
    }

    private void initialiseNumberKeys() {
        for (int i = 0; i < TOTAL_KEYS; i++) {

            // add mouse listeners to keys 0-31
            if (i < CONNECTED_KEYS) {
                numberKeys[i] = new PushButton("Typewriter adjusts bit " + i + " of the action line.");

                int keyNumber = i; // Necessary for lambda
                numberKeys[i].addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        runCallback(onKeyPressedCallbacks[keyNumber]);
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        runCallback(onKeyReleasedCallbacks[keyNumber]);
                    }
                });

            } else {
                numberKeys[i] = new PushButton("Left unconnected.");
                numberKeys[i].setVisible(AppSettings.getInstance().isShowDisconnectedButtons());
            }
        }

        // keys are numbered down each column in turn, then left to right
        for (int i = 0; i < TOTAL_KEYS; i++) {
            add(numberKeys[i], new Rectangle(
                    KEY_ORIGIN_X + (i / KEY_ROWS) * KEY_WIDTH,
                    KEY_ORIGIN_Y + (i % KEY_ROWS) * KEY_HEIGHT,
                    KEY_WIDTH, KEY_HEIGHT));
        }
    }

    public void setOnKeyPressed(int i, Runnable callback) {
        if (i >= 0 && i < onKeyPressedCallbacks.length) {
            onKeyPressedCallbacks[i] = callback;
        }
    }

    public void setOnKeyReleased(int i, Runnable callback) {
        if (i >= 0 && i < onKeyReleasedCallbacks.length) {
            onKeyReleasedCallbacks[i] = callback;
        }
    }

    // The original code used doClick() which simulates a button click but doesn't trigger
    // MouseListener events. It only triggers ActionListener events, which weren't set up.
    // but this doesn't visually press the button so have hacky flick of the icon back and
    // forth to simulate a press. Doing a doClick on the button had horrible redraw issues
    // anyway.
    public void pressKey(int i) {
        runCallback(onKeyPressedCallbacks[i]);
        try {
            numberKeys[i].setIconPressed(true);
            Thread.sleep(50);
            numberKeys[i].setIconPressed(false);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        runCallback(onKeyReleasedCallbacks[i]);
    }

}