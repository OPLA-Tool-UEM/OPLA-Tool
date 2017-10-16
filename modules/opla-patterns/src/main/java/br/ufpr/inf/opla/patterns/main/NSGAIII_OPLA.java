package br.ufpr.inf.opla.patterns.main;

import arquitetura.io.ReaderConfig;
import arquitetura.representation.Architecture;
import br.ufpr.inf.opla.patterns.indicadores.Hypervolume;
import br.ufpr.inf.opla.patterns.operator.impl.jmetal5.PLAFeatureMutation;
import br.ufpr.inf.opla.patterns.problem.multiobjective.OPLAProblem;
import br.ufpr.inf.opla.patterns.solution.ArchitectureSolution;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.NSGAIII;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.NSGAIIIBuilder;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.impl.crossover.NullCrossover;
import org.uma.jmetal.operator.impl.selection.BinaryTournamentSelection;
import org.uma.jmetal.util.SolutionListUtils;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Experimentos com NSGAIII
 */
@SuppressWarnings("Duplicates")
public class NSGAIII_OPLA {

    private static Map<Integer, Long> executionTime = new ConcurrentSkipListMap<>();

    private static Callable<List<ArchitectureSolution>> buildNSGAIIIWrapperCallable(NSGAIIIBuilder<ArchitectureSolution> builder, int run) {
        return () -> {
            NSGAIII<ArchitectureSolution> nsgaiii = builder.build();
            System.out.println("\tIniciando execução #" + run + "... Popsize: " + nsgaiii.getMaxPopulationSize());
            long init = System.currentTimeMillis();
            nsgaiii.run();
            long end = System.currentTimeMillis();
            assert end > init;
            executionTime.put(run, end - init);
            System.out.println("\tRun #" + run + " finalizada.");
            //remover dominadas
            return nsgaiii.getResult();
        };
    }

    public static void main(String... args) throws ClassNotFoundException, IOException {
        String plaName = "AGM";
        Path plaPath = Paths.get("D:", "pedro", "OpenSource", "PLAs", plaName, plaName + ".uml");
        double mutationProbability = 0.9;
        int runsNumber = 32;
        int numIterations = 300;

        String fitnessFilename = "FITNESS.txt";
        String runtimesFilename = "TEMPOEXEC.txt";

        //2: DC e COE
        //3: COE, ACLASS e DC
        //5: COE, ACLASS, DC, LCC, TAM
        String[] objectives = {
                "coe", "dc"
        };
        String context = "nsgaiii-" + objectives.length + "obj";

        CrossoverOperator<ArchitectureSolution> crossoverOperator = new NullCrossover<>();
        MutationOperator<ArchitectureSolution> mutationOperator = new PLAFeatureMutation(mutationProbability);
        BinaryTournamentSelection<ArchitectureSolution> selectionOperator = new BinaryTournamentSelection<>();

        String fileSep = FileSystems.getDefault().getSeparator();

        Path rootDir = Paths.get("experiment", plaName, context);
        Path manipulationDir = rootDir.resolve("manipulation");
        Path outputDir = rootDir.resolve("output");

        Files.createDirectories(manipulationDir);
        Files.createDirectories(outputDir);

        ReaderConfig.setDirTarget(manipulationDir.toString() + fileSep);
        ReaderConfig.setDirExportTarget(outputDir.toString() + fileSep);

        Path plaDirectory = plaPath.getParent();
        Path smartyProfilePath = plaPath.resolveSibling("smarty.profile.uml");
        Path concernProfilePath = plaPath.resolveSibling("concerns.profile.uml");
        Path relationshipProfilePath = plaPath.resolveSibling("relationships.profile.uml");
        Path patternProfilePath = plaPath.resolveSibling("patterns.profile.uml");

        ReaderConfig.setPathToTemplateModelsDirectory(plaDirectory.toString());
        ReaderConfig.setPathToProfileSMarty(smartyProfilePath.toString());
        ReaderConfig.setPathToProfileConcerns(concernProfilePath.toString());
        ReaderConfig.setPathProfileRelationship(relationshipProfilePath.toString());
        ReaderConfig.setPathToProfilePatterns(patternProfilePath.toString());

        OPLAProblem oplaProblem = new OPLAProblem(plaPath.toString(), objectives);

        NSGAIIIBuilder<ArchitectureSolution> nsgaiiiBuilder = new NSGAIIIBuilder<>(oplaProblem).setCrossoverOperator(crossoverOperator)
                .setMutationOperator(mutationOperator).setSelectionOperator(selectionOperator).setMaxIterations(numIterations);

        Path fitnessFile = rootDir.resolve(fitnessFilename);
        Hypervolume.clearFile(fitnessFile);

        Path exectimeFile = rootDir.resolve(runtimesFilename);
        Hypervolume.clearFile(exectimeFile);


        System.out.println("============ NSGAIII ============");
        System.out.println("Avaliando projeto " + plaName);
        System.out.println("Path: " + plaPath.toString());
        System.out.println("Valores do experimento: ");
        System.out.println("\tNúmero Objetivos: " + objectives.length);
        System.out.println("\tObjetivos: " + Arrays.stream(objectives).collect(Collectors.joining(", ")));
        System.out.println("\tProb mutação: " + mutationProbability);
        System.out.println("\tNúmero de iterações: " + numIterations);
        System.out.println("\tAvaliação dos objetivos do projeto base:");
        {
            ArchitectureSolution base = oplaProblem.createSolution();
            oplaProblem.evaluate(base);
            int k = base.getNumberOfObjectives();
            String baseObjVals = IntStream.range(0, k).mapToObj(j -> "" + base.getObjective(j)).collect(Collectors.joining(", "));
            System.out.println("\t[" + baseObjVals + "]");
        }
        System.out.println("Arquivo com os Fitness: " + fitnessFile.toAbsolutePath());
        System.out.println("Arquivo com tempo de execucao: " + exectimeFile.toAbsolutePath());

        System.out.println();
        System.out.println("System info: ");

        int processors = Runtime.getRuntime().availableProcessors();
        System.out.println("Processadores: " + processors);

        System.out.println("Executando " + runsNumber + " rodadas.");

        ExecutorService executorService = Executors.newFixedThreadPool(processors);

        List<Callable<List<ArchitectureSolution>>> callables = IntStream.range(0, runsNumber)
                .mapToObj(r -> buildNSGAIIIWrapperCallable(nsgaiiiBuilder, r)).collect(Collectors.toList());

        try {
            System.out.println("Inicio: " + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            List<Future<List<ArchitectureSolution>>> futureResults = executorService.invokeAll(callables);
            System.out.println("Fim das execuções: " + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

            Files.createFile(exectimeFile);
            Files.write(exectimeFile, "TEMPO DE EXECUÇÃO DE CADA RUN\r\n".getBytes(), StandardOpenOption.APPEND);
            for (Map.Entry<Integer, Long> entry : executionTime.entrySet()) {
                int run = entry.getKey();
                long time = entry.getValue();

                Files.write(exectimeFile, ("Exec " + run + " => " + time + "ms\r\n").getBytes(), StandardOpenOption.APPEND);
            }
            Files.write(exectimeFile, "\r\n".getBytes(), StandardOpenOption.APPEND);

            List<ArchitectureSolution> allSolutions = new ArrayList<>();
            for (Future<List<ArchitectureSolution>> future : futureResults) {
                allSolutions.addAll(future.get());
            }

            allSolutions = SolutionListUtils.getNondominatedSolutions(allSolutions);
            System.out.println("Total de soluções não-dominadas: " + allSolutions.size());
            Hypervolume.printFormatedHypervolumeFile(allSolutions, fitnessFile);

            System.out.println("Salvando soluções...");
            int index = 0;
            for (ArchitectureSolution architectureSolution : allSolutions) {
                Architecture arch = architectureSolution.getArchitecture();
                arch.save(arch, "ARCH_", "_" + index++);
                System.out.println("\t Solução " + (index - 1) + "salva.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        executorService.shutdown();

    }

}
