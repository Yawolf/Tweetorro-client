package client

import java.rmi.Remote
import java.rmi.RemoteException

trait ClientTrait extends Remote { 
  
  @throws(classOf[RemoteException])
  def notifyDM: Unit

  @throws(classOf[RemoteException])
  def notifyTweets: Unit
  
  @throws(classOf[RemoteException])
  def stopCallback(name: String): Unit
  
  @throws(classOf[RemoteException])
  def setPort(port: Int): Unit

}
