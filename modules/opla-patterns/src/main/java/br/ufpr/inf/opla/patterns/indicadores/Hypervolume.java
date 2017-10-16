/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufpr.inf.opla.patterns.indicadores;

import br.ufpr.inf.opla.patterns.solution.ArchitectureSolution;
import jmetal4.core.SolutionSet;
import jmetal4.qualityIndicator.util.MetricsUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.List;

/**
 * @author giovaniguizzo
 */
public class Hypervolume {

    //legado
    public static void clearFile(String path) {
        try {
            clearFile(Paths.get(path));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static void clearFile(Path path) throws IOException {
        Files.deleteIfExists(path);
    }

    public static void printFormatedHypervolumeFile(List<ArchitectureSolution> allSolutions, Path path) throws IOException {
        Files.createDirectories(path.getParent());
        if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
            Files.createFile(path);
        }

        for (ArchitectureSolution architectureSolution : allSolutions) {
            Files.write(path, (architectureSolution.toString() + "\n").getBytes(), StandardOpenOption.APPEND);
        }
        Files.write(path, "\n".getBytes(), StandardOpenOption.APPEND);
    }

    public static void printFormatedHypervolumeFile(SolutionSet allSolutions, String path, boolean append) throws IOException {
        File file = new File(path);
        file.getParentFile().mkdirs();
        try (FileWriter fileWriter = new FileWriter(file, append)) {
            for (int i = 0; i < allSolutions.size(); i++) {
                fileWriter.write(allSolutions.get(i).toString());
                fileWriter.write("\n");
            }
            fileWriter.write("\n");
        }
    }

    public static double[] printReferencePoint(double[][] allSolutions, String path, int objectives) throws IOException {
        File file = new File(path);
        file.getParentFile().mkdirs();
        double[] max = new MetricsUtil().getMaximumValues(allSolutions, objectives);
        try (FileWriter fileWriter = new FileWriter(file)) {
            for (double d : max) {
                fileWriter.write(Double.toString(d + 0.1D) + " ");
            }
        }
        return max;
    }

}
