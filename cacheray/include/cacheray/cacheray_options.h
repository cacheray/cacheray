#ifndef CACHERAY_OPTIONS_H_INCLUDED
#define CACHERAY_OPTIONS_H_INCLUDED

typedef struct cacheray_options {
  int enabled;
  char tracefile[256];
} cacheray_options_t;

void cacheray_options_init_defaults(cacheray_options_t *options);

/** Parse an options string into a cacheray_options_t.
 *
 * Options strings are on the format 'key=value:x=y', i.e. colon-separated
 * key-value pairs.
 *
 * @return non-zero on success.
 */
int cacheray_options_parse(const char *optstr, cacheray_options_t *options);

#endif
