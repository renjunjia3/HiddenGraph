package wiki.scene.hiddengraph

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import com.blankj.utilcode.util.ToastUtils
import com.blankj.utilcode.util.Utils
import com.bumptech.glide.Glide
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import com.hjq.permissions.OnPermission


class MainActivity : AppCompatActivity() {
    val REQ_SHOW_IMAGE: Int = 10001
    val REQ_HIDE_IMAGE: Int = 10002

    var showImagePath = ""
    var hideImagePath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Utils.init(applicationContext)
        btnChooseShowImage.setOnClickListener {
            chooseImage(1)
        }
        btnChooseHideImage.setOnClickListener {
            chooseImage(2)

        }
        btnCreateResultImage.setOnClickListener {
            if (TextUtils.isEmpty(showImagePath)) {
                ToastUtils.showShort("请选择显示的图片")
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(hideImagePath)) {
                ToastUtils.showShort("请选择隐藏的图片")
                return@setOnClickListener
            }
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

    fun chooseImage(type: Int) {
        if (XXPermissions.isHasPermission(this, Permission.Group.STORAGE)) {
            toChooseImage(type)
        } else {
            XXPermissions.with(this)
                .constantRequest() //可设置被拒绝后继续申请，直到用户授权或者永久拒绝
                .permission(Permission.Group.STORAGE)
                .request(object : OnPermission {
                    override fun hasPermission(granted: List<String>, isAll: Boolean) {
                        toChooseImage(type)
                    }

                    override fun noPermission(denied: List<String>, quick: Boolean) {
                        ToastUtils.showShort("权限被拒绝，请先开启权限")
                    }
                })
        }
    }

    fun toChooseImage(type: Int) {
        Matisse.from(this@MainActivity)
            .choose(MimeType.ofImage())
            .countable(true)
            .maxSelectable(1)
            .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
            .thumbnailScale(0.85f)
            .imageEngine(Glide4Engine())
            .forResult(if (type == 1) REQ_SHOW_IMAGE else REQ_HIDE_IMAGE)
    }
}
