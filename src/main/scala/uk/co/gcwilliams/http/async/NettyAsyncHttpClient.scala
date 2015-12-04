package uk.co.gcwilliams.http.async

import java.net.URI

import io.netty.bootstrap.Bootstrap
import io.netty.buffer.PooledByteBufAllocator
import io.netty.channel._
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.http._
import io.netty.handler.timeout.{ReadTimeoutHandler, WriteTimeoutHandler}
import io.netty.util.ReferenceCountUtil

/**
 * The netty async HTTP client
 *
 * Created by GWilliams on 02/12/2015.
 */
private [async] object NettyAsyncHttpClient {

  private val Host = HttpHeaders.newEntity("Host")

  private val Accept = HttpHeaders.newEntity("Accept")

  private val Json = HttpHeaders.newEntity("application/json")

}

/**
 * The netty async HTTP client
 *
 * Created by GWilliams on 02/12/2015.
 */
private [async] class NettyAsyncHttpClient(
  workerCount: Int,
  connectTimeoutMillis: Int,
  writeTimeoutSeconds: Int,
  readTimeoutSeconds: Int
) extends AsyncHttpClient{

  private [this] val bootstrap = new Bootstrap()

  private [this] val workers = new NioEventLoopGroup(workerCount)

  bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
  bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, new Integer(connectTimeoutMillis))
  bootstrap.group(workers)
  bootstrap.channel(classOf[NioSocketChannel])
  bootstrap.handler(new ChannelInitializer[NioSocketChannel]() {
    override def initChannel(c: NioSocketChannel): Unit = {
        c.pipeline().addLast(new WriteTimeoutHandler(writeTimeoutSeconds))
        c.pipeline().addLast(new ReadTimeoutHandler(readTimeoutSeconds))
        c.pipeline().addLast(new HttpClientCodec())
        c.pipeline().addLast(new HttpObjectAggregator(Integer.MAX_VALUE))
    }
  })

  /**
   * Performs a GET request on the specified URL
   *
   * @param url the URL
   * @return the task to execute the action
   */
  override def get(url: String): Task[AsyncHttpMessage] = {
    new Task[AsyncHttpMessage]((resolve, reject) => {

      val uri = URI.create(url)

      val request = new DefaultFullHttpRequest(
        HttpVersion.HTTP_1_1,
        HttpMethod.GET,
        uri.getRawPath + (if (uri.getRawQuery != null) "?" + uri.getRawQuery else "")
      )

      request.headers().set(NettyAsyncHttpClient.Host, uri.getHost)
      request.headers().set(NettyAsyncHttpClient.Accept, NettyAsyncHttpClient.Json)

      val channelFuture = bootstrap.connect(uri.getHost, if (uri.getPort > -1) uri.getPort else 80)

      channelFuture.addListener(new ChannelFutureListener() {
        override def operationComplete(future: ChannelFuture): Unit = {
          val channel = future.channel()
          if ("https" == uri.getScheme) {
            channel.pipeline.addBefore("write-timeout", "ssl", new SSLClientHandler)
          }
          channel.pipeline().addLast(new HttpRequestHandler(reject))
          if (future.isSuccess) {
            channel.writeAndFlush(request)
            channel.pipeline().addLast(new HttpResponseHandler(url, resolve, reject))
          } else {
            future.channel().pipeline().fireExceptionCaught(future.cause())
          }
        }
      })
    })
  }

  /**
   * Shuts down the client
   *
   */
  override def shutdown(): Unit = workers.shutdownGracefully()
}

private class HttpRequestHandler(private val reject: Exception => Unit) extends ChannelOutboundHandlerAdapter {
  @Override
  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) : Unit = {
    try {
      reject(cause match {
        case exception: Exception => exception
        case _ => new scala.RuntimeException(cause)
      })
    } finally {
      ctx.channel().closeFuture()
      ctx.close()
      ReferenceCountUtil.release(cause)
    }
  }
}

private class HttpResponseHandler(
 private val requestUrl: String,
 private val resolve: AsyncHttpMessage => Unit,
 private val reject: Exception => Unit
) extends SimpleChannelInboundHandler[FullHttpResponse] {

  override def channelRead0(ctx: ChannelHandlerContext, msg: FullHttpResponse): Unit = {
    try {
      val buffer = msg.content()
      val contents = new Array[Byte](buffer.readableBytes())
      msg.content().getBytes(buffer.readerIndex(), contents)
      resolve(new AsyncHttpMessage(
        requestUrl,
        msg.getStatus.code(),
        contents
      ))
    } finally {
      ctx.channel().closeFuture()
      ctx.close()
    }
  }

  override def exceptionCaught(ctx: ChannelHandlerContext , cause: Throwable): Unit = {
    try {
      reject(cause match {
        case exception: Exception => exception
        case _ => new scala.RuntimeException(cause)
      })
    } finally {
      ctx.channel().closeFuture()
      ctx.close()
      ReferenceCountUtil.release(cause)
    }
  }
}