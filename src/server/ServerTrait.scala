package server

import java.rmi.Remote
import java.rmi.RemoteException

trait ServerTrait extends Remote {

  @throws(classOf[RemoteException])
  def createUser(user: String, pass: String): Boolean

  @throws(classOf[RemoteException])
  def login(user: String, pass: String): Boolean
  
  @throws(classOf[RemoteException])
	def setProfile(nombre: String, alias: String): Boolean

	@throws(classOf[RemoteException])
	def follow(userLogged:String, idUsuarioASeguir: String): Boolean
}


