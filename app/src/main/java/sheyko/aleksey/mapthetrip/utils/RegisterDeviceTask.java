package sheyko.aleksey.mapthetrip.utils;

import android.os.AsyncTask;

public class RegisterDeviceTask extends AsyncTask<String, Void, String> {
    private static final String TAG = RegisterDeviceTask.class.getSimpleName();

    OnGetTripIdListener mCallback;

    // Main Activity will implement this interface
    public interface OnGetTripIdListener {
        public void onIdRetrieved(String tripId);
    }

    public RegisterDeviceTask(OnGetTripIdListener callback){
        mCallback = callback;
    }

    @Override
    protected String doInBackground(String... params) {
//
//        HttpURLConnection urlConnection = null;
//        BufferedReader reader = null;
//
//        // Will contain JSON response
//        String resultJsonStr;
//
//        String mTripId = null;
//        DeviceInfo deviceInfo = new DeviceInfo();
//
//        try {
//            // Construct the URL for the query
//            Uri.Builder builder = new Uri.Builder();
//            builder.scheme("http")
//                    .authority("wsapp.mapthetrip.com")
//                    .appendPath("TrucFuelLog.svc")
//                    .appendPath("TFLRegDeviceandGetTripId")
//                    .appendQueryParameter("DeviceUID", params[0])
//                    .appendQueryParameter("DeviceName", deviceInfo.getModel())
//                    .appendQueryParameter("DeviceType", params[1])
//                    .appendQueryParameter("DeviceManufacturerName", deviceInfo.getManufacturer())
//                    .appendQueryParameter("DeviceModelName", deviceInfo.getModel())
//                    .appendQueryParameter("DeviceModelNumber", Device.MODEL_NUMBER)
//                    .appendQueryParameter("DeviceSystemName", Device.SYSTEM_NAME)
//                    .appendQueryParameter("DeviceSystemVersion", deviceInfo.getAndroidVersion())
//                    .appendQueryParameter("DeviceSoftwareVersion", Device.SOFTWARE_VERSION)
//                    .appendQueryParameter("DevicePlatformVersion", deviceInfo.getAndroidVersion())
//                    .appendQueryParameter("DeviceFirmwareVersion", deviceInfo.getAndroidVersion())
//                    .appendQueryParameter("DeviceOS", Device.SYSTEM_NAME)
//                    .appendQueryParameter("DeviceTimezone", deviceInfo.getTimeZone())
//                    .appendQueryParameter("LanguageUsedOnDevice", deviceInfo.getLocale())
//                    .appendQueryParameter("HasCamera", params[2])
//                    .appendQueryParameter("UserId", Device.USER_ID)
//                    .appendQueryParameter("TripDateTime", deviceInfo.getCurrentDateTime())
//                    .appendQueryParameter("TripTimezone", deviceInfo.getTimeZone())
//                    .appendQueryParameter("UserDefinedTripId", Device.USER_DEFINED_TRIP_ID)
//                    .appendQueryParameter("TripReferenceNumber", Device.REFERENCE_NUMBER)
//                    .appendQueryParameter("EntityId", Device.ENTITY_ID);
//
//            String mUrlString = builder.build().toString();
//            Log.i(TAG, "Service: TFLRegDeviceandGetTripIdResult,\n" +
//                    "Query: " + java.net.URLDecoder.decode(mUrlString, "UTF-8"));
//
//            URL mUrl = new URL(mUrlString);
//
//            // Create the request and open the connection
//            urlConnection = (HttpURLConnection) mUrl.openConnection();
//            urlConnection.setRequestMethod("GET");
//            urlConnection.connect();
//
//            // Read the input stream into a String
//            InputStream inputStream = urlConnection.getInputStream();
//            StringBuilder buffer = new StringBuilder();
//            if (inputStream == null) return null;
//
//            reader = new BufferedReader(new InputStreamReader(inputStream));
//
//            String line;
//            while ((line = reader.readLine()) != null) {
//                buffer.append(line).append("\n");
//            }
//
//            if (buffer.length() == 0) {
//                // Stream was empty.  No point in parsing.
//                return null;
//            }
//            resultJsonStr = buffer.toString();
//
//            Log.i(TAG, "Service: TFLRegDeviceandGetTripIdResult,\n" +
//                    "Result: " + java.net.URLDecoder.decode(resultJsonStr, "UTF-8"));
//
//            try {
//                JSONObject regResultObject = new JSONObject(resultJsonStr);
//                JSONObject regResponse = regResultObject.getJSONObject("TFLRegDeviceandGetTripIdResult");
//
//                String mParseStatus = regResponse.getString("Status");
//                if (mParseStatus.equals("Success")) {
//                    mTripId = regResponse.getString("TripId");
//                }
//            } catch (JSONException e) {
//                Log.e(TAG, e.getMessage());
//            }
//        } catch (IOException e) {
//            Log.e(TAG, "Error ", e);
//            return null;
//        } finally {
//            if (urlConnection != null) {
//                urlConnection.disconnect();
//            }
//            if (reader != null) {
//                try {
//                    reader.close();
//                } catch (final IOException e) {
//                    Log.e(TAG, "Error closing stream", e);
//                }
//            }
//        }
        return "253";
    }

    @Override
    protected void onPostExecute(String mTripId) {
        super.onPostExecute(mTripId);

        mCallback.onIdRetrieved(mTripId);
    }
}
