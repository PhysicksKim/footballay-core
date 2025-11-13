package com.footballay.core.web.common.controller.dev;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DevToolController {

    /**
     * Chrome DevTools 앱 특정 설정 파일 요청에 에러를 방지하기 위한 맵핑입니다.
     *
     * @return 빈 JSON 객체 문자열
     */
    @GetMapping("/.well-known/appspecific/com.chrome.devtools.json")
    @ResponseBody
    public String chromeDevtoolsWellKnown() {
        return "{}";
    }
}
