var createError = require('http-errors');
var express = require('express');
var path = require('path');
var cookieParser = require('cookie-parser');
var logger = require('morgan');
var socket_io = require("socket.io");
var DroneRepo = require('./repository/dron-repo');
var Drone = require('./models/dron');

var indexRouter = require('./routes/index');
var usersRouter = require('./routes/users');

var app = express();
var droneRepo = new DroneRepo();

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'jade');

app.use(logger('dev'));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

app.use('/', indexRouter);
//app.use('/users', usersRouter);

// Socket.io
var io = socket_io();
app.io = io;

io.of("/ui").on("connection", function (socket) {
  socket.on('lock_layout', function () {
    droneRepo.lock();
  });
  socket.on('go', function (droneArray) {
    droneRepo.goDrones(droneArray);
  });
  socket.emit('new_drone_array', droneRepo.getAllAsList())
});

var router = express.Router();

/* GET users listing. */
router.get('/', function(req, res, next) {
  res.json(droneRepo.getAllAsList());
});
router.get('/:uuid', function(req, res, next) {
  var uuid = req.params["uuid"];
  if (!uuid){
    res.status(404);
    res.json({error: "UUID not in URL"});
  }
  res.json(droneRepo.get(uuid));
});
router.get('/:uuid/path', function(req, res, next) {
  var uuid = req.params["uuid"];
  if (!uuid){
    res.status(404);
    res.json({error: "UUID not in URL"});
  }
  res.json(droneRepo.getPath(uuid));
});
router.get('/:uuid/period/:t', function(req, res, next) {
  var uuid = req.params["uuid"];
  var t = req.params["t"];
  if (!uuid){
    res.status(404);
    res.json({error: "UUID not in URL"});
  }
  if (!t){
    res.status(404);
    res.json({error: "Period not in URL"});
  }
  res.json(droneRepo.setPeriod(uuid, t));
});
router.put('/:uuid', function(req, res, next) {
  var uuid = req.params["uuid"];
  var dron = req.body;
  if (!dron){
    res.status(404);
    res.json({error: "Dron data not in body"});
    return;
  };
  if (!uuid){
    res.status(404);
    res.json({error: "UUID not in URL"});
    return;
  } 
  droneRepo.modify(uuid, dron)
  req.app.io.of("/ui").emit('new_drone_array', droneRepo.getAllAsList());
  res.json();
});
router.post('/', function(req, res, next) {
  var dron = req.body;
  if (!dron || !dron.uuid){
    res.status(404);
    res.json({error: "Dron data or UUID not in body"});
    return;
  }
  droneRepo.put(dron.uuid, dron);
  req.app.io.of("/ui").emit('new_drone_array', droneRepo.getAllAsList());
  res.json();
});

app.use('/dron', router);


// catch 404 and forward to error handler
app.use(function (req, res, next) {
  next(createError(404));
});

// error handler
app.use(function (err, req, res, next) {
  // set locals, only providing error in development
  res.locals.message = err.message;
  res.locals.error = req.app.get('env') === 'development' ? err : {};

  // render the error page
  res.status(err.status || 500);
  res.render('error');
});

module.exports = app;
