#include "cacheray/cacheray.h"
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
  cacheray_log_t norm_event;
  cacheray_rtta_add_t norm_rtta_add;
  cacheray_rtta_remove_t norm_rtta_remove;
  cacheray_event_t type;
  int ch;
  while ((ch = fgetc(trace_file)) != EOF) {
    fseek(trace_file, -1, SEEK_CUR);
    type = (unsigned char)ch;

    /* Mask off high bits */
    unsigned char base_type =
        type & ~(CACHERAY_EVENT_MASK_UNALIGNED | CACHERAY_EVENT_MASK_ATOMIC);

    /* Type-check */
    switch (base_type) {
    case CACHERAY_EVENT_READ ... CACHERAY_EVENT_WRITE:
      fread(&norm_event, sizeof(cacheray_log_t), 1, trace_file);
      break;
    case CACHERAY_EVENT_RTTA_ADD: {
      int size = sizeof(norm_rtta_add) - sizeof(norm_rtta_add.typename);
      fread(&norm_rtta_add, size, 1, trace_file);
      fread(&norm_rtta_add.typename, 1, norm_rtta_add.typename_len, trace_file);
      break;
    }
    case CACHERAY_EVENT_RTTA_REMOVE: {
      fread(&norm_rtta_remove, sizeof(norm_rtta_remove), 1, trace_file);
      break;
    }
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
