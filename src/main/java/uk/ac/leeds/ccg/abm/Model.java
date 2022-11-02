/*
 * Copyright 2021 Centre for Computational Geography, University of Leeds.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.leeds.ccg.abm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.leeds.ccg.generic.util.Generic_Time;
import uk.ac.leeds.ccg.grids.core.Grids_Environment;
import uk.ac.leeds.ccg.grids.d2.chunk.i.Grids_ChunkIntFactoryArray;
import uk.ac.leeds.ccg.grids.d2.chunk.i.Grids_ChunkIntFactorySinglet;
import uk.ac.leeds.ccg.grids.d2.grid.i.Grids_GridIntFactory;
import uk.ac.leeds.ccg.grids.d2.grid.i.Grids_GridIntStats;
import uk.ac.leeds.ccg.io.IO_Cache;

/**
 * Class with a main method for running an agent-based model. Some model 
 * parameters be modified in the GUI. The model is comprised of an Environment
 * which contains: a grid of vegetation that grows with each iteration; a 
 * Grazer list - Agents that move randomly within the Environment and eat the
 * vegetation, grow or starve and may reproduce or die. The model outputs for
 * each iteration the number of grazers and counts of deaths and births.
 * 
 * @author Andy Turner
 * @version 1.0
 */
public class Model implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The command line arguments.
     */
    final String[] args;

    /**
     * For storing all the model parameters.
     */
    final Parameters parameters;

    /**
     * For storing all the model parameter constraints.
     */
    final ParameterConstraints parameterConstraints;
    
    /**
     * Environment
     */
    Environment environment;

    /**
     * Factory for creating data.
     */
    Grids_GridIntFactory factory;

    /**
     * Model Stats
     */
    final Stats stats;

    /**
     * The iteration.
     */
    int iteration;

    /**
     * @param args The command line arguments.
     */
    public Model(String[] args) {
        super();
        this.args = args;
        parameters = new Parameters();
        parameterConstraints = new ParameterConstraints();
        stats = new Stats();
    }

    /**
     * <ul>
     * <li>int randomSeed</li>
     * <li>int nIterations</li>
     * <li>int nrows</li>
     * <li>int ncols</li>
     * <li>int initialMaxVegetation</li>
     * <li>int initialNGrazers</li>
     * <li>int minSizeGrazer</li>
     * <li>int maxSizeGrazer</li>
     * </ul>
     */
    public void run() {
        try {
            long start = System.currentTimeMillis();
            if (args.length == 8) {
                parameters.randomSeed = Integer.parseInt(args[0]);
                parameters.nIterations = Integer.parseInt(args[1]);
                parameters.nrows = Integer.parseInt(args[2]);
                parameters.ncols = Integer.parseInt(args[3]);
                parameters.initialMaxVegetation = Integer.parseInt(args[4]);
                parameters.initialNGrazers = Integer.parseInt(args[5]);
                parameters.minSizeGrazer = Integer.parseInt(args[6]);
                parameters.maxSizeGrazer = Integer.parseInt(args[7]);
            } else {
                parameters.randomSeed = 1;
                parameters.nIterations = 100;
                parameters.nrows = 100;
                parameters.ncols = 100;
                parameters.initialMaxVegetation = 10;
                parameters.initialNGrazers = 1000;
                parameters.minSizeGrazer = 2;
                parameters.maxSizeGrazer = 9;
            }
            iteration = 0;
            Grids_Environment ge = new Grids_Environment();
            IO_Cache cache = new IO_Cache(ge.files.getDir(), "grids", (short) 100);
            factory = new Grids_GridIntFactory(ge, cache,
                    new Grids_ChunkIntFactorySinglet(0),
                    new Grids_ChunkIntFactoryArray(),
                    parameters.nrows, parameters.ncols);
            factory.stats = new Grids_GridIntStats(ge);
            GUI gui = new GUI(this);
            //Grids_ESRIAsciiGridExporter eage = new Grids_ESRIAsciiGridExporter(ge);
            Generic_Time.printTime(System.currentTimeMillis() - start);
        } catch (Exception | Error e) {
            e.printStackTrace(System.err);
        }
    }

    /**
     * Just calls the constructor. The arguments, if used, should be: java Model
     * numberOfAgents:int numberOfIterations:int eatingRate:double fullUp:double
     * fileIn:String fileOut:String
     *
     * @param args String sequence.
     */
    public static void main(String args[]) {
        try {
            Model model = new Model(args);
            model.run();
        } catch (Exception ex) {
            Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * For running the next iteration of the model.
     *
     * @param s The statistics about the model.
     * @return true if the model is running and false otherwise.
     */
    boolean iterate(Stats s) {
        if (iteration < parameters.nIterations) {
            environment.newGrazers = new ArrayList<>();
            environment.deadGrazers = new ArrayList<>();
            System.out.println("Iteration " + iteration);
            for (int j = 0; j < environment.grazers.size(); j++) {
                environment.grazers.get(j).act();
            }
            environment.grazers.addAll(environment.newGrazers);
            System.out.println("" + environment.grazers.size() + " grazers");
            // Deaths
            int deaths = environment.deadGrazers.size();
            System.out.println("" + deaths + " deaths");
            s.deaths += deaths;
            // Births
            int births = environment.newGrazers.size();
            System.out.println("" + births + " births");
            s.births += births;
            // Grow the vegetation
            environment.growVegetation();
            iteration++;
            return true;
        } else {
            return false;
        }
    }

    /**
     * A class for storing the model parameters.
     */
    public class Parameters {

        /**
         * The random seed.
         */
        int randomSeed;

        /**
         * The number of iterations.
         */
        int nIterations;

        /**
         * Initial maximum value in a cell of vegetation.
         */
        int initialMaxVegetation;

        /**
         * Initial number of grazers in model.
         */
        int initialNGrazers;

        /**
         * The number of rows.
         */
        int nrows;

        /**
         * The number of cols.
         */
        int ncols;

        /**
         * The maximum size for grazers.
         */
        int maxSizeGrazer;

        /**
         * The minimum size for grazers.
         */
        int minSizeGrazer;

        /**
         * Create a new instance.
         */
        public Parameters() {
        }

        /**
         * Create a new instance.
         *
         * @param randomSeed What {@link #randomSeed} is set to.
         * @param nIterations What {@link #nIterations} is set to.
         * @param initialMaxVegetation What {@link #initialMaxVegetation} is set to.
         * @param initialNGrazers What {@link #initialNGrazers} is set to.
         * @param nrows What {@link #nrows} is set to.
         * @param ncols What {@link #ncols} is set to.
         * @param maxSize What {@link #maxSizeGrazer} is set to.
         * @param minSize What {@link #minSizeGrazer} is set to.
         */
        public Parameters(int randomSeed, int nIterations, int initialMaxVegetation,
                int initialNGrazers, int nrows, int ncols, int maxSize, int minSize) {
            this.randomSeed = randomSeed;
            this.nIterations = nIterations;
            this.initialMaxVegetation = initialMaxVegetation;
            this.initialNGrazers = initialNGrazers;
            this.nrows = nrows;
            this.ncols = ncols;
            this.maxSizeGrazer = maxSize;
            this.minSizeGrazer = minSize;
        }
    }

    /**
     * A class for storing the model parameter constraints - the limits for the
     * parameter values.
     */
    public class ParameterConstraints {

        /**
         * Minimum number of iterations.
         */
        int minNIterations;

        /**
         * Maximum number of iterations.
         */
        int maxNIterations;

        /**
         * Minimum initial maximum value in a cell of vegetation.
         */
        int minInitialMaxVegetation;

        /**
         * Maximum initial maximum value in a cell of vegetation.
         */
        int maxInitialMaxVegetation;

        /**
         * Minimum initial number of grazers in model.
         */
        int minInitialNGrazers;

        /**
         * Maximum initial number of grazers in model.
         */
        int maxInitialNGrazers;

        /**
         * Minimum number of rows.
         */
        int minNrows;

        /**
         * Minimum number of rows.
         */
        int maxNrows;

        /**
         * Minimum number of cols.
         */
        int minNcols;

        /**
         * Minimum number of cols.
         */
        int maxNcols;

        /**
         * Minimum maximum size for grazers.
         */
        int minMaxSizeGrazer;

        /**
         * Maximum maximum size for grazers.
         */
        int maxMaxSizeGrazer;

        /**
         * Minimum minimum size for grazers.
         */
        int minMinSizeGrazer;

        /**
         * Maximum minimum size for grazers.
         */
        int maxMinSizeGrazer;

        /**
         * Create a new instance.
         */
        public ParameterConstraints() {
            minNIterations = 1;
            maxNIterations = 10000;
            minInitialMaxVegetation = 2;
            maxInitialMaxVegetation = 200000;
            minInitialNGrazers = 1;
            maxInitialNGrazers = 100000;
            minNrows = 1;
            maxNrows = 1000;
            minNcols = 1;
            maxNcols = 1000;
            minMaxSizeGrazer = 4;
            maxMaxSizeGrazer = 100;
            minMinSizeGrazer = 2;
            maxMinSizeGrazer = 10;
        }
    }

    /**
     * A class for storing the model statistics.
     */
    public class Stats {

        /**
         * The total number of births.
         */
        int births;

        /**
         * The total number of deaths.
         */
        int deaths;

        /**
         * Create a new instance.
         */
        public Stats() {
            births = 0;
            deaths = 0;
        }
    }
}
