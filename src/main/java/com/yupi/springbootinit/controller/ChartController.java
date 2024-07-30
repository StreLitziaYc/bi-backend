package com.yupi.springbootinit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.bizmq.BiMessageConsumer;
import com.yupi.springbootinit.bizmq.BiMessageProducer;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.DeleteRequest;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.constant.BiMqConstant;
import com.yupi.springbootinit.constant.TaskStatusConstant;
import com.yupi.springbootinit.constant.UserConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.manager.AiManager;
import com.yupi.springbootinit.manager.RedissonLimiterManager;
import com.yupi.springbootinit.model.dto.chart.*;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.model.entity.User;
import com.yupi.springbootinit.model.vo.BiResultVO;
import com.yupi.springbootinit.service.ChartService;
import com.yupi.springbootinit.service.UserService;
import com.yupi.springbootinit.utils.ExcelUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartController {

    @Resource
    private ChartService chartService;

    @Resource
    private UserService userService;

    @Resource
    private AiManager aiManager;

    @Resource
    private RedissonLimiterManager redissonLimiterManager;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private BiMessageConsumer messageConsumer;

    @Resource
    private BiMessageProducer messageProducer;

    // region 增删改查

    /**
     * 创建
     *
     * @param chartAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addChart(@RequestBody ChartAddRequest chartAddRequest, HttpServletRequest request) {
        if (chartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartAddRequest, chart);
        User loginUser = userService.getLoginUser(request);
        chart.setUserId(loginUser.getId());
        boolean result = chartService.save(chart);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newChartId = chart.getId();
        return ResultUtils.success(newChartId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldChart.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = chartService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param chartUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest) {
        if (chartUpdateRequest == null || chartUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);
        // 参数校验
        chartService.validChart(chart, false);
        long id = chartUpdateRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Chart> getChartById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = chartService.getById(id);
        if (chart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(chart);
    }

    /**
     * 分页获取列表
     *
     * @param chartQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Chart>> listChartByPage(@RequestBody ChartQueryRequest chartQueryRequest) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                chartService.getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }


    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<Chart>> listMyChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
            HttpServletRequest request) {
        if (chartQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        chartQueryRequest.setUserId(loginUser.getId());
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                chartService.getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    // endregion

    /**
     * 编辑（用户）
     *
     * @param chartEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editChart(@RequestBody ChartEditRequest chartEditRequest, HttpServletRequest request) {
        if (chartEditRequest == null || chartEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartEditRequest, chart);
        // 参数校验
        chartService.validChart(chart, false);
        User loginUser = userService.getLoginUser(request);
        long id = chartEditRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldChart.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 智能分析（同步）
     *
     * @param multipartFile
     * @param genChartRequest
     * @param request
     * @return
     */
    @PostMapping("/gen")
    @Transactional
    public BaseResponse<BiResultVO> genChart(@RequestPart("file") MultipartFile multipartFile,
                                               GenChartRequest genChartRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ExcelUtils.validate(multipartFile);
        String name = genChartRequest.getName();
        String goal = genChartRequest.getGoal();
        String chartType = genChartRequest.getChartType();
        // 校验
        // 分析目标是否为空
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "分析目标为空");
        // 名称是否为空且长度是否<100
        ThrowUtils.throwIf(StringUtils.isBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "名称过长");
        // 限流判断
        redissonLimiterManager.doRateLimit("genChart:" + loginUser.getId());
        // 保存基本数据
        Chart chart = Chart.builder()
                .name(name)
                .goal(goal)
                .chartType(chartType)
                .userId(loginUser.getId())
                .build();
        boolean save = chartService.save(chart);
        Long chartId = chart.getId();
        chartService.createChartTable(multipartFile, chartId);
        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR, "输入图表保存失败");
        BiResultVO biResult = null;
        try {
            // 用户输入
            String userInput = AiManager.getUserInput(chartService.queryChartData(chartId), goal, chartType);
            // 生成结果
            biResult = aiManager.doChat(userInput);
            chart.setGenChart(biResult.getGenChart());
            chart.setGenResult(biResult.getGenResult());
            chart.setStatus(TaskStatusConstant.SUCCEED);
            save = chartService.updateById(chart);
            ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR, "生成图表保存失败");
            biResult.setChatId(chart.getId());
        } catch (Exception e) {
            chart.setExecMessage(e.getMessage());
            boolean update3 = chartService.updateById(chart);
            ThrowUtils.throwIf(!update3, ErrorCode.SYSTEM_ERROR, "图表错误信息保存失败");
        }
        return ResultUtils.success(biResult);
    }

    /**
     * 智能分析（异步）
     *
     * @param multipartFile
     * @param genChartRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/async/threadPool")
    @Transactional
    public BaseResponse<BiResultVO> genChartAsync(@RequestPart("file") MultipartFile multipartFile,
                                               GenChartRequest genChartRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ExcelUtils.validate(multipartFile);
        String name = genChartRequest.getName();
        String goal = genChartRequest.getGoal();
        String chartType = genChartRequest.getChartType();
        // 校验
        // 分析目标是否为空
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "分析目标为空");
        // 名称是否为空且长度是否<100
        ThrowUtils.throwIf(StringUtils.isBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "名称过长");
        // 限流判断
        redissonLimiterManager.doRateLimit("genChart:" + loginUser.getId());
        // 保存基本数据
        Chart chart = Chart.builder()
                .name(name)
                .goal(goal)
                .chartType(chartType)
                .userId(loginUser.getId())
                .build();
        boolean save = chartService.save(chart);
        Long chartId = chart.getId();
        chartService.createChartTable(multipartFile, chartId);
        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR, "输入图表保存失败");
        CompletableFuture.runAsync(() -> {
            try {
                // 先修改状态为 running
                chart.setStatus(TaskStatusConstant.RUNNING);
                boolean update1 = chartService.updateById(chart);
                ThrowUtils.throwIf(!update1, ErrorCode.SYSTEM_ERROR, "图表信息更新失败");
                // 用户输入
                String userInput = AiManager.getUserInput(chartService.queryChartData(chartId), goal, chartType);
                // 生成结果
                BiResultVO biResult = aiManager.doChat(userInput);
//                biResult.setGenResult(result.getGenResult());
//                biResult.setGenChart(result.getGenChart());
                chart.setGenChart(biResult.getGenChart());
                chart.setGenResult(biResult.getGenResult());
                boolean saveUpdate = chartService.updateById(chart);
                ThrowUtils.throwIf(!saveUpdate, ErrorCode.SYSTEM_ERROR, "生成图表保存失败");
//                biResult.setChatId(chart.getId());
                chart.setStatus(TaskStatusConstant.SUCCEED);
                boolean update2 = chartService.updateById(chart);
                ThrowUtils.throwIf(!update2, ErrorCode.SYSTEM_ERROR, "图表信息更新失败");
            } catch (Exception e) {
                chart.setExecMessage(e.getMessage());
                boolean update3 = chartService.updateById(chart);
                ThrowUtils.throwIf(!update3, ErrorCode.SYSTEM_ERROR, "图表错误信息保存失败");
            }
        }, threadPoolExecutor);
        return ResultUtils.success(new BiResultVO());
    }

    /**
     * 智能分析（异步）
     *
     * @param multipartFile
     * @param genChartRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/async")
    @Transactional
    public BaseResponse<BiResultVO> genChartAsyncMq(@RequestPart("file") MultipartFile multipartFile,
                                                  GenChartRequest genChartRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ExcelUtils.validate(multipartFile);
        String name = genChartRequest.getName();
        String goal = genChartRequest.getGoal();
        String chartType = genChartRequest.getChartType();
        // 校验
        // 分析目标是否为空
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "分析目标为空");
        // 名称是否为空且长度是否<100
        ThrowUtils.throwIf(StringUtils.isBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "名称过长");
        // 限流判断
        redissonLimiterManager.doRateLimit("genChart:" + loginUser.getId());
        // 保存基本数据
        Chart chart = Chart.builder()
                .name(name)
                .goal(goal)
                .chartType(chartType)
                .userId(loginUser.getId())
                .build();
        boolean save = chartService.save(chart);
        Long chartId = chart.getId();
        chartService.createChartTable(multipartFile, chartId);
        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR, "输入图表保存失败");
        messageProducer.sendMessage(BiMqConstant.BI_EXCHANGE, BiMqConstant.BI_ROUTING_KEY, chartId.toString());
        return ResultUtils.success(new BiResultVO());
    }
}
