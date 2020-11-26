CC?=clang
CFLAGS=-g -O0
OBJS=test1.elf test2.elf test3.elf test4.elf test5.elf

.PHONY: all clean

all: $(OBJS)

%.elf: %.o
	$(CC) $(CFLAGS) -o $@ $<

%.o: %.c
	$(CC) -c $(CFLAGS) -o $@ $<

clean:
	rm -f *.elf
