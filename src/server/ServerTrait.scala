package server

import java.rmi.Remote
import java.rmi.RemoteException


trait ServerTrait extends Remote {

  @throws(classOf[RemoteException])
  def createUser(user: String, pass: String): Boolean

  @throws(classOf[RemoteException])
  def login(user: String, pass: String): Boolean

  @throws(classOf[RemoteException])
  def sendTweet(user: String, message: String): Boolean

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
  def modifyRemoteProfile(user: String, param: String): Boolean
}
