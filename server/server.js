var express = require('express');
var pythonShell = require('python-shell');
var app = express();
var port = process.env.PORT || 8080;

var pyshell = new pythonShell('./hello.py');

var bodyParser = require('body-parser');
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended:true}));
app.listen(port);

app.get('/',function(req,res){
   res.send("Hello world");
});
console.log("Server Started at http://localhost:" + port);
app.post('/request',function(req,res){
  console.log("Request Received.");
  var latitude = req.body.latitude;
  var longitude = req.body.longitude;
  var address = req.body.address;
  res.send("response:1\n"+
  "Request Received! Sending drone to:\n"+address);

  var options = {
    mode: 'text',
    pythonPath: 'C:\\Python27\\python.exe',
    pythonOptions: ['-u'],
    scriptPath: './',
    args: ["com7",latitude,longitude, 1]
  };

  pythonShell.run('launch.py',options,function(err,results){
    if(err) throw err;
     console.log("Script Executed.");
  });

  // pyshell.on('message',function(message){
  //   console.log(message);
  // })
  // console.log(latitude + "," + longitude);
});
