/*
 * Copyright (c) 2017-2021 "Neo4j,"
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
package org.neo4j.graphalgo.api;

import com.carrotsearch.hppc.ObjectLongMap;
import org.neo4j.graphalgo.RelationshipType;
import org.neo4j.graphalgo.annotation.ValueClass;
import org.neo4j.graphalgo.config.GraphCreateConfig;
import org.neo4j.graphalgo.core.GraphDimensions;
import org.neo4j.graphalgo.core.loading.RelationshipsBuilder;
import org.neo4j.graphalgo.core.utils.ProgressLogger;
import org.neo4j.graphalgo.core.utils.mem.MemoryEstimation;

import java.util.Map;

/**
 * The Abstract Factory defines the construction of the graph
 */
public abstract class GraphStoreFactory<STORE extends GraphStore, CONFIG extends GraphCreateConfig> {

    public interface Supplier {
        GraphStoreFactory<? extends GraphStore, ? extends GraphCreateConfig> get(GraphLoaderContext loaderContext);

        default GraphStoreFactory<? extends GraphStore, ? extends GraphCreateConfig> getWithDimension(GraphLoaderContext loaderContext, GraphDimensions graphDimensions) {
            return get(loaderContext);
        }
    }

    public static final String TASK_LOADING = "LOADING";

    protected final CONFIG graphCreateConfig;
    protected final GraphLoaderContext loadingContext;
    protected final GraphDimensions dimensions;
    protected final ProgressLogger progressLogger;

    public GraphStoreFactory(
        CONFIG graphCreateConfig,
        GraphLoaderContext loadingContext,
        GraphDimensions dimensions
    ) {
        this.graphCreateConfig = graphCreateConfig;
        this.loadingContext = loadingContext;
        this.dimensions = dimensions;
        this.progressLogger = initProgressLogger();
    }

    public abstract ImportResult<STORE> build();

    public abstract MemoryEstimation memoryEstimation();

    public GraphDimensions dimensions() {
        return this.dimensions;
    }

    public GraphDimensions estimationDimensions() {
        return dimensions;
    }

    protected abstract ProgressLogger initProgressLogger();

    @ValueClass
    public interface ImportResult<STORE extends GraphStore> {
        GraphDimensions dimensions();

        STORE graphStore();

        static <STORE extends GraphStore> ImportResult<STORE> of(GraphDimensions dimensions, STORE graphStore) {
            return ImmutableImportResult.<STORE>builder()
                .dimensions(dimensions)
                .graphStore(graphStore)
                .build();
        }
    }

    @ValueClass
    public interface RelationshipImportResult {
        Map<RelationshipType, RelationshipsBuilder> builders();

        ObjectLongMap<RelationshipType> counts();

        GraphDimensions dimensions();

        static RelationshipImportResult of(
            Map<RelationshipType, RelationshipsBuilder> builders,
            ObjectLongMap<RelationshipType> counts,
            GraphDimensions dimensions
        ) {
            return ImmutableRelationshipImportResult.of(builders, counts, dimensions);
        }
    }
}
