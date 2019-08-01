package com.pinyougou.search.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.FilterQuery;
import org.springframework.data.solr.core.query.GroupOptions;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.HighlightQuery;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.SolrDataQuery;
import org.springframework.data.solr.core.query.result.GroupEntry;
import org.springframework.data.solr.core.query.result.GroupPage;
import org.springframework.data.solr.core.query.result.GroupResult;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightEntry.Highlight;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.core.query.result.ScoredPage;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;

@Service(timeout=5000)
public class ItemSearchServiceImpl implements ItemSearchService {

	@Autowired
	private SolrTemplate solrTemplate;
	
	@Override
	public Map search(Map searchMap) {
		Map map = new HashMap();
		
		//空格处理
		String keywords = (String) searchMap.get("keywords");
		searchMap.put("keywords", keywords.replace(" ", ""));
		
		//1.关键字查询列表
		map.putAll(searchList(searchMap));
		//2.分类查询列表
		List<String> categotyList = searchCategotyList(searchMap);
		map.put("categoryList", categotyList); 
		
		//3.查询品牌和规格列表
		String category = (String) searchMap.get("category");
		if (!category.equals("")) {
			map.putAll(searchBrandAndSpecList(category));
		}else{		
			if (categotyList.size()>0) {
				map.putAll(searchBrandAndSpecList(categotyList.get(0)));		
			}
		}
		return map;
	}

	/**
	 * 关键字查询
	 * @param searchMap
	 * @return
	 */
	private Map searchList(Map searchMap){
		Map map = new HashMap<>();
		
		//高亮显示设置
		HighlightQuery query = new SimpleHighlightQuery();
		
		HighlightOptions options = new HighlightOptions().addField("item_title");
		options.setSimplePrefix("<em style='color:red'>");
		options.setSimplePostfix("</em>");
		query.setHighlightOptions(options );//为查询对象设置高亮
		//1.1关键字查询
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria );
		
		//1.2按照商品分类进行过滤
		if (!"".equals(searchMap.get("category"))) {//如果用户点击了分类
			FilterQuery filterQuery = new SimpleFilterQuery();
			Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
			filterQuery.addCriteria(filterCriteria);
			query.addFilterQuery(filterQuery);			
		}
		
		//1.3按照品牌进行过滤
		if (!"".equals(searchMap.get("brand"))) {//如果用户点击了品牌
			FilterQuery filterQuery = new SimpleFilterQuery();
			Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
			filterQuery.addCriteria(filterCriteria);
			query.addFilterQuery(filterQuery);			
		}
		//1.4按照规格过滤
		if (searchMap.get("spec")!=null) {
			Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");
			for(String key:specMap.keySet()){
				FilterQuery filterQuery = new SimpleFilterQuery();
				Criteria filterCriteria = new Criteria("item_spec_"+key).is(specMap.get(key));
				filterQuery.addCriteria(filterCriteria);
				query.addFilterQuery(filterQuery);	
			}
		}
		
		//1.5按照价格过滤
		if (!"".equals(searchMap.get("price"))) {
			String[] price =  ((String) searchMap.get("price")).split("-");
			if (!price[0].equals("0")) {//如果最低价格不等于0
				FilterQuery filterQuery = new SimpleFilterQuery();
				Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(price[0]);
				filterQuery.addCriteria(filterCriteria);
				query.addFilterQuery(filterQuery);
			}
			if (!price[1].equals("*")) {//如果最高价格不等于*
				FilterQuery filterQuery = new SimpleFilterQuery();
				Criteria filterCriteria = new Criteria("item_price").lessThanEqual(price[1]);
				filterQuery.addCriteria(filterCriteria);
				query.addFilterQuery(filterQuery);
			}
		}
		
		//1.6分页查询
		Integer pageNo = (Integer) searchMap.get("pageNo");//获取页码
		if (pageNo==null) {
			pageNo=1;
		}
		Integer pageSize = (Integer) searchMap.get("pageSize");//获取每页记录数
		if (pageSize==null) {
			pageSize=20;
		}
		query.setOffset((pageNo-1)*pageSize);//开始索引
		query.setRows(pageSize);
			
		//1.7排序
		String sortValue = (String) searchMap.get("sort");
		String sortField = (String) searchMap.get("sortField");
		if (sortValue!=null && !sortValue.equals("")) {
			
			//升序排列
			if (sortValue.equals("ASC")) {
				Sort sort  = new Sort(Sort.Direction.ASC, "item_"+sortField);
				query.addSort(sort);
			}
			//降序排列
			if (sortValue.equals("DESC")) {
				Sort sort  = new Sort(Sort.Direction.DESC, "item_"+sortField);
				query.addSort(sort);
			}
		}
			
		//高亮页对象
		HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
		//高亮集合入口(每条记录的高亮入口)
		List<HighlightEntry<TbItem>> entryList = page.getHighlighted();
		
		for (HighlightEntry<TbItem> entry : entryList) {
			//获取高亮列表(高亮域的个数)
			List<Highlight> highlights = entry.getHighlights();
			
			if (highlights.size()>0 && highlights.get(0).getSnipplets().size()>0) {
				TbItem item = entry.getEntity();
				item.setTitle(highlights.get(0).getSnipplets().get(0));
			}
		}
		
		map.put("rows", page.getContent());
		map.put("totalPages", page.getTotalPages());//总页数
		map.put("total", page.getTotalElements());//总记录数
		return map;
	}
	
	/**
	 * 分组查询(查询商品分类列表)
	 * @param searchMap
	 * @return
	 */
	private List searchCategotyList(Map searchMap){
		
		List list = new ArrayList();
		
		Query query = new SimpleQuery("*:*");
		//根据关键字查询
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		//设置分组选项
		GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
		query.setGroupOptions(groupOptions);
		//获取分组页
		GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query , TbItem.class);
		//获取分组结果对象
		GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
		//获取分组入口页
		Page<GroupEntry<TbItem>> entries = groupResult.getGroupEntries();
		//获取分组入口集合
		List<GroupEntry<TbItem>> entryList = entries.getContent();
	
		for (GroupEntry<TbItem> groupEntry : entryList) {
			//将分组的结果添加到list集合中
			list.add(groupEntry.getGroupValue());
		}
		
		return list;
	}
	
	@Autowired
	private RedisTemplate redisTemplate;
	/**
	 * 根据商品分类名称查询缓存中品牌与规格列表
	 * @param category
	 * @return
	 */
	private Map searchBrandAndSpecList(String category){
		Map map = new HashMap();
		//根据分类名称查询模板Id
		Long templateId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
		if (templateId!=null) {
			//根据模板id查询品牌列表
			List brandList = (List) redisTemplate.boundHashOps("brandList").get(templateId);
			map.put("brandList", brandList);
			//根据模板id查询规格列表
			List specList =  (List) redisTemplate.boundHashOps("specList").get(templateId);
			map.put("specList", specList);
		}
		return map;
	}

	@Override
	public void importList(List list) {
		
		solrTemplate.saveBeans(list);
		solrTemplate.commit();
	}

	@Override
	public void deleteByGoodsIds(List goodIds) {
		
		SolrDataQuery query = new SimpleQuery("*:*");
		Criteria criteria = new Criteria("item_goodsid").in(goodIds);
		query.addCriteria(criteria );
		solrTemplate.delete(query );
		solrTemplate.commit();
	}	
	
	
}
