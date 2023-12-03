import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.*

class CPUTest {
    @Test
    fun testLdaImmediateLoadData() {
        val cpu = CPU()
        cpu.loadAndRun(listOf(0xa9, 0x05, 0x00))
        assertThat(cpu.a).isEqualTo(0x05)
        assertThat(cpu.status.and(0b0000_0010)).isEqualTo(0)
        assertThat(cpu.status.and(0b1000_0000)).isEqualTo(0)
    }

    @Test
    fun testLdaZeroFlag() {
        val cpu = CPU()
        cpu.loadAndRun(listOf(0xa9, 0x00, 0x00))
        assertThat(cpu.status.and(0b0000_0010)).isEqualTo(0b10)
    }

    @Test
    fun testFiveOpsWorkingTogether() {
        val cpu = CPU()
        cpu.loadAndRun(listOf(0xa9, 0xc0, 0xaa, 0xe8, 0x00))
        assertThat(cpu.x).isEqualTo(0xc1)
    }
}