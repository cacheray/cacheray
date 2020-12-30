#include <stdio.h>
#include <stdlib.h>

#define N (1024)
#define M (1024)
#define ASSOC (8)
#define B_SIZE (64)

struct OrderA {
  int a;
  char pad[B_SIZE - sizeof(int)];
  int b;
  char pad2[B_SIZE - sizeof(int)];
};

struct OrderB {
  int a;
  int b;
  char pad[B_SIZE - sizeof(int)];
  char pad2[B_SIZE - sizeof(int)];
};

typedef struct OrderA order_a_t;
typedef struct OrderB order_b_t;

void loop_A(order_a_t *list) {
  for (unsigned int mult = 0; mult < M; mult++) {
    for (unsigned int i = ASSOC; i < N; i++) {
      list[i].a = list[i].a + list[i - ASSOC].b;
      list[i].b = list[i].b + list[i - ASSOC].a;
    }
  }
}

void loop_B(order_b_t *list) {
  for (unsigned int mult = 0; mult < M; mult++) {
    for (unsigned int i = ASSOC; i < N; i++) {
      list[i].a = list[i].a + list[i - ASSOC].b;
      list[i].b = list[i].b + list[i - ASSOC].a;
    }
  }
}

int main(int argc, char **argv) {
  if (argc < 2) {
    printf("Need arg\n");
    return -1;
  }
  if (argv[1][0] == 'b') {
    order_a_t *list = calloc(N * M, sizeof(order_a_t));
    // cacheray_rtta_add(list, "order_a_t", sizeof(order_a_t), N*8);
    loop_A(list);
    free(list);
  } else {
    order_b_t *list = calloc(N * M, sizeof(order_b_t));
    // cacheray_rtta_add(list, "order_b_t", sizeof(order_a_t), N*8);
    loop_B(list);
    free(list);
  }
  return 0;
}
