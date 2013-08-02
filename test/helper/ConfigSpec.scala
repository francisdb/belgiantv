package helper

import services.PlayUtil
import org.specs2.matcher.ThrownExpectations


trait ConfigSpec extends ThrownExpectations{
  def skipIfMissingConfig(property:String) = {
    if(!PlayUtil.configExists(property)){
      skipped(s"$property is not set")
    }
  }
}
