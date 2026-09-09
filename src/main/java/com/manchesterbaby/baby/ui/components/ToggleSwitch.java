package com.manchesterbaby.baby.ui.components;

import javax.swing.*;

/**
 * A custom JCheckBox that simulates a toggle switch with custom icons.
 */
public class ToggleSwitch extends JCheckBox {

    // Cached default icons
    private static final Icon DEFAULT_UP_ICON = PanelIcons.toggleUp();
    private static final Icon DEFAULT_DOWN_ICON = PanelIcons.toggleDown();
    private static final Icon DEFAULT_HOLE_ICON = PanelIcons.hole();

    /**
     *  Constructs a ToggleSwitch with the specified button text and vertical position for the text.
     *
     * @param toolTipText The text to display when the button is hovered over.
     */
    public ToggleSwitch(String toolTipText) {

        setIcon(DEFAULT_UP_ICON);
        setSelectedIcon(DEFAULT_DOWN_ICON);
        setDisabledIcon(DEFAULT_HOLE_ICON);

        // GTK enables rollover on buttons, and with no rollover icon set Synth falls back to
        // the selected icon: hovering an unselected control paints it as if it were on. The
        // panel has no hover state to show anyway, so turn rollover off.
        setRolloverEnabled(false);

        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);

        setSelected(false);

        setHorizontalTextPosition(AbstractButton.CENTER);
        setVerticalTextPosition(SwingConstants.TOP);

        setToolTipText(toolTipText);
    }
}
