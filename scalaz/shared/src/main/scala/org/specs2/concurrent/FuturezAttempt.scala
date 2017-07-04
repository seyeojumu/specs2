package org.specs2
package concurrent

import scala.concurrent.duration._
import scalaz._, Scalaz._
import scalaz.concurrent._

/**
 * implicit methods to attempt a Scalaz future values with a given timeout and
 * number of retries
 */
trait FuturezAttempt {
  implicit class AttemptFuture[T](f: Future[T])(implicit ee: ExecutionEnv) {
    def attempt: TimeoutFailure \/ T =
      attempt(retries = 0, timeout = 1.second)

    def retry(retries: Int): TimeoutFailure \/ T =
      attempt(retries, timeout = 1.second)

    def attemptFor(timeout: FiniteDuration): TimeoutFailure \/ T =
      attempt(retries = 0, timeout)

    def attempt(retries: Int, timeout: FiniteDuration): TimeoutFailure \/ T = {
      val tf = ee.timeFactor
      val appliedTimeout = timeout * tf.toLong

      def attemptFuture(remainingRetries: Int, totalDuration: FiniteDuration): TimeoutFailure \/ T = {
        f.timed(appliedTimeout.toMillis).run.fold({
          case e if e.getClass == classOf[TimeoutException] =>
            if (remainingRetries <= 0) TimeoutFailure(appliedTimeout, totalDuration, tf).left
            else                       attemptFuture(remainingRetries - 1, totalDuration + appliedTimeout)

          case other: Throwable  => throw other
        },
          r => r.right)
      }

      attemptFuture(retries, 0.second)
    }
  }

}

object FuturezAttempt extends FuturezAttempt
