package br.ufpr.dinf.gres.opla.view;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.text.DefaultCaret;

import org.apache.commons.lang.StringUtils;

import arquitetura.io.FileUtils;
import br.ufpr.dinf.gres.loglog.LogLog;
import br.ufpr.dinf.gres.loglog.Logger;
import br.ufpr.dinf.gres.opla.config.ApplicationFile;
import br.ufpr.dinf.gres.opla.entity.Experiment;
import br.ufpr.dinf.gres.opla.view.log.LogListener;
import br.ufpr.dinf.gres.opla.view.model.AlgorithmComboModel;
import br.ufpr.dinf.gres.opla.view.model.TableModelExecution;
import br.ufpr.dinf.gres.opla.view.model.TableModelExperiment;
import br.ufpr.dinf.gres.opla.view.model.TableModelMapObjectiveName;
import br.ufpr.dinf.gres.opla.view.util.AlertUtil;
import br.ufpr.dinf.gres.opla.view.util.Constants;
import br.ufpr.dinf.gres.opla.view.util.OSUtils;
import br.ufpr.dinf.gres.opla.view.util.UserHome;
import br.ufpr.dinf.gres.opla.view.util.Utils;
import br.ufpr.dinf.gres.persistence.dao.ExecutionDAO;
import br.ufpr.dinf.gres.persistence.dao.ExperimentDAO;
import java.util.List;

/**
 *
 * @author Fernando
 */
public class Principal extends AbstractPrincipalJFrame {

    private static final long serialVersionUID = 1L;
    private final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(Principal.class);
    private static final LogLog VIEW_LOGGER = Logger.getLogger();

    private final TableModelExperiment tmExperiments = new TableModelExperiment();
    private final TableModelExperiment tmExecExperiments = new TableModelExperiment();
    private final TableModelExecution tmExecution = new TableModelExecution();
    private final TableModelMapObjectiveName tmMapObjectiveSolution = new TableModelMapObjectiveName();

    private final ExperimentDAO experimentDAO;
    private final ExecutionDAO executionDAO;

    public Principal() {
        initComponents();
        this.setLocationRelativeTo(null);
        this.setExtendedState(MAXIMIZED_BOTH);
        defineModels();
        this.experimentDAO = new ExperimentDAO();
        this.executionDAO = new ExecutionDAO();
        resultExecutionsLoad();
    }

    @SuppressWarnings("unchecked")
    private void defineModels() {
        this.cbAlgothm.setModel(new AlgorithmComboModel());
        this.tbExperiments.setModel(tmExperiments);
        this.tbExecutions.setModel(tmExecExperiments);
        this.tbRuns.setModel(tmExecution);
        this.tbObjectiveSolution.setModel(tmMapObjectiveSolution);
    }

    public void configureView() throws IOException, Exception {
        configPaths();
        configureLogArea();
        configurePainelProfiles();
        configurePanelMutation();
        configurePanelScopeSelection();
        configurePanelOperators();
        configureSmartyProfile();
        configureConcernsProfile();
        configurePatternsProfile();
        configureRelationshipsProfile();
        configureTemplates();
        configureLocaleToSaveModels();
        configureLocaleToExportModels();
        copyBinHypervolume();
        configureDb();
    }

    private void configPaths() {
        try {
            Utils.createPathsOplaTool();
            config = ApplicationFile.getInstance();
        } catch (Exception ex) {
            LOGGER.error(ex);
            AlertUtil.showMessage(AlertUtil.DEFAULT_ALERT_ERROR);
        }
    }

    private void configurePanelOperators() {
        enableAllSliders(panelOperatorOption, false);
    }

    private void configurePanelMutation() {
        checkAll(panelMutations, true);
        enableAllChecks(panelMutations, false);
    }

    private void configurePanelScopeSelection() {
        enableAllRadioButons(panelScopeSelection, false);
    }

    private void configureLogArea() {
        DefaultCaret cared = (DefaultCaret) taLogStatus.getCaret();
        cared.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        Logger.addListener(new LogListener(taLogStatus));
        VIEW_LOGGER.putLog("Inicializando OPLA-Tool");
    }

    private void configurePainelProfiles() {
        checkAll(panelCkProfiles, true);
    }

    private void configureSmartyProfile() throws IOException {
        try {
            configureProfile(tfSmartProfile, config.getConfig().getPathToProfile(), Constants.PROFILE_SMART_NAME);
            config.updatePathToProfileSmarty(tfSmartProfile.getText());
        } catch (IOException ex) {
            LOGGER.error("Smart Profile Config error: ", ex);
            throw ex;
        }
    }

    private void configureConcernsProfile() throws IOException {
        try {
            configureProfile(tfFeatureProfile, config.getConfig().getPathToProfileConcern(), Constants.PROFILE_CONCERNS_NAME);
            config.updatePathToProfileConcerns(tfFeatureProfile.getText());
        } catch (IOException ex) {
            LOGGER.error("Feature Profile Config error: ", ex);
            throw ex;
        }
    }

    private void configurePatternsProfile() throws IOException {
        try {
            configureProfile(tfPatternProfile, config.getConfig().getPathToProfilePatterns(), Constants.PROFILE_PATTERN_NAME);
            config.updatePathToProfilePatterns(tfPatternProfile.getText());
        } catch (IOException ex) {
            LOGGER.error("Pattern Profile Config error: ", ex);
            throw ex;
        }
    }

    private void configureRelationshipsProfile() throws IOException {
        try {
            configureProfile(tfRelationshipProfile, config.getConfig().getPathToProfileRelationships(), Constants.PROFILE_RELATIONSHIP_NAME);
            config.updatePathToProfileRelationships(tfRelationshipProfile.getText());
        } catch (IOException ex) {
            LOGGER.error("Relationship Profile Config error: ", ex);
            throw ex;
        }
    }

    private void configureTemplates() throws IOException {
        if (StringUtils.isNotBlank(config.getConfig().getPathToTemplateModelsDirectory())) {
            LOGGER.info("Templates Path is configured");
            tfTemplateDiretory.setText(config.getConfig().getPathToTemplateModelsDirectory());
        } else {
            try {
                String templatesBasePath = Constants.TEMPLATES_DIR + Constants.FILE_SEPARATOR;
                String simplesUmlPath = templatesBasePath + Constants.SIMPLES_UML_NAME;
                String simplesDiPath = templatesBasePath + Constants.SIMPLES_DI_NAME;
                String simplesNotationPath = templatesBasePath + Constants.SIMPLES_NOTATION_NAME;

                Path externalPathSimplesUml = Paths.get(UserHome.getOplaUserHome() + simplesUmlPath);
                Path externalPathSimplesDi = Paths.get(UserHome.getOplaUserHome() + simplesDiPath);
                Path externalPathSimplesNotation = Paths.get(UserHome.getOplaUserHome() + simplesNotationPath);

                FileUtils.copy(simplesUmlPath, externalPathSimplesUml);
                FileUtils.copy(simplesDiPath, externalPathSimplesDi);
                FileUtils.copy(simplesNotationPath, externalPathSimplesNotation);

                String template = UserHome.getOplaUserHome() + templatesBasePath;
                tfTemplateDiretory.setText(template);
                config.updatePathToTemplateFiles(tfTemplateDiretory.getText());
            } catch (IOException ex) {
                LOGGER.error("Template directory Config error: ", ex);
                throw ex;
            }
        }
    }

    private void configureLocaleToSaveModels() throws IOException {
        if (StringUtils.isNotBlank(config.getConfig().getDirectoryToSaveModels())) {
            LOGGER.info("Manipulation Directory is configured");
            tfManipulationDirectory.setText(config.getConfig().getDirectoryToSaveModels());
        } else {
            try {
                String path = UserHome.getOplaUserHome() + Constants.TEMP_DIR + Constants.FILE_SEPARATOR;
                tfManipulationDirectory.setText(path);
                config.updatePathToSaveModels(path);
            } catch (IOException ex) {
                LOGGER.error("Manipulation directory Config error: ", ex);
                throw ex;
            }
        }
    }

    private void configureLocaleToExportModels() throws IOException {
        if (StringUtils.isNotBlank(config.getConfig().getDirectoryToExportModels())) {
            LOGGER.info("Output Directory is configured");
            tfOutputDirectory.setText(config.getConfig().getDirectoryToExportModels());
        } else {
            try {
                String path = UserHome.getOplaUserHome() + Constants.OUTPUT_DIR + Constants.FILE_SEPARATOR;
                tfOutputDirectory.setText(path);
                tfOutputDirectory.updateUI();
                config.updatePathToExportModels(path);
            } catch (IOException ex) {
                LOGGER.error("Output directory Config error: ", ex);
                throw ex;
            }
        }
    }

    /**
     * Copy hybervolume binary to oplatool bins directory if OS isn't Windows.
     *
     * @throws Exception
     *
     */
    private void copyBinHypervolume() throws Exception {
        if (!OSUtils.isWindows()) {
            String target = UserHome.getOplaUserHome() + Constants.BINS_DIR;
            Path path = Paths.get(target + Constants.FILE_SEPARATOR + Constants.HYPERVOLUME_DIR);
            if (!Files.exists(path)) {
                FileUtils.createDirectory(path);
            }
            Utils.copy(Constants.HYPERVOLUME_DIR, path.toString());
        }
    }

    /**
     * Somente faz uma copia do banco de dados vazio para a pasta da oplatool no
     * diretorio do usuario se o mesmo nao existir.
     *
     * @throws Exception
     *
     */
    private void configureDb() throws Exception {
        Utils.createDataBaseIfNotExists();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        panelCkProfiles = new javax.swing.JPanel();
        ckSmarty = new javax.swing.JCheckBox();
        ckFeature = new javax.swing.JCheckBox();
        ckPatterns = new javax.swing.JCheckBox();
        ckRelationship = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        tfSmartProfile = new javax.swing.JTextField();
        btBrowserSmartProfile = new javax.swing.JButton();
        tfFeatureProfile = new javax.swing.JTextField();
        btBrowserFeatureProfile = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        tfPatternProfile = new javax.swing.JTextField();
        btBrowserPatternProfile = new javax.swing.JButton();
        tfRelationshipProfile = new javax.swing.JTextField();
        btBrowserRelationshipProfile = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        tfTemplateDiretory = new javax.swing.JTextField();
        btTemplateDirectory = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        tfManipulationDirectory = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        btManipulationDirectory = new javax.swing.JButton();
        btViewApplicationConfig = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        cbAlgothm = new javax.swing.JComboBox<>();
        tfNumberRuns = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        tfMaxEvaluations = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        tfPopulationSize = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        tfArchiveSize = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        ckConventional = new javax.swing.JCheckBox();
        ckComponentCoupling = new javax.swing.JCheckBox();
        ckClassCoupling = new javax.swing.JCheckBox();
        ckSize = new javax.swing.JCheckBox();
        ckFeatureDriven = new javax.swing.JCheckBox();
        ckFeatureInterlacing = new javax.swing.JCheckBox();
        ckFeatureDifusion = new javax.swing.JCheckBox();
        ckPLAExtensibility = new javax.swing.JCheckBox();
        ckCohesion = new javax.swing.JCheckBox();
        ckElegance = new javax.swing.JCheckBox();
        panelOperators = new javax.swing.JPanel();
        panelOperatorOption = new javax.swing.JPanel();
        jsMutation = new javax.swing.JSlider();
        jsCrossover = new javax.swing.JSlider();
        ckMutation = new javax.swing.JCheckBox();
        ckCrossover = new javax.swing.JCheckBox();
        panelMutations = new javax.swing.JPanel();
        ckFeatureDrivenMutation = new javax.swing.JCheckBox();
        ckMoveMethodMutation = new javax.swing.JCheckBox();
        ckAddClassMutation = new javax.swing.JCheckBox();
        ckMoveOperationMutation = new javax.swing.JCheckBox();
        ckAddManagerClassMutation = new javax.swing.JCheckBox();
        ckMoveAttributeMutation = new javax.swing.JCheckBox();
        jPanel12 = new javax.swing.JPanel();
        tfInputArchitecturePath = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        btConfirme = new javax.swing.JButton();
        btClean = new javax.swing.JButton();
        btSelectPath = new javax.swing.JButton();
        jPanel14 = new javax.swing.JPanel();
        tfOutputDirectory = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        btSelectOutputDirectory = new javax.swing.JButton();
        tfDescription = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        btRun = new javax.swing.JButton();
        jPanel15 = new javax.swing.JPanel();
        jPanel16 = new javax.swing.JPanel();
        ckMediator = new javax.swing.JCheckBox();
        ckStrategy = new javax.swing.JCheckBox();
        ckBridge = new javax.swing.JCheckBox();
        panelScopeSelection = new javax.swing.JPanel();
        rbRandom = new javax.swing.JRadioButton();
        rbElements = new javax.swing.JRadioButton();
        jPanel18 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tbExecutions = new javax.swing.JTable();
        jPanel26 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tbRuns = new javax.swing.JTable();
        jPanel27 = new javax.swing.JPanel();
        jComboBox1 = new javax.swing.JComboBox<>();
        jLabel15 = new javax.swing.JLabel();
        jPanel28 = new javax.swing.JPanel();
        cbObjectiveSoluction = new javax.swing.JComboBox<>();
        jLabel16 = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        tbObjectiveSolution = new javax.swing.JTable();
        jPanel29 = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        tbMetrics = new javax.swing.JTable();
        btNonDomitedSolutions = new javax.swing.JButton();
        jPanel19 = new javax.swing.JPanel();
        jPanel20 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbExperiments = new javax.swing.JTable();
        jPanel21 = new javax.swing.JPanel();
        btSelectObjective = new javax.swing.JButton();
        btGenerateChart = new javax.swing.JButton();
        jPanel22 = new javax.swing.JPanel();
        btEuclidianDistance = new javax.swing.JButton();
        jPanel23 = new javax.swing.JPanel();
        btHypervolume = new javax.swing.JButton();
        ckUseNormalization = new javax.swing.JCheckBox();
        jPanel24 = new javax.swing.JPanel();
        jPanel25 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        taLogStatus = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Profiles Configuration", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 14))); // NOI18N

        panelCkProfiles.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        panelCkProfiles.setName("Profile Smart Configuration"); // NOI18N

        ckSmarty.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        ckSmarty.setText("SMarty");
        ckSmarty.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ckSmartyActionPerformed(evt);
            }
        });

        ckFeature.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        ckFeature.setText("Feature");

        ckPatterns.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        ckPatterns.setText("Patterns");

        ckRelationship.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        ckRelationship.setText("Relationships");

        javax.swing.GroupLayout panelCkProfilesLayout = new javax.swing.GroupLayout(panelCkProfiles);
        panelCkProfiles.setLayout(panelCkProfilesLayout);
        panelCkProfilesLayout.setHorizontalGroup(
            panelCkProfilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCkProfilesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ckSmarty)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ckFeature)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ckPatterns)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ckRelationship)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelCkProfilesLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {ckFeature, ckPatterns, ckRelationship, ckSmarty});

        panelCkProfilesLayout.setVerticalGroup(
            panelCkProfilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCkProfilesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelCkProfilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ckSmarty)
                    .addComponent(ckFeature)
                    .addComponent(ckPatterns)
                    .addComponent(ckRelationship))
                .addContainerGap(10, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel4.setName("Panel Text Fields Profiles"); // NOI18N

        jLabel1.setText("Smarty Profile:");

        tfSmartProfile.setColumns(60);

        btBrowserSmartProfile.setText("Browser");

        tfFeatureProfile.setColumns(60);

        btBrowserFeatureProfile.setText("Browser");

        jLabel3.setText("Pattern Profile:");

        tfPatternProfile.setColumns(60);

        btBrowserPatternProfile.setText("Browser");

        tfRelationshipProfile.setColumns(60);

        btBrowserRelationshipProfile.setText("Browser");

        jLabel5.setText("Feature Profile:");

        jLabel17.setText("Relationship Profile:");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel17)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(tfRelationshipProfile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(35, 35, 35)
                        .addComponent(tfPatternProfile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addGap(33, 33, 33)
                        .addComponent(tfFeatureProfile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(37, 37, 37)
                        .addComponent(tfSmartProfile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btBrowserSmartProfile)
                    .addComponent(btBrowserFeatureProfile)
                    .addComponent(btBrowserPatternProfile)
                    .addComponent(btBrowserRelationshipProfile))
                .addGap(20, 20, 20))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(tfSmartProfile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btBrowserSmartProfile))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfFeatureProfile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btBrowserFeatureProfile)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfPatternProfile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btBrowserPatternProfile)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfRelationshipProfile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btBrowserRelationshipProfile)
                    .addComponent(jLabel17))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelCkProfiles, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelCkProfiles, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Template Configuration", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 14))); // NOI18N

        tfTemplateDiretory.setColumns(63);

        btTemplateDirectory.setText("Select a Directory");
        btTemplateDirectory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btTemplateDirectoryActionPerformed(evt);
            }
        });

        jLabel2.setText("Directory:");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfTemplateDiretory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btTemplateDirectory)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfTemplateDiretory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btTemplateDirectory)
                    .addComponent(jLabel2))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Manipulation Directory", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 14))); // NOI18N

        tfManipulationDirectory.setColumns(63);

        jLabel6.setText("Directory:");

        btManipulationDirectory.setText("Select a Directory");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfManipulationDirectory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btManipulationDirectory)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfManipulationDirectory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(btManipulationDirectory))
                .addContainerGap(13, Short.MAX_VALUE))
        );

        btViewApplicationConfig.setText("Visualize your application config file");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btViewApplicationConfig)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btViewApplicationConfig)
                .addContainerGap(137, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("General Configuration", jPanel1);

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Settings", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 14))); // NOI18N
        jPanel8.setName("Panel Settings"); // NOI18N

        tfNumberRuns.setColumns(10);

        jLabel7.setText("Number of Runs:");

        jLabel8.setText("Select Algorithm Whinch Want Use ");

        tfMaxEvaluations.setColumns(10);

        jLabel9.setText("Max Evaluations:");

        tfPopulationSize.setColumns(10);

        jLabel10.setText("Population Size:");

        tfArchiveSize.setColumns(10);

        jLabel11.setText("Archive Size:");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel8)
                    .addComponent(cbAlgothm, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addComponent(jLabel9)
                            .addComponent(jLabel10)
                            .addComponent(jLabel11))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tfMaxEvaluations, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(tfNumberRuns, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(tfPopulationSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(tfArchiveSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbAlgothm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfNumberRuns, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfMaxEvaluations, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfPopulationSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfArchiveSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Objective Functions", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 14))); // NOI18N
        jPanel9.setName("Panel Objective Functions"); // NOI18N

        ckConventional.setText("Conventional");

        ckComponentCoupling.setText("Component Coupling");

        ckClassCoupling.setText("Class Coupling");

        ckSize.setText("Size");

        ckFeatureDriven.setText("Feature Driven");

        ckFeatureInterlacing.setText("Features Interlacing");
        ckFeatureInterlacing.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ckFeatureInterlacingActionPerformed(evt);
            }
        });

        ckFeatureDifusion.setText("Features Diffusion");

        ckPLAExtensibility.setText("PLA Extensibility");

        ckCohesion.setText("Cohesion");

        ckElegance.setText("Elegance");

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ckConventional)
                    .addComponent(ckComponentCoupling)
                    .addComponent(ckClassCoupling)
                    .addComponent(ckSize)
                    .addComponent(ckPLAExtensibility))
                .addGap(100, 100, 100)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ckElegance)
                    .addComponent(ckCohesion)
                    .addComponent(ckFeatureDifusion)
                    .addComponent(ckFeatureInterlacing)
                    .addComponent(ckFeatureDriven))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ckConventional)
                    .addComponent(ckFeatureDriven))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ckComponentCoupling)
                    .addComponent(ckFeatureInterlacing))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ckClassCoupling)
                    .addComponent(ckFeatureDifusion))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ckSize)
                    .addComponent(ckCohesion))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ckPLAExtensibility)
                    .addComponent(ckElegance))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelOperators.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Operators", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 14))); // NOI18N

        panelOperatorOption.setName("Panel Operators Options"); // NOI18N

        jsMutation.setMajorTickSpacing(10);
        jsMutation.setMaximum(10);
        jsMutation.setMinorTickSpacing(1);
        jsMutation.setPaintLabels(true);
        jsMutation.setPaintTicks(true);
        jsMutation.setBorder(javax.swing.BorderFactory.createTitledBorder("Mutation Probability"));

        jsCrossover.setMajorTickSpacing(10);
        jsCrossover.setMaximum(10);
        jsCrossover.setMinorTickSpacing(1);
        jsCrossover.setPaintLabels(true);
        jsCrossover.setPaintTicks(true);
        jsCrossover.setSnapToTicks(true);
        jsCrossover.setBorder(javax.swing.BorderFactory.createTitledBorder("Crossover Probability"));

        ckMutation.setText("Mutation");
        ckMutation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ckMutationActionPerformed(evt);
            }
        });

        ckCrossover.setText("Crossover");

        javax.swing.GroupLayout panelOperatorOptionLayout = new javax.swing.GroupLayout(panelOperatorOption);
        panelOperatorOption.setLayout(panelOperatorOptionLayout);
        panelOperatorOptionLayout.setHorizontalGroup(
            panelOperatorOptionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelOperatorOptionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelOperatorOptionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelOperatorOptionLayout.createSequentialGroup()
                        .addComponent(ckMutation)
                        .addGap(18, 18, 18)
                        .addComponent(ckCrossover)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(panelOperatorOptionLayout.createSequentialGroup()
                        .addComponent(jsMutation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jsCrossover, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        panelOperatorOptionLayout.setVerticalGroup(
            panelOperatorOptionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelOperatorOptionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelOperatorOptionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ckMutation)
                    .addComponent(ckCrossover))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelOperatorOptionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jsCrossover, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jsMutation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelOperatorsLayout = new javax.swing.GroupLayout(panelOperators);
        panelOperators.setLayout(panelOperatorsLayout);
        panelOperatorsLayout.setHorizontalGroup(
            panelOperatorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelOperatorsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelOperatorOption, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelOperatorsLayout.setVerticalGroup(
            panelOperatorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelOperatorsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelOperatorOption, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        panelMutations.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Mutation Operators", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 14))); // NOI18N
        panelMutations.setName("Panel Mutations Operators"); // NOI18N

        ckFeatureDrivenMutation.setText("Feature-driven Mutation");

        ckMoveMethodMutation.setText("Move Method Mutation");

        ckAddClassMutation.setText("Add Class Mutation");

        ckMoveOperationMutation.setText("Move Operation Mutation");

        ckAddManagerClassMutation.setText("Add Manager Class Mutation");

        ckMoveAttributeMutation.setText("Move Attribute Mutation");

        javax.swing.GroupLayout panelMutationsLayout = new javax.swing.GroupLayout(panelMutations);
        panelMutations.setLayout(panelMutationsLayout);
        panelMutationsLayout.setHorizontalGroup(
            panelMutationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMutationsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelMutationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ckFeatureDrivenMutation)
                    .addComponent(ckMoveMethodMutation)
                    .addComponent(ckAddClassMutation))
                .addGap(64, 64, 64)
                .addGroup(panelMutationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ckMoveAttributeMutation)
                    .addComponent(ckAddManagerClassMutation)
                    .addComponent(ckMoveOperationMutation))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelMutationsLayout.setVerticalGroup(
            panelMutationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMutationsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelMutationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ckFeatureDrivenMutation)
                    .addComponent(ckMoveOperationMutation))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelMutationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ckMoveMethodMutation)
                    .addComponent(ckAddManagerClassMutation))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelMutationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ckAddClassMutation)
                    .addComponent(ckMoveAttributeMutation))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel12.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Input Architecture", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 14))); // NOI18N

        tfInputArchitecturePath.setColumns(50);

        jLabel12.setText("Path:");

        btConfirme.setText("Confirme");

        btClean.setText("Clean");

        btSelectPath.setText("Select  a Path");

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfInputArchitecturePath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel12Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btConfirme, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btClean, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btSelectPath, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel12Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btClean, btConfirme, btSelectPath});

        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfInputArchitecturePath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btConfirme)
                    .addComponent(btClean)
                    .addComponent(btSelectPath))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        jPanel12Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {btClean, btConfirme, btSelectPath});

        jPanel14.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Output Directory", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 14))); // NOI18N

        tfOutputDirectory.setColumns(50);

        jLabel13.setText("Path:");

        btSelectOutputDirectory.setText("Select a Directory");

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btSelectOutputDirectory)
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfOutputDirectory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfOutputDirectory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btSelectOutputDirectory)
                .addContainerGap(14, Short.MAX_VALUE))
        );

        tfDescription.setColumns(50);

        jLabel14.setText("Set a description for this execuition: (Optional)");

        btRun.setText("RUN");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jPanel12, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(panelOperators, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(panelMutations, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jLabel14)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfDescription, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btRun, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(panelOperators, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelMutations, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfDescription, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14)
                    .addComponent(btRun, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Execution Configuration", jPanel7);

        jPanel16.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Design Pattern Selection", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 14))); // NOI18N
        jPanel16.setName("Panel Desig Patterns Selection"); // NOI18N

        ckMediator.setText("Mediator");

        ckStrategy.setText("Strategy");

        ckBridge.setText("Bridge");

        javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ckMediator)
                .addGap(18, 18, 18)
                .addComponent(ckStrategy)
                .addGap(18, 18, 18)
                .addComponent(ckBridge)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel16Layout.setVerticalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ckMediator)
                    .addComponent(ckStrategy)
                    .addComponent(ckBridge))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelScopeSelection.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Scope Selection", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 14))); // NOI18N
        panelScopeSelection.setName("Panel Scope Selection"); // NOI18N

        rbRandom.setText("Random");

        rbElements.setText("Elements with same design pattern or none");

        javax.swing.GroupLayout panelScopeSelectionLayout = new javax.swing.GroupLayout(panelScopeSelection);
        panelScopeSelection.setLayout(panelScopeSelectionLayout);
        panelScopeSelectionLayout.setHorizontalGroup(
            panelScopeSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelScopeSelectionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelScopeSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rbRandom)
                    .addComponent(rbElements))
                .addContainerGap(711, Short.MAX_VALUE))
        );
        panelScopeSelectionLayout.setVerticalGroup(
            panelScopeSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelScopeSelectionLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(rbRandom)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rbElements)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelScopeSelection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(jPanel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelScopeSelection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(397, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Design Patterns", jPanel15);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Executions", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 14))); // NOI18N

        tbExecutions.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jScrollPane3.setViewportView(tbExecutions);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel26.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Runs", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 14))); // NOI18N

        tbRuns.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jScrollPane4.setViewportView(tbRuns);

        javax.swing.GroupLayout jPanel26Layout = new javax.swing.GroupLayout(jPanel26);
        jPanel26.setLayout(jPanel26Layout);
        jPanel26Layout.setHorizontalGroup(
            jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel26Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4)
                .addContainerGap())
        );
        jPanel26Layout.setVerticalGroup(
            jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel26Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 91, Short.MAX_VALUE)
                .addGap(15, 15, 15))
        );

        jPanel27.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel15.setText("Solution:");

        javax.swing.GroupLayout jPanel27Layout = new javax.swing.GroupLayout(jPanel27);
        jPanel27.setLayout(jPanel27Layout);
        jPanel27Layout.setHorizontalGroup(
            jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel27Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel15)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 414, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel27Layout.setVerticalGroup(
            jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel27Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel28.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel16.setText("Objective Solution:");

        tbObjectiveSolution.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jScrollPane5.setViewportView(tbObjectiveSolution);

        javax.swing.GroupLayout jPanel28Layout = new javax.swing.GroupLayout(jPanel28);
        jPanel28.setLayout(jPanel28Layout);
        jPanel28Layout.setHorizontalGroup(
            jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel28Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane5)
                    .addGroup(jPanel28Layout.createSequentialGroup()
                        .addComponent(jLabel16)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbObjectiveSoluction, javax.swing.GroupLayout.PREFERRED_SIZE, 362, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel28Layout.setVerticalGroup(
            jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel28Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbObjectiveSoluction, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel29.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        tbMetrics.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jScrollPane6.setViewportView(tbMetrics);

        btNonDomitedSolutions.setText("Non-Domited Solutions");

        javax.swing.GroupLayout jPanel29Layout = new javax.swing.GroupLayout(jPanel29);
        jPanel29.setLayout(jPanel29Layout);
        jPanel29Layout.setHorizontalGroup(
            jPanel29Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel29Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel29Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel29Layout.createSequentialGroup()
                        .addComponent(btNonDomitedSolutions)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane6))
                .addContainerGap())
        );
        jPanel29Layout.setVerticalGroup(
            jPanel29Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel29Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btNonDomitedSolutions)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel18Layout = new javax.swing.GroupLayout(jPanel18);
        jPanel18.setLayout(jPanel18Layout);
        jPanel18Layout.setHorizontalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel18Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel27, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel28, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel18Layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel26, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(10, 10, 10))
                    .addComponent(jPanel29, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0))
        );
        jPanel18Layout.setVerticalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel18Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel26, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel27, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel28, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel29, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Results", jPanel18);

        jPanel20.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        tbExperiments.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(tbExperiments);

        javax.swing.GroupLayout jPanel20Layout = new javax.swing.GroupLayout(jPanel20);
        jPanel20.setLayout(jPanel20Layout);
        jPanel20Layout.setHorizontalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel20Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 938, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel20Layout.setVerticalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel20Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel21.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Soluctions in the Seach Space", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 14))); // NOI18N

        btSelectObjective.setText("Select the Objective");

        btGenerateChart.setText("Generate Chart");

        javax.swing.GroupLayout jPanel21Layout = new javax.swing.GroupLayout(jPanel21);
        jPanel21.setLayout(jPanel21Layout);
        jPanel21Layout.setHorizontalGroup(
            jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel21Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btSelectObjective)
                .addGap(18, 18, 18)
                .addComponent(btGenerateChart)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel21Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btGenerateChart, btSelectObjective});

        jPanel21Layout.setVerticalGroup(
            jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel21Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btSelectObjective)
                    .addComponent(btGenerateChart))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel22.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Euclidean Distance", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 14))); // NOI18N

        btEuclidianDistance.setText("Number Of Soluction Per Eucidean Distance");

        javax.swing.GroupLayout jPanel22Layout = new javax.swing.GroupLayout(jPanel22);
        jPanel22.setLayout(jPanel22Layout);
        jPanel22Layout.setHorizontalGroup(
            jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel22Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btEuclidianDistance, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel22Layout.setVerticalGroup(
            jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel22Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btEuclidianDistance)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel23.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Hypervolume", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 14))); // NOI18N

        btHypervolume.setText("Hypervolume");

        ckUseNormalization.setText("Use Normalization");

        javax.swing.GroupLayout jPanel23Layout = new javax.swing.GroupLayout(jPanel23);
        jPanel23.setLayout(jPanel23Layout);
        jPanel23Layout.setHorizontalGroup(
            jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel23Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btHypervolume, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(ckUseNormalization)
                .addContainerGap(533, Short.MAX_VALUE))
        );
        jPanel23Layout.setVerticalGroup(
            jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel23Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btHypervolume)
                    .addComponent(ckUseNormalization))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel19Layout = new javax.swing.GroupLayout(jPanel19);
        jPanel19.setLayout(jPanel19Layout);
        jPanel19Layout.setHorizontalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel19Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel20, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel22, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel23, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel19Layout.setVerticalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel19Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(160, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Experiments", jPanel19);

        jPanel25.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Status", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 14))); // NOI18N

        taLogStatus.setColumns(114);
        taLogStatus.setRows(25);
        jScrollPane2.setViewportView(taLogStatus);

        javax.swing.GroupLayout jPanel25Layout = new javax.swing.GroupLayout(jPanel25);
        jPanel25.setLayout(jPanel25Layout);
        jPanel25Layout.setHorizontalGroup(
            jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel25Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2)
                .addContainerGap())
        );
        jPanel25Layout.setVerticalGroup(
            jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel25Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel24Layout = new javax.swing.GroupLayout(jPanel24);
        jPanel24.setLayout(jPanel24Layout);
        jPanel24Layout.setHorizontalGroup(
            jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel24Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel25, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(22, 22, 22))
        );
        jPanel24Layout.setVerticalGroup(
            jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel24Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel25, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(56, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Logs", jPanel24);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void ckSmartyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ckSmartyActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ckSmartyActionPerformed

    private void btTemplateDirectoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btTemplateDirectoryActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btTemplateDirectoryActionPerformed

    private void ckFeatureInterlacingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ckFeatureInterlacingActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ckFeatureInterlacingActionPerformed

    private void ckMutationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ckMutationActionPerformed
        enableMutationOption();
    }//GEN-LAST:event_ckMutationActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btBrowserFeatureProfile;
    private javax.swing.JButton btBrowserPatternProfile;
    private javax.swing.JButton btBrowserRelationshipProfile;
    private javax.swing.JButton btBrowserSmartProfile;
    private javax.swing.JButton btClean;
    private javax.swing.JButton btConfirme;
    private javax.swing.JButton btEuclidianDistance;
    private javax.swing.JButton btGenerateChart;
    private javax.swing.JButton btHypervolume;
    private javax.swing.JButton btManipulationDirectory;
    private javax.swing.JButton btNonDomitedSolutions;
    private javax.swing.JButton btRun;
    private javax.swing.JButton btSelectObjective;
    private javax.swing.JButton btSelectOutputDirectory;
    private javax.swing.JButton btSelectPath;
    private javax.swing.JButton btTemplateDirectory;
    private javax.swing.JButton btViewApplicationConfig;
    private javax.swing.JComboBox<String> cbAlgothm;
    private javax.swing.JComboBox<String> cbObjectiveSoluction;
    private javax.swing.JCheckBox ckAddClassMutation;
    private javax.swing.JCheckBox ckAddManagerClassMutation;
    private javax.swing.JCheckBox ckBridge;
    private javax.swing.JCheckBox ckClassCoupling;
    private javax.swing.JCheckBox ckCohesion;
    private javax.swing.JCheckBox ckComponentCoupling;
    private javax.swing.JCheckBox ckConventional;
    private javax.swing.JCheckBox ckCrossover;
    private javax.swing.JCheckBox ckElegance;
    private javax.swing.JCheckBox ckFeature;
    private javax.swing.JCheckBox ckFeatureDifusion;
    private javax.swing.JCheckBox ckFeatureDriven;
    private javax.swing.JCheckBox ckFeatureDrivenMutation;
    private javax.swing.JCheckBox ckFeatureInterlacing;
    private javax.swing.JCheckBox ckMediator;
    private javax.swing.JCheckBox ckMoveAttributeMutation;
    private javax.swing.JCheckBox ckMoveMethodMutation;
    private javax.swing.JCheckBox ckMoveOperationMutation;
    private javax.swing.JCheckBox ckMutation;
    private javax.swing.JCheckBox ckPLAExtensibility;
    private javax.swing.JCheckBox ckPatterns;
    private javax.swing.JCheckBox ckRelationship;
    private javax.swing.JCheckBox ckSize;
    private javax.swing.JCheckBox ckSmarty;
    private javax.swing.JCheckBox ckStrategy;
    private javax.swing.JCheckBox ckUseNormalization;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JPanel jPanel23;
    private javax.swing.JPanel jPanel24;
    private javax.swing.JPanel jPanel25;
    private javax.swing.JPanel jPanel26;
    private javax.swing.JPanel jPanel27;
    private javax.swing.JPanel jPanel28;
    private javax.swing.JPanel jPanel29;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JSlider jsCrossover;
    private javax.swing.JSlider jsMutation;
    private javax.swing.JPanel panelCkProfiles;
    private javax.swing.JPanel panelMutations;
    private javax.swing.JPanel panelOperatorOption;
    private javax.swing.JPanel panelOperators;
    private javax.swing.JPanel panelScopeSelection;
    private javax.swing.JRadioButton rbElements;
    private javax.swing.JRadioButton rbRandom;
    private javax.swing.JTextArea taLogStatus;
    private javax.swing.JTable tbExecutions;
    private javax.swing.JTable tbExperiments;
    private javax.swing.JTable tbMetrics;
    private javax.swing.JTable tbObjectiveSolution;
    private javax.swing.JTable tbRuns;
    private javax.swing.JTextField tfArchiveSize;
    private javax.swing.JTextField tfDescription;
    private javax.swing.JTextField tfFeatureProfile;
    private javax.swing.JTextField tfInputArchitecturePath;
    private javax.swing.JTextField tfManipulationDirectory;
    private javax.swing.JTextField tfMaxEvaluations;
    private javax.swing.JTextField tfNumberRuns;
    private javax.swing.JTextField tfOutputDirectory;
    private javax.swing.JTextField tfPatternProfile;
    private javax.swing.JTextField tfPopulationSize;
    private javax.swing.JTextField tfRelationshipProfile;
    private javax.swing.JTextField tfSmartProfile;
    private javax.swing.JTextField tfTemplateDiretory;
    // End of variables declaration//GEN-END:variables

    private void enableMutationOption() {
        jsMutation.setEnabled(ckMutation.isSelected());
        enableAllChecks(panelMutations, ckMutation.isSelected());
    }

    private void resultExecutionsLoad() {
        List<Experiment> experiments =  experimentDAO.findAllOrdened();
        this.tmExecExperiments.setLista(experiments);
        tbExecutions.setModel(tmExecExperiments);
        tbExecutions.updateUI();
        this.tmExperiments.setLista(experiments);
        tbExperiments.setModel(tmExperiments);
        tbExperiments.updateUI();
    }
}
