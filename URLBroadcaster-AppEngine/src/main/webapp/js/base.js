//
// Custom Code
//

var init = (function (w) {
    'use strict';

    var API_URL = 'https://' + w.location.host + '/_ah/api'
      , API_KEY = 'AIzaSyA8zZ-iNQGs7Y93OSCPXIocprUYjYndXj0'
      , CLIENT_ID = '506201070116-o6d2moark0f4qulrc3nsh4h0bvfcn0j3.apps.googleusercontent.com'
      , SCOPES = 'https://www.googleapis.com/auth/userinfo.email'
      , RESPONSE_TYPE = 'token id_token'
      , signedIn = false
      ;

    function signIn(mode, callback) {
        $("#spinner").show();
        $("#login").hide();
        $("#logout").hide();
        $("#authLayer").hide();
        gapi.auth.authorize({client_id: CLIENT_ID, scope: SCOPES, immediate: mode, response_type: RESPONSE_TYPE}, callback);
    }

    function userAuthed() {
      var request = gapi.client.oauth2.userinfo.get()
        .execute(function(resp) {
            $("#spinner").hide();

            if (!resp.code) {
                var token = gapi.auth.getToken();
                token.access_token = token.id_token;
                gapi.auth.setToken(token);

                signedIn = true;
                init.target = resp.email;

                $("#logout").show();
                $("#authBtn").text('Sign Out');

                updateRegisteredDeviceTable();
            } else {
                $("#login").show();
                $("#authBtn").text('Sign In');
            }

            $("#authLayer").show();
        });
    }

    function auth() {
        if ( !signedIn ) {
            signIn(false, userAuthed);
        } else {
            gapi.auth.setToken(null);
            init.target = null;
            signedIn = false;

            $("#login").show();
            $("#logout").hide();
            $("#authBtn").text('Sign In');
        }
    }

    function init() {
        gapi.client.setApiKey(API_KEY);

        // must match number of calls to gapi.client.load()
        var apisToLoad = 3;

        var callback = function() {
            if (--apisToLoad == 0) {
                signIn(true, userAuthed);
            }
        };
        gapi.client.load('deviceinfoendpoint', 'v1', callback, API_URL);
        gapi.client.load('messageEndpoint', 'v1', callback, API_URL);
        gapi.client.load('oauth2', 'v2', callback);
    }

    $(document).ready(function() {
        $("#authBtn").click(auth);

        $("#sendButton").click(sendMessage);

        $('.alert .close').on('click', function() {
          $(this).parent().hide();
        });
    });

    return init;
})(window);

//
// Use generated functions
//

function showSuccess() {
    $("#alertArea").hide();
    $("#successArea").show();
    $("#successArea").fadeOut(1000);
}

function showError(errorHtml) {
    $("#alertArea").removeClass('alert-danger alert-info alert-success').addClass('alert-danger');
    $("#alertContentArea").html(errorHtml);
    $("#alertArea").show();
}

function showInfo(infoHtml) {
    $("#alertArea").removeClass('alert-danger alert-info alert-success').addClass('alert-info');
    $("#alertContentArea").html(infoHtml);
    $("#alertArea").show();
}

// Specialized function for checking error responses; it's needed to work around bugs in the
// current version of the DevAppServer
function checkErrorResponse(result, rawResult) {
    // Generally, !result should never occur. It's a bug with the DevAppServer. It will be fixed
    // in a forthcoming version of App Engine.
    if (!result) {
      try {
        // This is some special exception-handling code to deal with the DevAppServer not
        // handling empty (void responses) from an endpoint method in App Engine 1.7.5.
        result = JSON.parse(rawResult);
      } catch (err) {
      // This is a spurious error. Return true.
      if (rawResult.indexOf("Error 400 Failed to parse JSON request: Unexpected character") != -1) {
          return {isError: false};
      } else {
        if (rawResult == "") {
          // Empty result; perhaps we're disconnected?
          return {isError: true, errorMessage: "No response from server! Is it up and running?" };
    
        } else {
          // Unknown error; this shouldn't really happen
          var safeErrorHtml = $('<div/>').text(rawResult).html();
          return {isError: true, errorMessage: safeErrorHtml};
        }
      }
      }
    }
    
    if (result.error) {
      // This is really what should happen; if there's an error, a result.error object will
      // exist
          var safeErrorHtml = $('<div/>').text(result.error.message).html();
      return {isError: true, errorMessage: safeErrorHtml};
    } else if (result[0] && result[0].error) {
      // This is yet another hack; the DevAppServer incorrectly returns an array of error
      // responses in the case where the endpoint method throws an app-level exception.
      // Again, this will be fixed in a forthcoming version of App Engine.
          var safeErrorHtml = $('<div/>').text(result[0].error.message).html();
      return {isError: true, errorMessage: safeErrorHtml};
    }
    
    return {isError: false};
}

function generateRegDeviceTable(deviceInfoItems) {
    items = deviceInfoItems.items;
    
    var htmlString = "<thead>" + "<tr>"
          + "<th style='min-width:150px'>Device Name</th>"
          + "<th>Timestamp</th>"
          + "</tr>" + "</thead>";
          
    if (!items || items.length == 0) {
      htmlString += "<tbody><tr><td colspan=\"2\">There are no registered devices</td></tr></tbody>";
    } else {
      htmlString += "<tbody>";
    
      for (var i = 0; i < items.length; i++) {
        item = items[i];
        htmlString += "<tr>";
        if (item.deviceInformation) {
          htmlString += "<td class='devicenameCol'>" + item.deviceInformation
              + "</td>";
        } else {
          htmlString += "<td class='devicenameCol'>" + "(unknown)"
              + "</td>";
        }
    
        if (item.timestamp) {
          var timestampNumberic = new Number(item.timestamp);
          var date = new Date(timestampNumberic);
    
    
            htmlString += "<td>" + date.toString() + "</td>";
          } else {
            htmlString += "<td>" + "(unknown)"
            + "</td>";
          }
    
        htmlString += "</tr>";
      }
    
      htmlString += "</tbody>";
    }
    $("#regDevicesTable").html(htmlString);

    if ( init.target ) {
        setTimeout(function(){updateRegisteredDeviceTable();}, 2000);
    }
}

function updateRegisteredDeviceTable() {
    if ( !init.target ) {
        generateRegDeviceTable(null);
        return ;
    }

    // listDeviceInfo does not accept a null argument, so we pass in an empty map. Will be fixed in a future
    // version of the DevAppServer

    var filter = {};

    if ( init.target ) {
        filter.emailAddress = init.target;
    }

    gapi.client.deviceinfoendpoint
        .listDeviceInfo(filter)
        .execute(
            function(deviceInfoItems, deviceInfoItemsRaw) {
              errorResult = checkErrorResponse(deviceInfoItems, deviceInfoItemsRaw);
              if (errorResult.isError) {
                showError("There was a problem contacting the server when attempting to list the registered devices. "
                    + "Please refresh the page and try again in a short while. Here's the error information:<br/> "
                    + errorResult.errorMessage);
              } else {
                generateRegDeviceTable(deviceInfoItems);
              }
            });
}

function handleMessageResponse(data, dataRaw) {
    errorResult = checkErrorResponse(data, dataRaw);
    if (!errorResult.isError) {
        showSuccess();
    } else {
    showError("There was a problem when attempting to send the message using the server at "
        + API_URL + ". " + " Is your API Key in MessageEndpoint.java "
        + "(in your App Engine project) set correctly? Here's the error information:<br/>"
            + errorResult.errorMessage);
    }
}

function sendMessage() {
    var message = $("#inputMessage").val();
    
    if (message == "") {
      showInfo("URL must not be empty!");
      return ;
    }

    if ( init.target ) {
        // TODO URL Validation
        gapi.client.messageEndpoint
            .sendMessage({"message": message}, init.target)
            .execute(handleMessageResponse);
    }
}