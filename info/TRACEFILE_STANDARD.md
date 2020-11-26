#The Memtrack Tracefile Standard

*Version 0.1 - 2020-08-19*

##About

This document outlines the Memtrack Tracefile standard. Everything written here is true.

All numbers mentioned are unsigned unless specified otherwise.

##The Header

The header is comprised of 9 fields of data. They appear in the following order at the start of the tracefile. The byte order is the native byte order from the CPU the tracefile was generated on. Hint: to figure out the byte order, find out which order produces the correct identification number, as it should be the same for every file.

1. Header Length (1 byte)
2. Identification number (2 bytes)
...Should always be 4473 in decimal.
3. Amount of events held in the tracefile (8 bytes)
4. Out of memory check (1 byte)
...Not used as of this version
5. Width of type data (1 byte)
6. Width of address data (1 byte)
7. Width of the struct\_pos data structure (1 byte)
8. Width of the size field in the struct\_pos data structure (1 byte)
9. Width of the id field in the struct\_pos data structure (1 byte)

##Events

After the header, a list of events follows. The amount of events should be equal to field 3 in the header. There are 3 different events:

###Memory event

The memory event captures reads, writes and frees. The layout is the following:

1. Event type
2. Address

###Struct Allocation Event

The struct allocation event captures mallocs and callocs.

1. Event type (8 bytes long)
2. Address (architecture dependent. Probably 8 bytes)
3. Struct size (8 bytes long)
4. Id (30 byte string)

###Struct Reallocation Event

The struct reallocation event captures reallocs. 

1. Event type
2. Old Address
3. New Address
4. Struct size
5. Id

##FAQ

Q: Is the header really needed when there is this standard?
A: No and the header might be removed in the next version


