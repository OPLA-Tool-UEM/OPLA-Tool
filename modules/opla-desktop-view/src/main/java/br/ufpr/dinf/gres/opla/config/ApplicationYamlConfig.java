package br.ufpr.dinf.gres.opla.config;

import org.apache.commons.lang.StringUtils;

/**
 * Representação do arquivo de configuração "application.yaml"
 * 
 * @author Fernando-Godoy
 *
 */
public class ApplicationYamlConfig {

	private String pathToTemplateModelsDirectory;
	private String directoryToSaveModels;
	private String directoryToExportModels;
	private String pathToProfile;
	private String pathToProfileConcern;
	private String pathToProfileRelationships;
	private String pathToProfilePatterns;

	public String getPathToTemplateModelsDirectory() {
		return StringUtils.defaultString(pathToTemplateModelsDirectory);
	}

	public void setPathToTemplateModelsDirectory(String pathToTemplateModelsDirectory) {
		this.pathToTemplateModelsDirectory = pathToTemplateModelsDirectory;
	}

	public String getDirectoryToSaveModels() {
		return StringUtils.defaultString(directoryToSaveModels);
	}

	public void setDirectoryToSaveModels(String directoryToSaveModels) {
		this.directoryToSaveModels = directoryToSaveModels;
	}

	public String getDirectoryToExportModels() {
		return StringUtils.defaultString(directoryToExportModels);
	}

	public void setDirectoryToExportModels(String directoryToExportModels) {
		this.directoryToExportModels = directoryToExportModels;
	}

	public String getPathToProfile() {
		return StringUtils.defaultString(pathToProfile);
	}

	public void setPathToProfile(String pathToProfile) {
		this.pathToProfile = pathToProfile;
	}

	public String getPathToProfileConcern() {
		return StringUtils.defaultString(pathToProfileConcern);
	}

	public void setPathToProfileConcern(String pathToProfileConcern) {
		this.pathToProfileConcern = pathToProfileConcern;
	}

	public String getPathToProfileRelationships() {
		return StringUtils.defaultString(pathToProfileRelationships);
	}

	public void setPathToProfileRelationships(String pathToProfileRelationships) {
		this.pathToProfileRelationships = pathToProfileRelationships;
	}

	public String getPathToProfilePatterns() {
		return StringUtils.defaultString(pathToProfilePatterns);
	}

	public void setPathToProfilePatterns(String pathToProfilePatterns) {
		this.pathToProfilePatterns = pathToProfilePatterns;
	}

}
