package com.manchesterbaby.baby.ui;

import javax.swing.*;

import com.manchesterbaby.baby.ui.components.ToggleSwitch;
import com.manchesterbaby.baby.utils.AppSettings;

import static com.manchesterbaby.baby.utils.CallbackUtils.runCallback;

import java.awt.Rectangle;


public class StaticisorPanel extends JPanel {
    private Runnable onManAutoChange;
    private Runnable onLineSwitchChange;

    private final ToggleSwitch manAuto;
    private final ToggleSwitch[] lineSwitch = new ToggleSwitch[13];
    private final ToggleSwitch[] functionSwitch = new ToggleSwitch[8];

    // Design coordinates of the staticisor switches
    public static final int DESIGN_WIDTH = 700;
    public static final int DESIGN_HEIGHT = 124;
    private static final int SWITCH_WIDTH = 34;
    private static final int SWITCH_HEIGHT = 37;
    private static final int LINE_ROW_Y = 25;
    private static final int LINE_ORIGIN_X = 112;
    private static final int FUNCTION_ROW_Y = 62;
    private static final int FUNCTION_ORIGIN_X = 231;
    private static final int MAN_AUTO_X = 469;

    public StaticisorPanel() {
        setLayout(new ScaledLayout(DESIGN_WIDTH, DESIGN_HEIGHT));
        setOpaque(false);

        ToggleSwitch dud = new ToggleSwitch("Left unconnected.");
        dud.setSelected(true); // Ensures it starts in the correct default state
        place(dud, LINE_ORIGIN_X, LINE_ROW_Y); // Needed to align ManAuto switch with the L stat switches
        dud.setVisible(AppSettings.getInstance().isShowDisconnectedButtons());

        initialiseLineSwitches();  // L stat switches
        initialiseFunctionSwitches(); // F stat switches

        // ManAuto switch
        manAuto = new ToggleSwitch("Select whether to execute the store or the manual instruction.");
        manAuto.addActionListener(e -> runCallback(onManAutoChange));
        place(manAuto, MAN_AUTO_X, FUNCTION_ROW_Y);
    }

    private void place(ToggleSwitch toggleSwitch, int x, int y) {
        add(toggleSwitch, new Rectangle(x, y, SWITCH_WIDTH, SWITCH_HEIGHT));
    }


    public void setOnManAutoChange(Runnable callback) {
        this.onManAutoChange = callback;
    }

    public void setOnLineSwitchChange(Runnable callback) {
        this.onLineSwitchChange = callback;
    }


    public ToggleSwitch getLineSwitch(int index) {
        if (index < 0 || index >= lineSwitch.length) {
            throw new IndexOutOfBoundsException("Invalid line switch index: " + index);
        }
        return lineSwitch[index];
    }

    public void setLineSwitch(int index, boolean selected) {
        if (index >= 0 && index < lineSwitch.length) {
            lineSwitch[index].setSelected(selected);
        }
    }


    public ToggleSwitch getFunctionSwitch(int index) {
        if (index < 0 || index >= functionSwitch.length) {
            throw new IndexOutOfBoundsException("Invalid function switch index: " + index);
        }
        return functionSwitch[index];
    }

    public void setFunctionSwitch(int index, boolean selected) {
        if (index >= 0 && index < functionSwitch.length) {
            functionSwitch[index].setSelected(selected);
        }
    }


    public boolean getManAuto() {
        return manAuto.isSelected();
    }

    public void setManAuto(boolean value) {
        manAuto.setSelected(value);
    }


    private void initialiseLineSwitches() {
        for (int i = 0; i < 13; i++) {
            if (i < 5) {
                lineSwitch[i] = new ToggleSwitch("Selects the action line to be adjusted or executed.");
            } else if (i == 5 || i == 6 || i == 12) {
                lineSwitch[i] = new ToggleSwitch("Left unconnected.");
                lineSwitch[i].setSelected(true);
                lineSwitch[i].setVisible(AppSettings.getInstance().isShowDisconnectedButtons());
            } else {
                lineSwitch[i] = new ToggleSwitch(""); // No tooltip for other switches
            }

            // Disable switches between 7 and 11
            if (i > 6 && i < 12) {
                lineSwitch[i].setEnabled(false);
            }

            // the dud switch occupies the first position, so these start one along
            place(lineSwitch[i], LINE_ORIGIN_X + (i + 1) * SWITCH_WIDTH, LINE_ROW_Y);

            // Add ActionListener
            lineSwitch[i].addActionListener(e -> runCallback(onLineSwitchChange));
        }
    }


    private void initialiseFunctionSwitches() {
        for (int i = 0; i < 7; i++) {

            if (i < 3) {
                functionSwitch[i] = new ToggleSwitch("Selects the function number to be manually executed.");
            }

            if (i == 3) {
                functionSwitch[i] = new ToggleSwitch("Left unconnected.");
                functionSwitch[i].setSelected(true);
                functionSwitch[i].setVisible(AppSettings.getInstance().isShowDisconnectedButtons());
            }

            if (i > 3) {
                functionSwitch[i] = new ToggleSwitch("");
                functionSwitch[i].setEnabled(false);
                functionSwitch[i].setVisible(false);
            }

            place(functionSwitch[i], FUNCTION_ORIGIN_X + i * SWITCH_WIDTH, FUNCTION_ROW_Y);
        }
    }


}