package client

import java.rmi.Remote
import java.rmi.RemoteException

trait ClientTrait extends Remote { 
  
  @throws(classOf[RemoteException])
  def notifyMe(): Unit
  
  @throws(classOf[RemoteException])
  def getUserName():String
}
