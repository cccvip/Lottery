package com.xiao.domain.strategy.service.draw.impl;

import com.alibaba.fastjson.JSON;
import com.xiao.domain.strategy.model.aggregates.StrategyRich;
import com.xiao.domain.strategy.model.req.DrawReq;
import com.xiao.domain.strategy.model.res.DrawResult;
import com.xiao.domain.strategy.repository.IStrategyRepository;
import com.xiao.domain.strategy.service.algorithm.IDrawAlgorithm;
import com.xiao.domain.strategy.service.draw.AbstractDrawBase;
import com.xiao.domain.strategy.service.draw.DrawBase;
import com.xiao.domain.strategy.service.draw.IDrawExec;
import com.xiao.infrastructure.po.Award;
import com.xiao.infrastructure.po.Strategy;
import com.xiao.infrastructure.po.StrategyDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @description: 抽奖系统
 * @author：carl
 * @date: 2021/10/10
 */
@Service("drawExec")
@Slf4j
public class DrawExecImpl extends AbstractDrawBase {
    @Resource
    private IStrategyRepository strategyRepository;

    @Override
    protected List<String> queryExcludeAwardIds(Long strategyId) {
        List<String> awardList = strategyRepository.queryNoStockStrategyAwardList(strategyId);
        log.info("执行抽奖策略 strategyId：{}，无库存排除奖品列表ID集合 awardList：{}", strategyId, JSON.toJSONString(awardList));
        return awardList;
    }

    @Override
    protected String drawAlgorithm(Long strategyId, IDrawAlgorithm drawAlgorithm, List<String> excludeAwardIds) {
        // 执行抽奖
        String awardId = drawAlgorithm.randomDraw(strategyId, excludeAwardIds);
        // 判断抽奖结果
        if (null == awardId) {
            return null;
        }

        /*
         * 扣减库存，暂时采用数据库行级锁的方式进行扣减库存，后续优化为 Redis 分布式锁扣减 decr/incr
         * 注意：通常数据库直接锁行记录的方式并不能支撑较大体量的并发，但此种方式需要了解，因为在分库分表下的正常数据流量下的个人数据记录中，是可以使用行级锁的，因为他只影响到自己的记录，不会影响到其他人
         */
        boolean isSuccess = strategyRepository.deductStock(strategyId, awardId);
        // 返回结果，库存扣减成功返回奖品ID，否则返回NULL 「在实际的业务场景中，如果中奖奖品库存为空，则会发送兜底奖品，比如各类券」
        return isSuccess ? awardId : null;
    }
}