package br.ufpr.inf.opla.patterns.problem.multiobjective;

import arquitetura.builders.ArchitectureBuilder;
import arquitetura.representation.Architecture;
import arquitetura.representation.Class;
import arquitetura.representation.Interface;
import arquitetura.representation.Package;
import arquitetura.representation.relationship.GeneralizationRelationship;
import arquitetura.representation.relationship.Relationship;
import br.ufpr.inf.opla.patterns.solution.ArchitectureSolution;
import jmetal4.core.Solution;
import jmetal4.metrics.MetricsEvaluation;
import org.uma.jmetal.problem.ConstrainedProblem;
import org.uma.jmetal.problem.impl.AbstractGenericProblem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implementação do problema de otimizaçao de PLA no jmetal5
 *
 * @see jmetal4.problems.OPLA
 */
public class OPLAProblem extends AbstractGenericProblem<ArchitectureSolution> implements ConstrainedProblem<ArchitectureSolution> {

    // ThreadLocal pois cada Run do OPLAProblem ocorre em uma thread separada
    public static final ThreadLocal<Integer> contClass = ThreadLocal.withInitial(() -> 0);
    public static final ThreadLocal<Integer> contComponent = ThreadLocal.withInitial(() -> 0);
    public static final ThreadLocal<Integer> contInterface = ThreadLocal.withInitial(() -> 0);
    public static final ThreadLocal<Integer> discardedSolutions = ThreadLocal.withInitial(() -> 0);

    /**
     * Construir o problema a partir das entradas da GUI
     */
    private Architecture architecture_;
    private List<String> selectedMetrics;

    public OPLAProblem(String xmiFilePath, String[] objectiveFunctions) {
        this(xmiFilePath, Arrays.asList(objectiveFunctions));
    }

    public OPLAProblem(String xmiFilePath, List<String> objectiveFunctions) {
        setNumberOfObjectives(objectiveFunctions.size());
        setNumberOfConstraints(0);
        setNumberOfVariables(1);
        setName("OPLA");

        this.architecture_ = new ArchitectureBuilder().create(xmiFilePath);
        this.selectedMetrics = objectiveFunctions;

    }

    public Architecture getBaseArchitecture() {
        return architecture_;
    }

    @Override
    public void evaluate(ArchitectureSolution solution) {

        for (int i = 0, selectedMetricsSize = selectedMetrics.size(); i < selectedMetricsSize; i++) {
            String metric = selectedMetrics.get(i);

            MetricsEvaluation evaluation = new MetricsEvaluation();
            Architecture arch = solution.getArchitecture();

            double result;

            switch (metric) {
                case "elegance":
                    result = evaluation.evaluateElegance(arch);
                    break;
                case "conventional":
                    result = evaluation.evaluateMACFitness(arch);
                    break;
                case "featureDriven":
                    result = evaluation.evaluateMSIFitnessDesignOutset(arch);
                    break;
                case "PLAExtensibility":
                    result = evaluation.evaluatePLAExtensibility(arch);
                    break;
                //implementado por marcelo
                case "acomp":
                    result = evaluation.evaluateACOMP(arch);
                    break;
                case "aclass":
                    result = evaluation.evaluateACLASS(arch);
                    break;
                case "tam":
                    result = evaluation.evaluateTAM(arch);
                    break;
                case "coe":
                    result = evaluation.evaluateCOE(arch);
                    break;
                case "dc":
                    result = evaluation.evaluateDC(arch);
                    break;
                case "ec":
                    result = evaluation.evaluateEC(arch);
                    break;
                default:
                    result = 0.0;
                    break;
            }

            solution.setObjective(i, result);
        }
    }

    @Override
    public ArchitectureSolution createSolution() {
        return new ArchitectureSolution(this);
    }

    /**
     * @see jmetal4.problems.OPLA#evaluateConstraints(Solution)
     */
    @Override
    public void evaluateConstraints(ArchitectureSolution solution) {
        List<Package> allPackages = new ArrayList<>(solution.getArchitecture().getAllPackages());
        for (Package pkg : allPackages) {
            //componentes, classes e interfaces não podem ser vazios

            //classes devem estar envolvidas em pelo menos um relacionamento
            //relacionamentos de generalização devem ser mantidos
            List<Class> pkgClasses = new ArrayList<>(pkg.getAllClasses());
            if (!pkgClasses.isEmpty()) {
                for (Class cls : pkgClasses) {
                    if (cls.getAllAttributes().isEmpty() &&
                            cls.getAllMethods().isEmpty() &&
                            cls.getImplementedInterfaces().isEmpty() &&
                            cls.getRelationships().stream().noneMatch(GeneralizationRelationship.class::isInstance) &&
                            cls.getVariantType() == null) {

                        List<Relationship> tmpRelationshipList = new ArrayList<>(cls.getRelationships());

                        pkg.removeClass(cls);
                        tmpRelationshipList.forEach(solution.getArchitecture()::removeRelationship);
                    }
                }

            }

            //interfaces devem estar envolvidas em pelo menos um relacionamento
            List<Interface> pkgInterfaces = new ArrayList<>(pkg.getAllInterfaces());
            if (!pkgInterfaces.isEmpty()) {
                cleanupInterfaces(solution, pkg, pkgInterfaces);
            }
            //limpar interfaces da arquitetura antes de analisar o pacote em si
            cleanupInterfaces(solution, pkg, new ArrayList<>(solution.getArchitecture().getAllInterfaces()));

            if (pkg.getAllClasses().isEmpty() && pkg.getImplementedInterfaces().isEmpty() && pkg.getAllInterfaces().isEmpty()) {
                //removeComponentRelationships da implementação original removia os relacionamentos
                //do tipo Abstraction e Dependency que tivessem este pacote como Client antes de todos os outros
                //porém não existe um motivo específico para isto atualmente -- talvez seja resquício de uma impl antiga?

                solution.getArchitecture().removePackage(pkg);
            }
        }
    }

    private void cleanupInterfaces(ArchitectureSolution solution, Package pkg, List<Interface> interfaces) {
        for (Interface itf : interfaces) {
            if (itf.getOperations().isEmpty()) {
                if (pkg.getImplementedInterfaces().size() > 1 || pkg.getAllClasses().isEmpty()) {
                    solution.getArchitecture().removeInterface(itf);
                }
            }
        }
    }
}
