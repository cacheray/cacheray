#include "cacheray/cacheray-utils.h"
#include "cacheray/cacheray.h"
#include "matrixer.h"
#include "sorter.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define TRACEBUFFER_SIZE (65536)
#define SORTER_BUF_SIZE (100)
#define FILE_BASENAME "cooperative"

// Mimics the temporary buf
static cacheray_log_t TRACEBUFFER[TRACEBUFFER_SIZE];

// Mimics pre-write storage
static cacheray_log_t *STORAGE = NULL;
static size_t storage_pos = 0;

static unsigned int TRACE_DATA_INDEX = 0;
static unsigned int file_index = 0;

char fname[256];

// This is a good example on how one can make a callback function
void handle_trace_data(const void *buf, unsigned long size,
                       unsigned long nmemb) {
  sprintf(fname, "%s.%d.%s", FILE_BASENAME, file_index++, "trace");
  // FILE *fp = fopen(fname, "w");
  cacheray_util_write_out(buf, size, nmemb, fname);
}

int main() {
  size_t final_size = 0;
  int arr[SORTER_BUF_SIZE];
  char filename[256];

  sorter_fill_random(arr, SORTER_BUF_SIZE);

  cacheray_start(TRACEBUFFER, TRACEBUFFER_SIZE, &handle_trace_data);

  sorter_bubble(arr, SORTER_BUF_SIZE);

  final_size = cacheray_stop();

  // Write everything to a file
  cacheray_util_get_file_name(filename);
  cacheray_util_write_out(STORAGE, storage_pos, storage_pos, filename);
  free(STORAGE);

  return 0;
  // handle_trace_data(TRACEBUF, final);
}
