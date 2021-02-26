import java.awt.Rectangle
import java.util.concurrent.RecursiveAction
import kotlin.math.floor
import kotlin.math.min

class Generator(
    private val configuration: Configuration,
    private val bitmap: Bitmap,
    private val bounds: Rectangle,
    private var alive: Array<Boolean> = arrayOf(true),
    private val repaint: () -> Unit
) : RecursiveAction() {
    private val zoom = configuration.point.z / min(bitmap.width, bitmap.height)

    fun kill() {
        alive[0] = false
    }

    override fun compute() {
        if (!alive[0]) return

        if (bounds.width < bitmap.width && edgeFill()) {
            bitmap.fill(Rectangle(bounds.x + 1, bounds.y + 1, bounds.width - 2, bounds.height - 2), bitmap[bounds.x, bounds.y])
            repaint()
        } else {
            if (!alive[0]) return
            if (bounds.width <= 24) {
                for (y in bounds.y + 1 until bounds.y + bounds.height - 1) {
                    for (x in bounds.x + 1 until bounds.x + bounds.width - 1) {
                        bitmap[x, y] = calculate(x, y)
                    }
                }
            } else {
                val hw = floor(bounds.width / 2.0).toInt()
                val hh = floor(bounds.height / 2.0).toInt()
                val rect1 = Rectangle(bounds.x, bounds.y, hw, hh)
                val rect2 = Rectangle(bounds.x, bounds.y + hh, hw, bounds.height - hh)
                val rect3 = Rectangle(bounds.x + hw, bounds.y + hh, bounds.width - hw, bounds.height - hh)
                val rect4 = Rectangle(bounds.x + hw, bounds.y, bounds.width - hw, hh)
                invokeAll(
                    Generator(configuration, bitmap, rect1, alive, repaint),
                    Generator(configuration, bitmap, rect2, alive, repaint),
                    Generator(configuration, bitmap, rect3, alive, repaint),
                    Generator(configuration, bitmap, rect4, alive, repaint)
                )
            }
            repaint()
        }
    }

    private fun edgeFill(): Boolean {
        for (x in bounds.x until bounds.x + bounds.width) {
            bitmap[x, bounds.y] = calculate(x, bounds.y)
            bitmap[x, bounds.y + bounds.height - 1] = calculate(x, bounds.y + bounds.height - 1)
        }

        for (y in bounds.y + 1 until bounds.y + bounds.height - 1) {
            bitmap[bounds.x, y] = calculate(bounds.x, y)
            bitmap[bounds.x + bounds.width - 1, y] = calculate(bounds.x + bounds.width - 1, y)
        }

        val c = bitmap[bounds.x, bounds.y]

        for (x in bounds.x + 1 until bounds.x + bounds.width) {
            if (bitmap[x, bounds.y] != c || bitmap[x, bounds.y + bounds.height - 1] != c) return false
        }

        for (y in bounds.y + 1 until bounds.y + bounds.height) {
            if (bitmap[bounds.x, y] != c || bitmap[bounds.x + bounds.width - 1, y] != c) return false
        }

        return true
    }

    private fun calculate(x: Int, y: Int): Int {
        val a = 2.5 * (configuration.point.x + x * zoom) - 2.0
        val b = 1.25 - 2.5 * (configuration.point.y + y * zoom)
        val c1 = blend(escape(a - zoom, b), escape(a + zoom, b))
        val c2 = blend(escape(a, b - zoom), escape(a, b + zoom))
        return blend(escape(a, b), blend(c1, c2))
    }

    private fun escape(a: Double, b: Double, iterations: Int = configuration.iterations): Int {
        var x = 0.0
        var y = 0.0
        var z = 0.0
        var w = 0.0
        var i = 0
        while (++i < iterations && z + w < 4.0) {
            x = 2.0 * x * y + b
            y = w - z + a
            z = x * x
            w = y * y
        }
        return if (i < iterations) configuration.palette[i, x, y] else 0
    }

    private companion object {
        private fun blend(c1: Int, c2: Int): Int {
            val red = (c1 shr 0x10 and 255) + (c2 shr 0x10 and 255) shr 1
            val grn = (c1 shr 0x08 and 255) + (c2 shr 0x08 and 255) shr 1
            val blu = (c1 and 255) + (c2 and 255) shr 1
            return red shl 0x10 or (grn shl 0x08) or blu
        }
    }
}
