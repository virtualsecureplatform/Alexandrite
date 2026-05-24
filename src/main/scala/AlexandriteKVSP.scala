import chisel3._

class AlexandriteKVSPPort(implicit val conf: Config) extends Bundle {
  val finishFlag = Output(Bool())

  val romAddr = Output(UInt(conf.romAddrWidth.W))
  val romData = Input(UInt(conf.romDataWidth.W))

  val ramAddr = Output(UInt(conf.ramAddrWidth.W))
  val ramReadData = Input(UInt(conf.ramDataWidth.W))
  val ramWriteData = Output(UInt(conf.ramDataWidth.W))
  val ramWriteEnable = Output(UInt((conf.ramDataWidth / 8).W))

  val x0 = Output(UInt(conf.dataWidth.W))
  val x1 = Output(UInt(conf.dataWidth.W))
  val x2 = Output(UInt(conf.dataWidth.W))
  val x3 = Output(UInt(conf.dataWidth.W))
  val x4 = Output(UInt(conf.dataWidth.W))
  val x5 = Output(UInt(conf.dataWidth.W))
  val x6 = Output(UInt(conf.dataWidth.W))
  val x7 = Output(UInt(conf.dataWidth.W))
  val x8 = Output(UInt(conf.dataWidth.W))
  val x9 = Output(UInt(conf.dataWidth.W))
  val x10 = Output(UInt(conf.dataWidth.W))
  val x11 = Output(UInt(conf.dataWidth.W))
  val x12 = Output(UInt(conf.dataWidth.W))
  val x13 = Output(UInt(conf.dataWidth.W))
  val x14 = Output(UInt(conf.dataWidth.W))
  val x15 = Output(UInt(conf.dataWidth.W))
  val x16 = Output(UInt(conf.dataWidth.W))
  val x17 = Output(UInt(conf.dataWidth.W))
  val x18 = Output(UInt(conf.dataWidth.W))
  val x19 = Output(UInt(conf.dataWidth.W))
  val x20 = Output(UInt(conf.dataWidth.W))
  val x21 = Output(UInt(conf.dataWidth.W))
  val x22 = Output(UInt(conf.dataWidth.W))
  val x23 = Output(UInt(conf.dataWidth.W))
  val x24 = Output(UInt(conf.dataWidth.W))
  val x25 = Output(UInt(conf.dataWidth.W))
  val x26 = Output(UInt(conf.dataWidth.W))
  val x27 = Output(UInt(conf.dataWidth.W))
  val x28 = Output(UInt(conf.dataWidth.W))
  val x29 = Output(UInt(conf.dataWidth.W))
  val x30 = Output(UInt(conf.dataWidth.W))
  val x31 = Output(UInt(conf.dataWidth.W))
}

class AlexandriteKVSP(implicit val conf: Config) extends Module {
  val io = IO(new AlexandriteKVSPPort)

  val core = Module(new CoreUnit)
  core.io.romPort.data := io.romData
  io.romAddr := core.io.romPort.addr

  core.io.ramPort.readData := io.ramReadData
  io.ramAddr := core.io.ramPort.addr
  io.ramWriteData := core.io.ramPort.writeData
  io.ramWriteEnable := core.io.ramPort.writeEnable

  val finish = RegInit(false.B)
  when(core.io.ramPort.writeEnable.orR &&
       core.io.ramPort.addr === 4.U &&
       core.io.ramPort.writeData =/= 0.U) {
    finish := true.B
  }
  io.finishFlag := finish

  io.x0 := 0.U
  io.x1 := core.io.mainRegOut.reg(1)
  io.x2 := core.io.mainRegOut.reg(2)
  io.x3 := core.io.mainRegOut.reg(3)
  io.x4 := core.io.mainRegOut.reg(4)
  io.x5 := core.io.mainRegOut.reg(5)
  io.x6 := core.io.mainRegOut.reg(6)
  io.x7 := core.io.mainRegOut.reg(7)
  io.x8 := core.io.mainRegOut.reg(8)
  io.x9 := core.io.mainRegOut.reg(9)
  io.x10 := core.io.mainRegOut.reg(10)
  io.x11 := core.io.mainRegOut.reg(11)
  io.x12 := core.io.mainRegOut.reg(12)
  io.x13 := core.io.mainRegOut.reg(13)
  io.x14 := core.io.mainRegOut.reg(14)
  io.x15 := core.io.mainRegOut.reg(15)
  io.x16 := core.io.mainRegOut.reg(16)
  io.x17 := core.io.mainRegOut.reg(17)
  io.x18 := core.io.mainRegOut.reg(18)
  io.x19 := core.io.mainRegOut.reg(19)
  io.x20 := core.io.mainRegOut.reg(20)
  io.x21 := core.io.mainRegOut.reg(21)
  io.x22 := core.io.mainRegOut.reg(22)
  io.x23 := core.io.mainRegOut.reg(23)
  io.x24 := core.io.mainRegOut.reg(24)
  io.x25 := core.io.mainRegOut.reg(25)
  io.x26 := core.io.mainRegOut.reg(26)
  io.x27 := core.io.mainRegOut.reg(27)
  io.x28 := core.io.mainRegOut.reg(28)
  io.x29 := core.io.mainRegOut.reg(29)
  io.x30 := core.io.mainRegOut.reg(30)
  io.x31 := core.io.mainRegOut.reg(31)
}

object AlexandriteKVSPTop extends App {
  implicit val conf: Config = Config()
  (new chisel3.stage.ChiselStage).emitVerilog(new AlexandriteKVSP)
}
