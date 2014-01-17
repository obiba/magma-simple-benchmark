package org.obiba.magma.benchmark;

import javax.annotation.Nullable;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;

public class BenchmarkResult {

  public enum Action {
    IMPORT_DATA, READ_TABLES, READ_VARIABLES, READ_VALUE_SETS, READ_ENTITIES, READ_VALUES, READ_VECTORS
  }

  @Nullable
  private final String table;

  private final Action action;

  private final long elapsedMillis;

  public BenchmarkResult(@Nullable String table, Action action, long elapsedMillis) {
    this.table = table;
    this.action = action;
    this.elapsedMillis = elapsedMillis;
  }

  public BenchmarkResult(Action action, long elapsedMillis) {
    this(null, action, elapsedMillis);
  }

  @Nullable
  public String getTable() {
    return table;
  }

  public Action getAction() {
    return action;
  }

  public long getElapsedMillis() {
    return elapsedMillis;
  }

  public String formatDuration() {
    return PeriodFormat.getDefault().print(new Period(elapsedMillis));
  }

}
