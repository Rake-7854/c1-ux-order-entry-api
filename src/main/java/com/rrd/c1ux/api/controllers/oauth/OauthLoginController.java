package com.rrd.c1ux.api.controllers.oauth;

import com.rrd.c1ux.api.controllers.RouteConstants;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OauthLoginController {

    @GetMapping(RouteConstants.OAUTH_LOGIN)
    public String getLoginPage(Model model) {

        return "redirect:/oauth2/authorization/wso2";
    }
}
