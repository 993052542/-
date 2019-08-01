package com.pinyougou.pay.service;

import java.util.Map;

public interface WeiXinPayService {

	/**
	 * 返回生产二维码所需要的数据
	 * @return
	 */
	public Map createNative(String out_trade_no,String total_fee);
	
}
