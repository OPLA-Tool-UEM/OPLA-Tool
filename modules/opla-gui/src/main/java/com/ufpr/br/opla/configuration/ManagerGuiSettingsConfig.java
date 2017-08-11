/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ufpr.br.opla.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author elf
 */
public class ManagerGuiSettingsConfig {

	private GuiSettings guisettings;

	public ManagerGuiSettingsConfig() {
		try {
			Yaml yaml = new Yaml();
			this.guisettings = yaml.loadAs(new FileInputStream(new File(UserHome.getGuiSettingsFilePath())),
					GuiSettings.class);
		} catch (FileNotFoundException ex) {
			Logger.getLogger(GuiSettings.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public int getFontSize() {
		return this.guisettings.getFontSize();
	}

	public String getEdChartType() {
		return this.guisettings.getEdChartType();
	}

	public String getSaveChartsAsPng() {
		return this.guisettings.getSaveChartsAsPng();
	}

	/**
	 * Não usado ainda.
	 * 
	 * @param fontSize
	 */
	public void setFontSize(int fontSize) {
		this.guisettings.setFontSize(fontSize);
		updateConfigurationFile();
	}

	private void updateConfigurationFile() {
		try {
			Yaml yaml = new Yaml();
			yaml.dump(guisettings, new FileWriter(new File(UserHome.getGuiSettingsFilePath())));
		} catch (IOException ex) {
			Logger.getLogger(ManagerApplicationConfig.class.getName()).log(Level.SEVERE,
					"Ops, Error when try update configuration gui file: {0}", ex);
		}
	}
}
