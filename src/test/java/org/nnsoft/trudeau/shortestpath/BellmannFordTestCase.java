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

import java.util.function.Function;

import org.junit.Test;
import org.nnsoft.trudeau.api.InMemoryWeightedPath;
import org.nnsoft.trudeau.api.WeightedPath;
import org.nnsoft.trudeau.math.monoid.primitive.DoubleWeightBaseOperations;

import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;

public final class BellmannFordTestCase
{

    @Test( expected = NullPointerException.class )
    public void testNullGraph()
    {
        // the actual weighted path
        findShortestPath( null )
            .whereEdgesAreWeights()
            .from( null )
            .applyingBelmannFord( null );
    }

    @Test( expected = NullPointerException.class )
    public void testNullVertices()
    {
        final MutableValueGraph<String, Double> graph = ValueGraphBuilder.undirected().build();

        findShortestPath( graph )
            .whereEdgesAreWeights()
            .from( null )
            .applyingBelmannFord( new DoubleWeightBaseOperations() );
    }

    @Test( expected = NullPointerException.class )
    public void testNullMonoid()
    {
        final MutableValueGraph<String, Double> graph = ValueGraphBuilder.undirected().build();
        graph.addNode( "a" );
        graph.addNode( "b" );

        // the actual weighted path
        findShortestPath( graph )
            .whereEdgesAreWeights()
            .from( "a" )
            .applyingBelmannFord( null );
    }

    @Test//( expected = PathNotFoundException.class )
    public void testNotConnectGraph()
    {
        String a = null;
        String b = null;
        // the actual weighted path
        AllVertexPairsShortestPath<String, Double, Double> allVertexPairsShortestPath =
            null;
        try
        {
            final MutableValueGraph<String, Double> graph = ValueGraphBuilder.undirected().build();

            a = "a";
            b = "b";
            graph.addNode( a );
            graph.addNode( b );

            allVertexPairsShortestPath = findShortestPath( graph )
                            .whereEdgesAreWeights()
                            .from( a )
                            .applyingBelmannFord( new DoubleWeightBaseOperations() );
        }
        //catch ( PathNotFoundException e )
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }
        allVertexPairsShortestPath.findShortestPath( a, b );
    }

    /**
     * Test Graph and Dijkstra's solution can be seen on
     * <a href="http://compprog.wordpress.com/2007/11/29/one-source-shortest-path-the-bellman-ford-algorithm/">Wikipedia</a>
     */
    @Test
    public void findShortestPathAndVerify()
    {
        // the input graph
        final MutableValueGraph<String, Double> graph = ValueGraphBuilder.undirected().build();

        String one = "1";
        String two = "2";
        String three = "3";
        String four = "4";
        String five = "5";

        graph.addNode( one );
        graph.addNode( two );
        graph.addNode( three );
        graph.addNode( four );
        graph.addNode( five );

        graph.putEdgeValue( one, two, 6D );
        graph.putEdgeValue( one, four, 7D );

        graph.putEdgeValue( two, three, 5D );
        graph.putEdgeValue( two, five, -4D );
        graph.putEdgeValue( two, four, 8D );

        graph.putEdgeValue( three, two, -2D );

        graph.putEdgeValue( four, three, -3D );
        graph.putEdgeValue( four, five, 9D );

        graph.putEdgeValue( five, three, 7D );
        graph.putEdgeValue( five, one, 2D );

        // the expected weighted path
        Function<Double, Double> identity = we -> we;

        InMemoryWeightedPath<String, Double, Double> expected =
            new InMemoryWeightedPath<String, Double, Double>( one, three, new DoubleWeightBaseOperations(), identity );
        expected.addConnectionInTail( one, 7D, four );
        expected.addConnectionInTail( four, -3D, three );

        // the actual weighted path
        AllVertexPairsShortestPath<String, Double, Double> allVertexPairsShortestPath =
            findShortestPath( graph )
                .whereEdgesAreWeights()
                .from( one )
                .applyingBelmannFord( new DoubleWeightBaseOperations() );

        WeightedPath<String, Double, Double> actual =
            allVertexPairsShortestPath.findShortestPath( one, three );
        assertEquals( expected, actual );
    }

}
