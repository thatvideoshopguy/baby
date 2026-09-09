package com.manchesterbaby.baby.ui.components;

import javax.swing.JButton;
import javax.swing.Icon;
import javax.swing.AbstractButton;
import javax.swing.SwingConstants;

import java.awt.Color;
import java.awt.Insets;

public class KeySwitch extends JButton {

    private static final Insets DEFAULT_MARGIN = new Insets(3, 3, 3, 3);

    /**
     * Enum for the colours of the KeySwitch.
     */
    public enum KeyColour {
        GREY(PanelIcons.KEY_GREY),
        WHITE(PanelIcons.KEY_IVORY);

        private final Icon upIcon;
        private final Icon downIcon;

        KeyColour(Color top) {
            this.upIcon = PanelIcons.keyUp(top);
            this.downIcon = PanelIcons.keyDown(top);
        }

        public Icon getUpIcon() {
            return upIcon;
        }

        public Icon getDownIcon() {
            return downIcon;
        }
    }

    /**
     * Constructs a KeySwitch with the specified tooltip text and color.
     *
     * @param toolTipText the text to display when the button is hovered over
     * @param keyColour   the color of the KeySwitch (GREY or WHITE)
     */
    public KeySwitch(String toolTipText, KeyColour keyColour) {
        if (keyColour == null) {
            throw new IllegalArgumentException("KeyColour cannot be null");
        }

        setIcon(keyColour.getUpIcon());
        setPressedIcon(keyColour.getDownIcon());

        // GTK enables rollover on buttons, and with no rollover icon set Synth falls back to
        // another state's icon. The panel has no hover state to show, so turn rollover off.
        setRolloverEnabled(false);

        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);

        setMargin(DEFAULT_MARGIN);

        setHorizontalTextPosition(AbstractButton.CENTER);
        setVerticalTextPosition(SwingConstants.TOP);

        setText("");
        setToolTipText(toolTipText);
    }
}
