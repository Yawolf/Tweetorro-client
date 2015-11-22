package server

import java.rmi.Remote
import java.rmi.RemoteException

object Shared {
  case class DMTweet(user: String, msg: String, date: String)
  case class DMT(id: String, user: String, msg: String, date: String)
  case class Test(f: Int => Unit)
}

trait ServerTrait extends Remote {
  import Shared._
  
  @throws(classOf[RemoteException])
  def registerForCallback(userName: String,callback: client.ClientTrait): Unit
  
  @throws(classOf[RemoteException])
  def searchUsers(name: String): List[String]

  @throws(classOf[RemoteException])
  def sendDM(dm: DMTweet, userTo: String): Unit
  
  @throws(classOf[RemoteException])
  def getDM(user: String, number: Int): List[DMT]

  @throws(classOf[RemoteException])
  def createUser(user: String, pass: String): Boolean

  @throws(classOf[RemoteException])
  def login(user: String, pass: String): Boolean
  
  @throws(classOf[RemoteException])
  def sendTweet(tweet: DMTweet): Unit

  @throws(classOf[RemoteException])
  def retweet(user: String, tweetID: String): Boolean

  @throws(classOf[RemoteException])
  def follow(user: String, userID: String): Boolean

  @throws(classOf[RemoteException])
  def unfollow(user: String, userID: String): Boolean

  @throws(classOf[RemoteException])
  def followers(user: String, number: Int): List[String]

  @throws(classOf[RemoteException])
  def following(user: String, number: Int): List[String]

  @throws(classOf[RemoteException])
  def logoutRemote(user: String): Boolean

  @throws(classOf[RemoteException])
  def checkRemoteProfile(user: String, param: String): Option[String]

  @throws(classOf[RemoteException])
  def modifyRemoteProfile(user: String, param: String, value: String): Boolean

  @throws(classOf[RemoteException])
  def getTweets(user: String, number: Int): List[DMT]

  @throws(classOf[RemoteException])
  def justRegisterMe(name: String, port: Int): Unit
}



