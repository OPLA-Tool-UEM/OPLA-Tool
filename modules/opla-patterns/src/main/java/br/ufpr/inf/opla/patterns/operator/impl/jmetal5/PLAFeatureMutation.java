package br.ufpr.inf.opla.patterns.operator.impl.jmetal5;

import arquitetura.representation.*;
import arquitetura.representation.Class;
import arquitetura.representation.Package;
import arquitetura.representation.relationship.AssociationRelationship;
import arquitetura.representation.relationship.GeneralizationRelationship;
import br.ufpr.inf.opla.patterns.solution.ArchitectureSolution;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Implementaçao de {@link jmetal4.operators.mutation.PLAFeatureMutation} para o jmetal5
 */
public class PLAFeatureMutation implements MutationOperator<ArchitectureSolution> {
    private static Logger LOGGER = LogManager.getLogger(PLAFeatureMutation.class.getName());
    private final AtomicInteger contInterfaces = new AtomicInteger(0);
    private final AtomicInteger contComponents = new AtomicInteger(0);
    private final AtomicInteger contClass = new AtomicInteger(0);
    private double probability;

    public PLAFeatureMutation(double probability) {
        this.probability = probability;
    }

    private <T> T randomObjectFromCollection(Collection<T> collection) {
        //avoid creating a list if unecessary
        if (collection.isEmpty()) return null;

        if (collection.size() == 1) {
            return collection.iterator().next();
        }

        List<T> tmpList;
        //avoid creating a NEW list if unecessary
        if (collection instanceof List) {
            tmpList = (List<T>) collection;
        } else {
            tmpList = new ArrayList<>(collection);
        }

        return tmpList.get(JMetalRandom.getInstance().getRandomGenerator().nextInt(0, tmpList.size() - 1));
    }

    private boolean searchForGeneralizations(Class cls) {
        return cls.getGeneralizationRelationship() != null;
    }

    private void createAssociation(Architecture arch, Class targetClass, Class sourceClass) {
        AssociationRelationship associationRelationship = new AssociationRelationship(targetClass, sourceClass);
        arch.addRelationship(associationRelationship);
    }

    // private void moveMethod(Architecture arch, Class targetClass, Class sourceClass, Package targetComp, Package sourceComp)
    private void moveRandomMethodWithoutConcernsToClass(Architecture arch, Class targetClass, Class sourceClass) {
        if (!sourceClass.getAllMethods().isEmpty()) {
            Method method = randomObjectFromCollection(sourceClass.getAllMethods());
            assert method != null;
            if (sourceClass.moveMethodToClass(method, targetClass)) {
                createAssociation(arch, targetClass, sourceClass);
            }
        }
    }

    // private void removeClassesInPatternStructureFromArray(List<Class> ClassesComp)
    private Predicate<Class> patternAppliedClassFilter() {
        return classToFilter -> !classToFilter.getPatternsOperations().hasPatternApplied();
    }

    // private void removeInterfacesInPatternStructureFromArray(List<Interface> InterfacesSourceComp)
    private Predicate<Interface> patternAppliedInterfaceFilter() {
        return (Interface itfToFilter) -> !itfToFilter.getPatternsOperations().hasPatternApplied();
    }

    // private void moveMethodToNewClass(Architecture arch, Class sourceClass, List<Method> MethodsClass, Class newClass)
    private void moveRandomMethodWithConcernsToClass(Architecture arch, Class sourceClass, Class newClass) {
        if (!sourceClass.getAllMethods().isEmpty()) {
            Method targetMethod = randomObjectFromCollection(sourceClass.getAllMethods());
            assert targetMethod != null;
            if (sourceClass.moveMethodToClass(targetMethod, newClass)) {
                newClass.addConcerns(targetMethod.getOwnConcerns());
                createAssociation(arch, newClass, sourceClass);
            }
        }
    }

    // private void moveAttributeToNewClass(Architecture arch, Class sourceClass, List<Attribute> AttributesClass, Class newClass)
    private void moveRandomAttributeWithConcernsToNewClass(Architecture arch, Class sourceClass, Class newClass) {
        if (!sourceClass.getAllAttributes().isEmpty()) {
            Attribute targetAttr = randomObjectFromCollection(sourceClass.getAllAttributes());
            assert targetAttr != null;
            if (sourceClass.moveAttributeToClass(targetAttr, newClass)) {
                newClass.addConcerns(targetAttr.getOwnConcerns());
                createAssociation(arch, newClass, sourceClass);
            }
        }
    }

    private boolean checkSameLayer(Package source, Package target) {
        String sourceName = source.getName();
        String targetName = target.getName();
        return ((sourceName.endsWith("Mgr") && targetName.endsWith("Mgr")) ||
                (sourceName.endsWith("Ctrl") && targetName.endsWith("Ctrl")) ||
                (sourceName.endsWith("GUI") && targetName.endsWith("GUI")));
    }

    // private List<Package> searchComponentsAssignedToConcern(Concern concern, List<Package> allComponents)
    // private Set<Concern> getNumberOfConcernsFor(Package pkg)
    private List<Package> getComponentsAssignedToConcern(Concern concern, List<Package> allComponents) {
        List<Package> componentsAssignedToConcern = new ArrayList<>();
        for (Package pkg : allComponents) {
            Set<Concern> pkgConcernsSet = Stream.concat(pkg.getAllClasses().stream(), pkg.getAllInterfaces().stream())
                    .map(Element::getOwnConcerns).flatMap(Set::stream).collect(Collectors.toSet());
            if (pkgConcernsSet.size() == 1 && pkgConcernsSet.contains(concern)) {
                componentsAssignedToConcern.add(pkg);
            }
        }
        return componentsAssignedToConcern;
    }

    private void modularizeConcernInComponent(Collection<Package> allComponents, Package targetComp, Concern concern, Architecture arch) {
        for (Package c : allComponents) {
            if (!c.equals(targetComp) && checkSameLayer(c, targetComp)) {
                Set<Interface> allInterfaces = new HashSet<>(targetComp.getAllInterfaces());
                allInterfaces.addAll(targetComp.getImplementedInterfaces());

                for (Interface interfaceComp : allInterfaces) {
                    if (interfaceComp.hasOnlyOneConcern(concern)) {
                        moveInterfaceToComponent(interfaceComp, targetComp, c, arch, concern);
                    } else if (!interfaceComp.getPatternsOperations().hasPatternApplied()) {
                        List<Method> operationsInterfaceComp = new ArrayList<>(interfaceComp.getOperations());
                        operationsInterfaceComp.forEach(operation -> {
                            if (operation.hasOnlyOneConcern(concern)) {
                                moveOperationToComponent(operation, interfaceComp, targetComp, c, arch, concern);
                            }
                        });
                    }
                }


                List<Class> allClasses = new ArrayList<>(c.getAllClasses());
                for (Class classComp : allClasses) {//getAllClasses() pode mudar no meio da iteração
                    if (c.getAllClasses().contains(classComp)) {
                        if (classComp.hasOnlyOneConcern(concern)) {
                            GeneralizationRelationship gr = classComp.getGeneralizationRelationship();
                            if (gr == null) {
//                            moveClassToComponent(classComp, targetComp, comp, arch, concern);
                                c.moveClassToPackage(classComp, targetComp);
                            } else {
                                arch.forGeneralization().moveGeneralizationToPackage(gr, targetComp);
//                            moveHierarchyToComponent(classComp, targetComp, comp, arch, concern);
                            }
                        } else if (!searchForGeneralizations(classComp)) {
//                        if (!isVarPointOfConcern(arch, classComp, concern) && !isVariantOfConcern(arch, classComp, concern)) {
                            if (!classComp.isVariabilityPointOfConcern(arch, concern) && !classComp.isVariantOfConcern(arch, concern)) {
                                List<Attribute> attrClassComp = new ArrayList<>(classComp.getAllAttributes());
                                for (Attribute attribute : attrClassComp) {
                                    if (attribute.hasOnlyOneConcern(concern)) {
                                        moveAttributeToComponent(attribute, classComp, targetComp, c, arch, concern);
                                    }
                                }

                                if (!classComp.getPatternsOperations().hasPatternApplied()) {
                                    List<Method> methodsClassComp = new ArrayList<>(classComp.getAllMethods());
                                    for (Method method : methodsClassComp) {
                                        if (method.hasOnlyOneConcern(concern)) {
                                            moveMethodToComponent(method, classComp, targetComp, c, arch, concern);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Deprecated
    private void moveClassToComponent(Class classComp, Package targetComp, Package comp, Architecture arch, Concern concern) {
        comp.moveClassToPackage(classComp, targetComp);
    }

    @Deprecated
    private void moveHierarchyToComponent(Class classComp, Package targetComp, Package comp, Architecture arch, Concern concern) {

    }

    private void moveMethodToComponent(Method method, Class classComp, Package targetComp, Package comp, Architecture arch, Concern concern) {
        Class targetClass = findOrCreateClassWithConcern(targetComp, concern);
        targetClass.moveMethodToClass(method, targetClass);
        createAssociation(arch, targetClass, classComp);
    }

    private void moveAttributeToComponent(Attribute attribute, Class classComp, Package targetComp, Package comp, Architecture arch, Concern concern) {
        Class targetClass = findOrCreateClassWithConcern(targetComp, concern);
        targetClass.moveAttributeToClass(attribute, targetClass);
        createAssociation(arch, targetClass, classComp);
    }

    private void moveOperationToComponent(Method operation, Interface sourceInterface, Package targetComp, Package sourceComp, Architecture arch, Concern concern) {
        Interface targetInterface = targetComp.getInterfaceWithConcern(concern)
                .orElseGet(() -> {
                    Interface newIntf = targetComp.createInterface("Interface" + contInterfaces.getAndIncrement());
                    newIntf.addConcern(concern);
                    return newIntf;
                });

        sourceInterface.moveOperationToInterface(operation, targetInterface);
        addRelationship(sourceInterface, targetComp, sourceComp, arch, concern, targetInterface);
    }

    private void moveInterfaceToComponent(Interface interfaceComp, Package targetComp, Package sourceComp, Architecture arch, Concern concern) {
        if (!sourceComp.moveInterfaceToPackage(interfaceComp, targetComp)) {
            arch.moveElementToPackage(interfaceComp, targetComp);
        }

        // ponto de possível conflito, pois na impl original não usava o método - mas ele é quase uma duplicata
        addRelationship(interfaceComp, targetComp, sourceComp, arch, concern, interfaceComp);
    }

    private Class findOrCreateClassWithConcern(Package targetComp, Concern concern) {
        return targetComp.getAllClasses().stream()
                .filter(concern::isContainedByElement)
                .findAny().orElseGet(() -> {
                    Class newClass = targetComp.createClass("Class" + contClass.getAndIncrement(), false);
                    newClass.addConcern(concern);
                    return newClass;
                });
    }

    private void addRelationship(Interface sourceInterface, Package targetComp, Package sourceComp, Architecture arch, Concern concern, Interface targetInterface) {
        for (Element implementor : sourceInterface.getImplementors()) {
            if (implementor instanceof Package) {
                /*
                Busca uma classe aleatória do pacote que tenha o Concern em questão,
                remove a sourceInterface de sourceComp,
                adiciona a interface targetInterface ao pacote(ou à arquitetura, se for um pacote de "model")
                Se não houver um relacionamento de realização entre targetInterface e a classe selecionada,
                adiciona targetInterface como sendo implementada por esta classe.
                 */
                List<Class> classesWithConcern = targetComp.getAllClasses().stream()
                        .filter(concern::isContainedByElement).collect(Collectors.toList());
                /*
                Se o pacote não tiver nenhuma classe, busca na arquitetura inteira
                 */
                if (classesWithConcern.isEmpty()) {
                    classesWithConcern = arch.getAllClasses().stream()
                            .filter(concern::isContainedByElement).collect(Collectors.toList());
                }

                Class selectedClass = randomObjectFromCollection(classesWithConcern);

                if (selectedClass != null) {
                    arch.removeImplementedInterface(sourceInterface, sourceComp);
                    addExternalInterface(targetComp, arch, targetInterface);
                    addImplementedInterface(targetComp, arch, targetInterface, selectedClass);
                }
            } else if (implementor instanceof Class) {
                /*
                 Recupera quem estava implementando a interface que teve a operacao movida
                 e cria uma realizacao entre a interface que recebeu a operacao (targetInterface)
                 e quem tava implementando a interface que teve a operacao movida (sourceInterface).
                 */
                arch.removeImplementedInterface(sourceInterface, sourceComp);
                addExternalInterface(targetComp, arch, targetInterface);
                addImplementedInterface(targetComp, arch, targetInterface, (Class) implementor);
            }
        }
    }

    private void addImplementedInterface(Package targetComp, Architecture architecture, Interface targetInterface, Class uniqueClass) {
        if (!targetComp.hasRealizationWithSupplier(targetInterface)) {
            architecture.addImplementedInterface(targetInterface, uniqueClass);
        }
    }

    /**
     * adiciona uma interface externa a um pacote;
     * se o pacote for um model, adiciona ela à arquitetura
     */
    private void addExternalInterface(Package targetComp, Architecture architecture, Interface targetInterface) {
        if (targetComp.isModelPackage()) {
            architecture.addExternalInterface(targetInterface);
        } else {
            targetComp.addExternalInterface(targetInterface);
        }
    }

    private boolean canClassBeMutated(Class sourceClass, Architecture architecture) {
        return !searchForGeneralizations(sourceClass) &&
                sourceClass.getAllAttributes().size() > 1 &&
                sourceClass.getAllMethods().size() > 1 &&
                !sourceClass.isVariant(architecture) &&
                !sourceClass.isVariationPoint(architecture) &&
                !sourceClass.isOptional();
    }

    private String getSuffix(Package component) {
        String componentName = component.getName();
        String[] possibleSuffixes = {"Mgr", "Ctrl", "GUI"};
        for (String possibleSuffix : possibleSuffixes) {
            if (componentName.endsWith(possibleSuffix)) {
                return possibleSuffix;
            }
        }
        return "";
    }

    //////////////////////////// MUTATIONS ////////////////////////////////
    public void MoveAttributeMutation(ArchitectureSolution architectureSolution) {
        LOGGER.info("MoveAttributeMutation");
        Architecture arch = architectureSolution.getArchitecture();
        //samecomponent
        Package pkg = randomObjectFromCollection(arch.getAllPackages());
        if (pkg == null) return;
        List<Class> pkgClasses = new ArrayList<>(pkg.getAllClasses());
        if (pkgClasses.size() > 1) {
            Class targetClass = randomObjectFromCollection(pkgClasses);
            Class sourceClass = randomObjectFromCollection(pkgClasses);
            assert (targetClass != null && sourceClass != null);
            if (!targetClass.equals(sourceClass) && canClassBeMutated(sourceClass, arch)) {
                //moveAttribute(arch, targetClass, sourceClass)
                Attribute attrToMove = randomObjectFromCollection(sourceClass.getAllAttributes());
                if (attrToMove != null) {
                    if (sourceClass.moveAttributeToClass(attrToMove, targetClass)) {
                        createAssociation(arch, targetClass, sourceClass);
                    }
                }
            }
        }
    }

    public void MoveMethodMutation(ArchitectureSolution architectureSolution) {
        LOGGER.info("MoveMethodMutation");
        Architecture arch = architectureSolution.getArchitecture();
        Package pkg = randomObjectFromCollection(arch.getAllPackages());
        if (pkg == null) return;

        // ignorar classes que tem um pattern aplicado
        List<Class> pkgClasses = pkg.getAllClasses().stream()
                .filter(patternAppliedClassFilter()).collect(Collectors.toList());
        if (pkgClasses.size() > 1) {
            Class targetClass = randomObjectFromCollection(pkgClasses);
            Class sourceClass = randomObjectFromCollection(pkgClasses);
            assert (targetClass != null && sourceClass != null);
            if (!targetClass.equals(sourceClass) && canClassBeMutated(sourceClass, arch)) {
                //moveMethod(arch, targetClass, sourceClass, sourceComp, sourceComp);
                Method methodToMove = randomObjectFromCollection(sourceClass.getAllMethods());
                if (methodToMove != null) {
                    if (sourceClass.moveMethodToClass(methodToMove, targetClass)) {
                        createAssociation(arch, targetClass, sourceClass);
                    }
                }
            }
        }
    }

    public void MoveOperationMutation(ArchitectureSolution architectureSolution) {
        LOGGER.info("MoveOperationMutation");
        Architecture arch = architectureSolution.getArchitecture();

        Package sourcePkg = randomObjectFromCollection(arch.getAllPackages());
        Package targetPkg = randomObjectFromCollection(arch.getAllPackages());
        if (sourcePkg == null || targetPkg == null) {
            return;
        }

        if (checkSameLayer(sourcePkg, targetPkg)) {
            // não incluir interfaces que tem um pattern aplicado
            List<Interface> interfacesSource = sourcePkg.getAllInterfaces().stream()
                    .filter(patternAppliedInterfaceFilter()).collect(Collectors.toList());
            List<Interface> interfacesTarget = new ArrayList<>(targetPkg.getAllInterfaces());
            if (!interfacesSource.isEmpty() && !interfacesTarget.isEmpty()) {
                Interface sourceIntf = randomObjectFromCollection(interfacesSource);
                Interface targetIntf = randomObjectFromCollection(interfacesTarget);
                assert (sourceIntf != null && targetIntf != null);

                if (targetIntf != sourceIntf) {
                    List<Method> methodsSrcIntf = new ArrayList<>(sourceIntf.getOperations());
                    if (!methodsSrcIntf.isEmpty()) {
                        Method selectedMethod = randomObjectFromCollection(methodsSrcIntf);
                        assert (selectedMethod != null);

                        sourceIntf.moveOperationToInterface(selectedMethod, targetIntf);
                        sourceIntf.getImplementors().forEach(implementor -> arch.addImplementedInterface(targetIntf, implementor));
                    }
                }
            }
        }
    }

    public void AddClassMutation(ArchitectureSolution architectureSolution) {
        LOGGER.info("AddClassMutation");
        Architecture arch = architectureSolution.getArchitecture();
        Package sourcePkg = randomObjectFromCollection(arch.getAllPackages());
        if (sourcePkg == null) return;

        List<Class> classesPkg = sourcePkg.getAllClasses().stream().filter(patternAppliedClassFilter()).collect(Collectors.toList());
        if (!classesPkg.isEmpty()) {
            Class sourceClass = randomObjectFromCollection(classesPkg);
            assert (sourceClass != null);
            if (canClassBeMutated(sourceClass, arch)) {
                // 50% chance para attribute ou method
                boolean doAttr = JMetalRandom.getInstance().nextInt(0, 1) == 0;
                Class newClass = sourcePkg.createClass("Class" + contClass.getAndIncrement(), false);
                if (doAttr) {
                    moveRandomAttributeWithConcernsToNewClass(arch, sourceClass, newClass);
                } else {
                    moveRandomMethodWithConcernsToClass(arch, sourceClass, newClass);
                }
            }
        }
    }

    public void AddManagerClassMutation(ArchitectureSolution architectureSolution) {
        LOGGER.info("AddManagerClassMutation");
        Architecture arch = architectureSolution.getArchitecture();
        Package sourcePkg = randomObjectFromCollection(arch.getAllPackages());
        if (sourcePkg == null) return;
        // não incluir interfaces que tem um pattern aplicado
        List<Interface> interfacesSource = sourcePkg.getAllInterfaces().stream()
                .filter(patternAppliedInterfaceFilter()).collect(Collectors.toList());

        Interface sourceInterface = randomObjectFromCollection(interfacesSource);
        if (sourceInterface != null) {
            List<Method> methodsInterface = new ArrayList<>(sourceInterface.getOperations());
            Method selectedMethod = randomObjectFromCollection(methodsInterface);
            if (selectedMethod != null) {
                Package newPkg = arch.createPackage("Package" + contComponents.getAndIncrement() + getSuffix(sourcePkg));
                Interface newIntf = newPkg.createInterface("Interface" + contInterfaces.getAndIncrement());

                sourceInterface.moveOperationToInterface(selectedMethod, newIntf);
                sourceInterface.getImplementors().forEach(implementor -> arch.addImplementedInterface(newIntf, implementor));
                newIntf.addConcerns(selectedMethod.getOwnConcerns());
            }
        }
    }

    public void FeatureMutation(ArchitectureSolution architectureSolution) {
        LOGGER.info("FeatureMutation");
        Architecture arch = architectureSolution.getArchitecture();
        List<Package> allPackages = new ArrayList<>(arch.getAllPackages());
        Package selectedPkg = randomObjectFromCollection(allPackages);
        if (selectedPkg == null) return;

        List<Concern> selectedPkgConcerns = new ArrayList<>(selectedPkg.getAllConcerns());
        Concern selectedConcern = randomObjectFromCollection(selectedPkgConcerns);
        if (selectedConcern == null) return;

        List<Package> allPackagesAssignedOnlyToConcern = getComponentsAssignedToConcern(selectedConcern, allPackages);
        Package targetPkg = randomObjectFromCollection(allPackagesAssignedOnlyToConcern);
        if (targetPkg == null) {
            targetPkg = arch.createPackage("Package" + contComponents.getAndIncrement() + getSuffix(selectedPkg));
        }

        modularizeConcernInComponent(allPackages, targetPkg, selectedConcern, arch);
    }

    @Override
    public ArchitectureSolution execute(ArchitectureSolution solution) {
        ArchitectureSolution backup = solution.copy();
        if (JMetalRandom.getInstance().nextDouble() > probability) {
            return backup;
        }

        int randomMutation = JMetalRandom.getInstance().nextInt(0, 5);
        switch (randomMutation) {
            case 0:
                FeatureMutation(solution);
                break;
            case 1:
                MoveMethodMutation(solution);
                break;
            case 2:
                MoveAttributeMutation(solution);
                break;
            case 3:
                MoveOperationMutation(solution);
                break;
            case 4:
                AddClassMutation(solution);
                break;
            case 5:
                AddManagerClassMutation(solution);
                break;
        }

        if (!isValidSolution(solution)) {
            return backup;
        }

        return solution;
    }

    private boolean isValidSolution(ArchitectureSolution mutatedSolution) {
        Architecture arch = mutatedSolution.getArchitecture();
        return arch.getAllInterfaces().stream().noneMatch(itf ->
                (itf.getImplementors().isEmpty())
                        && (itf.getDependents().isEmpty())
                        && (!itf.getOperations().isEmpty()));
    }
}



