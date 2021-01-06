# Cacheray trace format

Cacheray emits information about a running program to describe the relationship
between execution, memory and types. A Cacheray trace consists of a series of
type-tagged records.


## Concepts

The instrumentation primarily records basic memory reads and writes.

But Cacheray also lets the target program provide _Run-Time Type Annotations_
(RTTA) to annotate a memory region with very basic type information, which can
be used by trace analysis to indicate the data type of a memory access.

RTTA is encoded as add/remove events to model for example `malloc`/`free`. That
way type annotations can be valid only for a limited time, and memory access
records are cleanly delineated by RTTA add/remove records.


## Encoding details

All data is serialized in native host byte-order (**TODO:** fix this).

Strings are encoded with a 32-bit length-prefix and without NUL terminator.


## Record types

The type tag is represented as a single byte from the following enumeration:

| Value | Name                            | Description                        |
|-------|---------------------------------|------------------------------------|
| 0x00  | `CACHERAY_EVENT_READ`           | Memory was read                    |
| 0x01  | `CACHERAY_EVENT_WRITE`          | Memory was written                 |
| 0x02  | `CACHERAY_EVENT_RTTA_ADD`       | RTTA added for a memory region     |
| 0x03  | `CACHERAY_EVENT_RTTA_REMOVE`    | RTTA removed for a memory region   |
| 0x40  | `CACHERAY_EVENT_MASK_ATOMIC`    | Bit flag for atomic access         |
| 0x80  | `CACHERAY_EVENT_MASK_UNALIGNED` | Bit flag for unaligned access      |

The two bit flags can only be combined with `CACHERAY_EVENT_READ` and
`CACHERAY_EVENT_WRITE`.

Parsers must read the tag byte and strip off any atomic/unaligned flags before
attempting to interpret the record type, e.g.
```
recordtype = type & ~(CACHERAY_EVENT_MASK_UNALIGNED |
                      CACHERAY_EVENT_MASK_ATOMIC)
```

After this operation, `recordtype` is one of the basic `CACHERAY_EVENT_` record
types.


### Record `CACHERAY_EVENT_READ`/`CACHERAY_EVENT_WRITE`

A memory access event.

| Offset   | Size | Type | Name     | Description                              |
|----------|------|------|----------|------------------------------------------|
| 0        | 1    | u8   | Type     | Memory access type                       |
| 1        | 8    | u64  | Address  | Address accessed                         |
| 9        | 1    | u8   | Size     | Number of bytes accessed                 |
| 10       | 8    | u64  | Thread   | Thread ID                                |

The `Type` field is one of:

* `CACHERAY_EVENT_READ`
* `CACHERAY_EVENT_WRITE`

potentially bitwise OR-ed with

* `CACHERAY_EVENT_MASK_ATOMIC`
* `CACHERAY_EVENT_MASK_UNALIGNED`


### Record `CACHERAY_EVENT_RTTA_ADD`

Register a typename with a memory region.

| Offset   | Size | Type | Name      | Description                              |
|----------|------|------|---------- |------------------------------------------|
| 0        | 1    | u8   | Type      | `CACHERAY_EVENT_RTTA_ADD`                |
| 1        | 8    | u64  | Address   | Start address of region                  |
| 9        | 8    | u64  | Thread    | Thread ID                                |
| 17       | 4    | u32  | Elemsize  | Size of each element                     |
| 21       | 4    | u32  | Elemcount | Number of elements                       |
| 25       | 4+   | str  | Typename  | Element type name                        |

`[Address, Address + Elemsize * Elemcount)` describes a memory region inhabited
by values of type `Typename`.


### Record `CACHERAY_EVENT_RTTA_REMOVE`

Remove a registered typename from a memory region.

| Offset   | Size | Type | Name      | Description                              |
|----------|------|------|---------- |------------------------------------------|
| 0        | 1    | u8   | Type      | `CACHERAY_EVENT_RTTA_REMOVE`             |
| 1        | 8    | u64  | Address   | Start address of region                  |
| 9        | 8    | u64  | Thread    | Thread ID                                |
