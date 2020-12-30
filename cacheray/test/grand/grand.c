#include <stdio.h>
#include <stdlib.h>

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

int main(int argc, char **argv) {
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

  return 0;
}
