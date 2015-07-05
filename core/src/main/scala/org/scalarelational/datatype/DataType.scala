package org.scalarelational.datatype

import java.sql.{Blob, Timestamp}

import org.powerscala.enum.{EnumEntry, Enumerated}
import org.powerscala.reflect._
import org.scalarelational.column.WrappedString
import org.scalarelational.column.property.{IgnoreCase, NumericStorage}
import org.scalarelational.model.ColumnLike

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait DataType[T] {
  def sqlType(column: ColumnLike[_]): String
  def toSQLType(column: ColumnLike[_], value: T): Any
  def fromSQLType(column: ColumnLike[_], value: Any): T
}

object DataTypeGenerators {
  def option[T](implicit dt: DataType[T]): DataType[Option[T]] = new DataType[Option[T]] {
    def sqlType(column: ColumnLike[_]) = dt.sqlType(column)

    def toSQLType(column: ColumnLike[_], value: Option[T]): Any = {
      value match {
        case None => null
        case Some(t) => dt.toSQLType(column, t)
      }
    }

    def fromSQLType(column: ColumnLike[_], value: Any): Option[T] =
      value match {
        case null => None
        case t => Some(dt.fromSQLType(column, t))
      }
  }
}

object BooleanDataType extends DataType[Boolean] {
  def sqlType(column: ColumnLike[_]) = "BOOLEAN"
  def toSQLType(column: ColumnLike[_], value: Boolean) = value
  def fromSQLType(column: ColumnLike[_], value: Any) = value.asInstanceOf[Boolean]
}

object IntDataType extends DataType[Int] {
  def sqlType(column: ColumnLike[_]) = "INTEGER"
  def toSQLType(column: ColumnLike[_], value: Int) = value
  def fromSQLType(column: ColumnLike[_], value: Any) = value.asInstanceOf[Int]
}

object JavaIntDataType extends DataType[java.lang.Integer] {
  def sqlType(column: ColumnLike[_]) = "INTEGER"
  def toSQLType(column: ColumnLike[_], value: java.lang.Integer) = value
  def fromSQLType(column: ColumnLike[_], value: Any) = value.asInstanceOf[java.lang.Integer]
}

object LongDataType extends DataType[Long] {
  def sqlType(column: ColumnLike[_]) = "BIGINT"
  def toSQLType(column: ColumnLike[_], value: Long) = value
  def fromSQLType(column: ColumnLike[_], value: Any) = value.asInstanceOf[Long]
}

object JavaLongDataType extends DataType[java.lang.Long] {
  def sqlType(column: ColumnLike[_]) = "BIGINT"
  def toSQLType(column: ColumnLike[_], value: java.lang.Long) = value
  def fromSQLType(column: ColumnLike[_], value: Any) = value.asInstanceOf[java.lang.Long]
}

object DoubleDataType extends DataType[Double] {
  def sqlType(column: ColumnLike[_]) = "DOUBLE"
  def toSQLType(column: ColumnLike[_], value: Double) = value
  def fromSQLType(column: ColumnLike[_], value: Any) = value.asInstanceOf[Double]
}

object JavaDoubleDataType extends DataType[java.lang.Double] {
  def sqlType(column: ColumnLike[_]) = "DOUBLE"
  def toSQLType(column: ColumnLike[_], value: java.lang.Double) = value
  def fromSQLType(column: ColumnLike[_], value: Any) = value.asInstanceOf[java.lang.Double]
}

object BigDecimalDataType extends DataType[BigDecimal] {
  def sqlType(column: ColumnLike[_]) = {
    val numericStorage =
      column.get[NumericStorage](NumericStorage.Name)
        .getOrElse(NumericStorage.DefaultBigDecimal)
    s"DECIMAL(${numericStorage.precision}, ${numericStorage.scale})"
  }
  def toSQLType(column: ColumnLike[_], value: BigDecimal) = value.underlying()
  def fromSQLType(column: ColumnLike[_], value: Any) = BigDecimal(value.asInstanceOf[java.math.BigDecimal])
}

object StringDataType extends DataType[String] {
  val VarcharType = s"VARCHAR(${Int.MaxValue})"
  val VarcharIngoreCaseType = s"VARCHAR_IGNORECASE(${Int.MaxValue})"

  def sqlType(column: ColumnLike[_]) =
    if (column.has(IgnoreCase)) VarcharIngoreCaseType
    else VarcharType
  def toSQLType(column: ColumnLike[_], value: String) = value
  def fromSQLType(column: ColumnLike[_], value: Any) = value.asInstanceOf[String]
}

object WrappedStringDataType extends DataType[WrappedString] {
  def sqlType(column: ColumnLike[_]) =
    if (column.has(IgnoreCase)) StringDataType.VarcharIngoreCaseType
    else StringDataType.VarcharType
  def toSQLType(column: ColumnLike[_], value: WrappedString) = value.value
  def fromSQLType(column: ColumnLike[_], value: Any) = column.manifest.runtimeClass.create(Map("value" -> value.asInstanceOf[String]))
}

object ByteArrayDataType extends DataType[Array[Byte]] {
  def sqlType(column: ColumnLike[_]) = "BINARY(1000)"
  def toSQLType(column: ColumnLike[_], value: Array[Byte]) = value
  def fromSQLType(column: ColumnLike[_], value: Any) = value.asInstanceOf[Array[Byte]]
}

object BlobDataType extends DataType[Blob] {
  def sqlType(column: ColumnLike[_]) = "BLOB"
  def toSQLType(column: ColumnLike[_], value: Blob) = value
  def fromSQLType(column: ColumnLike[_], value: Any) = value.asInstanceOf[Blob]
}

object TimestampDataType extends DataType[Timestamp] {
  def sqlType(column: ColumnLike[_]) = "TIMESTAMP"
  def toSQLType(column: ColumnLike[_], value: Timestamp) = value
  def fromSQLType(column: ColumnLike[_], value: Any) = value.asInstanceOf[Timestamp]
}

class EnumDataType[T <: EnumEntry](implicit manifest: Manifest[T]) extends DataType[T] {
  val enumerated = manifest.runtimeClass.instance
    .getOrElse(throw new RuntimeException(s"Unable to find companion for ${manifest.runtimeClass}"))
    .asInstanceOf[Enumerated[T]]

  def sqlType(column: ColumnLike[_]) = s"VARCHAR(${Int.MaxValue})"
  def toSQLType(column: ColumnLike[_], value: T) = value.name
  def fromSQLType(column: ColumnLike[_], value: Any) =
    enumerated(value.asInstanceOf[String])
}