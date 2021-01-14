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
package org.neo4j.gds.ml.nodemodels.multiclasslogisticregression;

import org.neo4j.gds.embeddings.graphsage.ddl4j.tensor.Matrix;
import org.neo4j.gds.embeddings.graphsage.subgraph.LocalIdMap;

public class ClassProbabilities {
    private final Matrix probabilities;
    private final LocalIdMap classMap;

    public ClassProbabilities(Matrix probabilities, LocalIdMap classMap) {
        this.probabilities = probabilities;
        this.classMap = classMap;
    }

    public Matrix probabilities() {
        return probabilities;
    }

    public int classToColumn(long clazz) {
        return classMap.toMapped(clazz);
    }

    public long columnToClass(int col) {
        return classMap.toOriginal(col);
    }
}