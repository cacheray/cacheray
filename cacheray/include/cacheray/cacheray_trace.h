#ifndef CACHERAY_TRACE_H_INCLUDED
#define CACHERAY_TRACE_H_INCLUDED

#include <assert.h>
#include <stdint.h>
#include <stdio.h>
#include <string.h>

static inline void cacheray_put_data(void *fp, const void *v, size_t size) {
  int res = fwrite(v, size, 1, fp);
  assert(res == 1);
}

static inline void cacheray_put_ptr(void *fp, const void *ptr) {
  cacheray_put_data(fp, &ptr, sizeof(ptr));
}

static inline void cacheray_put_u8(void *fp, const uint8_t val) {
  cacheray_put_data(fp, &val, sizeof(val));
}

static inline void cacheray_put_u32(void *fp, const uint32_t val) {
  cacheray_put_data(fp, &val, sizeof(val));
}

static inline void cacheray_put_u64(void *fp, const uint64_t val) {
  cacheray_put_data(fp, &val, sizeof(val));
}

static inline void cacheray_put_str(void *fp, const char *val) {
  uint32_t len = strlen(val);
  cacheray_put_u32(fp, len);
  cacheray_put_data(fp, val, len);
}

#endif
