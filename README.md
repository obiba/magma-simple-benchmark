magma-simple-benchmark
===============

Simple benchmark for [Magma Datasources](https://github.com/obiba/magma)

Supports:

* Hibernate Datasource (MySQL)
* MongoDB Datasource

To run `./gradlew run` and results in `benchmark.log`

## Results

With FNAC.zip

* Import in
** 3.604 min (hibernate)
** 2.466 min (mongo)
* Load 1 tables in
** 14.41 μs (hibernate)
** 19.36 μs (mongo)
* Load 222 variables in
** 702.8 μs (hibernate)
** 91.17 ms (mongo)
* Load 3000 valueSets in
** 82.98 μs (hibernate)
** 449.3 ms (mongo)
* Load 3000 entities in
** 50.52 μs (hibernate)
** 268.9 ms (mongo)
* Load values in
** 1.786 h (hibernate)
** 2.141 d (mongo)
* Load vectors in
** 184.8 ms (hibernate)
** 156.8 ms (mongo)
