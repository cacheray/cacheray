#ifndef __CACHERAY_UTIL_H__
#define __CACHERAY_UTIL_H__

#include <cacheray/cacheray.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>

#define CACHERAY_CHECK_CORRECT (1)
#define CACHERAY_CHECK_WRONG_TYPE (-1)

typedef struct {
  int ret;
  int accs[UINT8_MAX];
} cacheray_access_stats_t;

// Functions which can be good

int cacheray_util_write_out(const void *buffer, unsigned long size,
                            unsigned long nmemb, const char *fname);

void cacheray_util_get_file_name(char *fname);

int cacheray_check_trace(FILE *trace_file, cacheray_access_stats_t *stats);

void cacheray_util_simple_log_begin(unsigned long buf_size);

void cacheray_util_simple_log_end();

#endif
