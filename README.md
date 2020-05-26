# A Simple Data Exploration Library

A few problems, some quick explorations, export to/from SQL.


## Usage

Precompile tech.ml.dataset.  There is a lot of type-specific code in there...

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
