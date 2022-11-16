package com.ubirch.avatar.history

import com.ubirch.avatar.config.Config

/**
  * author: cvandrei
  * since: 2016-10-28
  */
object HistoryIndexUtil {

  def calculateBeginIndex(fromOpt: Option[Int]): Int = {
    fromOpt match {
      case Some(from) => from
      case None       => 0
    }
  }

  def calculateEndIndex(elementCount: Int, from: Int = 0, size: Int = Config.esDefaultPageSize): Option[Int] = {

    from >= elementCount match {

      case true => None

      case false =>
        from + size < elementCount match {
          case true  => Some(from + size - 1)
          case false => Some(elementCount - 1)
        }

    }

  }

  def calculateExpectedSize(beginIndex: Int, endIndex: Int) = endIndex - beginIndex + 1

}
