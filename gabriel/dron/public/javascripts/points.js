// Global Variables
var points = [], destinationPoints = [], maxRate = 30, stepRate = 5;

// Register Slider
var slider = document.getElementById("rateSlider");
var output = document.getElementById("rateSpan");
var rate = slider.value;
output.innerHTML = slider.value;
points = discretize_map(slider.value);
slider.oninput = function () {
    clearDestinations();
    clearPaths();
    output.innerHTML = this.value;
    rate = this.value;
    points = discretize_map(this.value);
    injectAndDrawOrigins();    
    
}

function isDroneSelected(){
    return drone_selected != -1;
}
function isPointABarrier(point) {
    return points[point.yi][point.xi] == 0;
}
function isPointADestination(point) {
    return droneArray.findIndex(function(d){
        return d.destination && d.destination[0] == point.xi 
        && d.destination[1] == point.yi;
    }) != -1;
}
function hasDroneDestination(drone) {
    return !!droneArray[drone]['destination'];
}
function isDroneDestination(point) {
    var d = droneArray[drone_selected]["destination"];
    return  d && d[0] == point.xi && d[1] == point.yi;
}
//** Handle clicking of point */
function handlePointClick(d, i) {
    var point = this;
    var point_svg = d3.select(this);

    // Select trip destination 
    if (isDroneSelected()) {
        if (isPointABarrier(point)) {
            return;
        }
        // Forbid destination if is already a destination for other dron
        if (isPointADestination(point) && !isDroneDestination(point)){
            return;
        }
        if(hasDroneDestination(drone_selected)){
            // Remove previous destination
            var prev = droneArray[drone_selected].destination;
            d3.selectAll("circle").filter(function(d,i){
                return this.xi == prev[0] && this.yi == prev[1];
            }).style('fill', 'gray');
            d3.selectAll("text").filter(function(d, i){
                return this.innerHTML === String(drone_selected) && !this.is_origin
            }).remove();
            if (isDroneDestination(point)){
                droneArray[drone_selected]["destination"] = null;
                return;
            } 
        }

        // Set Destination
        point_svg.style('fill', 'green');
        d3.select("svg").append("text").text(drone_selected).attr("x", point.xpixel+7).attr("y", point.ypixel);
        droneArray[drone_selected]["destination"] = [point.xi, point.yi];
        
    } else { // Select Barrier
        // Cannot set destination as barrier
        if (isPointADestination(point)) {
            return;
        }

        // Deselecting 
        if (isPointABarrier(point)) {
            point_svg.style('fill', 'gray');
            points[this.yi][this.xi] = 1;
            return;
        }

        point_svg.style('fill', 'red');
        points[this.yi][this.xi] = 0;
    }
}

//** Draw and store matrix of points */
function discretize_map(rate) {
    // Remove previous points
    d3.selectAll("circle").remove();

    // Local vars
    var points = [], pointsy = [];
    var stepW = Math.floor(canvas_size.w / rate);
    var stepH = Math.floor(canvas_size.h / rate);
    rate = rate || stepRate;

    // Build points matrix and draw points
    for (var y = stepH; y <= canvas_size.h - stepH; y += stepH) {
        for (var x = stepW; x <= canvas_size.w - stepW; x += stepW) {
            pointsy.push(1);
            d3.select("svg")
                .append("circle")
                .attr('cx', x)
                .attr('cy', y)
                .attr('r', maxRate / rate + 1)
                .attr('fill', 'grey')
                .property('xi', x / stepW - 1)
                .property('xpixel', x)
                .property('yi', y / stepH - 1)
                .property('ypixel', y)
                .on('click', handlePointClick);
        }
        points.push(pointsy);
        pointsy = [];
    }
    return points;
}

//** Convert index in points matrix to pixel values */
function indexToPixelValues(x, y) {
    var rate = output.innerHTML;
    var stepW = Math.floor(canvas_size.w / rate);
    var stepH = Math.floor(canvas_size.h / rate);
    return [stepW * (x + 1), stepH * (y + 1)];
}
function indexToPixelX(x) {
    var rate = output.innerHTML;
    var stepW = Math.floor(canvas_size.w / rate);
    return stepW * (x + 1);
}
function indexToPixelY(y) {
    var rate = output.innerHTML;
    var stepH = Math.floor(canvas_size.h / rate);
    return stepH * (y + 1);
}
function pixelToIndexValues(xpixel, ypixel) {
    var rate = output.innerHTML;
    var stepW = Math.floor(canvas_size.w / rate);
    var stepH = Math.floor(canvas_size.h / rate);
    return [xpixel / stepW, ypixel / stepH]
}
function pixel2IndexX(xpixel) {
    var rate = output.innerHTML;
    var stepW = Math.floor(canvas_size.w / rate);
    return Math.floor(xpixel / stepW);
}
function pixel2IndexY(ypixel) {
    var rate = output.innerHTML;
    var stepH = Math.floor(canvas_size.h / rate);
    return Math.floor(ypixel / stepH);
}

function reopenOrigins(){
    droneArray.forEach(function(d){
        if (d.origin_pixel) {
            var x = pixel2IndexX(d.origin_pixel[0]);
            var y = pixel2IndexY(d.origin_pixel[1]);
            points[x][y] = 1;
            return;
        }        
    });
}