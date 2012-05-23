import org.msgpack.rpc.loop.EventLoop
import org.msgpack.rpc.{Client, Server}
import org.msgpack.ScalaMessagePack
import org.scalatest.FunSuite

class FlakePackSuite extends FunSuite {

  test("ids should be unique") {
    val loop = EventLoop.start(new ScalaMessagePack)
    val server = new Server(loop)
    server.serve(new IdServer(0))
    server.listen(17000)

    val clloop = EventLoop.start(new ScalaMessagePack)
    val client = new Client("127.0.0.1", 17000, clloop)
    val uids = for (i <- 0 to 10000) yield {
      client.callApply("generateID", Array[java.lang.Object]()).toString()
    }

    client.close
    server.close

    assert(uids.length == uids.distinct.length)
  }

  test("ids should be time ordered") {
    val loop = EventLoop.start(new ScalaMessagePack)
    val server = new Server(loop)
    server.serve(new IdServer(0))
    server.listen(17000)

    val clloop = EventLoop.start(new ScalaMessagePack)
    val client = new Client("127.0.0.1", 17000, clloop)
    val uids = for (i <- 0 to 10000) yield {
      client.callApply("generateID", Array[java.lang.Object]()).toString()
    }

    client.close
    server.close

    var allSorted = true
    for (i <- 0 to uids.length - 2) allSorted = uids(0) < uids(1)
    assert(allSorted)
  }

}
