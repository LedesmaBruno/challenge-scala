package dto

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Reads}

case class AuthResponseDto(auth: Boolean, token: String)

object AuthResponseDto {
  implicit val reads: Reads[AuthResponseDto] = (
    (JsPath \ "auth").read[Boolean] and
      (JsPath \ "token").read[String]
    )(AuthResponseDto.apply _)
}
