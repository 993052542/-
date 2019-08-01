package com.pinyougou.cart.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.WeiXinPayService;

import util.IdWorker;

@RestController
@RequestMapping("/pay")
public class PayController {

	@Reference
	private WeiXinPayService weiXinPayService;
	
	
	@RequestMapping("/createNative")
	public Map createNative(){
		IdWorker idWorker = new IdWorker();
		return weiXinPayService.createNative(idWorker.nextId()+"", "1");
	}
	
}
