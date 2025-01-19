package com.ccs.baby.menu;

import com.ccs.baby.ui.CrtPanel;
import com.ccs.baby.ui.SwitchPanel;
import com.ccs.baby.disassembler.Disassembler;
import com.ccs.baby.debug.DebugPanel;
import com.ccs.baby.ui.display.DisplayStore;
import com.ccs.baby.ui.display.DisplayControl;
import com.ccs.baby.ui.display.DisplayAccumulator;
import com.ccs.baby.ui.display.DisplayDisassemblerWindow;
import com.ccs.baby.ui.display.DisplayDebugPanel;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.KeyStroke;

import java.awt.event.KeyEvent;

public class ViewMenu {

    /**
     * Create the View menu
     *
     * @param crtPanel     the CRT panel to display the store, control, and accumulator
     * @param switchPanel  the switch panel to display the store, control, and accumulator
     * @param disassembler the disassembler to display the disassembled code
     * @param debugPanel   the debug panel to display the debug information
     * @return the View menu
     */
    public static JMenu createViewMenu(CrtPanel crtPanel, SwitchPanel switchPanel, Disassembler disassembler, DebugPanel debugPanel) {

        // Create the View menu
        JMenu viewMenu = new JMenu("View");

        // Create menu items
        JMenuItem viewStore = new JMenuItem("Store");
        JMenuItem viewControl = new JMenuItem("Control");
        JMenuItem viewAccumulator = new JMenuItem("Accumulator");
        JMenuItem viewDisassembler = new JMenuItem("Disassembler");
        JMenuItem viewDebugPanel = new JCheckBoxMenuItem("Debug ");

        // Add action listeners for each item
        viewStore.addActionListener(new DisplayStore(crtPanel, switchPanel));
        viewControl.addActionListener(new DisplayControl(crtPanel, switchPanel));
        viewAccumulator.addActionListener(new DisplayAccumulator(crtPanel, switchPanel));
        viewDisassembler.addActionListener(new DisplayDisassemblerWindow(disassembler));
        viewDebugPanel.addActionListener(new DisplayDebugPanel(debugPanel));

        // Add mnemonics (keyboard shortcuts) for macOS, Windows, and Linux
        viewMenu.setMnemonic(KeyEvent.VK_V); // Alt + V
        viewStore.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.ALT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK)); // Alt + Ctrl + S
        viewControl.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.ALT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK)); // Alt + Ctrl + C
        viewAccumulator.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.ALT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK)); // Alt + Ctrl + A
        viewDisassembler.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.ALT_DOWN_MASK)); // Alt + D
        viewDebugPanel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.ALT_DOWN_MASK )); // Alt + B

        // Add items to the view menu
        viewMenu.add(viewStore);
        viewMenu.add(viewControl);
        viewMenu.add(viewAccumulator);
        viewMenu.add(viewDisassembler);
        viewMenu.add(viewDebugPanel);

        return viewMenu;
    }
}