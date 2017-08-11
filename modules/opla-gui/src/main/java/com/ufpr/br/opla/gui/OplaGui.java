package com.ufpr.br.opla.gui;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;

import com.ufpr.br.opla.configuration.UserHome;

import arquitetura.io.ReaderConfig;


public class OplaGui {
  public static void main(String args[]) {
    java.awt.EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        try {
          ReaderConfig.load();
          database.Database.setPathToDB(UserHome.getPathToDb());

          StartUp gui = new StartUp();
          gui.setExtendedState(JFrame.MAXIMIZED_BOTH);
          gui.setVisible(true);

        } catch (Exception ex) {
          Logger.getLogger(OplaGui.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    });
  }
}
