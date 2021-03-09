package com.youlai.admin.controller;

import cn.hutool.core.util.StrUtil;
import com.youlai.admin.common.constant.ESConstants;
import com.youlai.admin.pojo.domain.LoginRecord;
import com.youlai.common.elasticsearch.service.ElasticSearchService;
import com.youlai.common.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author hxr
 * @date 2021-03-09
 */
@Api(tags = "登录记录")
@RestController
@RequestMapping("/api.admin/v1/records/login")
@Slf4j
@AllArgsConstructor
public class LoginRecordController {

    ElasticSearchService elasticSearchService;



    @ApiOperation(value = "列表分页", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", defaultValue = "1", paramType = "query", dataType = "Long"),
            @ApiImplicitParam(name = "limit", value = "每页数量", defaultValue = "10", paramType = "query", dataType = "Long"),
            @ApiImplicitParam(name = "startDate", value = "开始日期", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "endDate", value = "结束日期", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "clientIP", value = "客户端IP", paramType = "query", dataType = "String")
    })
    @GetMapping
    public Result list(
            Integer page,
            Integer limit,
            String startDate,
            String endDate,
            String clientIP
    ) {

        // 日期范围
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("date");

        if (StrUtil.isNotBlank(startDate)) {
            rangeQueryBuilder.from(startDate);
        }
        if (StrUtil.isNotBlank(endDate)) {
            rangeQueryBuilder.to(endDate);
        }

        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery().must(rangeQueryBuilder);

        if (StrUtil.isNotBlank(clientIP)) {
            queryBuilder.must(QueryBuilders.wildcardQuery("clientIP", "*" + clientIP + "*"));
        }
        // 总记录数
        long count = elasticSearchService.count(queryBuilder, ESConstants.INDEX_LOGIN_PATTERN);

        // 排序
        FieldSortBuilder sortBuilder = new FieldSortBuilder("@timestamp").order(SortOrder.DESC);

        // 分页数
        List<LoginRecord> list = elasticSearchService.search(queryBuilder, sortBuilder, page, limit, LoginRecord.class, ESConstants.INDEX_LOGIN_PATTERN);
        return Result.success(list, count);
    }


    @ApiOperation(value = "删除登录记录", httpMethod = "DELETE")
    @ApiImplicitParam(name = "ids", value = "id集合", required = true, paramType = "query", dataType = "String")
    @DeleteMapping("/{ids}")
    public Result delete(@PathVariable String ids) {
        return Result.judge(true);
    }




}
