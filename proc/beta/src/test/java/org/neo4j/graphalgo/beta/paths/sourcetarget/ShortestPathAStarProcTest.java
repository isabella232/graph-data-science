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
package org.neo4j.graphalgo.beta.paths.sourcetarget;

import org.jetbrains.annotations.NotNull;
import org.junit.Ignore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.neo4j.graphalgo.AlgoBaseProcTest;
import org.neo4j.graphalgo.BaseProcTest;
import org.neo4j.graphalgo.GdsCypher;
import org.neo4j.graphalgo.HeapControlTest;
import org.neo4j.graphalgo.MemoryEstimateTest;
import org.neo4j.graphalgo.NodeLabel;
import org.neo4j.graphalgo.NodeProjection;
import org.neo4j.graphalgo.NodeProjections;
import org.neo4j.graphalgo.PropertyMapping;
import org.neo4j.graphalgo.PropertyMappings;
import org.neo4j.graphalgo.QueryRunner;
import org.neo4j.graphalgo.RelationshipWeightConfigTest;
import org.neo4j.graphalgo.api.DefaultValue;
import org.neo4j.graphalgo.beta.paths.ShortestPathBaseConfig;
import org.neo4j.graphalgo.beta.paths.astar.AStar;
import org.neo4j.graphalgo.beta.paths.dijkstra.DijkstraResult;
import org.neo4j.graphalgo.catalog.GraphCreateProc;
import org.neo4j.graphalgo.config.GraphCreateConfig;
import org.neo4j.graphalgo.config.GraphCreateFromStoreConfig;
import org.neo4j.graphalgo.config.ImmutableGraphCreateFromCypherConfig;
import org.neo4j.graphalgo.config.ImmutableGraphCreateFromStoreConfig;
import org.neo4j.graphalgo.core.CypherMapWrapper;
import org.neo4j.graphalgo.core.GraphLoader;
import org.neo4j.graphalgo.core.loading.GraphStoreCatalog;
import org.neo4j.graphalgo.extension.Neo4jGraph;
import org.neo4j.kernel.internal.GraphDatabaseAPI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.neo4j.graphalgo.beta.paths.ShortestPathBaseConfig.SOURCE_NODE_KEY;
import static org.neo4j.graphalgo.beta.paths.ShortestPathBaseConfig.TARGET_NODE_KEY;
import static org.neo4j.graphalgo.beta.paths.astar.config.ShortestPathAStarBaseConfig.LATITUDE_PROPERTY_KEY;
import static org.neo4j.graphalgo.beta.paths.astar.config.ShortestPathAStarBaseConfig.LONGITUDE_PROPERTY_KEY;
import static org.neo4j.graphalgo.config.GraphCreateFromCypherConfig.NODE_QUERY_KEY;
import static org.neo4j.graphalgo.config.GraphCreateFromStoreConfig.NODE_PROJECTION_KEY;

abstract class ShortestPathAStarProcTest<CONFIG extends ShortestPathBaseConfig> extends BaseProcTest implements
    AlgoBaseProcTest<AStar, CONFIG, DijkstraResult>,
    MemoryEstimateTest<AStar, CONFIG, DijkstraResult>,
    HeapControlTest<AStar, CONFIG, DijkstraResult>,
    RelationshipWeightConfigTest<AStar, CONFIG, DijkstraResult> {

    private static final String NODE_QUERY = "MATCH (n) RETURN id(n) AS id, n.latitude AS latitude, n.longitude AS longitude";

    static final String LONGITUDE_PROPERTY = "longitude";
    static final String LATITUDE_PROPERTY = "latitude";
    static final String COST_PROPERTY = "cost";
    protected static final String GRAPH_NAME = "graph";

    @Neo4jGraph
    private static final String DB_CYPHER =
        "CREATE" +
        "  (nA:Node {latitude: 1.304444,    longitude: 103.717373})" + // name: 'SINGAPORE'
        ", (nB:Node {latitude: 1.1892,      longitude: 103.4689})" + // name: 'SINGAPORE STRAIT'
        ", (nC:Node {latitude: 8.83055556,  longitude: 111.8725})" + // name: 'WAYPOINT 68'
        ", (nD:Node {latitude: 10.82916667, longitude: 113.9722222})" + // name: 'WAYPOINT 70'
        ", (nE:Node {latitude: 11.9675,     longitude: 115.2366667})" + // name: 'WAYPOINT 74'
        ", (nF:Node {latitude: 16.0728,     longitude: 119.6128})" + // name: 'SOUTH CHINA SEA'
        ", (nG:Node {latitude: 20.5325,     longitude: 121.845})" + // name: 'LUZON STRAIT'
        ", (nH:Node {latitude: 29.32611111, longitude: 131.2988889})" + // name: 'WAYPOINT 87'
        ", (nI:Node {latitude: -2.0428,     longitude: 108.6225})" + // name: 'KARIMATA STRAIT'
        ", (nJ:Node {latitude: -8.3256,     longitude: 115.8872})" + // name: 'LOMBOK STRAIT'
        ", (nK:Node {latitude: -8.5945,     longitude: 116.6867})" + // name: 'SUMBAWA STRAIT'
        ", (nL:Node {latitude: -8.2211,     longitude: 125.2411})" + // name: 'KOLANA AREA'
        ", (nM:Node {latitude: -1.8558,     longitude: 126.5572})" + // name: 'EAST MANGOLE'
        ", (nN:Node {latitude: 3.96861111,  longitude: 128.3052778})" + // name: 'WAYPOINT 88'
        ", (nO:Node {latitude: 12.76305556, longitude: 131.2980556})" + // name: 'WAYPOINT 89'
        ", (nP:Node {latitude: 22.32027778, longitude: 134.700000})" + // name: 'WAYPOINT 90'
        ", (nX:Node {latitude: 35.562222,   longitude: 140.059187})" + // name: 'CHIBA'
        ", (nA)-[:TYPE {cost: 29.0}]->(nB)" +
        ", (nB)-[:TYPE {cost: 694.0}]->(nC)" +
        ", (nC)-[:TYPE {cost: 172.0}]->(nD)" +
        ", (nD)-[:TYPE {cost: 101.0}]->(nE)" +
        ", (nE)-[:TYPE {cost: 357.0}]->(nF)" +
        ", (nF)-[:TYPE {cost: 299.0}]->(nG)" +
        ", (nG)-[:TYPE {cost: 740.0}]->(nH)" +
        ", (nH)-[:TYPE {cost: 587.0}]->(nX)" +
        ", (nB)-[:TYPE {cost: 389.0}]->(nI)" +
        ", (nI)-[:TYPE {cost: 584.0}]->(nJ)" +
        ", (nJ)-[:TYPE {cost: 82.0}]->(nK)" +
        ", (nK)-[:TYPE {cost: 528.0}]->(nL)" +
        ", (nL)-[:TYPE {cost: 391.0}]->(nM)" +
        ", (nM)-[:TYPE {cost: 364.0}]->(nN)" +
        ", (nN)-[:TYPE {cost: 554.0}]->(nO)" +
        ", (nO)-[:TYPE {cost: 603.0}]->(nP)" +
        ", (nP)-[:TYPE {cost: 847.0}]->(nX)";

    long idA, idB, idC, idD, idE, idF, idG, idH, idX;
    long[] ids0;
    double[] costs0;


    @BeforeEach
    void setup() throws Exception {
        registerProcedures(
            getProcedureClazz(),
            GraphCreateProc.class
        );

        idA = idFunction.of("nA");
        idB = idFunction.of("nB");
        idC = idFunction.of("nC");
        idD = idFunction.of("nD");
        idE = idFunction.of("nE");
        idF = idFunction.of("nF");
        idG = idFunction.of("nG");
        idH = idFunction.of("nH");
        idX = idFunction.of("nX");

        ids0 = new long[]{idA, idB, idC, idD, idE, idF, idG, idH, idX};
        costs0 = new double[]{0.0, 29.0, 723.0, 895.0, 996.0, 1353.0, 1652.0, 2392.0, 2979.0};

        runQuery(GdsCypher.call()
            .withAnyLabel()
            .withNodeProperty(LATITUDE_PROPERTY)
            .withNodeProperty(LONGITUDE_PROPERTY)
            .withAnyRelationshipType()
            .withRelationshipProperty(COST_PROPERTY)
            .graphCreate(GRAPH_NAME)
            .yields());
    }

    @AfterEach
    void teardown() {
        GraphStoreCatalog.removeAllLoadedGraphs();
    }

    @Override
    public GraphDatabaseAPI graphDb() {
        return db;
    }

    @Override
    public CypherMapWrapper createMinimalConfig(CypherMapWrapper mapWrapper) {
        return mapWrapper
            .withNumber(SOURCE_NODE_KEY, idFunction.of("nA"))
            .withNumber(TARGET_NODE_KEY, idFunction.of("nX"))
            .withString(LONGITUDE_PROPERTY_KEY, LONGITUDE_PROPERTY)
            .withString(LATITUDE_PROPERTY_KEY, LATITUDE_PROPERTY);
    }

    @Override
    public void assertResultEquals(DijkstraResult result1, DijkstraResult result2) {
        assertEquals(result1.pathSet(), result2.pathSet());
    }

    /**
     * From here it's just some voodoo to make all this test machinery work ...
     */
    @Override
    public @NotNull GraphLoader graphLoader(GraphCreateConfig graphCreateConfig) {
        GraphCreateConfig configWithNodeProperty = graphCreateConfig instanceof GraphCreateFromStoreConfig
            ? ImmutableGraphCreateFromStoreConfig
            .builder()
            .from(graphCreateConfig)
            .nodeProperties(PropertyMappings.of(
                PropertyMapping.of(LONGITUDE_PROPERTY),
                PropertyMapping.of(LATITUDE_PROPERTY))
            )
            .build()
            : ImmutableGraphCreateFromCypherConfig
                .builder()
                .from(graphCreateConfig)
                .nodeQuery(NODE_QUERY)
                .build();

        return graphLoader(graphDb(), configWithNodeProperty);
    }

    @Override
    public void loadGraph(String graphName) {
        QueryRunner.runQuery(
            graphDb(),
            GdsCypher.call()
                .withAnyLabel()
                .withNodeProperty(LATITUDE_PROPERTY)
                .withNodeProperty(LONGITUDE_PROPERTY)
                .withAnyRelationshipType()
                .withRelationshipProperty(COST_PROPERTY)
                .graphCreate(graphName)
                .yields()
        );
    }

    @Override
    public CypherMapWrapper createMinimalImplicitConfig(CypherMapWrapper baseMap) {
        baseMap = RelationshipWeightConfigTest.super.createMinimalImplicitConfig(baseMap);
        if (baseMap.containsKey(NODE_PROJECTION_KEY) && !baseMap.containsKey(NODE_QUERY_KEY)) {
            baseMap = baseMap
                .withEntry(NODE_PROJECTION_KEY, NodeProjections.builder()
                    .putProjection(NodeLabel.ALL_NODES, NodeProjection.of("*", PropertyMappings.of(
                        PropertyMapping.of(LONGITUDE_PROPERTY, DefaultValue.forDouble()),
                        PropertyMapping.of(LATITUDE_PROPERTY, DefaultValue.forDouble())
                    ))).build());
        } else if (!baseMap.containsKey(NODE_PROJECTION_KEY) && baseMap.containsKey(NODE_QUERY_KEY)) {
            baseMap = baseMap.withString(NODE_QUERY_KEY, NODE_QUERY);
        }
        return createMinimalConfig(baseMap);
    }

    @Ignore
    @Override
    public void testMemoryEstimateOnExplicitDimensions() {
        // test assumes one node property, we load two
    }

    @Ignore
    @Override
    public void testFailOnMissingNodeLabel() {
        // test adds a node projection to the minimal config input
        // we override this with our own node projection
    }
}
