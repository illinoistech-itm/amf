var canvas_size = {
    w: 600,
    h: 600
};

var gps2pixelX = d3.scaleLinear()
    .domain([-87.625590, -87.622490])
    .range([0, 600]);

var gps2pixelY = d3.scaleLinear()
    .domain([41.840299, 41.838009])
    .range([0, 600]);

var drone_standard = {
    height: 50,
    height_step: 5
};

var svg = d3.select("#canvas").append("svg")
    .attr("width", canvas_size.w)
    .attr("height", canvas_size.h)
    .style("background-color", "#ddd");

function clearPaths(){
    d3.selectAll("path").remove();
}

function clearOrigins(){
    d3.selectAll("text")
    .filter(function (d) {
        return this.is_origin;
    }).remove();

    d3.selectAll("circle")
    .filter(function (d) {
        return this.is_origin;
    })
    .style('fill', 'gray');
}

function clearDestinations(){
    d3.selectAll("text")
    .filter(function (d) {
        return !this.is_origin;
    }).remove();

    d3.selectAll("circle")
    .filter(function (d) {
        return !this.is_origin;
    })
    .remove();
}

function clearDrones() {
    d3.selectAll("image").remove();
}

function clearCards() {
    d3.select("#legend").select("div.container").select("div.row").selectAll("div").remove();
}