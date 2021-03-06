package org.scalarelational.instruction

import org.scalarelational.column.ColumnValue
import org.scalarelational.table.Table

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class InsertMultiple(table: Table, rows: Seq[Seq[ColumnValue[_, _]]])
  extends Insert[List[Int]] with Instruction[List[Int]] {
  def result: List[Int] = table.datastore.exec(this)

  def and(nextRow: ColumnValue[_, _]*): InsertMultiple =
    InsertMultiple(table, rows ++ Seq(nextRow))

  override def add(value: ColumnValue[_, _]): InsertMultiple = {
    val filtered = rows.map(_.filterNot(_.column == value.column))
    copy(rows = filtered.map(row => value :: row.toList))
  }
}