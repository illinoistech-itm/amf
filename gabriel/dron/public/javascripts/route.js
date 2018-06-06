d3.select("#btnCompute")
    .on("click", function (c, i) {
        // Clear Any Drawings for Paths
        clearPaths();

        // Calculate individual paths
        computePaths();

        // Draw Paths
        drawPaths();

        // Avoid Collisions
        syncronizePaths();

        // Translate to GPS
        injectGPSPaths();
    });

var easystar = new EasyStar.js();

function computePaths(done) {
    for (var i = 0; i < droneArray.length; i++) {
        var drone = droneArray[i];
        if (!drone.destination) continue;
        var ix = drone.pickup[0];
        var iy = drone.pickup[1];

        var fx = drone.destination[0];
        var fy = drone.destination[1];

        points[ix][iy] = 1;
        easystar.setGrid(points);
        points[ix][iy] = 0;
        easystar.setAcceptableTiles([1]);
        //easystar.enableDiagonals();
        easystar.enableSync();

        //console.log(easystar.findPath(ix, iy, fx, fy));
        easystar.findPath(ix, iy, fx, fy, function (path) {
            if (path === null) {
                alert("Path was not found.");
            } else {
                path = path.map(function (p, i) {
                    p.h = drone_standard.height;
                    return p;
                })
                droneArray = droneArray.map(function (d) {
                    if (d.pickup[0] == path[0].x && d.pickup[1] == path[0].y) {
                        d.path = path;
                    }
                    return d;
                });
            }
        });
        easystar.calculate();
    }
}

function drawPaths() {
    droneArray.forEach(function (d) {
        if (d.path) {
            drawPath(d.path);
        }
    });
}
function drawPath(path) {
    var sline = d3.line()
        .x(function (d, i) { return indexToPixelX(d.x); })
        .y(function (d, i) { return indexToPixelY(d.y); })

    d3.select("svg").append("path")
        .attr("class", "line")
        .attr("d", sline(path))
        .style("fill", "none")
        .style("stroke", "black")
        .style("stroke-width", 2);
}

function isEqualPoint(p1, p2) {
    return p1.x === p2.x && p1.y === p2.y && p1.h === p2.h;
}
function increasePreviousAndCurrentHeight(i, j, t) {
    var ti = t < droneArray[i].path.length ? t : droneArray[i].path.length - 1;
    var tj = t < droneArray[j].path.length ? t : droneArray[j].path.length - 1;
    var h = droneArray[i].path[ti].h;
    if (ti > 1) {
        h = Math.max(h, droneArray[i].path[ti - 1].h);
    }
    if (tj >= 1) {
        droneArray[j].path[tj - 1].h = h + drone_standard.height_step;
    }
    droneArray[j].path[tj].h = h + drone_standard.height_step;

    // Check for lower priority paths at t
    if (j + 1 > droneArray.length - 1 || !droneArray[j + 1].path) return; // No more drones
    var lowP1 = droneArray[j].path[tj];
    var tj2 = tj < droneArray[j + 1].path.length ? tj : droneArray[j + 1].path.length - 1;
    var lowwP2 = droneArray[j + 1].path[tj2];

    if (isEqualPoint(lowP1, lowwP2)) {
        increasePreviousAndCurrentHeight(j, j + 1, t);
    }
    // Check for lower priority paths at t - 1
    var lowP1Prev = droneArray[j].path[tj - 1];
    var tj2Prev = tj < droneArray[j + 1].path.length ? tj - 1 : droneArray[j + 1].path.length - 1;
    var lowwP2Prev = droneArray[j + 1].path[tj2Prev];
    if (isEqualPoint(lowP1Prev, lowwP2Prev)) {
        increasePreviousAndCurrentHeight(j, j + 1, t - 1);
    }
}
function decreaseNextHeightToStandard(j, t) {
    if (t + 1 < droneArray[j].path.length) {
        droneArray[j].path[t + 1].h = drone_standard.height;
    }
}
function increaseFrontalCollisionHeight(i, j, t) {
    var ti = t < droneArray[i].path.length ? t : droneArray[i].path.length - 1;
    if (t >= 1) {
        droneArray[j].path[t - 1].h = droneArray[i].path[ti].h + drone_standard.height_step;
    }
    droneArray[j].path[t].h = droneArray[i].path[ti].h + drone_standard.height_step;
    if (t + 1 < droneArray[j].path.length - 1) {
        droneArray[j].path[t + 1].h = droneArray[i].path[ti].h + drone_standard.height_step;
    }
}
function syncronizePaths() {
    setFirstHeight();
    var t = 0, maxArr = 0;
    while (t <= maxArr) {
        for (var i = 0; i < droneArray.length; i++) {
            var path1 = droneArray[i].path;
            if (!path1) continue;
            if (path1.length > maxArr) maxArr = droneArray[i].path.length;
            for (var j = i + 1; j < droneArray.length; j++) {
                var path2 = droneArray[j].path;
                if (!path2) continue;
                if (path2.length > maxArr) maxArr = droneArray[j].path.length;

                var t1 = t > path1.length - 1 ? path1.length - 1 : t;
                var t2 = t > path2.length - 1 ? path2.length - 1 : t;
                var p1 = path1[t1];
                var p2 = path2[t2];
                var p1Next = t + 1 > path1.length - 1 ? path1[path1.length - 1] : path1[t + 1];
                var p2Next = t + 1 > path2.length - 1 ? path2[path2.length - 1] : path2[t + 1];
                if (isEqualPoint(p1, p2)) { // Side cross
                    increasePreviousAndCurrentHeight(i, j, t2);
                    decreaseNextHeightToStandard(j, t2);
                } else if (isEqualPoint(p1, p2Next) && isEqualPoint(p2, p1Next)) { // Frontal crossing
                    increaseFrontalCollisionHeight(i, j, t2);
                    decreaseNextHeightToStandard(j, t2 + 1 > path2.length - 1 ? t2 : t2 + 1);
                }
            }
        }
        t += 1;
    }
}

function setFirstHeight() {
    droneArray = droneArray.map(function (el, i) {
        if (el.path && el.path.length > 0) {
            el.path[0].h = drone_standard.height + i * drone_standard.height_step;
        }
        return el;
    })
}

function injectGPSPaths() {
    droneArray = droneArray.map(function(e) {
        if (e.path) {
            e.gpsPath = [];
            e.path.forEach(function(p){
                var o = {};
                o.lng = gps2pixelX.invert(indexToPixelX(p.x));
                o.lat = gps2pixelY.invert(indexToPixelY(p.y));
                o.h = p.h
                e.gpsPath.push(o);
            })
        }
        return e;
    })
}