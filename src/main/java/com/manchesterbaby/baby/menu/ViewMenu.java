package com.manchesterbaby.baby.menu;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.KeyStroke;
import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;

import com.manchesterbaby.baby.controller.CrtPanelController;
import com.manchesterbaby.baby.core.Baby;
import com.manchesterbaby.baby.disassembler.Disassembler;
import com.manchesterbaby.baby.manual.ReferenceManual;
import com.manchesterbaby.baby.ui.DebugPanel;
import com.manchesterbaby.baby.ui.CrtPanel.DisplayType;
import com.manchesterbaby.baby.ui.display.DisplayDebugPanel;
import com.manchesterbaby.baby.ui.display.DisplayDisassemblerWindow;
import com.manchesterbaby.baby.utils.AppSettings;
import com.manchesterbaby.baby.utils.CheerpJUtils;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public class ViewMenu {

    private static JFrame _parentFrame;

    /**
     * Create the View menu
     *
     * @param crtPanelController the CRT control panel to control the CRT panel
     * @param disassembler    the disassembler to display the disassembled code
     * @param debugPanel      the debug panel to display the debug information
     * @return the View menu
     */
    public static JMenu createViewMenu(
            CrtPanelController crtPanelController,
            Disassembler disassembler,
            DebugPanel debugPanel,
            JFrame parentFrame
    ) {
        _parentFrame = parentFrame;

        // Create the View menu
        JMenu viewMenu = new JMenu("View");

        // Create menu items
        JMenuItem viewStore = new JMenuItem("Store");
        JMenuItem viewControl = new JMenuItem("Control");
        JMenuItem viewAccumulator = new JMenuItem("Accumulator");
        JMenuItem viewDisassemblerWindow = new JMenuItem("Disassembler");
        JCheckBoxMenuItem viewDisassemblerTab = new JCheckBoxMenuItem("Disassembler Tab");
        JMenuItem viewDebugPanel = new JCheckBoxMenuItem("Debug");

        // Add action listeners for each item
        viewStore.addActionListener(e -> crtPanelController.setCrtDisplay(DisplayType.STORE));
        viewControl.addActionListener(e -> crtPanelController.setCrtDisplay(DisplayType.CONTROL));
        viewAccumulator.addActionListener(e -> crtPanelController.setCrtDisplay(DisplayType.ACCUMULATOR));

        if(CheerpJUtils.onCheerpj()) {
        
            
            viewDisassemblerTab.addActionListener(e -> {
                JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
                if (item.isSelected()) {
                    ((Baby) _parentFrame).addDisassemblerTab(disassembler);
                } else {
                    ((Baby) _parentFrame).removeDisassemblerTab(disassembler);
                }
            });
            
        }
        else
        {
            viewDisassemblerWindow.addActionListener(new DisplayDisassemblerWindow(disassembler));
        }
        viewDebugPanel.addActionListener(new DisplayDebugPanel(debugPanel));

        // Add mnemonics (keyboard shortcuts) for macOS, Windows, and Linux
        viewMenu.setMnemonic(KeyEvent.VK_V); // Alt + V
        viewStore.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.ALT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK)); // Alt + Ctrl + S
        viewControl.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.ALT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK)); // Alt + Ctrl + C
        viewAccumulator.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.ALT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK)); // Alt + Ctrl + A
        viewDebugPanel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.ALT_DOWN_MASK)); // Alt + B

        // Add items to the view menu
        viewMenu.add(viewStore);
        viewMenu.add(viewControl);
        viewMenu.add(viewAccumulator);
        viewMenu.addSeparator();
        if(CheerpJUtils.onCheerpj()) {
            viewMenu.add(viewDisassemblerTab);
            viewDisassemblerTab.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.ALT_DOWN_MASK)); // Alt + T
        }
        else {  
            viewMenu.add(viewDisassemblerWindow);
            viewDisassemblerWindow.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.ALT_DOWN_MASK)); // Alt + D
        }
        viewMenu.addSeparator();
        viewMenu.add(viewDebugPanel);
        
        viewDebugPanel.setSelected(AppSettings.getInstance().isShowDebugPanel());

        // Add zoom submenu if not running in browser and not macOS
        if(!CheerpJUtils.onCheerpj() && !System.getProperty("os.name").startsWith("Mac")) {
        
            JMenu zoomMenu = new JMenu("Zoom on next start");
            String[] zoomLevels;
            boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");
            
            if (isWindows) {
                zoomLevels = new String[]{"0.5", "0.75", "default", "1.25", "1.5", "2", "2.5", "3"};
            } else {
                zoomLevels = new String[]{"default", "2", "3"};
            }
            
            String currentZoom = AppSettings.getInstance().getUiScaleSetting();
            ButtonGroup zoomGroup = new ButtonGroup();

            for (String level : zoomLevels) {
                JCheckBoxMenuItem zoomItem = new JCheckBoxMenuItem(level);
                zoomItem.setSelected(level.equals(currentZoom));
                zoomItem.addActionListener(e -> {
                    AppSettings.getInstance().setUiScaleSetting(level);

                    int choice = JOptionPane.showConfirmDialog(
                        _parentFrame,
                        "Zoom will take effect when the simulator is next started. Restart it now?",
                        "Restart Required",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                    );
                    
                    if (choice == JOptionPane.YES_OPTION) {
                        try {
                            // The exact Java executable, e.g. the bundled JRE under the
                            // launch4j .exe on Windows rather than whatever is on the PATH.
                            String javaPath = ProcessHandle.current().info().command().orElse(null);

                            // Fallback to java.home if the OS won't tell us the command
                            if (javaPath == null) {
                                String javaHome = System.getProperty("java.home");
                                if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                                    javaPath = javaHome + File.separator + "bin" + File.separator + "java.exe";
                                } else {
                                    javaPath = javaHome + File.separator + "bin" + File.separator + "java";
                                }
                            }
                            
                            // Get our JAR's location
                            String jarPath;
                            URL location = Baby.class.getProtectionDomain().getCodeSource().getLocation();
                            try {
                                jarPath = new File(location.toURI()).getAbsolutePath();
                            } catch (URISyntaxException e1) {
                                // Fallback to raw path if URI conversion fails
                                jarPath = location.getPath();
                            }

                            ProcessBuilder pb = new ProcessBuilder(javaPath, "-jar", jarPath);
                            pb.start();
                            System.exit(0);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(
                                _parentFrame,
                                "Error restarting simulator: " + ex.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                            );
                        }
                    }
                });
                zoomGroup.add(zoomItem);
                zoomMenu.add(zoomItem);
            }
            viewMenu.addSeparator();
            viewMenu.add(zoomMenu);
        }

        return viewMenu;
    }
}