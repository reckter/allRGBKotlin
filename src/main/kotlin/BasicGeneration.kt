package me.reckter.allRGB

import javafx.scene.image.Image
import javafx.scene.layout.Background
import javafx.scene.layout.Pane
import java.awt.Color
import java.awt.Graphics
import java.awt.image.BufferedImage
import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter
import java.util.Random
import java.util.Scanner
import java.util.logging.Logger
import javax.imageio.ImageIO
import javax.swing.JFrame

/**
 * Created with IntelliJ IDEA.
 * To change this template use File | Settings | File Templates.
 */
abstract class BasicGeneration {
    var showThread: Thread
    private val bi: BufferedImage = BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB)
    private val graphics = bi.graphics
    protected var pixel: Array<Array<ByteArray>>
    var isSaving = false
    val Log = Logger.getLogger("")

    init {
        Log.info("allocationg RAM...")
        pixel = Array(SIZE) { Array(SIZE) { ByteArray(3) } }
        Log.info("done")
        showThread = Thread(ShowThread())
        showThread.start()
        Log.info("init from BasicGeneration done.")
    }

    abstract fun render()
    fun writePicture() {
        isSaving = true
        Log.info("checking image")
        val colors = Array(COLORS) { Array(COLORS) { IntArray(COLORS) } }
        for (x in 0 until SIZE) {
            for (y in 0 until SIZE) {
                colors[byteToColor(pixel[y][x][R])][byteToColor(pixel[y][x][G])][byteToColor(
                    pixel[y][x][B]
                )]++
            }
        }
        var errors = 0
        val errorMessage: StringBuilder = StringBuilder("")
        for (r in 0 until COLORS) {
            for (g in 0 until COLORS) {
                for (b in 0 until COLORS) {
                    if (colors[r][g][b] != 1) {
                        errors++
                        //errorMessage.append("color (").append(r).append(", ").append(g).append( ", ").append(b).append(") exists ").append(colors[r][g][b]).append(" times.\n");
                    }
                }
            }
        }
        if (errors == 0) {
            Log.info("picture validated.")
        } else {
            Log.info("picture faied: $errors errors")
            Log.info(errorMessage.toString())
        }
        var r: Int
        var g: Int
        var b: Int
        Log.info("painting Image")
        for (x in 0 until SIZE) {
            for (y in 0 until SIZE) {
                r = pixel[y][x][R].toInt()
                g = pixel[y][x][G].toInt()
                b = pixel[y][x][B].toInt()
                r = if (r >= 0) r else 127 - r
                g = if (g >= 0) g else 127 - g
                b = if (b >= 0) b else 127 - b
                graphics.setColor(Color(r, g, b))
                graphics.drawLine(x, y, x, y)
            }
        }
        var br: BufferedReader? = null
        br = BufferedReader(FileReader("pictures.txt"))
        val pictureNumberFile = File("pictures.txt")
        val nextPictureNumber = (pictureNumberFile.readLines().firstOrNull()?.toInt() ?: 0) + 1
        pictureNumberFile.writeText(nextPictureNumber.toString())


        Log.info("saving Image '$nextPictureNumber.png'")
        ImageIO.write(bi, "PNG", File("pictures/$nextPictureNumber.png"))
        Log.info("finished.")
        isSaving = false
    }

    private fun byteToColor(b: Byte): Int {
        return if (b >= 0) b.toInt() else 127 - b
    }

    fun save() {
        writePicture()
    }

    inner class ShowThread : Pane(), Runnable {
        override fun run() {
            Log.info("started ShowThread")
            val frame = JFrame("allRGB")
            // frame.setContentPane(new SwingTemplateJPanel());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
            frame.pack() // "this" JFrame packs its components
            // frame.setLocationRelativeTo(null); // center the application window
            frame.setSize(CANVAS_SIZE, CANVAS_SIZE)
            frame.setVisible(true)
            paintComponent(frame.getGraphics())
        }

        fun paintComponent(graphics: Graphics) {
            setBackground(Background.EMPTY)
            var r: Int
            var g: Int
            var b: Int
            var rTMP: Int
            var gTMP: Int
            var bTMP: Int
            while (true) {
                try {
                    Thread.sleep(0)
                } catch (e: InterruptedException) {
                    e.printStackTrace() //To change body of catch statement use File | Settings | File Templates.
                }
                for (x in 0 until CANVAS_SIZE) {
                    for (y in 0 until CANVAS_SIZE) {
                        r = 0
                        g = 0
                        b = 0
                        for (xi in 0 until FACTOR) {
                            for (yi in 0 until FACTOR) {
                                rTMP = pixel[x * FACTOR + xi][y * FACTOR + yi][R].toInt()
                                gTMP = pixel[x * FACTOR + xi][y * FACTOR + yi][G].toInt()
                                bTMP = pixel[x * FACTOR + xi][y * FACTOR + yi][B].toInt()
                                rTMP = if (rTMP >= 0) rTMP else 127 - rTMP
                                gTMP = if (gTMP >= 0) gTMP else 127 - gTMP
                                bTMP = if (bTMP >= 0) bTMP else 127 - bTMP
                                r += rTMP
                                g += gTMP
                                b += bTMP
                            }
                        }
                        r /= FACTOR * FACTOR
                        g /= FACTOR * FACTOR
                        b /= FACTOR * FACTOR
                        if (r < 256 && r >= 0 && g < 256 && g >= 0 && b < 256 && b >= 0) {
                            graphics.setColor(Color(r, g, b))
                            graphics.drawLine(x, y, x, y)
                        } else {
                            Log.info("The pixel ($x|$y) is wrong: $r,$g,$b")
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val SIZE = 4096
        const val CANVAS_SIZE = 1024
        const val FACTOR = SIZE / CANVAS_SIZE
        const val COLORS = 256
        const val R = 0
        const val G = 1
        const val B = 2
        const val X = 0
        const val Y = 1
        var random: Random = Random()
    }
}
