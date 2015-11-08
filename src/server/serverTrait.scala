package server

import java.rmi.Remote
import java.rmi.RemoteException

trait serverTrait extends Remote {
	@throws(classOf[RemoteException])
	def echo(input: String): String
	def giveFromRedis(key: String): Option[String]
}
