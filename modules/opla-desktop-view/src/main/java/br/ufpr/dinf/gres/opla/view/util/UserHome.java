package br.ufpr.dinf.gres.opla.view.util;

import arquitetura.io.FileUtils;

import java.nio.file.Paths;

/**
 * @author elf
 */
public class UserHome {

    /**
     * User Home directory Ex: C:/User/oplatool/ or /home/user/oplatool/
     *
     * @return
     */
    public static String getOplaUserHome() {
        return Constants.USER_HOME + Constants.FILE_SEPARATOR + "oplatool" + Constants.FILE_SEPARATOR;
    }

    public static String getConfigurationFilePath() {
        return getOplaUserHome() + "application.yaml";
    }

    public static String getGuiSettingsFilePath() {
        return getOplaUserHome() + "guisettings.yml";
    }

    public static void createDefaultOplaPathIfDontExists() {
        FileUtils.createDirectory(Paths.get(getOplaUserHome()));
    }

    public static void createProfilesPath() {
        Utils.createPath(getOplaUserHome() + "profiles/");
    }

    public static void createTemplatePath() {
        Utils.createPath(getOplaUserHome() + "templates/");
    }

    public static void createOutputPath() {
        Utils.createPath(getOplaUserHome() + "output/");
    }

    public static void createTempPath() {
        Utils.createPath(getOplaUserHome() + "temp/");
    }

    public static String getPathToDb() {
        return getOplaUserHome() + "db" + Constants.FILE_SEPARATOR + "oplatool.db";
    }

    public static String getPathToConfigFile() {
        return getOplaUserHome() + "application.yaml";
    }
}
