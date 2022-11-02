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
 * A class for an spatial Agent positioned in 2D space in an Environment.
 * 
 * @version 1.0
 * @author Andy Turner
 */
public class Agent {

    /**
     * Reference to the environment set up in Model.
     */
    protected Environment environment;
    
    /**
     * X location coordinate.
     */
    protected Math_BigRational x;

    /**
     * Y location coordinate.
     */
    protected Math_BigRational y;

    /**
     * @param environment What {@link #environment} is set to.
     */
    public Agent(Environment environment) {
        this.environment = environment;
    }

    /**
     * @return col
     */
    public int getCol() {
        return (int) environment.vegetation.getCellID(x, y).getCol();
    }

    /**
     * @return row
     */
    public int getRow() {
        return (int) environment.vegetation.getCellID(x, y).getRow();
    }
}
