package br.ufpr.inf.opla.patterns.operator.impl.jmetal5;

import arquitetura.helpers.UtilResources;
import arquitetura.representation.*;
import arquitetura.representation.Class;
import arquitetura.representation.Package;
import arquitetura.representation.relationship.AssociationRelationship;
import arquitetura.representation.relationship.GeneralizationRelationship;
import arquitetura.representation.relationship.RealizationRelationship;
import br.ufpr.inf.opla.patterns.solution.ArchitectureSolution;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.pseudorandom.PseudoRandomGenerator;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Implementaçao de {@link br.ufpr.inf.opla.patterns.operator.impl.PLAMutation} para o jmetal5
 */
public class PLAMutation implements MutationOperator<ArchitectureSolution> {

    private static final Logger LOGGER = LogManager.getLogger(PLAMutation.class);
    private final double probability;

    private final AtomicInteger contInterfaces = new AtomicInteger(0);
    private final AtomicInteger contComponents = new AtomicInteger(0);
    private final AtomicInteger contClass = new AtomicInteger(0);


    public PLAMutation(double probability) {
        this.probability = probability;
    }

    @Override
    public ArchitectureSolution execute(ArchitectureSolution architectureSolution) {
        return doRandomMutation(architectureSolution);
    }

    private ArchitectureSolution doRandomMutation(ArchitectureSolution architectureSolution) {
        String scope = "sameComponent"; //"allComponents" usar "sameComponent" para que a troca seja realizada dentro do mesmo componente da arquitetura
        String scopeLevels = "allLevels"; //usar "oneLevel" para não verificar a presença de interesses nos atributos e métodos

        if (getRng().nextDouble() > probability) {
            return architectureSolution.copy();
        }

        int r = getRng().nextInt(0, 5);
        switch (r) {
            case 0:
                FeatureMutation(architectureSolution);
                break;
            case 1:
                MoveMethodMutationSameComponent(architectureSolution);
                break;
            case 2:
                MoveAttributeMutationSameComponent(architectureSolution);
                break;
            case 3:
                MoveOperationMutation(architectureSolution);
                break;
            case 4:
                AddClassMutation(architectureSolution);
                break;
            case 5:
                AddManagerClassMutation(architectureSolution);
                break;
        }
        return architectureSolution;
    }

    //<editor-fold defaultstate="collapsed" desc="Helpers">
    private PseudoRandomGenerator getRng() {
        return JMetalRandom.getInstance().getRandomGenerator();
    }

    private <T> T randomObjectFromCollection(Collection<T> collection) {
        //avoid creating a list if unecessary
        assert (!collection.isEmpty());

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

        return tmpList.get(getRng().nextInt(0, tmpList.size() - 1));
    }

    private String getPackageSuffix(Package component) {
        String componentName = component.getName();
        final List<String> possibleSuffixes = Arrays.asList("Mgr", "Ctrl", "GUI");
        return possibleSuffixes.stream()
                .filter(componentName::endsWith)
                .findFirst().orElse("");
    }

    private boolean searchForGeneralizations(Class cls) {
        return cls.getRelationships().stream()
                .filter(GeneralizationRelationship.class::isInstance)
                .map(GeneralizationRelationship.class::cast)
                .anyMatch(generalizationRelationship ->
                        generalizationRelationship.getChild().equals(cls) ||
                                generalizationRelationship.getParent().equals(cls));
    }

    private boolean isOptional(Architecture arch, Class cls) {
        return cls.getVariantType() != null && cls.getVariantType().equalsIgnoreCase("optional");
    }

    private boolean isVariant(Architecture arch, Class cls) {
        List<Variability> variabilities = arch.getAllVariabilities();
        return variabilities.stream().map(Variability::getVariationPoint)
                .filter(Objects::nonNull)
                .map(VariationPoint::getVariants)
                .anyMatch(
                        variants -> variants.stream().map(Variant::getVariantElement)
                                .anyMatch(element -> element.equals(cls))
                );
    }

    private boolean isVarPoint(Architecture arch, Class cls) {
        List<Variability> variabilities = arch.getAllVariabilities();
        return variabilities.stream().map(Variability::getVariationPoint)
                .filter(Objects::nonNull)
                .map(variationPoint -> (Class) variationPoint.getVariationPointElement())
                .anyMatch(classVP -> classVP.equals(cls));
    }

    private List<Class> removeClassesInPatternStructureFromArray(Collection<Class> classesComp) {
        List<Class> newList = new ArrayList<>();
        for (Class klass : classesComp) {
            if (!klass.getPatternsOperations().hasPatternApplied()) {
                newList.add(klass);
            }
        }
        return newList;
    }

    private List<Interface> removeInterfacesInPatternStructureFromArray(Collection<Interface> classesComp) {
        List<Interface> newList = new ArrayList<>();
        for (Interface anInterface : classesComp) {
            if (!anInterface.getPatternsOperations().hasPatternApplied()) {
                newList.add(anInterface);
            }
        }
        return newList;
    }

    private void createAssociation(Architecture arch, Class targetClass, Class sourceClass) {
        arch.addRelationship(new AssociationRelationship(targetClass, sourceClass));
    }

    private boolean checkSameLayer(Package source, Package target) {
        String sourceName = source.getName();
        String targetName = target.getName();
        return ((sourceName.endsWith("Mgr") && targetName.endsWith("Mgr")) ||
                (sourceName.endsWith("Ctrl") && targetName.endsWith("Ctrl")) ||
                (sourceName.endsWith("GUI") && targetName.endsWith("GUI")));
    }

    private void moveMethodToNewClass(Architecture arch, Class sourceClass, List<Method> methods, Class newClass) {
        Method targetMethod = randomObjectFromCollection(methods);
        sourceClass.moveMethodToClass(targetMethod, newClass);
        newClass.addConcerns(targetMethod.getOwnConcerns());
        createAssociation(arch, newClass, sourceClass);
    }

    private void moveAttributeToNewClass(Architecture arch, Class sourceClass, List<Attribute> attributes, Class targetClass) {
        Attribute targetAttr = randomObjectFromCollection(attributes);
        sourceClass.moveAttributeToClass(targetAttr, targetClass);

        targetClass.addConcerns(targetAttr.getOwnConcerns());
        createAssociation(arch, targetClass, sourceClass);
    }

    private void moveInterfaceToComponent(Interface interfaceComp, Package targetComp, Package sourceComp, Architecture arch, Concern selectedConcern) {
        if (!sourceComp.moveInterfaceToPackage(interfaceComp, targetComp)) {
            arch.moveElementToPackage(interfaceComp, targetComp);
        }

        for (Element implementor : interfaceComp.getImplementors()) {
            if (implementor instanceof Package) {
                Class targetCompClass = targetComp.getAllClasses().iterator().next();

                targetCompClass.getOwnConcerns().stream()
                        .filter(interfaceComp::containsConcern)
                        .forEach(concern -> {
                            arch.removeImplementedInterface(interfaceComp, sourceComp);
                            addExternalInterface(targetComp, arch, interfaceComp);
                            addImplementedInterface(targetComp, arch, interfaceComp, targetCompClass);
                        });
            } else {
                List<Class> targetClasses;
                if (targetComp.getAllClasses().size() > 1) {
                    targetClasses = allClassesWithConcern(selectedConcern, targetComp.getAllClasses());
                } else { //busca na arquitetura como um todo
                    targetClasses = allClassesWithConcern(selectedConcern, arch.getAllClasses());
                }

                if (!targetClasses.isEmpty()) {
                    Class klass = randomObjectFromCollection(targetClasses);
                    arch.removeImplementedInterface(interfaceComp, sourceComp);
                    addExternalInterface(targetComp, arch, interfaceComp);
                    addImplementedInterface(targetComp, arch, interfaceComp, klass);
                }
            }
        }
    }

    /**
     * filtra allClasses, retornando apenas aquelas que contém concern
     *
     * @return uma Lista coletada a partir de allClasses
     */
    private List<Class> allClassesWithConcern(Concern concern, Collection<Class> allClasses) {
        return allClasses.stream()
                .filter(klass -> klass.getOwnConcerns().stream().anyMatch(concern::namesMatch))
                .collect(Collectors.toList());
    }

    private void addImplementedInterface(Package targetComp, Architecture arch, Interface targetInterface, Class targetCompClass) {
        if (!packageTargetHasRealization(targetComp, targetInterface)) {
            arch.addImplementedInterface(targetInterface, targetCompClass);
        }
    }

    private boolean packageTargetHasRealization(Package targetComp, Interface targetInterface) {
        return targetComp.getRelationships().stream()
                .filter(RealizationRelationship.class::isInstance)
                .map(RealizationRelationship.class::cast)
                .anyMatch(realization -> realization.getSupplier().equals(targetInterface));
    }

    private void addExternalInterface(Package targetComp, Architecture arch, Interface targetInterface) {
        String pkgNameInterface = UtilResources.extractPackageName(targetInterface.getNamespace().trim());
        if (pkgNameInterface.equalsIgnoreCase("model")) {
            arch.addExternalInterface(targetInterface);
        } else {
            targetComp.addExternalInterface(targetInterface);
        }
    }

    private void modularizeConcernInComponent(Collection<Package> allComponents, Package targetComponent, Concern concern, Architecture arch) {
        //realiza a mutação em classes que não estão numa hierarquia de herança
        allComponents.stream()
                .filter(comp -> !comp.equals(targetComponent) && checkSameLayer(comp, targetComponent))
                .forEach(comp -> {
                    Set<Interface> allInterfaces = comp.getAllInterfaces();
                    allInterfaces.addAll(comp.getImplementedInterfaces());
                    allInterfaces.forEach(interfaceComp -> {
                        if (interfaceComp.getOwnConcerns().size() == 1 && interfaceComp.containsConcern(concern)) {
                            moveInterfaceToComponent(interfaceComp, targetComponent, comp, arch, concern);
                        } else if (!interfaceComp.getPatternsOperations().hasPatternApplied()) {
                            interfaceComp.getOperations().stream()
                                    .filter(method -> method.getOwnConcerns().size() == 1 && method.containsConcern(concern))
                                    .forEach(method -> moveOperationToComponent(method, interfaceComp, targetComponent, comp, arch, concern));
                        }
                    });

                    comp.getAllClasses().forEach(classComp -> {
                        if (classComp.getOwnConcerns().size() == 1 && classComp.containsConcern(concern)) {
                            if (!searchForGeneralizations(classComp)) { //realiza a mutação em classes que não estão numa hierarquia de herança
                                moveClassToComponent(classComp, targetComponent, comp, arch, concern);
                            } else {
                                moveHierarchyToComponent(classComp, targetComponent, arch);
                            }
                        } else {
                            if (!searchForGeneralizations(classComp)) {
                                if (!isVarPointOrVariantOfConcern(arch, classComp, concern)) {
                                    Set<Attribute> attributesClassComp = classComp.getAllAttributes();
                                    attributesClassComp.stream()
                                            .filter(attribute -> attribute.getOwnConcerns().size() == 1 && attribute.containsConcern(concern))
                                            .forEach(attribute -> moveAttributeToComponent(attribute, classComp, targetComponent, comp, arch, concern));
                                }

                                if (!classComp.getPatternsOperations().hasPatternApplied()) {
                                    classComp.getAllMethods().stream()
                                            .filter(method -> method.getOwnConcerns().size() == 1 && method.containsConcern(concern))
                                            .forEach(method -> moveMethodToComponent(method, classComp, targetComponent, comp, arch, concern));
                                }
                            }
                        }
                    });
                });
    }

    private void moveMethodToComponent(Method method, Class classComp, Package targetComponent, Package comp, Architecture arch, Concern concern) {
        Class targetClass = findOrCreateClassWithConcern(targetComponent, concern);
        classComp.moveMethodToClass(method, targetClass);
        createAssociation(arch, targetClass, classComp);
    }

    private void moveAttributeToComponent(Attribute attribute, Class classComp, Package targetComponent, Package sourceComp, Architecture architecture, Concern concern) {
        Class targetClass = findOrCreateClassWithConcern(targetComponent, concern);
        classComp.moveAttributeToClass(attribute, targetClass);
        createAssociation(architecture, targetClass, classComp);
    }

    private Class findOrCreateClassWithConcern(Package targetComp, Concern concern) {
        return targetComp.getAllClasses().stream()
                .filter(cls -> cls.containsConcern(concern))
                .findFirst()
                .orElseGet(() -> {
                    Class targetClass = targetComp.createClass("Class" + contClass.getAndIncrement(), false);
//                    Class targetClass = targetComp.createClass("Class" + OPLAProblem.contClass.get(), false);
//                    OPLAProblem.contClass.set(OPLAProblem.contClass.get() + 1);
                    targetClass.addConcern(concern);
                    return targetClass;
                });
    }

    /**
     * Otimizando duas iterações.
     *
     * @see br.ufpr.inf.opla.patterns.operator.impl.PLAMutation#isVarPointOfConcern
     * @see br.ufpr.inf.opla.patterns.operator.impl.PLAMutation#isVariantOfConcern
     */
    private boolean isVarPointOrVariantOfConcern(Architecture arch, Class cls, Concern concern) {
        boolean fallback = (cls.getVariantType() != null && cls.getVariantType().equalsIgnoreCase("optional"));

        return arch.getAllVariabilities().stream()
                .anyMatch(variability -> {
                    VariationPoint varPoint = variability.getVariationPoint();
                    if (varPoint != null) {
                        if (!variability.getName().equals(concern.getName())) {
                            return false;
                        }

                        Class classVP = (Class) varPoint.getVariationPointElement();
                        return classVP.equals(cls) ||
                                varPoint.getVariants().stream()
                                        .map(Variant::getVariantElement)
                                        .anyMatch(cls::equals);

                    }
                    return fallback;
                });
    }

    /**
     * @see br.ufpr.inf.opla.patterns.operator.impl.PLAMutation#isVarPointOfConcern
     */
    @Deprecated
    private boolean isVarPointOfConcern(Architecture arch, Class classComp, Concern concern) {
        return arch.getAllVariabilities().stream()
                .filter(variability -> variability.getName().equals(concern.getName()))
                .map(Variability::getVariationPoint)
                .filter(Objects::nonNull)
                .map(VariationPoint::getVariationPointElement)
                .map(Class.class::cast)
                .anyMatch(classComp::equals);
    }

    /**
     * @see br.ufpr.inf.opla.patterns.operator.impl.PLAMutation#isVariantOfConcern
     */
    @Deprecated
    private boolean isVariantOfConcern(Architecture arch, Class cls, Concern concern) {

        boolean fallback = (cls.getVariantType() != null && cls.getVariantType().equalsIgnoreCase("optional"));

        return arch.getAllVariabilities().stream()
                .anyMatch(variability -> {
                    VariationPoint varPoint = variability.getVariationPoint();
                    return (varPoint != null) ? ((variability.getName().equals(concern.getName()) &&
                            varPoint.getVariants().stream().map(Variant::getVariantElement).anyMatch(cls::equals)))
                            : (fallback);
                });
    }

    /**
     * metodo que move a hierarquia de classes para um outro componente que esta
     * modularizando o interesse
     *
     * @param classComp    - Classe selecionada
     * @param targetComp   - Pacote destino
     * @param architecture - arquiteutra
     */
    private void moveHierarchyToComponent(Class classComp, Package targetComp, Architecture architecture) {
        architecture.forGeneralization()
                .moveGeneralizationToPackage(getGeneralizationRelationshipForClass(classComp), targetComp);
    }

    /**
     * Dado um {@link Element} retorna a {@link GeneralizationRelationship} no
     * qual o mesmo pertence.
     *
     * @param element: o Elemento a ser analisado
     * @return {@link GeneralizationRelationship}
     */
    private GeneralizationRelationship getGeneralizationRelationshipForClass(Element element) {
        return element.getRelationships().stream()
                .filter(GeneralizationRelationship.class::isInstance)
                .map(GeneralizationRelationship.class::cast)
                .filter(g -> g.getParent().equals(element) || g.getChild().equals(element))
                .findFirst().orElse(null);
    }

    private void moveClassToComponent(Class classComp, Package targetComponent, Package comp, Architecture arch, Concern concern) {
    }

    private void moveOperationToComponent(Method method, Interface sourceInterface, Package targetComponent, Package comp, Architecture arch, Concern concern) {
        Optional<Interface> maybeTargetInterface = searchForInterfaceWithConcern(concern, targetComponent);
        Interface targetInterface;

        if (maybeTargetInterface.isPresent()) {
            targetInterface = maybeTargetInterface.get();
        } else {
//            targetInterface = targetComponent.createInterface("Interface" + OPLAProblem.contInterface.get());
            targetInterface = targetComponent.createInterface("Interface" + contInterfaces.getAndIncrement());
//            OPLAProblem.contInterface.set(OPLAProblem.contInterface.get() + 1);
            targetInterface.addConcern(concern);
        }
        sourceInterface.moveOperationToInterface(method, targetInterface);
    }

    private Optional<Interface> searchForInterfaceWithConcern(Concern concern, Package targetComponent) {
        Optional<Interface> implIntf = targetComponent.getImplementedInterfaces().stream()
                .filter(itf -> itf.containsConcern(concern)).findFirst();
        if (implIntf.isPresent()) {
            return implIntf;
        }
        return targetComponent.getAllInterfaces().stream()
                .filter(itf -> itf.containsConcern(concern)).findFirst();
    }

    private List<Package> searchComponentsAssignedToConcern(Concern concern, Collection<Package> allComponents) {
        return allComponents.stream().filter(component -> {
            Set<Concern> concernsForPackage = new HashSet<>();
            component.getAllClasses().stream().map(Class::getAllConcerns)
                    .forEach(concernsForPackage::addAll);
            component.getAllInterfaces().stream().map(Interface::getOwnConcerns)
                    .forEach(concernsForPackage::addAll);
            return concernsForPackage.size() == 1 && concernsForPackage.contains(concern);
        }).collect(Collectors.toList());
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MoveAttributeMutation">
    private void MoveAttributeMutationSameComponent(ArchitectureSolution solution) {
        LOGGER.info("Executando MoveAttributeMutationSameComponent");
        Architecture arch = solution.getArchitecture();
        List<Package> archPackages = new ArrayList<>(arch.getAllPackages());

        Package randomPackage = randomObjectFromCollection(archPackages);
        Set<Class> classesComp = randomPackage.getAllClasses();
        if (classesComp.size() > 1) {
            Class targetClass = randomObjectFromCollection(classesComp);
            Class sourceClass = randomObjectFromCollection(classesComp);
            if (!targetClass.equals(sourceClass)) {
                if (!searchForGeneralizations(sourceClass) &&
                        sourceClass.getAllAttributes().size() > 1 &&
                        sourceClass.getAllMethods().size() > 1 &&
                        !isOptional(arch, sourceClass) &&
                        !isVariant(arch, sourceClass) &&
                        !isVarPoint(arch, sourceClass)) {
                    moveAttribute(arch, targetClass, sourceClass);
                }
            }
        }
    }

    boolean MoveAttributeMutationAllLevels(ArchitectureSolution solution) {
        throw new UnsupportedOperationException("Método ainda não implementado.");
    }

    private void moveAttribute(Architecture arch, Class targetClass, Class sourceClass) {
        Set<Attribute> attrsClass = sourceClass.getAllAttributes();
        if (!attrsClass.isEmpty()) {
            Attribute randomAttr = randomObjectFromCollection(attrsClass);
            if (sourceClass.moveAttributeToClass(randomAttr, targetClass)) {
                createAssociation(arch, targetClass, sourceClass);
            }
        }
    }

    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MoveMethodMutation">
    private void MoveMethodMutationSameComponent(ArchitectureSolution architectureSolution) {
        LOGGER.info("Executando MoveMethodMutationSameComponent");
        Architecture arch = architectureSolution.getArchitecture();
        Package sourceComp = randomObjectFromCollection(arch.getAllPackages());
        List<Class> classesComp = new ArrayList<>(sourceComp.getAllClasses());
        classesComp = removeClassesInPatternStructureFromArray(classesComp);

        if (classesComp.size() > 1) {
            Class targetClass = randomObjectFromCollection(classesComp);
            Class sourceClass = randomObjectFromCollection(classesComp);
            if (!targetClass.equals(sourceClass)) {
                if (!searchForGeneralizations(sourceClass) &&
                        sourceClass.getAllAttributes().size() > 1 &&
                        sourceClass.getAllMethods().size() > 1 &&
                        !isOptional(arch, sourceClass) &&
                        !isVariant(arch, sourceClass) &&
                        !isVarPoint(arch, sourceClass)) {
                    moveMethod(arch, targetClass, sourceClass);
                }
            }
        }
    }

    void MoveMethodMutationAllLevels(ArchitectureSolution architectureSolution) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    private void moveMethod(Architecture arch, Class targetClass, Class sourceClass) {
        Set<Method> methodsClass = sourceClass.getAllMethods();
        if (!methodsClass.isEmpty()) {
            Method randomMethod = randomObjectFromCollection(methodsClass);
            if (sourceClass.moveMethodToClass(randomMethod, targetClass)) {
                createAssociation(arch, targetClass, sourceClass);
            }
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MoveOperationMutation">
    private void MoveOperationMutation(ArchitectureSolution solution) {
        LOGGER.info("Executando MoveOperationMutation");

        Architecture arch = solution.getArchitecture();
        Set<Package> archPackages = arch.getAllPackages();

        Package sourceComp = randomObjectFromCollection(archPackages);
        Package targetComp = randomObjectFromCollection(archPackages);

        if (checkSameLayer(sourceComp, targetComp)) {
            List<Interface> sourceInterfaces = new ArrayList<>(sourceComp.getImplementedInterfaces());
            sourceInterfaces = removeInterfacesInPatternStructureFromArray(sourceInterfaces);

            List<Interface> targetInterfaces = new ArrayList<>(targetComp.getImplementedInterfaces());
            if (!sourceInterfaces.isEmpty() && !targetInterfaces.isEmpty()) {
                Interface sourceInterface = randomObjectFromCollection(sourceInterfaces);
                Interface targetInterface = randomObjectFromCollection(targetInterfaces);

                if (!targetInterface.equals(sourceInterface)) {
                    List<Method> methodsInterface = new ArrayList<>(sourceInterface.getOperations());
                    if (!methodsInterface.isEmpty()) {
                        Method randomMethod = randomObjectFromCollection(methodsInterface);
                        sourceInterface.moveOperationToInterface(randomMethod, targetInterface);
                        sourceInterface.getImplementors().forEach(implementor -> {
                            if (implementor instanceof Package) {
                                arch.addImplementedInterface(targetInterface, (Package) implementor);
                            } else if (implementor instanceof Class) {
                                arch.addImplementedInterface(targetInterface, (Class) implementor);
                            }
                        });
                    }
                }
            }
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="AddClassMutation">
    private void AddClassMutation(ArchitectureSolution solution) {
        LOGGER.info("Executand AddClassMutation ");

        Architecture arch = solution.getArchitecture();
        Set<Package> packages = arch.getAllPackages();

        Package sourceComp = randomObjectFromCollection(packages);
        List<Class> classCompList = new ArrayList<>(sourceComp.getAllClasses());
        classCompList = removeClassesInPatternStructureFromArray(classCompList);

        if (!classCompList.isEmpty()) {
            Class sourceClass = randomObjectFromCollection(classCompList);

            if (!searchForGeneralizations(sourceClass) &&
                    sourceClass.getAllAttributes().size() > 1 &&
                    sourceClass.getAllMethods().size() > 1 &&
                    !isOptional(arch, sourceClass) &&
                    !isVariant(arch, sourceClass) &&
                    !isVarPoint(arch, sourceClass)) {
                if (getRng().nextInt(0, 1) == 0) { //attribute
                    List<Attribute> attrClassList = new ArrayList<>(sourceClass.getAllAttributes());
                    if (!attrClassList.isEmpty()) {
                        Class newClass = sourceComp.createClass("Class" + contClass.getAndIncrement(), false);

//                        Class newClass = sourceComp.createClass("Class" + OPLAProblem.contClass.get(), false);
//                        OPLAProblem.contClass.set(OPLAProblem.contClass.get() + 1);
                        //TODO: allComponents
                        //arquitetura.representation.Package targetComp = randomObject(new ArrayList<arquitetura.representation.Package>(arch.getAllPackages()));
                        //if (checkSameLayer(sourceComp, targetComp)) {
                        //    moveAttributeToNewClass(arch, sourceClass, AttributesClass, targetComp.createClass("Class" + OPLA.contClass_++, false));
                        //}

                        moveAttributeToNewClass(arch, sourceClass, attrClassList, newClass);
                    }
                } else { //method
                    List<Method> methodsClassList = new ArrayList<>(sourceClass.getAllMethods());
                    if (!methodsClassList.isEmpty()) {
                        Class newClass = sourceComp.createClass("Class" + contClass.getAndIncrement(), false);

//                        Class newClass = sourceComp.createClass("Class" + OPLAProblem.contClass.get(), false);
//                        OPLAProblem.contClass.set(OPLAProblem.contClass.get() + 1);
                        moveMethodToNewClass(arch, sourceClass, methodsClassList, newClass);
                        //TODO: allComponents
                    }
                }
            }

        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="AddManagerClassMutation">
    private void AddManagerClassMutation(ArchitectureSolution solution) {
        LOGGER.info("Executando AddManagerClassMutation");

        Architecture arch = solution.getArchitecture();

        Package sourceComp = randomObjectFromCollection(arch.getAllPackages());
        List<Interface> interfacesComp = new ArrayList<>(sourceComp.getImplementedInterfaces());
        interfacesComp = removeInterfacesInPatternStructureFromArray(interfacesComp);

        if (!interfacesComp.isEmpty()) {
            Interface sourceInterface = randomObjectFromCollection(interfacesComp);
            Set<Method> interfaceMethods = sourceInterface.getOperations();
            if (!interfaceMethods.isEmpty()) {
                Method method = randomObjectFromCollection(interfaceMethods);
                Package newComponent = arch.createPackage("Package" + contComponents.getAndIncrement() + getPackageSuffix(sourceComp));

//                Package newComponent = arch.createPackage("Package" + OPLAProblem.contComponent.get() + getPackageSuffix(sourceComp));
//                OPLAProblem.contComponent.set(OPLAProblem.contComponent.get() + 1);

                Interface newInterface = newComponent.createInterface("Interface" + contInterfaces.getAndIncrement());

//                Interface newInterface = newComponent.createInterface("Interface" + OPLAProblem.contInterface.get());
//                OPLAProblem.contInterface.set(OPLAProblem.contInterface.get() + 1);

                sourceInterface.moveOperationToInterface(method, newInterface);

                sourceInterface.getImplementors().forEach(implementor ->
                        arch.addImplementedInterface(newInterface, implementor));

                newInterface.addConcerns(method.getOwnConcerns());
            }
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="FeatureMutation">
    private void FeatureMutation(ArchitectureSolution solution) {
        LOGGER.info("Executando FeatureMutation");
        Architecture arch = solution.getArchitecture();
        Set<Package> allComponents = arch.getAllPackages();
        if (!allComponents.isEmpty()) {
            Package selectedComponent = randomObjectFromCollection(allComponents);

            Set<Concern> concernsSelectedComp = selectedComponent.getAllConcerns();
            if (concernsSelectedComp.size() > 1) { // = somente para testes (sic)
                Concern selectedConcern = randomObjectFromCollection(concernsSelectedComp);

                List<Package> allComponentsForConcern = searchComponentsAssignedToConcern(selectedConcern, allComponents);
                Package targetPackage;
                if (allComponentsForConcern.isEmpty()) {
                    targetPackage = arch.createPackage("Package" + contComponents.getAndIncrement() + getPackageSuffix(selectedComponent));
//                    targetPackage = arch.createPackage("Package" + OPLAProblem.contComponent.get() + getPackageSuffix(selectedComponent));
//                    OPLAProblem.contComponent.set(OPLAProblem.contComponent.get() + 1);
                } else {
                    targetPackage = randomObjectFromCollection(allComponentsForConcern);
                }

                modularizeConcernInComponent(allComponents, targetPackage, selectedConcern, arch);
            }
        }
    }
    //</editor-fold>

}
