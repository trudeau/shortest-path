shortest-path
=============

Graph shortest paths problem solver implementation

# Usage

This is a small set of fluent APIs to apply shortest path algorithms on `org.nnsoft.trudeau.api.Graph`.

## Specify edges weight

The `org.nnsoft.trudeau.api.Mapper` interface is used to map a Graph edge to the target weight; given a generic `Graph<V, E>`, a `org.nnsoft.trudeau.api.Mapper<E, W>` returns the associated weight to the input edge.

This is to allow more flexibility to the shortest-path APIs which don't force users to define a specific weighted edge implementation, but rather applying different weight measures on the same graph. 

## Dijkstra Algorithm

The [Dijkstra Algorithm](http://en.wikipedia.org/wiki/Dijkstra's_algorithm) requires a _source_ and a _target_ node to find the shortest path, the `org.nnsoft.trudeau.api.Mapper` function to assign weights to edges and the [Monoid](https://github.com/trudeau/monoid) for weight operations.
A `org.nnsoft.trudeau.inmemory.PathNotFoundException` is thrown if the path is not found.

```
import static org.nnsoft.trudeau.shortestpath.ShortestPathSolver.findShortestPath;

import org.nnsoft.trudeau.api.Mapper;
import org.nnsoft.trudeau.api.UndirectedGraph;
import org.nnsoft.trudeau.api.WeightedPath
import org.nnsoft.trudeau.math.monoid.OrderedMonoid;

…

UndirectedGraph<V, E> graph;
V source, target;
Mapper<E, W> weights;
OrderedMonoid<W> weightMonoid;

// variables initialization omitted

WeightedPath<V, E> shortestPath = findShortestPath( graph )
                                  .whereEdgesHaveWeights( weights )
                                  .from( source )
                                  .to( target )
                                  .applyingDijkstra( weightMonoid );
```

## Bidirectional Dijkstra Algorithm

The [Bidirectional Dijkstra Algorithm](http://en.wikipedia.org/wiki/Bidirectional_search) requires a _source_ and a _target_ node to find the shortest path, the `org.nnsoft.trudeau.api.Mapper` function to assign weights to edges and the [Monoid](https://github.com/trudeau/monoid) for weight operations.
A `org.nnsoft.trudeau.inmemory.PathNotFoundException` is thrown if the path is not found.

```
import static org.nnsoft.trudeau.shortestpath.ShortestPathSolver.findShortestPath;

import org.nnsoft.trudeau.api.Mapper;
import org.nnsoft.trudeau.api.UndirectedGraph;
import org.nnsoft.trudeau.api.WeightedPath
import org.nnsoft.trudeau.math.monoid.OrderedMonoid;

…

UndirectedGraph<V, E> graph;
V source, target;
Mapper<E, W> weights;
OrderedMonoid<W> weightMonoid;

// variables initialization omitted

WeightedPath<V, E> shortestPath = findShortestPath( graph )
                                  .whereEdgesHaveWeights( weights )
                                  .from( source )
                                  .to( target )
                                  .applyingBidirectionalDijkstra( weightMonoid );
```

# A* Algorithm

The [A* Algorithm](http://en.wikipedia.org/wiki/A*_search_algorithm) requires a _source_ and a _target_ node to find the shortest path, the `org.nnsoft.trudeau.api.Mapper` function to assign weights to edges, the [Monoid](https://github.com/trudeau/monoid) for weight operations and the `org.nnsoft.trudeau.shortestpath.Heuristic` that represents the A* heuristic function.
A `org.nnsoft.trudeau.inmemory.PathNotFoundException` is thrown if the path is not found.

```
import static org.nnsoft.trudeau.shortestpath.ShortestPathSolver.findShortestPath;

import org.nnsoft.trudeau.api.Mapper;
import org.nnsoft.trudeau.api.UndirectedGraph;
import org.nnsoft.trudeau.api.WeightedPath
import org.nnsoft.trudeau.math.monoid.OrderedMonoid;
import org.nnsoft.trudeau.shortestpath.Heuristic;

…

UndirectedGraph<V, E> graph;
V source, target;
Mapper<E, W> weights;
OrderedMonoid<W> weightMonoid;
Heuristic<V, W> heuristic;

// variables initialization omitted

WeightedPath<V, E> shortestPath = findShortestPath( graph )
                                  .whereEdgesHaveWeights( weights )
                                  .from( source )
                                  .to( target )
                                  .applyingAStar( weightMonoid )
                                  .withHeuristic( heuristic );
```

# Floyd-Warshall Algorithm

The [Floyd-Warshall Algorithm](http://en.wikipedia.org/wiki/Floyd%E2%80%93Warshall_algorithm) requires a _source_ node to find all the shortest paths in the graph, the `org.nnsoft.trudeau.api.Mapper` function to assign weights to edges and the [Monoid](https://github.com/trudeau/monoid) for weight operations.
The algorithm execution returns a `org.nnsoft.trudeau.shortestpath.AllVertexPairsShortestPath` instance.

```
import static org.nnsoft.trudeau.shortestpath.ShortestPathSolver.findShortestPath;

import org.nnsoft.trudeau.api.Mapper;
import org.nnsoft.trudeau.api.UndirectedGraph;
import org.nnsoft.trudeau.api.WeightedPath
import org.nnsoft.trudeau.math.monoid.OrderedMonoid;
import org.nnsoft.trudeau.shortestpath.AllVertexPairsShortestPath;

…

UndirectedGraph<V, E> graph;
V source, target;
Mapper<E, W> weights;
OrderedMonoid<W> weightMonoid;

// variables initialization omitted

AllVertexPairsShortestPath<V, E> shortestPaths = findShortestPath( graph )
                                                 .whereEdgesHaveWeights( weights )
                                                 .applyingFloydWarshall( weightMonoid );

// AllVertexPairsShortestPath#findShortestPath(V, E) can throw org.nnsoft.trudeau.inmemory.PathNotFoundException

WeightedPath<V, E> shortestPath = shortestPaths.findShortestPath( source, target );
```

# Bellman-Ford Algorithm

The [Bellman-Ford Algorithm](http://en.wikipedia.org/wiki/Bellman%E2%80%93Ford_algorithm) requires a _source_ node to find all the shortest paths in the graph, the `org.nnsoft.trudeau.api.Mapper` function to assign weights to edges and the [Monoid](https://github.com/trudeau/monoid) for weight operations.
The algorithm execution returns a `org.nnsoft.trudeau.shortestpath.AllVertexPairsShortestPath` instance.

```
import static org.nnsoft.trudeau.shortestpath.ShortestPathSolver.findShortestPath;

import org.nnsoft.trudeau.api.Mapper;
import org.nnsoft.trudeau.api.UndirectedGraph;
import org.nnsoft.trudeau.api.WeightedPath
import org.nnsoft.trudeau.math.monoid.OrderedMonoid;
import org.nnsoft.trudeau.shortestpath.AllVertexPairsShortestPath;

…

UndirectedGraph<V, E> graph;
V source, target;
Mapper<E, W> weights;
OrderedMonoid<W> weightMonoid;

// variables initialization omitted

AllVertexPairsShortestPath<V, E> shortestPaths = findShortestPath( graph )
                                                 .whereEdgesHaveWeights( weights )
                                                 .applyingBelmannFord( weightMonoid );

// AllVertexPairsShortestPath#findShortestPath(V, E) can throw org.nnsoft.trudeau.inmemory.PathNotFoundException

WeightedPath<V, E> shortestPath = shortestPaths.findShortestPath( source, target );
```
