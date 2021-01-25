package dto

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Json, Reads, Writes}

case class BriefPictureDto(id: String, cropped_picture: String)

object BriefPictureDto {
  implicit val reads: Reads[BriefPictureDto] = (
    (JsPath \ "id").read[String] and
      (JsPath \ "cropped_picture").read[String]
    ) (BriefPictureDto.apply _)

  implicit val writes: Writes[BriefPictureDto] = (p: BriefPictureDto) => Json.obj(
    "id" -> p.id,
    "cropped_picture" -> p.cropped_picture
  )
}

case class PictureDto(id: String, author: String, camera: String, tags: String, cropped_picture: String, full_picture: String)

object PictureDto {
  implicit val reads: Reads[PictureDto] = (
    (JsPath \ "id").read[String] and
      (JsPath \ "author").read[String] and
      (JsPath \ "camera").read[String] and
      (JsPath \ "tags").read[String] and
      (JsPath \ "cropped_picture").read[String] and
      (JsPath \ "full_picture").read[String]
    ) (PictureDto.apply _)

  implicit val writes: Writes[PictureDto] = Json.format[PictureDto]
}

