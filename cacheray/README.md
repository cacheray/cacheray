# Cacheray runtime

The main responsibility of a runtime for Cacheray is to collect memory accesses
and type annotations into a sequence of events that can be interpreted by the
Cacheray simulator.

A runtime has two layers:

* The translation from compiler instrumentation (currently `-fsanitize=thread`)
  to Cacheray semantics
* The recording, serialization and persistence of Cacheray traces

The first layer is relatively simple and reusable -- it's just a matter of
implementing all compiler instrumentation callbacks and translating them to
something Cacheray might care about. In fact, this first layer has a canonical
implementation in `src/cacheray_instrum.inc`.

The second layer has different optima depending on context. For example, tracing
runtimes might want to write data to a file, a pipe, a socket, or a shared
memory area. Some need synchronization for multiple threads, some don't. We ship
a basic runtime for a POSIX userspace context, but it's easy to build a more
tailored runtime on top of layer 1.


## Types of runtimes

Cacheray can either be used in cooperative or non-cooperative mode.

For Cacheray to be used without cooperation from the target application, its
runtime needs to self-initialize and traces need to be written to a pre-agreed
destination. The application can't help with runtime type annotations, so
compiler instrumentation is the only source of Cacheray data. This is basically
what the included runtime does (sic! currently I don't think it's entirely
self-bootstrapping, but we should make it so).

If it's feasible to modify the target application source code to cooperate, it's
possible to create a completely custom, embedded runtime with better concurrency
control, better type fidelity and performance.


## Building a custom runtime

Cacheray's runtime architecture is carefully architected to be inelegant but
effective :-).

The layer-1 runtime is all contained in `cacheray_instrum.inc`. This file is
designed to be included directly into a single source file implementing the
actual runtime, and assumes that a number of symbols are available:

* `void cacheray_init(void)` -- called from init routine on startup
* `void cacheray_log(uint8_t type, void* addr, uint8_t size)` -- called for
  every memory access recorded by the compiler instrumentation
* `void cacheray_rtta_add(void* addr, const char* typename, uint32_t elem_size,
  uint32_t elem_count)` -- called by the MallocTracker LLVM pass to annotate a
  memory region with a type name (e.g. on `malloc`)
* `void cacheray_rtta_remove(void* addr)` -- called by the MallocTracker LLVM
  pass to remove a type annotation (e.g. on `free`)
* `CACHERAY_DPRINT` -- `print`-style debug logging
* `CACHERAY_ASSERT` -- `assert`-style debug assertions
* `CACHERAY_EVENT_*` -- the Cacheray event types

A custom runtime would implement these functions in a source file and then
include `cacheray_instrum.inc` at the end, to wire everything with
instrumentation. A naive printing runtime could be:

```
#include <stdio.h>
#include <assert.h>
#include <cacheray/cacheray.h>

#define CACHERAY_DPRINT printf
#define CACHERAY_ASSERT assert

static void cacheray_init(void) {
    printf("Welcome to Cacheray!\n");
}

static void cacheray_log(uint8_t type, void* addr, uint8_t size) {
    printf("Memory event: 0x%02x  0x%08x  %d bytes\n");
}

static void cacheray_rtta_add(void* addr, const char* typename,
                              uint32_t elem_size, uint32_t elem_count) {
    char* end_addr = (char*)addr + (elem_size * elem_count);
    printf("Type annotation added: 0x%08x-0x%08x: %s\n",
           addr, end_addr, typename);
}

static void cacheray_rtta_remove(void* addr) {
    printf("Type annotation removed: 0x%08x\n", addr);
}
```

The fact that this is a single compilation unit has the following benefits:

* Instrumentation can be easily disabled for the file during compilation
* The compiler has plenty opportunity for inlining, making instrumentation
  overhead smaller

The runtime is also responsible for dealing with reentrancy due to
instrumentation (e.g. in the above case, `printf` is very likely instrumented
and might generate recursive instrumentation callbacks. Dealing with that is
left as an exercise for the reader.)
