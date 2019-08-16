###### Project: Forex Rate Proxy<br/> Author: Enrico Caporali <br/> Date Submitted: 12 August, 2019

# Forex Rate Proxy

## Application Design

In order to overcome the OneForge request per day limitation, a fast and reliable cache is required with the ability to have TTL on the entry values.
In addition, I decided to make a single request every 300 seconds and retrieve fresh rates for all the supported currency pairs.
This led me to update the service `Algebra` from `get: Pair => Error Either Rate` to `getRates: => Error Either Seq[Rate]`.
OneForge seems to have a fast response time regardless of how many pairs are passed into the request, plus the size of the payload returned
is not above `70 KB`.
From a design point of view, the cache was added to the `Program` instead of the `OneForgeInterpreter` simply because a decision can be made in
the future to upgrade to a different OneForge tier (which will allow to have more requests per day) but still keeping the cache as mechanism to
increase performance. Moreover, another interpreter from a different provider could be added and  and the cache will be left unchanged. 

Moreover, `Monad transformers` are not famous for having high performance in scala but for simplicity, after careful consideration, I decided to use 
`EitherT` monad transformer in the `OneForgeInterpreter as there will only be a request every 300 seconds. If this becomes a bottleneck, a small 
refactoring will be needed to overcome this issue.  

I created `HttpClient` and `CacheClient` as third-party library wrappers because, in general, it is not good practice to leak the internals throughout the codebase.

## External Libraries decisions

I upgraded some of the already included libraries to keep them as up-to-date as possible and removed the conflicts by setting specific versions to be used.
The following points summarize the decisions made:

- `scalacache` was chosen as external library wrapper with `Caffeine` as the actual underlying cache.
- `http4s-client` was chosen to make http requests within the application
- `log4cats` was chosen to have effectful logging throughout the codebase (with `cats` support for `IO` monad)
- `enumeratum` was chosen to maximise the use of the currency domain objects which provides useful benefits. For example, the function `findValues` 
  will remove the risk of forgetting to add/remove other currencies to/from a custom list require to calculate the unique product between them.
  I am not really in favour of using enumerations in scala, but considering the use case and this library's documentation, I decided to add it to the project.

## Assumptions
The main assumptions taken while developing this application are as follow:

- Considering that friendly error messages should be returned, I decided to call the `/quota` endpoint to check whether it is possible to proceed with
  requesting fresh rates from OneForge Api. This call does NOT increase the quota count towards the limit.
- There are only 9 currencies supported in this application - it is just a matter of adding/removing them to/from the codebase to have them supported.
  The OneForge `/quotes` Api, returns all the currencies if the `pairs` query parameter gets removed. However, the domain Currency class must be updated
  to support all of them.
- From the given requirements, it is clear that availability of the service is the main key point so I decided to make one call and return all of the supported currencies at once.
  (`The application should at least support 10.000 requests per day`)
  
  
- Considering this system is not in production, I modified the `/rates` endpoint to include versioning `/v1/rates` 
  as this will benefit future development and deployments. 
- I added support for the conversion between two equal currencies (eg. `from JPY to JPY`) as it seems to be a valid use case. This support generates a response on the fly
  and returns a flat rate with price equal to 1.

## Development

in order to run this project, the `ONE_FORGE_API_KEY` environment variable must be set to a valid OneForge API key otherwise the project will not run.

### How to run unit tests
```
sbt test
```

### How to run unit tests with coverage
```
sbt check
```

### How to run the application
```
sbt run
```

## Coverage
The coverage percentage for this project must be above **95%** and it currently stands at:  
`96.37%`

## Additional possible improvements
- Include integration tests and load tests (possibly with `Gatlin`) to assert the performance and response of this proxy and the upstream
  OneForge Api.
- If a distributed cache is required, in case multiple service instances read/write to it, `Caffeine` cache should be replaced with
  `Redis` or `Memcache` which are perfect for this use case.
- Add docker support for application by including a `docker-compose.yml` file.
- If multiple Rates interpreters are included, the `/quota` request could be added into the algebra as `/healthcheck` and used in the Program to route requests
  to the first interpreter having remaining quota > 0.  

## Library reference
- [scalacache](https://cb372.github.io/scalacache/) 
- [caffeine](https://github.com/ben-manes/caffeine/)
- [http4s-blaze-client](https://http4s.org/v0.18/client/)
- [log4cats](https://christopherdavenport.github.io/log4cats/)
- [enumeratum](https://github.com/lloydmeta/enumeratum/)
