# web-spider
Web-Crawler
<br>One server actor to accept url from  the client actor and posts result back to the client actor (client actor will provide URL and depth of crawling)
<br>Actor to collect crawled links per client request and provide result to crawl server
<br>Actor which is responsible for getting body of the web page, searching links present in it and sending it back to link collector. 
---
client is the entry point from where the execution begins

`StartingPoint` is an actor of `ServerActor`, using which, Client sends message `StartCrawling` to ServerActor
message contains url to crawl and depth of crawling 

At serverActor : When ServerActor finds message `StartCrawling` in the mailbox ( in `receive` method) 

it passes url and the depth of crawling as an argument to `LinkChecker` Actor where 
`LinkCheker` sends message to itself with `self ! CheckUrl` 
When LinkChecker receives message `CheckUrl` it checks whether url is already visited and depth is greater >= 0  if this condition satisfies url and depth is passed to `GetterActor`
and url marked as visited

At GetterActor : 
Here `currentHost` holds the url to be crawled
if connection with the url is succesfully established (`case success` in onComplete) ,
then `GetterActor` sends a message to itself with `self ! body` where urls from the current url are collected with the help of `Jsoup` in the `getAllLinks` method.

when `GetterActor` gets message `Abort` it calls method `stop()` where message `Done` is sent to its parent Actor (`LinkChecker`).

At LinkChecker : When message `Done` is received it sends `Result` message to its parent actor (`ServerActor`) , message contains url given to crawl and collected urls

At serverActor : when Mailbox receives `Result` it is then processed and with message `CrawledUrls` Crawled result is sent back to `StartingPoint` .
