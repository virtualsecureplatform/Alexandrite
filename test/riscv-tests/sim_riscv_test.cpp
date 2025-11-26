#include <verilated.h>
#include <verilated_fst_c.h>
#include <VCoreUnit.h>
#include <array>
#include <cstdint>
#include <iostream>
#include <elfio/elfio.hpp>
#include <string>
#include <cstring>

constexpr int romsize = 1<<14;  // 16KB ROM
constexpr int ramsize = 1<<12;  // 4KB RAM
constexpr int romword = romsize*8/32;
constexpr int ramword = ramsize*8/32;
std::array<uint32_t,romword> rom = {};
std::array<uint32_t,ramword> ram = {};

// tohost is at address 0x10000, which maps to RAM offset 0x0
constexpr int tohost_addr = 0x0;
constexpr int tohost_word = tohost_addr / 4;

void clock(VCoreUnit *dut, VerilatedFstC* tfp){
  static uint time_counter = 0;
  static uint prev_io_ramPort_addr = 0;
  static uint prev_io_ramPort_writeData = 0;
  static uint prev_io_ramPort_writeEnable = 0;
  for(int i = 0; i<2; i++){
    dut->io_romPort_data = rom[dut->io_romPort_addr % romword];
    uint ram_idx = (dut->io_ramPort_addr / 4) % ramword;
    dut->io_ramPort_readData = ram[ram_idx];
    uint prev_ram_idx = (prev_io_ramPort_addr / 4) % ramword;
    // Handle byte-level writes using writeEnable as a 4-bit byte enable mask
    if(prev_io_ramPort_writeEnable) {
      uint32_t mask = 0;
      if(prev_io_ramPort_writeEnable & 0x1) mask |= 0x000000FF;
      if(prev_io_ramPort_writeEnable & 0x2) mask |= 0x0000FF00;
      if(prev_io_ramPort_writeEnable & 0x4) mask |= 0x00FF0000;
      if(prev_io_ramPort_writeEnable & 0x8) mask |= 0xFF000000;
      ram[prev_ram_idx] = (ram[prev_ram_idx] & ~mask) | (prev_io_ramPort_writeData & mask);
    }
    if(i!=0){
      prev_io_ramPort_addr = dut->io_ramPort_addr;
      prev_io_ramPort_writeData = dut->io_ramPort_writeData;
      prev_io_ramPort_writeEnable = dut->io_ramPort_writeEnable;
    }
    dut->eval();
    if(tfp) tfp->dump(1000*time_counter);
    time_counter++;
    dut->clock = !dut->clock;
  }
}

int main(int argc, char** argv) {
  if(argc < 2) {
    std::cerr << "Usage: " << argv[0] << " <elf_file> [--trace]" << std::endl;
    return 1;
  }

  std::string elf_file = argv[1];
  bool enable_trace = (argc > 2 && std::string(argv[2]) == "--trace");

  // Load ELF file
  {
    ELFIO::elfio reader;
    if(!reader.load(elf_file)) {
      std::cerr << "Failed to load ELF file: " << elf_file << std::endl;
      return 1;
    }

    // Load all LOAD segments
    for(int seg = 0; seg < reader.segments.size(); seg++) {
      const ELFIO::segment* pseg = reader.segments[seg];
      if(pseg->get_type() != ELFIO::PT_LOAD) continue;

      uint32_t vaddr = pseg->get_virtual_address();
      uint32_t filesz = pseg->get_file_size();
      const char* data = pseg->get_data();

      if(data == nullptr) continue;

      // Determine if this goes to ROM or RAM based on address
      if(vaddr < 0x10000) {
        // ROM region
        for(uint32_t i = 0; i < filesz && (vaddr + i) < romsize; i++) {
          ((char*)&rom[0])[vaddr + i] = data[i];
        }
      } else {
        // RAM region (subtract base address)
        uint32_t ram_offset = vaddr - 0x10000;
        for(uint32_t i = 0; i < filesz && (ram_offset + i) < ramsize; i++) {
          ((char*)&ram[0])[ram_offset + i] = data[i];
        }
      }
    }
  }

  Verilated::commandArgs(argc, argv);
  VCoreUnit *dut = new VCoreUnit();

  VerilatedFstC* tfp = nullptr;
  if(enable_trace) {
    Verilated::traceEverOn(true);
    tfp = new VerilatedFstC;
    dut->trace(tfp, 100);
    tfp->open("trace.fst");
  }

  // Reset
  dut->reset = 1;
  dut->clock = 0;
  clock(dut, tfp);

  // Release reset
  dut->reset = 0;

  // Run simulation
  constexpr int max_cycles = 10000;
  int result = -1;

  for(int i = 0; i < max_cycles; i++) {
    clock(dut, tfp);

    // Check tohost for test completion
    uint32_t tohost_val = ram[tohost_word];
    if(tohost_val != 0) {
      if(tohost_val == 1) {
        // All tests passed
        result = 0;
      } else {
        // Test failed: extract test number
        // Format: (test_num << 1) | 1
        int failed_test = tohost_val >> 1;
        std::cerr << "FAILED: Test " << failed_test << " (tohost=0x"
                  << std::hex << tohost_val << std::dec << ")" << std::endl;
        result = failed_test;
      }
      break;
    }
  }

  if(result == -1) {
    std::cerr << "TIMEOUT: Test did not complete within " << max_cycles << " cycles" << std::endl;
    result = 255;
  }

  dut->final();
  if(tfp) tfp->close();

  if(result == 0) {
    std::cout << "PASSED" << std::endl;
  }

  return result;
}
