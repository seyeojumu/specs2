package org.specs2
package specification
package core

import execute._
import process._
import control._
import org.specs2.concurrent._

import scala.concurrent._, duration._

class ExecutionSpec(implicit ee: ExecutionEnv) extends Specification { def is = s2"""

 A link is executed by getting the corresponding specification ref status in the Statistics store
   the Stats is the stats of the spec + specs += 1 $linkExecution

 An execution can be created from a result throwing a FailureException $withFailureException

"""

  def linkExecution = { env1: Env =>
    val store = StatisticsRepositoryCreation.memory
    val env = env1.setStatisticRepository(store)
    val stats =  Stats(specs = 2, failures = 1, examples = 1)
    store.storeStatistics(getClass.getName, stats).runOption

    Execution.specificationStats(getClass.getName).result(env) must beLike {
      case DecoratedResult(s: Stats, r) =>
        (s must_== Stats(specs = 3, failures = 1, examples = 1)) and
          (r.isSuccess must beFalse)
    }

  }

  def withFailureException = { env: Env =>
    val failure = Failure("ko")
    Execution.withEnv(_ => {throw new FailureException(failure); success}).result(env) === failure
  }

  /**
   * HELPERS
   */

  implicit class ExecutionOps(e: Execution) {
    def result(env: Env): Result =
      Await.result(e.startExecution(env).executedResult.runNow(ee.executorServices), 10.seconds).result
  }
}
