package client

import java.rmi.server.UnicastRemoteObject
import java.rmi.registry.Registry
import java.rmi.registry.LocateRegistry

class ClientTraitImpl(user: String) extends ClientTrait {
  var numeros = 0
  // TODO decidir qué notificamos
  
  def getUserName(): String = {
    user
  }
  
  def notifyMe(): Unit = {
    println(numeros)
    numeros += 1
  }
  
}