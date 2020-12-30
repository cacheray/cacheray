#if __has_feature(thread_sanitizer)
#error "Don't sanitize cacheray"
#endif

#include "cacheray/cacheray.h"
#include <assert.h>
#include <stdio.h>
#include <stdlib.h>

#ifdef DEBUG
#define CACHERAY_DPRINT(...) printf(__VA_ARGS__)
#else
#define CACHERAY_DPRINT(...)                                                   \
  do {                                                                         \
  } while (0)
#endif

#define CACHERAY_ASSERT(...) assert(__VA_ARGS__)

static int enabled;
static FILE *cacheray_fp;

static const char *cacheray_getenv(const char *name, const char *defaultval) {
  const char *val = getenv(name);
  if (!val)
    return defaultval;
  return val;
}

static unsigned char string_copy(char *dest, const char *src) {
  unsigned int i = 0;
  char c;
  while ((c = src[i])) {
    dest[i] = c;
    i++;
  }
  dest[i] = '\0';
  return (unsigned char)i;
}

/* Instrumentation API */
static void cacheray_shutdown(void) {
  enabled = 0;
  if (cacheray_fp) {
    fclose(cacheray_fp);
  }
}

static void cacheray_init(void) {
  static int is_initialized = 0;
  if (!is_initialized) {
    const char *tracefilename =
        cacheray_getenv("CACHERAY_FILENAME", "cacheray.trace");
    cacheray_fp = fopen(tracefilename, "w");
    if (!cacheray_fp) {
      perror("Cacheray: failed to open trace file");
      abort();
    }

    atexit(cacheray_shutdown);
    enabled = 1;
    is_initialized = 1;
  }
}

/**
 * Log a single address event
 * @param type event type
 * @param addr address
 */
static void cacheray_log(cacheray_event_t type, void *addr,
                         unsigned char size) {
  // Return instantly if not turned on
  if (!enabled)
    return;

  cacheray_log_t tmp = {
      .type = type, .addr = addr, .size = size, .threadid = 0};
  unsigned int log_size = sizeof(cacheray_log_t);
  fwrite(&tmp, log_size, 1, cacheray_fp);
}

static void cacheray_rtta_add(void *addr, const char *typename,
                              unsigned elem_size, unsigned elem_count) {
  if (!enabled)
    return;

  cacheray_rtta_add_t tmp = {.type = CACHERAY_EVENT_RTTA_ADD,
                             .threadid = 0,
                             .addr = addr,
                             .elem_size = elem_size,
                             .elem_count = elem_count};
  tmp.typename_len = string_copy(tmp.typename, typename);
  // Full struct - buffer size + typename length
  unsigned int log_size = sizeof(tmp) - sizeof(tmp.typename) + tmp.typename_len;
  fwrite(&tmp, log_size, 1, cacheray_fp);
}

static void cacheray_rtta_remove(void *addr) {
  if (!enabled)
    return;

  cacheray_rtta_remove_t tmp = {
      .type = CACHERAY_EVENT_RTTA_REMOVE,
      .threadid = 0,
      .addr = addr,
  };
  fwrite(&tmp, sizeof(tmp), 1, cacheray_fp);
}

#include "cacheray_instrum.inc"
