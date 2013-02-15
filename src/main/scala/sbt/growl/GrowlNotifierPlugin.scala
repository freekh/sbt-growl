import sbt._
import Keys._
import akka.actor._

class GrowlNotifier extends Actor {
  var last = -1L

  def receive = {
    case (msg: String, title: String, image: String) => 
      import scala.sys.process.Process
      if ((System.currentTimeMillis - last) > 500) {
        last = System.currentTimeMillis
        Process(Seq("growlnotify","--title",title,"--image",image,"--message",msg)).!!
      }
    case m =>
      println("CATASTROPHIC FAILURE: got unexepected message" + m) //can't log
  }
}

object GrowlNotifier extends Plugin{
  lazy val growlActor = {
    import akka.actor._

    val classloader = ActorSystem.getClass.getClassLoader
    val thread = Thread.currentThread
    val oldLoader = thread.getContextClassLoader
    try {
      thread.setContextClassLoader(classloader)
      val system = ActorSystem("GrowlNotification")
      system.actorOf(Props[GrowlNotifier])
    } finally {
      thread.setContextClassLoader(oldLoader)
    }
  }

  val growlCompileNotification = TaskKey[Unit]("growl-compile-notification")
  val growlCompileNotificationTask = (compile in Compile, baseDirectory, state) map { (a, dir, state) => 
    growlActor ! ("Finished compiling: "+dir.getAbsolutePath, "Compilation completed!", 
      dir.getAbsolutePath+"/public/images/favicon.png")
  }

  override lazy val projectSettings = super.projectSettings  ++ Seq(
    GrowlNotifier.growlCompileNotification <<= GrowlNotifier.growlCompileNotificationTask.triggeredBy(compile in Compile))
}
