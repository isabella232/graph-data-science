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
package org.neo4j.graphalgo.beta.paths;

import org.neo4j.graphalgo.AlgoBaseProc;
import org.neo4j.graphalgo.Algorithm;
import org.neo4j.graphalgo.StreamProc;
import org.neo4j.graphalgo.api.NodeProperties;
import org.neo4j.graphalgo.beta.paths.dijkstra.DijkstraResult;
import org.neo4j.graphalgo.config.AlgoBaseConfig;

import java.util.stream.Stream;

public abstract class ShortestPathStreamProc<
    ALGO extends Algorithm<ALGO, DijkstraResult>,
    CONFIG extends AlgoBaseConfig & ReturnsPathConfig> extends StreamProc<ALGO, DijkstraResult, StreamResult, CONFIG> {

    @Override
    protected StreamResult streamResult(long originalNodeId, long internalNodeId, NodeProperties nodeProperties) {
        throw new UnsupportedOperationException("Shortest path algorithm handles result building individually.");
    }

    @Override
    public Stream<StreamResult> stream(AlgoBaseProc.ComputationResult<ALGO, DijkstraResult, CONFIG> computationResult) {
        return runWithExceptionLogging("Result streaming failed", () -> {
            var graph = computationResult.graph();
            var config = computationResult.config();

            if (computationResult.isGraphEmpty()) {
                graph.release();
                return Stream.empty();
            }

            var resultBuilder = new StreamResult.Builder(graph, transaction.internalTransaction());
            return computationResult
                .result()
                .paths()
                .map(path -> resultBuilder.build(path, config.path()));
        });
    }
}
