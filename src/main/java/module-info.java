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

/**
 * Module for Agent Based Modelling
 */
module uk.ac.leeds.ccg.abm {
    //requires transitive java.logging;
    //requires transitive java.desktop;
    
    /**
     * The ccg-generic library.
     */
    requires transitive uk.ac.leeds.ccg.generic;
    
    /**
     * The ccg-io library.
     */
    requires transitive uk.ac.leeds.ccg.io;
    
    /**
     * The ccg-math library.
     */
    requires transitive uk.ac.leeds.ccg.math;
    
    /**
     * The ccg-grids library.
     */
    requires transitive uk.ac.leeds.ccg.grids;
    
    /**
     * The ccg-data library.
     */
    requires transitive uk.ac.leeds.ccg.data;
    
    /**
     * Exports.
     */
    exports uk.ac.leeds.ccg.abm;
}
