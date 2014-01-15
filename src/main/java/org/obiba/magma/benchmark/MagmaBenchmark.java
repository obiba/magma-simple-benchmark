package org.obiba.magma.benchmark;

import java.util.ArrayList;
import java.util.List;
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
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class MagmaBenchmark {

  private static final Logger benchmarkLog = LoggerFactory.getLogger("benchmark");

  private static final String ONYX_5_DATA_ZIP = "5-onyx-data.zip";

  private static final String ONYX_20_DATA_ZIP = "20-onyx-data.zip";

//  private static final String FNAC_ZIP = "FNAC.zip";

  @Autowired
  private List<DatasourceBenchmark> benchmarks = new ArrayList<>();

  private MagmaBenchmark() {}

  public static void main(String... args) throws Exception {
    new ClassPathXmlApplicationContext("/context.xml").getBean(MagmaBenchmark.class).runBenchmarks();
  }

  private void runBenchmarks() throws Exception {

    new MagmaEngine().extend(new MagmaXStreamExtension());

    Datasource fsDatasource = new FsDatasource("fs", FileUtil.getFileFromResource(ONYX_5_DATA_ZIP));
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

  }

}