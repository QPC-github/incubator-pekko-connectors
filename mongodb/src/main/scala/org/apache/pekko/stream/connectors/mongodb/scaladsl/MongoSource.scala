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

package org.apache.pekko.stream.connectors.mongodb.scaladsl

import org.apache.pekko
import pekko.NotUsed
import pekko.stream.scaladsl.Source
import org.reactivestreams.Publisher

object MongoSource {

  def apply[T](query: Publisher[T]): Source[T, NotUsed] =
    Source.fromPublisher(query)

}
