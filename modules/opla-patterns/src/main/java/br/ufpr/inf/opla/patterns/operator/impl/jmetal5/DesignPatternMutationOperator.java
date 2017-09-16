package br.ufpr.inf.opla.patterns.operator.impl.jmetal5;


import arquitetura.representation.Architecture;
import arquitetura.representation.Interface;
import arquitetura.representation.Patterns;
import br.ufpr.inf.opla.patterns.designpatterns.DesignPattern;
import br.ufpr.inf.opla.patterns.models.Scope;
import br.ufpr.inf.opla.patterns.repositories.ArchitectureRepository;
import br.ufpr.inf.opla.patterns.solution.ArchitectureSolution;
import br.ufpr.inf.opla.patterns.strategies.designpatternselection.DesignPatternSelectionStrategy;
import br.ufpr.inf.opla.patterns.strategies.designpatternselection.defaultstrategy.RandomDesignPatternSelection;
import br.ufpr.inf.opla.patterns.strategies.scopeselection.ScopeSelectionStrategy;
import br.ufpr.inf.opla.patterns.strategies.scopeselection.defaultstrategy.RandomScopeSelection;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.pseudorandom.PseudoRandomGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementaçao de {@link br.ufpr.inf.opla.patterns.operator.impl.DesignPatternMutationOperator} para o jmetal5
 */
public class DesignPatternMutationOperator implements MutationOperator<ArchitectureSolution> {

    private static final Logger LOGGER = LogManager.getLogger(DesignPatternMutationOperator.class);
    private final double probability;
    private final ScopeSelectionStrategy scopeSelectionStrategy;
    private final DesignPatternSelectionStrategy designPatternSelectionStrategy;

    public DesignPatternMutationOperator(double probability, DesignPatternSelectionStrategy designPatternSelectionStrategy) {
        this(probability, new RandomScopeSelection(), designPatternSelectionStrategy);
    }

    public DesignPatternMutationOperator(double probability, ScopeSelectionStrategy scopeSelectionStrategy) {
        this(probability, scopeSelectionStrategy, new RandomDesignPatternSelection());
    }

    public DesignPatternMutationOperator(double probability) {
        this(probability, new RandomScopeSelection(), new RandomDesignPatternSelection());
    }

    public DesignPatternMutationOperator(double probability, ScopeSelectionStrategy scopeSelectionStrategy, DesignPatternSelectionStrategy designPatternSelectionStrategy) {
        this.probability = probability;
        this.scopeSelectionStrategy = scopeSelectionStrategy;
        this.designPatternSelectionStrategy = designPatternSelectionStrategy;
    }

    private PseudoRandomGenerator getRng() {
        return JMetalRandom.getInstance().getRandomGenerator();
    }

    @Override
    public ArchitectureSolution execute(ArchitectureSolution architectureSolution) {
        throw new UnsupportedOperationException("DesignPatternMutationOperator não funciona multithreaded.");

        /*

        if(getRng().nextDouble() > probability) {
            return architectureSolution.copy();
        }

        Architecture backuparch = architectureSolution.getArchitecture().deepClone();
        Architecture arch =
                this.mutateArchitecture(architectureSolution.getArchitecture(), scopeSelectionStrategy, designPatternSelectionStrategy);

        if(!this.isValidSolution(arch)) {
            LOGGER.info("Invalid Solution. Reverting Modifications.");
            //OPLAProblem.discardedSolutions.set(OPLAProblem.discardedSolutions.get() + 1);
            architectureSolution.setArchitecture(backuparch);
        }


        return architectureSolution.copy();*/

    }

    private boolean isValidSolution(Architecture arch) {
        List<Interface> allInterfaces = new ArrayList<>(arch.getAllInterfaces());
        return allInterfaces.isEmpty() ||
                allInterfaces.stream()
                        .noneMatch(itf ->
                                (itf.getImplementors().isEmpty()) &&
                                        (itf.getDependents().isEmpty()) &&
                                        (!itf.getOperations().isEmpty()));
    }

    private Architecture mutateArchitecture(Architecture arch, ScopeSelectionStrategy scopeSelectionStrategy, DesignPatternSelectionStrategy designPatternSelectionStrategy) {
        ArchitectureRepository.setCurrentArchitecture(arch);

        DesignPattern designPattern = designPatternSelectionStrategy.selectDesignPattern();
        Scope scope = scopeSelectionStrategy.selectScope(arch, Patterns.valueOf(designPattern.getName().toUpperCase()));
        if (designPattern.randomlyVerifyAsPSOrPSPLA(scope)) {
            if (designPattern.apply(scope)) {
                LOGGER.info("Design Pattern " + designPattern.getName() + " applied to scope " + scope.getElements().toString() + " successfully!");

            }

        }
        return arch;
    }
}
