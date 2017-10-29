package com.github.fgoncalves.identicon

import org.amshove.kluent.`should equal to`
import org.junit.Test

@Suppress("IllegalIdentifier")
class IdenticonTest {
    @Test
    fun `md5 function should create the md5 hash for the given input`() {
        val result = Md5Function.apply("foobar")

        result.bitmapPoints.toHex() `should equal to` "3858F62230AC3C915F300C664312C63F"
    }

    @Test
    fun `drop last byte should drop the last byte for a given byte array`() {
        val identicon = IdenticonData(bitmapPoints = listOf(
                IdenticonBitmapPoint(value = 0),
                IdenticonBitmapPoint(value = 0xFF.toByte())))

        val result = DropLastByteFunction.apply(identicon)

        result.bitmapPoints.toHex() `should equal to` "00"
    }

    private val HEX_CHARS = "0123456789ABCDEF"

    private fun List<IdenticonBitmapPoint>.toHex(): String {
        val result = StringBuffer()

        forEach {
            val octet = it.value.toInt()
            val firstIndex = (octet and 0xF0).ushr(4)
            val secondIndex = octet and 0x0F
            result.append(HEX_CHARS[firstIndex])
            result.append(HEX_CHARS[secondIndex])
        }

        return result.toString()
    }
}
