 //控制层 
app.controller('userController' ,function($scope,$controller,userService){	
	
	
	$scope.reg=function(){
		//比较两次输入的密码是否一致
		if($scope.entity.password!=$scope.password){
			alert("输入的两次密码不一致");
			$scope.entity.password="";
			$scope.password="";
			return ;
		}
		
		userService.add($scope.entity,$scope.smscode).success(
				function(response){
					alert(response.message);
				}
		);
		
	}

	//发送验证码
	$scope.sendCode=function(){
		
		if($scope.entity.phone==null || $scope.entity.phone==""){
			alert("手机号码不能为空");
			return ;
		}
		
		userService.sendCode($scope.entity.phone).success(
				function(response){
					alert(response.message);
				}
		);
	}
	
	
	
});	
