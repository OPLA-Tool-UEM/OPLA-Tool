package org.uma.jmetal.util.pseudorandom;

import java.io.Serializable;

/**
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public interface PseudoRandomGenerator extends Serializable {
    public int nextInt(int lowerBound, int upperBound);

    public double nextDouble(double lowerBound, double upperBound);

    public double nextDouble();

    public long getSeed();

    public void setSeed(long seed);

    public String getName();
}
