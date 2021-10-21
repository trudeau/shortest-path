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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.nnsoft.trudeau.api.PredecessorsList;
import org.nnsoft.trudeau.api.WeightedPath;
import org.nnsoft.trudeau.math.monoid.OrderedMonoid;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.ValueGraph;

final class DefaultPathSourceSelector<V, WE, W>
    implements PathSourceSelector<V, WE, W>
{

    private final ValueGraph<V, WE> graph;

    private final Function<WE, W> weightedEdges;

    public DefaultPathSourceSelector( ValueGraph<V, WE> graph, Function<WE, W> weightedEdges )
    {
        this.graph = graph;
        this.weightedEdges = weightedEdges;
    }

    /**
     * {@inheritDoc}
     */
    public <WO extends OrderedMonoid<W>> AllVertexPairsShortestPath<V, WE, W> applyingFloydWarshall( WO weightOperations )
    {
        weightOperations = requireNonNull( weightOperations, "Floyd-Warshall algorithm can not be applied using null weight operations" );

        AllVertexPairsShortestPath<V, WE, W> shortestPaths = new AllVertexPairsShortestPath<V, WE, W>( weightOperations );
        Map<EndpointPair<V>, V> next = new HashMap<EndpointPair<V>, V>();

        // init
        for ( EndpointPair<V> e : graph.edges() )
        {
            WE we = graph.edgeValue( e.source(), e.target() ).get();
            W weight = weightedEdges.apply( we );

            shortestPaths.addShortestDistance( e.source(), e.target(), weight );

            if ( !graph.isDirected() )
            {
                shortestPaths.addShortestDistance( e.target(), e.source(), weight );
            }
        }

        // run the Floyd-Warshall algorithm.
        for ( V k : graph.nodes() )
        {
            for ( V i : graph.nodes() )
            {
                for ( V j : graph.nodes() )
                {
                    if ( shortestPaths.hasShortestDistance( i, k ) && shortestPaths.hasShortestDistance( k, j ) )
                    {
                        W newDistance = weightOperations.append( shortestPaths.getShortestDistance( i, k ), shortestPaths.getShortestDistance( k, j ) );
                        if ( !shortestPaths.hasShortestDistance( i, j )
                                || weightOperations.compare( newDistance, shortestPaths.getShortestDistance( i, j ) ) < 0 )
                        {
                            shortestPaths.addShortestDistance( i, j, newDistance );

                            // store the intermediate vertex
                            next.put( EndpointPair.ordered( i, j ), k );
                        }
                    }

                }
            }
        }

        // fills all WeightedPaths
        for ( V source : graph.nodes() )
        {
            for ( V target : graph.nodes() )
            {
                if ( !source.equals( target ) )
                {
                    PredecessorsList<V, WE, W> predecessorsList = new PredecessorsList<V, WE, W>( graph, weightOperations, weightedEdges );

                    pathReconstruction( predecessorsList, source, target, next );
                    if ( !predecessorsList.isEmpty() )
                    {
                        WeightedPath<V, WE, W> weightedPath = predecessorsList.buildPath( source, target );
                        if ( weightedPath.getOrder() > 0 )
                        {
                            shortestPaths.addShortestPath( source, target, weightedPath );
                        }
                    }
                }
            }
        }

        return shortestPaths;
    }

    private void pathReconstruction( PredecessorsList<V, WE, W> path,
                                     V source, V target,
                                     Map<EndpointPair<V>, V> next )
    {
        V k = next.get( EndpointPair.ordered( source, target ) );
        if ( k == null )
        {
            // there is a direct path between a and b
            Optional<WE> edge = graph.edgeValue( source, target );
            if ( edge.isPresent() )
            {
                path.addPredecessor( target, source );
            }
        }
        else
        {
            pathReconstruction( path, source, k, next );
            pathReconstruction( path, k, target, next );
        }
    }

    /**
     * {@inheritDoc}
     */
    public <H extends V> TargetSourceSelector<V, WE, W> from( H source )
    {
        source = requireNonNull( source, "Shortest path can not be calculated from a null source" );
        return new DefaultTargetSourceSelector<V, WE, W>( graph, weightedEdges, source );
    }

}