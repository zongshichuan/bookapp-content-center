package com.itmuch.contentcenter.controller.content;

import com.github.pagehelper.PageInfo;
import com.itmuch.contentcenter.auth.CheckLogin;
import com.itmuch.contentcenter.domain.dto.content.ShareDTO;
import com.itmuch.contentcenter.domain.entity.content.Share;
import com.itmuch.contentcenter.service.content.ShareService;
import com.itmuch.contentcenter.util.JwtOperator;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/shares")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ShareController {
    private final ShareService shareService;
    private final JwtOperator jwtOperator;

    @GetMapping("/{id}")
    @CheckLogin
    public ShareDTO findById(@PathVariable Integer id,
                             @RequestHeader("x-token") String token){
        return this.shareService.findById(id,token);
    }

    @GetMapping("/q")
    public PageInfo<Share> q(
            @RequestParam(required = false) String title,
            @RequestParam(required = false,defaultValue = "1") Integer pageNo,
            @RequestParam(required = false,defaultValue = "10") Integer pageSize,
            @RequestHeader(value = "x-token",required = false) String token){
        //注意：pageSize务必做控制，防止前端传来的pageSize太大
        if (pageSize > 100){
            pageSize = 100;
        }
        Integer userId = null;
        if (StringUtils.isNotBlank(token)){
            Claims claimsFromToken = this.jwtOperator.getClaimsFromToken(token);
            userId = (Integer) claimsFromToken.get("id");
        }

        PageInfo<Share> info = this.shareService.q(title, pageNo, pageSize,userId);
        return info;
    }

    @GetMapping("/exchange/{id}")
    @CheckLogin
    public Share exchangeById(@PathVariable Integer id, HttpServletRequest request){
        return this.shareService.exchangeById(id,request);
    }

}
