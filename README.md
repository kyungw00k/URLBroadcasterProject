URL Broadcaster
=====

What is URL Broadcaster?
----
![Original icon][img-icon]

Don't type URL every single device!

Make easy to send a URL to the mobile devices. :D

If you still don't get it, [see the demo](http://vimeo.com/35988619 "Demo")

How To Use
----
<img src="https://github.com/kyungw00k/UrlBroadcaster/raw/master/client/android/res/drawable-hdpi/off.png" height="96" style="display:inline"/><img src="https://github.com/kyungw00k/UrlBroadcaster/raw/master/client/android/res/drawable-hdpi/signs.png" height="96" style="display:inline"/>

* Step 1. Install Client
 * Option 1 - [Install through Android Market](https://market.android.com/details?id=kyungw00k.UrlReceiverWidget "Market URL")
 * Option 2 - Build yourself
* Step 2. Configure Client
 * Touch the icon on the right side of the switch if you change the server url or user id.
* Step 3. Turn on the switch!
* Step 4. Visit Website(http://urlcoder.nodester.com/)
* Step 5. Send URL!

If you want to run your own server, checkout `server` and run command below.

	$ npm install .
	$ node app.js
	
Then, change the target server infomation in client app. That's all. :D
	
API
-----
### Client #
* deviceOn Event
	* Client send the device infomation to the server when socket has connected.

			{ username : '', uuid : '', devid : '', osver : '' }

* deviceOff Event
	* Client send the device infomation to the server before socket has disconnected.

			{ username : '', uuid : '', devid : '', osver : '' }

* `usename` Event
	* Server push the URL to the target device.

			{ url : '', uuid : '' }

### Server #

#### Routing #
* Frontpage

		/

* `username` page

		/:id

#### Socket.io #

* `deviceOn` / `deviceOff` Event
	* Get the device infomation from client, then broadcast to the specific user page.
	* When `deviceOff`` occurs, simply delete target device info from memory, not from db.
	* Broadcast deviceUpdateFor_`username` event

			{
				userName : '', 
				target : {
					/* connected device info */
					`uuid` : {
						devid : '' /* Model Id */
						osver : '' /* OS Version */
					}
				}
			}

* `broadcastUrl` Event
	* When user select the devices from user web page, then server send the url to the chosen devices .
	* If, the URL valid, then broadcast `username` event

			{
				uuid : '', /* Device UUID */
				url : '' /* URL */
			}


Supported phones
----
* Android devices only

[img-icon]: https://github.com/kyungw00k/UrlBroadcaster/raw/master/client/android/res/drawable-hdpi/icon.png "Widget Icon" height="96px"
[img-on]: https://github.com/kyungw00k/UrlBroadcaster/raw/master/client/android/res/drawable-hdpi/on.png "Switch On" height="96px"
[img-off]: https://github.com/kyungw00k/UrlBroadcaster/raw/master/client/android/res/drawable-hdpi/off.png "Switch Off" height="96px"
[img-signs]: https://github.com/kyungw00k/UrlBroadcaster/raw/master/client/android/res/drawable-hdpi/signs.png "Widget Configuration" height="96px"
