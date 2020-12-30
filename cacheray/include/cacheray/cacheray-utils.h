#ifndef __CACHERAY_UTIL_H__
#define __CACHERAY_UTIL_H__

// Functions which can be good

int cacheray_util_write_out(const void *buffer, unsigned long size,
                            unsigned long nmemb, const char *fname);

void cacheray_util_get_file_name(char *fname);

void cacheray_util_simple_log_begin(unsigned long buf_size);

void cacheray_util_simple_log_end();

#endif
