import java.util.Queue
import java.util.concurrent.ArrayBlockingQueue
import java.util.stream.Stream
import kotlin.math.abs

/**
 * Created with IntelliJ IDEA.
 * User: reckter
 * Date: 7/31/13
 * Time: 12:48 AM
 * To change this template use File | Settings | File Templates.
 */
open class PictureRandomSort(file: String) : BasicPicture(file) {

    override fun render() {
        randomizePixel()
        Log.info("repainting image")
        randomSwapPixels(
            SIZE,
            SIZE * SIZE * 2
        )
    }

    fun randomSwapPixels(maxIter: Int, maxStream: Int) {
        val pixelToSwitch: Queue<IntArray> =
            ArrayBlockingQueue(SIZE * SIZE * 3)
        Thread {
            while (true) {
                if (!pixelToSwitch.isEmpty()) {
                    val point =
                        pixelToSwitch.remove()
                    switchPixel(
                        point[0],
                        point[1],
                        point[2],
                        point[3]
                    )
                }
            }
        }.start()

        val startTime = System.currentTimeMillis()
        for (i in 0 until maxIter) {
            while (isSaving) {
                Thread.yield()
            }
            val percent = i.toFloat() / maxIter.toFloat() * 100f
            Log.info("[" + i + "/" + maxIter + "]" + percent + "%(ETA: " + (System.currentTimeMillis() - startTime).toFloat() / percent * (100f - percent) / 1000f + "s) : " + calculateFittnes())
            Stream.iterate(0) { n: Int -> n + 1 }
                .limit(maxStream.toLong())
                .parallel()
                .map { ignored: Int? ->
                    val x1: Int =
                        random.nextInt(SIZE)
                    val y1: Int =
                        random.nextInt(SIZE)
                    val x2: Int =
                        random.nextInt(SIZE)
                    val y2: Int =
                        random.nextInt(SIZE)
                    val ret = intArrayOf(x1, y1, x2, y2)
                    ret
                }.forEach { point: IntArray ->
                    if (isChangedBetter(point[0], point[1], point[2], point[3])) {
                        pixelToSwitch.add(point)
                    }
                }
        }
    }

    protected fun switchPixel(x1: Int, y1: Int, x2: Int, y2: Int) {
        while (isSaving) {
            Thread.yield()
        }
        if (x1 == x2 && y1 == y2) {
            return
        }
        val tmpR = pixel[x1][y1][R]
        val tmpG = pixel[x1][y1][G]
        val tmpB = pixel[x1][y1][B]
        pixel[x1][y1][R] = pixel[x2][y2][R]
        pixel[x1][y1][G] = pixel[x2][y2][G]
        pixel[x1][y1][B] = pixel[x2][y2][B]
        pixel[x2][y2][R] = tmpR
        pixel[x2][y2][G] = tmpG
        pixel[x2][y2][B] = tmpB
    }

    protected fun calculateFittnes(): Long {
        var fittnes: Long = 0
        for (x in 0 until SIZE) {
            for (y in 0 until SIZE) {
                fittnes += getDifference(x, y, x, y).toLong()
            }
        }
        return fittnes
    }

    /**
     * randomizes the pixel. Takes care of allRGB things
     */
    protected fun randomizePixel() {
        Log.info("randomizing Pixel")
        var r = 0
        var g = 0
        var b = 0
        for (x in 0 until SIZE) {
            for (y in 0 until SIZE) {
                pixel[x][y][R] = r.toByte()
                pixel[x][y][G] = g.toByte()
                pixel[x][y][B] = b.toByte()
                r += 4096 / SIZE
                if (r >= 256) {
                    r = 0
                    g += 4096 / SIZE
                    if (g >= 256) {
                        g = 0
                        b += 4096 / SIZE
                    }
                }
            }
        }
        for (x in SIZE - 1 downTo 0) {
            for (y in SIZE - 1 downTo 0) {
                val x2: Int = random.nextInt(x + 1)
                var y2: Int =
                    random.nextInt(SIZE)
                if (x2 == x) {
                    y2 = random.nextInt(y + 1)
                }
                val tmpR = pixel[x][y][R]
                val tmpG = pixel[x][y][G]
                val tmpB = pixel[x][y][B]
                pixel[x][y][R] =
                    pixel[x2][y2][R]
                pixel[x][y][G] =
                    pixel[x2][y2][G]
                pixel[x][y][B] =
                    pixel[x2][y2][B]
                pixel[x2][y2][R] = tmpR
                pixel[x2][y2][G] = tmpG
                pixel[x2][y2][B] = tmpB
            }
        }
        Log.info("finished.")
    }

    /**
     * Compares if changing is better and returns true if so
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    protected fun isChangedBetter(x1: Int, y1: Int, x2: Int, y2: Int): Boolean {
        return getDifference(x1, y1, x2, y2) + getDifference(x2, y2, x1, y1) < getDifference(
            x1,
            y1,
            x1,
            y1
        ) + getDifference(x2, y2, x2, y2)
    }

    fun getDifference(x1: Int, y1: Int, x2: Int, y2: Int): Int {
        val average1 = getAverageWithSwitched(x1, y1, x2, y2)
        val dif = arrayOf(
            average1[R] - pixelShould[allign(x2) / SCALE][allign(y2) / SCALE][R],
            average1[G] - pixelShould[allign(x2) / SCALE][allign(y2) / SCALE][G],
            average1[B] - pixelShould[allign(x2) / SCALE][allign(y2) / SCALE][B],
        )

        return abs(dif[R]) + abs(dif[G]) + abs(dif[B])
    }

    protected fun allign(i: Int): Int {
        var i = i
        while (i % SCALE != 0) {
            i--
        }
        return i
    }

    fun getAverageWithSwitched(x1: Int, y1: Int, x2: Int, y2: Int): Array<Int> {
        val pixels = (-1..1).flatMap { yDiff ->
            (-1..1).map { xDiff ->
                if (xDiff == 0 && yDiff == 0) x1 to y1 else (x2 + xDiff) to (y2 + yDiff)
            }
        }
            .filter { (x,y) -> x in 0 until SIZE && y in 0 until SIZE}
            .map { (x, y) -> pixel[x][y] }

        val sum = pixels
            .fold(arrayOf(0, 0, 0)) { cur, acc ->
                arrayOf(
                    cur[R] + acc[R],
                    cur[G] + acc[G],
                    cur[B] + acc[B],
                )
            }

        return arrayOf(sum[R] / sum.size, sum[G] / sum.size, sum[B] / sum.size)
    }

    /**
     * returns the diffrence between pixel[x1][y1] and pixelshould[x2][y2]
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    protected fun getSwitchedDifferences(x1: Int, y1: Int, x2: Int, y2: Int): Int {
        val difr: Int = pixel[x1][y1][R] - pixelShould[allign(x2) / SCALE][allign(y2) / SCALE][R]
        val difg: Int = pixel[x1][y1][G] - pixelShould[allign(x2) / SCALE][allign(y2) / SCALE][G]
        val difb: Int = pixel[x1][y1][B] - pixelShould[allign(x2) / SCALE][allign(y2) / SCALE][B]
        return abs(difr) * abs(difg) * abs(difb)
    }

    companion object {
        protected const val SWITCH_PIXEL_BARRIER = 0
        const val SCALE = 1
    }
}
