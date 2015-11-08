package client

import java.rmi.registry.Registry
import java.rmi.registry.LocateRegistry

object Client {
  def repl: Unit = {
    println("Que funcion quieres ejecutar?")
    println("[? for help] >>> ")
    val ln = readLine()
    ln match {
      case "?" =>
        println("echo\n query")
      case _ =>
        println("implementando!")
    }
    repl
  }

  def main(args: Array[String]): Unit = {
	println("Hola sabroson!")
	try {
	  val registry = LocateRegistry getRegistry("192.168.1.5")
	  val stub = registry.lookup("testservice").asInstanceOf[server.serverTrait]

      repl
	  // println("ASking for a key :D")

	  // val query = stub giveFromRedis("mykey")
      // query match {
      //   case Some(value) => println("Got \""+value+"\"")
      //   case None => println("This key does not exists")
      // }

      // println("Sending \"Hello world!\"")
      // var response = stub echo("Hello world!")
	  // println("Got \""+response+"\"")
	} catch {
	  case e: Exception => e printStackTrace
	}
  }
}
