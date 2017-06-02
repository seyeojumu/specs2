package org.specs2
package specification
package core

import control._
import io.FileSystem
import main.Arguments
import reporter.LineLogger.NoLineLogger
import specification.process._

object EnvDefault {

  def default: Env =
    Env(
      Arguments(),
      selectorInstance = (arguments: Arguments) => DefaultSelector,

      executorInstance = (arguments: Arguments) => DefaultExecutor,

      lineLogger = NoLineLogger,

      statsRepository = (arguments: Arguments) => StatisticsRepositoryStore.memory,

      systemLogger = consoleLogging,

      random = new scala.util.Random,

      fileSystem = FileSystem,

      executionParameters = ExecutionParameters())


  def inaccessibleFileSystem: FileSystem = new FileSystem {

  }

  def defaultInstances(env: Env) =
    List(env.arguments.commandLine,
      env.executionEnv,
      env.executionContext,
      env.arguments,
      env)

}
