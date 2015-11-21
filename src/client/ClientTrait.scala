package client

import java.rmi.Remote
import java.rmi.RemoteException

trait ClientTrait extends Remote { 
  
  @throws(classOf[RemoteException])
  def notifyDM: Unit

  @throws(classOf[RemoteException])
  def notifyTweets: Unit

}
