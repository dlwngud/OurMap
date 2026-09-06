package com.wngud.ourmap.data.demo

import com.wngud.ourmap.core.model.Memory
import com.wngud.ourmap.core.model.Place
import com.wngud.ourmap.core.model.Space
import javax.inject.Inject

/** Deterministic fixtures for week-one UI. No account, persistence, or network. */
class DemoContent @Inject constructor() {
    val space = Space("demo-space", "주형 · 수빈", listOf("주형", "수빈"))
    val memories = listOf(
        Memory("forest", Place("seoul-forest", "서울숲", "서울 성동구 성수동", 37.5444, 127.0374),
            "2026.05.21", "날씨가 좋아 오래 걸었던 날", "수빈", listOf("산책", "햇살좋음")),
        Memory("cafe", Place("seongsu-cafe", "성수 카페거리", "서울 성동구 성수동", 37.5445, 127.0560),
            "2026.05.10", "예쁜 카페에서 나눈 이야기", "수빈", listOf("카페")),
        Memory("river", Place("hangang", "한강공원", "서울 영등포구 여의도동", 37.5284, 126.9328),
            "2026.04.28", "노을이 정말 예뻤던 피크닉", "수빈", listOf("피크닉")),
    )
}
