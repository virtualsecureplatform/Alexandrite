
case class Config() {
    var debugIf = false
    var debugId = false
    var debugEx = false
    var debugMem = false
    var debugWb = false

    var test = false
    var testRom:Seq[BigInt] = Seq(BigInt(0))
    var testRam:Seq[BigInt] = Seq(BigInt(0))

    val instWidth = 32
    val datainbyte = 4
    val dataWidth = datainbyte*8
    val regBit = 5

    //IF Unit
    val romAddrWidth = 10
    val romDataWidth = dataWidth

    val instAddrWidth = romAddrWidth+2

    var ramAddrWidth = 10
    val ramDataWidth = dataWidth

    val dataAddrWidth = ramAddrWidth+1
}
