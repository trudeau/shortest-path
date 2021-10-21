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
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;

import org.nnsoft.trudeau.api.PathNotFoundException;
import org.nnsoft.trudeau.api.PredecessorsList;
import org.nnsoft.trudeau.api.WeightedPath;
import org.nnsoft.trudeau.math.monoid.OrderedMonoid;

import com.google.common.graph.ValueGraph;

final class DefaultShortestPathAlgorithmSelector<V, WE, W>
    implements ShortestPathAlgorithmSelector<V, WE, W>
{

    private final ValueGraph<V, WE> graph;

    private final Function<WE, W> weightedEdges;

    private final V source;

    private final V target;

    public DefaultShortestPathAlgorithmSelector( ValueGraph<V, WE> graph, Function<WE, W> weightedEdges, V source, V target )
    {
        this.graph = graph;
        this.weightedEdges = weightedEdges;
        this.source = source;
        this.target = target;
    }

    /**
     * {@inheritDoc}
     */
    public <WO extends OrderedMonoid<W>> HeuristicBuilder<V, WE, W> applyingAStar( WO weightOperations )
    {
        weightOperations = requireNonNull( weightOperations, "A* algorithm can not be applied using null weight operations" );
        return new DefaultHeuristicBuilder<V, WE, W>( graph, weightedEdges, source, target, weightOperations );
    }

    /**
     * {@inheritDoc}
     */
    public <WO extends OrderedMonoid<W>> WeightedPath<V, WE, W> applyingDijkstra( WO weightOperations )
    {
        weightOperations = requireNonNull( weightOperations, "Dijkstra algorithm can not be applied using null weight operations" );

        final ShortestDistances<V, W> shortestDistances = new ShortestDistances<V, W>( weightOperations );
        shortestDistances.setWeight( source, weightOperations.identity() );

        final Queue<V> unsettledNodes = new PriorityQueue<V>( shortestDistances );
        unsettledNodes.add( source );

        final Set<V> settledNodes = new HashSet<V>();

        final PredecessorsList<V, WE, W> predecessors = new PredecessorsList<V, WE, W>( graph, weightOperations, weightedEdges );

        // extract the node with the shortest distance
        while ( !unsettledNodes.isEmpty() )
        {
            V vertex = unsettledNodes.remove();

            // destination reached, stop and build the path
            if ( target.equals( vertex ) )
            {
                return predecessors.buildPath( source, target );
            }

            settledNodes.add( vertex );

            for ( V v : graph.successors( vertex ) )
            {
                // skip node already settled
                if ( !settledNodes.contains( v ) )
                {
                    WE edge = graph.edgeValue( vertex, v ).get();
                    if ( shortestDistances.alreadyVisited( vertex ) )
                    {
                        W shortDist = weightOperations.append( shortestDistances.getWeight( vertex ), weightedEdges.apply( edge ) );

                        if ( !shortestDistances.alreadyVisited( v )
                                || weightOperations.compare( shortDist, shortestDistances.getWeight( v ) ) < 0 )
                        {
                            // assign new shortest distance and mark unsettled
                            shortestDistances.setWeight( v, shortDist );
                            unsettledNodes.add( v );

                            // assign predecessor in shortest path
                            predecessors.addPredecessor( v, vertex );
                        }
                    }

                }
            }
        }

        throw new PathNotFoundException( "Path from '%s' to '%s' doesn't exist in Graph '%s'", source, target, graph );
    }

    /**
     * {@inheritDoc}
     */
    public <WO extends OrderedMonoid<W>> WeightedPath<V, WE, W> applyingBidirectionalDijkstra( WO weightOperations )
    {
        weightOperations = requireNonNull( weightOperations, "Bidirectional Dijkstra algorithm can not be applied using null weight operations" );

        final ShortestDistances<V, W> shortestDistancesForward = new ShortestDistances<V, W>( weightOperations );
        shortestDistancesForward.setWeight( source, weightOperations.identity() );

        final ShortestDistances<V, W> shortestDistancesBackwards = new ShortestDistances<V, W>( weightOperations );
        shortestDistancesBackwards.setWeight( target, weightOperations.identity() );

        final Queue<V> openForward = new PriorityQueue<V>( shortestDistancesForward );
        openForward.add( source );

        final Queue<V> openBackwards = new PriorityQueue<V>( shortestDistancesBackwards );
        openBackwards.add( target );

        final Set<V> closedForward = new HashSet<V>();

        final Set<V> closedBackwards = new HashSet<V>();

        final PredecessorsList<V, WE, W> predecessorsForward = new PredecessorsList<V, WE, W>( graph, weightOperations, weightedEdges );

        final PredecessorsList<V, WE, W> predecessorsBackwards = new PredecessorsList<V, WE, W>( graph, weightOperations, weightedEdges );

        W best = null;
        V touch = null;

        while (!openForward.isEmpty() && !openBackwards.isEmpty())
        {
            if ( best != null )
            {
                final W tmp = weightOperations.append( shortestDistancesForward.getWeight( openForward.peek() ),
                                                       shortestDistancesBackwards.getWeight( openBackwards.peek() ) );

                if ( weightOperations.compare( tmp, best ) >= 0 )
                {
                    return predecessorsForward.buildPath( source, touch, target, predecessorsBackwards );
                }
            }

            V vertex = openForward.remove();

            closedForward.add( vertex );

            for ( V v : graph.successors( vertex ) )
            {
                if ( !closedForward.contains( v ) )
                {
                    WE edge = graph.edgeValue( vertex, v ).get();
                    if ( shortestDistancesForward.alreadyVisited( vertex ) )
                    {
                        W shortDist = weightOperations.append( shortestDistancesForward.getWeight( vertex ), weightedEdges.apply( edge ) );

                        if ( !shortestDistancesForward.alreadyVisited( v )
                                || weightOperations.compare( shortDist, shortestDistancesForward.getWeight( v ) ) < 0 )
                        {
                            shortestDistancesForward.setWeight( v, shortDist );
                            openForward.add( v );
                            predecessorsForward.addPredecessor( v, vertex );

                            if ( closedBackwards.contains( v ) )
                            {
                                W tmpBest = weightOperations.append( shortDist, shortestDistancesBackwards.getWeight( v ) );

                                if ( best == null || weightOperations.compare( tmpBest, best ) < 0 )
                                {
                                    best = tmpBest;
                                    touch = v;
                                }
                            }
                        }
                    }
                }
            }

            vertex = openBackwards.remove();

            closedBackwards.add( vertex );

            Iterable<V> parentsIterable = graph.isDirected() ? graph.predecessors( vertex ) : graph.adjacentNodes( vertex );
            for ( V v : parentsIterable )
            {
                if ( !closedBackwards.contains( v ) )
                {
                    WE edge = graph.edgeValue( v, vertex ).get();
                    if ( shortestDistancesBackwards.alreadyVisited( vertex ) )
                    {
                        W shortDist = weightOperations.append( shortestDistancesBackwards.getWeight( vertex ), weightedEdges.apply( edge ) );

                        if ( !shortestDistancesBackwards.alreadyVisited( v )
                                || weightOperations.compare( shortDist, shortestDistancesBackwards.getWeight( v ) ) < 0 )
                        {
                            shortestDistancesBackwards.setWeight( v, shortDist );
                            openBackwards.add( v );
                            predecessorsBackwards.addPredecessor( v, vertex );

                            if ( closedForward.contains( v ) )
                            {
                                W tmpBest = weightOperations.append( shortDist, shortestDistancesForward.getWeight( v ) );

                                if ( best == null || weightOperations.compare( tmpBest, best ) < 0 )
                                {
                                    best = tmpBest;
                                    touch = v;
                                }
                            }
                        }
                    }
                }
            }
        }

        if ( touch == null )
        {
            throw new PathNotFoundException( "Path from '%s' to '%s' doesn't exist in Graph '%s'", source, target, graph);
        }

        return predecessorsForward.buildPath( source, touch, target, predecessorsBackwards );
    }
}
