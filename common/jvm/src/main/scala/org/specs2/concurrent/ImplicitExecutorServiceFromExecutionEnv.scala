package org.specs2.concurrent

trait ImplicitExecutorServiceFromExecutionEnv {
  /**
   * if an implicit execution environment is in scope, it can be used as an executor service
   */
  implicit def executionEnvToExecutorService(implicit ee: ExecutionEnv): ExecutorServices =
    ee.executorServices
}

/**
 * deactivate the conversion between an implicit execution environment to an executor service
 */
trait NoImplicitExecutorServiceFromExecutionEnv extends ImplicitExecutorServiceFromExecutionEnv {
  override def executionEnvToExecutorService(implicit ee: ExecutionEnv): ExecutorServices =
    super.executionEnvToExecutorService(ee)
}

