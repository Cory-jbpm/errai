/*
 * Copyright 2011 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.ioc.rebind.ioc.graph;

import org.jboss.errai.codegen.meta.MetaClass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Topological sort algorithm to sort the dependency graph prior to emission of the generated code for the IOC
 * bootstrapper.
 *
 * @author Mike Brock
 */
public final class GraphSort {
  private GraphSort() {
  }

  /**
   * Performs of a topological sort and returns a new list of sorted {@link SortUnit}s.
   *
   * @param in
   *         a list of sort units to be sorted.
   *
   * @return a new sorted lis
   */
  public static List<SortUnit> sortGraph(final Collection<SortUnit> in) {
    List<SortUnit> sortUnitList = topologicalSort(new ArrayList<SortUnit>(in));
    Collections.sort(sortUnitList);
    return sortUnitList;
  }


  public static Set<List<SortUnit>> sortAndPartitionGraph(final Collection<SortUnit> in) {
    final Map<MetaClass, Set<SortUnit>> builderMap = new LinkedHashMap<MetaClass, Set<SortUnit>>();

    for (final SortUnit unit : in) {
      final Set<SortUnit> traversal = new HashSet<SortUnit>();
      _traverseGraphExtent(traversal, unit);

      Set<SortUnit> partition = null;
      for (final SortUnit travUnit : traversal) {
        if (builderMap.containsKey(travUnit.getType())) {
          partition = builderMap.get(travUnit.getType());
          break;
        }
      }

      if (partition == null) {
        partition = new HashSet<SortUnit>();
      }

      partition.addAll(traversal);

      for (final SortUnit partitionedUnit : traversal) {
        builderMap.put(partitionedUnit.getType(), partition);
      }
    }

    final Set<List<SortUnit>> consolidated = new LinkedHashSet<List<SortUnit>>();
    final Map<Set<SortUnit>, List<SortUnit>> sortingCache = new IdentityHashMap<Set<SortUnit>, List<SortUnit>>();

    for (final Map.Entry<MetaClass, Set<SortUnit>> metaClassSetEntry : _consolidatePartitions(builderMap).entrySet()) {
      if (!sortingCache.containsKey(metaClassSetEntry.getValue())) {
        sortingCache.put(metaClassSetEntry.getValue(), sortGraph(metaClassSetEntry.getValue()));
      }

      consolidated.add(sortingCache.get(metaClassSetEntry.getValue()));
    }

    return consolidated;
  }

  private static Map<MetaClass, Set<SortUnit>> _consolidatePartitions(final Map<MetaClass, Set<SortUnit>> partitionedMap) {
    final Map<MetaClass, Set<SortUnit>> consolidated = new LinkedHashMap<MetaClass, Set<SortUnit>>(partitionedMap);

    final Set<Map.Entry<MetaClass, Set<SortUnit>>> entries = partitionedMap.entrySet();
    for (final Map.Entry<MetaClass, Set<SortUnit>> metaClassSetEntry : entries) {
      final Set<SortUnit> value = metaClassSetEntry.getValue();

      for (final SortUnit unit : value) {
        final Set<SortUnit> part = partitionedMap.get(unit.getType());

        if (part != value) {
          final Set<SortUnit> combined = new HashSet<SortUnit>();
          combined.addAll(value);
          combined.addAll(part);

          consolidated.put(unit.getType(), combined);
          consolidated.put(metaClassSetEntry.getKey(), combined);
        }
      }
    }

    return consolidated;
  }

  private static void _traverseGraphExtent(final Set<SortUnit> partition,
                                           final SortUnit toVisit) {
    if (partition.contains(toVisit)) return;
    partition.add(toVisit);

    for (final SortUnit dep : toVisit.getDependencies()) {
      _traverseGraphExtent(partition, dep);
    }
  }


  private static List<SortUnit> topologicalSort(List<SortUnit> toSort) {
    final Set<String> visited = new HashSet<String>();
    final List<SortUnit> sorted = new ArrayList<SortUnit>();
    for (SortUnit n : toSort) {
      _topologicalSort(visited, sorted, n);
    }
    return sorted;
  }

  private static void _topologicalSort(final Set<String> V, final List<SortUnit> L, final SortUnit n) {
    if (!V.contains(n.getType().getFullyQualifiedName())) {
      V.add(n.getType().getFullyQualifiedName());
      for (SortUnit m : n.getDependencies()) {
        _topologicalSort(V, L, m);
      }
      L.add(n);
    }
  }
}
