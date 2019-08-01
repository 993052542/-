package com.pinyougou.cart.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojogroup.Cart;

import entity.Result;
import util.CookieUtil;

@RestController
@RequestMapping("/cart")
public class CartController {

	@Reference(timeout=6000)
	private CartService cartService;
	
	@Autowired
	private HttpServletRequest request;
	
	@Autowired
	private HttpServletResponse response;
	
	@RequestMapping("/findCartList")
	public List<Cart> findCartList(){
		
		//获取当前登录人账号
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		System.out.println("当前登录人："+username);
		
		//从cookie中提取购物车
		System.out.println("从cookie中提取购物车");
		String cartListString = CookieUtil.getCookieValue(request, "cartList", "utf-8");
		if (cartListString == null || cartListString.equals("")) {
			cartListString = "[]";
		}
		List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);
		
		//表示用户没有登录
		if (username.equals("anonymousUser")) {
		
			return cartList_cookie;
		
		}else{//用户已经登录
			//获取redis购物车
			List<Cart> cartList_redis = cartService.findCartListFormRedis(username);
			
			//判断本地购物车中是否有数据
			if (cartList_cookie.size()>0) {
				//合并购物车
				List<Cart> cartList = cartService.mergeCartList(cartList_cookie, cartList_redis);
				//将合并后的购物车存入redis
				cartService.saveCartToRedis(username, cartList);
				//将本地的购物车清楚
				CookieUtil.deleteCookie(request, response, "cartList");	
				System.out.println("执行了合并购物车");
				return cartList;
			}
			
			return cartList_redis;
		}
	}
	
	@RequestMapping("/addGoodsToCartList")
	@CrossOrigin(origins="*",allowCredentials="true")
	public Result addGoodsToCartList(Long itemId,Integer num){
		
		String name = SecurityContextHolder.getContext().getAuthentication().getName();//当前登录人账号
		System.out.println("当前登录人："+name);
		
		try {
			
			//1.从cookie中提取购物车
			List<Cart> cartList = findCartList();
			//2.调用服务方法操作购物车
			cartList = cartService.addGoodsToCart(cartList, itemId, num);
			
			if (name.equals("anonymousUser")) {//如果用户没有登录
				//3.将新的购物车存入cookie
				String cartListString = JSON.toJSONString(cartList);
				CookieUtil.setCookie(request, response, "cartList", cartListString , 3600*24 , "utf-8");
				System.out.println("向cookie中存储购物车");
				
			}else {//当用户已经登录
				cartService.saveCartToRedis(name, cartList);
			}
			
			return new Result(true, "存入购物车成功");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new Result(false, "存入购物车失败");
		}
		
	}
	
}
