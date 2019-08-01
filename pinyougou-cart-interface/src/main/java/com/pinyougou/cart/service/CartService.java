package com.pinyougou.cart.service;

import java.util.List;

import com.pinyougou.pojogroup.Cart;

/**
 * 购物车服务接口
 * @author 禹佩辛
 *
 */
public interface CartService {

	/**
	 * 添加商品到购物车
	 * @param list 添加一个购物车
	 * @param itemId 商品id
	 * @param num 商品数量
	 * @return
	 */
	public List<Cart> addGoodsToCart(List<Cart> cartList,Long itemId,Integer num);
	
	/**
	 * 从redis中取出购物车
	 * @param username
	 * @return
	 */
	public List<Cart> findCartListFormRedis(String username);

	/**
	 * 将购物车列表存入redis
	 * @param username
	 * @param cartList
	 */
	public void saveCartToRedis(String username,List<Cart> cartList);

	/**
	 *合并购物车
	 * @param cartList1
	 * @param cartList2
	 * @return
	 */
	public List<Cart> mergeCartList(List<Cart> cartList1,List<Cart> cartList2);
	
}
