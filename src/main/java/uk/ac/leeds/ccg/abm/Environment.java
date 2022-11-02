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

import java.awt.Color;
import java.awt.Image;
import java.awt.Panel;
import java.awt.image.MemoryImageSource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.leeds.ccg.abm.Model.Parameters;
import uk.ac.leeds.ccg.grids.d2.grid.Grids_Dimensions;
import uk.ac.leeds.ccg.grids.d2.grid.i.Grids_GridInt;
import uk.ac.leeds.ccg.grids.d2.grid.i.Grids_GridIntFactory;
import uk.ac.leeds.ccg.grids.d2.stats.Grids_StatsInt;
import uk.ac.leeds.ccg.math.number.Math_BigRational;

/**
 * The environment contains lists of grazers and a raster of vegetation cover
 * for a square celled region. The vegetation cover grows by a fixed amount each
 * iteration. The grazers eat the vegetation, move, multiply and starve/die.
 *
 * @version 1.0
 * @author Andy Turner
 */
public class Environment {

    /**
     * The model parameters.
     */
    public final Parameters parameters;

    /**
     * The random instance for randomness.
     */
    public final Random random;

    /**
     * Existing grazers.
     */
    public final ArrayList<Grazer> grazers;

    /**
     * Dead grazers.
     */
    public ArrayList<Grazer> deadGrazers;

    /**
     * New grazers.
     */
    public ArrayList<Grazer> newGrazers;

    /**
     * The number of columns as stored in {@link #parameters}.
     */
    public final int ncols;

    /**
     * The number of rows as stored in {@link #parameters}.
     */
    public final int nrows;

    /**
     * The minimum x coordinate.
     */
    public final Math_BigRational xmin;

    /**
     * The maximum x coordinate.
     */
    public final Math_BigRational xmax;

    /**
     * The minimum y coordinate.
     */
    public final Math_BigRational ymin;

    /**
     * The maximum y coordinate.
     */
    public final Math_BigRational ymax;

    /**
     * The vegetation raster data.
     */
    public Grids_GridInt vegetation;

    /**
     * Create a new instance.
     *
     * @param parameters Parameters.
     * @param factory Grids_GridIntFactory.
     */
    public Environment(Parameters parameters, Grids_GridIntFactory factory) {
        this.parameters = parameters;
        random = new Random();
        random.setSeed(parameters.randomSeed);
        this.nrows = parameters.nrows;
        this.ncols = parameters.ncols;
        Grids_Dimensions d = new Grids_Dimensions(nrows, ncols);
        xmin = d.getXMin();
        xmax = d.getXMax();
        ymin = d.getYMin();
        ymax = d.getYMax();
        // Initialise vegetation cover
        try {
            vegetation = factory.create(nrows, ncols, d);
            for (int i = 0; i < nrows; i++) {
                for (int j = 0; j < ncols; j++) {
                    vegetation.setCell(i, j, BigDecimal.valueOf(random.nextInt(9)));
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        System.out.println(vegetation.getStats());
        // Initialise grazers
        grazers = new ArrayList<>();
        for (int i = 0; i < parameters.initialNGrazers; i++) {
            int row = random.nextInt(nrows);
            int col = random.nextInt(ncols);
            grazers.add(new Grazer(this, row, col, parameters.minSizeGrazer,
                    parameters.maxSizeGrazer));
        }
    }

    /**
     * Grow vegetation.
     */
    public void growVegetation() {
        try {
            for (int i = 0; i < nrows; i++) {
                for (int j = 0; j < ncols; j++) {
                    vegetation.addToCell(i, j, 1);
                }
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Environment.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Environment.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @param min The minimum value.
     * @param max The maximum value.
     * @return A copy of {@link #data} with values rescaled into the range
     * [{@code min}, {@code max}].
     */
    int[] get1DRescaledData(int min, int max) throws Exception {
        Grids_StatsInt stats = vegetation.getStats();
        System.out.println(stats);
        int minv = stats.getMin(true);
        int maxv = stats.getMax(false);
        System.out.println(stats);
        int rangev = maxv - minv;
        int range = max - min;
        int[] r = new int[nrows * ncols];
        for (int i = 0; i < nrows; i++) {
            for (int j = 0; j < ncols; j++) {
                int v = (int) (((vegetation.getCell(i, j) - minv)
                        / (double) rangev) * range) + min;
                if (v < 0) {
                    v = 0;
                }
                if (v > 255) {
                    v = 255;
                }
                r[(i * ncols) + j] = v;
            }
        }
        return r;
    }

    /**
     * Gets current data as an image.
     *
     * @return {@link #get1DRescaledData(int, int)} as an Image. This may return
     * {@code null}.
     * @throws Exception If encountered.
     */
    public Image getDataAsImage() throws Exception {
        if (vegetation == null) {
            return null;
        }
        int[] data1Dreranged = get1DRescaledData(0, 255);
        // Convert to int.
        int l = data1Dreranged.length;
        int[] pix = new int[l];
        for (int i = 0; i < pix.length; i++) {
            int v = data1Dreranged[i];
            Color c = new Color(v, v, v);
            pix[i] = c.getRGB();
        }
        MemoryImageSource m = new MemoryImageSource(ncols, nrows, pix, 0, ncols);
        Panel panel = new Panel();
        Image image = panel.createImage(m);
        //image = image.getScaledInstance(multiplier * 100, multiplier * 100, Image.SCALE_DEFAULT);
        
        return image;
    }

}
