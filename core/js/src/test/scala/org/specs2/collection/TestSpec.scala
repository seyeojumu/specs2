package org.specs2.collection

import org.specs2.{Specification}

import scala.scalajs.reflect.annotation.EnableReflectiveInstantiation

@EnableReflectiveInstantiation
class TestSpec extends Specification { def is =
  "test" ! ok

}
