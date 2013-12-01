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

import static org.nnsoft.trudeau.connector.GraphPopulator.populate;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static org.junit.Assert.assertEquals;
import static org.nnsoft.trudeau.shortestpath.ShortestPathSolver.findShortestPath;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;
import org.nnsoft.trudeau.api.Graph;
import org.nnsoft.trudeau.api.GraphException;
import org.nnsoft.trudeau.api.Path;
import org.nnsoft.trudeau.api.WeightedPath;
import org.nnsoft.trudeau.connector.AbstractGraphConnection;
import org.nnsoft.trudeau.inmemory.DirectedMutableGraph;
import org.nnsoft.trudeau.inmemory.InMemoryWeightedPath;
import org.nnsoft.trudeau.inmemory.PathNotFoundException;
import org.nnsoft.trudeau.inmemory.UndirectedMutableGraph;
import org.nnsoft.trudeau.inmemory.labeled.BaseLabeledVertex;
import org.nnsoft.trudeau.inmemory.labeled.BaseLabeledWeightedEdge;
import org.nnsoft.trudeau.inmemory.labeled.BaseWeightedEdge;
import org.nnsoft.trudeau.math.monoid.OrderedMonoid;
import org.nnsoft.trudeau.math.monoid.primitive.DoubleWeightBaseOperations;

public final class BidirDijkstraTestCase
{

    private static final int TIMES = 10;

    private static final int NODES = 5000;

    private static final int EDGES = 100000;

    private static final double EPSILON = 1.0e-6;

    private static DirectedMutableGraph<BaseLabeledVertex, BaseLabeledWeightedEdge<Double>> graph;

    private static List<BaseLabeledVertex> vertices;

    private static OrderedMonoid<Double> weightOperations;

    @BeforeClass
    public static void setUp()
    {
        weightOperations = new DoubleWeightBaseOperations();

        graph = populate( new DirectedMutableGraph<BaseLabeledVertex, BaseLabeledWeightedEdge<Double>>() )
                .withConnections( new AbstractGraphConnection<BaseLabeledVertex, BaseLabeledWeightedEdge<Double>>()
        {
            Random r = new Random();

            public void connect()
            {
                vertices = new ArrayList<BaseLabeledVertex>();
                for ( int i = 0; i < NODES; i++ )
                {
                    BaseLabeledVertex v = new BaseLabeledVertex( valueOf( i ) );
                    addVertex( v );
                    vertices.add( v );
                }

                // form a connected graph
                for ( int i = 0; i < NODES - 1; i++ )
                {
                    addEdge( vertices.get( i ), vertices.get( i + 1 ) );
                }

                addEdge( vertices.get( NODES - 1 ) , vertices.get( 0 ) );

                // we have already created #NODES edges
                int maxEdges = Math.max(0, EDGES - NODES);
                for ( int i = 0; i < maxEdges; i++)
                {
                    while ( ! addEdge( vertices.get( r.nextInt(NODES) ), vertices.get( r.nextInt(NODES) ) ) ) {
                        // do nothing
                    }
                }
            }

            private boolean addEdge( BaseLabeledVertex src, BaseLabeledVertex dst )
            {
                try {
                  addEdge( new BaseLabeledWeightedEdge<Double>( format( "%s -> %s", src, dst ),
                                                                10.0 * r.nextDouble() + 1.0 ) ).from( src ).to( dst );
                  return true;
              } catch (GraphException e) {
                  // ignore duplicate edge exceptions
                  return false;
              }
            }
        } );
    }

    @Test( expected = NullPointerException.class )
    public void testNullGraph()
    {
        // the actual weighted path
        findShortestPath( (Graph<BaseLabeledVertex, BaseLabeledWeightedEdge<Double>>) null )
            .whereEdgesHaveWeights( new BaseWeightedEdge<Double>() )
            .from( null )
            .to( null )
            .applyingBidirectionalDijkstra( new DoubleWeightBaseOperations() );
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
            .applyingBidirectionalDijkstra( new DoubleWeightBaseOperations() );
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
            .applyingBidirectionalDijkstra( null );
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
            .applyingBidirectionalDijkstra( new DoubleWeightBaseOperations() );
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
                            .applyingBidirectionalDijkstra( new DoubleWeightBaseOperations() );

        // assert!

        assertEquals( expected, actual );
    }

    @Test
    public void verifyTwoNodePath() {
        DirectedMutableGraph<BaseLabeledVertex, BaseLabeledWeightedEdge<Double>> graph =
            new DirectedMutableGraph<BaseLabeledVertex, BaseLabeledWeightedEdge<Double>>();

        // building Graph

        BaseLabeledVertex one = new BaseLabeledVertex( "1" );
        BaseLabeledVertex two = new BaseLabeledVertex( "2" );

        graph.addVertex( one );
        graph.addVertex( two );

        graph.addEdge( one, new BaseLabeledWeightedEdge<Double>( "1 -> 2", 14D ), two );

        // expected path
        InMemoryWeightedPath<BaseLabeledVertex, BaseLabeledWeightedEdge<Double>, Double> expected =
            new InMemoryWeightedPath<BaseLabeledVertex, BaseLabeledWeightedEdge<Double>, Double>( one, two, new DoubleWeightBaseOperations(), new BaseWeightedEdge<Double>() );

        expected.addConnectionInTail( one, new BaseLabeledWeightedEdge<Double>( "1 -> 2", 14D ), two );

        // actual path
        Path<BaseLabeledVertex, BaseLabeledWeightedEdge<Double>> actual =
                        findShortestPath( graph )
                            .whereEdgesHaveWeights( new BaseWeightedEdge<Double>() )
                            .from( one )
                            .to( two )
                            .applyingBidirectionalDijkstra( new DoubleWeightBaseOperations() );

        // assert!
        assertEquals( expected, actual );
    }

    @Test
    public void verifyThreeNodePath() {
        DirectedMutableGraph<BaseLabeledVertex, BaseLabeledWeightedEdge<Double>> graph =
            new DirectedMutableGraph<BaseLabeledVertex, BaseLabeledWeightedEdge<Double>>();

        // building Graph

        BaseLabeledVertex a = new BaseLabeledVertex( "a" );
        BaseLabeledVertex b = new BaseLabeledVertex( "b" );
        BaseLabeledVertex c = new BaseLabeledVertex( "c" );

        graph.addVertex( a );
        graph.addVertex( b );
        graph.addVertex( c );

        graph.addEdge( a, new BaseLabeledWeightedEdge<Double>( "a -> b", 14D ), b );
        graph.addEdge( b, new BaseLabeledWeightedEdge<Double>( "b -> c", 10D ), c );

        // expected path
        InMemoryWeightedPath<BaseLabeledVertex, BaseLabeledWeightedEdge<Double>, Double> expected =
            new InMemoryWeightedPath<BaseLabeledVertex, BaseLabeledWeightedEdge<Double>, Double>( a, c, new DoubleWeightBaseOperations(), new BaseWeightedEdge<Double>() );

        expected.addConnectionInTail( a, new BaseLabeledWeightedEdge<Double>( "a -> b", 14D ), b );
        expected.addConnectionInTail( b, new BaseLabeledWeightedEdge<Double>( "b -> c", 10D ), c );

        // actual path
        Path<BaseLabeledVertex, BaseLabeledWeightedEdge<Double>> actual =
                        findShortestPath( graph )
                            .whereEdgesHaveWeights( new BaseWeightedEdge<Double>() )
                            .from( a )
                            .to( c )
                            .applyingBidirectionalDijkstra( new DoubleWeightBaseOperations() );

        // assert!
        assertEquals( expected, actual );
    }

    @Test
    public void compareToUnidirectional() {
        // It is hard to get unidirectional Dijkstra's algorithm wrong;
        // therefore compare a sequence of outputs.
        Random r = new Random();

        for ( int ii = 0; ii < TIMES; ii++ )
        {
            BaseLabeledVertex s = vertices.get( r.nextInt( vertices.size() ) );
            BaseLabeledVertex t;

            do
            {
                t = vertices.get( r.nextInt( vertices.size() ) );
            }
            while ( s.equals( t ) );

            WeightedPath<BaseLabeledVertex, BaseLabeledWeightedEdge<Double>, Double> pathUni =
                    findShortestPath( graph )
                        .whereEdgesHaveWeights( new BaseWeightedEdge<Double>() )
                        .from( s )
                        .to( t )
                        .applyingDijkstra( weightOperations );

            WeightedPath<BaseLabeledVertex, BaseLabeledWeightedEdge<Double>, Double> pathBi =
                    findShortestPath( graph )
                        .whereEdgesHaveWeights( new BaseWeightedEdge<Double>() )
                        .from( s )
                        .to( t )
                        .applyingBidirectionalDijkstra( weightOperations );

            assertEquals( pathUni.getSize(), pathBi.getSize() );
            assertEquals( pathUni.getWeight(), pathBi.getWeight(), EPSILON );
        }
    }

}
