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

import org.nnsoft.trudeau.api.PathNotFoundException;
import org.nnsoft.trudeau.api.PredecessorsList;
import org.nnsoft.trudeau.api.WeightedPath;
import org.nnsoft.trudeau.math.monoid.OrderedMonoid;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.ValueGraph;

final class DefaultTargetSourceSelector<V, WE, W>
    implements TargetSourceSelector<V, WE, W>
{

    private final ValueGraph<V, WE> graph;

    private final Function<WE, W> weightedEdges;

    private final V source;

    public DefaultTargetSourceSelector( ValueGraph<V, WE> graph, Function<WE, W> weightedEdges, V source )
    {
        this.graph = graph;
        this.weightedEdges = weightedEdges;
        this.source = source;
    }

    /**
     * {@inheritDoc}
     */
    public <WO extends OrderedMonoid<W>> AllVertexPairsShortestPath<V, WE, W> applyingBelmannFord( WO weightOperations )
    {
        weightOperations = requireNonNull( weightOperations, "Belmann-Ford algorithm can not be applied using null weight operations" );

        final ShortestDistances<V, W> shortestDistances = new ShortestDistances<V, W>( weightOperations );
        shortestDistances.setWeight( source, weightOperations.identity() );

        final PredecessorsList<V, WE, W> predecessors = new PredecessorsList<V, WE, W>( graph, weightOperations, weightedEdges );

        for ( int i = 0; i < graph.getOrder(); i++ )
        {
            for ( EndpointPair<V> edge : graph.edges() )
            {
                V u = edge.nodeU();
                V v = edge.nodeV();

                if ( shortestDistances.alreadyVisited( u ) )
                {
                    WE we = graph.edgeValue( u, v ).get();
                    W shortDist = weightOperations.append( shortestDistances.getWeight( u ), weightedEdges.apply( we ) );

                    if ( !shortestDistances.alreadyVisited( v )
                            || weightOperations.compare( shortDist, shortestDistances.getWeight( v ) ) < 0 )
                    {
                        // assign new shortest distance and mark unsettled
                        shortestDistances.setWeight( v, shortDist );

                        // assign predecessor in shortest path
                        predecessors.addPredecessor( v, u );
                    }
                }
            }
        }

        for ( EndpointPair<V> edge : graph.edges() )
        {
            V u = edge.nodeU();
            V v = edge.nodeV();

            if ( shortestDistances.alreadyVisited( u ) )
            {
                WE we = graph.edgeValue( u, v ).get();
                W shortDist = weightOperations.append( shortestDistances.getWeight( u ), weightedEdges.apply( we ) );

                if ( !shortestDistances.alreadyVisited( v )
                        || weightOperations.compare( shortDist, shortestDistances.getWeight( v ) ) < 0 )
                {
                    // TODO it would be nice printing the cycle
                    throw new NegativeWeightedCycleException( "Graph contains a negative-weight cycle in vertex %s",
                                                              v, graph );
                }
            }
        }

        AllVertexPairsShortestPath<V, WE, W> allVertexPairsShortestPath = new AllVertexPairsShortestPath<V, WE, W>( weightOperations );

        for ( V target : graph.nodes() )
        {
            if ( !source.equals( target ) )
            {
                try
                {
                    WeightedPath<V, WE, W> weightedPath = predecessors.buildPath( source, target );
                    allVertexPairsShortestPath.addShortestPath( source, target, weightedPath );
                }
                catch ( PathNotFoundException e )
                {
                    continue;
                }
            }
        }

        return allVertexPairsShortestPath;
    }

    /**
     * {@inheritDoc}
     */
    public <T extends V> ShortestPathAlgorithmSelector<V, WE, W> to( T target )
    {
        target = requireNonNull( target, "Shortest path can not be calculated to a null target" );
        return new DefaultShortestPathAlgorithmSelector<V, WE, W>( graph, weightedEdges, source, target );
    }

}
