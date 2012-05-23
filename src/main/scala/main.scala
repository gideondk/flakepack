import java.math.BigInteger
import java.net.{NetworkInterface, InetAddress}
import org.msgpack.rpc.{Client, Request, Server}
import org.msgpack.ScalaMessagePack
import org.msgpack.rpc.loop.EventLoop


class IdServer(workerId: Long, var sequence: Long = 0L) extends org.msgpack.rpc.dispatcher.Dispatcher {
  // | 64 bits timestamp | 48 bits mac | 16 bits seq |
  private[this] val sequenceBits = 16
  private[this] val workerIdShift = sequenceBits
  private[this] val timestampShift = 64
  private[this] val sequenceMask = -1 ^ (-1 << sequenceBits)
  private[this] var lastTimestamp = -1L

  protected def tilNextMillis(lastTimestamp: Long): Long = {
    var timestamp = timeGen()
    while (timestamp <= lastTimestamp) {
      timestamp = timeGen()
    }
    timestamp
  }

  protected def timeGen(): Long = System.currentTimeMillis()

  protected def nextId(): BigInteger = synchronized {
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
    new BigInteger(timestamp.toString)
      .shiftLeft(timestampShift)
      .add(BigInteger.valueOf((workerId << workerIdShift) | sequence))
  }

  def dispatch(request: Request): Unit = {
    if (request.getMethodName == "generateID") request.sendResult(Base62.encode(nextId))
    else request.sendError("Method unknown.")
  }
}

object Main {
  def main(args: Array[String]) = {
    val networkInterface = if (args.length > 0) Some(args(0)) else None
    val port = if (args.length > 1) args(1).toInt else 6000
    val macAddr = getMacAddressAsLong(networkInterface)

    val loop = EventLoop.start(new ScalaMessagePack)
    val server = new Server(loop)
    server.serve(new IdServer(macAddr))

    println("Starting server on port " + port)
    server.listen(port)
  }

  def getMacAddressAsLong(interface: Option[String] = None) = {
    var macAddressAsLong: Long = 0
    try {
      val ni = {
        interface.flatMap {
          iface =>
            Some(NetworkInterface.getByName(iface))
        }.getOrElse(NetworkInterface.getByInetAddress(InetAddress.getLocalHost))
      }
      val mac = ni.getHardwareAddress;
      for (i <- 0 to mac.length - 1)
        macAddressAsLong |= ((mac(i).toLong & 0xff) << (mac.length - i.toLong - 1L) * 8L)
    } catch {
      case e => throw new Exception("Please check your ethernet device settings.")
    }
    macAddressAsLong
  }
}