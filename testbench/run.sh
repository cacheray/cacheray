#!/bin/bash

# This program runs the test and writes it to a report
FILE=testrun-$(date -I)
TIME=/usr/bin/time
SIM=../sim/simulator_java/target/simple-sim-java-0.2.0.jar

# Import vars from simple/init.sh
. ./simple/init.sh
. ./reorder/init.sh

# Clean up everything
make --directory=./simple clean
make --directory=./reorder clean

# Make the programs
make --directory=./simple
make --directory=./reorder

# Create traces
CACHERAY_FILENAME=good ./simple/simple.out g
CACHERAY_FILENAME=bad  ./simple/simple.out b

CACHERAY_FILENAME=good-reorder ./reorder/reorder.out g
CACHERAY_FILENAME=bad-reorder ./reorder/reorder.out b
mv good.0 simple/good.trace
mv bad.0 simple/bad.trace
mv good-reorder.0 reorder/good-reorder.trace
mv bad-reorder.0 reorder/bad-reorder.trace

echo -e "Size difference:\n" > $FILE
ls -l simple/*.out >> $FILE

# Proper time differnce
hyperfine --warmup 10 --style basic './simple/simple-no-instrum.out g' &>> $FILE
hyperfine --warmup 10 --style basic './simple/simple-no-instrum.out b' &>> $FILE

# Resource difference
echo "Testing resource usage..."
echo -e "\nWith instrum:\n" >> $FILE
valgrind -q --tool=massif --pages-as-heap=yes --massif-out-file=massif.out ./simple/simple.out g; grep mem_heap_B massif.out | sed -e 's/mem_heap_B=\(.*\)/\1/' | sort -g | tail -n 1 &>> $FILE

echo -e "\nWithout instrum:\n" >> $FILE
valgrind -q --tool=massif --pages-as-heap=yes --massif-out-file=massif.out ./simple/simple-no-instrum.out g; grep mem_heap_B massif.out | sed -e 's/mem_heap_B=\(.*\)/\1/' | sort -g | tail -n 1 &>> $FILE

# Test with cachegrind
valgrind --tool=cachegrind ./simple/simple-no-instrum.out g &>> $FILE
valgrind --tool=cachegrind ./simple/simple-no-instrum.out b &>> $FILE

# Time to run the simulator
echo -e "Running the simulator. This might take a while..."
java -jar $SIM -i $CACHERAY_TRACE_GOOD -d $CACHERAY_TYPEDATA -c $CACHERAY_CONFIG --test-case >> $FILE
java -jar $SIM -i $CACHERAY_TRACE_BAD -d $CACHERAY_TYPEDATA -c $CACHERAY_CONFIG --test-case >> $FILE

# Lets do some reordering
# First the good in cachegrind
valgrind --tool=cachegrind ./reorder/reorder-no-instrum.out g &>> $FILE
# then the bad
valgrind --tool=cachegrind ./reorder/reorder-no-instrum.out b &>> $FILE

echo -e "Running the simulator for reorder..."
# Lets test both in simulator
java -jar $SIM -i $CACHERAY_TRACE_REORDER_GOOD -d $CACHERAY_TYPEDATA_REORDER -c $CACHERAY_CONFIG --test-case >> $FILE
java -jar $SIM -i $CACHERAY_TRACE_REORDER_BAD -d $CACHERAY_TYPEDATA_REORDER -c $CACHERAY_CONFIG --test-case >> $FILE

# Now, lets test with the values swapped
java -jar $SIM -i $CACHERAY_TRACE_REORDER_BAD -d $CACHERAY_TYPEDATA_REORDER -c $CACHERAY_CONFIG -r $CACHERAY_REORDER_REMAP --test-case >> $FILE

# Clean up
rm -f cachegrind.* cacheray.trace.* simple/*.trace reorder/*.trace
