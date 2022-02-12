import me.reckter.allRGB.BasicGeneration
import me.tongfei.progressbar.ProgressBar
import java.awt.image.BufferedImage
import java.awt.image.DataBuffer
import java.io.File
import javax.imageio.ImageIO

/**
 * Created with IntelliJ IDEA.
 * User: reckter
 * Date: 7/31/13
 * Time: 12:32 AM
 * To change this template use File | Settings | File Templates.
 */
open class BasicPicture(file: String) : BasicGeneration() {
    protected lateinit var pixelShould: Array<Array<ByteArray>>
    protected var height = 0
    protected var width = 0

    init {
        readImage(file)
    }

    private fun readImage(file: String) {
        Log.info("reading $file")
        var img: BufferedImage = ImageIO.read(File(file))
        height = img.height
        width = img.width
        pixelShould = Array(height) { Array(width) { ByteArray(3) } }
        val result = IntArray(height * width * 3 + 1)

        // img.getData().getPixels(0, 0, height, width, result);
        val buffer: DataBuffer = img.getData().getDataBuffer()
        var k = 0
        (0 until height * width)
            .let{
                ProgressBar.wrap(it, "set up")
            }
            .forEach { i ->
                pixelShould[i % height][(i.toFloat() / height.toFloat()).toInt()][R] =
                    buffer.getElem(3 * i + R).toByte()
                pixelShould[i % height][(i.toFloat() / height.toFloat()).toInt()][G] =
                    buffer.getElem(3 * i + G).toByte()
                pixelShould[i % height][(i.toFloat() / height.toFloat()).toInt()][B] =
                    buffer.getElem(3 * i + B).toByte()
                //Log.debug("reading pixel: (" + i % height + "|" + (int) ((float) i / (float) height) + "): [" + buffer.getElem(3 * i + R) + "|" + buffer.getElem(3 * i + G) + "|" + buffer.getElem(3 * i + B) + "]");
            }
        Log.info("finished.")
    }

    override fun render() {
        for (x in 0 until SIZE) {
            for (y in 0 until SIZE) {
                pixel[x][y][R] = pixelShould[x][y][R]
                pixel[x][y][G] = pixelShould[x][y][G]
                pixel[x][y][B] = pixelShould[x][y][B]
            }
        }
    }
}
