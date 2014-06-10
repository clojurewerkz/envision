# Envision

Envision is a small, easy to use Clojure library for data processing, cleanup
and visualisation. If you've heard about Incanter, you may see a couple of things
that we do in a similar way.

## Project Maturity

Envision is a relatively young project. Since it's never meant to be used in hard-
production (e.g. it will never be something user-facing), and is intended to be 
used by people who'd like to yield some information from their data, it should 
be stable enough from the very early releases.

## General Approach

Main idea of this library is to make exploratory analysis more interactive and visual,
although in programmer's way. Envision creates a "throwaway environment" every time
you, for example, make a line chart. You can modify chart the way you want, change
all the possible configuration parameters, filter data, add exponents the ways we 
wouldn't be able to program for you.

We concluded that visual environments are often constraining, and creating an API
for every since feature would make it amazingly big and bloated. So we do a bare 
minimum, which is already helpful by default through the API and let you configure
everything you could've possibly imagined yourself: adding interactivity, combining
charts, customizing layouts and so on.

## Usage

Main entrypoint is `clojurewerkz.envision.core/prepare-tmp-dir`. It creates a temporary
directory with all the required dependencies and returns you a path to it. For example,
let's generate some data and render a line and area charts:

```clj
(ns my-ns
  (:require [clojurewerkz.envision.core         :as envision]
            [clojurewerkz.envision.chart-config :as cfg]
  
(envision/prepare-tmp-dir
 [(cfg/make-chart-config
   {:id            "line"
    :x             "year"
    :y             "income"
    :x-order       "year"
    :series-type   "line"
    :data          (into [] (for [i (range 0 20)] {:year (+ 2000 i) :income (+ 10 i (rand-int 10))}))
    :interpolation :cardinal
    })
  (cfg/make-chart-config
   {:id            "line2"
    :x             "year"
    :y             "income"
    :x-order       "year"
    :series-type   "area"
    :data          (into [] (for [i (range 0 20)] {:year (+ 2000 i) :income (+ 10 i (rand-int 10))}))
    :interpolation :cardinal
    })
   ])
```

Function will return a tmp folder path, like: 

```
/var/folders/1y/xr7zvp2j035bpq09whg7th5w0000gn/T/envision-1402385765815-3502705781
```

`cd` into this path and start Jekyll:

```
bundle install
jekyll serve --watch
```

After that you can point your browser to 

```
http://localhost:4000/templates/index.html
```

And see the resulting graphs: 

![Preview](https://www.evernote.com/shard/s9/sh/985ec7c9-3ee8-42a7-8078-839ce7631ec0/a9d2f8cc5ad717717dc24f2946c04044/res/5d96d756-c68f-4527-8585-d6032f761ad9/skitch.png?resizeSmall&width=300)

We decided to use Jekyll, since sometimes `d3` doesn't like `file://` protocol. However, you can just 
open open `templates/index_file.html` in your browser and get pretty much same result.

## Chart configuration

In order to configure chart, you have to specify:

  * `id`, a unique string literal identifying the chart
  * `data`, sequence of maps, where each map represents an entry to be displayed
  * `x`, key that should be taken as `x` value for each rendered point
  * `y`, key that should be taken as `y` value for each rendered point
  * `series-type`, one of `line`, `bubble`, `area` and `bar` for line charts, Scatterplots, 
     area charts and barcharts, correspondingly
     
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
