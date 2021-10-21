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

import static java.util.Objects.requireNonNull;

import java.util.function.Function;

import com.google.common.graph.ValueGraph;

public final class DefaultWeightedEdgesSelector<V, WE>
    implements PathWeightedEdgesBuilder<V, WE>
{

    private final ValueGraph<V, WE> graph;

    public DefaultWeightedEdgesSelector( ValueGraph<V, WE> graph )
    {
        this.graph = graph;
    }

    @Override
    public PathSourceSelector<V, WE, WE> whereEdgesAreWeights()
    {
        Function<WE, WE> identity = we -> we;
        return whereEdgesHaveWeights( identity );
    }

    public <W, M extends Function<WE, W>> PathSourceSelector<V, WE, W> whereEdgesHaveWeights( M weightedEdges )
    {
        weightedEdges = requireNonNull( weightedEdges, "Function to calculate edges weight can not be null." );
        return new DefaultPathSourceSelector<V, WE, W>( graph, weightedEdges );
    }

}
