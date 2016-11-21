(function() {
    'use strict';

    angular
        .module('app')
        .controller('DashboardController', DashboardController);

    DashboardController.$inject = ['$interval', '$http', '$log', 'LoadingIndicatorService'];

    function DashboardController($interval, $http, $log, LoadingIndicatorService) {
        var vm = this;

        vm.breadcrumb = [];
        vm.alerts = [];
        vm.selectBreadcrumb = selectBreadcrumb;
        vm.detailsTitle = detailsTitle;
        vm.playCurrentFolder = playCurrentFolder;
        vm.playRandomFolder = playRandomFolder;
        vm.refreshCache = refreshCache;
        vm.navigateDown = navigateDown;
        vm.navigateUp = navigateUp;
        vm.closeAlert = closeAlert;

        vm.navigateDown("0:0");

        vm.refreshCacheStatusAlert = null;

        $interval(function () {
            updateRefreshCacheStatus();
        }, 1500);

        function updateRefreshCacheStatusAlert(messsage) {
            if (vm.refreshCacheStatusAlert == null) {
                vm.refreshCacheStatusAlert = {type: 'info', msg: 'Refresh cache: ' + messsage};
                vm.alerts.push(vm.refreshCacheStatusAlert);
            } else {
                vm.refreshCacheStatusAlert.msg = 'Refresh cache: ' + messsage;
            }
        }

        function updateRefreshCacheStatus() {
            $http({
                method: 'GET', url: 'api/refresh-cache'
            }).then(function successCallback(response) {

                if (response.data && response.data.message) {
                    updateRefreshCacheStatusAlert(response.data.message);
                } else {
                    closeAlert(vm.alerts.indexOf(vm.updateCacheStatusAlert));
                    vm.refreshCacheStatusAlert = null;
                }

            }, function errorCallback(response) {
                $log.error(angular.toJson(response));
            });
        }

        function closeAlert(index) {
            if (index > -1) {
                vm.alerts.splice(index, 1);
            }
        }

        function navigateUp() {
            var current = vm.breadcrumb.pop();
            var parentOfCurrent = vm.breadcrumb.pop();
            vm.navigateDown(parentOfCurrent.id);
        }

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

        function selectBreadcrumb(item) {
            while (vm.breadcrumb[vm.breadcrumb.length-1].id != item.id) {
                vm.breadcrumb.pop();
            }
            var parentOfCurrent = vm.breadcrumb.pop();
            vm.navigateDown(parentOfCurrent.id);
        }

        function detailsTitle() {
            var result = "-";
            if (vm.itemsInSelectedFolder && vm.itemsInSelectedFolder.length > 0) {
                result = vm.breadcrumb[vm.breadcrumb.length-1].title;
            }
            return result;
        }

        function navigateDown(folderId) {
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
        }

        function playCurrentFolder() {
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
        }

        function playRandomFolder() {
            LoadingIndicatorService.startLoading();

            $log.info("Play random folder");

            $http({
                method: 'POST', url: 'api/play-random-folder'
            }).then(function successCallback(response) {
                vm.alerts.push({type: 'success', msg: response.data.message});
                LoadingIndicatorService.stopLoading();
            }, function errorCallback(response) {
                LoadingIndicatorService.stopLoading();
                $log.error(angular.toJson(response));
            });
        }

        function refreshCache() {
            LoadingIndicatorService.startLoading();

            $log.info("Refresh cache");
            $http({
                method: 'POST', url: 'api/refresh-cache'
            }).then(function successCallback(response) {
                LoadingIndicatorService.stopLoading();
            }, function errorCallback(response) {
                LoadingIndicatorService.stopLoading();
                $log.error(angular.toJson(response));
            });
        }
    }
})();
