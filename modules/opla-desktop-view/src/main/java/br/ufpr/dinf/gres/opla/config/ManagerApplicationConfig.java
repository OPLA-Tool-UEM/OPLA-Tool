package br.ufpr.dinf.gres.opla.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import arquitetura.io.DirTarget;
import br.ufpr.dinf.gres.opla.view.util.AlertUtil;
import br.ufpr.dinf.gres.opla.view.util.UserHome;

/**
 *
 * @author elf
 */
public class ManagerApplicationConfig {

    private static final Logger LOGGER = Logger.getLogger(ManagerApplicationConfig.class);

    private DirTarget configurationFile;

    public ManagerApplicationConfig() {
        try {
            Yaml yaml = new Yaml();
            Path path = Paths.get(UserHome.getConfigurationFilePath());
            this.configurationFile = yaml.loadAs(new FileInputStream(path.toFile()), DirTarget.class);
        } catch (FileNotFoundException ex) {
            LOGGER.warn(ex);
            AlertUtil.showMessage(AlertUtil.DEFAULT_ALERT_ERROR);
        }
    }

    public DirTarget getConfig() {
        return this.configurationFile;
    }

    public void updatePathToProfileSmarty(String newpath) throws IOException {
        this.configurationFile.setPathToProfile(newpath);
        updateConfigurationFile();
    }

    public void updatePathToProfilePatterns(String newpath) throws IOException {
        this.configurationFile.setPathToProfilePatterns(newpath);
        updateConfigurationFile();
    }

    public void updatePathToProfileRelationships(String newpath) throws IOException {
        this.configurationFile.setPathToProfileRelationships(newpath);
        updateConfigurationFile();
    }

    public void updatePathToProfileConcerns(String newpath) throws IOException {
        this.configurationFile.setPathToProfileConcern(newpath);
        updateConfigurationFile();
    }

    public void updatePathToTemplateFiles(String newpath) throws IOException {
        this.configurationFile.setPathToTemplateModelsDirectory(newpath);
        updateConfigurationFile();
    }

    public void updatePathToExportModels(String newpath) throws IOException {
        this.configurationFile.setDirectoryToExportModels(newpath);
        updateConfigurationFile();
    }

    public void updatePathToSaveModels(String path) throws IOException {
        this.configurationFile.setDirectoryToSaveModels(path);
        updateConfigurationFile();

    }

    /**
     * Retorna os profile que estão em uso ou seja, não "" nem null.
     *
     */
    public String getProfilesUsed() {
        StringBuilder profiles = new StringBuilder();

        if (StringUtils.isNotBlank(this.configurationFile.getPathToProfile())) {
            profiles.append(this.configurationFile.getPathToProfile());
            profiles.append(",");
        }

        if (StringUtils.isNotBlank(this.configurationFile.getPathToProfileConcern())) {
            profiles.append(this.configurationFile.getPathToProfileConcern());
            profiles.append(",");
        }

        if (StringUtils.isNotBlank(this.configurationFile.getPathToProfilePatterns())) {
            profiles.append(this.configurationFile.getPathToProfilePatterns());
            profiles.append(",");
        }

        if (StringUtils.isNotBlank(this.configurationFile.getPathToProfileRelationships())) {
            profiles.append(this.configurationFile.getPathToProfileRelationships());
        }
        return profiles.toString();
    }

    private void updateConfigurationFile() throws IOException {
        try {
            final DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.FLOW);
            options.setPrettyFlow(true);

            Yaml yaml = new Yaml(options);
            yaml.dump(configurationFile, new FileWriter(Paths.get(UserHome.getConfigurationFilePath()).toFile()));
        } catch (IOException ex) {
            LOGGER.warn("Ops, Error when try update configuration file:", ex);
            throw ex;
        }
    }
}
