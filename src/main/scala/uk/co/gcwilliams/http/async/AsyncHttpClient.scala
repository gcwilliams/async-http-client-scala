package uk.co.gcwilliams.http.async

/**
 * The async HTTP client
 *
 * Created by GWilliams on 02/12/2015.
 *
 */
object AsyncHttpClient {

  /**
   * Creates a new client
   *
   * @param workerCount the worker count
   * @return the client
   */
  def apply(workerCount:Int): AsyncHttpClient = {
    apply(workerCount, 1000, 2, 2)
  }

  /**
   * Creates a new client
   *
   * @param workerCount the worker count
   * @param connectTimeoutMillis the connect timeout
   * @param writeTimeOutSeconds the write timeout
   * @param readTimeoutSeconds the read timeout
   * @return the client
   */
  def apply(workerCount: Int, connectTimeoutMillis: Int, writeTimeOutSeconds: Int, readTimeoutSeconds: Int): AsyncHttpClient = {
    new NettyAsyncHttpClient(workerCount, connectTimeoutMillis, writeTimeOutSeconds, readTimeoutSeconds)
  }
}

/**
 * The async HTTP client
 *
 * Created by GWilliams on 02/12/2015.
 */
trait AsyncHttpClient {

  /**
   * Shuts down the client
   *
   */
  def shutdown(): Unit

  /**
   * Performs a GET request on the specified URL
   *
   * @param url the URL
   * @return the task to execute the action
   */
  def get(url: String): Task[AsyncHttpMessage]
}
