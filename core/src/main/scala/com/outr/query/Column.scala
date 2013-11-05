package com.outr.query

import scala.util.matching.Regex
import org.powerscala.reflect._

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class Column[T](name: String,
                     notNull: Boolean = false,
                     autoIncrement: Boolean = false,
                     primaryKey: Boolean = false,
                     unique: Boolean = false,
                     foreignKey: Option[Column[T]] = None)
                    (implicit val manifest: Manifest[T], val table: Table) {
  lazy val classType: EnhancedClass = manifest.runtimeClass

  def apply(value: T) = ColumnValue[T](this, value)

  def ===(value: T) = DirectCondition(this, Operator.Equal, value)
  def <>(value: T) = DirectCondition(this, Operator.NotEqual, value)
  def !=(value: T) = DirectCondition(this, Operator.NotEqual, value)
  def >(value: T) = DirectCondition(this, Operator.GreaterThan, value)
  def <(value: T) = DirectCondition(this, Operator.LessThan, value)
  def >=(value: T) = DirectCondition(this, Operator.GreaterThanOrEqual, value)
  def <=(value: T) = DirectCondition(this, Operator.LessThanOrEqual, value)
  def between(range: Seq[T]) = RangeCondition(this, Operator.Between, range)
  def like(regex: Regex) = LikeCondition(this, regex)
  def in(range: Seq[T]) = RangeCondition(this, Operator.In, range)
}