package wiki.scene.hiddengraph

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.blankj.utilcode.util.Utils
import com.bumptech.glide.Glide
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    val REQ_SHOW_IMAGE: Int = 10001
    val REQ_HIDE_IMAGE: Int = 10002

    var showImagePath = ""
    var hideImagePath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Utils.init(applicationContext)
        btnChooseHideImage.setOnClickListener {
            Matisse.from(this@MainActivity)
                .choose(MimeType.ofImage())
                .countable(true)
                .maxSelectable(1)
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .imageEngine(Glide4Engine())
                .forResult(REQ_HIDE_IMAGE)
        }

        btnChooseShowImage.setOnClickListener {
            Matisse.from(this@MainActivity)
                .choose(MimeType.ofImage())
                .countable(true)
                .maxSelectable(1)
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .imageEngine(Glide4Engine())
                .forResult(REQ_SHOW_IMAGE)
        }
        btnCreateResultImage.setOnClickListener {
            val dialog: ProgressDialog = ProgressDialog.show(this@MainActivity, "提示", "正在处理图片...");
            Observable.create<String> {
                it.onNext(HiddenImageUtil.calculateHiddenImage(showImagePath, hideImagePath))
            }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { path ->
                    Glide.with(this@MainActivity)
                        .load(path)
                        .into(ivResult)
                    dialog.dismiss()
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQ_SHOW_IMAGE) {
                val mSelected: List<String> = Matisse.obtainPathResult(data);
                if (mSelected.size > 0) {
                    showImagePath = mSelected.get(0)
                    Glide.with(this@MainActivity)
                        .load(showImagePath)
                        .into(ivShow)
                }
            } else if (requestCode == REQ_HIDE_IMAGE) {
                val mSelected: List<String> = Matisse.obtainPathResult(data);
                if (mSelected.size > 0) {
                    hideImagePath = mSelected.get(0)
                    Glide.with(this@MainActivity)
                        .load(hideImagePath)
                        .into(ivHide)
                }
            }
        }
    }
}
