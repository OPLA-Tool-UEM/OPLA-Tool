package br.ufpr.inf.opla.patterns.main;

import arquitetura.io.ReaderConfig;
import arquitetura.representation.Architecture;
import br.ufpr.inf.opla.patterns.indicadores.Hypervolume;
import br.ufpr.inf.opla.patterns.operator.impl.jmetal5.PLACrossover;
import br.ufpr.inf.opla.patterns.operator.impl.jmetal5.PLAMutation;
import br.ufpr.inf.opla.patterns.problem.multiobjective.OPLAProblem;
import br.ufpr.inf.opla.patterns.solution.ArchitectureSolution;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAII;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.util.SolutionListUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Usando os pacotes do JMetal5, tenta imitar o funcionamento da classe NSGAII_OPLA
 * Multithreading via {@link ExecutorService}
 */
@SuppressWarnings("Duplicates")
public class NSGAII_Jmetal5_Multithreaded {

    private static int populationSize_;
    private static int maxEvaluations_;
    private static double mutationProbability_;
    private static double crossoverProbability_;
    private static Map<Integer, List<ArchitectureSolution>> allSolutions = new HashMap<>();

    static private List<ArchitectureSolution> removeDominadas(List<ArchitectureSolution> solutions) {
        for (int i = 0; i < solutions.size() - 1; i++) {
            ArchitectureSolution first = solutions.get(i);

            for (int j = i + 1; j < solutions.size(); j++) {
                ArchitectureSolution second = solutions.get(j);

                boolean dominador = true;
                boolean dominado = true;

                for (int obj = 0; obj < first.getNumberOfObjectives(); obj++) {
                    double valor1 = first.getObjective(obj);
                    double valor2 = second.getObjective(obj);

                    if (valor1 > valor2) {
                        dominador = false;
                    }

                    if (valor2 > valor1) {
                        dominado = false;
                    }
                }

                if (dominador) {
                    solutions.remove(j);
                    j = j - 1;
                } else if (dominado) {
                    solutions.remove(i);
                    j = i;
                }
            }
        }
        return solutions;
    }

    static private List<ArchitectureSolution> removeRepetidas(List<ArchitectureSolution> solutions) {
        return solutions.stream().distinct().collect(Collectors.toList());
    }

    public static void main(String... args) throws ClassNotFoundException, IOException {
        //versao com args próprios

        String[] myArgs = {
                /*population size*/"30",
                /*max evaluations*/"100",
                /*Mutation probability*/"0.9",
                /*PLA path*/"/home/barbiero/TCC/PLAs/banking/banking.uml",
                /*Context*/"teste1",
                /*Mutation operator*/"PLAMutation",
                /*print variables?*/"true"
        };

        runNSGAII_OPLA(myArgs);
    }

    private static Runnable buildNSGAIIWrapperRunnable(NSGAIIBuilder<ArchitectureSolution> builder, int run) {
        return () -> {
            System.out.println("\tInicializando run #" + run);
            long init = System.currentTimeMillis();
            NSGAII<ArchitectureSolution> nsgaii = builder.build();
            nsgaii.run();
            List<ArchitectureSolution> result = removeRepetidas(nsgaii.getResult());
            allSolutions.put(run, result);
            System.out.println("\tFim da run #" + run + " em " + ((System.currentTimeMillis() - init)) / 1000.0 + " segundos");
        };
    }


    //--  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --
    private static void runNSGAII_OPLA(String[] args) throws IOException, ClassNotFoundException {
        if (args.length < 7) {
            System.out.println("You need to inform the following parameters:");
            System.out.println("\t1 - Population Size (Integer);"
                    + "\n\t2 - Max Evaluations (Integer);"
                    + "\n\t3 - Mutation Probability (Double);"
                    + "\n\t4 - PLA path;"
                    + "\n\t5 - Context;"
                    + "\n\t6 - Mutation Operator class simple name;"
                    + "\n\t7 - If you want to write the variables (Boolean).");
            System.exit(0);
        }

        int runsNumber = 16; //30;
        if (args[0] == null || args[0].trim().equals("")) {
            System.out.println("Missing population size argument.");
            System.exit(1);
        }
        try {
            populationSize_ = Integer.valueOf(args[0]); //100;
        } catch (NumberFormatException ex) {
            System.out.println("Population size argument not integer.");
            System.exit(1);
        }
        if (args[1] == null || args[1].trim().equals("")) {
            System.out.println("Missing max evaluations argument.");
            System.exit(1);
        }
        try {
            maxEvaluations_ = Integer.valueOf(args[1]); //300 geraçõeshttp://loggr.net/
        } catch (NumberFormatException ex) {
            System.out.println("Max evaluations argument not integer.");
            System.exit(1);
        }
        crossoverProbability_ = 0.5;
        if (args[2] == null || args[2].trim().equals("")) {
            System.out.println("Missing mutation probability argument.");
            System.exit(1);
        }
        try {
            mutationProbability_ = Double.valueOf(args[2]);
        } catch (NumberFormatException ex) {
            System.out.println("Mutation probability argument not double.");
            System.exit(1);
        }

        if (args[3] == null || args[3].trim().equals("")) {
            System.out.println("Missing PLA Path argument.");
            System.exit(1);
        }
        String pla = args[3];

        if (args[4] == null || args[4].trim().equals("")) {
            System.out.println("Missing context argument.");
            System.exit(1);
        }
        String context = args[4];

        if (args[5] == null || args[5].trim().equals("")) {
            System.out.println("Missing mutation operator argument.");
            System.exit(1);
        }

        if (args[6] == null || args[6].trim().equals("")) {
            System.out.println("Missing print variables argument.");
            System.exit(1);
        }

        String plaName = getPlaName(pla);

        Path rootDir = Paths.get("experiment", plaName, context);
        Path manipulationDir = rootDir.resolve("manipulation");
        Path outputDir = rootDir.resolve("output");

        Files.createDirectories(manipulationDir);
        Files.createDirectories(outputDir);

        ReaderConfig.setDirTarget(manipulationDir.toString() + "/");
        ReaderConfig.setDirExportTarget(outputDir.toString() + "/");

        String plaDirectory = Paths.get(pla).getParent().toString() + "/";

        ReaderConfig.setPathToTemplateModelsDirectory(plaDirectory);
        ReaderConfig.setPathToProfileSMarty(plaDirectory + "smarty.profile.uml");
        ReaderConfig.setPathToProfileConcerns(plaDirectory + "concerns.profile.uml");
        ReaderConfig.setPathProfileRelationship(plaDirectory + "relationships.profile.uml");
        ReaderConfig.setPathToProfilePatterns(plaDirectory + "patterns.profile.uml");

        //executar FeatureDriven antes de Conventional é cerca de 17% mais rápido
        String[] objectives = {"featureDriven", "conventional"};
        OPLAProblem oplaProblem = new OPLAProblem(pla, objectives);

        CrossoverOperator<ArchitectureSolution> plaOperator = new PLACrossover(crossoverProbability_);
        MutationOperator<ArchitectureSolution> mutationOperator = new PLAMutation(mutationProbability_);

        NSGAIIBuilder<ArchitectureSolution> nsgaiiBuilder = new NSGAIIBuilder<>(oplaProblem, plaOperator, mutationOperator)
                .setMaxEvaluations(maxEvaluations_).setPopulationSize(populationSize_);


        System.out.println("\n================ NSGAII ================");
        System.out.println("Context: " + context);
        System.out.println("PLA: " + pla);
        System.out.println("Params:");
        System.out.println("\tPop -> " + populationSize_);
        System.out.println("\tMaxEva -> " + maxEvaluations_);
        System.out.println("\tCross -> " + crossoverProbability_);
        System.out.println("\tMuta -> " + mutationProbability_);
        System.out.println("\tRuns -> " + runsNumber);
        System.out.println("\tThreads -> " + Runtime.getRuntime().availableProcessors());


        Hypervolume.clearFile(rootDir.toString() + "/HYPERVOLUME.txt");

        System.out.println("Execução paralela do NSGAII");

        System.out.print("dummy run pra quebrar quaisquer biases de cache...");
        long init = System.currentTimeMillis();
        buildNSGAIIWrapperRunnable(nsgaiiBuilder, -1).run();
        System.out.println(" tempo: " + (System.currentTimeMillis() - init) / 1000.0 + " segundos.");


        int n = Runtime.getRuntime().availableProcessors();

        ExecutorService executorService = Executors.newFixedThreadPool(8);
        long initTotal = System.currentTimeMillis();
        for (int r = 0; r < runsNumber; r++) {
            executorService.execute(buildNSGAIIWrapperRunnable(nsgaiiBuilder, r));
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long endTotal = System.currentTimeMillis();

        System.out.println("Tempo total: " + (endTotal - initTotal) / 1000.0 + " segundos.");

        System.out.println("Numero de soluções: ");
        allSolutions.forEach((key, value) -> System.out.println("\t[" + key + "] => " + value.size()));

        List<ArchitectureSolution> condensedList = SolutionListUtils.getNondominatedSolutions(allSolutions.values().stream().flatMap(List::stream).collect(Collectors.toList()));
        System.out.println("Tamanho final => " + condensedList.size());

        System.out.println("Salvando em " + ReaderConfig.getDirExportTarget() + "...");
        IntStream.range(0, condensedList.size()).forEach(i -> {
            Architecture architecture = condensedList.get(i).getArchitecture();
            architecture.save(architecture, "VAR_ALL_", "-" + i);
            System.out.println("VAR_ALL_" + i + " salvo.");
        });

    }

    private static String getPlaName(String pla) {
        int beginIndex = pla.lastIndexOf('/') + 1;
        int endIndex = pla.length() - 4;
        return pla.substring(beginIndex, endIndex);
    }
}
