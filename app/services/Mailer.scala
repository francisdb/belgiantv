package services

import play.api.Play.current

import play.api.Logger
import com.typesafe.plugin._
import java.io.{PrintStream, ByteArrayOutputStream}
import com.typesafe.plugin.{MailerAPI, MailerPlugin}
import org.apache.commons.mail.EmailException

object Mailer {
  private val LOGGER = Logger(getClass)

  def sendMail(subject: String, reason:Throwable) {
    val mail = use[MailerPlugin].email

    mail.setRecipient("Francis De Brabandere <francisdb@gmail.com>")
    mail.setFrom("BelgianTV <belgiantv@somatik.be>")

    val stacktrace = stacktraceToString(reason)
    val content = "Unhandled serverside exception, please check this ASAP.\n\n" + stacktrace

    mail.setSubject("BelgianTV - " + subject)
    sendAndLogFailure(mail, content)
  }

  private def stacktraceToString(throwable:Throwable) = {
    val oStream = new ByteArrayOutputStream()
    val stream = new PrintStream(oStream)
    throwable.printStackTrace(stream)
    stream.flush()
    val stacktrace = oStream.toString
    stream.close()
    oStream.close()
    stacktrace
  }

  private def sendAndLogFailure(mail: MailerAPI, content: String) {
    try {
      mail.send(content)
    } catch {
      case ex: EmailException =>
        LOGGER.error(ex.getMessage, ex)
    }
  }
}
