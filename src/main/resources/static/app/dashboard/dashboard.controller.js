(function() {
    'use strict';

    angular
        .module('app')
        .controller('DashboardController', DashboardController);

    DashboardController.$inject = ['$scope', '$http', '$log', 'LoadingIndicatorService'];

    function DashboardController($scope, $http, $log, LoadingIndicatorService) {
        var vm = this;

        vm.breadcrumb = [];

        vm.up = function() {
            var current = vm.breadcrumb.pop();
            var parentOfCurrent = vm.breadcrumb.pop();
            $log.info(parentOfCurrent);
            vm.load(parentOfCurrent.id);
        };

        function getFolders(items, folder) {
            var folders = [];
            for (var key in items) {
                var item = items[key];
                if (item.container === folder) {
                    folders.push(item);
                }
            }
            return folders;
        }

        vm.detailsTitle = function() {
            var result = "-";
            if (vm.itemsInSelectedFolder && vm.itemsInSelectedFolder.length > 0) {
                result = vm.breadcrumb[vm.breadcrumb.length-1].title;
            }
            return result;
        };

        vm.load = function(folderId) {
            $log.info("Load folderId=" + folderId);

            LoadingIndicatorService.startLoading();
            $http({
                method: 'GET', url: 'api/folder/' + folderId
            }).then(function successCallback(response) {
                vm.breadcrumb.push(response.data);

                vm.foldersInSelectedFolder = getFolders(response.data.children, true);
                vm.itemsInSelectedFolder = getFolders(response.data.children, false);

                LoadingIndicatorService.stopLoading();
            }, function errorCallback(response) {
                LoadingIndicatorService.stopLoading();
                $log.error(angular.toJson(response));
            });
        };

        vm.load("0:0");

        vm.playCurrentFolder = function() {
            LoadingIndicatorService.startLoading();

            var folderId = vm.breadcrumb[vm.breadcrumb.length-1].id;

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

        vm.playRandomFolder = function() {
            LoadingIndicatorService.startLoading();

            $log.info("Play random folder");
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
    }
})();
