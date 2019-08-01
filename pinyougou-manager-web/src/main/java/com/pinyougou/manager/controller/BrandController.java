package com.pinyougou.manager.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;

import entity.PageResult;
import entity.Result;

@RestController
@RequestMapping("/brand")
public class BrandController {

	@Reference
	private BrandService brandService;
	
	@RequestMapping("/findAll")
	public List<TbBrand> findAll(){
		
		return brandService.findAll();
	}
	
	/**
	 * 这是分页查询的方法
	 * @param page 当前页
	 * @param rows 每页记录数
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult findPage(int page,int rows){
		
		return brandService.findPage(page, rows);
	}

	/**
	 * 这是添加品牌的方法
	 * @param brand
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody TbBrand brand){
		
		try {
			brandService.add(brand);
			return new Result(true, "品牌添加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "品牌添加失败");
		}
				
	}
	/**
	 * 这是查询单一一个品牌的方法
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public TbBrand findOne(Long id){
		
		return brandService.findOne(id);
	}
	
	
	@RequestMapping("/update")
	public Result update(@RequestBody TbBrand brand){
		
		try {
			brandService.update(brand);
			return new Result(true, "品牌修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "品牌修改失败");
		}
				
	}
		
	@RequestMapping("/delete")
	public Result delete(Long[] ids){
		
		try {
			brandService.delete(ids);;
			return new Result(true, "品牌删除成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "品牌删除失败");
		}
	}
	
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbBrand brand,int page,int rows){
		
		return brandService.findPage(brand, page, rows);
	}
	
	@RequestMapping("/selectOptionList")
	public List<Map> selectOptionList(){
		
		return brandService.selectOptionList();
	}
}
