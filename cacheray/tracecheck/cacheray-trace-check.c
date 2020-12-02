#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#if __has_feature(thread_sanitizer)
#error "Don't sanitize this code"
#endif

#include "cacheray/cacheray-utils.h"

int main(int argc, char **argv) {
  FILE *fp;
  cacheray_access_stats_t *stats = calloc(1, sizeof(cacheray_access_stats_t));
  if (argc < 2) {
    // Need args
    printf("Needs a file to check\n");
  }

  // Init all stats to -1, so we can see which were incremented.
  //memset(stats->accs, -1, sizeof(stats->accs) / sizeof(stats->accs[0]));
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
