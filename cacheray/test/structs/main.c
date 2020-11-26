/**
 * This program is a test of functionalities of the struct capturing.
 */

#include "cacheray/cacheray-utils.h"
#include "cacheray/cacheray.h"
#include <stdio.h>
#include <stdlib.h>

#if !(__has_feature(thread_sanitizer))
#error "This code needs to be sanitized"
#endif

#define BUFFER_SIZE (10000)
#define FILE_BASENAME "cacheray"

// A basic structs
struct Basic {
  int a;
  long b;
  char *c;
};

// A basic typedeffed struct
typedef struct TdBasic {
  int at;
  int bt;
} TheTdBasic;

// A struct that contains other structs
struct Deep {
  int a;
  struct Basic b;
  struct Basic *c;
  TheTdBasic d;
  TheTdBasic *e;
};

cacheray_log_t BUFFER[BUFFER_SIZE];

char fname[256];

static int file_index = 0;

void handle_data(const void *buf, unsigned long size, unsigned long nmemb) {
  sprintf(fname, "%s.%d.%s", FILE_BASENAME, file_index++, "trace");
  // FILE *fp = fopen(fname, "w");
  cacheray_util_write_out(buf, size, nmemb, fname);
}

int main(int argc, char **argv) {
  // Test basic
  char *text = "This is some text";
  struct Basic basic_instance;

  // Here we start the tracking
  cacheray_start(BUFFER, BUFFER_SIZE, &handle_data);

  // Some basic global fun
  basic_instance.a = 1;
  printf("basic_instance.a=%d\n", basic_instance.a);
  basic_instance.c = text;

  // How about some heap stuff?
  struct Basic *basic_alloc = malloc(sizeof(struct Basic));
  TheTdBasic *ttdBasic_alloc = calloc(1, sizeof(TheTdBasic));
  struct Deep *big_boy_struct_alloc = malloc(sizeof(struct Deep));

  // Lets do some writing
  basic_alloc->a = 1;
  basic_alloc->b = 1337;
  printf("basic_alloc: %d, %ld, \"%s\" \n", basic_alloc->a, basic_alloc->b,
         basic_alloc->c);
  printf("ttdBasic, should both be zero: %d %d\n", ttdBasic_alloc->at,
         ttdBasic_alloc->bt);

  free(basic_alloc);
  free(ttdBasic_alloc);

  cacheray_stop();

  return 0;
}
