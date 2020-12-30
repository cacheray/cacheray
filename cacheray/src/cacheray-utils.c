#include "cacheray/cacheray-utils.h"
#include "cacheray/cacheray.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

void cacheray_util_get_file_name(char *fname) {
  char *name = getenv("CACHERAY_FILENAME");
  if (name == NULL) {
    // Environment not set, use default
    strcpy(fname, "cacheray.trace");
  } else {
    strcpy(fname, name);
  }
}

int cacheray_util_write_out(const void *buffer, unsigned long size,
                            unsigned long nmemb, const char *fname) {
  FILE *fp = fopen(fname, "w");
  if (fp == NULL) {
    // Couldn't open file
    return -1;
  }
  fwrite(buffer, size, 1, fp);
  fclose(fp);
  return 0;
}

static char *simple_buf = NULL;
static char base_filename[128];
static char static_filename[256];
static int trace_nbr = 0;

static void buffer_callback(const void *buf, unsigned long size,
                            unsigned long nmemb) {
  cacheray_util_get_file_name(base_filename);
  sprintf(static_filename, "%s.%d", base_filename, trace_nbr);
  cacheray_util_write_out(buf, size, nmemb, static_filename);
  trace_nbr++;
}

void cacheray_util_simple_log_begin(unsigned long buf_size) {
  // Setup
  simple_buf = malloc(buf_size);
  cacheray_start(simple_buf, buf_size, &buffer_callback);
  // Start logging
}

void cacheray_util_simple_log_end() {
  // write out the rest of the data
  cacheray_stop();
  free(simple_buf);
}
