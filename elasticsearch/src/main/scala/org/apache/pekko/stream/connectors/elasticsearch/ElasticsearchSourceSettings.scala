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

package org.apache.pekko.stream.connectors.elasticsearch

import java.util.concurrent.TimeUnit

import scala.concurrent.duration.FiniteDuration

/**
 * Configure Elastiscsearch sources.
 */
final class ElasticsearchSourceSettings private (connection: ElasticsearchConnectionSettings,
    bufferSize: Int,
    includeDocumentVersion: Boolean,
    scrollDuration: FiniteDuration,
    apiVersion: ApiVersion)
    extends SourceSettingsBase[ApiVersion, ElasticsearchSourceSettings](connection,
      bufferSize,
      includeDocumentVersion,
      scrollDuration,
      apiVersion) {

  protected override def copy(connection: ElasticsearchConnectionSettings,
      bufferSize: Int,
      includeDocumentVersion: Boolean,
      scrollDuration: FiniteDuration,
      apiVersion: ApiVersion): ElasticsearchSourceSettings =
    new ElasticsearchSourceSettings(connection = connection,
      bufferSize = bufferSize,
      includeDocumentVersion = includeDocumentVersion,
      scrollDuration = scrollDuration,
      apiVersion = apiVersion)

  override def toString =
    s"""ElasticsearchSourceSettings(connection=$connection,bufferSize=$bufferSize,includeDocumentVersion=$includeDocumentVersion,scrollDuration=$scrollDuration,apiVersion=$apiVersion)"""

}

object ElasticsearchSourceSettings {

  /** Scala API */
  def apply(connection: ElasticsearchConnectionSettings): ElasticsearchSourceSettings =
    new ElasticsearchSourceSettings(connection,
      10,
      includeDocumentVersion = false,
      FiniteDuration(5, TimeUnit.MINUTES),
      ApiVersion.V7)

  /** Java API */
  def create(connection: ElasticsearchConnectionSettings): ElasticsearchSourceSettings =
    new ElasticsearchSourceSettings(connection,
      10,
      includeDocumentVersion = false,
      FiniteDuration(5, TimeUnit.MINUTES),
      ApiVersion.V7)
}
