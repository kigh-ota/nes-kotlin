fun main(args: Array<String>) {
    println("Hello")
}

typealias u8 = Int
typealias u16 = Int

class CPU {
    companion object {
        private val RESET_LOCATION: u16 = 0xFFFC
    }

    private var pc: u16 = 0
    var a: u8 = 0
    var x: u8 = 0
    var status: u8 = 0
    private val memory = MutableList(0xFFFF) { 0 }

    private fun readMemoryU8(addr: u16) = memory[addr]
    private fun writeMemoryU8(addr: u16, data: u8) {
        memory[addr] = data
    }

    // little-endian
    private fun readMemoryU16(pos: u16): u16 {
        val lo = readMemoryU8(pos)
        val hi = readMemoryU8(pos + 1)
        return hi.shl(8).or(lo)
    }

    private fun writeMemoryU16(pos: u16, data: u16) {
        val hi = data.shr(8)
        val lo = data.and(0xff)
        writeMemoryU8(pos, lo)
        writeMemoryU8(pos + 1, hi)
    }

    fun reset() {
        a = 0
        x = 0
        status = 0

        pc = readMemoryU16(RESET_LOCATION)
    }

    fun loadAndRun(program: List<u8>) {
        load(program)
        reset()
        run()
    }

    fun load(program: List<u8>) {
        program.forEachIndexed { i, d ->
            memory[0x8000 + i] = d
        }
        writeMemoryU16(RESET_LOCATION, 0x8000)
    }

    fun run() {
        repeat(10000) {
            val opcode = readMemoryU8(pc)
            pc++
            when (opcode) {
                0xA9 -> {   // LDA
                    val param = readMemoryU8(pc)
                    pc++
                    lda(param)
                }
                0xAA -> tax()
                0xE8 -> inx()
                0x00 -> return // BRK
                else -> error("Not implemented")
            }
        }

    }

    private fun lda(value: u8) {
        a = value
        updateZeroAndNegativeFlags(a)
    }

    private fun tax() {
        x = a
        updateZeroAndNegativeFlags(x)
    }

    private fun inx() {
        x = (x + 1) % 256
        updateZeroAndNegativeFlags(x)
    }

    private fun updateZeroAndNegativeFlags(result: u8) {
        // zero flag
        status = if (result == 0) {
            status.or(0b0000_0010)
        } else {
            status.and(0b1111_1101)
        }
        // negative flag
        status = if (result.and(0b1000_0000) != 0) {
            status.or(0b1000_0000)
        } else {
            status.and(0b0111_1111)
        }
    }
}
