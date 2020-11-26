/*! \file cacheray.h
    \brief Cacheray structs and defines

    This file needs to be included to use Cacheray.
*/

/*! \def LOG_FILE_URL
    \brief The standard file name for the tracefile.
*/

/*! \def LOG_FILE_HEADER_LENGTH
    \brief Length of the header.
*/

#ifndef __CACHERAY_H__
#define __CACHERAY_H__

typedef unsigned char cacheray_event_t;

#define CACHERAY_EVENT_READ (cacheray_event_t)(0)
#define CACHERAY_EVENT_WRITE (cacheray_event_t)(1)
#define CACHERAY_EVENT_RTTA_ADD (cacheray_event_t)(2)
#define CACHERAY_EVENT_RTTA_REMOVE (cacheray_event_t)(3)

#define CACHERAY_EVENT_MASK_UNALIGNED (cacheray_event_t)(1 << 7)
#define CACHERAY_EVENT_MASK_ATOMIC (cacheray_event_t)(1 << 6)

/* Memory access events */
typedef struct __attribute__((__packed__)) cacheray_log_l {
  cacheray_event_t type;
  void *addr;
  unsigned char size;
  unsigned long threadid; /* big enough to fit a pthread_t :-/ */
} cacheray_log_t;

/* Run-Time Type Annotation (RTTA) events */
typedef struct __attribute__((__packed__)) cacheray_rtta_add {
  cacheray_event_t type;
  void *addr;
  unsigned long threadid;
  unsigned elem_size;
  unsigned elem_count;
  unsigned typename_len;
  char typename[256];
} cacheray_rtta_add_t;

typedef struct __attribute__((__packed__)) cacheray_rtta_remove {
  cacheray_event_t type;
  void *addr;
  unsigned long threadid;
} cacheray_rtta_remove_t;

typedef void (*cacheray_buf_cb)(const void *buf, unsigned long size,
                                unsigned long nmemb);

int cacheray_start(void *my_buffer, unsigned long my_buffer_size,
                   cacheray_buf_cb commit_func);

unsigned long cacheray_stop(void);

void cacheray_rtta_add(void *addr, const char *typename, unsigned elem_size,
                       unsigned elem_count);

void cacheray_rtta_remove(void *addr);

#endif
