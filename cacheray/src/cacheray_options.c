#include "cacheray/cacheray_options.h"

#include <assert.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/** Return a C string containing a copy of the characters in range [begin, end)
 *
 * Return value must be deallocated with free().
 */
static char *cacheray_options_range_dup(const char *begin, const char *end) {
  assert(end >= begin);

  size_t len = (end - begin);
  char *buf = malloc(len + 1);
  memcpy(buf, begin, len);
  buf[len] = 0;

  return buf;
}

static void cacheray_options_set(cacheray_options_t *options, const char *key,
                                 const char *val) {
  if (!strcmp(key, "tracefile")) {
    int r = snprintf(options->tracefile, sizeof(options->tracefile), "%s", val);
    if (r >= sizeof(options->tracefile)) {
      fprintf(stderr, "Cacheray: tracefile too long: '%s'\n", val);
      abort();
    } else if (r < 0) {
      fprintf(stderr, "Cacheray: tracefile error %d for '%s'\n", r, val);
      abort();
    }
  } else {
    fprintf(stderr, "Cacheray: unknown option: '%s'\n", key);
  }
}

void cacheray_options_init_defaults(cacheray_options_t *options) {
  strcpy(options->tracefile, "cacheray.trace");
}

int cacheray_options_parse(const char *optstr, cacheray_options_t *options) {
  char *tokstr = NULL;
  char *key = NULL;
  char *val = NULL;
  char *sp;

  // Duplicate the input to get a mutable string
  tokstr = strdup(optstr);
  if (!tokstr)
    goto fail;

  char *optpair = strtok_r(tokstr, ":", &sp);
  while (optpair) {
    // optpairs have the following form:
    //   KEY=VALUE\0
    // where KEY= and VALUE\0 forms two ranges respectively: [kb,ke) and [vb,ve)

    // Parse key
    const char *kb = optpair;
    const char *ke = kb;
    while (*ke && *ke != '=')
      ++ke;

    // Parse value
    const char *vb = ke;
    if (*vb == '=')
      ++vb;
    const char *ve = vb;
    while (*ve)
      ++ve;

    // Dupe the ranges into proper C strings
    key = cacheray_options_range_dup(kb, ke);
    val = cacheray_options_range_dup(vb, ve);
    if (!key || !val)
      goto fail;

    // Update options struct
    cacheray_options_set(options, key, val);

    free(key);
    free(val);
    optpair = strtok_r(NULL, ":", &sp);
  }

  free(tokstr);
  return 1;

fail:
  free(tokstr);
  free(key);
  free(val);
  return 0;
}
