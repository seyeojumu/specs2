package org.specs2.concurrent

import org.specs2.control.eff.Evaluated
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

case class ExecutorServices(executionContextEval: Evaluated[ExecutionContext],
                            schedulerEval: Evaluated[Scheduler],
                            shutdown: Evaluated[Unit]) {

  implicit lazy val executionContext: ExecutionContext =
    executionContextEval.value

  implicit lazy val scheduler: Scheduler =
    schedulerEval.value

  /** convenience method to shutdown the services when the final future has completed */
  def shutdownOnComplete[A](future: scala.concurrent.Future[A]): ExecutorServices = {
    future.onComplete(_ => shutdown.value)
    this
  }

  def schedule(timedout: =>Unit, duration: FiniteDuration): () => Unit =
    scheduler.schedule(timedout, duration)

}
