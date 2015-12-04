package uk.co.gcwilliams.http.async

import javax.net.ssl.SSLContext

import io.netty.handler.ssl.SslHandler

/**
 * The SSL client handler
 *
 * Created by GWilliams on 25/08/2015.
 */
object SSLClientHandler {
  val context: SSLContext = SSLContext.getInstance("TLS")
  context.init(null, null, null)
  private val ENGINE = context.createSSLEngine
  ENGINE.setUseClientMode(true)
}

/**
 * The SSL client handler
 *
 * Created by GWilliams on 25/08/2015.
 */
class SSLClientHandler extends SslHandler(SSLClientHandler.ENGINE)
