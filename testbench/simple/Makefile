CC=${LLVM_BUILD_DIR}/bin/clang
CFLAGS=-Xclang -load -Xclang ${LLVM_BUILD_DIR}/lib/LLVMMallocTracker.so -g -O0 -fsanitize=thread
CFLAGS_N=-g -O0
LDFLAGS=-L/home/hastrom/cache/cacheray/build/lib/ -lcacheray

.PHONY: clean all

all: simple.out simple-no-instrum.out

%.out: %.o
	$(CC) -o $@ $^ $(LDFLAGS)

simple.o: simple.c
	$(CC) -c $(CFLAGS) -o $@ $^

simple-no-instrum.o: simple.c
	$(CC) -c $(CFLAGS_N) -o $@ $^

clean:
	rm -f *.out *.o *.trace *.trace.*
