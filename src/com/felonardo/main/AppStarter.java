package com.felonardo.main;

import com.felonardo.ui.MainScreen;

public class AppStarter {
    
    public static void main(String[] args) {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.name",
            "Genetic Magic Square Finder");
        
        MainScreen screen = new MainScreen();
        screen.setVisible(true);
    }
    
}
