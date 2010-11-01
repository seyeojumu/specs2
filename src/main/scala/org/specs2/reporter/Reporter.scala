package org.specs2
package reporter

import control.Exceptions._
import io._
import specification._

/**
 * A Reporter will report the execution of a Specification following 3 steps:
 * 1. an extraction of the Fragments to report (like creating Descriptions for junit)
 *   * filtering out some elements if necessary
 * 2. an ordering of the Fragments to execute:
 *   * action steps must be executed in order
 *   * dependency between Fragments can be specified
 *   * other Fragments can be executed concurrently (unless specified otherwise)
 * 3. a reporting to:
 *   * the console (ConsoleRunner or sbt)
 *   * a listener object (junit or sbt)
 *   * a file (html, xml, junit-report)
 *
 */
private[specs2]
trait Reporter extends Output with Selection with ExecutionStrategy with Exporting {
  def report(spec: BaseSpecification): this.type = {
    val fragments = spec.content.fragments match {
      case SpecStart(n) :: rest => spec.content.fragments
      case rest => SpecStart(name(spec)) +: spec.content.fragments
    }
	  report(new Fragments(() => fragments :+ SpecEnd(name(spec)), spec.content.arguments))
  }
   	  
  def report(fragments: Fragments): this.type = {
    implicit val args = fragments.arguments 
    (select andThen execute andThen export)(fragments)
    this
  }
  
  def name(spec: BaseSpecification) = ClassName.className(spec)
}

private[specs2]
trait AReporter {
  val reporter: Reporter
}

