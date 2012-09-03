package com.stage

import java.net.{NetworkInterface, InetAddress}
import org.msgpack.rpc.{Request, Server}
import org.msgpack.ScalaMessagePack
import org.msgpack.rpc.loop.EventLoop



object Main {
  def main(args: Array[String]) = {
    val networkInterface = if (args.length > 0) Some(args(0)) else None
    val port = if (args.length > 1) args(1).toInt else 6000
    val macAddr = getMacAddressAsLong(networkInterface)

    val loop = EventLoop.start(new ScalaMessagePack)
    val server = new Server(loop)
    server.serve(new IdGeneratorService(macAddr))

    println("Starting server on port " + port)
    server.listen(port)
  }

  def getMacAddressAsLong(interface: Option[String] = None) = try {
      val ni = interface match {
        case Some(iface) => NetworkInterface.getByName(iface)
        case None => NetworkInterface.getByInetAddress(InetAddress.getLocalHost)
      }
      val mac = ni.getHardwareAddress;
      val seq = for (i <- 0 to mac.length - 1) yield ((mac(i).toLong & 0xff) << (mac.length - i.toLong - 1L) * 8L)
      seq.foldLeft(0: Long) {(b: Long, a: Long) => b | a}
    } catch {
      case e => throw new Exception("Please check your ethernet device settings.")
    }
}