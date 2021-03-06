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
package org.neo4j.gds.embeddings.graphsage.algo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.neo4j.graphalgo.config.GraphCreateFromStoreConfig;
import org.neo4j.graphalgo.core.CypherMapWrapper;
import org.neo4j.graphalgo.core.loading.GraphStoreWithConfig;
import org.neo4j.graphalgo.core.loading.ImmutableGraphStoreWithConfig;
import org.neo4j.graphalgo.gdl.GdlFactory;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphSageTrainConfigTest {

    @Test
    void shouldThrowIfNoPropertiesProvided() {
        var mapWrapper = CypherMapWrapper.create(Map.of("modelName", "foo"));
        var expectedMessage = "GraphSage requires at least one property. Either `featureProperties` or `degreeAsProperty` must be set.";
        var throwable = assertThrows(IllegalArgumentException.class, () -> GraphSageTrainConfig.of("", Optional.empty(), Optional.empty(), mapWrapper));
        assertEquals(expectedMessage, throwable.getMessage());
    }

    @Test
    void shouldKnowIfMultiOrSingleLabel() {
        var multiLabelConfig = GraphSageTrainConfig.of(
            "",
            Optional.empty(),
            Optional.empty(),
            CypherMapWrapper.create(Map.of(
                "modelName", "graphSageModel",
                "degreeAsProperty", true,
                "projectedFeatureDimension", 42
            ))
        );
        assertTrue(multiLabelConfig.isMultiLabel());
        var singleLabelConfig = GraphSageTrainConfig.of(
            "",
            Optional.empty(),
            Optional.empty(),
            CypherMapWrapper.create(Map.of(
                "modelName", "graphSageModel",
                "degreeAsProperty", true
            ))
        );
        assertFalse(singleLabelConfig.isMultiLabel());
    }

    @Nested
    class MultiLabelGraphSageConfigProcTest {

        private static final String GRAPH =
            "CREATE" +
            "  (dan:Person {age: 20, height: 185, weight: 75})," +
            "  (annie:Person {age: 12, height: 124, weight: 42})," +
            "  (matt:Person {age: 67, height: 170, weight: 80})," +
            "  (jeff:Person {age: 45, height: 192, weight: 85})," +
            "  (brie:Person {age: 27, height: 176, weight: 57})," +
            "  (elsa:Person {age: 32, height: 158, weight: 55})," +
            "  (john:Person {age: 35, height: 172, weight: 76})," +
            "  (dan)-[:KNOWS]->(annie)," +
            "  (dan)-[:KNOWS]->(matt)," +
            "  (annie)-[:KNOWS]->(matt)," +
            "  (annie)-[:KNOWS]->(jeff)," +
            "  (annie)-[:KNOWS]->(brie)," +
            "  (matt)-[:KNOWS]->(brie)," +
            "  (brie)-[:KNOWS]->(elsa)," +
            "  (brie)-[:KNOWS]->(jeff)," +
            "  (john)-[:KNOWS]->(jeff)," +
            "  (guitar:Instrument {cost: 1337.0})," +
            "  (synth:Instrument {cost: 1337.0})," +
            "  (bongos:Instrument {cost: 42.0})," +
            "  (trumpet:Instrument {cost: 1337.0})," +
            "  (dan)-[:LIKES]->(guitar)," +
            "  (dan)-[:LIKES]->(synth)," +
            "  (dan)-[:LIKES]->(bongos)," +
            "  (annie)-[:LIKES]->(guitar)," +
            "  (annie)-[:LIKES]->(synth)," +
            "  (matt)-[:LIKES]->(bongos)," +
            "  (brie)-[:LIKES]->(guitar)," +
            "  (brie)-[:LIKES]->(synth)," +
            "  (brie)-[:LIKES]->(bongos)," +
            "  (john)-[:LIKES]->(trumpet)";

        private GraphStoreWithConfig store;

        @BeforeEach
        void setUp() {
            store = ImmutableGraphStoreWithConfig.of(
                GdlFactory.of(GRAPH).build().graphStore(),
                GraphCreateFromStoreConfig.emptyWithName("", "persons")
            );
        }

        @Test
        void testMissingPropertyForSingleLabel() {
            var singleLabelConfig = ImmutableGraphSageTrainConfig.builder()
                .modelName("singleLabel")
                .addFeatureProperties("doesnotexist")
                .addNodeLabel("Person")
                .build();

            assertThatIllegalArgumentException().isThrownBy(
                () -> singleLabelConfig.validateAgainstGraphStore(store)
            ).withMessage("The following node properties are not present for each label in the graph: [doesnotexist]. Properties that exist for each label are [weight, age, height]");
        }

        @Test
        void testMissingPropertyForMultiLabel() {
            var singleLabelConfig = ImmutableGraphSageTrainConfig.builder()
                .modelName("singleLabel")
                .addFeatureProperties("doesnotexist")
                .addNodeLabels("Person", "Instrument")
                .projectedFeatureDimension(4)
                .build();

            assertThatIllegalArgumentException().isThrownBy(
                () -> singleLabelConfig.validateAgainstGraphStore(store)
            ).withMessage("Each property set in `featureProperties` must exist for at least one label. Missing properties: [doesnotexist]");
        }

        @Test
        void testValidConfiguration() {
            var singleLabelConfig = ImmutableGraphSageTrainConfig.builder()
                .modelName("singleLabel")
                .addFeatureProperties("age", "height", "weight")
                .addNodeLabel("Person")
                .addRelationshipType("KNOWS")
                .build();

            assertThatNoException().isThrownBy(
                () -> singleLabelConfig.validateAgainstGraphStore(store)
            );

            var multiLabelConfig = ImmutableGraphSageTrainConfig.builder()
                .from(singleLabelConfig)
                .addFeatureProperties("cost")
                .modelName("multiLabel")
                .addNodeLabel("Instrument")
                .addRelationshipType("KNOWS")
                .projectedFeatureDimension(4)
                .build();

            assertThatNoException().isThrownBy(
                () -> multiLabelConfig.validateAgainstGraphStore(store)
            );
        }
    }
}
