package br.ufpr.inf.opla.patterns.operator.impl.jmetal5;

import arquitetura.exceptions.ConcernNotFoundException;
import arquitetura.helpers.UtilResources;
import arquitetura.representation.*;
import arquitetura.representation.Class;
import arquitetura.representation.Package;
import arquitetura.representation.relationship.GeneralizationRelationship;
import br.ufpr.inf.opla.patterns.solution.ArchitectureSolution;
import jmetal4.operators.crossover.CrossoverRelationship;
import jmetal4.operators.crossover.CrossoverUtils;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.pseudorandom.PseudoRandomGenerator;

import java.util.*;
import java.util.stream.Stream;

/**
 * Implementação no jmetal5 do algoritmo de crossover de PLA
 * <p>
 * Este arquivo é uma tentativa de copiar 1-a-1 o código do PLACrossover original
 *
 * @see jmetal4.operators.crossover.PLACrossover2
 */
public class PLACrossover implements CrossoverOperator<ArchitectureSolution> {

    public double crossoverProbability;

    private CrossoverUtils crossoverUtils;

    public PLACrossover(double probability) {
        crossoverProbability = probability;
        crossoverUtils = new CrossoverUtils();
    }

    static private Stream<GeneralizationRelationship> getClassGeneralizationRelationshipStream(Class cls) {
        return cls.getRelationships().stream().filter(GeneralizationRelationship.class::isInstance)
                .map(rlts -> ((GeneralizationRelationship) rlts));
    }

    /**
     * Busca a Classe parente de cls
     *
     * @param cls: Classe de quem será buscado o parente
     * @return o parente de cls em um Optional, ou Optional.empty() se cls nao possuir relações de generalização
     */
    private static Optional<Class> getParent(Class cls) {
        return getClassGeneralizationRelationshipStream(cls)
                .filter(genRelationship -> genRelationship.getChild().equals(cls))
                .findFirst().map(genRelationship -> (Class) genRelationship.getParent());
    }

    /**
     * Busca a Generalização da classe cls
     *
     * @param cls: Classe de quem será buscado a generalização
     * @return a generalização de cls em um Optional, ou Optional.empty() se cls não possuir relações de generalização
     */
    private static Optional<GeneralizationRelationship> getGeneralizationForClass(Element cls) {
        return getClassGeneralizationRelationshipStream((Class) cls)
                .filter(genRelationship -> genRelationship.getParent().equals(cls))
                .findFirst();
    }

    private PseudoRandomGenerator getRng() {
        return JMetalRandom.getInstance().getRandomGenerator();
    }

    private <T> T randomObjectFromCollection(Collection<T> collection) {
        //avoid creating a list if unnecessary
        assert (!collection.isEmpty());

        if (collection.size() == 1) {
            return collection.iterator().next();
        }

        List<T> tmpList;
        //avoid creating a NEW list if unnecessary
        if (collection instanceof List) {
            tmpList = (List<T>) collection;
        } else {
            tmpList = new ArrayList<>(collection);
        }

        return tmpList.get(getRng().nextInt(0, tmpList.size() - 1));
    }

    private void updateVariabilitiesOffspring(Architecture offspring) {
        offspring.getAllVariabilities().stream().map(Variability::getVariationPoint)
                .filter(Objects::nonNull).forEach(variationPoint -> {
            Element elementVP = variationPoint.getVariationPointElement();
            Element VP = offspring.findElementByName(elementVP.getName());
            if (VP != null) {
                if (!VP.equals(elementVP)) {
                    variationPoint.replaceVariationPointElement(offspring.findElementByName(elementVP.getName(), "class"));
                }
            }
        });
    }

    private void addElementsToOffspring(Concern feature, Architecture offspring, Architecture parent) {
        for (Package pkg : parent.getAllPackages()) {
            addOrCreatePackageIntoOffspring(feature, offspring, parent, pkg);
        }
        CrossoverRelationship.cleanRelationships();
    }

    /**
     * @see jmetal4.operators.crossover.PLACrossover2#addOrCreatePackageIntoOffspring
     */
    private void addOrCreatePackageIntoOffspring(Concern feature, Architecture offspring, Architecture parent, Package parentPackage) {
        // Caso parentPackage cuide de somente um feature, tenta localizar um pacote em offspring
        // Se não encontrá-lo, cria-o

        if (parentPackage.getOwnConcerns().size() == 1 && parentPackage.containsConcern(feature)) {
            Package offspringPackage = offspring.findPackageByName(parentPackage.getName());
            if (offspringPackage == null) { //não pode encontrar o pacote
                offspringPackage = offspring.createPackage(parentPackage.getName());
            }

            addPackageImplementedInterfacesToOffspring(offspring, parentPackage);
            addPackageRequiredInterfacesToOffspring(offspring, parentPackage);

            addInterfacesToPackageInOffspring(offspring, parentPackage, offspringPackage);

            addClassesToOffspring(parentPackage, offspringPackage, offspring, parent);

        } else {
            addClassesRealizingFeatureToOffspring(feature, parentPackage, offspring, parent);
            addInterfacesRealizingFeatureToOffspring(feature, parentPackage, offspring);
        }
    }

    /**
     * @see jmetal4.operators.crossover.PLACrossover2#addClassesRealizingFeatureToOffspring
     */
    private void addClassesRealizingFeatureToOffspring(Concern feature, Package parentPackage, Architecture offspring, Architecture parent) {
        Package newComponent = offspring.findPackageByName(parentPackage.getName());
        if (newComponent == null) {
            newComponent = offspring.createPackage(parentPackage.getName());
        }

        for (Class classComp : parentPackage.getAllClasses()) {
            if (classComp.getOwnConcerns().size() == 1 && classComp.containsConcern(feature)) {
                if (!classComp.belongsToGeneralization()) {
                    newComponent.addExternalClass(classComp);
                    addElementRelationshipsToArchitecture(classComp, offspring);
                } else {
                    findRootAndMoveToRelevantComponent(parentPackage, newComponent, offspring, parent, classComp);
                }

            }
        }

    }

    /**
     * @see jmetal4.operators.crossover.PLACrossover2#addInterfacesRealizingFeatureToOffspring
     */
    private void addInterfacesRealizingFeatureToOffspring(Concern feature, Package comp, Architecture offspring) {
        Set<Interface> interfaces = comp.getOnlyInterfacesImplementedByPackage();
        for (Interface interfaceComp : interfaces) {
            if (interfaceComp.getOwnConcerns().size() == 1 && interfaceComp.containsConcern(feature)) {
                Package newComp = offspring.findPackageByName(comp.getName());
                if (newComp == null) {
                    newComp = offspring.createPackage(comp.getName());
                    addElementRelationshipsToArchitecture(newComp, offspring);
                }
                addOneInterfaceToOffspring(offspring, interfaceComp);
            } else {
                addOperationsRealizingFeatureToOffspring(feature, interfaceComp, comp, offspring);
            }
        }
    }

    /**
     * @see jmetal4.operators.crossover.PLACrossover2#addOperationsRealizingFeatureToOffspring
     */
    private void addOperationsRealizingFeatureToOffspring(Concern feature, Interface interfaceComp, Package comp, Architecture offspring) {
        Interface targetInterface = offspring.findInterfaceByName(interfaceComp.getName());

        for (Method operation : interfaceComp.getOperations()) {
            if (operation.getOwnConcerns().size() == 1 && operation.containsConcern(feature)) {
                if (targetInterface == null) {
                    Package newComp = offspring.findPackageByName(comp.getName());
                    if (newComp == null) {
                        newComp = offspring.createPackage(comp.getName());
                        addElementRelationshipsToArchitecture(newComp, offspring);
                    }

                    if (interfaceComp.getNamespace().equalsIgnoreCase("model")) {
                        targetInterface = offspring.createInterface(interfaceComp.getName());
                    } else {
                        String pkgName = UtilResources.extractPackageName(interfaceComp.getNamespace());
                        Package pkg = offspring.findPackageByName(pkgName);
                        if (pkg == null) pkg = offspring.createPackage(pkgName);

                        targetInterface = pkg.createInterface(interfaceComp.getName());
                    }

                    try {
                        targetInterface.addConcern(feature.getName());
                    } catch (ConcernNotFoundException e) {
                        e.printStackTrace();
                    }

                    addElementRelationshipsToArchitecture(interfaceComp, offspring);
                }
                targetInterface.addExternalOperation(operation);
            }
        }
    }

    /**
     * @see jmetal4.operators.crossover.PLACrossover2#addInterfacesImplementedByClass
     */
    private void addInterfacesImplementedByClass(Architecture offspring, Class klass) {
        addInterfacesToOffspring(offspring, klass.getImplementedInterfaces());
    }

    /**
     * @see jmetal4.operators.crossover.PLACrossover2#addInterfacesRequiredByClass
     */
    private void addInterfacesRequiredByClass(Architecture offspring, Class klass) {
        addInterfacesToOffspring(offspring, klass.getRequiredInterfaces());
    }

    /**
     * @see jmetal4.operators.crossover.PLACrossover2#addImplementedInterfacesByPackageInOffspring
     */
    private void addPackageImplementedInterfacesToOffspring(Architecture offspring, Package parentPackage) {
        final Set<Interface> interfaces = parentPackage.getOnlyInterfacesImplementedByPackage();
        addInterfacesToOffspring(offspring, interfaces);
    }

    /**
     * @see jmetal4.operators.crossover.PLACrossover2#addRequiredInterfacesByPackageInOffspring
     */
    private void addPackageRequiredInterfacesToOffspring(Architecture offspring, Package parentPackage) {
        final Set<Interface> interfaces = parentPackage.getOnlyInterfacesRequiredByPackage();
        addInterfacesToOffspring(offspring, interfaces);
    }

    /**
     * @see jmetal4.operators.crossover.PLACrossover2#addInterfacesToPackageInOffSpring
     */
    private void addInterfacesToPackageInOffspring(Architecture offspring, Package parentPackage, Package packageInOffspring) {
        parentPackage.getAllInterfaces().forEach(anInterface -> {
            packageInOffspring.addExternalInterface(anInterface);
            addElementRelationshipsToArchitecture(anInterface, offspring);
        });
    }

    /**
     * @see jmetal4.operators.crossover.PLACrossover2#addClassesToOffspring
     */
    private void addClassesToOffspring(Package parentPackage, Package packageInOffspring, Architecture offspring, Architecture parent) {
        parentPackage.getAllClasses().forEach(classComp -> {
            if (!classComp.belongsToGeneralization()) {
                addClassToOffspring(classComp, packageInOffspring, offspring);
            } else {
                findRootAndMoveToRelevantComponent(parentPackage, packageInOffspring, offspring, parent, classComp);
            }
            addInterfacesImplementedByClass(offspring, classComp);
            addInterfacesRequiredByClass(offspring, classComp);
        });
    }

    private void findRootAndMoveToRelevantComponent(Package parentPackage, Package packageInOffspring, Architecture offspring, Architecture parent, Class classComp) {
        //buscar a raíz da classe atual
        Class classRoot = getComponentRoot(classComp);
        Package classCompPackage = parent.findPackageOfClass(classComp);
        Package rootPackage = parent.findPackageOfClass(classRoot);
        if (classCompPackage.equals(rootPackage)) {
            //mesmo componente
            moveHierarchyRootToSameComponent(classRoot, packageInOffspring, parentPackage, offspring, parent);
        } else {
            //componentes diferentes
            packageInOffspring.addExternalClass(classComp);
            moveHierarchyRootToDifferentPackage(classRoot, parentPackage, offspring, parent);
        }
        addElementRelationshipsToArchitecture(classComp, offspring);
    }


    /**
     * @see jmetal4.operators.crossover.PLACrossover2#addClassToOffspring
     */
    private void addClassToOffspring(Class klass, Package targetComponent, Architecture offspring) {
        targetComponent.addExternalClass(klass);
        addElementRelationshipsToArchitecture(klass, offspring);
    }

    private void addInterfacesToOffspring(Architecture offspring, Set<Interface> interfaces) {
        interfaces.forEach(interfaceComp -> addOneInterfaceToOffspring(offspring, interfaceComp));
    }

    private void addOneInterfaceToOffspring(Architecture offspring, Interface interfaceComp) {
        if (interfaceComp.getNamespace().equalsIgnoreCase("model")) {
            offspring.addExternalInterface(interfaceComp);
        } else {
            String packageName = UtilResources.extractPackageName(interfaceComp.getNamespace());

            Package pkg = offspring.findPackageByName(packageName);
            if (pkg == null) {
                pkg = offspring.createPackage(packageName);
            }

            pkg.addExternalInterface(interfaceComp);
        }
        addElementRelationshipsToArchitecture(interfaceComp, offspring);
    }

    private Class getComponentRoot(Class klass) {
        Class root = klass;
        Optional<Class> maybeParent = getParent(root);
        while (maybeParent.isPresent()) {
            root = maybeParent.get();
            maybeParent = getParent(root);
        }
        return root;
    }

    /**
     * Move a hierarquia para um mesmo componente a partir de uma raíz
     * Assume que "root" já tenha sido recebido de {@link PLACrossover#getComponentRoot(Class)}
     *
     * @see jmetal4.operators.crossover.PLACrossover2#moveHierarchyToSameComponent(Class, Package, Package, Architecture, Architecture, Concern)
     */
    private void moveHierarchyRootToSameComponent(Class root, Package targetComp, Package sourceComp, Architecture offspring, Architecture parent) {
        if (sourceComp.getAllClasses().contains(root)) {
            moveChildrenToSameComponent(root, sourceComp, targetComp, offspring, parent);
        }
    }

    /**
     * Move a hierarquia para um mesmo componente a partir de uma raíz
     * Assume que "root" já tenha sido recebido de {@link PLACrossover#getComponentRoot(Class)}
     *
     * @see jmetal4.operators.crossover.PLACrossover2#moveHierarchyToSameComponent(Class, Package, Package, Architecture, Architecture, Concern)
     */
    private void moveHierarchyRootToDifferentPackage(Class root, Package sourceComp, Architecture offspring, Architecture parent) {
        if (sourceComp.getAllClasses().contains(root)) {
            moveChildrenToDifferentComponent(root, offspring, parent);
        }
    }


    /**
     * @see jmetal4.operators.crossover.PLACrossover2#moveChildrenToSameComponent
     */
    private void moveChildrenToSameComponent(Class parent, Package sourceComponent, Package targetComponent, Architecture offspring, Architecture parentArch) {
        // mover cada subclasse
        getElementChildren(parent).forEach(child ->
                moveChildrenToSameComponent((Class) child,
                        sourceComponent, targetComponent, offspring, parentArch));

        // mover a super classe
        if (sourceComponent.getAllClasses().contains(parent)) {
            addClassToOffspring(parent, targetComponent, offspring);
            return;
        }

        try {
            Package component;
            Package toComponent = targetComponent;

            // implementacao original tem um "break" dentro do for sem nenhuma condição
            // que é o equivalente de dar um "findFirst"
            Optional<Package> maybeParentComp = parentArch.getAllPackages().stream().findFirst();
            if (maybeParentComp.isPresent()) {
                Package parentComponent = maybeParentComp.get();

                if (parentComponent.getAllClasses().contains(parent)) {
                    component = parentComponent;

                    if (!component.getName().equals(toComponent.getName())) {
                        toComponent = offspring.findPackageByName(component.getName());
                        if (toComponent == null) {
                            toComponent = offspring.createPackage(component.getName());
                            for (Concern feature : component.getOwnConcerns()) {
                                toComponent.addConcern(feature.getName());
                            }
                        }
                    }
                }
                addClassToOffspring(parent, toComponent, offspring);
            }
        } catch (ConcernNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see jmetal4.operators.crossover.PLACrossover2#moveChildrenToDifferentComponent
     */
    private void moveChildrenToDifferentComponent(Class root, Architecture offspring, Architecture parentArch) {

        String rootPackageName = UtilResources.extractPackageName(root.getNamespace());
        Package rootTargetPackage = offspring.findPackageByName(rootPackageName);
        if (rootTargetPackage == null) {
            rootTargetPackage = offspring.createPackage(rootPackageName);
        }

        addClassToOffspring(root, rootTargetPackage, offspring);

        addElementRelationshipsToArchitecture(parentArch.findPackageByName(rootPackageName), offspring);

        for (Element child : getElementChildren(root)) {
            String packageName = UtilResources.extractPackageName(child.getNamespace());
            Package targetPackage = parentArch.findPackageByName(packageName);
            if (targetPackage != null) {
                moveChildrenToDifferentComponent((Class) child, offspring, parentArch);
            }
        }
    }

    /**
     * @see jmetal4.operators.crossover.PLACrossover2#getChildren(Element)
     */
    private Set<Element> getElementChildren(Element element) {
        return getGeneralizationForClass(element)
                .map(GeneralizationRelationship::getAllChildrenForGeneralClass)
                .orElse(Collections.emptySet());
    }


    /**
     * Verifica que a arquitetura contém designs de PLA válidos, ou seja,
     * se as interfaces da arquitetura possuem relacionamentos com a mesma
     *
     * @param architecture: Arquitetura a ser verificada
     * @return true se a arquitetura for válida
     * @see jmetal4.operators.crossover.PLACrossover2#isValidSolution(Architecture)
     */
    private boolean isArchitectureValid(Architecture architecture) {
        return architecture.getAllInterfaces().stream()
                .anyMatch(interf ->
                        interf.getImplementors().isEmpty()
                                && interf.getDependents().isEmpty()
                                && !interf.getOperations().isEmpty());
    }

    /**
     * @see jmetal4.operators.crossover.PLACrossover2#saveAllRelationshiopForElement
     */
    private void addElementRelationshipsToArchitecture(Element element, Architecture offspring) {
        element.getRelationships().forEach(relationship -> offspring.getRelationshipHolder().addRelationship(relationship));
    }

    /**
     * @see jmetal4.operators.crossover.PLACrossover2#obtainChild
     */
    private void obtainChild(Concern feature, Architecture parent, Architecture offspring) {
        crossoverUtils.removeArchitecturalElementsRealizingFeature(feature, offspring, "allLevels");
        addElementsToOffspring(feature, offspring, parent);
        updateVariabilitiesOffspring(offspring);
    }

    /**
     * @see jmetal4.operators.crossover.PLACrossover2#doCrossover
     */
    private ArchitectureSolution[] doCrossover(ArchitectureSolution parent1, ArchitectureSolution parent2) {
        return crossoverFeatures(crossoverProbability, parent1, parent2);
    }

    /**
     * @see jmetal4.operators.crossover.PLACrossover2#crossoverFeatures
     */
    private ArchitectureSolution[] crossoverFeatures(double probability, ArchitectureSolution parent1, ArchitectureSolution parent2) {

        //STEP 0: Create offspring
        ArchitectureSolution offspring[] = {parent1.copy(), parent2.copy()};

        if (getRng().nextDouble(0.0, 1.0) < probability) {
            //STEP 1: Get Feature Crossover
            Architecture offspringArch = offspring[0].getArchitecture();
            Collection<Concern> concerns = offspringArch.getAllConcerns();
            Concern feature = randomObjectFromCollection(concerns);


            obtainChild(feature, parent2.getArchitecture(), offspringArch);
            if (!isArchitectureValid(offspringArch)) {
                offspring[0] = parent1;
//                OPLAProblem.discardedSolutions.set(OPLAProblem.discardedSolutions.get() + 1);
            }

            Architecture offspringArch2 = offspring[1].getArchitecture();
            obtainChild(feature, parent1.getArchitecture(), offspringArch2);
            if (!isArchitectureValid(offspringArch2)) {
                offspring[1] = parent2;
//                OPLAProblem.discardedSolutions.set(OPLAProblem.discardedSolutions.get() + 1);
            }
        }

        return offspring;

    }

    @Override
    public int getNumberOfParents() {
        return 2;
    }

    @Override
    public List<ArchitectureSolution> execute(List<ArchitectureSolution> architectureSolutions) {
        assert architectureSolutions.size() == getNumberOfParents();

        ArchitectureSolution parent1 = architectureSolutions.get(0);
        ArchitectureSolution parent2 = architectureSolutions.get(1);

        return Arrays.asList(doCrossover(parent1, parent2));
    }
}

