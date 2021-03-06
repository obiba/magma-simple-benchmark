package org.obiba.magma.benchmark;

import java.util.Collection;
import java.util.Set;

import org.obiba.core.util.FileUtil;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.fs.FsDatasource;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.xstream.MagmaXStreamExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class MagmaBenchmark {

  private static final Logger benchmarkLog = LoggerFactory.getLogger("benchmark");

  private static final String ONYX_5_DATA_ZIP = "5-onyx-data.zip";

  private static final String ONYX_20_DATA_ZIP = "20-onyx-data.zip";

  private static final String FNAC_ZIP = "FNAC.zip";

  @Autowired
  @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
  private Collection<DatasourceBenchmark> benchmarks;

  private MagmaBenchmark() {}

  public static void main(String... args) throws Exception {
    ConfigurableApplicationContext applicationContext = new ClassPathXmlApplicationContext("/benchmark-context.xml");
    applicationContext.registerShutdownHook();
    applicationContext.getBean(MagmaBenchmark.class).runBenchmarks(FNAC_ZIP);
    System.exit(0);
  }

  private void runBenchmarks(String path) throws Exception {

    benchmarkLog.info("Start benchmark with {}", path);

    new MagmaEngine().extend(new MagmaXStreamExtension());

    Datasource fsDatasource = new FsDatasource("fs", FileUtil.getFileFromResource(path));
    Initialisables.initialise(fsDatasource);

    for(DatasourceBenchmark benchmark : benchmarks) {
      benchmarkLog.info("Benchmark {}", benchmark.getClass().getSimpleName());
      benchmark.setup();
      benchmark.copyDatasource(fsDatasource);
      for(ValueTable valueTable : benchmark.getValueTables()) {
        benchmarkLog.info("{}", valueTable.getName());
        Iterable<Variable> variables = benchmark.getVariables(valueTable);
        Iterable<ValueSet> valueSets = benchmark.getValueSets(valueTable);
        Set<VariableEntity> entities = benchmark.getEntities(valueTable);
        benchmark.readValues(valueTable, variables, valueSets);
        benchmark.readVectors(valueTable, variables, entities);
      }
    }
    MagmaEngine.get().shutdown();
  }

}