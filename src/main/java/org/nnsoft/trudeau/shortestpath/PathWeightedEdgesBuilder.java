package org.nnsoft.trudeau.shortestpath;

import java.util.function.Function;

// TODO find a better name
public interface PathWeightedEdgesBuilder<V, WE>
{

    // TODO find a better sentence
    <W, M extends Function<WE, W>> PathSourceSelector<V, WE, W> whereEdgesHaveWeights( M weightedEdges );

    PathSourceSelector<V, WE, WE> whereEdgesAreWeights();

}
