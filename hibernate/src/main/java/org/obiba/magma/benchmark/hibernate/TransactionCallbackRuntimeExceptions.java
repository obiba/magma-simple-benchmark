package org.obiba.magma.benchmark.hibernate;

import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

abstract class TransactionCallbackRuntimeExceptions extends TransactionCallbackWithoutResult {

  protected abstract void doAction(TransactionStatus status) throws Exception;

  @Override
  protected void doInTransactionWithoutResult(TransactionStatus status) {
    try {
      doAction(status);
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }
}
