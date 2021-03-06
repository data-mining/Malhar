/*
 * Copyright (c) 2013 DataTorrent, Inc. ALL Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*global BigInteger, angular, _*/
(function () {
'use strict';

angular.module('widgets')
    .directive('widgetsStat', ['socket', function (socket) {
        return {
            restrict: 'A',
            templateUrl: 'views/stat.html',
            scope: {
                data: "=",
                label: "@",
                onClick: "&"
            },
            link: function($scope, iElement, iAttrs) {
                $scope.totalEmitted = 0;
                $scope.totalProcessed = 0;

                $scope.$watch('data', function (appId) {
                    if (appId) {
                        var topic = 'apps.' + appId + '.operators.list';

                        socket.subscribe(topic, function (message) {
                            var operators = message.data.operators;

                            var emitted = BigInteger.ZERO;
                            var processed = BigInteger.ZERO;
                            //var emitted = new BigInteger('9007199254740992');
                            _.each(operators, function (op) {
                                emitted = emitted.add(new BigInteger(op.tuplesEmittedPSMA10));
                                processed = processed.add(new BigInteger(op.totalTuplesProcessed));
                            });

                            $scope.totalEmitted = emitted.toString();
                            $scope.totalProcessed = processed.toString();
                            $scope.$apply();
                        });
                    }
                });
            }
        };
    }]);

})();
