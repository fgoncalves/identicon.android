package com.github.fgoncalves.identicon.lib

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import io.reactivex.Single
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import java.nio.charset.Charset
import java.security.MessageDigest

internal data class IdenticonBitmapPoint(
        val value: Byte = 0x00.toByte(),
        val index: Int = 0,
        val leftTopX: Float = 0f,
        val leftTopY: Float = 0f,
        val rightBottomX: Float = 0f,
        val rightBottomY: Float = 0f)

internal data class IdenticonData(
        val color: Int = 0,
        val bitmapPoints: List<IdenticonBitmapPoint> = emptyList()
)

/**
 * Entry point to get an Identicon for a string
 */
interface Identicon {

    /**
     * Generate an identicon for the given input text. Generate operates by default on the
     * computation scheduler
     */
    fun generate(text: String): Single<Bitmap>
}


class IdenticonImpl : Identicon {
    override fun generate(text: String): Single<Bitmap> =
            Single.fromCallable { text }
                    .map(Md5Function)
                    .map(DropLastByteFunction)
                    .map(GetColourFunction)
                    .map(MirrorFunction)
                    .map(WithIndexesFunction)
                    .map(FilterEvenValuesFunction)
                    .map(BitmapCoordinateCalculatorFunction())
                    .map(CreateBitmapFunction())
                    .subscribeOn(Schedulers.computation())
}

internal object Md5Function : Function<String, IdenticonData> {
    override fun apply(input: String): IdenticonData =
            IdenticonData(
                    bitmapPoints = MessageDigest.getInstance("MD5")
                            .digest(input.toByteArray(Charset.forName("UTF8"))).map {
                        IdenticonBitmapPoint(value = it)
                    })
}

internal object DropLastByteFunction : Function<IdenticonData, IdenticonData> {
    override fun apply(input: IdenticonData): IdenticonData = input.copy(bitmapPoints = input.bitmapPoints.dropLast(1))
}

internal object GetColourFunction : Function<IdenticonData, IdenticonData> {
    override fun apply(input: IdenticonData): IdenticonData =
            input.copy(color = Color.rgb(
                    input.bitmapPoints[0].value.toInt(),
                    input.bitmapPoints[1].value.toInt(),
                    input.bitmapPoints[2].value.toInt()))
}

internal object MirrorFunction : Function<IdenticonData, IdenticonData> {
    override fun apply(input: IdenticonData): IdenticonData =
            input.copy(bitmapPoints = input.bitmapPoints.chunk(3)
                    .map {
                        listOf(it[0], it[1], it[2], it[1], it[0])
                    }.flatten())

    private fun <T> List<T>.chunk(chunkSize: Int): List<List<T>> = asSequence().batch(chunkSize).toList()

    private fun <T> Sequence<T>.batch(n: Int): Sequence<List<T>> = BatchingSequence(this, n)

    private class BatchingSequence<T>(val source: Sequence<T>, val batchSize: Int) : Sequence<List<T>> {
        override fun iterator(): Iterator<List<T>> = object : AbstractIterator<List<T>>() {
            val iterate = if (batchSize > 0) source.iterator() else emptyList<T>().iterator()
            override fun computeNext() {
                if (iterate.hasNext()) setNext(iterate.asSequence().take(batchSize).toList())
                else done()
            }
        }
    }
}

internal object WithIndexesFunction : Function<IdenticonData, IdenticonData> {
    override fun apply(input: IdenticonData): IdenticonData =
            input.copy(bitmapPoints = input.bitmapPoints.withIndex().map {
                it.value.copy(index = it.index)
            })
}

internal object FilterEvenValuesFunction : Function<IdenticonData, IdenticonData> {
    override fun apply(input: IdenticonData): IdenticonData =
            input.copy(bitmapPoints = input.bitmapPoints.filter { it.value.toInt() % 2 == 0 })
}

internal class BitmapCoordinateCalculatorFunction(val bitmapSizePx: Int = 250) : Function<IdenticonData, IdenticonData> {
    override fun apply(input: IdenticonData): IdenticonData =
            input.copy(bitmapPoints = input.bitmapPoints.map {
                it.copy(
                        leftTopX = leftTopX(it.index),
                        leftTopY = leftTopY(it.index),
                        rightBottomX = rightBottomX(it.index),
                        rightBottomY = rightBottomY(it.index))
            })

    private fun leftTopX(index: Int): Float = bitmapSizePx / 5f * (index % 5)

    private fun leftTopY(index: Int): Float = bitmapSizePx / 5f * (index / 5)

    private fun rightBottomX(index: Int): Float = leftTopX(index) + bitmapSizePx / 5

    private fun rightBottomY(index: Int): Float = leftTopY(index) + bitmapSizePx / 5
}

internal class CreateBitmapFunction(val bitmapSizePx: Int = 250) : Function<IdenticonData, Bitmap> {
    override fun apply(input: IdenticonData): Bitmap {
        val bitmap = Bitmap.createBitmap(bitmapSizePx, bitmapSizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint()
        paint.color = input.color
        paint.style = Paint.Style.FILL

        input.bitmapPoints.forEach {
            canvas.drawRect(it.leftTopX, it.leftTopY, it.rightBottomX, it.rightBottomY, paint)
        }

        return bitmap
    }
}
