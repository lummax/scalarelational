package org.scalarelational.h2

import org.powerscala.concurrent.Time
import org.scalarelational.Session
import org.scalarelational.extra.StickySessionSupport
import org.scalarelational.table.Table
import org.scalarelational.column.property.{PrimaryKey, Unique, AutoIncrement}
import org.scalatest.{Matchers, WordSpec}

/**
 * @author Matt Hicks <matt@outr.com>
 */
class SessionSpec extends WordSpec with Matchers {
  "Session" when {
    "not sticky" should {
      import SessionDatastore._

      "not yet exist" in {
        hasSession should equal(false)
      }
      "properly create and release" in {
        session {
          hasSession should equal(true)
        }
        hasSession should equal(false)
      }
      "wrap session calls for a single session" in {
        session {
          val s = session
          session {
            s should be theSameInstanceAs session
          }
        }
      }
      "create distinct sessions" in {
        var s1: Session = null
        var s2: Session = null
        session {
          s1 = session
        }
        session {
          s2 = session
        }
        s1.disposed should equal(true)
        s2.disposed should equal(true)
        s1 should not be theSameInstanceAs(s2)
      }
    }
  }
  "sticky" should {
    import StickySessionDatastore._

    "not yet exist" in {
      hasSession should equal(false)
    }
    "properly create and release" in {
      session {
        hasSession should equal(true)
      }
      hasSession should equal(false)
    }
    "wrap session calls for a single session" in {
      session {
        val s = session
        session {
          s should be theSameInstanceAs session
        }
      }
    }
    "pick back up sticky session and make sure it closes gracefully" in {
      var s1: Session = null
      var s2: Session = null
      session {
        s1 = session
        s1.connection     // We have to establish a connection
      }
      session {
        s2 = session      // We have to establish a connection
      }
      s1.disposed should equal(false)
      s2.disposed should equal(false)
      s1 should be theSameInstanceAs s2
      Time.waitFor(1.0, errorOnTimeout = true) {
        s1.disposed
      }
    }
  }
}

object SessionDatastore extends H2Datastore {
  object fruit extends Table("FRUIT") {
    val name = column[String]("name", Unique)
    val id = column[Int]("id", PrimaryKey, AutoIncrement)
  }
}

object StickySessionDatastore extends H2Datastore with StickySessionSupport {
  override def sessionTimeout = 0.5   // 1 second timeout

  object fruit extends Table("FRUIT") {
    val name = column[String]("name", Unique)
    val id = column[Int]("id", PrimaryKey, AutoIncrement)
  }
}