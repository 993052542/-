package com.pinyougou.cart.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;

@Service
public class CartServiceImpl implements CartService {

	@Autowired
	private TbItemMapper itemMapper;
	
	@Override
	public List<Cart> addGoodsToCart(List<Cart> cartList, Long itemId, Integer num) {
		
		//1.根据SKU的id查询商品明细对象
		TbItem item = itemMapper.selectByPrimaryKey(itemId);
		
		if (item == null) {
			throw new RuntimeException("商品不存在");
		}
		
		if (!item.getStatus().equals("1")) {
			throw new RuntimeException("商品状态非法，请重试");
		}
		
		//2.根据SKU对象得到商家ID
		String sellerId = item.getSellerId();
		
		//3.根据商家id在购物车列表中查询购物车对象
		Cart cart = searchCart(cartList, sellerId);
		
		//4.如果购物车列表中不存在该商家的购物车
		if (cart == null) {
			
			//4.1创建一个购物车对象
			cart = new Cart();
			cart.setSellerId(sellerId);//给购物车添加商家id
			cart.setSellerName(item.getSeller());//给购物车添加商家名称
			
			//创建购物车明细列表
			List<TbOrderItem> orderItemList = new ArrayList<>();
			TbOrderItem orderItem = createOrderItem(item, num);
			orderItemList.add(orderItem);
			cart.setOrderItemList(orderItemList);
			
			//4.2将新的购物车对象添加到列表中
			cartList.add(cart);
		
		} else {
			
			//5.如果购物车列表中存在该商家的购物车
			TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(), itemId);
			
			//判断该商品是否在该购物车的明细列表中
			if (orderItem==null) {
				//5.1如果不存在，创建新的购物车明细对象并添加到商家购物车的明细列表中
				orderItem = createOrderItem(item, num);
				cart.getOrderItemList().add(orderItem);
				
			}else {
				//5.2如果存在，则数量增加,并更新金额
				orderItem.setNum(orderItem.getNum()+num);//数量增加
				orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue()*orderItem.getNum()));//更改总金额
				
				//当明细的数量小于等于0，移除该明细
				if (orderItem.getNum()<=0) {
					cart.getOrderItemList().remove(orderItem);
				}
				//当购物车的明细数量为空，移除该购物车
				if (cart.getOrderItemList().size()==0) {
					cartList.remove(cart);
				}
			}
		}
		return cartList;
	}

	/**
	 * 根据商家id在购物车列表中查询购物车对象
	 * @param cartList
	 * @param sellerId
	 * @return
	 */
	private Cart searchCart(List<Cart> cartList,String sellerId){
		
		for (Cart cart : cartList) {
			if (cart.getSellerId().equals(sellerId)) {
				return cart;
			}
		}
		return null;
	}
	
	/**
	 * 创建购物车明细对象
	 * @param item
	 * @param num
	 * @return
	 */
	private TbOrderItem createOrderItem(TbItem item,Integer num){

		if (num<=0) {
			throw new RuntimeException("数量非法，请重新输入");
		}
		
		TbOrderItem orderItem = new TbOrderItem();
		orderItem.setGoodsId(item.getGoodsId());
		orderItem.setItemId(item.getId());
		orderItem.setNum(num);
		orderItem.setPicPath(item.getImage());
		orderItem.setPrice(item.getPrice());
		orderItem.setSellerId(item.getSellerId());
		orderItem.setTitle(item.getTitle());
		orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));
		
		return orderItem;
	}
	/**
	 * 根据SKU的id在购物明细列表中查询是否有该商品
	 * @param orderItemList
	 * @param itemId
	 * @return
	 */
	private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList,Long itemId){
		for (TbOrderItem orderItem : orderItemList) {
			if (orderItem.getItemId().longValue() == itemId.longValue()) {
				return orderItem;
			}
		}
		return null;
		
	}

	@Autowired
	private RedisTemplate redisTemplate;
	
	@Override
	public List<Cart> findCartListFormRedis(String username) {
		System.out.println("从redis中取出购物车------"+username);
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
		if (cartList==null) {
			cartList = new ArrayList<>();
		}
		return cartList;
	}

	@Override
	public void saveCartToRedis(String username, List<Cart> cartList) {
		System.out.println("向redis中存入购物车-------"+username);
		redisTemplate.boundHashOps("cartList").put(username, cartList);
	}

	@Override
	public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
		
		for(Cart cart:cartList2){
			for(TbOrderItem orderItem : cart.getOrderItemList()){
				cartList1 = addGoodsToCart(cartList1, orderItem.getItemId(), orderItem.getNum());
			}
		}
		return cartList1;
	}
	
	
	
}
