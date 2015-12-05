## Async HTTP Client

Build on top of [Netty](http://netty.io/)

### Example

    // 1. create a client
    val client = AsyncHttpClient(10)

    // 2. get the bbc new page
    val firstImage = client.get("http://www.bbc.co.uk/news").chain(page => {

      // 3. parse the page
      val doc = Jsoup.parse(new String(page.response, Charset.forName("utf-8")))

      // 4. get the first image src
      val src = doc.select(".responsive-image img").first().attr("src")

      // 5. get the image
      client.get(src).map(image => image.response.length)
    })

    // 6. fork task, actually running it
    firstImage.fork(size => {

      // 7. print out the image size
      println(s"The size of the first image on the bbc news homepage is ${size}")

      // 8. shutdown the client
      client.shutdown()

    }, error => {

      // 9. print any error
      println(error)

      // 10. shutdown the client
      client.shutdown()
    })
