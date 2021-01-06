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

#endif
