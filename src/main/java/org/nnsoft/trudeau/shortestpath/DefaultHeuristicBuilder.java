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

import java.util.HashSet;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;

import org.nnsoft.trudeau.api.PathNotFoundException;
import org.nnsoft.trudeau.api.PredecessorsList;
import org.nnsoft.trudeau.api.WeightedPath;
import org.nnsoft.trudeau.math.monoid.OrderedMonoid;

import com.google.common.graph.ValueGraph;

final class DefaultHeuristicBuilder<V, WE, W>
    implements HeuristicBuilder<V, WE, W>
{

    private final ValueGraph<V, WE> graph;

    private final Function<WE, W> weightedEdges;

    private final V start;

    private final V goal;

    private final OrderedMonoid<W> weightOperations;

    public DefaultHeuristicBuilder( ValueGraph<V, WE> graph, Function<WE, W> weightedEdges, V source, V target, OrderedMonoid<W> weightOperations )
    {
        this.graph = graph;
        this.weightedEdges = weightedEdges;
        this.start = source;
        this.goal = target;
        this.weightOperations = weightOperations;
    }

    /**
     * {@inheritDoc}
     */
    public <H extends Heuristic<V, W>> WeightedPath<V, WE, W> withHeuristic( H heuristic )
    {
        heuristic = requireNonNull( heuristic, "A* algorithm can not be applied using a null heuristic" );

        // Cost from start along best known path.
        final ShortestDistances<V, W> gScores = new ShortestDistances<V, W>( weightOperations );
        gScores.setWeight( start, weightOperations.identity() );

        // Estimated total cost from start to goal through y.
        final ShortestDistances<V, W> fScores = new ShortestDistances<V, W>( weightOperations );
        W hScore = heuristic.applyHeuristic( start, goal );
        fScores.setWeight( start, hScore );

        // The set of nodes already evaluated.
        final Set<V> closedSet = new HashSet<V>();

        // The set of tentative nodes to be evaluated.
        final Queue<V> openSet = new PriorityQueue<V>( fScores );
        openSet.add( start );

        // The of navigated nodes
        final PredecessorsList<V, WE, W> predecessors = new PredecessorsList<V, WE, W>( graph, weightOperations, weightedEdges );

        // extract the node in openset having the lowest f_score[] value
        while ( !openSet.isEmpty() )
        {
            V current = openSet.remove();

            // destination reached, stop and build the path
            if ( goal.equals( current ) )
            {
                return predecessors.buildPath( start, goal );
            }

            closedSet.add( current );

            Iterable<V> connected = graph.isDirected() ? graph.successors( current ) : graph.adjacentNodes( current ) ;
            for ( V v : connected )
            {
                if ( !closedSet.contains( v ) )
                {
                    // no needs to check, the edge exists
                    Optional<WE> edge = graph.edgeValue( current, v );
                    // note that the weight of current can never be undefined
                    W tentativeGScore = weightOperations.append( gScores.getWeight( current ), weightedEdges.apply( edge.get() ) );

                    // if the first condition fails, v has already been visited (its weight is defined)
                    if ( openSet.add( v ) || weightOperations.compare( tentativeGScore, gScores.getWeight( v ) ) < 0 )
                    {
                        predecessors.addPredecessor( v, current );
                        gScores.setWeight( v, tentativeGScore );
                        hScore = heuristic.applyHeuristic( v, goal );
                        fScores.setWeight( v, weightOperations.append( gScores.getWeight( v ), hScore ) );
                    }
                }
            }
        }

        throw new PathNotFoundException( "Path from '%s' to '%s' doesn't exist in Graph '%s'", start, goal, graph );
    }

}
