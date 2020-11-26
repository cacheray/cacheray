#include <stdbool.h>
#include <stdlib.h>
#include <time.h>

#include "include/sorter.h"

void sorter_fill_random(int *array, size_t len) {
  srand(time(0));
  for (size_t i = 0; i < len; i++) {
    array[i] = rand();
  }
}

void sorter_bubble(int array[], size_t len) {
  size_t i, j;
  int temp;
  for (i = 0; i < len - 1; i++) {
    for (j = 0; j < len - i - 1; j++) {
      if (array[j] > array[j + 1]) {
        temp = array[j];
        array[j] = array[i];
        array[i] = temp;
      }
    }
  }
}
