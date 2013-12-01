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
import static org.nnsoft.trudeau.shortestpath.ShortestPathSolver.findShortestPath;

import org.junit.Test;
import org.nnsoft.trudeau.api.Graph;
import org.nnsoft.trudeau.api.Path;
import org.nnsoft.trudeau.inmemory.DirectedMutableGraph;
import org.nnsoft.trudeau.inmemory.InMemoryWeightedPath;
import org.nnsoft.trudeau.inmemory.PathNotFoundException;
import org.nnsoft.trudeau.inmemory.UndirectedMutableGraph;
import org.nnsoft.trudeau.inmemory.labeled.BaseLabeledVertex;
import org.nnsoft.trudeau.inmemory.labeled.BaseLabeledWeightedEdge;
import org.nnsoft.trudeau.inmemory.labeled.BaseWeightedEdge;
import org.nnsoft.trudeau.math.monoid.primitive.DoubleWeightBaseOperations;

public final class DijkstraTestCase
{

    @Test( expected = NullPointerException.class )
    public void testNullGraph()
    {
        // the actual weighted path
        findShortestPath( (Graph<BaseLabeledVertex, BaseLabeledWeightedEdge<Double>>) null )
            .whereEdgesHaveWeights( new BaseWeightedEdge<Double>() )
            .from( null )
            .to( null )
            .applyingDijkstra( new DoubleWeightBaseOperations() );
    }

    @Test( expected = NullPointerException.class )
    public void testNullVertices()
    {
        UndirectedMutableGraph<BaseLabeledVertex, BaseLabeledWeightedEdge<Double>> graph =
            new UndirectedMutableGraph<BaseLabeledVertex, BaseLabeledWeightedEdge<Double>>();

        // the actual weighted path
        findShortestPath( graph )
            .whereEdgesHaveWeights( new BaseWeightedEdge<Double>() )
            .from( null )
            .to( null )
            .applyingDijkstra( new DoubleWeightBaseOperations() );
    }

    @Test( expected = NullPointerException.class )
    public void testNullMonoid()
    {
        UndirectedMutableGraph<BaseLabeledVertex, BaseLabeledWeightedEdge<Double>> graph =
            new UndirectedMutableGraph<BaseLabeledVertex, BaseLabeledWeightedEdge<Double>>();

        final BaseLabeledVertex a = new BaseLabeledVertex( "a" );
        final BaseLabeledVertex b = new BaseLabeledVertex( "b" );
        graph.addVertex( a );
        graph.addVertex( b );

        // the actual weighted path
        findShortestPath( graph )
            .whereEdgesHaveWeights( new BaseWeightedEdge<Double>() )
            .from( a )
            .to( b )
            .applyingDijkstra( null );
    }

    @Test( expected = PathNotFoundException.class )
    public void testNotConnectGraph()
    {
        UndirectedMutableGraph<BaseLabeledVertex, BaseLabeledWeightedEdge<Double>> graph =
            new UndirectedMutableGraph<BaseLabeledVertex, BaseLabeledWeightedEdge<Double>>();

        final BaseLabeledVertex a = new BaseLabeledVertex( "a" );
        final BaseLabeledVertex b = new BaseLabeledVertex( "b" );
        graph.addVertex( a );
        graph.addVertex( b );

        // the actual weighted path
        findShortestPath( graph )
            .whereEdgesHaveWeights( new BaseWeightedEdge<Double>() )
            .from( a )
            .to( b )
            .applyingDijkstra( new DoubleWeightBaseOperations() );
    }

    /**
     * Test Graph and Dijkstra's solution can be seen on
     * <a href="http://en.wikipedia.org/wiki/Dijkstra's_algorithm>Wikipedia</a>
     */
    @Test
    public void findShortestPathAndVerify()
    {
        DirectedMutableGraph<BaseLabeledVertex, BaseLabeledWeightedEdge<Double>> graph =
            new DirectedMutableGraph<BaseLabeledVertex, BaseLabeledWeightedEdge<Double>>();

        // building Graph

        BaseLabeledVertex one = new BaseLabeledVertex( "1" );
        BaseLabeledVertex two = new BaseLabeledVertex( "2" );
        BaseLabeledVertex three = new BaseLabeledVertex( "3" );
        BaseLabeledVertex four = new BaseLabeledVertex( "4" );
        BaseLabeledVertex five = new BaseLabeledVertex( "5" );
        BaseLabeledVertex six = new BaseLabeledVertex( "6" );

        graph.addVertex( one );
        graph.addVertex( two );
        graph.addVertex( three );
        graph.addVertex( four );
        graph.addVertex( five );
        graph.addVertex( six );

        graph.addEdge( one, new BaseLabeledWeightedEdge<Double>( "1 -> 6", 14D ), six );
        graph.addEdge( one, new BaseLabeledWeightedEdge<Double>( "1 -> 3", 9D ), three );
        graph.addEdge( one, new BaseLabeledWeightedEdge<Double>( "1 -> 2", 7D ), two );

        graph.addEdge( two, new BaseLabeledWeightedEdge<Double>( "2 -> 3", 10D ), three );
        graph.addEdge( two, new BaseLabeledWeightedEdge<Double>( "2 -> 4", 15D ), four );

        graph.addEdge( three, new BaseLabeledWeightedEdge<Double>( "3 -> 6", 2D ), six );
        graph.addEdge( three, new BaseLabeledWeightedEdge<Double>( "3 -> 4", 11D ), four );

        graph.addEdge( four, new BaseLabeledWeightedEdge<Double>( "4 -> 5", 6D ), five );
        graph.addEdge( six, new BaseLabeledWeightedEdge<Double>( "6 -> 5", 9D ), five );

        // expected path

        InMemoryWeightedPath<BaseLabeledVertex, BaseLabeledWeightedEdge<Double>, Double> expected =
            new InMemoryWeightedPath<BaseLabeledVertex, BaseLabeledWeightedEdge<Double>, Double>( one, five, new DoubleWeightBaseOperations(), new BaseWeightedEdge<Double>() );

        expected.addConnectionInTail( one, new BaseLabeledWeightedEdge<Double>( "1 -> 3", 9D ), three );
        expected.addConnectionInTail( three, new BaseLabeledWeightedEdge<Double>( "3 -> 6", 2D ), six );
        expected.addConnectionInTail( six, new BaseLabeledWeightedEdge<Double>( "6 -> 5", 9D ), five );

        // actual path

        Path<BaseLabeledVertex, BaseLabeledWeightedEdge<Double>> actual =
                        findShortestPath( graph )
                            .whereEdgesHaveWeights( new BaseWeightedEdge<Double>() )
                            .from( one )
                            .to( five )
                            .applyingDijkstra( new DoubleWeightBaseOperations() );

        // assert!

        assertEquals( expected, actual );
    }

}
