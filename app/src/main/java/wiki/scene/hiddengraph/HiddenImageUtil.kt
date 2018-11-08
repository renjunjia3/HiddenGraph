package wiki.scene.hiddengraph

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Environment
import com.blankj.utilcode.util.ImageUtils
import java.io.File

object HiddenImageUtil {
    fun calculateHiddenImage(blackImagePath: String, whiteImagePath: String): String {
        val blackImage = BitmapFactory.decodeFile(blackImagePath)
        val whiteImage = BitmapFactory.decodeFile(whiteImagePath)
        val b_width = blackImage.width
        val b_height = blackImage.height
        val w_width = whiteImage.width
        val w_height = whiteImage.height
        //设定最终图片的尺寸
        val f_width = Math.max(b_width, w_width)
        val f_height = Math.max(b_height, w_height)
        val result = Bitmap.createBitmap(f_width, f_height, Bitmap.Config.ARGB_8888)
        //黑色图片距离边缘的距离
        val b_widthOffset = if (b_width == f_width) 0 else (f_width - b_width) / 2
        val b_heightOffset = if (b_height == f_height) 0 else (f_height - b_height) / 2
        //白色图片离边缘距离
        val w_widthOffset = if (w_width == f_width) 0 else (f_width - w_width) / 2
        val w_heightOffset = if (w_height == f_height) 0 else (f_height - w_height) / 2
        for (x in 0..f_width) {
            for (y in 0..f_height) {
                //上下左右交叉排列黑白像素
                val blackPixel = (x + y) % 2 == 0
                var coor_x: Int
                var coor_y: Int
                //决定当前像素位置是否对应图一或图二某像素，如果没有，跳过循环
                var validPixel = true
                if (blackPixel) {
                    coor_x = x - b_widthOffset
                    if (coor_x > b_width - 1) validPixel = false
                    coor_y = y - b_heightOffset
                    if (coor_y > b_height - 1) validPixel = false
                } else {
                    coor_x = x - w_widthOffset
                    if (coor_x > w_width - 1) validPixel = false
                    coor_y = y - w_heightOffset
                    if (coor_y > w_height - 1) validPixel = false
                }
                validPixel = validPixel && coor_x >= 0 && coor_y >= 0
                if (!validPixel) {
                    continue
                }
                //根据颜色计算像素灰度，设定透明度
                if (blackPixel) {
                    val origin = blackImage.getPixel(coor_x, coor_y)
                    val gray =
                        Color.red(origin) * 19595 + Color.green(origin) * 38469 + Color.blue(origin) * 7472 shr 16
                    val finalColor = Color.argb(255 - gray, 0, 0, 0)
                    result.setPixel(x, y, finalColor)
                } else {
                    val origin = whiteImage.getPixel(coor_x, coor_y)
                    val gray =
                        Color.red(origin) * 19595 + Color.green(origin) * 38469 + Color.blue(origin) * 7472 shr 16
                    val finalColor = Color.argb(gray, 255, 255, 255)
                    result.setPixel(x, y, finalColor)
                }

            }
        }
        val path =
            Environment.getExternalStorageDirectory().toString() + File.separator + "灰度图" + File.separator + System.currentTimeMillis() + ".png"
        return if (ImageUtils.save(result, path, Bitmap.CompressFormat.PNG)) {
            path
        } else {
            ""
        }
    }
}