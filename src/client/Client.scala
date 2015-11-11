package client

import java.rmi.registry.Registry
import java.rmi.registry.LocateRegistry

object Client {
  
  def loginRepl(stub: server.ServerTrait, t: Int): Option[String] = {
    if (t < 0) {
      println("Error, number of attempts exceeded")
      System.exit(1)
      None
    } else {
      val user = readLine("User: ")
      val pass = readLine("Password: ")
      val ret = stub login(user,pass)
      if (ret) {
        println("Welcome back! :D")
        Some(user)
        
      }
      else {
        println("Error, incorrect user or password ")
        loginRepl(stub,t-1)
      }
    }
  }

  def repl(stub: server.ServerTrait, userLogged: String): Unit = {
    println("Que funcion quieres ejecutar?")
    println("[? for help] >>> ")
    val ln = readLine()
    ln match {
      case "?" =>
        println("echo\n query")
      case "follow" =>
        follow(stub,userLogged)
      case _ =>
        println("implementando!")
    }
    repl(stub, userLogged)
  }

  def createUserCli(stub: server.ServerTrait): Unit = {
    val user = readLine("User: ")
    val pass = readLine("Password: ")
    val ret = stub createUser(user,pass)

    if (ret)
      println("User created successfully")
    else
      println("Error! This user already exists! :(")
  }
  def follow(stub: server.ServerTrait,userLogged: String) : Unit = {
    val userToFollow = readLine("User:")
    
    val ret = stub follow(userLogged,userToFollow)
    if (ret)
      println("User followed successfully")
    else
      println("Error! Something ocurred =( ")
  }

  def starting(stub: server.ServerTrait): Unit = {
    println("Welcome to Tweetorro terminal App!")
    println("What do you want to do?")
    val option = readLine("1 for login, 2 for register, 3 for follow >>> ")
    option match {
      case "1" => loginRepl(stub,3)
      case "2" =>
        createUserCli(stub)
        None
      case _ =>
        println("I dunno Understand...")
        starting(stub)
    }
  }

  def main(args: Array[String]): Unit = {
    println("Hola sabroson!")
    try {
      val registry = LocateRegistry getRegistry("localhost")
      val stub = registry.lookup("tweetorro").asInstanceOf[server.ServerTrait]

      val userLogged = starting(stub)
      repl(stub,userLogged)
    }catch {
      case e: Exception => e printStackTrace
    }
  }
}
