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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.nnsoft.trudeau.shortestpath.ShortestPathSolver.findShortestPath;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.junit.Test;
import org.nnsoft.trudeau.api.InMemoryWeightedPath;
import org.nnsoft.trudeau.api.Path;
import org.nnsoft.trudeau.api.PathNotFoundException;
import org.nnsoft.trudeau.math.monoid.primitive.DoubleWeightBaseOperations;

import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;

public final class AStarTestCase
{

    @Test( expected = NullPointerException.class )
    public void testNullGraph()
    {
        findShortestPath( null )
            .whereEdgesAreWeights()
            .from( null )
            .to( null )
            .applyingAStar( null )
            .withHeuristic( null );
    }

    @Test( expected = NullPointerException.class )
    public void testNullVertices()
    {
        final MutableValueGraph<String, Double> graph = ValueGraphBuilder.undirected().build();

        findShortestPath( graph )
            .whereEdgesAreWeights()
            .from( null )
            .to( null )
            .applyingAStar( new DoubleWeightBaseOperations() )
            .withHeuristic( null );
    }

    @Test( expected = NullPointerException.class )
    public void testNullHeuristic()
    {
        final MutableValueGraph<String, Double> graph = ValueGraphBuilder.undirected().build();

        findShortestPath( graph )
            .whereEdgesAreWeights()
            .from( "a" )
            .to( "b" )
            .applyingAStar( new DoubleWeightBaseOperations() )
            .withHeuristic( null );
    }

    @Test( expected = NullPointerException.class )
    public void testNullMonoid()
    {
        final MutableValueGraph<String, Double> graph = ValueGraphBuilder.undirected().build();

        final String a = "a";
        final String b = "b";
        final Map<String, Double> heuristics = new HashMap<String, Double>();
        Heuristic<String, Double> heuristic = null;

        try
        {
            graph.addNode( a );
            graph.addNode( b );

            heuristic = ( current, goal ) -> heuristics.get( current );
        }
        catch ( NullPointerException e )
        {
            fail( e.getMessage() );
        }

        findShortestPath( graph )
            .whereEdgesAreWeights()
            .from( a )
            .to( b )
            .applyingAStar( null )
            .withHeuristic( heuristic );
    }

    @Test( expected = PathNotFoundException.class )
    public void testNotConnectGraph()
    {
        final MutableValueGraph<String, Double> graph = ValueGraphBuilder.undirected().build();

        final String a = "a";
        final String b = "b";
        graph.addNode( a );
        graph.addNode( b );

        final Map<String, Double> heuristics = new HashMap<String, Double>();

        Heuristic<String, Double> heuristic = ( current, goal ) -> heuristics.get( current );

        findShortestPath( graph )
            .whereEdgesAreWeights()
            .from( a )
            .to( b )
            .applyingAStar( new DoubleWeightBaseOperations() )
            .withHeuristic( heuristic );
    }

    /**
     * Test Graph and Dijkstra's solution can be seen on
     * <a href="http://en.wikipedia.org/wiki/A*_search_algorithm">Wikipedia</a>
     */
    @Test
    public void findShortestPathAndVerify()
    {
        final MutableValueGraph<String, Double> graph = ValueGraphBuilder.undirected().build();

        // building Graph

        String start = "start";
        String a = "a";
        String b = "b";
        String c = "c";
        String d = "d";
        String e = "e";
        String goal = "goal";

        graph.addNode( start );
        graph.addNode( a );
        graph.addNode( b );
        graph.addNode( c );
        graph.addNode( d );
        graph.addNode( e );
        graph.addNode( goal );

        graph.putEdgeValue( start, a, 1.5D );
        graph.putEdgeValue( start, d, 2D );

        graph.putEdgeValue( a, b, 2D );
        graph.putEdgeValue( b, c, 3D );
        graph.putEdgeValue( c, goal, 3D );

        graph.putEdgeValue( d, e, 3D );
        graph.putEdgeValue( e, goal, 2D );

        // euristics

        final Map<String, Double> heuristics = new HashMap<String, Double>();
        heuristics.put( a, 4D );
        heuristics.put( b, 2D );
        heuristics.put( c, 4D );
        heuristics.put( d, 4.5D );
        heuristics.put( e, 2D );
        heuristics.put( goal, 6D );

        Heuristic<String, Double> heuristic = new Heuristic<String, Double>()
        {

            public Double applyHeuristic( String current, String goal )
            {
                return heuristics.get( current );
            }

        };

        // expected path

        Function<Double, Double> identity = we -> we;

        InMemoryWeightedPath<String, Double, Double> expected =
            new InMemoryWeightedPath<String, Double, Double>( start, goal, new DoubleWeightBaseOperations(), identity );

        expected.addConnectionInTail( start, 1.5D, a );
        expected.addConnectionInTail( a, 2D, b );
        expected.addConnectionInTail( b, 3D, c );
        expected.addConnectionInTail( c, 3D, goal );

        // actual path

        Path<String, Double> actual =
            findShortestPath( graph )
                .whereEdgesAreWeights()
                .from( start )
                .to( goal )
                .applyingAStar( new DoubleWeightBaseOperations() )
                .withHeuristic( heuristic );

        // assert!

        assertEquals( expected, actual );
    }
}
