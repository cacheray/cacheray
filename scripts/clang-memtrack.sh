#!/bin/bash

# This is a test script which will take a given C source file (*.c)
# and compile it into an object file with 

FILE=$1
FILE_BN=$(basename $FILE .c)
FILE_LL=$FILE_BN.ll
FILE_OUT=$FILE_BN.o

CFLAGS=-lm

# Checks that LLVM_DIR is set
if [ -z $LLVM_DIR ]; then
    echo "LLVM_DIR environment variable needs to be set!"
    exit -1
fi

if [ ! -f "$FILE" ]; then
    echo "$FILE does not exist"
    exit -1
fi

# Compile to intermediate format
clang -S -emit-llvm -g -Wall -fsanitize=thread -o $FILE_LL $FILE

# Add malloc recognizing
opt -load $LLVM_DIR/build/lib/LLVMTest.so -test -S -o $FILE_LL $FILE_LL

# 
clang -c -o $FILE_OUT $FILE_LL

# Cleanup
rm -f $FILE_LL
