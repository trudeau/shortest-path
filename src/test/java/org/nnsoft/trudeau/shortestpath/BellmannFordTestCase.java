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

import org.junit.Test;
import org.nnsoft.trudeau.api.Graph;
import org.nnsoft.trudeau.api.WeightedPath;
import org.nnsoft.trudeau.inmemory.DirectedMutableGraph;
import org.nnsoft.trudeau.inmemory.InMemoryWeightedPath;
import org.nnsoft.trudeau.inmemory.PathNotFoundException;
import org.nnsoft.trudeau.inmemory.UndirectedMutableGraph;
import org.nnsoft.trudeau.inmemory.labeled.BaseLabeledVertex;
import org.nnsoft.trudeau.inmemory.labeled.BaseLabeledWeightedEdge;
import org.nnsoft.trudeau.inmemory.labeled.BaseWeightedEdge;
import org.nnsoft.trudeau.math.monoid.primitive.DoubleWeightBaseOperations;

public final class BellmannFordTestCase
{

    @Test( expected = NullPointerException.class )
    public void testNullGraph()
    {
        // the actual weighted path
        findShortestPath( (Graph<BaseLabeledVertex, BaseLabeledWeightedEdge<Double>>) null )
            .whereEdgesHaveWeights( new BaseWeightedEdge<Double>() )
            .from( null )
            .applyingBelmannFord( new DoubleWeightBaseOperations() );
    }

    @Test( expected = NullPointerException.class )
    public void testNullVertices()
    {
        UndirectedMutableGraph<BaseLabeledVertex, BaseLabeledWeightedEdge<Double>> graph =
            new UndirectedMutableGraph<BaseLabeledVertex, BaseLabeledWeightedEdge<Double>>();

        findShortestPath( graph )
            .whereEdgesHaveWeights( new BaseWeightedEdge<Double>() )
            .from( null )
            .applyingBelmannFord( new DoubleWeightBaseOperations() );
    }

    @Test( expected = NullPointerException.class )
    public void testNullMonoid()
    {
        UndirectedMutableGraph<BaseLabeledVertex, BaseLabeledWeightedEdge<Double>> graph = null;
        BaseLabeledVertex a = null;
        try
        {
            graph = new UndirectedMutableGraph<BaseLabeledVertex, BaseLabeledWeightedEdge<Double>>();

            a = new BaseLabeledVertex( "a" );
            final BaseLabeledVertex b = new BaseLabeledVertex( "b" );
            graph.addVertex( a );
            graph.addVertex( b );
        }
        catch ( NullPointerException e )
        {
            fail( e.getMessage() );
        }

        // the actual weighted path
        findShortestPath( graph )
            .whereEdgesHaveWeights( new BaseWeightedEdge<Double>() )
            .from( a )
            .applyingBelmannFord( null );
    }

    @Test( expected = PathNotFoundException.class )
    public void testNotConnectGraph()
    {
        BaseLabeledVertex a = null;
        BaseLabeledVertex b = null;
        // the actual weighted path
        AllVertexPairsShortestPath<BaseLabeledVertex, BaseLabeledWeightedEdge<Double>, Double> allVertexPairsShortestPath =
            null;
        try
        {
            UndirectedMutableGraph<BaseLabeledVertex, BaseLabeledWeightedEdge<Double>> graph =
                new UndirectedMutableGraph<BaseLabeledVertex, BaseLabeledWeightedEdge<Double>>();

            a = new BaseLabeledVertex( "a" );
            b = new BaseLabeledVertex( "b" );
            graph.addVertex( a );
            graph.addVertex( b );

            allVertexPairsShortestPath = findShortestPath( graph )
                            .whereEdgesHaveWeights( new BaseWeightedEdge<Double>() )
                            .from( a )
                            .applyingBelmannFord( new DoubleWeightBaseOperations() );
        }
        catch ( PathNotFoundException e )
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
        DirectedMutableGraph<BaseLabeledVertex, BaseLabeledWeightedEdge<Double>> graph =
            new DirectedMutableGraph<BaseLabeledVertex, BaseLabeledWeightedEdge<Double>>();

        BaseLabeledVertex one = new BaseLabeledVertex( "1" );
        BaseLabeledVertex two = new BaseLabeledVertex( "2" );
        BaseLabeledVertex three = new BaseLabeledVertex( "3" );
        BaseLabeledVertex four = new BaseLabeledVertex( "4" );
        BaseLabeledVertex five = new BaseLabeledVertex( "5" );

        graph.addVertex( one );
        graph.addVertex( two );
        graph.addVertex( three );
        graph.addVertex( four );
        graph.addVertex( five );

        graph.addEdge( one, new BaseLabeledWeightedEdge<Double>( "1 -> 2", 6D ), two );
        graph.addEdge( one, new BaseLabeledWeightedEdge<Double>( "1 -> 4", 7D ), four );

        graph.addEdge( two, new BaseLabeledWeightedEdge<Double>( "2 -> 3", 5D ), three );
        graph.addEdge( two, new BaseLabeledWeightedEdge<Double>( "2 -> 5", -4D ), five );
        graph.addEdge( two, new BaseLabeledWeightedEdge<Double>( "2 -> 4", 8D ), four );

        graph.addEdge( three, new BaseLabeledWeightedEdge<Double>( "3 -> 2", -2D ), two );

        graph.addEdge( four, new BaseLabeledWeightedEdge<Double>( "4 -> 3", -3D ), three );
        graph.addEdge( four, new BaseLabeledWeightedEdge<Double>( "4 -> 5", 9D ), five );

        graph.addEdge( five, new BaseLabeledWeightedEdge<Double>( "5 -> 3", 7D ), three );
        graph.addEdge( five, new BaseLabeledWeightedEdge<Double>( "5 -> 1", 2D ), one );

        // the expected weighted path
        InMemoryWeightedPath<BaseLabeledVertex, BaseLabeledWeightedEdge<Double>, Double> expected =
            new InMemoryWeightedPath<BaseLabeledVertex, BaseLabeledWeightedEdge<Double>, Double>( one, three, new DoubleWeightBaseOperations(), new BaseWeightedEdge<Double>() );
        expected.addConnectionInTail( one, new BaseLabeledWeightedEdge<Double>( "1 -> 4", 7D ), four );
        expected.addConnectionInTail( four, new BaseLabeledWeightedEdge<Double>( "4 -> 3", -3D ), three );

        // the actual weighted path
        AllVertexPairsShortestPath<BaseLabeledVertex, BaseLabeledWeightedEdge<Double>, Double> allVertexPairsShortestPath =
            findShortestPath( graph )
                .whereEdgesHaveWeights( new BaseWeightedEdge<Double>() )
                .from( one )
                .applyingBelmannFord( new DoubleWeightBaseOperations() );

        WeightedPath<BaseLabeledVertex, BaseLabeledWeightedEdge<Double>, Double> actual =
            allVertexPairsShortestPath.findShortestPath( one, three );
        assertEquals( expected, actual );
    }

}
