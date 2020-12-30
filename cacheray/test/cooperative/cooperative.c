#include "matrixer.h"
#include "sorter.h"

#define SORTER_BUF_SIZE (100)

int main() {
  int arr[SORTER_BUF_SIZE];

  sorter_fill_random(arr, SORTER_BUF_SIZE);

  sorter_bubble(arr, SORTER_BUF_SIZE);

  return 0;
  // handle_trace_data(TRACEBUF, final);
}
