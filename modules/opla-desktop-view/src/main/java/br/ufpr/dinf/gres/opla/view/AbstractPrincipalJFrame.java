package br.ufpr.dinf.gres.opla.view;

import arquitetura.io.FileUtils;
import br.ufpr.dinf.gres.opla.config.ManagerApplicationConfig;
import br.ufpr.dinf.gres.opla.view.util.Constants;
import br.ufpr.dinf.gres.opla.view.util.UserHome;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Fernando
 */
public abstract class AbstractPrincipalJFrame extends javax.swing.JFrame {

    private org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(Principal.class);

    protected ManagerApplicationConfig config;

    protected void checkAll(JPanel panel, Boolean isChecked) {
        Arrays.asList(panel.getComponents()).stream()
                .filter(component -> component instanceof JCheckBox)
                .map(JCheckBox.class::cast)
                .forEach(check -> check.setSelected(isChecked));
        LOGGER.info("checked all JCheckBox " + panel.getName());
    }

    protected void enableAllChecks(JPanel panel, Boolean isDisabled) {
        Arrays.asList(panel.getComponents()).stream()
                .filter(component -> component instanceof JCheckBox)
                .map(JCheckBox.class::cast)
                .forEach(check -> check.setEnabled(isDisabled));

        LOGGER.info("JCheckBox in " + panel.getName() + " enable = " + isDisabled);
    }

    protected void enableAllRadioButons(JPanel panel, Boolean isDisabled) {
        Arrays.asList(panel.getComponents()).stream()
                .filter(component -> component instanceof JRadioButton)
                .map(JRadioButton.class::cast)
                .forEach(radioButon -> radioButon.setEnabled(isDisabled));

        LOGGER.info("JRadioButton in " + panel.getName() + " enable = " + isDisabled);
    }

    protected void enableAllSliders(JPanel panel, Boolean isDisabled) {
        Arrays.asList(panel.getComponents()).stream()
                .filter(component -> component instanceof JSlider)
                .map(JSlider.class::cast)
                .forEach(jSlider -> jSlider.setEnabled(isDisabled));

        LOGGER.info("JSlider in " + panel.getName() + " enable = " + isDisabled);
    }

    protected void configureProfile(JTextField jTexField, String path, String profileName) throws IOException {
        if (StringUtils.isNotBlank(path)) {
            LOGGER.info(profileName + " is configured");
            jTexField.setText(path);
        } else {
            Path target = Paths.get(UserHome.getOplaUserHome() + Constants.PROFILES_DIR + Constants.FILE_SEPARATOR + profileName);
            FileUtils.copy(Constants.PROFILES_DIR + Constants.FILE_SEPARATOR + profileName, target);
            jTexField.setText(target.toString());
            LOGGER.info("new profile = " + profileName + " has configured");
        }
    }

}
