/*var droneArray = [
    { "uid": 0, "height": 50, "origin_pixel": [300, 300] },
    { "uid": 1, "height": 50, "origin_pixel": [125, 250] },
    { "uid": 2, "height": 50, "origin_pixel": [35, 35] },
    { "uid": 3, "height": 50, "origin_pixel": [340, 340] }];
*/
var droneArray = [];
var drone_size = {
    w: 30, //Math.floor(canvas_size.w / 20) > 30 ? 30 : Math.floor(canvas_size.w / 20),
    h: this.w
};

var hScale = d3.scaleLinear()
    .domain([0, 100])
    .range([0.7, 1]);

var drone_selected = -1;

// Data preparing
function injectAndDrawOrigins() {
    clearOrigins();

    droneArray = droneArray.map(function (d) {
        if (!d.origin_pixel){
            d.origin_pixel = [gps2pixelX(d.lng), gps2pixelY(d.lat)];
        }
        var pickup = closestFreePoint(d.origin_pixel)
        if (pickup == -1) {
            d.status = 'ERROR';
        } else {
            d.pickup = pickup;
            d3.selectAll("circle")
                .filter(function (d) {
                    return this.xi == pickup[0] && this.yi == pickup[1];
                })
                .style("fill", "orange")
                .property("is_origin", true);

            let pickup_pixel = indexToPixelValues(pickup[0], pickup[1]);
            d3.select("svg").append("text")
                .text(d.uid)
                .property("is_origin", true)
                .attr("x", pickup_pixel[0] + 7)
                .attr("y", pickup_pixel[1]);

        }
        return d;
    });
}

function closestFreePoint(point) {
    var stepW = Math.floor(canvas_size.w / rate);
    var stepH = Math.floor(canvas_size.h / rate);

    var i = Math.floor(point[0] / stepW);
    var j = Math.floor(point[1] / stepH)
    var p;

    if (points[i][j]) p = [i, j];
    else if (points[i][j + 1]) p = [i, j + 1];
    else if (points[i + 1][j]) p = [i + 1, j];
    else if (points[i + 1][j + 1]) p = [i + 1, j + 1];
    else return -1;

    points[p[0]][p[1]] = 0;
    return p;
}

injectAndDrawOrigins();

var drones = d3.select("svg").selectAll("image");
var legend_cards = d3.select("#legend").select("div.container").select("div.row");

// Draw Drones
function drawDrones() {
    drones
        .data(droneArray)
        .enter()
        .append("svg:image")
        .attr("xlink:href", function (d) {
            return d.status == 'ERROR' ? "svgs/dron_error.svg" : "svgs/dron.svg";
        })
        .attr("x", function (d) { return d.pixel[0]; })
        .attr("y", function (d) { return d.pixel[1]; })
        .property("is_selected", false)
        .property("drone_id", function (d, i) { return d.uid; })
        .attr("width", function (d) { return hScale(d.h) * drone_size.w })
        .on("mousedown", handleDronClick)
}


// Draw drone stats in legend
function drawCards() {
    var card = legend_cards
        .selectAll("div").attr("class", "col")
        .data(droneArray)
        .enter()
        .append("div").attr("class", "card")
        .append("div").attr("class", "card-body")
        .property("drone_id", function (d, i) { return d.uid; })
        .style("background", function (d, i) {
            if (d.status === 'ERROR') return "red";
        });
    card.on("click", handleDronCardClick)
    card.append("h5").attr("class", "card-title").text(function (d, i) { return "Drone " + i })
    card.append("p").attr("class", "card-text").text(function (d) { return "X,Y: " + d.pixel[0] + ", " + d.pixel[1] })
    card.append("p").attr("class", "card-text").text(function (d) { return "Height: " + d.h + "m" })
}

drawDrones();
drawCards();

function update_data(time) {
    clearOrigins();
    clearDrones();
    clearCards();

    /*
    time = time || 750;
    d3.selectAll("image")
        .transition()
        .duration(time)
        .ease(d3.easeLinear)
        .attr("x", function (d) { return d.origin_pixel[0]; })
        .attr("y", function (d) { return d.origin_pixel[1]; }) */

    drawDrones();
    drawCards();
    reopenOrigins();
    injectAndDrawOrigins();
}

function handleDronClick(d, i) {
    if (d.status === 'ERROR') return;
    handleDronSelection(this.drone_id);
}

function handleDronCardClick(d, i) {
    if (d.status === 'ERROR') return;
    handleDronSelection(this.drone_id);
}

function handleDronSelection(id) {
    // Remove all selections if selected twice
    if (id == drone_selected) {
        drone_selected = -1;

        legend_cards.selectAll(".card-body")
            .classed("selected_card", false);

        d3.selectAll("image")
            .attr("xlink:href", function (d) {
                return d.status == 'ERROR' ? "svgs/dron_error.svg" : "svgs/dron.svg";
            }).property("is_selected", false)
        return;
    }

    // Set drone selected
    drone_selected = id;

    // Select drone card from Legend
    d3.select("#legend").select("div.container").select("div.row").selectAll(".card-body")
        .classed("selected_card", false)
        .filter(function (c, i) {
            return i == id;
        })
        .classed("selected_card", true);

    // Remove all other selections
    d3.selectAll("image")
        .attr("xlink:href", function (d) {
            return d.status == 'ERROR' ? "svgs/dron_error.svg" : "svgs/dron.svg";
        })
        .property("is_selected", false)
        .filter(function (c, i) {
            return i == id;
        })
        .attr("xlink:href", "svgs/dron_selected.svg").property("is_selected", true);
}


var socket = io('ws://localhost:3000/ui');
socket.on('disconnect', function () { console.log('disconnected') });
socket.on('connect', function () { console.log('connected') });

socket.on("new_drone", function (data) {
    console.log(data);
});
socket.on("new_position", function (data) {
    console.log(data);
});
socket.on("new_drone_array", function (arr) {
    if (!arr) return;
    droneArray = arr.map(function (d, i) {
        d.F = [Math.floor(gps2pixelX(d.origin.lng)), Math.floor(gps2pixelY(d.origin.lat))];
        d.pixel = [Math.floor(gps2pixelX(d.lng)), Math.floor(gps2pixelY(d.lat))];
        d.h = d.h || 50;
        d.uid = d.uid || i;
        d.uuid = d.uuid || i;
        return d
    });
    update_data();
});

d3.select("#btnLock")
    .on("click", function (c, i) {
        socket.emit('lock_layout');
    });

d3.select("#btnGo")
    .on("click", function (c, i) {
        socket.emit('go', droneArray);
    });