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

package org.apache.pekko.stream.connectors.google

import org.apache.pekko
import pekko.actor.ActorSystem
import pekko.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import pekko.http.scaladsl.model.HttpMethods.POST
import pekko.http.scaladsl.model.{ ContentTypes, HttpRequest, Uri }
import pekko.http.scaladsl.unmarshalling.Unmarshaller
import pekko.stream.connectors.google.scaladsl.`X-Upload-Content-Type`
import pekko.stream.scaladsl.Source
import pekko.testkit.TestKit
import pekko.util.ByteString
import io.specto.hoverfly.junit.core.SimulationSource.dsl
import io.specto.hoverfly.junit.dsl.HoverflyDsl.service
import io.specto.hoverfly.junit.dsl.ResponseCreators.{ created, serverError, success }
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import spray.json.{ JsObject, JsValue }

class ResumableUploadSpec
    extends TestKit(ActorSystem("ResumableUploadSpec"))
    with AnyWordSpecLike
    with Matchers
    with BeforeAndAfterAll
    with ScalaFutures
    with HoverflySupport {

  implicit val patience = PatienceConfig(remainingOrDefault)

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
    super.afterAll()
  }

  "ResumableUpload" should {

    "complete interrupted upload" in {

      hoverfly.simulate(
        dsl(
          service("example.com")
            .post("/")
            .queryParam("uploadType", "resumable")
            .queryParam("prettyPrint", "false")
            .header("Authorization", "Bearer yyyy.c.an-access-token")
            .header("X-Upload-Content-Type", "application/octet-stream")
            .willReturn(success().header("Location", "https://example.com/upload123"))
            .put("/upload123")
            .queryParam("prettyPrint", "false")
            .header("Authorization", "Bearer yyyy.c.an-access-token")
            .header("Content-Range", "bytes 0-9/10")
            .body("helloworld")
            .willReturn(serverError().header("Content-Type", "application/json").body("{}"))
            .put("/upload123")
            .queryParam("prettyPrint", "false")
            .header("Authorization", "Bearer yyyy.c.an-access-token")
            .header("Content-Range", "bytes */*")
            .willReturn(success().header("Range", "bytes=0-4"))
            .put("/upload123")
            .queryParam("prettyPrint", "false")
            .header("Authorization", "Bearer yyyy.c.an-access-token")
            .header("Content-Range", "bytes 5-9/10")
            .body("world")
            .willReturn(created().header("Content-Type", "application/json").body("{}"))))

      import implicits._
      implicit val um =
        Unmarshaller.messageUnmarshallerFromEntityUnmarshaller(sprayJsValueUnmarshaller).withDefaultRetry

      val result = Source
        .single(ByteString("helloworld"))
        .runWith(
          ResumableUpload[JsValue](
            HttpRequest(POST,
              Uri("https://example.com?uploadType=resumable"),
              List(`X-Upload-Content-Type`(ContentTypes.`application/octet-stream`)))))

      result.futureValue shouldEqual JsObject.empty
    }

  }

}
