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

import static org.nnsoft.trudeau.utils.Assertions.checkNotNull;

import org.nnsoft.trudeau.api.Graph;

public final class ShortestPathSolver
{

    /**
     * Find the sortest on the input {@link Graph}
     *
     * @param <V> the Graph vertices type
     * @param <WE> the Graph edges type
     * @param <G> the Graph type
     * @param graph the input edge-weighted graph
     * @return the caluculated the sortest
     */
    public static <V, WE, G extends Graph<V, WE>> PathWeightedEdgesBuilder<V, WE> findShortestPath( G graph )
    {
        graph = checkNotNull( graph, "Shortest path can not be calculated on null graph" );
        return new DefaultWeightedEdgesSelector<V, WE>( graph );
    }

    private ShortestPathSolver()
    {
        // do nothing
    }

}
