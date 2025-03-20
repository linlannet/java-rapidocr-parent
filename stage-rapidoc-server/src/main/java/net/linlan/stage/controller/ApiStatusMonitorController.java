package net.linlan.stage.controller;

import net.linlan.stage.webconfig.ServerConfig;
import net.linlan.commons.core.ResponseResult;
import net.linlan.commons.script.json.StringMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


/**
 * Filename:ApiStatusMonitorController.java
 * Desc:平台当前应用的版本，用于验证进程服务是否正常
 *
 * @author Linlan
 * CreateTime:12/19/17 9:00 PM
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("api/stage")
public class ApiStatusMonitorController
{
    @Value("${spring.profiles.active}")
    private String activeMode;

    /** 每次发版前，将版本号进行调整，以明确当前发布的版本和启动的版本是一致的
     * @param params
     * @return
     */
    @GetMapping("getStatus")
    public ResponseResult<Map<String, Object>> getStatus(@RequestParam Map<String, Object> params)
    {
        String url = ServerConfig.getUrl();
        Map<String, Object> map = new StringMap().put("version", "1.X.X")
                .put("activeMode", activeMode)
                .put("url", url)
                .put("params", params)
                .put("timestamp", System.currentTimeMillis()).map();
        return ResponseResult.ok(map);
    }

}
