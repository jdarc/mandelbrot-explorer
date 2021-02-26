import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.Transparency
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt

class Bitmap(val width: Int, val height: Int) {
    private val bounds = Rectangle(width, height)
    private val image = BufferedImage(width, height, Transparency.OPAQUE)
    private val pixels = (image.raster.dataBuffer as DataBufferInt).data

    operator fun get(x: Int, y: Int) = pixels[y * width + x]

    operator fun set(x: Int, y: Int, rgb: Int) {
        pixels[y * width + x] = rgb
    }

    fun fill(rect: Rectangle, rgb: Int) {
        val safe = rect.intersection(bounds)
        for (y in safe.y until safe.y + safe.height) {
            for (x in safe.x until safe.x + safe.width) {
                this[x, y] = rgb
            }
        }
    }

    fun draw(g: Graphics2D) = g.drawImage(image, null, 0, 0)
}
