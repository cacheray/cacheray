# Contributing to Cacheray

This document describes the rules and assumptions for patches whether from
maintainers or external contributors.

Examples below assume the working directory is in a fresh checkout of
https://github.com/cacheray/cacheray.


### Cacheray runtime

To build, use CMake something like this:
```
$ mkdir build && cd build
$ cmake -G "Unix Makefiles" -DCMAKE_C_COMPILER=clang ../cacheray
$ make
```

Note that the Cacheray runtime and tests require ThreadSanitizer
instrumentation and so only build with Clang.


## Cacheray simulator

The simulator is written in Java, and builds with Maven:
```
$ cd sim/simulator_java
$ mvn package
```


## Contributing code

* Use GitHub [pull requests](https://github.com/cacheray/cacheray/pulls)
* No merge commits on `main` branch, we prefer a linear history
* Small and/or focused commits with good commit messages (see
  https://chris.beams.io/posts/git-commit/)
