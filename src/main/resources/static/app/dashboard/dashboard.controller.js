(function() {
    'use strict';

    angular
        .module('app')
        .controller('DashboardController', DashboardController);

    DashboardController.$inject = ['$http', '$log'];

    function DashboardController($http, $log) {
        var vm = this;

        loadTree();

        function loadTree() {
            $http({
                method: 'GET', url: 'api/folder'
            }).then(function successCallback(response) {
                vm.treedata = response.data;
            }, function errorCallback(response) {
                $log.error(angular.toJson(response));
            });
        }

        //vm.treedata =
        //    [
        //        { "title" : "User", "id" : "role1", "children" : [
        //            { "title" : "subUser1", "id" : "role11", "children" : [] },
        //            { "title" : "subUser2", "id" : "role12", "children" : [
        //                { "title" : "subUser2-1", "id" : "role121", "children" : [
        //                    { "title" : "subUser2-1-1", "id" : "role1211", "children" : [] },
        //                    { "title" : "subUser2-1-2", "id" : "role1212", "children" : [] }
        //                ]}
        //            ]}
        //        ]},
        //        { "title" : "Admin", "id" : "role2", "children" : [] },
        //        { "title" : "Guest", "id" : "role3", "children" : [] }
        //    ];


    }
})();

