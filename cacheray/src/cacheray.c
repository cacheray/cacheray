#if __has_feature(thread_sanitizer)
#error "Don't sanitize cacheray"
#endif

#include <assert.h>
#include <cacheray/cacheray.h>
#include <cacheray/cacheray_options.h>
#include <cacheray/cacheray_trace.h>
#include <stdint.h>
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
static cacheray_options_t cacheray_opts;

static inline uint64_t cacheray_threadid(void) {
  /* TODO: Use some threading library construct */
  return 0;
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
    cacheray_options_init_defaults(&cacheray_opts);

    const char *envopts = getenv("CACHERAY_OPTIONS");
    if (envopts) {
      if (!cacheray_options_parse(envopts, &cacheray_opts)) {
        fprintf(stderr, "Cacheray: failed to parse options (out of memory)\n");
        abort();
      }
    }

    cacheray_fp = fopen(cacheray_opts.tracefile, "w");
    if (!cacheray_fp) {
      perror("Cacheray: failed to open trace file");
      abort();
    }

    atexit(cacheray_shutdown);
    enabled = cacheray_opts.enabled;
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

  cacheray_put_u8(cacheray_fp, type);
  cacheray_put_ptr(cacheray_fp, addr);
  cacheray_put_u8(cacheray_fp, size);
  cacheray_put_u64(cacheray_fp, cacheray_threadid());
}

static void cacheray_rtta_add(void *addr, const char *typename,
                              unsigned elem_size, unsigned elem_count) {
  if (!enabled)
    return;

  cacheray_put_u8(cacheray_fp, CACHERAY_EVENT_RTTA_ADD);
  cacheray_put_ptr(cacheray_fp, addr);
  cacheray_put_u64(cacheray_fp, cacheray_threadid());
  cacheray_put_u32(cacheray_fp, elem_size);
  cacheray_put_u32(cacheray_fp, elem_count);
  cacheray_put_str(cacheray_fp, typename);
}

static void cacheray_rtta_remove(void *addr) {
  if (!enabled)
    return;

  cacheray_put_u8(cacheray_fp, CACHERAY_EVENT_RTTA_REMOVE);
  cacheray_put_ptr(cacheray_fp, addr);
  cacheray_put_u64(cacheray_fp, cacheray_threadid());
}

#include "cacheray_instrum.inc"
