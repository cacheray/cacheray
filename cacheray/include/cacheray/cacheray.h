/** Cacheray event type definitions.
 */

#ifndef CACHERAY_H_INCLUDED
#define CACHERAY_H_INCLUDED

typedef unsigned char cacheray_event_t;

#define CACHERAY_EVENT_READ (cacheray_event_t)(0)
#define CACHERAY_EVENT_WRITE (cacheray_event_t)(1)
#define CACHERAY_EVENT_RTTA_ADD (cacheray_event_t)(2)
#define CACHERAY_EVENT_RTTA_REMOVE (cacheray_event_t)(3)

#define CACHERAY_EVENT_MASK_ATOMIC (cacheray_event_t)(1 << 6)
#define CACHERAY_EVENT_MASK_UNALIGNED (cacheray_event_t)(1 << 7)

#endif
