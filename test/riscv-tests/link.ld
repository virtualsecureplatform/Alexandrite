OUTPUT_ARCH( "riscv" )

ENTRY(_start)

SECTIONS
{
  . = 0x00000000;
  .text.init : { *(.text.init) }
  .text : { *(.text) }

  . = 0x00010000;
  .data : { *(.data) }
  .rodata : { *(.rodata) }
  .bss : { *(.bss) }

  /* tohost/fromhost for test communication */
  . = 0x00010100;
  .tohost : { *(.tohost) }
}
