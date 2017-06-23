package org.specs2
package reflect

import scala.reflect.ClassTag
import control._

trait Classes extends ClassOperations {

  def createInstance[T <: AnyRef](className: String)(implicit m: ClassTag[T]): Operation[T] =
    ???

  def createInstance[T <: AnyRef](className: String, loader: ClassLoader, defaultInstances: =>List[AnyRef] = Nil)(implicit m: ClassTag[T]): Operation[T] =
    ???

  def createInstanceFromClass[T <: AnyRef](klass: Class[T], loader: ClassLoader, defaultInstances: =>List[AnyRef] = Nil)(implicit m: ClassTag[T]): Operation[T] =
    ???

  /** try to create an instance but return an exception if this is not possible */
  def createInstanceEither[T <: AnyRef](className: String, loader: ClassLoader, defaultInstances: =>List[AnyRef] = Nil)(implicit m: ClassTag[T]): Operation[Throwable Either T] =
    ???

  def loadClassEither[T <: AnyRef](className: String, loader: ClassLoader): Operation[Throwable Either Class[T]] =
    ???

  def loadClass[T <: AnyRef](className: String, loader: ClassLoader): Operation[Class[T]] =
    ???

  def existsClass(className: String, loader: ClassLoader): Operation[Boolean] =
    ???

}

object Classes extends Classes
