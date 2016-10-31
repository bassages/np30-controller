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

        vm.playFolder = function(folderId) {
            LoadingIndicatorService.startLoading();

            $log.info("Play folder with id " + folderId);
            $http({
                method: 'POST', url: 'api/play-folder/' + folderId
            }).then(function successCallback(response) {
                LoadingIndicatorService.stopLoading();
            }, function errorCallback(response) {
                LoadingIndicatorService.stopLoading();
                $log.error(angular.toJson(response));
            });
        };

        vm.playRandomFolder = function(folderId) {
            LoadingIndicatorService.startLoading();

            $log.info("Play random folder. folderId=" + folderId);
            $http({
                method: 'POST', url: 'api/play-random-folder'
            }).then(function successCallback(response) {
                $log.info(response.data);
                LoadingIndicatorService.stopLoading();
            }, function errorCallback(response) {
                LoadingIndicatorService.stopLoading();
                $log.error(angular.toJson(response));
            });
        };

        vm.updateLocalDb = function() {
            LoadingIndicatorService.startLoading();

            $log.info("Update local DB");
            $http({
                method: 'POST', url: 'api/update-local-db'
            }).then(function successCallback(response) {
                $log.info(response.data);
                LoadingIndicatorService.stopLoading();
            }, function errorCallback(response) {
                LoadingIndicatorService.stopLoading();
                $log.error(angular.toJson(response));
            });
        };

        function loadItemsInNode(folderId) {
            LoadingIndicatorService.startLoading();
            $http({
                method: 'GET', url: 'api/files-in-folder/' + folderId
            }).then(function successCallback(response) {
                vm.itemsInSelectedNode = response.data;
                LoadingIndicatorService.stopLoading();
            }, function errorCallback(response) {
                LoadingIndicatorService.stopLoading();
                $log.error(angular.toJson(response));
            });
        }

        $scope.$watch('tree.currentNode', function(newObj, oldObj) {
            if( $scope.tree && angular.isObject($scope.tree.currentNode) ) {
                $log.info("Load items, folderId=" + $scope.tree.currentNode.id);
                loadItemsInNode($scope.tree.currentNode.id);
            }
        }, false);

    }
})();

