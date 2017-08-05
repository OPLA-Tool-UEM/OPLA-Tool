package com.ufpr.br.opla.gui2;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;

import com.ufpr.br.opla.configuration.UserHome;

import arquitetura.io.ReaderConfig;


public class Oplagui {
  public static void main(String args[]) {
    java.awt.EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        com.ufpr.br.opla.gui2.StartUp gui;
        
        try {
          ReaderConfig.load();
          database.Database.setPathToDB(UserHome.getPathToDb());

          gui = new StartUp();
          gui.setExtendedState(JFrame.MAXIMIZED_BOTH);
          gui.setVisible(true);

        } catch (Exception ex) {
          Logger.getLogger(Oplagui.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    });
  }
}
