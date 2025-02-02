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

package docs.scaladsl

import org.apache.pekko
import pekko.actor.ActorSystem
import pekko.stream.connectors.reference._
import pekko.stream.connectors.reference.scaladsl.Reference
import pekko.stream.connectors.testkit.scaladsl.LogCapturing
import pekko.stream.scaladsl.{ Flow, Sink, Source }
import pekko.testkit.TestKit
import pekko.util.ByteString
import pekko.{ Done, NotUsed }
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.collection.immutable
import scala.concurrent.Future

/**
 * Append "Spec" to every Scala test suite.
 */
class ReferenceSpec extends AnyWordSpec with BeforeAndAfterAll with ScalaFutures with Matchers with LogCapturing {

  implicit val system: ActorSystem = ActorSystem("ReferenceSpec")

  final val ClientId = "test-client-id"

  "reference connector" should {

    /**
     * Type annotations not generally needed on local variables.
     * However it allows to check if the types are really what we want.
     */
    "compile settings" in {
      val providedAuth: Authentication.Provided =
        Authentication.Provided().withVerifier(c => true)

      val noAuth: Authentication.None =
        Authentication.None

      val settings: SourceSettings = SourceSettings(ClientId)

      settings.withAuthentication(providedAuth)
      settings.withAuthentication(noAuth)
    }

    "compile source" in {
      // #source
      val settings: SourceSettings = SourceSettings(ClientId)

      val source: Source[ReferenceReadResult, Future[Done]] =
        Reference.source(settings)
      // #source

      source
    }

    "compile flow" in {
      // #flow
      val flow: Flow[ReferenceWriteMessage, ReferenceWriteResult, NotUsed] =
        Reference.flow()
      // #flow

      flow
    }

    "run source" in {
      val source = Reference.source(SourceSettings(ClientId))

      val msg = source.runWith(Sink.head).futureValue
      msg.data should contain theSameElementsAs Seq(ByteString("one"))
    }

    "run flow" in {
      val flow = Reference.flow()
      val source = Source(
        immutable.Seq(
          ReferenceWriteMessage()
            .withData(immutable.Seq(ByteString("one")))
            .withMetrics(Map("rps" -> 20L, "rpm" -> 30L)),
          ReferenceWriteMessage().withData(
            immutable.Seq(
              ByteString("two"),
              ByteString("three"),
              ByteString("four"))),
          ReferenceWriteMessage().withData(
            immutable.Seq(
              ByteString("five"),
              ByteString("six"),
              ByteString("seven")))))

      val result = source.via(flow).runWith(Sink.seq).futureValue

      result.flatMap(_.message.data) should contain theSameElementsAs Seq(
        "one",
        "two",
        "three",
        "four",
        "five",
        "six",
        "seven").map(ByteString.apply)

      result.head.metrics.get("total") should contain(50L)
    }

    "resolve resource from application config" in {
      val result = Source
        .single(ReferenceWriteMessage().withData(immutable.Seq(ByteString("one"))))
        .via(Reference.flowWithResource())
        .runWith(Sink.seq)

      result.futureValue.flatMap(_.message.data).map(_.utf8String) shouldBe Seq("one default msg")
    }

    "use resource from attributes" in {
      val resource = Resource(ResourceSettings("attributes msg"))

      val result = Source
        .single(ReferenceWriteMessage().withData(immutable.Seq(ByteString("one"))))
        .via(Reference.flowWithResource().withAttributes(ReferenceAttributes.resource(resource)))
        .runWith(Sink.seq)

      result.futureValue.flatMap(_.message.data).map(_.utf8String) shouldBe Seq("one attributes msg")
    }

  }

  override def afterAll() =
    TestKit.shutdownActorSystem(system)

}
