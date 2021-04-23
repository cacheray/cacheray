/*-*- C -*-*/

/*******************************************************************************
 * Embeddable instrumentation runtime for cacheray.
 *
 * To embed, include this file into a source file which defines the following
 * macros and functions before the #include directive:
 *
 * - void cacheray_init(void);
 * - void cacheray_log(event_type_t type, void *addr, unsigned char size);
 * - void cacheray_rtta_add(void *addr, const char *typename,
 *                          unsigned elem_size, unsigned elem_count);
 * - void cacheray_rtta_remove(void *addr);
 * - the cacheray event types (CACHERAY_EVENT_*)
 * - CACHERAY_DPRINT, printf-style debug logging
 * - CACHERAY_ASSERT for debug assertions
 *
 * The embedder promises to handle buffer management, synchronization, and
 * toggling to avoid recursive instrumentation.
 *
 * This file declares and defines the TSAN instrumentation callbacks and calls
 * embedder functions to record corresponding memory access patterns.
 */

#if __has_feature(thread_sanitizer)
#error "Don't sanitize cacheray"
#endif

/* Cacheray malloc-tracking LLVM pass callbacks */
void __malloc_recorded(long byte_size, char *type_name, void *addr);
void __calloc_recorded(long nmemb, long size, char *name, void *addr);
void __realloc_recorded(void *old_addr, long size, char *name, void *addr);
void __free_recorded(void *addr);

/* Basic TSAN instrumentation callbacks */
void __tsan_init(void);
void __tsan_read1(void *addr);
void __tsan_read2(void *addr);
void __tsan_read4(void *addr);
void __tsan_read8(void *addr);
void __tsan_read16(void *addr);
void __tsan_read_range(void *addr, unsigned long size);
void __tsan_write1(void *addr);
void __tsan_write2(void *addr);
void __tsan_write4(void *addr);
void __tsan_write8(void *addr);
void __tsan_write16(void *addr);
void __tsan_write_range(void *addr, unsigned long size);
void __tsan_func_entry(void *addr);
void __tsan_func_exit(void *addr);
void __tsan_unaligned_read1(void *addr);
void __tsan_unaligned_read2(void *addr);
void __tsan_unaligned_read4(void *addr);
void __tsan_unaligned_read8(void *addr);
void __tsan_unaligned_read16(void *addr);
void __tsan_unaligned_write1(void *addr);
void __tsan_unaligned_write2(void *addr);
void __tsan_unaligned_write4(void *addr);
void __tsan_unaligned_write8(void *addr);
void __tsan_unaligned_write16(void *addr);

/* Atomic TSAN instrumentation callbacks */
typedef unsigned char a8;
typedef unsigned short a16;
typedef unsigned int a32;
typedef unsigned long long a64;

typedef enum {
  mo_relaxed,
  mo_consume,
  mo_acquire,
  mo_release,
  mo_acq_rel,
  mo_seq_cst
} morder;

a8 __tsan_atomic8_load(const volatile a8 *a, morder mo);
a16 __tsan_atomic16_load(const volatile a16 *a, morder mo);
a32 __tsan_atomic32_load(const volatile a32 *a, morder mo);
a64 __tsan_atomic64_load(const volatile a64 *a, morder mo);

void __tsan_atomic8_store(volatile a8 *a, a8 v, morder mo);
void __tsan_atomic16_store(volatile a16 *a, a16 v, morder mo);
void __tsan_atomic32_store(volatile a32 *a, a32 v, morder mo);
void __tsan_atomic64_store(volatile a64 *a, a64 v, morder mo);

a8 __tsan_atomic8_exchange(volatile a8 *a, a8 v, morder mo);
a16 __tsan_atomic16_exchange(volatile a16 *a, a16 v, morder mo);
a32 __tsan_atomic32_exchange(volatile a32 *a, a32 v, morder mo);
a64 __tsan_atomic64_exchange(volatile a64 *a, a64 v, morder mo);

a8 __tsan_atomic8_fetch_add(volatile a8 *a, a8 v, morder mo);
a16 __tsan_atomic16_fetch_add(volatile a16 *a, a16 v, morder mo);
a32 __tsan_atomic32_fetch_add(volatile a32 *a, a32 v, morder mo);
a64 __tsan_atomic64_fetch_add(volatile a64 *a, a64 v, morder mo);

a8 __tsan_atomic8_fetch_sub(volatile a8 *a, a8 v, morder mo);
a16 __tsan_atomic16_fetch_sub(volatile a16 *a, a16 v, morder mo);
a32 __tsan_atomic32_fetch_sub(volatile a32 *a, a32 v, morder mo);
a64 __tsan_atomic64_fetch_sub(volatile a64 *a, a64 v, morder mo);

a8 __tsan_atomic8_fetch_and(volatile a8 *a, a8 v, morder mo);
a16 __tsan_atomic16_fetch_and(volatile a16 *a, a16 v, morder mo);
a32 __tsan_atomic32_fetch_and(volatile a32 *a, a32 v, morder mo);
a64 __tsan_atomic64_fetch_and(volatile a64 *a, a64 v, morder mo);

a8 __tsan_atomic8_fetch_or(volatile a8 *a, a8 v, morder mo);
a16 __tsan_atomic16_fetch_or(volatile a16 *a, a16 v, morder mo);
a32 __tsan_atomic32_fetch_or(volatile a32 *a, a32 v, morder mo);
a64 __tsan_atomic64_fetch_or(volatile a64 *a, a64 v, morder mo);

a8 __tsan_atomic8_fetch_xor(volatile a8 *a, a8 v, morder mo);
a16 __tsan_atomic16_fetch_xor(volatile a16 *a, a16 v, morder mo);
a32 __tsan_atomic32_fetch_xor(volatile a32 *a, a32 v, morder mo);
a64 __tsan_atomic64_fetch_xor(volatile a64 *a, a64 v, morder mo);

a8 __tsan_atomic8_fetch_nand(volatile a8 *a, a8 v, morder mo);
a16 __tsan_atomic16_fetch_nand(volatile a16 *a, a16 v, morder mo);
a32 __tsan_atomic32_fetch_nand(volatile a32 *a, a32 v, morder mo);
a64 __tsan_atomic64_fetch_nand(volatile a64 *a, a64 v, morder mo);

int __tsan_atomic8_compare_exchange_strong(volatile a8 *a, a8 *c, a8 v,
                                           morder mo, morder fmo);
int __tsan_atomic16_compare_exchange_strong(volatile a16 *a, a16 *c, a16 v,
                                            morder mo, morder fmo);
int __tsan_atomic32_compare_exchange_strong(volatile a32 *a, a32 *c, a32 v,
                                            morder mo, morder fmo);
int __tsan_atomic64_compare_exchange_strong(volatile a64 *a, a64 *c, a64 v,
                                            morder mo, morder fmo);

int __tsan_atomic8_compare_exchange_weak(volatile a8 *a, a8 *c, a8 v, morder mo,
                                         morder fmo);
int __tsan_atomic16_compare_exchange_weak(volatile a16 *a, a16 *c, a16 v,
                                          morder mo, morder fmo);
int __tsan_atomic32_compare_exchange_weak(volatile a32 *a, a32 *c, a32 v,
                                          morder mo, morder fmo);
int __tsan_atomic64_compare_exchange_weak(volatile a64 *a, a64 *c, a64 v,
                                          morder mo, morder fmo);

a8 __tsan_atomic8_compare_exchange_val(volatile a8 *a, a8 c, a8 v, morder mo,
                                       morder fmo);
a16 __tsan_atomic16_compare_exchange_val(volatile a16 *a, a16 c, a16 v,
                                         morder mo, morder fmo);
a32 __tsan_atomic32_compare_exchange_val(volatile a32 *a, a32 c, a32 v,
                                         morder mo, morder fmo);
a64 __tsan_atomic64_compare_exchange_val(volatile a64 *a, a64 c, a64 v,
                                         morder mo, morder fmo);

void __tsan_atomic_thread_fence(morder mo);
void __tsan_atomic_signal_fence(morder mo);

/* Cacheray runtime type annotation instrumentation callbacks */
void __malloc_recorded(long byte_size, char *type_name, void *addr) {
  CACHERAY_DPRINT("MALLOC_RECORDED: %s, size %ld, addr 0x%p\n", type_name,
                  byte_size, addr);

  cacheray_rtta_add(addr, type_name, byte_size, 1);
}

void __calloc_recorded(long nmemb, long size, char *name, void *addr) {
  /* This could use a different data structure in the future.
   * For now, just use malloc
   */
  cacheray_rtta_add(addr, name, size, nmemb);
}

void __realloc_recorded(void *old_addr, long size, char *name, void *addr) {
  /* Simulate a free and an allocate */
  cacheray_rtta_remove(old_addr);
  cacheray_rtta_add(addr, name, size, 1);
}

void __free_recorded(void *addr) {
  CACHERAY_DPRINT("Recorded a free, at address 0x%p\n", addr);
  cacheray_rtta_remove(addr);
}

/* TSAN instrumentation callbacks */
void __tsan_init(void) {
  cacheray_init();
}

void __tsan_read1(void *addr) {
  cacheray_log(CACHERAY_EVENT_READ, addr, 1);
}

void __tsan_read2(void *addr) {
  cacheray_log(CACHERAY_EVENT_READ, addr, 2);
}

void __tsan_read4(void *addr) {
  cacheray_log(CACHERAY_EVENT_READ, addr, 4);
}

void __tsan_read8(void *addr) {
  cacheray_log(CACHERAY_EVENT_READ, addr, 8);
}

void __tsan_read16(void *addr) {
  cacheray_log(CACHERAY_EVENT_READ, addr, 16);
}

void __tsan_read_range(void *addr, unsigned long size) {
  CACHERAY_ASSERT(0 && "Let me know if this needs to be implemented");
}

void __tsan_write1(void *addr) {
  cacheray_log(CACHERAY_EVENT_WRITE, addr, 1);
}

void __tsan_write2(void *addr) {
  cacheray_log(CACHERAY_EVENT_WRITE, addr, 2);
}

void __tsan_write4(void *addr) {
  cacheray_log(CACHERAY_EVENT_WRITE, addr, 4);
}

void __tsan_write8(void *addr) {
  cacheray_log(CACHERAY_EVENT_WRITE, addr, 8);
}

void __tsan_write16(void *addr) {
  cacheray_log(CACHERAY_EVENT_WRITE, addr, 16);
}

void __tsan_write_range(void *addr, unsigned long size) {
  CACHERAY_ASSERT(0 && "Let me know if this needs to be implemented");
}

void __tsan_func_entry(void *addr) {
  (void)addr;
}

void __tsan_func_exit(void *addr) {
  (void)addr;
}

void __tsan_unaligned_read1(void *addr) {
  cacheray_log(CACHERAY_EVENT_MASK_UNALIGNED | CACHERAY_EVENT_READ, addr, 1);
}

void __tsan_unaligned_read2(void *addr) {
  cacheray_log(CACHERAY_EVENT_MASK_UNALIGNED | CACHERAY_EVENT_READ, addr, 2);
}

void __tsan_unaligned_read4(void *addr) {
  cacheray_log(CACHERAY_EVENT_MASK_UNALIGNED | CACHERAY_EVENT_READ, addr, 4);
}

void __tsan_unaligned_read8(void *addr) {
  cacheray_log(CACHERAY_EVENT_MASK_UNALIGNED | CACHERAY_EVENT_READ, addr, 8);
}

void __tsan_unaligned_read16(void *addr) {
  cacheray_log(CACHERAY_EVENT_MASK_UNALIGNED | CACHERAY_EVENT_READ, addr, 16);
}

void __tsan_unaligned_write1(void *addr) {
  cacheray_log(CACHERAY_EVENT_MASK_UNALIGNED | CACHERAY_EVENT_WRITE, addr, 1);
}

void __tsan_unaligned_write2(void *addr) {
  cacheray_log(CACHERAY_EVENT_MASK_UNALIGNED | CACHERAY_EVENT_WRITE, addr, 2);
}

void __tsan_unaligned_write4(void *addr) {
  cacheray_log(CACHERAY_EVENT_MASK_UNALIGNED | CACHERAY_EVENT_WRITE, addr, 4);
}

void __tsan_unaligned_write8(void *addr) {
  cacheray_log(CACHERAY_EVENT_MASK_UNALIGNED | CACHERAY_EVENT_WRITE, addr, 8);
}

void __tsan_unaligned_write16(void *addr) {
  cacheray_log(CACHERAY_EVENT_MASK_UNALIGNED | CACHERAY_EVENT_WRITE, addr, 16);
}

/* Atomic instrumentation/implementation adapted from compiler-rt.

   Judging from the compiler-rt TSAN runtime, these atomic callbacks must both
   implement the atomic operations and collect statistics on them.

   GCC __atomic builtins match TSAN's API best, they seem to map 1-1 with the
   __tsan callbacks, so use those directly.
 */
static int to_gcc_mo(morder mo) {
  /* Translate TSAN memory order to GCC __atomic memory order. */
  switch (mo) {
  case mo_relaxed:
    return __ATOMIC_RELAXED;
  case mo_consume:
    return __ATOMIC_CONSUME;
  case mo_acquire:
    return __ATOMIC_ACQUIRE;
  case mo_release:
    return __ATOMIC_RELEASE;
  case mo_acq_rel:
    return __ATOMIC_ACQ_REL;
  case mo_seq_cst:
    return __ATOMIC_SEQ_CST;
  }
  return __ATOMIC_SEQ_CST;
}

a8 __tsan_atomic8_load(const volatile a8 *a, morder mo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, (void *)a, 1);
  return __atomic_load_n(a, to_gcc_mo(mo));
}

a16 __tsan_atomic16_load(const volatile a16 *a, morder mo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, (void *)a, 2);
  return __atomic_load_n(a, to_gcc_mo(mo));
}

a32 __tsan_atomic32_load(const volatile a32 *a, morder mo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, (void *)a, 4);
  return __atomic_load_n(a, to_gcc_mo(mo));
}

a64 __tsan_atomic64_load(const volatile a64 *a, morder mo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, (void *)a, 8);
  return __atomic_load_n(a, to_gcc_mo(mo));
}

void __tsan_atomic8_store(volatile a8 *a, a8 v, morder mo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, (void *)a, 1);
  __atomic_store_n(a, v, to_gcc_mo(mo));
}

void __tsan_atomic16_store(volatile a16 *a, a16 v, morder mo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, (void *)a, 2);
  __atomic_store_n(a, v, to_gcc_mo(mo));
}

void __tsan_atomic32_store(volatile a32 *a, a32 v, morder mo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, (void *)a, 4);
  __atomic_store_n(a, v, to_gcc_mo(mo));
}

void __tsan_atomic64_store(volatile a64 *a, a64 v, morder mo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, (void *)a, 8);
  __atomic_store_n(a, v, to_gcc_mo(mo));
}

a8 __tsan_atomic8_exchange(volatile a8 *a, a8 v, morder mo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, (void *)a, 1);
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, (void *)a, 1);
  return __atomic_exchange_n(a, v, to_gcc_mo(mo));
}

a16 __tsan_atomic16_exchange(volatile a16 *a, a16 v, morder mo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, (void *)a, 2);
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, (void *)a, 2);
  return __atomic_exchange_n(a, v, to_gcc_mo(mo));
}

a32 __tsan_atomic32_exchange(volatile a32 *a, a32 v, morder mo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, (void *)a, 4);
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, (void *)a, 4);
  return __atomic_exchange_n(a, v, to_gcc_mo(mo));
}

a64 __tsan_atomic64_exchange(volatile a64 *a, a64 v, morder mo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, (void *)a, 8);
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, (void *)a, 8);
  return __atomic_exchange_n(a, v, to_gcc_mo(mo));
}

a8 __tsan_atomic8_fetch_add(volatile a8 *a, a8 v, morder mo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, (void *)a, 1);
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, (void *)a, 1);
  return __atomic_fetch_add(a, v, to_gcc_mo(mo));
}

a16 __tsan_atomic16_fetch_add(volatile a16 *a, a16 v, morder mo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, (void *)a, 2);
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, (void *)a, 2);
  return __atomic_fetch_add(a, v, to_gcc_mo(mo));
}

a32 __tsan_atomic32_fetch_add(volatile a32 *a, a32 v, morder mo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, (void *)a, 4);
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, (void *)a, 4);
  return __atomic_fetch_add(a, v, to_gcc_mo(mo));
}

a64 __tsan_atomic64_fetch_add(volatile a64 *a, a64 v, morder mo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, (void *)a, 8);
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, (void *)a, 8);
  return __atomic_fetch_add(a, v, to_gcc_mo(mo));
}

a8 __tsan_atomic8_fetch_sub(volatile a8 *a, a8 v, morder mo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, (void *)a, 1);
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, (void *)a, 1);
  return __atomic_fetch_sub(a, v, to_gcc_mo(mo));
}

a16 __tsan_atomic16_fetch_sub(volatile a16 *a, a16 v, morder mo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, (void *)a, 2);
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, (void *)a, 2);
  return __atomic_fetch_sub(a, v, to_gcc_mo(mo));
}

a32 __tsan_atomic32_fetch_sub(volatile a32 *a, a32 v, morder mo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, (void *)a, 4);
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, (void *)a, 4);
  return __atomic_fetch_sub(a, v, to_gcc_mo(mo));
}

a64 __tsan_atomic64_fetch_sub(volatile a64 *a, a64 v, morder mo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, (void *)a, 8);
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, (void *)a, 8);
  return __atomic_fetch_sub(a, v, to_gcc_mo(mo));
}

a8 __tsan_atomic8_fetch_and(volatile a8 *a, a8 v, morder mo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, (void *)a, 1);
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, (void *)a, 1);
  return __atomic_fetch_and(a, v, to_gcc_mo(mo));
}

a16 __tsan_atomic16_fetch_and(volatile a16 *a, a16 v, morder mo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, (void *)a, 2);
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, (void *)a, 2);
  return __atomic_fetch_and(a, v, to_gcc_mo(mo));
}

a32 __tsan_atomic32_fetch_and(volatile a32 *a, a32 v, morder mo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, (void *)a, 4);
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, (void *)a, 4);
  return __atomic_fetch_and(a, v, to_gcc_mo(mo));
}

a64 __tsan_atomic64_fetch_and(volatile a64 *a, a64 v, morder mo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, (void *)a, 8);
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, (void *)a, 8);
  return __atomic_fetch_and(a, v, to_gcc_mo(mo));
}

a8 __tsan_atomic8_fetch_or(volatile a8 *a, a8 v, morder mo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, (void *)a, 1);
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, (void *)a, 1);
  return __atomic_fetch_or(a, v, to_gcc_mo(mo));
}

a16 __tsan_atomic16_fetch_or(volatile a16 *a, a16 v, morder mo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, (void *)a, 2);
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, (void *)a, 2);
  return __atomic_fetch_or(a, v, to_gcc_mo(mo));
}

a32 __tsan_atomic32_fetch_or(volatile a32 *a, a32 v, morder mo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, (void *)a, 4);
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, (void *)a, 4);
  return __atomic_fetch_or(a, v, to_gcc_mo(mo));
}

a64 __tsan_atomic64_fetch_or(volatile a64 *a, a64 v, morder mo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, (void *)a, 8);
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, (void *)a, 8);
  return __atomic_fetch_or(a, v, to_gcc_mo(mo));
}

a8 __tsan_atomic8_fetch_xor(volatile a8 *a, a8 v, morder mo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, (void *)a, 1);
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, (void *)a, 1);
  return __atomic_fetch_xor(a, v, to_gcc_mo(mo));
}

a16 __tsan_atomic16_fetch_xor(volatile a16 *a, a16 v, morder mo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, (void *)a, 2);
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, (void *)a, 2);
  return __atomic_fetch_xor(a, v, to_gcc_mo(mo));
}

a32 __tsan_atomic32_fetch_xor(volatile a32 *a, a32 v, morder mo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, (void *)a, 4);
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, (void *)a, 4);
  return __atomic_fetch_xor(a, v, to_gcc_mo(mo));
}

a64 __tsan_atomic64_fetch_xor(volatile a64 *a, a64 v, morder mo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, (void *)a, 8);
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, (void *)a, 8);
  return __atomic_fetch_xor(a, v, to_gcc_mo(mo));
}

/*
 * According to GCC builtins guide, __atomic_fetch_nand is implemented slightly
 * differently than the other fetch_* functions. There is an argument for having
 * 2 reads and 2 writes. However, this is probably compiler dependent and we
 * will ignore it for now.
 */

a8 __tsan_atomic8_fetch_nand(volatile a8 *a, a8 v, morder mo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, (void *)a, 1);
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, (void *)a, 1);
  return __atomic_fetch_nand(a, v, to_gcc_mo(mo));
}

a16 __tsan_atomic16_fetch_nand(volatile a16 *a, a16 v, morder mo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, (void *)a, 2);
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, (void *)a, 2);
  return __atomic_fetch_nand(a, v, to_gcc_mo(mo));
}

a32 __tsan_atomic32_fetch_nand(volatile a32 *a, a32 v, morder mo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, (void *)a, 4);
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, (void *)a, 4);
  return __atomic_fetch_nand(a, v, to_gcc_mo(mo));
}

a64 __tsan_atomic64_fetch_nand(volatile a64 *a, a64 v, morder mo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, (void *)a, 8);
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, (void *)a, 8);
  return __atomic_fetch_nand(a, v, to_gcc_mo(mo));
}

int __tsan_atomic8_compare_exchange_strong(volatile a8 *a, a8 *c, a8 v,
                                           morder mo, morder fmo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, (void *)a, 1);
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, c, 1);
  if (__atomic_compare_exchange_n(a, c, v, /*weak=*/0, to_gcc_mo(mo),
                                  to_gcc_mo(fmo))) {
    cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, (void *)a,
                 1);
    return 1;
  } else {
    cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, c, 1);
    return 0;
  }
}

int __tsan_atomic16_compare_exchange_strong(volatile a16 *a, a16 *c, a16 v,
                                            morder mo, morder fmo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, (void *)a, 2);
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, c, 2);
  if (__atomic_compare_exchange_n(a, c, v, /*weak=*/0, to_gcc_mo(mo),
                                  to_gcc_mo(fmo))) {
    cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, (void *)a,
                 2);
    return 1;
  } else {
    cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, c, 2);
    return 0;
  }
}

int __tsan_atomic32_compare_exchange_strong(volatile a32 *a, a32 *c, a32 v,
                                            morder mo, morder fmo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, (void *)a, 4);
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, c, 4);
  if (__atomic_compare_exchange_n(a, c, v, /*weak=*/0, to_gcc_mo(mo),
                                  to_gcc_mo(fmo))) {
    cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, (void *)a,
                 4);
    return 1;
  } else {
    cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, c, 4);
    return 0;
  }
}

int __tsan_atomic64_compare_exchange_strong(volatile a64 *a, a64 *c, a64 v,
                                            morder mo, morder fmo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, (void *)a, 8);
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, c, 8);
  if (__atomic_compare_exchange_n(a, c, v, /*weak=*/0, to_gcc_mo(mo),
                                  to_gcc_mo(fmo))) {
    cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, (void *)a,
                 8);
    return 1;
  } else {
    cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, c, 8);
    return 0;
  }
}

int __tsan_atomic8_compare_exchange_weak(volatile a8 *a, a8 *c, a8 v, morder mo,
                                         morder fmo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, (void *)a, 1);
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, c, 1);
  if (__atomic_compare_exchange_n(a, c, v, /*weak=*/0, to_gcc_mo(mo),
                                  to_gcc_mo(fmo))) {
    cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, (void *)a,
                 1);
    return 1;
  } else {
    cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, c, 1);
    return 0;
  }
}

int __tsan_atomic16_compare_exchange_weak(volatile a16 *a, a16 *c, a16 v,
                                          morder mo, morder fmo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, (void *)a, 2);
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, c, 2);
  if (__atomic_compare_exchange_n(a, c, v, /*weak=*/0, to_gcc_mo(mo),
                                  to_gcc_mo(fmo))) {
    cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, (void *)a,
                 2);
    return 1;
  } else {
    cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, c, 2);
    return 0;
  }
}

int __tsan_atomic32_compare_exchange_weak(volatile a32 *a, a32 *c, a32 v,
                                          morder mo, morder fmo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, (void *)a, 4);
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, c, 4);
  if (__atomic_compare_exchange_n(a, c, v, /*weak=*/0, to_gcc_mo(mo),
                                  to_gcc_mo(fmo))) {
    cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, (void *)a,
                 4);
    return 1;
  } else {
    cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, c, 4);
    return 0;
  }
}

int __tsan_atomic64_compare_exchange_weak(volatile a64 *a, a64 *c, a64 v,
                                          morder mo, morder fmo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, (void *)a, 8);
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, c, 8);
  if (__atomic_compare_exchange_n(a, c, v, /*weak=*/0, to_gcc_mo(mo),
                                  to_gcc_mo(fmo))) {
    cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, (void *)a,
                 8);
    return 1;
  } else {
    cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, c, 8);
    return 0;
  }
}

a8 __tsan_atomic8_compare_exchange_val(volatile a8 *a, a8 c, a8 v, morder mo,
                                       morder fmo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, (void *)a, 1);
  if (__atomic_compare_exchange_n(a, &c, v, /*weak=*/0, to_gcc_mo(mo),
                                  to_gcc_mo(fmo))) {
    cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, (void *)a,
                 1);
  }
  return c;
}

a16 __tsan_atomic16_compare_exchange_val(volatile a16 *a, a16 c, a16 v,
                                         morder mo, morder fmo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, (void *)a, 2);
  if (__atomic_compare_exchange_n(a, &c, v, /*weak=*/0, to_gcc_mo(mo),
                                  to_gcc_mo(fmo))) {
    cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, (void *)a,
                 2);
  }
  return c;
}

a32 __tsan_atomic32_compare_exchange_val(volatile a32 *a, a32 c, a32 v,
                                         morder mo, morder fmo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, (void *)a, 4);
  if (__atomic_compare_exchange_n(a, &c, v, /*weak=*/0, to_gcc_mo(mo),
                                  to_gcc_mo(fmo))) {
    cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, (void *)a,
                 4);
  }
  return c;
}

a64 __tsan_atomic64_compare_exchange_val(volatile a64 *a, a64 c, a64 v,
                                         morder mo, morder fmo) {
  cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_READ, (void *)a, 8);
  if (__atomic_compare_exchange_n(a, &c, v, /*weak=*/0, to_gcc_mo(mo),
                                  to_gcc_mo(fmo))) {
    cacheray_log(CACHERAY_EVENT_MASK_ATOMIC | CACHERAY_EVENT_WRITE, (void *)a,
                 8);
  }
  return c;
}

/* TODO: Maybe add some flag for when we are between these fences? */
void __tsan_atomic_thread_fence(morder mo) {
  __atomic_thread_fence(to_gcc_mo(mo));
}

void __tsan_atomic_signal_fence(morder mo) {
  __atomic_signal_fence(to_gcc_mo(mo));
}
