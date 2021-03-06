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

public interface AdjacencyList extends AutoCloseable {

    int degree(long index);

    // Cursors

    PropertyCursor rawCursor();

    default PropertyCursor cursor(long offset) {
        return rawCursor().init(offset);
    }

    /**
     * Returns a new, uninitialized delta cursor.
     */
    AdjacencyCursor rawDecompressingCursor();

    /**
     * Get a new cursor initialised on the given offset
     */
    default AdjacencyCursor decompressingCursor(long offset) {
        return rawDecompressingCursor().initializedTo(offset);
    }

    @Override
    void close();
}
