package org.specs2.concurrent

import org.specs2.control.eff.Memoized

import scala.concurrent.ExecutionContext

object ExecutorServicesCreation {
  def fromExecutionContext(ec: =>ExecutionContext): ExecutorServices = {
    ExecutorServices(
      Memoized(ec),
      Memoized(Schedulers.default),
      Memoized(() => ())
    )
  }
}
