package com.ccs.baby.core;

// Manchester Baby Simulator
// by David Sharp
// January 2001
// requires Java v1.2 or later

import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JFrame;

import com.ccs.baby.disassembler.Disassembler;
import com.ccs.baby.animation.AnimationManager;
import com.ccs.baby.menu.MenuSetup;
import com.ccs.baby.ui.SwitchPanel;
import com.ccs.baby.ui.CrtPanel;
import com.ccs.baby.ui.TexturedJPanel;
import com.ccs.baby.ui.LampManager;
import com.ccs.baby.ui.FpsLabelService;
import com.ccs.baby.ui.DebugPanel;

public class Baby extends JFrame {

    // Get the current directory
    private static String currentDir;

    private final AnimationManager animationManager;
    public static volatile boolean running = false;

    public static TexturedJPanel mainPanel;

    public Baby() {

        try {
            currentDir = System.getProperty("user.home");
            System.out.println(currentDir);
        } catch (SecurityException e) {
            System.out.println("user.dir not accessible from applet");
            System.out.println(e.getMessage());
        }

        // Create LampManager
        LampManager lampManager = new LampManager();

        // Create main hardware components
        Store store = new Store();
        Control control = new Control(store, lampManager);
        store.setControl(control);

        CrtPanel crtPanel = new CrtPanel(store, control);
        crtPanel.setOpaque(false);
        crtPanel.setPreferredSize(new Dimension(400, 386));

        SwitchPanel switchPanel = new SwitchPanel(store, control, crtPanel, this);
        switchPanel.setOpaque(false);
        control.setSwitchPanel(switchPanel);    // Tell control about switchPanel

        // Create Disassembler
        Disassembler disassembler = new Disassembler(store, control, crtPanel);

        // Create a container mainPanel that wraps crtPanel and switchPanel
        mainPanel = new TexturedJPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setSize(690, 905);
        mainPanel.add(crtPanel, BorderLayout.NORTH);
        mainPanel.add(switchPanel);

        // Setup a DebugPanel (aka modernControls)
        DebugPanel debugPanel = new DebugPanel(control, switchPanel);
        debugPanel.setOpaque(true);

        // Set up display window GUI
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(mainPanel, BorderLayout.CENTER);
        contentPane.add(debugPanel, BorderLayout.SOUTH);

        // Get the FpsLabelService from the debugPanel
        FpsLabelService fpsLabelService = debugPanel.getFpsLabelService();

        // Initialise AnimationManager
        animationManager = new AnimationManager(control, crtPanel, switchPanel, true, fpsLabelService);

        // Set up and add menu bars to the window
        JMenuBar menuBar = new JMenuBar();
        new MenuSetup(menuBar, store, control, crtPanel, switchPanel, disassembler, currentDir, this, debugPanel);
        setJMenuBar(menuBar);

        // Reset the hardware to initial values
        store.reset();
        control.reset();

        // Load a program by default "diffeqt.asm"
        try {
            store.loadLocalModernAssembly("demos/diffeqt.asm");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(getContentPane(), "Default program not loaded. " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        // render and display the CRT display
        crtPanel.setToolTipText("The monitor.");
        crtPanel.render();
        crtPanel.repaint();

        // Open window
        setVisible(true);

        // This helps toggle the visibility of the debugPanel
        contentPane.revalidate();
        contentPane.repaint();


        // quit program when close icon clicked
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }

    // Main method to create main window
    public static void main(String args[]) {

        Baby baby = new Baby();
        baby.setSize(700, 950);
        baby.setTitle("Baby");
        baby.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        baby.setVisible(true);
        baby.setResizable(false);
    }

    // Delegate animation control methods
    public synchronized void startAnimation() {
        animationManager.startAnimation();
        running = true;
    }

    public synchronized void stopAnimation() {
        animationManager.stopAnimation();
        running = false;
    }
}