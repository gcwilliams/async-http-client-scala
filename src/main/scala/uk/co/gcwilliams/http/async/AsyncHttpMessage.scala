package uk.co.gcwilliams.http.async

/**
 * The async HTTP message
 *
 * Created by GWilliams on 02/12/2015.
 */
class AsyncHttpMessage(
  val requestUrl: String,
  val statusCode: Int,
  val headers: Seq[(String, String)],
  val body: Array[Byte]
)
