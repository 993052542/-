package com.pinyougou.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import com.pinyougou.pojo.TbSeckillGoodsExample.Criteria;

@Component
public class SeckillTask {

	@Autowired
	private RedisTemplate redisTemplate;
	
	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;
	
	@Scheduled(cron="0 * * * * ?")
	public void refreshSeckill(){
	
		System.out.println("执行了秒杀商品增量更新"+new Date());
		//查询缓存中的秒杀商品的id集合
		List goodsIdList = new ArrayList<>(redisTemplate.boundHashOps("seckillGoods").keys());
		
		TbSeckillGoodsExample example = new TbSeckillGoodsExample();
		Criteria criteria = example.createCriteria();
		criteria.andStatusEqualTo("1");//审核通过的商品
		criteria.andStockCountGreaterThan(0);//库存数大于0
		criteria.andStartTimeLessThanOrEqualTo(new Date());//开始时间小于当前时间
		criteria.andEndTimeGreaterThanOrEqualTo(new Date());//结束时间大于当前时间
		
		if (goodsIdList.size()>0) {
			criteria.andIdNotIn(goodsIdList);//排除缓存中已近存在的商品ID集合			
		}
		
		//从数据库中查询数据
		List<TbSeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example );
		//将数据库中查询的数据存入缓存
		for (TbSeckillGoods seckillGoods : seckillGoodsList) {
			redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getId(), seckillGoods);
			System.out.println("增量更新秒杀商品id："+seckillGoods.getId());
		}
		
	}
	
	@Scheduled(cron="* * * * * ?")
	public void removeSeckillGoods(){
		//查询出缓存中的数据，扫描每条记录，判断时间。如果当前时间超过了截止时间，移除此记录
		List<TbSeckillGoods> seckillGoodsList = redisTemplate.boundHashOps("seckillGoods").values();
		System.out.println("开始执行清除秒杀商品");
		for (TbSeckillGoods seckillGoods : seckillGoodsList) {
			if (seckillGoods.getEndTime().getTime() < new Date().getTime()) {
				//同步数据到数据库
				seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
				//清楚缓存中的数据
				redisTemplate.boundHashOps("seckillGoods").delete(seckillGoods.getId());
				System.out.println("商品："+seckillGoods.getId()+"已过期");
			}
		}
		System.out.println("清除秒杀商品完毕");
	}
	
	
}
