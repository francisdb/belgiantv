package models

import org.specs2.mutable.Specification

class ChannelSpec extends Specification {

  "The channel" should {

    "correctly unify" in {
      //Map(5 -> 2BE HD, 10 -> Animal Planet HD, 14 -> Eurosport HD, 20 -> Sporting HD1, 1 -> VTM HD, 6 -> VIJF HD, 9 -> Discovery Vl HD, 13 -> FoxLife HD, 2 -> één HD, 17 -> RTL TVI HD, 12 -> Sundance HD, 7 -> Cultuur 7 HD, 3 -> VIER HD, 18 -> Club RTL HD, 16 -> La Deux HD, 11 -> BBC One HD, 8 -> Nat Geo HD, 19 -> TF1 HD, 4 -> Canvas HD, 15 -> La Une HD)
      //Map(5 -> La Une HD, 10 -> AB3, 14 -> France 3, 20 -> Sundance FR, 1 -> NPO 1, 6 -> La Deux HD, 9 -> Plug RTL, 13 -> TF1 HD, 2 -> NPO 2, 17 -> Arte Belgique, 12 -> 2M Monde, 7 -> RTL TVI HD, 3 -> NPO 3, 18 -> TV5 Monde, 16 -> France 5, 11 -> La Trois, 8 -> Club RTL HD, 19 -> France O, 4 -> BBC 2, 15 -> France 4)
      //Map(5 -> 2BE HD, 10 -> Ketnet, 14 -> vtmKzoom, 20 -> Cultuur 7 HD, 1 -> VTM HD, 6 -> VIJF HD, 9 -> Play time, 13 -> Sundance HD, 2 -> één HD, 17 -> TMF/Comedy Central, 12 -> njam!, 7 -> Vitaya, 3 -> VIER HD, 18 -> Q-music, 16 -> MENT TV, 11 -> Libelle, 8 -> Acht, 19 -> DOBBIT TV, 4 -> Canvas HD, 15 -> JIM)

      Channel.unify("BBC One HD") must be equalTo "BBC 1"
    }
  }

}
