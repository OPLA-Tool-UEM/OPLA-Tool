package com.ufpr.br.opla.gui;

import javax.swing.JFrame;

import org.apache.log4j.Logger;

import com.ufpr.br.opla.configuration.UserHome;

import arquitetura.io.ReaderConfig;

public class OplaGui {
	private static final Logger LOGGER = Logger.getLogger(OplaGui.class);

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
					LOGGER.info(ex);
				}
			}
		});
	}
}
