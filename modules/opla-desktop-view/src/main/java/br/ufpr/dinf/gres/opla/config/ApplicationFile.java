package br.ufpr.dinf.gres.opla.config;

/**
 * @author elf
 */
public class ApplicationFile {

    private static ManagerApplicationConfig instance = null;

    protected ApplicationFile() {
    }

    public static ManagerApplicationConfig getInstance() {
        if (instance == null) {
            instance = new ManagerApplicationConfig();
        }
        return instance;
    }
}
