/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * license agreements; and to You under the Apache License, version 2.0:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * This file is part of the Apache Pekko project, derived from Akka.
 */

/*
 * Copyright (C) since 2016 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.stream.connectors.sqs.scaladsl

import org.apache.pekko
import pekko.stream.connectors.sqs.MessageAttributeName
import pekko.stream.connectors.testkit.scaladsl.LogCapturing
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MessageAttributeNameSpec extends AnyFlatSpec with Matchers with LogCapturing {

  it should "not allow names which have periods at the beginning" in {
    a[IllegalArgumentException] should be thrownBy {
      MessageAttributeName(".failed")
    }
  }

  it should "not allow names which have periods at the end" in {
    a[IllegalArgumentException] should be thrownBy {
      MessageAttributeName("failed.")
    }

  }

  it should "reject names which are longer than 256 characters" in {
    a[IllegalArgumentException] should be thrownBy {
      MessageAttributeName(
        "A.really.realy.long.attribute.name.that.is.longer.than.what.is.allowed.256.characters.are.allowed." +
        "however.they.cannot.contain.anything.other.than.alphanumerics.hypens.underscores.and.periods.though" +
        "you.cant.have.more.than.one.consecutive.period.they.are.also.case.sensitive")
    }
  }
  it should "reject names with multiple sequential periods" in {
    a[IllegalArgumentException] should be thrownBy {
      MessageAttributeName("multiple..periods")
    }
  }

}
