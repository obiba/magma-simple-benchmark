package org.obiba.magma.benchmark.mongodb;

import java.io.IOException;
import java.util.Set;

import org.obiba.magma.Datasource;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.benchmark.AbstractDatasourceBenchmark;
import org.obiba.magma.benchmark.DatasourceBenchmark;
import org.obiba.magma.datasource.mongodb.MongoDBDatasourceFactory;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.Initialisables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;
import com.mongodb.MongoClient;

import static com.google.common.collect.Iterables.size;
import static org.obiba.magma.benchmark.BenchmarkResult.Action.IMPORT_DATA;
import static org.obiba.magma.benchmark.BenchmarkResult.Action.READ_ENTITIES;
import static org.obiba.magma.benchmark.BenchmarkResult.Action.READ_TABLES;
import static org.obiba.magma.benchmark.BenchmarkResult.Action.READ_VALUES;
import static org.obiba.magma.benchmark.BenchmarkResult.Action.READ_VALUE_SETS;
import static org.obiba.magma.benchmark.BenchmarkResult.Action.READ_VARIABLES;
import static org.obiba.magma.benchmark.BenchmarkResult.Action.READ_VECTORS;

@Component
public class MongoDBDatasourceBenchmark extends AbstractDatasourceBenchmark implements DatasourceBenchmark {

  private static final Logger benchmarkLog = LoggerFactory.getLogger("benchmark");

  private static final String DATASOURCE = "mongo-benchmark";

  private static final String DB_URL = "mongodb://localhost/" + DATASOURCE;

  @Override
  public void setup() throws Exception {
    MongoClient client = new MongoClient();
    client.dropDatabase(DATASOURCE);
    Initialisables.initialise(datasource = new MongoDBDatasourceFactory(DATASOURCE, DB_URL).create());
  }

  @Override
  public void copyDatasource(Datasource source) throws IOException {
    stopwatch.reset().start();
    DatasourceCopier.Builder.newCopier().build().copy(source, datasource);
    logResult(IMPORT_DATA);
    benchmarkLog.info("Import in {}", stopwatch);
  }

  @Override
  public Set<ValueTable> getValueTables() {
    stopwatch.reset().start();
    Set<ValueTable> valueTables = datasource.getValueTables();
    logResult(READ_TABLES);
    benchmarkLog.info("Load {} tables in {}", valueTables.size(), stopwatch);
    return valueTables;
  }

  @Override
  public Iterable<Variable> getVariables(ValueTable valueTable) {
    stopwatch.reset().start();
    Iterable<Variable> variables = valueTable.getVariables();
    logResult(valueTable, READ_VARIABLES);
    benchmarkLog.info("  load {} variables in {}", size(variables), stopwatch);
    return variables;
  }

  @Override
  public Iterable<ValueSet> getValueSets(ValueTable valueTable) {
    stopwatch.reset().start();
    Iterable<ValueSet> valueSets = valueTable.getValueSets();
    logResult(valueTable, READ_VALUE_SETS);
    benchmarkLog.info("  load {} valueSets in {}", size(valueSets), stopwatch);
    return valueSets;
  }

  @Override
  public Set<VariableEntity> getEntities(ValueTable valueTable) {
    stopwatch.reset().start();
    Set<VariableEntity> entities = valueTable.getVariableEntities();
    logResult(valueTable, READ_ENTITIES);
    benchmarkLog.info("  load {} entities in {}", size(entities), stopwatch);
    return entities;
  }

  @Override
  public void readValues(ValueTable valueTable, Iterable<Variable> variables, Iterable<ValueSet> valueSets) {
    stopwatch.reset().start();
    for(Variable variable : variables) {
      for(ValueSet valueSet : valueSets) {
        Value value = valueTable.getValue(variable, valueSet);
        if(!value.isNull()) value.getValue();
      }
    }
    logResult(valueTable, READ_VALUES);
    benchmarkLog.info("  load values in {}", stopwatch);
  }

  @SuppressWarnings("ConstantConditions")
  @Override
  public void readVectors(ValueTable valueTable, Iterable<Variable> variables, Iterable<VariableEntity> entities) {
    stopwatch.reset().start();
    for(Variable variable : variables) {
      Iterable<Value> values = valueTable.getVariableValueSource(variable.getName()).asVectorSource()
          .getValues(Sets.newTreeSet(entities));
      for(Value value : values) {
        if(!value.isNull()) value.getValue();
      }
    }
    logResult(valueTable, READ_VECTORS);
    benchmarkLog.info("  load vectors in {}", stopwatch);
  }

}
