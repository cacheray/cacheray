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

static inline int cacheray_get_data(void *fp, void *v, size_t size) {
  return fread(v, size, 1, fp);
}

static inline void cacheray_skip(void *fp, size_t offset) {
  fseek(fp, offset, SEEK_CUR);
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

static inline int cacheray_get_ptr(void *fp, void **ptr) {
  return cacheray_get_data(fp, ptr, sizeof(*ptr));
}

static inline int cacheray_get_u8(void *fp, uint8_t *val) {
  return cacheray_get_data(fp, val, sizeof(*val));
}

static inline int cacheray_get_u32(void *fp, uint32_t *val) {
  return cacheray_get_data(fp, val, sizeof(*val));
}

static inline int cacheray_get_u64(void *fp, uint64_t *val) {
  return cacheray_get_data(fp, val, sizeof(*val));
}

static inline int cacheray_get_str(void *fp, char *buf, size_t size) {
  uint32_t len;
  if (!cacheray_get_u32(fp, &len))
    return 0;

  if (len >= size)
    return 0;

  if (!cacheray_get_data(fp, buf, len))
    return 0;

  buf[len] = '\0';
  return 1;
}

static inline int cacheray_get_str_trunc(void *fp, char *buf, size_t size) {
  uint32_t len;
  if (!cacheray_get_u32(fp, &len))
    return 0;

  uint32_t tail = 0;
  if (len >= size) {
    tail = size - len + 1;
    len -= tail;
  }

  if (!cacheray_get_data(fp, buf, len))
    return 0;

  cacheray_skip(fp, tail);

  buf[len] = '\0';
  return 1;
}

#endif
