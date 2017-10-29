package com.github.fgoncalves.identicon

import android.graphics.Color
import com.github.fgoncalves.identicon.lib.*
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

    @Test
    fun `get colour should take colour from the first 3 bytes as rgb`() {
        val identicon = IdenticonData(bitmapPoints = listOf(
                IdenticonBitmapPoint(value = 0),
                IdenticonBitmapPoint(value = 0xFF.toByte()),
                IdenticonBitmapPoint(value = 0xEE.toByte())))

        val result = GetColourFunction.apply(identicon)

        result.color `should equal to` Color.rgb(0, 0xFF.toByte().toInt(), 0xEE.toByte().toInt())
    }

    @Test
    fun `mirror function should repeat the same values after the vertical axis`() {
        val identicon = IdenticonData(bitmapPoints = listOf(
                IdenticonBitmapPoint(value = 0),
                IdenticonBitmapPoint(value = 0xFF.toByte()),
                IdenticonBitmapPoint(value = 0xEE.toByte())))

        val result = MirrorFunction.apply(identicon)

        result.bitmapPoints[0].value `should equal to` 0
        result.bitmapPoints[1].value `should equal to` 0xFF.toByte()
        result.bitmapPoints[2].value `should equal to` 0xEE.toByte()
        result.bitmapPoints[3].value `should equal to` 0xFF.toByte()
        result.bitmapPoints[4].value `should equal to` 0
    }

    @Test
    fun `with indexes function should put indexes into the points`() {
        val identicon = IdenticonData(bitmapPoints = listOf(
                IdenticonBitmapPoint(value = 0),
                IdenticonBitmapPoint(value = 0xFF.toByte()),
                IdenticonBitmapPoint(value = 0xEE.toByte())))

        val result = WithIndexesFunction.apply(identicon)

        result.bitmapPoints[0].value `should equal to` 0
        result.bitmapPoints[1].index `should equal to` 1
        result.bitmapPoints[2].index `should equal to` 2
    }

    @Test
    fun `filter function should filter out odd values`() {
        val identicon = IdenticonData(bitmapPoints = listOf(
                IdenticonBitmapPoint(value = 0),
                IdenticonBitmapPoint(value = 0xFF.toByte()),
                IdenticonBitmapPoint(value = 0xEE.toByte())))

        val result = FilterEvenValuesFunction.apply(identicon)

        result.bitmapPoints.size `should equal to` 2
        result.bitmapPoints[0].value `should equal to` 0
        result.bitmapPoints[1].value `should equal to` 0xEE.toByte()
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
