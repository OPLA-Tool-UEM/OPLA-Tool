package jmetal4.operators.crossover;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import arquitetura.representation.Architecture;
import arquitetura.representation.Class;
import arquitetura.representation.Interface;
import jmetal4.core.Solution;
import jmetal4.experiments.ExperimentCommomConfigs;

/**
 * 
 * @author Fernando
 *
 */
public class PLAComplementaryCrossover extends Crossover {

	private static final Logger LOG = Logger.getLogger(PLAComplementaryCrossover.class);

	private static final long serialVersionUID = 1L;

	private int numberOfObjetive;

	public PLAComplementaryCrossover(Map<String, Object> parameters) {
		super(parameters);
		numberOfObjetive = (int) getParameter("numberOfObjectives");
	}

	public PLAComplementaryCrossover(Map<String, Object> parameters, ExperimentCommomConfigs configs) {
		this(parameters);
	}

	@Override
	public Object execute(Object object) throws Exception {
		Solution[] parents = (Solution[]) object;
		Solution father = parents[0];
		Solution mother = parents[1];

		List<Map<Integer, List<Solution>>> bests = new ArrayList<>();

		LOG.info("Quantidade de funções objetivo selecionadas " + numberOfObjetive);
		selectByFitness(father, mother, bests);

		Random random = new Random();
		if (numberOfObjetive > 2 && !bests.isEmpty()) {
			int index = numberOfObjetive - 1;
			Map<Integer, List<Solution>> solutionOne = bests.get(random.nextInt(index));
			Map<Integer, List<Solution>> solutionTwo = bests.get(random.nextInt(index));
			father = selectRandom(random, solutionOne);
			mother = selectRandom(random, solutionTwo);
		} else {
			father = selectRandom(random, bests.get(0));
			mother = selectRandom(random, bests.get(1));
		}

		Architecture architectureFather = (Architecture) father.getDecisionVariables()[0];
		Architecture architectureMother = (Architecture) mother.getDecisionVariables()[0];

		Architecture offSpring1 = architectureFather.deepClone();
		Architecture offSpring2 = architectureMother.deepClone();

		Set<Class> fatherClasses = architectureFather.getAllClasses();
		Set<Interface> fatherInterfaces = architectureFather.getAllInterfaces();
		LOG.info("Pai: Clases: " + fatherClasses.size() + ", Interfaces: " + fatherInterfaces.size());

		Set<Class> motherClasses = architectureMother.getAllClasses();
		Set<Interface> motherInterfaces = architectureMother.getAllInterfaces();
		LOG.info("Mãe: Clases: " + motherClasses.size() + ", Interfaces: " + motherInterfaces.size());

		Random randomCpFather = new Random();
		int cpClassesFather = randomCpFather.nextInt(fatherClasses.size() - 1);
		int cpInterfacesFather = randomCpFather.nextInt(fatherInterfaces.size() - 1);

		Random randomCpMother = new Random();
		int cpClassesMother = randomCpMother.nextInt(motherClasses.size() - 1);
		int cpInterfacesMother = randomCpMother.nextInt(motherInterfaces.size() - 1);

		Set<Class> diffClassesFather = new HashSet<>(fatherClasses);
		Set<Interface> diffInterafacesFather = new HashSet<>(fatherInterfaces);
		diffClassesFather.removeAll(motherClasses);
		diffInterafacesFather.removeAll(motherInterfaces);
		LOG.info("Elementos existentes apenas no pai: Clases: " + diffClassesFather.size() + ", Interfaces: " + diffInterafacesFather.size());

		Set<Class> diffClassesMother = new HashSet<>(motherClasses);
		Set<Interface> diffInterafacesMother = new HashSet<>(motherInterfaces);
		diffClassesMother.removeAll(fatherClasses);
		diffInterafacesMother.removeAll(fatherInterfaces);
		LOG.info("Elementos existentes apenas mãe: Clases: " + diffClassesMother.size() + ", Interfaces: " + diffClassesMother.size());

		Set<Class> offSpringClassesFather = Stream.of(fatherClasses).limit(cpClassesFather == 0 ? 1 : cpClassesFather).map(HashSet::new).findFirst().get();
		Set<Interface> offSpringInterfacesFather = Stream.of(fatherInterfaces).limit(cpInterfacesFather == 0 ? 1 : cpInterfacesFather).map(HashSet::new).findFirst().get();

		Set<Class> offSpringClassesMother = Stream.of(motherClasses).limit(cpClassesMother == 0 ? 1 : cpClassesMother).map(HashSet::new).findFirst().get();
		Set<Interface> offSpringInterfacesMother = Stream.of(motherInterfaces).limit(cpInterfacesMother == 0 ? 1 : cpInterfacesMother).map(HashSet::new).findFirst().get();

		offSpringClassesFather.addAll(diffClassesFather);
		offSpringClassesFather.addAll(motherClasses);

		offSpringClassesMother.addAll(diffClassesMother);
		offSpringClassesMother.addAll(fatherClasses);

		offSpringInterfacesFather.addAll(diffInterafacesFather);
		offSpringInterfacesFather.addAll(motherInterfaces);

		offSpringInterfacesMother.addAll(diffInterafacesMother);
		offSpringInterfacesMother.addAll(fatherInterfaces);

		offSpring1.addAllClasses(offSpringClassesFather);
		offSpring1.addAllInterfaces(offSpringInterfacesFather);
		LOG.info("Descendete PAI: Clases: " + offSpring1.getAllClasses().size() + ", Interfaces: " + offSpring1.getAllInterfaces().size());

		offSpring2.addAllClasses(offSpringClassesMother);
		offSpring2.addAllInterfaces(offSpringInterfacesMother);
		LOG.info("Descendente Mãe: Clases: " + offSpring2.getAllClasses().size() + ", Interfaces: " + offSpring2.getAllInterfaces().size());

		father.getDecisionVariables()[0] = offSpring1;
		mother.getDecisionVariables()[0] = offSpring2;

		parents[0] = father;
		parents[1] = mother;
		return parents;
	}

	private void selectByFitness(Solution father, Solution mother, List<Map<Integer, List<Solution>>> bests) {
		for (int i = 0; i < numberOfObjetive; i++) {
			Map<Integer, List<Solution>> map = new HashMap<>();
			double fitnessFather = father.getObjective(i);
			double fitnessMother = mother.getObjective(i);
			if (fitnessFather < fitnessMother) {
				LOG.info("Pai com melhor fitnes");
				map.put(i, Arrays.asList(father));
			} else if (fitnessFather > fitnessMother) {
				LOG.info("Mãe com melhor fitnes");
				map.put(i, Arrays.asList(mother));
			} else {
				LOG.info("Valores de fitness iguais");
				map.put(i, Arrays.asList(father, mother));
			}
			bests.add(map);
		}
		
	}

	private Solution selectRandom(Random random, Map<Integer, List<Solution>> solutions) {
		return solutions.entrySet().stream()
				.map(entry -> entry.getValue().get(random.nextInt(solutions.entrySet().size()))).findFirst().get();
	}

	public void doCrossover(Solution father, Solution mother) {

	}

}
