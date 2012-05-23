import org.msgpack.rpc.loop.EventLoop
import org.msgpack.rpc.{Client, Server}
import org.msgpack.ScalaMessagePack
import org.scalatest.FunSuite

class FlakePackSuite extends FunSuite {

  def initNewServer = {
    val loop = EventLoop.start(new ScalaMessagePack)
    val server = new Server(loop)
    server.serve(new IdServer(0))
    server.listen(17000)
    server
  }

  def initNewClient = {
    val clloop = EventLoop.start(new ScalaMessagePack)
    new Client("127.0.0.1", 17000, clloop)
  }

  def shutDownServerAndClient(server: Server, client: Client) = {
    client.close
    server.close
  }

  test("ids should be unique") {
    val server = initNewServer
    val client = initNewClient

    val uids = for (i <- 0 to 10000) yield {
      client.callApply("generateID", Array[java.lang.Object]()).toString()
    }

    shutDownServerAndClient(server, client)

    assert(uids.length == uids.distinct.length)
  }

  test("ids should be time ordered") {
    val server = initNewServer
    val client = initNewClient

    val uids = for (i <- 0 to 10000) yield {
      client.callApply("generateID", Array[java.lang.Object]()).toString()
    }

    shutDownServerAndClient(server, client)

    var allSorted = true
    for (i <- 0 to uids.length - 2) allSorted = uids(0) < uids(1)
    assert(allSorted)
  }

  test("a batch call to ids should retrieve the correct amount of ids") {
    val server = initNewServer
    val client = initNewClient
    val count = 2000

    val ids = client.callApply("generateIDs", Array[java.lang.Object](new java.lang.Integer(count))).asArrayValue().getElementArray

    shutDownServerAndClient(server, client)

    assert(ids.length == count)
  }

  test("a batch call to ids should give unique numbers") {
    println("Test")
    val server = initNewServer
    val client = initNewClient

    val count = 20000
    val uids = client.callApply("generateIDs", Array[java.lang.Object](new java.lang.Integer(count)))
      .asArrayValue()
      .getElementArray
      .map(_.toString())

    shutDownServerAndClient(server, client)

    assert(uids.length == uids.distinct.length)
  }

  test("a batch call to ids should give time ordered ids") {
    val server = initNewServer
    val client = initNewClient

    val count = 20000
    val uids = client.callApply("generateIDs", Array[java.lang.Object](new java.lang.Integer(count)))
      .asArrayValue()
      .getElementArray
      .map(_.toString())

    shutDownServerAndClient(server, client)

    var allSorted = true
    for (i <- 0 to uids.length - 2) allSorted = uids(0) < uids(1)
    assert(allSorted)
  }
}
