package helper

import modules.TvApplicationLoader
import play.api.ApplicationLoader.Context
import play.api.{ApplicationLoader, Environment, Mode}

object TestApplicationBuilder {
  def build() = {
    val env =new Environment(new java.io.File("."), ApplicationLoader.getClass.getClassLoader, Mode.Test)
    // Copied from WithApplicationLoader
    val context = Context.create(env)
    new TvApplicationLoader().load(context)
  }
}