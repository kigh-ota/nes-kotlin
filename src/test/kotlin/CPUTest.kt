import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.*

class CPUTest {
    @Test
    fun testLdaImmediateLoadData() {
        val cpu = CPU()
        cpu.interpret(listOf(0xa9, 0x05, 0x00))
        assertThat(cpu.getA()).isEqualTo(0x05)
        assertThat(cpu.getStatus().and(0b0000_0010)).isEqualTo(0)
        assertThat(cpu.getStatus().and(0b1000_0000)).isEqualTo(0)
    }

    @Test
    fun testLdaZeroFlag() {
        val cpu = CPU()
        cpu.interpret(listOf(0xa9, 0x00, 0x00))
        assertThat(cpu.getStatus().and(0b0000_0010)).isEqualTo(0b10)
    }
}