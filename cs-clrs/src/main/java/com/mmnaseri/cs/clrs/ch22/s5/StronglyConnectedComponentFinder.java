package com.mmnaseri.cs.clrs.ch22.s5;

import com.mmnaseri.cs.clrs.ch21.DisjointSet;
import com.mmnaseri.cs.clrs.ch21.s3.PathCompressingRankedForestDisjointSet;
import com.mmnaseri.cs.clrs.ch21.s3.RankedTreeElement;
import com.mmnaseri.cs.clrs.ch22.GraphVertexVisitorAdapter;
import com.mmnaseri.cs.clrs.ch22.s1.EdgeDetails;
import com.mmnaseri.cs.clrs.ch22.s1.Graph;
import com.mmnaseri.cs.clrs.ch22.s1.Vertex;
import com.mmnaseri.cs.clrs.ch22.s1.VertexDetails;
import com.mmnaseri.cs.clrs.ch22.s3.DepthFirstGraphVisitor;
import com.mmnaseri.cs.clrs.common.ParameterizedTypeReference;

import java.util.Comparator;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (8/3/15)
 */
public class StronglyConnectedComponentFinder<E extends EdgeDetails, V extends VertexDetails> {

    private final DepthFirstGraphVisitor<E, V> first = new DepthFirstGraphVisitor<>();
    private final DepthFirstGraphVisitor<E, V> second = new DepthFirstGraphVisitor<>(new VertexFinishTimeComparator<V>());

    public DisjointSet<?, Vertex<V>> find(Graph<E, V> graph) {
        //visit all nodes
        first.visit(graph, -1, new GraphVertexVisitorAdapter<E, V>() {
        });
        //compute the inverse
        final Graph<E, V> inverse = graph.inverse();
        //visit the inverse nodes in decreasing order of the finish time the first time around
        second.visit(inverse, -1, new GraphVertexVisitorAdapter<E, V>() {});
        //create a disjoint set where each set is represented by a root of the DFS tree and contains all its internal nodes
        final PathCompressingRankedForestDisjointSet<Vertex<V>> set = new PathCompressingRankedForestDisjointSet<>();
        for (Vertex<V> vertex : inverse.getVertices()) {
            final RankedTreeElement<Vertex<V>> element = set.create(vertex);
            final Vertex<V> root = findRoot(vertex);
            if (root.getIndex() == vertex.getIndex()) {
                continue;
            }
            RankedTreeElement<Vertex<V>> rootElement = null;
            for (RankedTreeElement<Vertex<V>> treeElement : set.sets()) {
                if (treeElement.getValue().getIndex() == root.getIndex()) {
                    rootElement = treeElement;
                    break;
                }
            }
            if (rootElement == null) {
                rootElement = set.create(root);
            }
            set.union(rootElement, element);
        }
        return set;
    }
    
    private Vertex<V> findRoot(Vertex<V> vertex) {
        while (true) {
            final Vertex<V> parent = vertex.getProperty("parent", new ParameterizedTypeReference<Vertex<V>>() {
            });
            if (parent == null) {
                return vertex;
            }
            vertex = parent;
        }
    }

    private static class VertexFinishTimeComparator<V extends VertexDetails> implements Comparator<Vertex<V>> {

        @Override
        public int compare(Vertex<V> first, Vertex<V> second) {
            return Integer.compare(second.getProperty("finish", Integer.class), first.getProperty("finish", Integer.class));
        }

    }

}