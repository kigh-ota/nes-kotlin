fun main(args: Array<String>) {
    println("Hello")
}

typealias Int8 = Int
typealias Int16 = Int

class CPU {
    private var pc: Int16 = 0
    var a: Int8 = 0
    var x: Int8 = 0
    var status: Int8 = 0

    fun interpret(program: List<Int8>) {
        pc = 0
        repeat(10000) {
            val opcode = program[pc]
            pc++
            when (opcode) {
                0xA9 -> {   // LDA
                    val param = program[pc]
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

    private fun lda(value: Int8) {
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

    private fun updateZeroAndNegativeFlags(result: Int8) {
        status = if (result == 0) {
            status.or(0b0000_0010)
        } else {
            status.and(0b1111_1101)
        }
        status = if (result.and(0b1000_0000) != 0) {
            status.or(0b1000_0000)
        } else {
            status.and(0x0111_1111)
        }
    }
}
