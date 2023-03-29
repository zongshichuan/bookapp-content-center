package com.itmuch.contentcenter.controller.content;

import com.itmuch.contentcenter.auth.CheckAuthorization;
import com.itmuch.contentcenter.domain.dto.content.ShareAuditDTO;
import com.itmuch.contentcenter.domain.entity.content.Share;
import com.itmuch.contentcenter.service.content.ShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/shares")
public class ShareAdminController {

    @Autowired
    private ShareService shareService;

    @CheckAuthorization("admin") //表示只有角色是admin才能访问
    @PutMapping("/audit/{id}")
    public Share auditById(@PathVariable Integer id,@RequestBody ShareAuditDTO auditDTO){
        //TODO 认证、授权

        return shareService.auditById(id,auditDTO);
    }
}
