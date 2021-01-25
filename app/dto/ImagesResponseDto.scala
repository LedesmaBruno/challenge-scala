package dto

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Reads}

case class ImagesResponseDto(pictures: List[BriefPictureDto], page: Int, pageCount: Int, hasMore: Boolean)

object ImagesResponseDto {
  implicit val reads: Reads[ImagesResponseDto] = (
    (JsPath \ "pictures").read[List[BriefPictureDto]] and
      (JsPath \ "page").read[Int] and
      (JsPath \ "pageCount").read[Int] and
      (JsPath \ "hasMore").read[Boolean]
    )(ImagesResponseDto.apply _)
}