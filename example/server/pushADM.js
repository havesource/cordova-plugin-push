
// Client ID and Client Secret received from ADM
// For more info, see: https://developer.amazon.com/public/apis/engage/device-messaging/tech-docs/02-obtaining-adm-credentials
const CLIENT_ID = 'amzn1.application-oa2-client.8e838f6629554e26ae3f43a6c663cd60';
const CLIENT_SECRET = '0af96083320f5d70dc4f358cc783ac65a22e78b297ba257df34d5f723f24543f';

// Registration ID, received on device after it registers with ADM server
const REGISTRATION_IDS = ['amzn1.adm-registration.v2.Y29tLmFtYXpvbi5EZXZpY2VNZXNzYWdpbmcuUmVnaXN0cmF0aW9uSWRFbmNyeXB0aW9uS2V5ITEhOE9rZ2h5TXlhVEFFczg2ejNWL3JMcmhTa255Uk5BclhBbE1XMFZzcnU1aFF6cTlvdU5FbVEwclZmdk5oTFBVRXVDN1luQlRSNnRVRUViREdQSlBvSzRNaXVRRUlyUy9NYWZCYS9VWTJUaGZwb3ZVTHhlRTM0MGhvampBK01hVktsMEhxakdmQStOSXRjUXBTQUhNU1NlVVVUVkFreVRhRTBCYktaQ2ZkUFdqSmIwcHgzRDhMQnllVXdxQ2EwdHNXRmFVNklYL0U4UXovcHg0K3Jjb25VbVFLRUVVOFVabnh4RDhjYmtIcHd1ZThiekorbGtzR2taMG95cC92Y3NtZytrcTRPNjhXUUpiZEk3QzFvQThBRTFWWXM2NHkyMjdYVGV5RlhhMWNHS0k9IW5GNEJMSXNleC9xbWpHSU52NnczY0E9PQ'];

// Message payload to be sent to client
const payload = {
  data: {
    message: 'PushPlugin works!!',
    sound: 'beep.wav',
    url: 'http://www.amazon.com',
    timeStamp: new Date().toISOString(),
    foo: 'baz'
  },
  consolidationKey: 'my app',
  expiresAfter: 3600
};

//* ********************************

const https = require('https');
const querystring = require('querystring');

if (CLIENT_ID === '' || CLIENT_SECRET === '' || REGISTRATION_IDS.length === 0) {
  console.log('******************\nSetup Error: \nYou need to edit the pushADM.js file and enter your ADM credentials and device registration ID(s).\n******************');
  process.exit(1);
}

// Get access token from server, and use it to post message to device
getAccessToken(function (accessToken) {
  for (let i = 0; i < REGISTRATION_IDS.length; i++) {
    const registrationID = REGISTRATION_IDS[i];

    postMessage(accessToken, registrationID, payload);
  }
});

// Query OAuth server for access token
// For more info, see: https://developer.amazon.com/public/apis/engage/device-messaging/tech-docs/05-requesting-an-access-token

function getAccessToken (callback) {
  console.log('Requesting access token from server...');

  const credentials = {
    scope: 'messaging:push',
    grant_type: 'client_credentials',
    client_id: CLIENT_ID,
    client_secret: CLIENT_SECRET
  };

  const post_data = querystring.stringify(credentials);

  const post_options = {
    host: 'api.amazon.com',
    port: '443',
    path: '/auth/O2/token',
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
    }
  };

  const req = https.request(post_options, function (res) {
    let data = '';

    res.on('data', function (chunk) {
      data += chunk;
    });

    res.on('end', function () {
      console.log('\nAccess token response:', data);
      const accessToken = JSON.parse(data).access_token;
      callback(accessToken);
    });
  });

  req.on('error', function (e) {
    console.log('\nProblem with access token request: ', e.message);
  });

  req.write(post_data);
  req.end();
}

// Post message payload to ADM server
// For more info, see: https://developer.amazon.com/public/apis/engage/device-messaging/tech-docs/06-sending-a-message

function postMessage (accessToken, registrationID, payload) {
  if (accessToken === undefined || registrationID === undefined || payload === undefined) {
    return;
  }

  console.log('\nSending message...');

  const post_data = JSON.stringify(payload);

  const api_path = '/messaging/registrations/' + registrationID + '/messages';

  const post_options = {
    host: 'api.amazon.com',
    port: '443',
    path: api_path,
    method: 'POST',
    headers: {
      Authorization: 'Bearer ' + accessToken,
      'X-Amzn-Type-Version': 'com.amazon.device.messaging.ADMMessage@1.0',
      'X-Amzn-Accept-Type': 'com.amazon.device.messaging.ADMSendResult@1.0',
      'Content-Type': 'application/json',
      Accept: 'application/json'
    }
  };

  const req = https.request(post_options, function (res) {
    let data = '';

    res.on('data', function (chunk) {
      data += chunk;
    });

    res.on('end', function () {
      console.log('\nSend message response: ', data);
    });
  });

  req.on('error', function (e) {
    console.log('\nProblem with send message request: ', e.message);
  });

  req.write(post_data);
  req.end();
}
