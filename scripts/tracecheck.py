#!/usr/bin/python3

import sys
import struct

def print_header(header):
    print('IDENT: \t' + str(header[0]))
    print('N_EVENTS: \t' + str(header[1]))
    print('OUT_OF_MEMORY: \t' + str(header[2]))
    print('TYPE_LEN: \t' + str(header[3]))
    print('ADDR_LEN: \t' + str(header[4]))
    print('STRUCT_POS_LEN: \t' + str(header[5]))
    print('STRUCT_SIZE_LEN: \t' + str(header[6]))
    print('STRUCT_ID_LEN: \t' + str(header[7]))

def decode_header(fp):
    data = []
    header_length = int.from_bytes(fp.read(1), byteorder=sys.byteorder)
    return struct.unpack('=HQ6B', fp.read(int(header_length)))

def check_header(fp, header):
    print("Checking trace for irregularities...")
    n_events = header[1]


    for i in range(n_events):


if __name__ == '__main__':
    fp = open(sys.argv[1], 'rb')
    #data = fp.read()
    header_data = decode_header(fp)
    print_header(header_data)
