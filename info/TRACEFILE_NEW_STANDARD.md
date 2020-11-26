#Ideas for the new Tracefile Standard

##About

This document is supposed to be used as a scratchpad for discussing new ideas for the tracefile standard.

##Current header (2020-09-14)

		typedef struct {
			unsigned short version; // Holds the version number
			unsigned long long n_events; // Should hold the amount of events
			char out_of_memory; // Should hold a boolean for error check
			char type_len; // Length of types
			char addr_len; // Length of addresses
			char struct_pos_len; // You get the idea..  .
			char struct_size_len; // not used?
			char struct_id_len; // not used?
		} logfile_header_t;

##Current Traceformat (2020-09-14)

The trace is made up of 3 different structs:

		typedef struct {
			event_type_t type;
			void *addr;
		} log_t;

		typedef struct {
			event_type_t type;
			void *addr;
			unsigned long size;
			char id[30];
		} struct_pos_t;

		typedef struct {
			event_type_t type;
			void *old_addr;
			void *new_addr;
			unsigned long size;
			char id[30];
		} struct_repos_t;

##Ideas for a new header

###Short header

		typedef struct {
			unsigned short version; // Holds the version number
			unsigned long long n_events; // Should hold the amount of events
			char out_of_memory; // Should hold a boolean for error check
			char[] exe_name; // could be useful for identifying traces
		} logfile_header_t;

##Ideas for new Traceformat

###Cool beans

		typedef struct {
			event_type_t type;
			void *addr;
		} mem_event_t;

		typedef struct {
			event_type_t type;
			void *addr;
			unsigned long size;
			unsigned char id_size;
			char id[]; // pretty slick tbh
		} struct_pos_t;

Remove realloc and implement as free and alloc.

