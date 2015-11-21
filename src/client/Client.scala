package client

import java.rmi.registry.Registry
import java.rmi.registry.LocateRegistry
import java.util.Calendar
import scala.annotation.tailrec
import scala.language.postfixOps

sealed abstract class State
object State {
  case object StartState extends State
  case object MainState extends State // Main esta reservado
  case object Login extends State
  case object Logout extends State
  case object Stop extends State
  case object CreateUser extends State
  case object ProfileState extends State
  case object DirectMState extends State
}

import State._
import server.Shared._

object Client {

  def login(stub: server.ServerTrait, t: Int): (State,String) = {
    if (t < 0) {
      println("Error, number of attempts exceeded")
      (Stop,"")
    } else {
      val user = readLine("User: ")
      val pass = readLine("Password: ")
      val ret = stub login(user,pass)
      if (ret) {
        println("Welcome back! :D")
        (MainState,user)
      }
      else {
        println("Error, incorrect user or password ")
        login(stub,t-1)
      }
    }
  }

  def createUserCli(stub: server.ServerTrait): (State,String) = {
    val user = readLine("User: ")
    val pass = readLine("Password: ")
    val ret = stub createUser(user,pass)

    if (ret)
      println("User created successfully")
    else
      println("Error! This user already exists! :(")

    (StartState,"")
  }

  def follow(stub: server.ServerTrait,userLogged: String) : Unit = {
    val userToFollow = readLine("User:")
    
    val ret = stub follow(userLogged,userToFollow)
    if (ret)
      println("User followed successfully")
    else
      println("Error! Something ocurred =( ")
  }

  def startMenu(stub: server.ServerTrait, user: String): (State,String) = {
    println("What do you want to do?")
    val option = readLine("1 for login, 2 for register, e for exit >>> ")
    option match {
      case "1" => (Login,"")
      case "2" => (CreateUser,"")
      case "e" => (Stop,"")
      case _ => {
        println("I dunno Understand...")
        (StartState,"")
      }
    }
  }

  def sendTweet(stub: server.ServerTrait,
    user: String,
    msg: List[String]): (State,String) = {
    if (msg.isEmpty)
      println("Hey! You must write a message! :P")
    else {
      println("Sending you tweet...")
      val message = msg mkString " "

      if (testTweet(message)) {
        println("Tweet so long, short it!")
        (MainState,user)
      } else{
        val format = new java.text.SimpleDateFormat("dd-MM-yyyy-HH:mm")
        stub sendTweet(DMTweet(user,message,format.format(new java.util.Date())))
        println("Tweet sent! :D")
      }
    }
    (MainState,user)
  }

  def testTweet(message: String): Boolean = message.length > 140

  def retweet(stub: server.ServerTrait, user: String, msg: List[String])
      : (State,String) = {
    if (msg.length != 1)
      println("Hey! Give me a tweet ID!!")
    else {
      val tweetID = msg.head
      println(s"Let's retweet this tweet: $tweetID")
      val ret = stub retweet(user,tweetID)
      ret match {
        case true => println("The tweet was retweeted!! Yay! Party! :D")
        case _ => println("Wow! Don't ask me why but... I cannot retweet that tweet")
      }
    }
    (MainState,user)
  }

  def funcFollow(stub: server.ServerTrait, user: String, userID: List[String])
      : (State,String) = {
    if (userID.length != 1)
      println("Just one argument bro, ONE ARGUMENT!")
    else {
      val ret = stub follow(user,userID.head)
      ret match {
        case true => println(s"You started following ${userID.head}!")
        case _ => println("Are you sure that user does exist?")
      }
    }
    (MainState,user)
  }

  def funcUnfollow(stub: server.ServerTrait, user: String, userID: List[String])
      : (State,String) = {
    if (userID.length != 1)
      println("Just one argument bro, ONE ARGUMENT!")
    else {
      val ret = stub unfollow(user,userID.head)
      ret match {
        case true => println(s"You stoped following ${userID.head}!")
        case _ => println("Are you sure that user does exist?")
      }
    }
    (MainState,user)
  }

  def getFollows(f: String, stub: server.ServerTrait, user: String, list: List[String])
      : (State,String) = {
    val n = if (list.isEmpty)
      10
    else
      list.head.toInt

    if (n <= 0)
      println("Seriously? That's not a real Natural Number! Well Zero really is, but... please")
    else {
      
      val ret = if (f == "followers")
        stub followers(user,n)
      else
        stub following(user,n)
      ret match {
        case List() => println("So sad bro... No one! :'(")
        case _ =>
          ret.map(x => println(s">> $x"))
      }
    }
    (MainState,user)
  }

  def logout(stub: server.ServerTrait, user: String): (State,String) = {
    val ret = stub logoutRemote(user)
    ret match {
      case true => {
        println("User successfully loged out")
        (StartState,user)
      }
      case _ => {
        println("This message souldn't be showed never!")
        (MainState, user)
      }
    }
  }

  def checkProfile(stub: server.ServerTrait, user: String, param: List[String])
      : (State,String) = {
    if (param.length != 1) {
      println("Something went wrong.... We wrote help command just for somthing! USE IT!")
      (ProfileState,user)
    } else {
      val paramStr = param mkString " "
      val ret = stub checkRemoteProfile(user,paramStr)
      ret match {
        case Some(value) => {
          println(s"@ >> $paramStr: $value")
          (ProfileState, user)
        }
        case _ => {
          println("Are you sure you know your own profile :P")
          (ProfileState, user)
        }
      }
    }
  }

  def modifyProfile(stub: server.ServerTrait, user: String, param: List[String])
      : (State,String) = {
    if (param.length != 2) {
      println("Why you don't what to use the 'h' command? T_T")
      (ProfileState, user)
    } else {
      val paramStr = param.head
      val paramVal = param.tail.head
      val ret = stub modifyRemoteProfile(user,paramStr,paramVal)
      ret match {
        case true => {
          println(s"@ >>>>>> $user modified $paramStr with $paramVal")
          (ProfileState, user)
        }
        case _ => {
          println("Are you sure that field exists in your profile? xD")
          (ProfileState, user)
        }
      }
    }
  }

  def profileRepl(stub: server.ServerTrait, user: String): (State,String) = {
    val f::tail = readLine("[? for help] @ >>> ").split(" ").toList
    f match {
      case "check" => checkProfile(stub,user,tail)
      case "modify" => modifyProfile(stub,user,tail)
      case "?" => {
        println("""Modifiying your profile eh? I can help you!
    check <PARAM> => Check your actual PARAM value
    modify <PARAM> <NEWVALUE> => Modify your actual PARAM value
        PARAMS:
           USERNAME => Your user name (that's obvious, isn't it?)
           REALNAME => Your real name
           PASSWORD => Your password (only modify (obviously too (yeah! lisp style!)))
           WORKST => Your current workstation, somethibg as... Awesomeness Inc.
    e => Exit profile menu""")
        (ProfileState,user)
      }
      case "e" => {
        println("Leaving your profile settings!")
        (MainState,user)
      }
      case _ => {
        println("Sorry but... What did you said?")
        (ProfileState,user)
      }
    }
  }

  def getTweets(
    stub: server.ServerTrait,
    user: String,
    list: List[String]): (State,String) = {
    
    val n = list.headOption.map(_.toInt).getOrElse(10)

    if (n <= 0)
      println("Seriously? That's not a real Natural Number! Well Zero really is, but... please")
    else {
      val ret = stub getTweets(user,n)
      ret match {
        case List() => println("Oh man! you don't have any tweet! :(")
        case x => x.map(printFormatTweet _)
      }
    }
    (MainState,user)
  }

  def printFormatTweet(tweet: DMT): Unit = {
    println(s"""=====================================================================
Tweet from: ${tweet.user}
Date: ${tweet.date}
TweetID: ${tweet.id} 
Message: ${tweet.msg}""")
  }

  def selectFunction(stub: server.ServerTrait,
    user: String): (State,String) = {
    val f::tail = readLine("[? for help] >>> ").split(" ").toList
    f match {
      case "getweets" => getTweets(stub,user,tail)
      case "tweet" => sendTweet(stub,user,tail)
      case "reply" => (MainState,user)//replyTweet(stub,user,tail)
      case "directm" => (DirectMState,user)
      case "retweet" => retweet(stub,user,tail)
      case "follow" => funcFollow(stub,user,tail)
      case "unfollow" => funcUnfollow(stub,user,tail)
      case "following" => getFollows("following",stub,user,tail)
      case "followers" => getFollows("followers",stub,user,tail)
      case "profile" => (ProfileState,user)
      case "logout" => logout(stub,user)
      case "?" => {
        println("This is the help, YAY!")
        println("\tgetweets [N] => receive the first N tweets, default 10 :)")
        println("\ttweet <message> => Sends a new tweet!")
        println("\tretweet <tweet_ID> => Retweets de tweet_ID tweet")
        println("\tfollow user_ID => Start following the user_ID profile")
        println("\tunfollow user_ID => Stop following the user_ID profile")
        println("\tfollowing => Returns a list of Following profiles")
        println("\tfollowers => Return a list of your followers profiles")
        println("\tprofile => Here you can check and modify your profile")
        println("\tdirectm => Here you can check and send direct messages")
        println("\tlogout => just... Logout :D")
        println("\t? => show this help")
        println("\nAs you can se, I'm so easy to use ;D")
        (MainState,user)
      }
      case _ => {
        println("Have you thought what did you send me?")
        (MainState,user)
      }
    }
  }

  def sendDM(stub: server.ServerTrait, user: String, msg: List[String]): (State,String) = {
    if (msg.length < 2) 
      println("Hey! You must write a message and a user!")
    else {
      println("Sending you tweet...")
      val userTo = msg.head
      val message = msg.tail mkString " "

      if (testTweet(message)) 
        println("Message so long, short it!")
      else {
        val format = new java.text.SimpleDateFormat("dd-MM-yyyy-HH:mm")
        stub sendDM(DMTweet(user,message,format.format(new java.util.Date())),userTo)
        println("Message sent! :D")
      }
    }
      (DirectMState,user)
  }

    def getDM(
    stub: server.ServerTrait,
    user: String,
    list: List[String]): (State,String) = {
    
    val n = list.headOption.map(_.toInt).getOrElse(10)

    if (n <= 0)
      println("Seriously? That's not a real Natural Number!")
    else {
      val ret = stub getDM(user,n)
      ret match {
        case List() => println("Oh man! you don't have any tweet! :(")
        case x => x.map(printFormatTweet _)
      }
    }
    (DirectMState,user)
  }

  def directMRepl(stub: server.ServerTrait, user: String): (State, String) = {
    val f::tail = readLine("[? for help] DM >>> ").split(" ").toList
    f match {
      case "get" => getDM(stub,user,tail)
      case "send" => sendDM(stub,user,tail)
      case "answer" => (DirectMState,user)//answerDM(user,tail)
      case "e" => (MainState,user)
      case "?" => {
        println("""
Well so... checking your private messages eh? Lemme help you!
    get [N] => Takes your N first DM and shows you
    send <USER> <MSG> => Sends a private message MSG to USER
    answer <ID> <MSG> => Sends an answer to a ID tweet with MSG content
    e => Back to the futu... Main state :D
    ? => Show this help!!
""")
        (DirectMState,user)
      }
      case _ => {
        println("Sorry, I don't understand!")
        (DirectMState,user)
      }
    }
  }

  @tailrec
  def repl(stub: server.ServerTrait, sa: (State, String)): Unit = {
    sa._1 match {
      case StartState => repl(stub,startMenu(stub,""))
      case Login => repl(stub,login(stub,3))
      case CreateUser => repl(stub,createUserCli(stub))
      case MainState => repl(stub,selectFunction(stub,sa._2))
      case ProfileState => repl(stub,profileRepl(stub,sa._2))
      case DirectMState => repl(stub,directMRepl(stub,sa._2))
      case Logout => {
        logout(stub,sa._2)
        (Stop,sa._2)
      }
      case Stop =>
        println("Bye!! See you soon! :D")
    }
  }

  def main(args: Array[String]): Unit = {
    try {
      val registry = LocateRegistry getRegistry("localhost")
      val stub = registry.lookup("tweetorro").asInstanceOf[server.ServerTrait]

      println("Welcome to Tweetorro terminal App!")
      repl(stub,(StartState,""))
    } catch {
      case e: Exception => e printStackTrace
    }
  }
}
