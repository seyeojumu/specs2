package org.specs2.control

import org.specs2.concurrent.ExecutionEnv
import org.specs2.control.eff.ConsoleEffect.Console
import org.specs2.control.eff.ErrorEffect.{Error, ErrorOrOk, exception}
import org.specs2.control.eff.{Eff, Fx, Fx1, TimedFuture}
import org.specs2.control.eff.WarningsEffect.Warnings
import org.specs2.execute.{AsResult, Result}
import org.specs2.fp.Monoid

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.util.control.NonFatal

trait ExecuteActions {

  def executeAction[A](action: Action[A], printer: String => Unit = s => ())(ee: ExecutionEnv): (Error Either A, List[String]) = {
    implicit val s = ee.scheduledExecutorService
    implicit val ec = ee.executionContext

    type S = Fx.append[Fx.fx2[TimedFuture, ErrorOrOk], Fx.fx2[Console, Warnings]]

    Await.result(action.execSafe.flatMap(_.fold(t => exception[S, A](t), a => Eff.pure[S, A](a))).
      runError.runConsoleToPrinter(printer).runWarnings.into[Fx1[TimedFuture]].runAsync, Duration.Inf)
  }

  def runActionFuture[A](action: Action[A], printer: String => Unit = s => ())(ee: ExecutionEnv): Future[A] = {
    implicit val s = ee.scheduledExecutorService
    implicit val ec = ee.executionContext

    action.runError.runConsoleToPrinter(printer).discardWarnings.execSafe.runAsync.flatMap {
      case Left(t)               => Future.failed(t)
      case Right(Left(Left(t)))  => Future.failed(t)
      case Right(Left(Right(s))) => Future.failed(new Exception(s))
      case Right(Right(a))       => Future.successful(a)
    }
  }

  def runAction[A](action: Action[A], printer: String => Unit = s => ())(ee: ExecutionEnv): Error Either A =
    attemptExecuteAction(action, printer)(ee).fold(
      t => Left(Left(t)),
      other => other._1)

  def attemptExecuteAction[A](action: Action[A], printer: String => Unit = s => ()): Throwable Either (Error Either A, List[String]) =
    try Await.result(action.runError.runConsoleToPrinter(printer).runWarnings.execSafe.runAsync, Duration.Inf)
    catch { case NonFatal(t) => Left(t) }

  def runAction[A](action: Action[A], printer: String => Unit = s => ()): Error Either A =
    attemptExecuteAction(action, printer).fold(
      t => Left(Left(t)),
      other => other._1)

  def attemptAction[A](action: Action[A], printer: String => Unit = s => ()): Throwable Either A =
    runAction(action, printer) match {
      case Left(Left(t)) => Left(t)
      case Left(Right(f)) => Left(new Exception(f))
      case Right(a)      => Right(a)
    }

  /**
   * This implicit allows an Action[result] to be used inside an example.
   *
   * For example to read a database.
   */
  implicit def actionAsResult[T : AsResult]: AsResult[Action[T]] = new AsResult[Action[T]] {
    def asResult(action: =>Action[T]): Result =
      runAction(action).fold(
        err => err.fold(t => org.specs2.execute.Error(t), f => org.specs2.execute.Failure(f)),
        ok => AsResult(ok)
      )
  }

  implicit class actionOps[T](action: Action[T]) {
    def run(implicit e: Monoid[T]): T =
      runAction(action, println) match {
        case Right(a) => a
        case Left(t) => println("error while interpreting an action "+t.fold(Throwables.render, f => f)); Monoid[T].zero
      }

    def runOption: Option[T] =
      runAction(action, println) match {
        case Right(a) => Option(a)
        case Left(t) => println("error while interpreting an action "+t.fold(Throwables.render, f => f)); None
      }

    def when(condition: Boolean): Action[Unit] =
      if (condition) action.as(()) else Actions.ok(())

    def unless(condition: Boolean): Action[Unit] =
      action.when(!condition)

    def whenFailed(error: Error => Action[T]): Action[T] =
      Actions.whenFailed(action, error)

    def |||(other: Action[T]): Action[T] =
      Actions.orElse(action, other)

    def orElse(other: Action[T]): Action[T] =
      Actions.orElse(action, other)
  }


}
