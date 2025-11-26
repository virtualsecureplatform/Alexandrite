# CMakeLists.txt for riscv-tests

# Build the test runner
add_executable(riscv_test_runner sim_riscv_test.cpp)
target_include_directories(riscv_test_runner
PUBLIC
${PROJECT_SOURCE_DIR}/thirdparties/ELFIO
)
verilate(riscv_test_runner SOURCES ../../CoreUnit.v THREADS 2 TRACE_FST)

# Copy necessary files to build directory
file(COPY riscv_test.h DESTINATION .)
file(COPY link.ld DESTINATION .)

# List of rv32ui tests to build (basic integer instructions)
set(RV32UI_TESTS
  add
  addi
  and
  andi
  auipc
  beq
  bge
  bgeu
  blt
  bltu
  bne
  jal
  jalr
  lb
  lbu
  lh
  lhu
  lui
  lw
  or
  ori
  sb
  sh
  sll
  slli
  slt
  slti
  sltiu
  sltu
  sra
  srai
  srl
  srli
  sub
  sw
  xor
  xori
)

# Create directory for compiled tests
file(MAKE_DIRECTORY ${CMAKE_CURRENT_BINARY_DIR}/bin)

# Custom target to build all tests
add_custom_target(rv32ui_tests)

# For each test, compile it
foreach(test ${RV32UI_TESTS})
  add_custom_command(
    OUTPUT ${CMAKE_CURRENT_BINARY_DIR}/bin/rv32ui-${test}
    COMMAND riscv32-unknown-elf-gcc
      -march=rv32i -mabi=ilp32
      -nostdlib -nostartfiles
      -I${CMAKE_CURRENT_SOURCE_DIR}
      -I${PROJECT_SOURCE_DIR}/thirdparties/riscv-tests/isa/macros/scalar
      -T ${CMAKE_CURRENT_SOURCE_DIR}/link.ld
      -o ${CMAKE_CURRENT_BINARY_DIR}/bin/rv32ui-${test}
      ${PROJECT_SOURCE_DIR}/thirdparties/riscv-tests/isa/rv32ui/${test}.S
    DEPENDS
      ${PROJECT_SOURCE_DIR}/thirdparties/riscv-tests/isa/rv32ui/${test}.S
      ${CMAKE_CURRENT_SOURCE_DIR}/riscv_test.h
      ${CMAKE_CURRENT_SOURCE_DIR}/link.ld
    COMMENT "Building rv32ui-${test}"
  )
  add_custom_target(rv32ui-${test} DEPENDS ${CMAKE_CURRENT_BINARY_DIR}/bin/rv32ui-${test})
  add_dependencies(rv32ui_tests rv32ui-${test})
endforeach()

# Create a test script
file(WRITE ${CMAKE_CURRENT_BINARY_DIR}/run_tests.sh
"#!/bin/bash
PASS=0
FAIL=0
SKIP=0

for test in ${CMAKE_CURRENT_BINARY_DIR}/bin/rv32ui-*; do
  name=\\$(basename \\$test)
  result=\\$(${CMAKE_CURRENT_BINARY_DIR}/riscv_test_runner \\$test 2>&1)
  ret=\\$?
  if [ \\$ret -eq 0 ]; then
    echo \"PASS: \\$name\"
    ((PASS++))
  else
    echo \"FAIL: \\$name - \\$result\"
    ((FAIL++))
  fi
done

echo \"\"
echo \"Results: \\$PASS passed, \\$FAIL failed\"
exit \\$FAIL
")

# Make the script executable
add_custom_command(TARGET riscv_test_runner POST_BUILD
  COMMAND chmod +x ${CMAKE_CURRENT_BINARY_DIR}/run_tests.sh
)
