package org.specs2
package matcher

import control._
import AnyMatchers._

private[specs2]
trait IterableMatchers extends LazyParameters with IterableBeHaveMatchers {
  trait IterableMatcher[T] extends Matcher[Iterable[T]]
  
  def contain[T](t: LazyParameter[T]*): IterableMatcher[T] = new IterableMatcher[T] {
    def apply[S <: Iterable[T]](it: =>Expectable[S]) = {
      val (expected, iterable) = (t.toList.map(_.value), it)
      result(iterable.value.toList.intersect(expected) == expected, 
    		     iterable.description + " contains " + q(expected.mkString(", ")), 
    		     iterable.description + " doesn't contain " + q(expected.mkString(", ")), iterable)
    }
  }
  
  def containInOrder[T](t: LazyParameter[T]*): IterableMatcher[T] = new IterableMatcher[T] {
    def apply[S <: Iterable[T]](v: =>Expectable[S]) = {
      val (expected, iterable) = (t.toList.map(_.value), v)
      result(inOrder(iterable.value.toList, expected), 
             iterable.description + " contains in order " + q(expected.mkString(", ")), 
             iterable.description + " doesn't contain in order " + q(expected.mkString(", ")), iterable)
    }
  }
  private def inOrder[T](l1: List[T], l2: List[T]): Boolean = {
    l1 match {
      case Nil => l2 == Nil
      case other => l2.headOption == l1.headOption && inOrder(l1.drop(1), l2.drop(1)) || inOrder(l1.drop(1), l2)
    }
  }
  private def containLike[T](pattern: =>String, matchType: String) = new IterableMatcher[T] {
    def apply[S <: Iterable[T]](v: =>Expectable[S]) = {
      val (a, iterable) = (pattern, v)
      result(iterable.value.exists(_.toString.matches(a)), 
    		     iterable.description + " contains "+matchType+ " " + q(a), 
    		     iterable.description + " doesn't contain "+matchType+ " " + q(a), iterable)
    }
  }
  def containPattern[T](t: =>String) = containLike(t, "pattern")
  def containMatch[T](t: =>String): IterableMatcher[T] = containLike(".*"+t+".*", "match")

  def empty[T] = beEmpty[T]
  def beEmpty[T] = new IterableMatcher[T] {
    def apply[S <: Iterable[T]](v: =>Expectable[S]) = {
      val iterable = v
      result(iterable.value.isEmpty, 
             iterable.description + " is empty", 
             iterable.description + " is not empty", iterable)
    }
  }
  
}
private[specs2]
object IterableMatchers extends IterableMatchers

private[specs2]
trait IterableBeHaveMatchers { outer: IterableMatchers =>
  implicit def iterable[T](s: MatchResult[Iterable[T]]) = new IterableBeHaveMatchers(s)
  class IterableBeHaveMatchers[T](s: MatchResult[Iterable[T]]) {
    def contain(t: T) = s.apply(outer.contain(t))
    def empty = s.apply(outer.beEmpty[T])
    def beEmpty = s.apply(outer.beEmpty[T])
  }
}