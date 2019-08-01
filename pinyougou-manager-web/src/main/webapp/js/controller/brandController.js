app.controller('brandController',function($scope,$controller,brandService){
			
			$controller('baseController',{$scope:$scope});
	
			$scope.findAll=function(){
				brandService.findAll().success(
						function(response){
							$scope.list=response
							
				})
				
			}
			
			$scope.findPage = function(page,rows){
				brandService.findPage(page,rows).success(
					function(response){
						$scope.list=response.rows;
						$scope.paginationConf.totalItems=response.total;
					}		
				)
				
			}
			
			$scope.save=function(){
				var object=null;
				if($scope.entity.id!=null){
					object=brandService.update($scope.entity)
				}else{
					object=brandService.add($scope.entity)
				}
				object.success(
					function(response){
						if(response.success){
							$scope.reloadList();
						}else{
							alert(response.message);
						}
					}		
				)
				
			}
			
			$scope.findOne=function(id){
				brandService.findOne(id).success(
					function(response){
						$scope.entity=response
					}		
				)
			}
			
			
			
			$scope.delete=function(){
				brandService.delete($scope.selectIds).success(
					function(response){
						if(response.success){
							$scope.reloadList();
						}else{
							alert(response.message);
						}
					}		
				)
				
			}
			
			$scope.searchEntity={};
			$scope.search=function(page,rows){
				brandService.search(page,rows,$scope.searchEntity).success(
						function(response){
							$scope.list=response.rows;
							$scope.paginationConf.totalItems=response.total;
						}		
				)
				
			}
			
		})