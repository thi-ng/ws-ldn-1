# Clojure/Clojurescript workshop

(WS-LDN-1)

This repo contains a subset of commented examples created during the workshop.

## Day 1 namespaces

- [day1.csv](src/ws_ldn_1/day1/csv.clj) - CSV parsing & column extraction
- [day1.people](src/ws_ldn_1/day1/people.clj) - EDN parsing & transformation

Airport dataset from: http://ourairports.com/data/

## Day 2 namespaces

### Clojure SVG airport visualization

- [day2.mercator](src/ws_ldn_1/day2/mercator.clj) - Mercator projection
- [day2.svgmap](src/ws_ldn_1/day2/svgmap.clj) - SVG mercator map

![47k airports](http://workshop.thi.ng/ws-ldn-1/airports.svg)

47k airports mapped, data from [ourairports.com](http://ourairports.com/data/)

Github doesn't display the map background image, see original viz [here](http://workshop.thi.ng/ws-ldn-1/airports.svg)...

### Clojurescript, Reagent / React example

- [ui.day2.core](src-cljs-day2/ws_ldn_1/day2/core.cljs) - basic [Reagent](http://reagent-project.github.io) concepts & undo demo

To launch:

```bash
lein figwheel day2
```

## Day 3 namespaces

### Clojure

- [ui.day3.threedee](src/ws_ldn_1/day3/threedee.clj) - thi.ng/geom mesh examples

Exported meshes are located in [assets](assets/) folder.

### Clojurescript

- [ui.day3.core](src-cljs-day3-1/ws_ldn_1/day3/core.cljs) - thi.ng/geom vizualization examples (using Reagent)

To launch:

```bash
lein figwheel day3-viz
```

- [ui.day3.webgl](src-cljs-day3-2/ws_ldn_1/day3/webgl.cljs) - thi.ng/geom WebGL & in-browser STL mesh export example

To launch:

```bash
lein figwheel day3-webgl
```

## CLJS build w/ advanced optimizations

To build the CLJS examples with advanced optimizations, remove the commented lines from the `project.clj` file and disable the `:output-dir` key for the relevant build profile(s). Then compile the source with:

```bash
lein do clean, cljsbuild once <insert-build-profile-id>
```

## License

Copyright © 2015 Karsten Schmidt

Distributed under the Apache Software License either version 1.0 or (at
your option) any later version.
