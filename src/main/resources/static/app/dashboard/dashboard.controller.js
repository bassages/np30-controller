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
        vm.skipPrevious = skipPrevious;
        vm.skipNext = skipNext;
        vm.getPlaybackDetails = getPlaybackDetails;

        vm.navigateDown("0:0");

        vm.refreshCacheStatusAlert = null;
        vm.playbackDetailsAlert = null;

        $interval(function () {
            updateRefreshCacheStatus();
        }, 1500);

        $interval(function () {
            getPlaybackDetails();
        }, 1000);

        function updateRefreshCacheStatusAlert(messsage) {
            if (vm.refreshCacheStatusAlert == null) {
                vm.refreshCacheStatusAlert = {type: 'info', msg: 'Refresh cache: ' + messsage};
                vm.alerts.push(vm.refreshCacheStatusAlert);
            } else {
                vm.refreshCacheStatusAlert.msg = 'Refresh cache: ' + messsage;
            }
        }

        function updatePlaybackDetailsAlert(artist, title, album) {
            vm.playbackDetailsAlert = {
                artist: artist,
                title: title,
                album: album
            };
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

        function getPlaybackDetails() {
            $http({
                method: 'GET', url: 'api/playback-details'
            }).then(function successCallback(response) {
                if (response.data && response.data) {
                    updatePlaybackDetailsAlert(response.data.artist, response.data.title, response.data.album);
                } else {
                    closeAlert(vm.alerts.indexOf(vm.playbackDetailsAlert));
                    vm.playbackDetailsAlert = null;
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

        function getContent(items, onlyFolders) {
            var folders = [];
            for (var key in items) {
                var item = items[key];
                if (item.container === onlyFolders) {
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

                vm.foldersInSelectedFolder = getContent(response.data.children, true);
                vm.itemsInSelectedFolder = getContent(response.data.children, false);

                if (vm.foldersInSelectedFolder.length === 0 && vm.itemsInSelectedFolder.length === 0) {
                    vm.itemsInSelectedFolder = [{title: '<This folder is empty>'}];
                }

                LoadingIndicatorService.stopLoading();
            }, function errorCallback(response) {
                LoadingIndicatorService.stopLoading();
                $log.error(angular.toJson(response));
            });
        }

        function skipPrevious() {
            $log.info("Skip previous");
            $http({
                method: 'POST', url: 'api/skip-previous'
            }).then(function successCallback(response) {
                LoadingIndicatorService.stopLoading();
            }, function errorCallback(response) {
                LoadingIndicatorService.stopLoading();
                $log.error(angular.toJson(response));
            });
        }

        function skipNext() {
            $log.info("Skip next");
            $http({
                method: 'POST', url: 'api/skip-next'
            }).then(function successCallback(response) {
                LoadingIndicatorService.stopLoading();
            }, function errorCallback(response) {
                LoadingIndicatorService.stopLoading();
                $log.error(angular.toJson(response));
            });
        }

        function playCurrentFolder() {
            LoadingIndicatorService.startLoading();

            const folderId = vm.breadcrumb[vm.breadcrumb.length-1].id;

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
                $log.info(response.data.message);
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
