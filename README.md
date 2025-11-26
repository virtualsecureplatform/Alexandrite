# Alexandrite
This is a small RV32uI processor designed to be used as a virtual processor like [cahp-ruby](https://github.com/virtualsecureplatform/cahp-ruby).

This processor is based on [riscv-mini](https://github.com/ucb-bar/riscv-mini). Some unnecessary functionality, such as CSR, is omitted to reduce the gate count and introduce more pipeline registers (5-stage) to increase clock frequency and match with a school-book-like architecture. 
