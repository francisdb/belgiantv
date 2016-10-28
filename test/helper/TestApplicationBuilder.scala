package helper

import modules.TvApplicationLoader
import play.api.{ApplicationLoader, Environment, Mode}

object TestApplicationBuilder {
  def build() = {
    val env =new Environment(new java.io.File("."), ApplicationLoader.getClass.getClassLoader, Mode.Test)
    // Copied from WithApplicationLoader
    val context = ApplicationLoader.createContext(env)
    new TvApplicationLoader().load(context)
  }
}