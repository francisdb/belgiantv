package services

import play.api.Logger
import java.io.{PrintStream, ByteArrayOutputStream}
import org.apache.commons.mail.EmailException

import play.api.libs.mailer._
import javax.inject.Inject

class Mailer @Inject() (mailerClient: MailerClient){
  private[this] val LOGGER = Logger(getClass)

  def sendMail(subject: String, reason:Throwable) {

    val stacktrace = stacktraceToString(reason)
    val content = "Unhandled serverside exception, please check this ASAP.\n\n" + stacktrace

    val email = Email(
      "BelgianTV - " + subject,
      "BelgianTV <belgiantv@somatik.be>",
      Seq("Francis De Brabandere <francisdb@gmail.com>"),
      // sends text, HTML or both...
      bodyText = Some(content)
    )
    sendAndLogFailure(email)
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

  private def sendAndLogFailure(email: Email) {
    try {
      mailerClient.send(email)
    } catch {
      case ex: EmailException =>
        LOGGER.error(ex.getMessage, ex)
    }
  }
}
