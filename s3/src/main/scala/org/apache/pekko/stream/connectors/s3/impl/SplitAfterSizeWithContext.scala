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

package org.apache.pekko.stream.connectors.s3.impl

import org.apache.pekko
import pekko.annotation.InternalApi
import pekko.stream.scaladsl.{ Flow, SubFlow }
import pekko.stream.stage.{ GraphStage, GraphStageLogic, InHandler, OutHandler }
import pekko.stream.{ Attributes, FlowShape, Inlet, Outlet }
import pekko.util.ByteString

/**
 * Internal Api
 *
 * Splits up a byte stream source into sub-flows of a minimum size while maintaining a Context. Does not attempt to
 * create chunks of an exact size. Unlike `SplitAfterSize` there is no maximum size since this would imply splitting
 * up a Context which is not possible to do if the Context is generic.
 *
 * This also means that `SplitAfterSizeContext` currently doesn't support buffering since that would require a way to
 * serialize the Context in the case of a disk buffer which is currently unsupported (we would have to add in a
 * C => ByteString serializer in the public API)
 */
@InternalApi private[impl] object SplitAfterSizeWithContext {
  def apply[I, M, C](minChunkSize: Int)(
      in: Flow[(I, C), (ByteString, C), M]): SubFlow[(ByteString, C), M, in.Repr, in.Closed] = {

    in.via(insertMarkers(minChunkSize)).splitWhen(_ == NewStream).collect {
      case (bs: ByteString, context: C @unchecked) => (bs, context)
    }
  }

  private case object NewStream

  private def insertMarkers[C](minChunkSize: Long) =
    new GraphStage[FlowShape[(ByteString, C), Any]] {
      val in = Inlet[(ByteString, C)]("SplitAfterSize.in")
      val out = Outlet[Any]("SplitAfterSize.out")
      override val shape = FlowShape.of(in, out)

      override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
        new GraphStageLogic(shape) with OutHandler with InHandler {
          var count: Int = 0
          override def onPull(): Unit = pull(in)

          override def onPush(): Unit = {
            val (elem, context) = grab(in)
            count += elem.size
            if (count >= minChunkSize) {
              count = 0
              emitMultiple(out, (elem, context) :: NewStream :: Nil)
            } else emit(out, (elem, context))
          }

          setHandlers(in, out, this)
        }
    }

}
