package com.outr.query.orm

import com.outr.query._
import com.outr.query.Query

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait LazyList[T] extends (() => List[T]) {
  def manifest: Manifest[T]

  def loaded: Boolean
}

object LazyList {
  def Empty[T](implicit manifest: Manifest[T]) = PreloadedLazyList[T](Nil)
}

case class PreloadedLazyList[T](values: List[T])(implicit val manifest: Manifest[T]) extends LazyList[T] {
  def loaded = true

  def apply() = values
}

case class DelayedLazyList[T](table: ORMTable[T], conditions: Conditions)(implicit val manifest: Manifest[T]) extends LazyList[T] {
  @volatile private var _loaded = false
  @volatile private var values: List[T] = null
  def loaded = _loaded

  def apply() = synchronized {
    if (!loaded) {
      load()
    }
    values
  }

  private def load() = {
    val query = Query(table.*, table).where(conditions)
    values = table.query(query).toList
    _loaded = true
  }
}