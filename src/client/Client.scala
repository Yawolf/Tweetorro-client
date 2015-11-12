package client

import java.rmi.registry.Registry
import java.rmi.registry.LocateRegistry
import scala.annotation.tailrec

sealed abstract class State
object State {
  case object StartState extends State
  case object MainState extends State // Main esta reservado
  case object Login extends State
  case object Logout extends State
  case object Stop extends State
  case object CreateUser extends State
}

object Client {

  def login(stub: server.ServerTrait, t: Int): State = {
    if (t < 0) {
      println("Error, number of attempts exceeded")
      Stop
    } else {
      val user = readLine("User: ")
      val pass = readLine("Password: ")
      val ret = stub login(user,pass)
      if (ret) {
        println("Welcome back! :D")
        MainState
      }
      else {
        println("Error, incorrect user or password ")
        loginRepl(stub,t-1)
      }
    }
  }

  def createUserCli(stub: server.ServerTrait): State = {
    val user = readLine("User: ")
    val pass = readLine("Password: ")
    val ret = stub createUser(user,pass)

    if (ret)
      println("User created successfully")
    else
      println("Error! This user already exists! :(")

    StartState
  }

  def startMenu(stub: server.ServerTrait): State = {
    println("What do you want to do?")
    val option = readLine("1 for login, 2 for register, e for exit >>> ")
    option match {
      case "1" => Login
      case "2" => CreateUser
      case "e" => Stop
      case _ =>
        println("I dunno Understand...")
        StartState
    }
  }

  def selectFunction(stub: server.ServerTrait): State = {
    val f::args = readLine("[? for help] >>> ").split(" ")
    f match {
      case "getweets" => getTweets(stub,args)
      case "tweet" => sendTweet(stub,args)
      case "retweet" => retweet(stub,args)
      case "follow" => follow(args)
      case "unfollow" => unfollow(args)
      case "following" => getFollowing(stub)
      case "followers" => getFollowers(stub)
      case "logout" => Stop
      case "h" =>
        println("This is the help, YAY!")
        println("\tgetweets [N] => receive the first N tweets, default 10 :)")
        println("\ttweet <message> => Sends a new tweet!")
        println("\tretweet <tweet_ID> => Retweets de tweet_ID tweet")
        println("\tfollow user_ID => Start following the user_ID profile")
        println("\tunfollow user_ID => Stop following the user_ID profile")
        println("\tfollowing => Returns a list of Following profiles")
        println("\tfollowers => Return a list of your followers profiles")
        println("\tlogout => just... Logout :D")
        println("\th => show this help")
        println("\nAs you can se, I'm so easy to use ;D")
        MainState
    }
  }

  @tailrec
  def repl(s: State): Unit = {
    println("Welcome to Tweetorro terminal App!")
    s match {
      case StartState => repl(startMenu)
      case Login => repl(login(stub, 3))
      case CreateUser => repl(CreateUser(stub))
      case MainState => repl(selectFunction(stub))
      case Stop =>
        println("Bye!! See you soon! :D")
    }
  }

  def main(args: Array[String]): Unit = {
    println("Hola sabroson!")
    try {
      val registry = LocateRegistry getRegistry("localhost")
      val stub = registry.lookup("tweetorro").asInstanceOf[server.ServerTrait]

      starting(stub)
      // repl
    }catch {
      case e: Exception => e printStackTrace
    }
  }
}
