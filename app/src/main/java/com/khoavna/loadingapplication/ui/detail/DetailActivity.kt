package com.khoavna.loadingapplication.ui.detail

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.khoavna.loadingapplication.R
import com.khoavna.loadingapplication.databinding.ActivityDetailBinding
import com.khoavna.loadingapplication.ui.MainActivity

class DetailActivity : AppCompatActivity() {

    private val binding : ActivityDetailBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_detail)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        initData()
        initListener()
    }

    private fun initListener() {
        binding.btnDone.setOnClickListener {
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK;Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(this)
                finish()
            }
        }
    }

    private fun initData() {
        intent?.extras?.run {
            binding.apply {
                fileName = getString(MainActivity.FILE_NAME_KEY)
                status = getString(MainActivity.STATUS_KEY)
                lifecycleOwner = this@DetailActivity
            }
        }
    }
}