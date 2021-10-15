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

import uk.ac.leeds.ccg.math.number.Math_BigRational;

/**
 * A Grazer can move, eat, reproduce and die.
 *
 * @author Andy Turner
 * @version 1.0
 */
public class Grazer extends Agent {

    /**
     * The minimum size for the grazer.
     */
    int minSize;
    
    /**
     * The maximum size for the grazer.
     */
    int maxSize;
    
    /**
     * Size influences how much the grazer can eat and whether or not they
     * reproduce. The size is between {@link #minSize} and {@link #maxSize}.
     * If the grazer reaches {@link #maxSize} with a full store (i.e. when 
     * {@link #store} equals {@link #size}), then the grazer reproduces.
     */
    int size;

    /**
     * Store influences the amount the grazer can eat and whether or not they
     * die or can reproduce.
     */
    int store;

    /**
     * Randomly set {@link #size} to a number in the range [minSize, maxSize).
     *
     * @param environment What {@link #environment} is set to.
     * @param grazers What {@link #grazers} is set to.
     * @param random What {@link #random} is set to.
     * @param row The initial row location of the new grazer.
     * @param col The initial col location of the new grazer.
     * @param minSize What {@link #minSize} is set to.
     * @param maxSize What {@link #maxSize} is set to.
     */
    public Grazer(Environment environment, int row, int col, int minSize, 
            int maxSize) {
        this(environment, environment.vegetation.getCellX(col),
                environment.vegetation.getCellY(row), minSize, maxSize,
                environment.random.nextInt(minSize, maxSize));
    }

    /**
     * Randomly set {@link #store} to a number in the range [size/2, size).
     *
     * @param environment What {@link #environment} is set to.
     * @param random What {@link #random} is set to.
     * @param x What {@link #x} is set to.
     * @param y What {@link #y} is set to.
     * @param minSize What {@link #minSize} is set to.
     * @param maxSize What {@link #maxSize} is set to.
     * @param size What {@link #size} is set to.
     */
    public Grazer(Environment environment, Math_BigRational x,
            Math_BigRational y, int minSize, int maxSize, int size) {
        this(environment, x, y, minSize, maxSize, size,
                environment.random.nextInt(size / 2, size));
    }

    /**
     * Create a new instance.
     *
     * @param environment What {@link #environment} is set to.
     * @param grazers What {@link #grazers} is set to.
     * @param x What {@link #x} is set to.
     * @param y What {@link #y} is set to.
     * @param minSize What {@link #minSize} is set to.
     * @param maxSize What {@link #maxSize} is set to.
     * @param size What {@link #size} is set to.
     * @param store What {@link #store} is set to.
     */
    public Grazer(Environment environment, Math_BigRational x,
            Math_BigRational y, int minSize, int maxSize, int size, int store) {
        super(environment);
        this.x = x;
        this.y = y;
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.size = size;
        this.store = store;
    }

    /**
     * <ul>
     * <li>eat</li>
     * <li>grow</li>
     * <li>move</li>
     * </ul>
     */
    public void act() {
        eat();
        grow();
        move();
    }

    /**
     * If the grazer is full size and store is full, then it reproduces. If the
     * store has reached the size, then the size increases by 1 and the store
     * reduces in size.
     */
    private void grow() {
        if (size == maxSize && store == maxSize) {
            size /= 2;
            store /= 3;
            //System.out.println("Split");
            environment.newGrazers.add(new Grazer(environment, x, y, minSize,
                    maxSize, size, store));
        }
        if (store == size) {
            size++;
            store /= 2;
            //System.out.println("Growth");
        }
    }

    /**
     * Move randomly to a new position or stay still. About 60% of the time, the
     * grazer moves in the x direction; about 60% of the time, the grazer moves
     * in the y direction. Any movement incurs a store cost.
     */
    private void move() {
        double rn = environment.random.nextInt(10);
        if (rn < 3) {
            x = x.subtract(Math_BigRational.ONE);
            if (x.compareTo(environment.xmin) == -1) {
                x = environment.xmin;
            }
            store--;
        } else if (rn > 6) {
            x = x.add(Math_BigRational.ONE);
            if (x.compareTo(environment.xmax) == 1) {
                x = environment.xmax;
            }
            store--;
        }
        rn = environment.random.nextInt(10);
        if (rn < 3) {
            y = y.subtract(Math_BigRational.ONE);
            if (y.compareTo(environment.ymin) == -1) {
                y = environment.ymin;
            }
            store--;
        } else if (rn > 6) {
            y = y.add(Math_BigRational.ONE);
            if (y.compareTo(environment.ymax) == 1) {
                y = environment.ymax;
            }
            store--;
        }
    }

    /**
     * Checks the value of the vegetation at the location eats some or all of
     * it. If there is no vegetation to eat then the grazers starves and loses
     * store. If the grazer store is empty, then it shrinks. If the grazer
     * shrinks to size 0 then it dies.
     */
    private void eat() {
        try {
            int v = environment.vegetation.getCell(x, y);
            int capacity = size - store;
            if (v > 0) {
                if (v >= capacity) {
                    environment.vegetation.setCell(x, y, v - capacity);
                    store += capacity;
                } else {
                    environment.vegetation.setCell(x, y, 0);
                    store += v;
                }
            } else {
                if (store > 0) {
                    store--;
                    //System.out.println("Starve");
                } else {
                    size--;
                    //System.out.println("Shrink");
                    if (size == 0) {
                        //System.out.println("Death");
                        environment.grazers.remove(this);
                        environment.deadGrazers.add(this);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

}
