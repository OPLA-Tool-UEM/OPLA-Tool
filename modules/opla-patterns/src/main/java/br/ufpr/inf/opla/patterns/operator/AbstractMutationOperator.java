package br.ufpr.inf.opla.patterns.operator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import arquitetura.exceptions.ClassNotFound;
import arquitetura.exceptions.ConcernNotFoundException;
import arquitetura.exceptions.NotFoundException;
import arquitetura.exceptions.PackageNotFound;
import arquitetura.representation.Architecture;
import arquitetura.representation.Interface;
import br.ufpr.inf.opla.patterns.operator.impl.DesignPatternsAndPLAMutationOperator;
import jmetal.core.Solution;
import jmetal.operators.mutation.Mutation;
import jmetal.util.Configuration;
import jmetal.util.JMException;

public abstract class AbstractMutationOperator extends Mutation {

	private static final long serialVersionUID = 1L;

	public static final Logger LOGGER = LogManager.getLogger(DesignPatternsAndPLAMutationOperator.class);

    public AbstractMutationOperator(Map<String, Object> parameters) {
        super(parameters);
    }

    @Override
    public Object execute(Object o) throws JMException, CloneNotSupportedException, ClassNotFound, PackageNotFound, NotFoundException, ConcernNotFoundException {
        Solution solution = (Solution) o;
        Double probability = (Double) getParameter("probability");

        if (probability == null) {
            Configuration.logger_.severe("FeatureMutation.execute: probability not specified");
            java.lang.Class<String> cls = java.lang.String.class;
            String name = cls.getName();
            throw new JMException("Exception in " + name + ".execute()");
        }

        try {
            hookMutation(solution, probability);
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(AbstractMutationOperator.class.getName()).log(Level.SEVERE, null, ex);
        }

        return solution;
    }

    protected abstract boolean hookMutation(Solution solution, Double probability) throws Exception;

    protected boolean isValidSolution(Architecture solution) {
        boolean isValid = true;
        List<Interface> allInterfaces = new ArrayList<>(solution.getAllInterfaces());
        if (!allInterfaces.isEmpty()) {
            for (Interface itf : allInterfaces) {
                if ((itf.getImplementors().isEmpty()) && (itf.getDependents().isEmpty()) && (!itf.getOperations().isEmpty())) {
                    return false;
                }
            }
        }
        return isValid;
    }

}
