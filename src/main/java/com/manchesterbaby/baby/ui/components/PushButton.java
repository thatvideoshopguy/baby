package com.manchesterbaby.baby.ui.components;

import javax.swing.JButton;

import javax.swing.Icon;
import javax.swing.AbstractButton;

public class PushButton extends JButton {

    // Cached default icons
    private static final Icon DEFAULT_IN_ICON = PanelIcons.pushIn();
    private static final Icon DEFAULT_OUT_ICON = PanelIcons.pushOut();

    /**
     * A custom JButton that simulates a push switch with custom icons.
     *
     * @param toolTipText the text to display when the button is hovered over
     */
    public PushButton(String toolTipText) {
        setIcon(DEFAULT_OUT_ICON);
        setPressedIcon(DEFAULT_IN_ICON);

        // GTK enables rollover on buttons, and with no rollover icon set Synth falls back to
        // another state's icon. The panel has no hover state to show, so turn rollover off.
        setRolloverEnabled(false);

        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);

        setHorizontalTextPosition(AbstractButton.CENTER);
        setVerticalTextPosition(AbstractButton.CENTER);

        setText("");
        setToolTipText(toolTipText);
    }

    // hacky way to change icon without doClick()
    public void setIconPressed(boolean pressed)
    {
        if(pressed)
            setIcon(DEFAULT_IN_ICON);
        else
            setIcon(DEFAULT_OUT_ICON);
    }
}
