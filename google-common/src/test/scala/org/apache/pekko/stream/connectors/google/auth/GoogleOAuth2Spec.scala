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

package org.apache.pekko.stream.connectors.google.auth

import org.apache.pekko
import pekko.actor.ActorSystem
import pekko.stream.connectors.google.{ GoogleSettings, HoverflySupport }
import pekko.testkit.TestKit
import io.specto.hoverfly.junit.core.SimulationSource.dsl
import io.specto.hoverfly.junit.core.model.RequestFieldMatcher.newRegexMatcher
import io.specto.hoverfly.junit.dsl.HoverflyDsl.service
import io.specto.hoverfly.junit.dsl.ResponseCreators.success
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import java.time.Clock
import scala.concurrent.ExecutionContext
import scala.io.Source

class GoogleOAuth2Spec
    extends TestKit(ActorSystem())
    with AnyWordSpecLike
    with Matchers
    with ScalaFutures
    with BeforeAndAfterAll
    with HoverflySupport {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
    super.afterAll()
  }
  implicit val defaultPatience = PatienceConfig(remainingOrDefault)

  implicit val executionContext: ExecutionContext = system.dispatcher
  implicit val settings = GoogleSettings(system)
  implicit val clock = Clock.systemUTC()

  lazy val privateKey = {
    val inputStream = getClass.getClassLoader.getResourceAsStream("private_pcks8.pem")
    Source.fromInputStream(inputStream).getLines().mkString("\n").stripMargin
  }

  lazy val publicKey = {
    val inputStream = getClass.getClassLoader.getResourceAsStream("key.pub")
    Source.fromInputStream(inputStream).getLines().mkString("\n").stripMargin
  }

  val scopes = Seq("https://www.googleapis.com/auth/service")

  "GoogleTokenApi" should {

    "call the api as the docs want to and return the token" in {
      hoverfly.simulate(
        dsl(
          service("oauth2.googleapis.com")
            .post("/token")
            .queryParam("prettyPrint", "false")
            .body(newRegexMatcher("grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Ajwt-bearer&assertion=*"))
            .willReturn(
              success("""{"access_token": "token", "token_type": "String", "expires_in": 3600}""",
                "application/json"))))

      implicit val settings = GoogleSettings().requestSettings
      GoogleOAuth2.getAccessToken("email", privateKey, scopes).futureValue should matchPattern {
        case AccessToken("token", exp) if exp > (System.currentTimeMillis / 1000L + 3000L) =>
      }
    }
  }
}
