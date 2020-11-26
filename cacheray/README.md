#How to use MemTrack

##Setting environment variables

To compile with Memtrack, the environment variables *LLVM_DIR* and *LLVM_BUILD_DIR* needs to be set.

*LLVM_DIR* needs to be set to the root directory of the LLVM source. Not the LLVM project root or the build folder.

*LLVM_BUILD_DIR* needs to be set to the build directory of your LLVM build. Needs to be the target directory the *LLVM_DIR* was used with.

##Compiling

For the the testsfiles to compile, it is important to use the clang compiler which have been extended with the MallocTracker pass. Make sure to use the correct compiler (i.e. with CC=...) when setting up with cmake.

1. mkdir build

2. cd build

3. CC=/path/to/clang cmake -G "Unix Makefiles" ..

4. make

