package controllers

import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}
import services.ImagesService

import javax.inject.{Inject, Singleton}
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext

@Singleton
class ImageSearchController @Inject()(cc: ControllerComponents, imagesService: ImagesService)(implicit exec: ExecutionContext) extends AbstractController(cc) {

  // TODO Check Json.toJson(IMMUTABLE LIST). Had to convert it to ListBuffer.
  def searchImages(author: Option[String], camera: Option[String], tags: Option[String]) = Action.async {
    imagesService.search(author, camera, tags).map {
      list => Ok(Json.toJson(list.to[ListBuffer]))
    }
  }

  def getImageDetail(id: String) = Action.async {
    imagesService.fetchImageDetail(id).map {
      case Some(pic) => Ok(Json.toJson(pic))
      case None => NotFound
    }
  }

  def getCachedImages = Action.async {
    imagesService.getCachedImages.map {
      case Some(list) => Ok(Json.toJson(list.to[ListBuffer]))
      case None => NotFound
    }
  }
}
