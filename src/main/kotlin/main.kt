fun main(args: Array<String>) {
    println("Hello")
}

typealias u8 = Int
typealias u16 = Int


enum class AddressingMode {
    Immediate, ZeroPage, ZeroPageX, ZeroPageY, Absolute, AbsoluteX, AbsoluteY, IndirectX, IndirectY, NoneAddressing
}

class CPU {
    companion object {
        private const val RESET_LOCATION: u16 = 0xFFFC
    }

    private var pc: u16 = 0
    var a: u8 = 0
    var x: u8 = 0
    var y: u8 = 0
    var status: u8 = 0
    private val memory = MutableList(0xFFFF) { 0 }

    private fun readMemoryU8(addr: u16) = memory[addr]
    fun writeMemoryU8(addr: u16, data: u8) {
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

    private fun getOperandAddress(mode: AddressingMode): u16 = when (mode) {
        AddressingMode.Immediate -> pc
        AddressingMode.ZeroPage -> readMemoryU8(pc) // as u16
        AddressingMode.ZeroPageX -> {
            val pos = readMemoryU8(pc)
            (pos + x) % 0x10000
        }

        AddressingMode.ZeroPageY -> {
            val pos = readMemoryU8(pc)
            (pos + y) % 0x10000
        }

        AddressingMode.Absolute -> readMemoryU16(pc)
        AddressingMode.AbsoluteX -> {
            val base = readMemoryU16(pc)
            (base + x) % 0x10000
        }

        AddressingMode.AbsoluteY -> {
            val base = readMemoryU16(pc)
            (base + y) % 0x10000
        }

        AddressingMode.IndirectX -> {
            val base = readMemoryU8(pc)
            val ptr = (base + x) % 0x10000
            val lo = readMemoryU8(ptr)
            val hi = readMemoryU8((ptr + 1) % 0x10000)
            hi.shl(8).or(lo)
        }

        AddressingMode.IndirectY -> {
            val base = readMemoryU8(pc)
            val lo = readMemoryU8(base)
            val hi = readMemoryU8((base + 1) % 0x10000)
            val derefBase = hi.shl(8).or(lo)
            (derefBase + y) % 0x10000
        }

        AddressingMode.NoneAddressing -> TODO()
    }

    private fun reset() {
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

    private fun load(program: List<u8>) {
        program.forEachIndexed { i, d ->
            memory[0x8000 + i] = d
        }
        writeMemoryU16(RESET_LOCATION, 0x8000)
    }

    private fun run() {
        repeat(10000) {
            val opcode = readMemoryU8(pc)
            pc++
            INSTRUCTION_MAP[opcode]?.let { instruction ->
                when (instruction.mnemonic) {
                    "LDA" -> lda(instruction.mode)
                    "STA" -> sta(instruction.mode)
                    "TAX" -> tax()
                    "INX" -> inx()
                    "BRK" -> return
                    else -> error("Not implemented")
                }
                pc += instruction.length - 1
            } ?: error("Unknown opcode $opcode")
        }

    }

    private fun lda(mode: AddressingMode) {
        val addr = getOperandAddress(mode)
        val value = readMemoryU8(addr)
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

    private fun sta(mode: AddressingMode) {
        val addr = getOperandAddress(mode)
        writeMemoryU8(addr, a)
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

data class Instruction(
    val opcode: u8,
    val mnemonic: String,
    val length: Int,
    val cycleCount: Int,
    val mode: AddressingMode
)

private val INSTRUCTIONS = listOf(
    Instruction(0x00, "BRK", 1, 7, AddressingMode.NoneAddressing),
    Instruction(0xAA, "TAX", 1, 2, AddressingMode.NoneAddressing),
    Instruction(0xE8, "INX", 1, 2, AddressingMode.NoneAddressing),

    Instruction(0xA9, "LDA", 2, 2, AddressingMode.Immediate),
    Instruction(0xA5, "LDA", 2, 3, AddressingMode.ZeroPage),
    Instruction(0xB5, "LDA", 2, 4, AddressingMode.ZeroPageX),
    Instruction(0xAD, "LDA", 3, 4, AddressingMode.Absolute),
    Instruction(0xBD, "LDA", 3, 4, AddressingMode.AbsoluteX),
    Instruction(0xB9, "LDA", 3, 4, AddressingMode.AbsoluteY),
    Instruction(0xB9, "LDA", 3, 4, AddressingMode.AbsoluteY),
    Instruction(0xA1, "LDA", 2, 6, AddressingMode.IndirectX),
    Instruction(0xB1, "LDA", 2, 5, AddressingMode.IndirectY),

    Instruction(0x85, "STA", 2, 3, AddressingMode.ZeroPage),
    Instruction(0x95, "STA", 2, 4, AddressingMode.ZeroPageX),
    Instruction(0x8D, "STA", 3, 4, AddressingMode.Absolute),
    Instruction(0x9D, "STA", 3, 5, AddressingMode.AbsoluteX),
    Instruction(0x99, "STA", 3, 5, AddressingMode.AbsoluteY),
    Instruction(0x81, "STA", 2, 6, AddressingMode.IndirectX),
    Instruction(0x91, "STA", 2, 6, AddressingMode.IndirectY),
)
private val INSTRUCTION_MAP = INSTRUCTIONS.associateBy { it.opcode }