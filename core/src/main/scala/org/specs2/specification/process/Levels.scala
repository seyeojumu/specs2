package org.specs2
package specification
package process

import org.specs2.fp._
import Tree._
import data.Trees._
import specification.create._
import DefaultFragmentFactory._
import specification.core._

import scala.math._
import control._
import org.specs2.concurrent.ExecutionEnv
import producer._
import transducers._

/**
 * Compute the "level" of each fragment to be able to represent the whole specification
 * as a tree.
 *
 * In mutable specifications text fragments add one level to the following fragments,
 * Otherwise should and can blocks create Start/End fragments indicating that the level should go up then down
 */
trait Levels {

  def levelsProcess: AsyncTransducer[Fragment, (Fragment, Int)] =
    levelsProcess1.map { case (f, level) => (f, level.l) }
  
  def levelsProcess1: AsyncTransducer[Fragment, (Fragment, Level)] = {
    def sameLevel(f: Fragment, level: Level) = ((f, level), level)
    def nextLevel(f: Fragment, level: Level, next: Level) = ((f, level), next)

    state[ActionStack, Fragment, (Fragment, Level), Level](Level()) {
      // level goes +1 when a new block starts
      case (f @ Fragment(Start,_ ,_), level) => nextLevel(f, level, level.copy(start = true, incrementNext = false))
      case (f , level) if Fragment.isText(f) => nextLevel(f, level, level.copy(start = true, incrementNext = true))
      case (f @ Fragment(End,_ ,_), level)   => nextLevel(f, level, level.copy(start = false, incrementNext = false, max(0, level.l - 1)))
      case (f, level)                        =>
        if (level.incrementNext) nextLevel(f, level, level.copy(start = false, incrementNext = false, l = level.l + 1))
        else                     sameLevel(f, level)
    }
  }

  def fold(fragment: Fragment, level: Level): Level = fragment match {
    // level goes +1 when a new block starts
    case f @ Fragment(Start,_ ,_) => level.copy(start = true, incrementNext = false)
    case f if Fragment.isText(f)  => level.copy(start = true, incrementNext = true)
    case f @ Fragment(End,_ ,_)   => level.copy(start = false, incrementNext = false, max(0, level.l - 1))
    case f                        =>
      if (level.incrementNext) level.copy(start = false, incrementNext = false, l = level.l + 1)
      else                     level
  }


  def levelsToTreeLoc(mapper: Mapper): AsyncTransducer[(Fragment, Int), TreeLoc[Fragment]] = {
    val init = Leaf((DefaultFragmentFactory.text("root"), 0)).loc

    state[ActionStack, (Fragment, Int), TreeLoc[(Fragment, Int)], TreeLoc[(Fragment, Int)]](init) {
      case ((f, level), treeLoc) =>

        val parent = if (level == 0) treeLoc.root else (treeLoc.parentLocs :+ treeLoc).takeWhile(_.getLabel._2 < level).lastOption.getOrElse(treeLoc)
        val newTree = mapper(f) match {
          case Some(fragment) => parent.insertDownLast(Leaf((fragment, level)))
          case None           => treeLoc
        }
        (newTree, newTree)
    }.map(_.map(_._1))
  }

  def treeLoc(fs: Fragments)(ee: ExecutionEnv): Option[TreeLoc[Fragment]] =
    treeLocMap(fs)(identityMapper)(ee)

  def levels(fs: Fragments)(ee: ExecutionEnv): List[Int] =
    fs.contents.pipe(levelsProcess).runList.run(ee).map(_._2).toList

  def levels(f: Fragment)(ee: ExecutionEnv): List[Int] =
    levels(Fragments(f))(ee)

  def levels(structure: SpecStructure)(ee: ExecutionEnv): List[Int] =
    levels(structure.fragments)(ee)

  def treeLocMap(fs: Fragments)(mapper: Mapper)(ee: ExecutionEnv): Option[TreeLoc[Fragment]] =
    fs.contents.pipe(levelsProcess).pipe(levelsToTreeLoc(mapper)).runList.run(ee).lastOption

  def tree(fs: Fragments)(ee: ExecutionEnv): Option[Tree[Fragment]] = treeLoc(fs)(ee).map(_.toTree)
  def treeMap(fs: Fragments)(mapper: Mapper)(ee: ExecutionEnv): Option[Tree[Fragment]] = treeLocMap(fs)(mapper)(ee).map(_.toTree)
  def treeMap(structure: SpecStructure)(mapper: Mapper)(ee: ExecutionEnv): Option[Tree[Fragment]] = treeMap(structure.fragments)(mapper)(ee)

  type Mapper = Fragment => Option[Fragment]
  val identityMapper: Mapper = (f: Fragment) => Some(f)
}

object Levels extends Levels

case class Level(start: Boolean = false, incrementNext: Boolean = false, l: Int = 0)

object Level {
  val Root = Level()
}

