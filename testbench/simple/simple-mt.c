#include <stdio.h>
#include <stdlib.h>

#define COLUMNS 1024

struct node {
  short a;
  short b;
};

typedef struct node node_t;

void by_row(node_t *data, unsigned nrows) {
  for (unsigned r = 1; r < nrows; r++) {
    for (unsigned c = 0; c < COLUMNS; c++) {
      data[r * COLUMNS + c].a += data[(r - 1) * COLUMNS + c].b;
      data[r * COLUMNS + c].b += data[(r - 1) * COLUMNS + c].a;
    }
  }
}
void by_column(node_t *data, unsigned nrows) {
  for (unsigned c = 0; c < COLUMNS; c++) {
    for (unsigned r = 1; r < nrows; r++) {
      data[r * COLUMNS + c].a =
          data[r * COLUMNS + c].a + data[(r - 1) * COLUMNS + c].b;
      data[r * COLUMNS + c].b =
          data[r * COLUMNS + c].b + data[(r - 1) * COLUMNS + c].a;
    }
  }
}

int main(int argc, char **argv) {
  if (argc < 2) {
    printf("Needs an arg");
  }
  unsigned rows = 64;

  node_t *data = calloc(COLUMNS * rows, sizeof(node_t));

  if (argv[1][0] == 'g') { // good
    by_row(data, rows);
  } else if (argv[1][0] == 'b') {
    by_column(data, rows);
  } else {
    printf("Need b or g\n");
  }

  free(data);
  return 0;
}
