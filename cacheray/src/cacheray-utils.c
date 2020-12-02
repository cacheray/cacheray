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

int cacheray_check_trace(FILE *trace_file, cacheray_access_stats_t *stats) {
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
