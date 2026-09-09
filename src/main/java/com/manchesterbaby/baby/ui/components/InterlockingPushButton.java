package com.manchesterbaby.baby.ui.components;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

import java.awt.Insets;

public class InterlockingPushButton extends JRadioButton {

    private static final Insets DEFAULT_MARGIN = new Insets(3, 3, 3, 3);

    // Cached default icons
    private static final Icon DEFAULT_IN_ICON = PanelIcons.interlockIn();
    private static final Icon DEFAULT_OUT_ICON = PanelIcons.interlockOut();

    /**
     * A custom JRadioButton that simulates an interlocking push button with custom icons.
     *
     * @param toolTipText The text to display when the button is hovered over.
     */
    public InterlockingPushButton(String toolTipText) {
        setIcon(DEFAULT_OUT_ICON);
        setSelectedIcon(DEFAULT_IN_ICON);

        // GTK enables rollover on buttons, and with no rollover icon set Synth falls back to
        // the selected icon: hovering an unselected control paints it as if it were on. The
        // panel has no hover state to show anyway, so turn rollover off.
        setRolloverEnabled(false);

        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);

        setMargin(DEFAULT_MARGIN);

        setHorizontalTextPosition(AbstractButton.CENTER);
        setVerticalAlignment(SwingConstants.TOP);

        setToolTipText(toolTipText);
    }
}
