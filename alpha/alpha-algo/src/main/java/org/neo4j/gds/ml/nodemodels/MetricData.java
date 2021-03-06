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
package org.neo4j.gds.ml.nodemodels;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.neo4j.graphalgo.annotation.ValueClass;

import java.util.List;

@ValueClass
@JsonSerialize
@JsonDeserialize
public interface MetricData {


    /**
     * Train metrics
     * @return the metric stats for each candidate model on the train set
     */
    List<ConcreteModelStats> train();

    /**
     * Validation metrics
     * @return the metric stats for each candidate model on the validation set
     */
    List<ConcreteModelStats> validation();

    /**
     * Outer train metric
     * @return the metric value for the winner model on outer training set
     */
    double outerTrain();

    /**
     * Test metric
     * @return the metric value for the winner model on test set (holdout)
     */
    double test();

    static MetricData of(List<ConcreteModelStats> train, List<ConcreteModelStats> validation, double outerTrain, double test) {
        return ImmutableMetricData.of(train, validation, outerTrain, test);
    }
}

