CC=${LLVM_BUILD_DIR}/bin/clang
CFLAGS=-Xclang -load -Xclang ${LLVM_BUILD_DIR}/lib/LLVMMallocTracker.so -g -O0 -fsanitize=thread -I/home/hastrom/cache/cacheray/include -DUSE_CACHERAY
CFLAGS_N=-g -O0
LDFLAGS=-L/home/hastrom/cache/cacheray/build/lib/ -lcacheray

all: reorder-no-instrum.out reorder.out

%.out: %.o
	$(CC) -o $@ $< $(LDFLAGS)

reorder.o: reorder.c
	$(CC) -c $(CFLAGS) -o $@ $<

reorder-no-instrum.o: reorder.c
	$(CC) -c $(CFLAGS_N) -o $@ $<

clean:
	rm -f *.out *.o cachegrind* 
