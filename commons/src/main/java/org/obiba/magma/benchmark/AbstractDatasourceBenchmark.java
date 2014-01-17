package org.obiba.magma.benchmark;

import java.util.ArrayList;
import java.util.List;

import org.obiba.magma.Datasource;
import org.obiba.magma.ValueTable;

import com.google.common.base.Stopwatch;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public abstract class AbstractDatasourceBenchmark implements DatasourceBenchmark {

  protected final Stopwatch stopwatch = Stopwatch.createUnstarted();

  protected Datasource datasource;

  protected final List<BenchmarkResult> results = new ArrayList<>();

  @Override
  public List<BenchmarkResult> getResults() {
    return results;
  }

  protected void logResult(ValueTable valueTable, BenchmarkResult.Action action) {
    results.add(new BenchmarkResult(valueTable.getName(), action, stopwatch.stop().elapsed(MILLISECONDS)));
  }

  protected void logResult(BenchmarkResult.Action action) {
    results.add(new BenchmarkResult(action, stopwatch.stop().elapsed(MILLISECONDS)));
  }
}
