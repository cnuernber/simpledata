# A Simple Data Exploration Library

A few problems, some quick explorations, export to/from SQL.

Based on the TechAscent ml stack which includes [smile](https://haifengl.github.io/).


## Usage

Precompile tech.ml.dataset.  In order to get the best possible general performance,
we have rely on macros and the Clojure compiler in order to compile a lot of
primitive-type-specific code.  This does have a startup performance time hit, however, in
the case where the code hasn't been AOT compiled.

```console
rm -rf classes
mkdir classes
clojure -e "(compile 'tech.ml.dataset)"
```

- Make note to redo this step if you upgrade `tech.ml.dataset`.



If you would like to use postgres to store results then after 
[installing docker](https://docs.docker.com/get-docker/) run:

```console
scripts/start-local-postgres
```

Go into the various namespaces an REPL around for a while.  Or 
find a new dataset, create a new namespace, and get to it.
