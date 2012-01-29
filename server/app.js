/**
 * URL Broadcaster
 *
 * @author Kyungwook, Park
 */

process.on('uncaughtException', function (err) {
    console.log('Caught exception: ' + err);
});

var express = require('express')
  , app = module.exports = express.createServer()
  , io = require('socket.io').listen(app)
  ;

// Configuration
app.configure(function(){
  app.set('views', __dirname + '/views');
  app.set('view engine', 'ejs');
  app.use(express.bodyParser());
  app.use(express.methodOverride());
  app.use(app.router);
  app.use(express.static(__dirname + '/public'));
});

app.configure('development', function(){
  app.use(express.errorHandler({ dumpExceptions: true, showStack: true })); 
});

app.configure('production', function(){
  app.use(express.errorHandler()); 
  io.enable('browser client minification');  // send minified client
  io.enable('browser client etag');          // apply etag caching logic based on version number
  io.enable('browser client gzip');          // gzip the file
});

// Devices
var user = {
    // user have multiple devices
    // User : Devices Object
    guest : {
        device : {}
    }
};

app.get('/', function(req,res){
	res.render('user', { userName : 'guest' , target : user['guest'].device });	
});

app.get('/api', function(req,res){
	res.render('api');
});

app.get('/:id', function(req,res){
	var userName = req.params.id || 'guest';
	if ( !user[userName] ) {
		user[userName] = {};
		user[userName].device = {};
	}
	res.render('user', { userName : userName , target : user[userName].device });	
});

app.listen(9998);

io.sockets.on('connection', function (socket) {
	/**
	 * Device Connected
	 */
	socket.on('deviceOn', function (data) {
		console.log('connecting',data.userName, data.uuid,data.devid,data.osver);
		if ( !user[data.userName] || !user[data.userName].device ) {
			user[data.userName] = {};
			user[data.userName].device = {};
		}

		if ( !user[data.userName].device[data.uuid] ) {
			user[data.userName].device[data.uuid] = {};
		}

		user[data.userName].device[data.uuid].devid = data.devid || '';
		user[data.userName].device[data.uuid].osver = data.osver || '';

		socket.broadcast.emit('deviceUpdateFor_'+data.userName, {userName : data.userName, target : user[data.userName].device} );
	});

	/**
	 * Disconnecting Device
	 */
	socket.on('deviceOff', function (data) {
		console.log('disconnecting',data.userName, data.uuid,data.devid,data.osver);

		// Remove Device Info
	        user[data.userName].device[data.uuid] = null;
        	delete user[data.userName].device[data.uuid];
        
		// Update device lists to the pages
		socket.broadcast.emit('deviceUpdateFor_'+data.userName, {userName : data.userName, target : user[data.userName].device} );
	});

	/**
	 * Send URL to the device
	 */
	socket.on('broadcastUrl', function (data) {
		if ( data ) {
			if ( data.url.indexOf('http://') == -1 ) {
				data.url = 'http://'+data.url;
			}

			// Valid URL Only
			if ( isValidUrl(data.url) ) {
				data.uuids.forEach(function(uuid){
					if ( !user[data.userName].device[uuid] ) {
						return ;
					}
					socket.broadcast.emit(data.userName, { uuid : uuid, url : data.url });
				});
			}
		}
	});
});

// URL Validation
function isValidUrl(url){
	return /^(http|https|ftp):\/\/(([A-Z0-9][A-Z0-9_-]*)(\.[A-Z0-9][A-Z0-9_-]*)+)(:(\d+))?\/?/i.test(url);
}

