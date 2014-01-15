package org.obiba.magma.benchmark;

import java.util.Set;

import org.obiba.magma.Datasource;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;

public interface DatasourceBenchmark {

  void setup() throws Exception;

  void copyDatasource(Datasource source) throws Exception;

  Set<ValueTable> getValueTables();

  Iterable<Variable> getVariables(ValueTable valueTable);

  Iterable<ValueSet> getValueSets(ValueTable valueTable);

  Set<VariableEntity> getEntities(ValueTable valueTable);

  void readValues(ValueTable valueTable, Iterable<Variable> variables, Iterable<ValueSet> valueSets);

  void readVectors(ValueTable valueTable, Iterable<Variable> variables, Iterable<VariableEntity> entities);

  void shutdown();
}
