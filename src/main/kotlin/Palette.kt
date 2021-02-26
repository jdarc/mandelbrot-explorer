import java.awt.Color
import kotlin.math.ln

class Palette(vararg markers: Int = intArrayOf(0x000000, 0xFFFFFF)) {

    private val argb = generate(markers)

    operator fun get(index: Int) = argb[index and 255].rgb

    operator fun get(index: Int, x: Double, y: Double): Int {
        val n = index + 1.0 - ln(ln(x * x + y * y) * overLn2) * overLn2
        val rn = n.toInt()
        val d = n - rn
        val color1 = argb[255 and rn]
        val color2 = argb[255 and rn + 1]
        val blu = (color1.blue + (color2.blue - color1.blue) * d).toInt().coerceIn(0, 255)
        val grn = (color1.green + (color2.green - color1.green) * d).toInt().coerceIn(0, 255)
        val red = (color1.red + (color2.red - color1.red) * d).toInt().coerceIn(0, 255)
        return blu or (grn shl 8) or red.shl(16)
    }

    private companion object {
        val overLn2 = 1.0 / ln(2.0)

        fun generate(markers: IntArray): Array<Color> {
            val colors = arrayOfNulls<Color>(256)
            var eR = 255 and markers[0].shr(0x10)
            var eG = 255 and markers[0].shr(0x08)
            var eB = 255 and markers[0]
            val step = 256.0 / (markers.size - 1.0)
            for (j in 1 until markers.size) {
                var sR = eR.toDouble()
                var sG = eG.toDouble()
                var sB = eB.toDouble()
                eR = 255 and markers[j].shr(0x10)
                eG = 255 and markers[j].shr(0x08)
                eB = 255 and markers[j]
                val dr = (eR - sR) / step
                val dg = (eG - sG) / step
                val db = (eB - sB) / step
                var i = 0
                while (i < step) {
                    val red = sR.coerceIn(0.0, 255.0).toInt()
                    val grn = sG.coerceIn(0.0, 255.0).toInt()
                    val blu = sB.coerceIn(0.0, 255.0).toInt()
                    colors[((j - 1) * step + i).toInt()] = Color(red, grn, blu)
                    sR += dr
                    sG += dg
                    sB += db
                    ++i
                }
            }
            return colors.requireNoNulls()
        }
    }
}
