#include "cacheray/cacheray.h"
#include <cacheray/cacheray_trace.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#if __has_feature(thread_sanitizer)
#error "Don't sanitize this code"
#endif

#define CACHERAY_CHECK_CORRECT (1)
#define CACHERAY_CHECK_WRONG_TYPE (-1)

typedef struct {
  int ret;
  int accs[UINT8_MAX];
} cacheray_access_stats_t;

static int cacheray_check_trace(FILE *trace_file,
                                cacheray_access_stats_t *stats) {
  cacheray_event_t type;
  void *addr;
  uint8_t size;
  uint64_t threadid;
  uint32_t elem_size;
  uint32_t elem_count;
  char typename[256];

  int ch;
  while ((ch = fgetc(trace_file)) != EOF) {
    type = (unsigned char)ch;

    /* Mask off high bits */
    unsigned char base_type =
        type & ~(CACHERAY_EVENT_MASK_UNALIGNED | CACHERAY_EVENT_MASK_ATOMIC);

    /* Type-check */
    switch (base_type) {
    case CACHERAY_EVENT_READ ... CACHERAY_EVENT_WRITE:
      cacheray_get_ptr(trace_file, &addr);
      cacheray_get_u8(trace_file, &size);
      cacheray_get_u64(trace_file, &threadid);
      break;

    case CACHERAY_EVENT_RTTA_ADD:
      cacheray_get_ptr(trace_file, &addr);
      cacheray_get_u64(trace_file, &threadid);
      cacheray_get_u32(trace_file, &elem_size);
      cacheray_get_u32(trace_file, &elem_count);
      /* We don't care about the data, so allow truncation */
      cacheray_get_str_trunc(trace_file, typename, sizeof(typename));
      break;

    case CACHERAY_EVENT_RTTA_REMOVE:
      cacheray_get_ptr(trace_file, &addr);
      cacheray_get_u64(trace_file, &threadid);
      break;

    default:
      return CACHERAY_CHECK_WRONG_TYPE;
    };

    /* Update stats */
    if (stats) {
      if (stats->accs[ch] == -1) {
        stats->accs[ch] = 0;
      }
      stats->accs[ch] += 1;
    }
  }

  return CACHERAY_CHECK_CORRECT;
}

int main(int argc, char **argv) {
  FILE *fp;
  cacheray_access_stats_t *stats = calloc(1, sizeof(cacheray_access_stats_t));
  if (argc < 2) {
    // Need args
    printf("Needs a file to check\n");
  }

  // Init all stats to -1, so we can see which were incremented.
  // memset(stats->accs, -1, sizeof(stats->accs) / sizeof(stats->accs[0]));
  for (int i = 0; i < UINT8_MAX; i++) {
    stats->accs[i] = -1;
  }

  // TODO: check multiple files
  fp = fopen(argv[1], "r");

  int res = cacheray_check_trace(fp, stats);

  switch (res) {
  case CACHERAY_CHECK_WRONG_TYPE:
    printf("The type was wrong\n");
    break;

  default:
    printf("The file was correct\n");
    break;
  }
  printf("Access stats:\n");
  for (int i = 0; i < UINT8_MAX; i++) {
    if (stats->accs[i] > -1) {
      printf("\t%d: %d\n", i, stats->accs[i]);
    }
  }

  free(stats);

  return 0;
}
