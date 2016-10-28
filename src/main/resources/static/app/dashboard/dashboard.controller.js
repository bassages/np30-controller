(function() {
    'use strict';

    angular
        .module('app')
        .controller('DashboardController', DashboardController);

    DashboardController.$inject = ['$scope', '$http', '$log', 'LoadingIndicatorService'];

    function DashboardController($scope, $http, $log, LoadingIndicatorService) {
        var vm = this;

        loadTree();

        function loadTree() {
            LoadingIndicatorService.startLoading();
            $http({
                method: 'GET', url: 'api/folder'
            }).then(function successCallback(response) {
                vm.treedata = response.data;
                LoadingIndicatorService.stopLoading();
            }, function errorCallback(response) {
                LoadingIndicatorService.stopLoading();
                $log.error(angular.toJson(response));
            });
        }

        function loadItemsInNode(nodeId) {
            LoadingIndicatorService.startLoading();
            $http({
                method: 'GET', url: 'api/files-in-folder/' + nodeId
            }).then(function successCallback(response) {
                vm.itemsInSelectedNode = response.data;
                LoadingIndicatorService.stopLoading();
            }, function errorCallback(response) {
                LoadingIndicatorService.stopLoading();
                $log.error(angular.toJson(response));
            });
        }

        $scope.$watch( 'tree.currentNode', function(newObj, oldObj) {
            if( $scope.tree && angular.isObject($scope.tree.currentNode) ) {
                console.log( $scope.tree.currentNode.id );
                loadItemsInNode($scope.tree.currentNode.id);
            }
        }, false);

    }
})();

