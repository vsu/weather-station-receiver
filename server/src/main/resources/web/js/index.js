var _socket;

var OP = {
    query: "query",
    reset: "reset"
};

var RESULT = {
    error: "error",
    ok: "ok"
};

var KEY = {
    dateTime: "dateTime",
    op: "op",
    result: "result",
    type: "type"
};

var TYPE = {
    humidity: "humidity",
    sensor0: "sensor0",
    sensor1: "sensor1",
    sensor2: "sensor2",
    sensor3: "sensor3",
    sensor4: "sensor4",
    temperature: "temperature"
};

var WS_PATH = "/ws";

function _sendQuery(type) {
    var data = {};
    data[KEY.op] = OP.query;
    data[KEY.type] = type;

    _socket.push(JSON.stringify(data));
}

/*
function _connectWebSocket(onOpen) {
    var pageUri = new URI(window.location.href);
    var hostname = pageUri.hostname();
    var port = pageUri.port();
    var socketUri = "ws://" + hostname + (port != "" ? ":" + port : "") + WS_PATH;

    var request = {
        url: socketUri,
        contentType : "application/json",
        transport : "websocket"
    };

    request.onOpen = function(response) {
        if (!_.isUndefined(onOpen)) {
            onOpen();
        }
    };

    request.onMessage = function (response) {
        if (response.status == 200) {
            var body = response.responseBody.split("|");

            if (body.length == 2) {
                var data = JSON.parse(body[1]);

                // ignore if the query request is echoed back
                if (_(data).has(KEY.op)) {
                    return;
                }

                if (_(data).has(KEY.result) && (data.result == RESULT.ok)) {
                    if (_(data).has(KEY.dateTime)) {
                        var date = moment(data[KEY.dateTime]);
                        var html = "Last updated:&nbsp;" + date.format("llll");
                        $("#spanDateTime").html(html);
                    }

                    if (_(data).has(TYPE.temperature)) {
                        if (data[TYPE.temperature] != -1) {
                            var temperature = data[TYPE.temperature];
                            var tempCelsius = temperature / 10;
                            var tempFahrenheit = Math.round(10 * ((temperature * 0.18) + 32)) / 10;
                            var html = tempFahrenheit + "&nbsp;&deg;F&nbsp;(" + tempCelsius + "&nbsp;&deg;C)";

                            $("#spanTemperature").html(html);
                        }
                        else {
                            $("#spanTemperature").html("");
                        }
                    }

                    if (_(data).has(TYPE.humidity)) {
                        if (data[TYPE.humidity] != -1) {
                            $("#spanHumidity").html(data[TYPE.humidity] + "%");
                        }
                        else {
                            $("#spanHumidity").html("");
                        }
                    }

                    if (_(data).has(TYPE.sensor0)) {
                        $("#spanSensor0").text((data[TYPE.sensor0] != -1) ? data[TYPE.sensor0] : "");
                    }

                    if (_(data).has(TYPE.sensor1)) {
                        $("#spanSensor1").text((data[TYPE.sensor1] != -1) ? data[TYPE.sensor1] : "");
                    }

                    if (_(data).has(TYPE.sensor2)) {
                        $("#spanSensor2").text((data[TYPE.sensor2] != -1) ? data[TYPE.sensor2] : "");
                    }

                    if (_(data).has(TYPE.sensor3)) {
                        $("#spanSensor3").text((data[TYPE.sensor3] != -1) ? data[TYPE.sensor3] : "");
                    }

                    if (_(data).has(TYPE.sensor4)) {
                        $("#spanSensor4").text((data[TYPE.sensor4] != -1) ? data[TYPE.sensor4] : "");
                    }

                    return;
                }
            }
        }

        $("#spanTemperature").text("");
        $("#spanHumidity").text("");
        $("#spanSensor0").text("");
        $("#spanSensor1").text("");
        $("#spanSensor2").text("");
        $("#spanSensor3").text("");
        $("#spanSensor4").text("");
    };

    request.onClose = function(response) {
    }

    request.onError = function(response) {
        alert("Unable to connect to the web server.");
    };

    _socket = atmosphere.subscribe(request);
}
*/

function _sendQuery(type) {
    var request = {};
    request[KEY.op] = OP.query;
    request[KEY.type] = type;

    if (_socket != null && _socket.readyState == WebSocket.OPEN) {
        _socket.send(JSON.stringify(request));
    }
    else {
        _connectWebSocket(function () {
            _socket.onerror = function (evt) {
                alert("The connection to the web server was lost.");
            };

            _socket.send(JSON.stringify(request));
        });
    }
}

function _connectWebSocket(onOpen) {
    var pageUri = new URI(window.location.href);
    var hostname = pageUri.hostname();
    var port = pageUri.port();
    var socketUri = "ws://" + hostname + (port != "" ? ":" + port : "") + WS_PATH;

    _socket = new WebSocket(socketUri);

    _socket.onopen = function (evt) {
        if (!_.isUndefined(onOpen)) {
            onOpen();
        }
    };

    _socket.onclose = function (evt) {
        alert("The connection to the web server has been closed.");
        _socket = null;
    };

    _socket.onerror = function (evt) {
        //alert("Unable to connect to the web server.");
    };

    _socket.onmessage = function (evt) {
        if (!_.isUndefined(evt.data) && evt.data != null) {

            var data = JSON.parse(evt.data);

            if (_(data).has(KEY.result) && (data.result == RESULT.ok)) {
                if (_(data).has(KEY.dateTime)) {
                    var date = moment(data[KEY.dateTime]);
                    var html = "Last updated:&nbsp;" + date.format("llll");
                    $("#spanDateTime").html(html);
                }

                if (_(data).has(TYPE.temperature)) {
                    if (data[TYPE.temperature] != -1) {
                        var temperature = data[TYPE.temperature];
                        var tempCelsius = temperature / 10;
                        var tempFahrenheit = Math.round(10 * ((temperature * 0.18) + 32)) / 10;
                        var html = tempFahrenheit + "&nbsp;&deg;F&nbsp;(" + tempCelsius + "&nbsp;&deg;C)";

                        $("#spanTemperature").html(html);
                    }
                    else {
                        $("#spanTemperature").html("");
                    }
                }

                if (_(data).has(TYPE.humidity)) {
                    if (data[TYPE.humidity] != -1) {
                        $("#spanHumidity").html(data[TYPE.humidity] + "%");
                    }
                    else {
                        $("#spanHumidity").html("");
                    }
                }

                if (_(data).has(TYPE.sensor0)) {
                    $("#spanSensor0").text(data[TYPE.sensor0]);
                }

                if (_(data).has(TYPE.sensor1)) {
                    $("#spanSensor1").text(data[TYPE.sensor1]);
                }

                if (_(data).has(TYPE.sensor2)) {
                    $("#spanSensor2").text(data[TYPE.sensor2]);
                }

                if (_(data).has(TYPE.sensor3)) {
                    $("#spanSensor3").text(data[TYPE.sensor3]);
                }

                if (_(data).has(TYPE.sensor4)) {
                    $("#spanSensor4").text(data[TYPE.sensor4]);
                }

                return;
            }
        }
    };
}


$(document).ready(function () {
    _connectWebSocket(function () {
        _sendQuery(TYPE.temperature);
        _sendQuery(TYPE.sensor0);
        _sendQuery(TYPE.sensor1);
        _sendQuery(TYPE.sensor2);
        _sendQuery(TYPE.sensor3);
        _sendQuery(TYPE.sensor4);
    });

    $(".btn-sensor").on("click", function () {
        var type = $(this).attr("sensorType");
        _sendQuery(type);
    });

    $("#btnReset").on("click", function () {
        if (confirm ("Are you sure you want to reset the sensor?")) {
            var data = {};
            data[KEY.op] = OP.reset;

            _socket.push(JSON.stringify(data));
        }
    });
})