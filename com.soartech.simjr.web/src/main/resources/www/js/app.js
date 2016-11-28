'use strict';

angular.module('SimJrApp', []).
    config(['$routeProvider', function($routeProvider) {
        $routeProvider.
            when('/entities/list', {templateUrl: 'partials/entity-list.html'}).
            otherwise({redirectTo: '/entities/list'});
}]);
