package com.miao.ai_gen_web.service;

import com.miao.ai_gen_web.entity.App;
import com.miao.ai_gen_web.entity.User;
import com.miao.ai_gen_web.model.dto.app.AppAddRequest;
import com.miao.ai_gen_web.model.dto.app.AppQueryRequest;
import com.miao.ai_gen_web.model.vo.AppVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author miao
 */
public interface AppService extends IService<App> {

    AppVO getAppVO(App app);

    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    List<AppVO> getAppVOList(List<App> appList);

    Flux<String> chatToGenCode(Long appId, String message, User loginUser);

    void generateAppScreenshotAsync(Long appId, String appUrl);

    String deployApp(Long appId, User loginUser);

    Long createApp(AppAddRequest appAddRequest, User loginUser);
}
