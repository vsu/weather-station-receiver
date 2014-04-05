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
    history: "history",
    humidity: "humidity",
    sensor0: "sensor0",
    sensor1: "sensor1",
    sensor2: "sensor2",
    sensor3: "sensor3",
    sensor4: "sensor4",
    temperature: "temperature"
};

var WS_PATH = "/ws";

var _historyData = [];

function _sendQuery(type) {
    var data = {};
    data[KEY.op] = OP.query;
    data[KEY.type] = type;

    _socket.push(JSON.stringify(data));
}

function _getTempC(temperature) {
    return temperature / 10;
}

function _getTempF(temperature) {
    return Math.round(10 * ((temperature * 0.18) + 32)) / 10;
}

function _readShortBE(data, offset) {
    return data[offset] * 256 + data[offset + 1];
}

function _readIntBE(data, offset) {
    return data[offset] * 16777216 +
        data[offset + 1] * 65536 +
        data[offset + 2] * 256 +
        data[offset + 3];
}

function _readLongBE(data, offset) {
    var high = _readIntBE(data, offset);
    var low = _readIntBE(data, offset + 4);
    return (high * 4294967296) + low;
}

function _renderChart() {
   if (_historyData.length > 0) {
        $("#chart").show();
        $("#noChartData").hide();

        nv.addGraph(function() {
            var chart = nv.models.lineChart()
                .useInteractiveGuideline(true)
                .transitionDuration(350)
                .showLegend(true)
                .showYAxis(true)
                .showXAxis(true);

            chart.xAxis.tickFormat(function(d) { return d3.time.format("%X") (new Date(d)); });

            var temperature = _.chain(_historyData)
                .map(function (item) {
                    var dateTime = moment(item.dateTime);
                    return { x: dateTime.toDate(), y: _getTempF(item.temperature) };
                })
                .sortBy(function (item) { return item.x.getTime(); })
                .value();

            var humidity = _.chain(_historyData)
                .map(function (item) {
                    var dateTime = moment(item.dateTime);
                    return { x: dateTime.toDate(), y: (item.humidity) };
                })
                .sortBy(function (item) { return item.x.getTime(); })
                .value();

            var dataSet = [];
            dataSet.push({ key: "Temperature °F", values: temperature});
            dataSet.push({ key: "Humidity %", values: humidity});

            var svg = d3.select("#chart svg")
                .datum(dataSet)
                .call(chart);

            nv.utils.windowResize(function() { chart.update() });

            return chart;
        });
    }
    else {
        $("#chart").hide();
        $("#noChartData").show();
    }
}

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

            // determine the message type
            if (evt.data instanceof Blob) {
                // convert the blob into a uint8 array
                var fileReader = new FileReader();

                fileReader.onload = function(e) {
                    _historyData = [];

                    var data = new Uint8Array(this.result);

                    // each sample is 11 bytes, so the length must be divisible by that
                    if (data.length % 11 == 0) {
                        for (var ix = 0; ix < data.length; ix = ix + 11) {
                            var item = {};

                            item[KEY.dateTime] = _readLongBE(data, ix);
                            item[TYPE.temperature] = _readShortBE(data, ix + 8);
                            item[TYPE.humidity] = data[ix + 10];

                            _historyData.push(item);
                        }

                        _renderChart();
                    }
                };

                fileReader.readAsArrayBuffer(evt.data);

            }
            else if (typeof evt.data === "string") {
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
                            var tempCelsius = _getTempC(temperature);
                            var tempFahrenheit = _getTempF(temperature);
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

                    if (_(data).has(KEY.dateTime) &&
                        _(data).has(TYPE.temperature) &&
                        _(data).has(TYPE.humidity)) {

                        var item = {};
                        item[KEY.dateTime] = parseInt(data[KEY.dateTime]);
                        item[TYPE.temperature] = parseInt(data[TYPE.temperature]);
                        item[TYPE.humidity] = parseInt(data[TYPE.humidity]);

                        _historyData.push(item);
                        _renderChart();
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

    _renderChart();
})