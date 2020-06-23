/*
 * Copyright (c) 2017-2020 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.gds.embeddings.graphsage.subgraph;

import org.neo4j.gds.embeddings.graphsage.NeighborhoodFunction;
import org.neo4j.graphalgo.api.Graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SubGraphBuilderImpl implements SubGraphBuilder {
    @Override
    public SubGraph buildSubGraph(
        Collection<Long> nodeIds, NeighborhoodFunction neighborhoodFunction, Graph graph
    ) {
        int[][] adjacency = new int[nodeIds.size()][];
        int[] selfAdjacency = new int[nodeIds.size()];
        LocalIdMap idmap = new LocalIdMapImpl();
        nodeIds.forEach(idmap::toMapped);

        AtomicInteger nodeOffset = new AtomicInteger(0);
        nodeIds.forEach(nodeId -> {
            int internalId = nodeOffset.getAndIncrement();
            selfAdjacency[internalId] = idmap.toMapped(nodeId);
            List<Long> nodeNeighbors = neighborhoodFunction.apply(graph, nodeId);
            int[] neighborInternalIds = nodeNeighbors
                .stream()
                .mapToInt(idmap::toMapped)
                .toArray();
            adjacency[internalId] = neighborInternalIds;
        });
        return new SubGraph(adjacency, selfAdjacency, idmap.originalIds());
    }

    @Override
    public List<SubGraph> buildSubGraphs(
        Collection<Long> nodeIds, List<NeighborhoodFunction> neighborhoodFunctions, Graph graph
    ) {
        List<SubGraph> result = new ArrayList<>();
        Collection<Long> previousNodes = nodeIds;

        Collections.reverse(neighborhoodFunctions);
        for (NeighborhoodFunction neighborhoodFunction : neighborhoodFunctions) {
            SubGraph lastGraph = buildSubGraph(previousNodes, neighborhoodFunction, graph);
            result.add(lastGraph);
            previousNodes = lastGraph.nextNodes;
        }
        return result;
    }
}