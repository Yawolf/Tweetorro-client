package client

import java.rmi.server.UnicastRemoteObject
import java.rmi.registry.Registry
import java.rmi.registry.LocateRegistry
import scala.language.postfixOps

class ClientTraitImpl extends ClientTrait {
  
  def notifyTweets: Unit = println("You have new Tweets!")

  def notifyDM: Unit = println("You have new Direct Messages!")
}

object ClientTraitImpl {
  def main(name: String, port: Int): Unit = {
    try {
      val cb: ClientTrait = new ClientTraitImpl
      val stub = UnicastRemoteObject.exportObject(cb,0).asInstanceOf[ClientTrait]
      val registry = LocateRegistry.createRegistry(port)
      registry.rebind(name, stub)
    } catch {
      case e: Exception => e printStackTrace
    }
  }
}
