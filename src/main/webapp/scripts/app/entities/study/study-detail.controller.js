'use strict';

angular.module('synctestApp')
    .controller('StudyDetailController', function ($scope, $stateParams, Study) {
        $scope.study = {};
        $scope.load = function (id) {
            Study.get({id: id}, function(result) {
              $scope.study = result;
            });
        };
        $scope.load($stateParams.id);
    });
