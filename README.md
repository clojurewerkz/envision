# Envision

Envision is a small, easy to use Clojure library for data processing, cleanup
and visualisation. If you've heard about Incanter, you may see a couple of things
that we do in a similar way.

## Project Maturity

Envision is a relatively young project. Since it's never meant to be used in hard-
production (e.g. it will never be something user-facing), and is intended to be 
used by people who'd like to yield some information from their data, it should 
be stable enough from the very early releases.


## Dependency Information (Artifacts)

Envision artifacts are [released to Clojars](https://clojars.org/clojurewerkz/envision). If you are using Maven, add the following repository
definition to your `pom.xml`:

```xml
<repository>
  <id>clojars.org</id>
  <url>http://clojars.org/repo</url>
</repository>
```

### The Most Recent Version

With Leiningen:

``` clojure
[clojurewerkz/envision "0.1.0-SNAPSHOT"]
```

With Maven:

``` xml
<dependency>
  <groupId>clojurewerkz</groupId>
  <artifactId>envision</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```


## Supported Features

 * Histograms
 * Scatterplots
 * Boxplots
 * Barcharts
 * Regression lines
 * Cluster visualisation


## Supported Clojure Versions

Envision supports Clojure 1.4+.

## Community

To subscribe for announcements of releases, important changes and so on, please follow
[@ClojureWerkz](https://twitter.com/#!/clojurewerkz) on Twitter.


## Envision Is a ClojureWerkz Project

Envision is part of the [group of libraries known as ClojureWerkz](http://clojurewerkz.org), together with
[Monger](http://clojuremongodb.info), [Elastisch](http://clojureelasticsearch.info), [Langohr](http://clojurerabbitmq.info),
[Welle](http://clojureriak.info), [Titanium](http://titanium.clojurewerkz.org) and several others.



## Continuous Integration

[![Continuous Integration status](https://secure.travis-ci.org/clojurewerkz/envision.png)](http://travis-ci.org/clojurewerkz/envision)

CI is hosted by [travis-ci.org](http://travis-ci.org)

## Development

Envision uses [Leiningen 2](https://github.com/technomancy/leiningen/blob/master/doc/TUTORIAL.md). Make
sure you have it installed and then run tests against all supported Clojure versions using

```
lein2 all test
```

Then create a branch and make your changes on it. Once you are done with your changes and all
tests pass, submit a pull request on Github.

## License

Copyright © 2014 Alex Petrov, Michael S. Klishin 

Double licensed under the Eclipse Public License (the same as Clojure) or the Apache Public License 2.0.

## Credits

Development sponsored by [codecentric AG](http://codecentric.de)

![Development Sponsored](https://www.codecentric.de/wp-content/themes/ccHomepage/img/logo-codecentric.png)

LibSVM sources belong to their respected owners, except for the changes our team made to the source code.
