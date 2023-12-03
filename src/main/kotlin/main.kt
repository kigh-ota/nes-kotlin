fun main(args: Array<String>) {
    println("Hello")
}

typealias Int8 = Int
typealias Int16 = Int

class CPU {
    private var pc: Int16 = 0
    private var a: Int8 = 0
    fun getA() = a
    private var status: Int8 = 0
    fun getStatus() = status

    fun interpret(program: List<Int8>) {
        pc = 0
        repeat(10000) {
            val opcode = program[pc]
            pc++
            when (opcode) {
                0xA9 -> {
                    val param = program[pc]
                    pc++
                    a = param

                    status = if (a == 0) {
                        status.or(0b0000_0010)
                    } else {
                        status.and(0b1111_1101)
                    }

                    status = if (a.and(0b1000_0000) != 0) {
                        status.or(0b1000_0000)
                    } else {
                        status.and(0b0111_1111)
                    }
                }
                0xAA -> {

                }
                0x00 -> {
                    return
                }
                else -> error("Not implemented")
            }
        }
    }
}
