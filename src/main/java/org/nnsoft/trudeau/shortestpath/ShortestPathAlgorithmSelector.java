package org.nnsoft.trudeau.shortestpath;

/*
 *   Copyright 2013 The Trudeau Project
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

import org.nnsoft.trudeau.api.WeightedPath;
import org.nnsoft.trudeau.inmemory.PathNotFoundException;
import org.nnsoft.trudeau.math.monoid.OrderedMonoid;

/**
 *
 *
 * @param <V> the Graph vertices type.
 * @param <WE> the Graph weighted edges type
 * @param <W> the weight type
 */
public interface ShortestPathAlgorithmSelector<V, WE, W>
{

    /**
     *  Calculates the shortest path using the A* algorithm.
     *
     * @param <WO> the type of weight operations
     * @param weightOperations the class responsible for operations on weights
     * @return
     */
    <WO extends OrderedMonoid<W>> HeuristicBuilder<V, WE, W> applyingAStar( WO weightOperations );

    /**
     *  Calculates the shortest path using Dijkstra's algorithm.
     *
     * @param <WO> the type of weight operations
     * @param weightOperations the class responsible for operations on weights
     * @return a path which describes the shortest path, if any, otherwise a {@link PathNotFoundException} will be thrown
     */
    <WO extends OrderedMonoid<W>> WeightedPath<V, WE, W> applyingDijkstra( WO weightOperations );

    /**
     *  Calculates the shortest path using bidirectional Dijkstra's algorithm.
     *
     * @param <WO> the type of weight operations
     * @param weightOperations the class responsible for operations on weights
     * @return a path which describes the shortest path, if any, otherwise a {@link PathNotFoundException} will be thrown
     */
    <WO extends OrderedMonoid<W>> WeightedPath<V, WE, W> applyingBidirectionalDijkstra( WO weightOperations );

}
