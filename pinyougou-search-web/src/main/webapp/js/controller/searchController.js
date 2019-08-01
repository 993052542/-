app.controller('searchController',function($scope,$location,searchService){
	
	//定义搜索对象的结构
	$scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'paegSize':20,'sort':'','sortField':''};
	
	//搜索
	$scope.search=function(){
		$scope.searchMap.pageNo=parseInt($scope.searchMap.pageNo);//转换为数字
		searchService.search($scope.searchMap).success(
				function(response){
					$scope.resultMap=response;
					//$scope.searchMap.pageNo=1;
					buildPageLabel();
				}
		);
	}
	
	//构建分页栏
	buildPageLabel=function(){
		$scope.pageLabel=[];
		var firstPage=1;//开始页码
		var lastPage=$scope.resultMap.totalPages;//截止页码
		$scope.firstDot=true;//前面有省略号
		$scope.lastDot=true;//后面有省略号
		
		if($scope.resultMap.totalPages>5){//如果页码数大于5
			if($scope.searchMap.pageNo<=3){//如果当前页码数小于等于3，显示前5页
				lastPage=5;
				$scope.firstDot=false;
			}else if($scope.searchMap.pageNo>=$scope.resultMap.totalPages-2){//显示最后5页
				firstPage=$scope.resultMap.totalPages-4;
				$scope.lastDot=false;
			}else{//中间的5页
				firstPage=$scope.searchMap.pageNo-2;
				lastPage=$scope.searchMap.pageNo+2
			}	
		}else{//当总页数小于5页
			$scope.firstDot=false;//前面没有省略号
			$scope.lastDot=false;//后面没有省略号
		}
		
		//构建页码
		for(var i=firstPage ; i<= lastPage ; i++){
			$scope.pageLabel.push(i);
		}	
	}
	
	//添加搜索项
	$scope.addSearchItem=function(key,value){
		if(key=='category' || key=='brand' || key=='price'){//用户点击的是分类或者品牌选项
			$scope.searchMap[key]=value;
		}else{//用户点击的是规格选项
			$scope.searchMap.spec[key]=value;
		}
		$scope.search();
	}
	
	//撤销搜索项
	$scope.removeSearchItem=function(key){
		if(key=='category' || key=='brand' || key=='price'){//撤销分类或者品牌选项
			$scope.searchMap[key]='';
		}else{//撤销规格选项
			delete $scope.searchMap.spec[key];
		}
		$scope.search();
	}

	//分页查询
	$scope.queryByPage=function(pageNo){
		if(pageNo<1 || pageNo>$scope.resultMap.totalPages){
			return ;
		}
		$scope.searchMap.pageNo=pageNo;
		$scope.search();
	}
	
	//判断当前页是否为第一页
	$scope.isTopPage=function(){
		if($scope.searchMap.pageNo==1){
			return true;
		}else{
			return false;
		}
	}
	
	//判断当前页是否为最后一页
	$scope.isEndPage=function(){
		if($scope.searchMap.pageNo==$scope.resultMap.totalPages){
			return true;
		}else{
			return false;
		}
	}

	//排序查询
	$scope.sortSearch=function(sortField,sort){
		$scope.searchMap.sortField=sortField;
		$scope.searchMap.sort=sort;
		
		$scope.search();
	}
	
	//判断关键字是否为品牌
	$scope.keywordsIsBrand=function(){
		for(i=0 ; i<$scope.resultMap.brandList.length ; i++){
			if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text) >= 0){
				return true;
			}
		}
		return false;
	}
	
	//接受参数加载关键字
	$scope.loadkeywords=function(){
		$scope.searchMap.keywords = $location.search()['keywords'];
		
		$scope.search();
	}
	
});