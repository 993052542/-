package com.pinyougou.sellergoods.service;

import java.util.List;
import java.util.Map;

import com.pinyougou.pojo.TbBrand;

import entity.PageResult;
import entity.Result;

public interface BrandService {

	public List<TbBrand> findAll();
	
	//这是分页查询的方法
	public PageResult findPage(int pageNum,int pageSize);
	
	//这是添加品牌的方法
	public void add(TbBrand brand);
	
	//这是查找单独一个品牌的方法
	public TbBrand findOne(Long id);
	
	//这是修改品牌的方法
	public void update(TbBrand brand);
	
	//这是删除品牌的方法
	public void delete(Long[] ids);
	
	//这是设置条件分页查询
	public PageResult findPage(TbBrand brand,int pageNum,int pageSize);
	
	public List<Map> selectOptionList();
}
