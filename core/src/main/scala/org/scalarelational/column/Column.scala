package org.scalarelational.column

import org.scalarelational.column.property.ColumnProperty
import org.scalarelational.datatype.DataType
import org.scalarelational.table.Table

/**
 * @author Matt Hicks <matt@outr.com>
 */
private[scalarelational] class Column[T, S](val name: String,
                                         val dataType: DataType[T, S],
                                         val manifest: Manifest[T],
                                         val table: Table,
                                         val props: Seq[ColumnProperty]
                                        ) extends ColumnLike[T, S] {
  table.addColumn(this)     // Add this column to the table
  this.props(props: _*)

  lazy val classType = manifest.runtimeClass
  lazy val longName = s"${table.tableName}.$name"
  lazy val index = table.columns.indexOf(this)
  lazy val fieldName = table.fieldName(this)

  def as(alias: String) = ColumnAlias[T, S](this, None, None, Option(alias))

  override def toString = name
}