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
}

import State._
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
    val option = readLine("1 for login, 2 for register, 3 for exit >>> ")
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

  def sendTweet(stub: server.ServerTrait, user: String, msg: List[String]):
      (State,String) = {
    if (msg.isEmpty)
      println("Hey! You must write a message! :P")
    else {
      println("Sending you tweet...")
      val message = msg mkString " "

      if (testTweet(message)) {
        println("Tweet so long, short it!")
        (MainState,user)
      } else{
        val format = new java.text.SimpleDateFormat("dd-MM-yyyy")
        val ret = stub sendTweet((user,message,format.format(new java.util.Date())))
          ret match {
            case true => println("Tweew sent! :D")
            case _ => println("Woops! Something went wrong :(")
          }
      }
    }
    (MainState,user)
  }

  def testTweet(message: String): Boolean = message.length <= 140

  def retweet(stub: server.ServerTrait, user: String, msg: List[String])
      : (State,String) = {
    if (msg.length != 1)
      println("Hey! Give me a tweet ID!!")
    else {
      val tweetID = msg.head
      println("Let's retweet this tweet: $tweetID")
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

  def getFollows(f: String, stub: server.ServerTrait, user: String, n: Int)
      : (State,String) = {
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
          ret.map(x => println(">> $x"))
      }
    }
    (MainState,user)
  }

  def logout(stub: server.ServerTrait, user: String): (State,String) = {
    val ret = stub logoutRemote(user)
    ret match {
      case true => {
        println("User successfully loged out")
        (Stop,user)
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
      val paramVal = param.tail mkString " "
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
      case "h" => {
        println("""Modifiying your profile eh? I can help you!
    check <PARAM> => Check your actual PARAM value
    modify <PARAM> <NEWVALUE> => Modify your actual PARAM value
        PARAMS:
           username => Your user name (that's obvious, isn't it?)
           realname => Your real name
           passwd => Your password (only modify (obviously too (yeah! lisp style!)))
           workst => Your current workstation, somethibg as... Awesomeness Inc.""")
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

  def getTweets(stub: server.ServerTrait, user: String, n: Int): (State,String) = {
    if (n <= 0)
      println("Seriously? That's not a real Natural Number! Well Zero really is, but... please")
    else {
      val ret = stub getTweets(user,n)
      ret match {
        case List() => println("Oh man! you don't have any tweet! :(")
        case List(x) => List(x).map(x => printFormatTweet(x))
        case _ => println("You will never see this message!")
      }
    }
    (MainState,user)
  }

  def printFormatTweet(tweet: (String, String, String, String)): Unit = {
    println(s"""Tweet from: ${tweet._1}
Date: ${tweet._3}
TweetID: ${tweet._4} 
Message: ${tweet._2}

=====================================================================""")
  }

  def selectFunction(stub: server.ServerTrait,
    user: String): (State,String) = {
    val f::tail = readLine("[? for help] >>> ").split(" ").toList
    f match {
      case "getweets" => getTweets(stub,user,tail.head.toInt)
      case "tweet" => sendTweet(stub,user,tail)
      case "retweet" => retweet(stub,user,tail)
      case "follow" => funcFollow(stub,user,tail)
      case "unfollow" => funcUnfollow(stub,user,tail)
      case "following" => getFollows("following",stub,user,tail.head.toInt)
      case "followers" => getFollows("followers",stub,user,tail.head.toInt)
      case "profile" => (ProfileState,user)
      case "logout" => (Logout,user)
      case "h" => {
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
        (MainState,user)
      }
      case _ => {
        println("Have you thought what did you send me?")
        (MainState,user)
      }
    }
  }

  @tailrec
  def repl(stub: server.ServerTrait, sa: (State, String)): Unit = {
    println("Welcome to Tweetorro terminal App!")
    sa._1 match {
      case StartState => repl(stub,startMenu(stub,""))
      case Login => repl(stub,login(stub,3))
      case CreateUser => repl(stub,createUserCli(stub))
      case MainState => repl(stub,selectFunction(stub,sa._2))
      case ProfileState => repl(stub,profileRepl(stub,sa._2))
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
      repl(stub,(StartState,""))
    } catch {
      case e: Exception => e printStackTrace
    }
  }
}
