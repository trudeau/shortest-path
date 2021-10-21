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

import org.nnsoft.trudeau.api.PathNotFoundException;
import org.nnsoft.trudeau.api.WeightedPath;
import org.nnsoft.trudeau.math.monoid.OrderedMonoid;

import com.google.common.graph.EndpointPair;

/**
 * Represents all shortest paths between all vertex pairs calculated by {@link FloydWarshall} algorithm.
 *
 * @param <V> the Graph vertices type
 * @param <WE> the Graph weighted edges type
 * @param <W> the weight type
 */
public final class AllVertexPairsShortestPath<V, WE, W>
{

    private final Map<EndpointPair<V>, WeightedPath<V, WE, W>> paths = new HashMap<EndpointPair<V>, WeightedPath<V, WE, W>>();

    private final Map<EndpointPair<V>, W> shortestDistances = new HashMap<EndpointPair<V>, W>();

    private final OrderedMonoid<W> weightOperations;

    /**
     * Constructor visible only inside the package
     */
    AllVertexPairsShortestPath( OrderedMonoid<W> weightOperations )
    {
        this.weightOperations = weightOperations;
    }

    /**
     * @param source
     * @param target
     * @param weightedPath
     */
    void addShortestPath( V source, V target, WeightedPath<V, WE, W> weightedPath )
    {
        source = requireNonNull( source, "Impossible to add a shortest path from a null source" );
        target = requireNonNull( target, "Impossible to add a shortest path to a null target" );
        weightedPath = requireNonNull( weightedPath, "Impossible to add a null weightedPath path to a null target" );

        paths.put( EndpointPair.ordered( source, target ), weightedPath );
    }

    /**
     * Returns the shortest path between source and target
     *
     * @param source The source Vertex
     * @param target The target Vertex
     * @return Returns the shortest path between source and target
     */
    public WeightedPath<V, WE, W> findShortestPath( V source, V target )
    {
        source = requireNonNull( source, "Impossible to add a shortest path from a null source" );
        target = requireNonNull( target, "Impossible to add a shortest path to a null target" );

        WeightedPath<V, WE, W> path = paths.get( EndpointPair.ordered( source, target ) );

        if ( path == null )
        {
            throw new PathNotFoundException( "Path from '%s' to '%s' doesn't exist", source, target );
        }

        return path;
    }

    /**
     * @param source
     * @param target
     * @param distance
     */
    void addShortestDistance( V source, V target, W distance )
    {
        source = requireNonNull( source, "Impossible to add a shortest path from a null source" );
        target = requireNonNull( target, "Impossible to add a shortest path to a null target" );
        distance = requireNonNull( distance, "Impossible to add a shortest distance with a null distance" );

        shortestDistances.put( EndpointPair.ordered( source, target ), distance );
    }

    /**
     * Returns the shortest distance between source and target.
     *
     * @param source The source Vertex
     * @param target The target Vertex
     * @return Returns the shortest distance between source and target.
     */
    W getShortestDistance( V source, V target )
    {
        source = requireNonNull( source, "Impossible to add a shortest path from a null source" );
        target = requireNonNull( target, "Impossible to add a shortest path to a null target" );

        if ( source.equals( target ) )
        {
            return weightOperations.identity();
        }

        return shortestDistances.get( EndpointPair.ordered( source, target ) );
    }

    /**
     * Checks if there is a shortest distance between source and target.
     *
     * @param source The source Vertex
     * @param target The target Vertex
     * @return Returns true if there is a shortest distance between source and target, false otherwise.
     */
    boolean hasShortestDistance( V source, V target )
    {
        source = requireNonNull( source, "Impossible to add a shortest path from a null source" );
        target = requireNonNull( target, "Impossible to add a shortest path to a null target" );

        if ( source.equals( target ) )
        {
            return true;
        }

        return shortestDistances.containsKey( EndpointPair.ordered( source, target ) );
    }

    @Override
    public String toString()
    {
        return shortestDistances.toString();
    }

}
