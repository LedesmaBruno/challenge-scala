package services

import akka.actor.ActorSystem
import dto.{AuthResponseDto, BriefPictureDto, ImagesResponseDto, PictureDto}
import play.api.cache.AsyncCacheApi
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSRequest}

import javax.inject.{Inject, Singleton}
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.util.Success

@Singleton
class ImagesService @Inject()(ws: WSClient, cache: AsyncCacheApi, actorSystem: ActorSystem) {

  private val BASE_URL = "http://interview.agileengine.com"
  private var token = auth
  private var pageCount = 0

  (actorSystem.scheduler scheduleAtFixedRate(initialDelay = 0.second, interval = 1.minute)) { () => initLoadCache }

  // TODO invalid token handler and renewal
  private def auth = {
    val request: WSRequest = ws.url(s"${BASE_URL}/auth")
    val future = request.post(Json.obj("apiKey" -> "23567b218376f79d9415")).map {
      response => response.json.validate[AuthResponseDto].get.token
    }

    Await.result(future, 10 seconds)
  }

  private def initLoadCache = {
    fetchImages()
    if (pageCount > 1)
      for (page <- 2 until pageCount) {
        fetchImages(page)
      }
  }

  private def updateCache(pictures: ListBuffer[BriefPictureDto]) = {
    val futureCachedPictures: Future[Option[ListBuffer[BriefPictureDto]]] = cache.get[ListBuffer[BriefPictureDto]]("images")
    futureCachedPictures.andThen {
      case Success(option) => option match {
        case Some(list) => cache.set("images", list.++(pictures))
        case None => cache.set("images", pictures)
      }
    }
  }

  def fetchImages(page: Int = 1) = {
    val request: WSRequest = ws.url(s"${BASE_URL}/images?page=$page")
      .addHttpHeaders("Authorization" -> s"Bearer $token")
    val future = request.get().map {
      response => response.json.validate[ImagesResponseDto].get
    }

    val response = Await.result[ImagesResponseDto](future, 10 seconds)

    println(s"Loading page $page")
    updateCache(response.pictures.to[ListBuffer])

    pageCount = response.pageCount
  }

  def fetchImageDetail(imageId: String) = {
    val request: WSRequest = ws.url(s"${BASE_URL}/images/$imageId")
      .addHttpHeaders("Authorization" -> s"Bearer $token")

    request.get().map {
      response =>
        if (response.status == 200) Some(response.json.validate[PictureDto].get)
        else None
    }
  }

  def getCachedImages = {
    cache.get[ListBuffer[BriefPictureDto]]("images")
  }

  /**
   * This is not ideal (lot of requests), but the api does not provide a method for getting all the images with its details. So, in order
   * to have in cache the images with its details (`PictureDto` instead of `BriefPictureDto`), i would need to fetch
   * /images/${id} for every id returned in /images (lot of requests) or otherwise, I could also save in cache every
   * picture when a single image is queried (in `fetchImageDetail`) but for the latter, cache wouldn't be loaded with
   * all the images as requested by the challenge.
   */
  def search(author: Option[String], camera: Option[String], tags: Option[String]) = {
    cache.get[List[BriefPictureDto]]("images").map {
      case Some(list) =>
        list.map {
          briefpicture =>
            fetchImageDetail(briefpicture.id).map {
              case Some(pic) => pic
              case None => throw new IllegalStateException
            }
        }
//    TODO Here is missing the logic for filtering. I don't want to waste more time
      case None => List.empty
    }
  }
}
