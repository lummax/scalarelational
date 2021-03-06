package org.scalarelational.postgresql

import org.scalarelational.extra.HikariSupport
import org.scalarelational.{AbstractSpecialTypesDatastore, AbstractTableSpec, AbstractTestCrossReferenceDatastore, AbstractTestDatastore}
import org.scalatest.Ignore

import scala.language.postfixOps

/**
 * @author Robert Djubek <envy1988@gmail.com>
 */
@Ignore
class TableSpec extends AbstractTableSpec {
  override def testDatastore = TestDatastore
  override def specialTypes = SpecialTypesDatastore
  override def testCrossReference = TestCrossReferenceDatastore
}

object TestDatastore extends PostgreSQLDatastore(PostgreSQL.Config("localhost", "tablespec", "travis", "pa")) with AbstractTestDatastore with HikariSupport
object TestCrossReferenceDatastore extends PostgreSQLDatastore(PostgreSQL.Config("localhost", "cross_reference", "travis", "pa")) with AbstractTestCrossReferenceDatastore
object SpecialTypesDatastore extends PostgreSQLDatastore(PostgreSQL.Config("localhost", "special_types", "travis", "pa")) with AbstractSpecialTypesDatastore