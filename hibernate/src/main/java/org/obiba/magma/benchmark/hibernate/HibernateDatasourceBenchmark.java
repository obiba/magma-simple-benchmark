package org.obiba.magma.benchmark.hibernate;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.benchmark.AbstractDatasourceBenchmark;
import org.obiba.magma.benchmark.DatasourceBenchmark;
import org.obiba.magma.datasource.hibernate.HibernateDatasource;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.Initialisables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.collect.Sets;

import static com.google.common.collect.Iterables.size;
import static org.obiba.magma.benchmark.BenchmarkResult.Action.IMPORT_DATA;
import static org.obiba.magma.benchmark.BenchmarkResult.Action.READ_ENTITIES;
import static org.obiba.magma.benchmark.BenchmarkResult.Action.READ_TABLES;
import static org.obiba.magma.benchmark.BenchmarkResult.Action.READ_VALUES;
import static org.obiba.magma.benchmark.BenchmarkResult.Action.READ_VALUE_SETS;
import static org.obiba.magma.benchmark.BenchmarkResult.Action.READ_VARIABLES;
import static org.obiba.magma.benchmark.BenchmarkResult.Action.READ_VECTORS;

@Component
@SuppressWarnings("MethodOnlyUsedFromInnerClass")
public class HibernateDatasourceBenchmark extends AbstractDatasourceBenchmark implements DatasourceBenchmark {

  private static final Logger log = LoggerFactory.getLogger(HibernateDatasourceBenchmark.class);

  private static final Logger benchmarkLog = LoggerFactory.getLogger("benchmark");

  private static final String DATASOURCE = "hibernate-benchmark";

  private static final String ONYX_DATA_ZIP = "20-onyx-data.zip";

  private static final String FNAC_ZIP = "FNAC.zip";

  @Autowired
  private TransactionTemplate transactionTemplate;

  @Autowired
  private SessionFactory sessionFactory;

  @Override
  public void setup() throws UnknownHostException {
    dropDatasource();
    createDatasource();
  }

  private void dropDatasource() {
    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        try {
          HibernateDatasource ds = new HibernateDatasource(DATASOURCE, sessionFactory);
          Initialisables.initialise(ds);
          MagmaEngine.get().addDatasource(ds);
          ds.drop();
          MagmaEngine.get().removeDatasource(MagmaEngine.get().getDatasource(DATASOURCE));
          datasource = null;
        } catch(Exception e) {
          log.warn("Error while cleaning datasource", e);
        }
      }
    });
  }

  private void createDatasource() {
    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        Initialisables.initialise(datasource = new HibernateDatasource(DATASOURCE, sessionFactory));
      }
    });
  }

  @Override
  public void copyDatasource(final Datasource source) throws IOException {
    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        stopwatch.reset().start();
        try {
          DatasourceCopier.Builder.newCopier().build().copy(source, datasource);
        } catch(IOException e) {
          throw new RuntimeException(e);
        }
        logResult(IMPORT_DATA);
        benchmarkLog.info("Import in {}", stopwatch);
      }
    });
  }

  @Override
  public Set<ValueTable> getValueTables() {
    return transactionTemplate.execute(new TransactionCallback<Set<ValueTable>>() {
      @Override
      public Set<ValueTable> doInTransaction(TransactionStatus status) {
        stopwatch.reset().start();
        Set<ValueTable> valueTables = datasource.getValueTables();
        logResult(READ_TABLES);
        benchmarkLog.info("Load {} tables in {}", valueTables.size(), stopwatch);
        return valueTables;
      }
    });
  }

  @Override
  public Iterable<Variable> getVariables(final ValueTable valueTable) {
    return transactionTemplate.execute(new TransactionCallback<Iterable<Variable>>() {
      @Override
      public Iterable<Variable> doInTransaction(TransactionStatus status) {
        stopwatch.reset().start();
        Iterable<Variable> variables = valueTable.getVariables();
        logResult(valueTable, READ_VARIABLES);
        benchmarkLog.info("  load {} variables in {}", size(variables), stopwatch);
        return variables;
      }
    });
  }

  @Override
  public Iterable<ValueSet> getValueSets(final ValueTable valueTable) {
    return transactionTemplate.execute(new TransactionCallback<Iterable<ValueSet>>() {
      @Override
      public Iterable<ValueSet> doInTransaction(TransactionStatus status) {
        stopwatch.reset().start();
        Iterable<ValueSet> valueSets = valueTable.getValueSets();
        logResult(valueTable, READ_VALUE_SETS);
        benchmarkLog.info("  load {} valueSets in {}", size(valueSets), stopwatch);
        return valueSets;
      }
    });
  }

  @Override
  public Set<VariableEntity> getEntities(final ValueTable valueTable) {
    return transactionTemplate.execute(new TransactionCallback<Set<VariableEntity>>() {
      @Override
      public Set<VariableEntity> doInTransaction(TransactionStatus status) {
        stopwatch.reset().start();
        Set<VariableEntity> entities = valueTable.getVariableEntities();
        logResult(valueTable, READ_ENTITIES);
        benchmarkLog.info("  load {} entities in {}", size(entities), stopwatch);
        return entities;
      }
    });
  }

  @SuppressWarnings("ConstantConditions")
  @Override
  public void readVectors(final ValueTable valueTable, final Iterable<Variable> variables,
      final Iterable<VariableEntity> entities) {
    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        stopwatch.reset().start();
        for(Variable variable : variables) {
          valueTable.getVariableValueSource(variable.getName()).asVectorSource().getValues(Sets.newTreeSet(entities));
        }
        logResult(valueTable, READ_VECTORS);
        benchmarkLog.info("  load vectors in {}", stopwatch);
      }
    });
  }

  @Override
  public void readValues(final ValueTable valueTable, final Iterable<Variable> variables,
      final Iterable<ValueSet> valueSets) {
    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        stopwatch.reset().start();
        for(Variable variable : variables) {
          for(ValueSet valueSet : valueSets) {
            valueTable.getValue(variable, valueSet);
          }
        }
        logResult(valueTable, READ_VALUES);
        benchmarkLog.info("  load values in {}", stopwatch);
      }
    });
  }

}
