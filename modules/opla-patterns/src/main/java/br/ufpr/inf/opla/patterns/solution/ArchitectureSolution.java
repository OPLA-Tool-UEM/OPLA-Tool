package br.ufpr.inf.opla.patterns.solution;


import arquitetura.representation.Architecture;
import br.ufpr.inf.opla.patterns.problem.multiobjective.OPLAProblem;
import org.uma.jmetal.solution.impl.AbstractGenericSolution;

/**
 * Implementação no jmetal5 da ArchitectureSolutionType
 *
 * @see jmetal4.encodings.solutionType.ArchitectureSolutionType
 */
public class ArchitectureSolution extends AbstractGenericSolution<Architecture, OPLAProblem> {


    private Architecture architecture_;

    public ArchitectureSolution(OPLAProblem problem) {
        this(problem, problem.getArchitecture());
    }

    public ArchitectureSolution(OPLAProblem problem, Architecture copyArch) {
        super(problem);
        this.architecture_ = copyArch.deepClone();
        this.setVariableValue(0, this.architecture_);
    }

    public Architecture getArchitecture() {
        return architecture_;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ArchitectureSolution)) {
            return false;
        }

        ArchitectureSolution other = (ArchitectureSolution) o;
        return this.getArchitecture().hashCode() == other.getArchitecture().hashCode();
    }

    @Override
    public String getVariableValueString(int index) {
        return getVariableValue(index).toString();
    }

    @Override
    public ArchitectureSolution copy() {
        return new ArchitectureSolution(this.problem, this.architecture_);
    }

}