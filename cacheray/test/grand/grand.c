#include <stdio.h>
#include <stdlib.h>

#include <cacheray/cacheray-utils.h>
#include <cacheray/cacheray.h>

/*
 * Here are the structs that we are working with:
 */

#define LINK_LENGTH (100)

struct Link {
  struct Link *next;
  int data;
};

void free_ll(struct Link *head) {
  if (head->next == NULL) {
    free(head);
  } else {
    free_ll(head->next);
    free(head);
  }
}

void commit(const void *buf, unsigned long size, unsigned long nmemb) {
  char fname[100];
  cacheray_util_get_file_name(fname);
  cacheray_util_write_out(buf, size, nmemb, fname);
}

int main(int argc, char **argv) {
  void *buf = malloc((2 << 15));

  cacheray_start(buf, (2 << 15), commit);

  // LL creation
  struct Link *curr = NULL;
  struct Link *prev = NULL;
  for (int i = 0; i < LINK_LENGTH; i++) {
    curr = malloc(sizeof(struct Link));
    curr->data = i;
    curr->next = prev;
    prev = curr;
  }

  struct Link *head = curr;

  // LL search
  while (curr->data != LINK_LENGTH / 2) {
    curr = curr->next;
  }

  // Free
  free_ll(head);

  cacheray_stop();

  free(buf);

  return 0;
}
