package com.stage

import org.msgpack.ScalaMessagePack
import math.BigInt

trait IdGenerator {
  def generateID: String
  def generateIDs(nr: Int): List[String]
}

class IdGeneratorService(workerId: Long, var sequence: Long = 0L) extends IdGenerator {
  // | 64 bits timestamp | 48 bits mac | 16 bits seq |
  private[this] val sequenceBits = 16
  private[this] val workerIdShift = sequenceBits
  private[this] val timestampShift = 64
  private[this] val sequenceMask = -1 ^ (-1 << sequenceBits)
  private[this] var lastTimestamp = -1L

  private def tilNextMillis(lastTimestamp: Long): Long = {
    var timestamp = timeGen()
    while (timestamp <= lastTimestamp) {
      timestamp = timeGen()
    }
    timestamp
  }

  private def timeGen(): Long = System.currentTimeMillis()

  private def nextId(): BigInt = synchronized {
    var timestamp = timeGen()

    if (timestamp < lastTimestamp) {
      throw new Exception("Clock is moving backwards!")
    }

    if (lastTimestamp == timestamp) {
      sequence = (sequence + 1) & sequenceMask
      if (sequence == 0) {
        timestamp = tilNextMillis(lastTimestamp)
      }
    } else {
      sequence = 0
    }

    lastTimestamp = timestamp
    (BigInt(timestamp) << timestampShift) + (BigInt((workerId << workerIdShift) | sequence))
  }

  def generateID: String = Base62(nextId)

  def generateIDs(nr: Int): List[String] = (for (i <- 1 to nr) yield generateID).toList
}

object Base62 {
  def apply(value: BigInt) = {
    def toChar(digit: Int) = {
      if (digit < 10) '0' + digit
      else if (digit < 36) 'A' + digit - 10
      else 'a' + digit - 36
    }.asInstanceOf[Char]

    def convert(bi: BigInt): String = {
      if (bi == 0) ""
      else {
        val (div, rem: BigInt) = bi /% 62
        toChar(rem.toInt) + convert(div)
      }
    }
    convert(value).reverse.toString
  }
}