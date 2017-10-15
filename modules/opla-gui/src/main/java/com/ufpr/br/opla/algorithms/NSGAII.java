/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ufpr.br.opla.algorithms;

import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.ufpr.br.opla.configuration.UserHome;
import com.ufpr.br.opla.configuration.VolatileConfs;
import com.ufpr.br.opla.utils.MutationOperatorsSelected;

import arquitetura.io.ReaderConfig;
import br.ufpr.dinf.gres.loglog.Logger;
import jmetal4.experiments.FeatureMutationOperators;
import jmetal4.experiments.NSGAIIConfig;
import jmetal4.experiments.NSGAII_OPLA_FeatMutInitializer;
import jmetal4.experiments.OPLAConfigs;

/**
 * @author elf
 */
public class NSGAII {
	
	private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(NSGAII.class);

    public void execute(JComboBox comboAlgorithms, JCheckBox checkMutation, JTextField fieldMutationProb,
                        JTextArea fieldArchitectureInput, JTextField fieldNumberOfRuns, JTextField fieldPopulationSize,
                        JTextField fieldMaxEvaluations, JCheckBox checkCrossover, JTextField fieldCrossoverProbability,
                        String executionDescription) {
        try {

        	LOGGER.info("set configuration path");
            ReaderConfig.setPathToConfigurationFile(UserHome.getPathToConfigFile());
            ReaderConfig.load();

            LOGGER.info("Create NSGA Config");
            NSGAIIConfig configs = new NSGAIIConfig();

            configs.setLogger(Logger.getLogger());
            configs.activeLogs();
            configs.setDescription(executionDescription);

            // Se mutação estiver marcada, pega os operadores selecionados ,e seta a probabilidade de mutacao
            if (checkMutation.isSelected()) {
            	LOGGER.info("Configure Mutation Operator");
                List<String> mutationsOperators = MutationOperatorsSelected.getSelectedMutationOperators();
                configs.setMutationOperators(mutationsOperators);
                configs.setMutationProbability(Double.parseDouble(fieldMutationProb.getText()));
            }

            configs.setPlas(fieldArchitectureInput.getText());
            configs.setNumberOfRuns(Integer.parseInt(fieldNumberOfRuns.getText()));
            configs.setPopulationSize(Integer.parseInt(fieldPopulationSize.getText()));
            configs.setMaxEvaluations(Integer.parseInt(fieldMaxEvaluations.getText()));

            // Se crossover estiver marcado, configura probabilidade
            // Caso contrario desativa
            if (checkCrossover.isSelected()) {
            	LOGGER.info("Configure Crossover Probability");
                configs.setCrossoverProbability(Double.parseDouble(fieldCrossoverProbability.getText()));
            } else {
                configs.disableCrossover();
            }

            // OPA-Patterns Configurations
            if (MutationOperatorsSelected.getSelectedMutationOperators() .contains(FeatureMutationOperators.DESIGN_PATTERNS.getOperatorName())) {
                // joao
            	LOGGER.info("Instanciando o campo do Patterns - oplatool classe nsgaii");
                String[] array = new String[MutationOperatorsSelected.getSelectedPatternsToApply().size()];
                configs.setPatterns(MutationOperatorsSelected.getSelectedPatternsToApply().toArray(array));
                configs.setDesignPatternStrategy(VolatileConfs.getScopePatterns());

            }

            List<String> operadores = configs.getMutationOperators();

            for (int i = 0; i < operadores.size(); i++) {
                if (operadores.get(i) == "DesignPatterns") {
                    operadores.remove(i);
                }
            }
            configs.setMutationOperators(operadores);
            // operadores convencionais ok
            // operadores padroes ok
            // String[] padroes = configs.getPatterns();

            // Configura onde o db esta localizado
            configs.setPathToDb(UserHome.getPathToDb());

            // Instancia a classe de configuracao da OPLA.java
            LOGGER.info("Create OPLA Config");
            OPLAConfigs oplaConfig = new OPLAConfigs();

            // Funcoes Objetivo
            oplaConfig.setSelectedObjectiveFunctions(VolatileConfs.getObjectiveFunctionSelected());

            // Add as confs de OPLA na classe de configuracoes gerais.
            configs.setOplaConfigs(oplaConfig);

            // Utiliza a classe Initializer do NSGAII passando as configs.
            NSGAII_OPLA_FeatMutInitializer nsgaii = new NSGAII_OPLA_FeatMutInitializer(configs);

            // Executa
            LOGGER.info("Execução NSGAII");
            nsgaii.run();
            LOGGER.info("Fim Execução NSGAII");

        } catch (Exception e) {
        	LOGGER.error(e);
            throw new RuntimeException(e.getMessage());
        }

    }
}