//购物车控制层 
app.controller('cartController',function($scope,cartService){
	//查询购物车列表
	$scope.findCartList=function(){
		cartService.findCartList().success(
			function(response){
				$scope.cartList=response;
				$scope.totalValue = cartService.sum($scope.cartList);
			}
		);		
	}
	
	
	//数量的加减
	$scope.addGoodsToCartList=function(itemId,num){
		cartService.addGoodsToCartList(itemId,num).success(
			function(response){
				if(response.success){
					$scope.findCartList();
				}else{
					alert(response.message);
				}
			}
		);
		
	}
	
	//获取当前用户的地址列表
	$scope.findAddressList=function(){
		cartService.findAddressList().success(
			function(response){
				$scope.addressList=response;
				for(var i=0;i<$scope.addressList.length;i++){
					if($scope.addressList[i].isDefault=='1'){
						$scope.address=$scope.addressList[i];
						break;
					}
				}
			}
		);
	}
	
	//选择地址
	$scope.selectAddress=function(address){
		$scope.address=address;
	}
	
	//判断某地址是不是当前选择的地址
	$scope.isSelectedAddress=function(address){
		if(address==$scope.address){
			return true;
		}else{
			return false;
		}
	}
	
	$scope.order={paymentType:'1'};//订单对象
	
	$scope.selecyPayType=function(type){
		$scope.order.paymentType=type;
	}
	
	//提交订单
	$scope.submitOrder=function(){
		$scope.order.receiverAreaName=$scope.address.address;//地址
		$scope.order.receiverMobile=$scope.address.mobile;//手机
		$scope.order.receiver=$scope.address.contact;//联系人
		cartService.submitOrder($scope.order).success(
			function(response){
				//alert(response.message);
				if(response.success){
					if($scope.order.paymentType=='1'){//如果微信付款，跳到支付页面
						location.href='pay.html';
					}else{//如果货到付款，跳到成功页面
						location.href='paysuccess.html';
					}
				}else{
					alert(response.message);
				}
			
			}
		);
	}
	
	
	
	
});