/**
 * This program is a test of functionalities of the struct capturing.
 */

#include <stdio.h>
#include <stdlib.h>

#if !(__has_feature(thread_sanitizer))
#error "This code needs to be sanitized"
#endif

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

int main(int argc, char **argv) {
  // Test basic
  char *text = "This is some text";
  struct Basic basic_instance;

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

  return 0;
}
