package org.specs2
package specification
package core

import org.specs2.control.consoleLogging
import org.specs2.io.FileSystem
import org.specs2.io._
import org.specs2.main.Arguments
import org.specs2.reporter.LineLogger.NoLineLogger
import org.specs2.specification.process._

object EnvDefault {

  def default: Env =
    Env(Arguments(),
        selectorInstance = (arguments: Arguments) =>
          Arguments.instance(arguments.select.selector).getOrElse(DefaultSelector),

        executorInstance = (arguments: Arguments) =>
          Arguments.instance(arguments.execute.executor).getOrElse(DefaultExecutor),

        lineLogger = NoLineLogger,

        statsRepository = (arguments: Arguments) =>
          StatisticsRepository.file(arguments.commandLine.directoryOr("stats.outdir", "target" / "specs2-reports" / "stats")),

        systemLogger = consoleLogging,

        random = new scala.util.Random,

        fileSystem = FileSystem,

        executionParameters = ExecutionParameters())


  def defaultInstances(env: Env) =
    List(env.arguments.commandLine,
         env.executionEnv,
         env.executionContext,
         env.arguments,
         env)

}
