package com.wngud.ourmap.navigation

import androidx.lifecycle.ViewModel
import com.wngud.ourmap.data.demo.DemoContent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(val demo: DemoContent) : ViewModel()
